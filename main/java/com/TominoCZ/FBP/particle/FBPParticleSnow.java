package com.TominoCZ.FBP.particle;

import java.util.List;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.util.FBPRenderUtil;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticleSnow extends EntityDiggingFX implements IFBPShadedParticle {
	Minecraft mc;

	double scaleAlpha, prevParticleScale, prevParticleAlpha;

	FBPVector3d rotStep;

	FBPVector3d prevRot;
	FBPVector3d rot;

	boolean modeDebounce = false;

	double endMult = 1;

	float brightness = 1;

	FBPVector3d[] par;

	public FBPParticleSnow(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Blocks.snow, 0, 0);
		rot = new FBPVector3d();
		prevRot = new FBPVector3d();

		createRotationMatrix();

		this.motionX = xSpeedIn;
		this.motionY = -ySpeedIn;
		this.motionZ = zSpeedIn;
		this.particleGravity = 1f;

		mc = Minecraft.getMinecraft();

		particleScale *= FBP.random.nextDouble(FBP.scaleMult - 0.25f, FBP.scaleMult + 0.25f);
		particleMaxAge = (int) FBP.random.nextDouble(120, 200);
		this.particleRed = this.particleGreen = this.particleBlue = 0.7F + (0.25F * mc.gameSettings.gammaSetting);

		scaleAlpha = particleScale * 0.75;

		this.particleAlpha = 0f;
		this.particleScale = 0f;

		this.isAirBorne = true;

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

			if (posY < mc.thePlayer.posY - (mc.gameSettings.renderDistanceChunks * 16))
				this.isDead = true;

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
					setDead();
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

			motionY -= 0.04D * this.particleGravity;
			moveEntity(motionX, motionY, motionZ);

			motionX *= 0.9800000190734863D;

			if (motionY < -0.2) // minimal motionY
				motionY *= 0.7500000190734863D;

			motionZ *= 0.9800000190734863D;

			if (isCollided) {
				motionX *= 0.680000190734863D;
				motionZ *= 0.6800000190734863D;

				rotStep = rotStep.multiply(0.85);

				this.particleAge += 4;
			}
		}
	}

	@Override
	public void moveEntity(double x, double y, double z) {
		double d6 = x;
		double d7 = y;
		double d8 = z;
		double d0 = y;

		List<AxisAlignedBB> list = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.addCoord(x, y, z));

		for (AxisAlignedBB aabb : list) {
			x = aabb.calculateXOffset(this.getEntityBoundingBox(), x);
			y = aabb.calculateYOffset(this.getEntityBoundingBox(), y);
			z = aabb.calculateZOffset(this.getEntityBoundingBox(), z);
		}

		this.boundingBox.setBB(boundingBox.offset(x, y, z));

		// RESET
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
		this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
		this.posY = axisalignedbb.minY;
		this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;

		this.isCollided = y != d7 && d0 < 0.0D;

		if (x != d6)
			motionX *= 0.699999988079071D;
		if (y != d7)
			motionY = 0;
		if (z != d8)
			motionZ *= 0.699999988079071D;
	}

	public void renderParticle(Tessellator tes, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
			float rotationXY, float rotationXZ) {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float partialTicks) {
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.posZ);

		if (this.worldObj.blockExists(i, 0, j)) {
			double d0 = (this.boundingBox.maxY - this.boundingBox.minY) * 0.66D + 0.1D;
			int k = MathHelper.floor_double(this.posY - (double) this.yOffset + d0);
			return this.worldObj.getLightBrightnessForSkyBlocks(i, k, j, 0);
		} else {
			return 0;
		}
	}

	@Override
	public void renderShadedParticle(Tessellator tes, float partialTicks) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;

		float f = 0, f1 = 0, f2 = 0, f3 = 0;

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
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY) + 0.01275F;
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
		par = new FBPVector3d[] { new FBPVector3d(f1, f3, 0), new FBPVector3d(f1, f2, 0), new FBPVector3d(f, f2, 0),
				new FBPVector3d(f, f3, 0) };

		tes.setTranslation(f5, f6, f7);

		FBPRenderUtil.renderCubeShaded_S(tes, par, f5, f6, f7, f4 / 20, smoothRot, i, 1, 1, 1, alpha, FBP.cartoonMode);

		tes.setTranslation(0, 0, 0);
	}
}