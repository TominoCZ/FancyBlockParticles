package com.TominoCZ.FBP.particle;

import java.util.List;

import javax.annotation.Nullable;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.util.FBPRenderUtil;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticleSnow extends ParticleDigging implements IFBPShadedParticle {
	private final IBlockState sourceState;

	Minecraft mc;

	double scaleAlpha, prevParticleScale, prevParticleAlpha;

	FBPVector3d rotStep;

	FBPVector3d prevRot;
	FBPVector3d rot;

	boolean modeDebounce = false;

	double endMult = 1;

	float brightness = 1;

	Vec2f[] par;

	public FBPParticleSnow(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, IBlockState state) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);

		try {
			FBP.setSourcePos.invokeExact((ParticleDigging) this, new BlockPos(xCoordIn, yCoordIn, zCoordIn));
		} catch (Throwable e1) {
			e1.printStackTrace();
		}

		rot = new FBPVector3d();
		prevRot = new FBPVector3d();

		createRotationMatrix();

		this.motionX = xSpeedIn;
		this.motionY = -ySpeedIn;
		this.motionZ = zSpeedIn;
		this.particleGravity = 1f;
		sourceState = state;

		mc = Minecraft.getMinecraft();

		particleScale *= FBP.random.nextDouble(FBP.scaleMult - 0.25f, FBP.scaleMult + 0.25f);
		particleMaxAge = (int) FBP.random.nextDouble(250, 300);
		this.particleRed = this.particleGreen = this.particleBlue = 1;

		scaleAlpha = particleScale * 0.75;

		this.particleAlpha = 0f;
		this.particleScale = 0f;

		this.canCollide = true;

		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.7, 1);
	}

	private void createRotationMatrix() {
		double rx = FBP.random.nextDouble();
		double ry = FBP.random.nextDouble();
		double rz = FBP.random.nextDouble();

		rotStep = new FBPVector3d(rx > 0.5 ? 1 : -1, ry > 0.5 ? 1 : -1, rz > 0.5 ? 1 : -1);

		rot.copyFrom(rotStep);
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {

	}

	public Particle MultiplyVelocity(float multiplier) {
		this.motionX *= multiplier;
		this.motionY = (this.motionY - 0.10000000149011612D) * (multiplier / 2) + 0.10000000149011612D;
		this.motionZ *= multiplier;
		return this;
	}

	@Override
	protected void multiplyColor(@Nullable BlockPos p_187154_1_) {
		int i = mc.getBlockColors().colorMultiplier(this.sourceState, this.world, p_187154_1_, 0);
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
		prevRot.copyFrom(rot);

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;

		if (!mc.isGamePaused()) {
			particleAge++;

			if (posY < mc.player.posY - (mc.gameSettings.renderDistanceChunks * 16))
				setExpired();

			rot.add(rotStep.multiply(FBP.rotationMult * 5));

			if (this.particleAge >= this.particleMaxAge) {
				if (FBP.randomFadingSpeed)
					particleScale *= 0.75F * endMult;
				else
					particleScale *= 0.75F;

				if (particleAlpha > 0.01 && particleScale <= scaleAlpha) {
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.65F * endMult;
					else
						particleAlpha *= 0.65F;
				}

				if (particleAlpha <= 0.01)
					setExpired();
			} else {
				if (particleScale < 1) {
					if (FBP.randomFadingSpeed)
						particleScale += 0.075F * endMult;
					else
						particleScale += 0.075F;

					if (particleScale > 1)
						particleScale = 1;
				}

				if (particleAlpha < 1) {
					if (FBP.randomFadingSpeed)
						particleAlpha += 0.045F * endMult;
					else
						particleAlpha += 0.045F;

					if (particleAlpha > 1)
						particleAlpha = 1;
				}
			}

			if (world.getBlockState(new BlockPos(posX, posY, posZ)).getMaterial().isLiquid())
				setExpired();

			motionY -= 0.04D * this.particleGravity;

			move(motionX, motionY, motionZ);

			if (onGround && FBP.restOnFloor) {
				rot.x = (float) Math.round(rot.x / 90) * 90;
				rot.z = (float) Math.round(rot.z / 90) * 90;
			}

			motionX *= 0.9800000190734863D;

			if (motionY < -0.2) // minimal motionY
				motionY *= 0.7500000190734863D;

			motionZ *= 0.9800000190734863D;

			if (onGround) {
				motionX *= 0.680000190734863D;
				motionZ *= 0.6800000190734863D;

				rotStep = rotStep.multiply(0.85);

				this.particleAge += 2;
			}
		}
	}

	@Override
	public void move(double x, double y, double z) {
		double X = x;
		double Y = y;
		double Z = z;

		List<AxisAlignedBB> list = this.world.getCollisionBoxes((Entity) null, this.getBoundingBox().addCoord(x, y, z));

		for (AxisAlignedBB axisalignedbb : list) {
			y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
		}

		this.setBoundingBox(this.getBoundingBox().offset(0.0D, y, 0.0D));

		for (AxisAlignedBB axisalignedbb : list) {
			x = axisalignedbb.calculateXOffset(this.getBoundingBox(), x);
		}

		this.setBoundingBox(this.getBoundingBox().offset(x, 0.0D, 0.0D));

		for (AxisAlignedBB axisalignedbb : list) {
			z = axisalignedbb.calculateZOffset(this.getBoundingBox(), z);
		}

		this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, z));

		// RESET
		AxisAlignedBB axisalignedbb = this.getBoundingBox();
		this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
		this.posY = axisalignedbb.minY + (FBP.restOnFloor ? particleScale / 10 : 0);
		this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;

		this.onGround = y != Y && Y < 0.0D;

		if (x != X)
			motionX *= 0.699999988079071D;
		if (z != Z)
			motionZ *= 0.699999988079071D;
	}

	@Override
	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

	}

	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		int i = super.getBrightnessForRender(p_189214_1_);
		int j = 0;

		BlockPos pos = new BlockPos(posX, posY, posZ);

		if (this.world.isBlockLoaded(pos)) {
			j = this.world.getCombinedLight(pos, 0);
		}

		return i == 0 ? j : i;
	}

	@Override
	public void renderShadedParticle(VertexBuffer buf, float partialTicks) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;

		float f = 0, f1 = 0, f2 = 0, f3 = 0;

		if (particleTexture != null) {
			if (!FBP.cartoonMode) {
				f = particleTexture.getInterpolatedU(particleTextureJitterX / 4 * 16);
				f2 = particleTexture.getInterpolatedV(particleTextureJitterY / 4 * 16);
			}

			f1 = particleTexture.getInterpolatedU((particleTextureJitterX + 1) / 4 * 16);
			f3 = particleTexture.getInterpolatedV((particleTextureJitterY + 1) / 4 * 16);
		} else {
			f = (particleTextureIndexX + particleTextureJitterX / 4) / 16;
			f1 = f + 0.015609375F;
			f2 = (particleTextureIndexY + particleTextureJitterY / 4) / 16;
			f3 = f2 + 0.015609375F;
		}

		float f5 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

		int i = getBrightnessForRender(partialTicks);

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		float f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

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
		GlStateManager.enableCull();

		par = new Vec2f[] { new Vec2f(f1, f3), new Vec2f(f1, f2), new Vec2f(f, f2), new Vec2f(f, f3) };

		buf.setTranslation(f5, f6, f7);

		FBPRenderUtil.renderCubeShaded_S(buf, par, f5, f6, f7, f4 / 10, smoothRot, i >> 16 & 65535, i & 65535,
				particleRed, particleGreen, particleBlue, alpha, FBP.cartoonMode);

		buf.setTranslation(0, 0, 0);
	}
}