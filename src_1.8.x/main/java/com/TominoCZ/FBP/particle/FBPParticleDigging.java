package com.TominoCZ.FBP.particle;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.TominoCZ.FBP.util.FBPMathUtil;
import com.TominoCZ.FBP.util.FBPRenderUtil;
import com.TominoCZ.FBP.vector.FBPVector3d;
import com.sun.javafx.geom.Vec2f;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

@SideOnly(Side.CLIENT)
public class FBPParticleDigging extends EntityDiggingFX implements IFBPShadedParticle {
    private final IBlockState sourceState;

    Minecraft mc;

    int vecIndex;

    double scaleAlpha, prevParticleScale, prevParticleAlpha, prevMotionX, prevMotionZ;

    boolean modeDebounce = false, wasFrozen = false, destroyed = false;

    boolean spawned = false, dying = false, killToggle = false;

    FBPVector3d rotStep;

    FBPVector3d rot;
    FBPVector3d prevRot;

    Vec2f[] par;

    double endMult = 0.75;

    long tick = 0;

    protected FBPParticleDigging(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
                                 double ySpeedIn, double zSpeedIn, float R, float G, float B, IBlockState state, @Nullable EnumFacing facing,
                                 float scale, @Nullable TextureAtlasSprite texture) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);
        mc = Minecraft.getMinecraft();

        this.particleRed = R;
        this.particleGreen = G;
        this.particleBlue = B;

        try {
            FBP.setSourcePos.invokeExact((EntityDiggingFX) this, new BlockPos(xCoordIn, yCoordIn, zCoordIn));
        } catch (Throwable e1) {
            e1.printStackTrace();
        }

        rot = new FBPVector3d();
        prevRot = new FBPVector3d();

        createRotationMatrix();

        if (scale > -1)
            particleScale = scale;

        if (scale < -1) {
            if (facing != null) {
                tick = 0;
                if (facing == EnumFacing.UP && FBP.smartBreaking) {
                    motionX *= 1.5D;
                    motionY *= 0.1D;
                    motionZ *= 1.5D;

                    double particleSpeed = Math.sqrt(motionX * motionX + motionZ * motionZ);

                    Vec3 vec = mc.thePlayer.getLookVec();

                    double x = FBPMathUtil.add(vec.xCoord, 0.01D);
                    double z = FBPMathUtil.add(vec.zCoord, 0.01D);

                    motionX = x * particleSpeed;
                    motionZ = z * particleSpeed;
                }
            }
        }

        if (modeDebounce = !FBP.randomRotation) {
            this.rot.zero();
            calculateYAngle();
        }

        this.sourceState = state;

        Block b = state.getBlock();

        particleGravity = (float) (b.blockParticleGravity * FBP.gravityMult);

        particleScale *= FBP.scaleMult * 2.0F;
        particleMaxAge = (int) FBP.random.nextDouble(FBP.minAge, FBP.maxAge + 0.5);
        // = 0.7F + (0.25F * mc.gameSettings.gammaSetting);

        scaleAlpha = particleScale * 0.82;

        destroyed = facing == null;

        if (texture == null) {
            BlockModelShapes blockModelShapes = mc.getBlockRendererDispatcher().getBlockModelShapes();

            // GET THE TEXTURE OF THE BLOCK FACE
            if (!destroyed) {
                try {
                    IBakedModel model = blockModelShapes.getModelForState(state);
                    this.particleIcon = model.getParticleTexture();

                    List<BakedQuad> quads = model.getFaceQuads(facing);

                    if (quads != null && !quads.isEmpty()) {
                        //TODO
                        int[] data = quads.get(0).getVertexData();

                        float u1 = Float.intBitsToFloat(data[4]);
                        float v1 = Float.intBitsToFloat(data[5]);

                        float u2 = Float.intBitsToFloat(data[14 + 4]);
                        float v2 = Float.intBitsToFloat(data[14 + 5]);

                        if (!state.getBlock().isNormalCube() || (b.equals(Blocks.grass) && facing.equals(EnumFacing.UP)))
                            multiplyColor(state.getBlock(), new BlockPos(xCoordIn, yCoordIn, zCoordIn));
                    }
                } catch (Exception e) {
                }
            }

            if (particleIcon == null || particleIcon.getIconName() == "missingno")
                this.setParticleIcon(blockModelShapes.getTexture(state));
        } else
            this.particleIcon = texture;

        if (!state.getBlock().isNormalCube())
            multiplyColor(state.getBlock(), new BlockPos(xCoordIn, yCoordIn, zCoordIn));

        if (FBP.randomFadingSpeed)
            endMult = MathHelper.clamp_double(FBP.random.nextDouble(0.4151, 0.9875), 0.63875, 0.9875);
    }

    public EntityFX MultiplyVelocity(float multiplier) {
        this.motionX *= multiplier;
        this.motionY = (this.motionY - 0.10000000149011612D) * (multiplier / 2) + 0.10000000149011612D;
        this.motionZ *= multiplier;
        return this;
    }

    protected void multiplyColor(Block b, @Nullable BlockPos pos) {
        int i = b.colorMultiplier(worldObj, pos, 0);

        this.particleRed *= (i >> 16 & 255) / 255.0F;
        this.particleGreen *= (i >> 8 & 255) / 255.0F;
        this.particleBlue *= (i & 255) / 255.0F;
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public void onUpdate() {
        if (!spawned)
            tick++;

        if (!FBP.frozen && FBP.bounceOffWalls && !mc.isGamePaused()) {
            if (!wasFrozen && spawned && (MathHelper.abs((float) motionX) > 0.00001D)) {
                boolean xCollided = (prevPosX == posX);
                boolean zCollided = (prevPosZ == posZ);

                if (xCollided)
                    motionX = -prevMotionX;
                if (zCollided)
                    motionZ = -prevMotionZ;

                if (!FBP.randomRotation && (xCollided || zCollided))
                    calculateYAngle();
            } else
                wasFrozen = false;
        }
        if (FBP.frozen && FBP.bounceOffWalls && !wasFrozen)
            wasFrozen = true;

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        prevRot.copyFrom(rot);

        prevParticleAlpha = particleAlpha;
        prevParticleScale = particleScale;

        if (!mc.isGamePaused() && (!FBP.frozen || killToggle)) {
            boolean allowedToMove = MathHelper.abs((float) motionX) > 0.00001D;

            if (!killToggle) {
                if (!FBP.randomRotation) {
                    if (!modeDebounce) {
                        modeDebounce = true;

                        rot.z = 0;

                        calculateYAngle();
                    }

                    if (allowedToMove) {
                        double x = MathHelper.abs((float) (rotStep.x * getMult() * FBP.rotationMult));

                        if (motionX > 0) {
                            if (motionZ > 0)
                                rot.x -= x;
                            else if (motionZ < 0)
                                rot.x += x;
                        } else if (motionX < 0) {
                            if (motionZ < 0)
                                rot.x += x;
                            else if (motionZ > 0) {
                                rot.x -= x;
                            }
                        }
                    }
                } else {
                    if (modeDebounce) {
                        modeDebounce = false;

                        createRotationMatrix();
                    }

                    if (allowedToMove)
                        rot.add(rotStep.multiply(getMult() * FBP.rotationMult));
                }
            }

            if (!FBP.infiniteDuration)
                particleAge++;

            if (this.particleAge >= this.particleMaxAge || killToggle) {
                if (!dying)
                    dying = true;

                particleScale *= 0.887654321F * endMult;

                if (particleAlpha > 0.01 && particleScale <= scaleAlpha)
                    particleAlpha *= 0.68752F * endMult;

                if (particleAlpha <= 0.01)
                    setDead();
            }

            if (!killToggle) {
                if (onGround)
                    motionY = -0.08322508594922069D;
                else
                    motionY -= 0.04D * particleGravity;

                if (allowedToMove)
                    moveEntity(motionX, motionY, motionZ, false);
                else
                    moveEntity(0, motionY, 0, true);

                if (MathHelper.abs((float) motionX) > 0.00001D) {
                    prevMotionX = motionX;
                    prevMotionZ = motionZ;
                }

                if (allowedToMove) {
                    motionX *= 0.9800000190734863D;
                    motionZ *= 0.9800000190734863D;
                }

                motionY *= 0.9800000190734863D;

                // PHYSICS
                if (FBP.entityCollision) {
                    List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, this.getEntityBoundingBox());

                    for (Entity entityIn : list) {
                        if (!entityIn.noClip) {
                            double d0 = this.posX - entityIn.posX;
                            double d1 = this.posZ - entityIn.posZ;
                            double d2 = MathHelper.abs_max(d0, d1);

                            if (d2 >= 0.009999999776482582D) {
                                d2 = Math.sqrt(d2);
                                d0 /= d2;
                                d1 /= d2;

                                double d3 = 1.0D / d2;

                                if (d3 > 1.0D)
                                    d3 = 1.0D;

                                this.motionX += d0 * d3 / 20;
                                this.motionZ += d1 * d3 / 20;

                                if (!FBP.randomRotation)
                                    calculateYAngle();
                                if (!FBP.frozen)
                                    this.onGround = false;
                            }
                        }
                    }
                }

                if (onGround) {
                    if (FBP.lowTraction) {
                        motionX *= 0.932515086137662D;
                        motionZ *= 0.932515086137662D;
                    } else {
                        motionX *= 0.6654999988079071D;
                        motionZ *= 0.6654999988079071D;
                    }
                }
            }
        }

        if (destroyed || !spawned && tick >= 2)
            spawned = true;
    }

    public void moveEntity(double x, double y, double z, boolean YOnly) {
        double X = x;
        double Y = y;
        double Z = z;
        double d0 = y;

        List<AxisAlignedBB> list1 = this.worldObj.getCollidingBoundingBoxes(this, this.getEntityBoundingBox().addCoord(x, y, z));

        for (AxisAlignedBB axisalignedbb1 : list1) {
            y = axisalignedbb1.calculateYOffset(this.getEntityBoundingBox(), y);
        }

        this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));

        if (!YOnly) {
            for (AxisAlignedBB axisalignedbb2 : list1) {
                x = axisalignedbb2.calculateXOffset(this.getEntityBoundingBox(), x);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));

            for (AxisAlignedBB axisalignedbb13 : list1) {
                z = axisalignedbb13.calculateZOffset(this.getEntityBoundingBox(), z);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
        }

        this.resetPositionToBB();

        this.onGround = y != Y && d0 < 0.0D;

        if (!FBP.lowTraction && !FBP.bounceOffWalls) {
            if (x != X)
                motionX *= 0.699999988079071D;
            if (z != Z)
                motionZ *= 0.699999988079071D;
        }
    }

    private void resetPositionToBB() {
        this.posX = (this.getEntityBoundingBox().minX + this.getEntityBoundingBox().maxX) / 2.0D;
        this.posY = this.getEntityBoundingBox().minY;
        this.posZ = (this.getEntityBoundingBox().minZ + this.getEntityBoundingBox().maxZ) / 2.0D;
    }

    @Override
    public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
                               float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

    }

    private void createRotationMatrix() {
        double rx0 = FBP.random.nextDouble();
        double ry0 = FBP.random.nextDouble();
        double rz0 = FBP.random.nextDouble();

        rotStep = new FBPVector3d(rx0 > 0.5 ? 1 : -1, ry0 > 0.5 ? 1 : -1, rz0 > 0.5 ? 1 : -1);

        rot.copyFrom(rotStep);
    }

    @Override
    public int getBrightnessForRender(float p_189214_1_) {
        int i = super.getBrightnessForRender(p_189214_1_);
        int j = 0;

        if (this.worldObj.isBlockLoaded(new BlockPos(posX, posY, posZ))) {
            j = this.worldObj.getCombinedLight(new BlockPos(posX, posY, posZ), 0);
        }

        return i == 0 ? j : i;
    }

    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory {
        @Override
        public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
            return (new FBPParticleDigging(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, 1, 1, 1,
                    Block.getStateById(p_178902_15_[0]), null, -1, null));
        }
    }

    private void calculateYAngle() {
        double angleSin = Math.toDegrees(Math.asin(motionX / Math.sqrt(motionX * motionX + motionZ * motionZ)));

        if (motionX > 0) {
            if (motionZ > 0)
                rot.y = -angleSin;
            else
                rot.y = angleSin;
        } else {
            if (motionZ > 0)
                rot.y = -angleSin;
            else
                rot.y = angleSin;
        }
    }

    double getMult() {
        if (FBP.randomRotation) {
            if (destroyed)
                return Math.sqrt(motionX * motionX + motionZ * motionZ) * 200;
            else
                return Math.sqrt(motionX * motionX + motionZ * motionZ) * 300;
        } else {
            if (FBP.lowTraction) {
                if (destroyed)
                    return Math.sqrt(motionX * motionX + motionZ * motionZ) * 300;
                else
                    return Math.sqrt(motionX * motionX + motionZ * motionZ) * 1150;
            } else {
                if (destroyed)
                    return Math.sqrt(motionX * motionX + motionZ * motionZ) * 300;
                else
                    return Math.sqrt(motionX * motionX + motionZ * motionZ) * 1000;
            }
        }
    }

    @Override
    public void renderShadedParticle(WorldRenderer buf, float partialTicks) {
        if (!FBP.isEnabled() && particleMaxAge != 0)
            particleMaxAge = 0;
        if (FBPKeyBindings.FBPSweep.isKeyDown() && !killToggle)
            killToggle = true;

        float f = 0, f1 = 0, f2 = 0, f3 = 0;

        float f4 = particleScale;

        if (particleIcon != null) {
            if (!FBP.cartoonMode) {
                f = particleIcon.getInterpolatedU(particleTextureJitterX / 4 * 16);
                f2 = particleIcon.getInterpolatedV(particleTextureJitterY / 4 * 16);
            }

            f1 = particleIcon.getInterpolatedU((particleTextureJitterX + 1) / 4 * 16);
            f3 = particleIcon.getInterpolatedV((particleTextureJitterY + 1) / 4 * 16);
        } else {
            f = (particleTextureIndexX + particleTextureJitterX / 4) / 16;
            f1 = f + 0.015609375F;
            f2 = (particleTextureIndexY + particleTextureJitterY / 4) / 16;
            f3 = f2 + 0.015609375F;
        }

        float f5 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
        float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
        float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

        int i = this.getBrightnessForRender(partialTicks);

        par = new Vec2f[]{new Vec2f(f1, f3), new Vec2f(f1, f2), new Vec2f(f, f2), new Vec2f(f, f3)};

        float alpha = particleAlpha;

        // SMOOTH TRANSITION
        if ((dying && FBP.smoothTransitions && !FBP.frozen) || (FBP.frozen && killToggle && FBP.smoothTransitions)) {
            f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

            alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);
        }

        FBPVector3d smoothRot = new FBPVector3d(0, 0, 0);

        if (FBP.rotationMult > 0) {
            smoothRot.y = rot.y;
            smoothRot.z = rot.z;

            if (!FBP.randomRotation)
                smoothRot.x = rot.x;

            // SMOOTH ROTATION
            if (FBP.smoothTransitions && !FBP.frozen) {
                FBPVector3d vec = rot.partialVec(prevRot, partialTicks);

                if (FBP.randomRotation) {
                    smoothRot.y = vec.y;
                    smoothRot.z = vec.z;
                } else {
                    smoothRot.x = vec.x;
                }
            }
        }

        // RENDER
        if (spawned)
            FBPRenderUtil.renderCubeShaded_S(buf, par, f5, f6, f7, f4 / 20, smoothRot, i >> 16 & 65535, i & 65535,
                    particleRed, particleGreen, particleBlue, alpha);
    }
}