package com.TominoCZ.FBP.particle;

import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.util.vector.Vector2f;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.TominoCZ.FBP.util.FBPMathUtil;
import com.TominoCZ.FBP.util.FBPRenderUtil;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticleDigging extends EntityDiggingFX {
	private final IBlockState sourceState;

	Minecraft mc;

	double scaleAlpha, prevParticleScale, prevParticleAlpha, prevMotionX, prevMotionZ;
	float prevGravity;

	boolean modeDebounce = false, wasFrozen = false, destroyed = false;

	boolean dying = false, killToggle = false;

	FBPVector3d rotStep;

	FBPVector3d rot;
	FBPVector3d prevRot;

	double endMult = 0.75;

	Vector2f uvMin;
	Vector2f uvMax;

	protected FBPParticleDigging(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, float R, float G, float B, IBlockState state, @Nullable EnumFacing facing,
			float scale, @Nullable TextureAtlasSprite texture) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);

		this.particleRed = R;
		this.particleGreen = G;
		this.particleBlue = B;

		mc = Minecraft.getMinecraft();

		rot = new FBPVector3d();
		prevRot = new FBPVector3d();

		createRotationMatrix();

		try {
			FBP.setSourcePos.invokeExact((EntityDiggingFX) this, new BlockPos(xCoordIn, yCoordIn, zCoordIn));
		} catch (Throwable e1) {
			e1.printStackTrace();
		}

		if (scale > -1)
			particleScale = scale;

		if (scale < -1) {
			if (facing != null) {
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

		particleScale = (float) (FBP.scaleMult * (FBP.randomizedScale ? particleScale : 1));
		particleMaxAge = (int) FBP.random.nextDouble(FBP.minAge, FBP.maxAge + 0.5);

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
						int[] data = quads.get(0).getVertexData();

						float u1 = Float.intBitsToFloat(data[4]);
						float v1 = Float.intBitsToFloat(data[5]);

						float u2 = Float.intBitsToFloat(data[14 + 4]);
						float v2 = Float.intBitsToFloat(data[14 + 5]);

						uvMin = new Vector2f(u1, v1);
						uvMax = new Vector2f(u2, v2);

						if (!state.getBlock().isNormalCube()
								|| (b.equals(Blocks.grass) && facing.equals(EnumFacing.UP)))
							multiplyColor(state.getBlock(), new BlockPos(xCoordIn, yCoordIn, zCoordIn));
					}
				} catch (Exception e) {
				}
			}

			if (particleIcon == null || particleIcon.getIconName().equals("missingno")) {
				particleIcon = blockModelShapes.getTexture(state);

				if (particleIcon != null) {
					uvMin = new Vector2f(particleIcon.getMinU(), particleIcon.getMinV());
					uvMax = new Vector2f(particleIcon.getMaxU(), particleIcon.getMaxV());
				}
			}
		} else
			this.particleIcon = texture;

		if (!state.getBlock().isNormalCube())
			multiplyColor(state.getBlock(), new BlockPos(xCoordIn, yCoordIn, zCoordIn));

		if (FBP.randomFadingSpeed)
			endMult = MathHelper.clamp_double(FBP.random.nextDouble(0.5, 0.9), 0.55, 0.8);

		prevGravity = particleGravity;

		multipleParticleScaleBy(1);
	}

	@Override
	public EntityFX multipleParticleScaleBy(float scale) {
		EntityFX p = super.multipleParticleScaleBy(scale);

		float f = particleScale / 10;

		this.setEntityBoundingBox(new AxisAlignedBB(posX - f, posY, posZ - f, posX + f, posY + 2 * f, posZ + f));

		return p;
	}

	public EntityFX MultiplyVelocity(float multiplier) {
		this.motionX *= multiplier;
		this.motionY = (this.motionY - 0.10000000149011612D) * (multiplier / 2) + 0.10000000149011612D;
		this.motionZ *= multiplier;
		return this;
	}

	protected void multiplyColor(Block b, @Nullable BlockPos pos) {
		if (b == null || pos == null)
			return;

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
		boolean allowedToMove = MathHelper.abs((float) motionX) > 0.0001D || MathHelper.abs((float) motionZ) > 0.0001D;

		if (!FBP.frozen && FBP.bounceOffWalls && !mc.isGamePaused() && particleAge > 0) {
			if (!wasFrozen && allowedToMove) {
				boolean xCollided = prevPosX == posX;
				boolean zCollided = prevPosZ == posZ;

				if (xCollided)
					motionX = -prevMotionX * 0.625f;
				if (zCollided)
					motionZ = -prevMotionZ * 0.625f;

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
			if (!killToggle) {
				if (!FBP.randomRotation) {
					if (!modeDebounce) {
						modeDebounce = true;

						rot.z = 0;

						calculateYAngle();
					}

					if (allowedToMove) {
						double x = MathHelper.abs((float) (rotStep.x * getMult()));

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

						rot.z = FBP.random.nextDouble(30, 400);
					}

					if (allowedToMove)
						rot.add(rotStep.multiply(getMult()));
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
				if (!isCollided)
					motionY -= 0.04D * particleGravity;

				moveEntity(motionX, motionY, motionZ);

				if (isCollided && FBP.restOnFloor) {
					rot.x = (float) Math.round(rot.x / 90) * 90;
					rot.z = (float) Math.round(rot.z / 90) * 90;
				}

				if (MathHelper.abs((float) motionX) > 0.00001D)
					prevMotionX = motionX;
				if (MathHelper.abs((float) motionZ) > 0.00001D)
					prevMotionZ = motionZ;

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
									this.isCollided = false;
							}
						}
					}
				}

				if (FBP.waterPhysics) {
					if (isInWater()) {
						handleWaterMovement();

						if (FBP.INSTANCE.doesMaterialFloat(this.sourceState.getBlock().getMaterial())) {
							motionY = 0.11f + (particleScale / 1.25f) * 0.02f;
						} else {
							motionX *= 0.932515086137662D;
							motionZ *= 0.932515086137662D;
							particleGravity = 0.35f;

							motionY *= 0.85f;
						}

						if (!FBP.randomRotation)
							calculateYAngle();

						if (isCollided)
							isCollided = false;
					} else {
						particleGravity = prevGravity;
					}
				}

				if (isCollided) {
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
	}

	@Override
	public boolean isInWater() {
		double scale = particleScale / 20;

		int minX = MathHelper.floor_double(posX - scale);
		int maxX = MathHelper.ceiling_double_int(posX + scale);

		int minY = MathHelper.floor_double(posY - scale);
		int maxY = MathHelper.ceiling_double_int(posY + scale);

		int minZ = MathHelper.floor_double(posZ - scale);
		int maxZ = MathHelper.ceiling_double_int(posZ + scale);

		if (worldObj.isAreaLoaded(new StructureBoundingBox(minX, minY, minZ, maxX, maxY, maxZ), true)) {
			for (int x = minX; x < maxX; ++x) {
				for (int y = minY; y < maxY; ++y) {
					for (int z = minZ; z < maxZ; ++z) {
						IBlockState block = worldObj.getBlockState(new BlockPos(x, y, z));

						if (block.getBlock().getMaterial() == Material.water) {
							double d0 = (double) ((float) (y + 1)
									- BlockLiquid.getLiquidHeightPercent(block.getValue(BlockLiquid.LEVEL)));

							if (posY <= d0)
								return true;
						}
					}
				}
			}
		}

		return false;
	}

	@Override
	public void moveEntity(double x, double y, double z) {
		double X = x;
		double Y = y;
		double Z = z;

		List<AxisAlignedBB> list = this.worldObj.getCollidingBoundingBoxes(this,
				this.getEntityBoundingBox().addCoord(x, y, z));

		for (AxisAlignedBB axisalignedbb : list) {
			y = axisalignedbb.calculateYOffset(this.getEntityBoundingBox(), y);
		}

		this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));

		for (AxisAlignedBB axisalignedbb : list) {
			x = axisalignedbb.calculateXOffset(this.getEntityBoundingBox(), x);
		}

		this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));

		for (AxisAlignedBB axisalignedbb : list) {
			z = axisalignedbb.calculateZOffset(this.getEntityBoundingBox(), z);
		}

		this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));

		// RESET
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
		this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
		this.posY = axisalignedbb.minY;
		this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;

		this.isCollided = y != Y && Y < 0.0D;

		if (!FBP.lowTraction && !FBP.bounceOffWalls) {
			if (x != X)
				motionX *= 0.699999988079071D;
			if (z != Z)
				motionZ *= 0.699999988079071D;
		}
	}

	@Override
	public void renderParticle(WorldRenderer buf, Entity entityIn, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;
		if (FBPKeyBindings.FBPSweep.isKeyDown() && !killToggle)
			killToggle = true;

		float minX = 0, maxX = 0, minY = 0, maxY = 0;

		float f4 = particleScale;

		if (particleIcon != null) {
			if (uvMin == null && uvMax == null) {
				minX = particleIcon.getInterpolatedU((particleTextureJitterX) / 4 * 16);
				minY = particleIcon.getInterpolatedV((particleTextureJitterY) / 4 * 16);

				maxX = particleIcon.getInterpolatedU((particleTextureJitterX + 1) / 4 * 16);
				maxY = particleIcon.getInterpolatedV((particleTextureJitterY + 1) / 4 * 16);
			} else {
				int size = 4;

				float sizeX = uvMax.x - uvMin.x;
				float sizeY = uvMax.y - uvMin.y;

				float startX = (particleTextureJitterX + 1) * 4 - size;
				float startY = (particleTextureJitterY + 1) * 4 - size;

				minX = uvMin.x + (sizeX / 16 * startX);
				minY = uvMin.y + (sizeY / 16 * startY);

				maxX = uvMax.x - (sizeX / 16 * (16 - startX - size));
				maxY = uvMax.y - (sizeY / 16 * (16 - startY - size));
			}
		} else {
			minX = (0 + particleTextureJitterX / 4) / 16;
			minY = (0 + particleTextureJitterY / 4) / 16;

			maxX = minX + 0.015609375F;
			maxY = minY + 0.015609375F;
		}

		float f5 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

		int i = this.getBrightnessForRender(partialTicks);

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		if ((dying && !FBP.frozen) || (FBP.frozen && killToggle)) {
			f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

			alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);
		}

		if (FBP.restOnFloor)
			f6 += f4 / 10;

		FBPVector3d smoothRot = new FBPVector3d(0, 0, 0);

		if (FBP.rotationMult > 0) {
			smoothRot.y = rot.y;
			smoothRot.z = rot.z;

			if (!FBP.randomRotation)
				smoothRot.x = rot.x;

			// SMOOTH ROTATION
			if (!FBP.frozen) {
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
		FBPRenderUtil.renderCubeShaded_S(buf,
				new Vector2f[] { new Vector2f(maxX, maxY), new Vector2f(maxX, minY), new Vector2f(minX, minY),
						new Vector2f(minX, maxY) },
				f5, f6, f7, f4 / 10, smoothRot, i >> 16 & 65535, i & 65535, particleRed, particleGreen, particleBlue,
				alpha);
	}

	private void createRotationMatrix() {
		double rx0 = FBP.random.nextDouble();
		double ry0 = FBP.random.nextDouble();
		double rz0 = FBP.random.nextDouble();

		rotStep = new FBPVector3d(rx0 > 0.5 ? 1 : -1, ry0 > 0.5 ? 1 : -1, rz0 > 0.5 ? 1 : -1);

		rot.copyFrom(rotStep);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float partialTicks) {
		AxisAlignedBB box = getEntityBoundingBox();

		if (this.worldObj.isBlockLoaded(new BlockPos(posX, 0, posZ))) {
			double d0 = (box.maxY - box.minY) * 0.66D;
			double k = this.posY + d0 - 0.01;
			return this.worldObj.getCombinedLight(new BlockPos(posX, k, posZ), 0);
		} else {
			return 0;
		}
	}

	@SideOnly(Side.CLIENT)
	public static class Factory implements IParticleFactory {
		@Override
		public EntityFX getEntityFX(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
				double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
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
		return Math.sqrt(motionX * motionX + motionZ * motionZ) * (FBP.randomRotation ? 200 : 500) * FBP.rotationMult;
	}
}