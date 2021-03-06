package com.TominoCZ.FBP.particle;

import java.util.List;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.util.FBPRenderUtil;
import com.TominoCZ.FBP.vector.FBPVector3d;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class FBPParticleRain extends EntityDiggingFX {

	Minecraft mc;

	double AngleY, particleHeight, prevParticleScale, prevParticleHeight, prevParticleAlpha;
	double scalar = FBP.scaleMult;
	double endMult = 1;

	FBPVector3d[] par;

	public FBPParticleRain(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Blocks.snow, 0, 0);

		AngleY = FBP.random.nextDouble() * 45;

		this.motionX = xSpeedIn;
		this.motionY = -ySpeedIn;
		this.motionZ = zSpeedIn;

		this.particleGravity = 0.025f;

		mc = Minecraft.getMinecraft();

		particleMaxAge = (int) FBP.random.nextDouble(50, 70);

		this.particleAlpha = 0f;
		this.particleScale = 0f;

		this.isAirBorne = true;

		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.85, 1);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;
		prevParticleHeight = particleHeight;

		if (!mc.isGamePaused()) {
			particleAge++;

			if (posY < mc.thePlayer.posY - (mc.gameSettings.renderDistanceChunks * 9))
				setDead();

			if (!onGround) {
				if (this.particleAge < this.particleMaxAge) {
					double max = scalar * 0.5;

					if (particleScale < max) {
						if (FBP.randomFadingSpeed)
							particleScale += 0.05F * endMult;
						else
							particleScale += 0.05F;

						if (particleScale > max)
							particleScale = (float) max;

						particleHeight = particleScale;
					}

					if (particleAlpha < 0.65f) {
						if (FBP.randomFadingSpeed)
							particleAlpha += 0.085F * endMult;
						else
							particleAlpha += 0.085F;

						if (particleAlpha > 0.65f)
							particleAlpha = 0.65f;
					}
				} else
					setDead();
			}

			if (worldObj.getBlock(MathHelper.floor_double(posX), MathHelper.floor_double(posY),
					MathHelper.floor_double(posZ)).getMaterial().isLiquid())
				setDead();

			motionY -= 0.04D * this.particleGravity;

			moveEntity(motionX, motionY, motionZ);

			motionY *= 1.00025000190734863D;

			if (onGround) {
				motionX = 0;
				motionY = -0.25f;
				motionZ = 0;

				if (particleHeight > 0.075f)
					particleHeight *= 0.725f;

				float max = (float) scalar * 4.25f;

				if (particleScale < max) {
					particleScale += max / 10;

					if (particleScale > max)
						particleScale = max;
				}

				if (particleScale >= max / 2) {
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.75F * endMult;
					else
						particleAlpha *= 0.75F;

					if (particleAlpha <= 0.001f)
						setDead();
				}
			}
		}

		Vec3 rgb = mc.theWorld.getSkyColor(mc.thePlayer, 0);

		this.particleRed = (float) rgb.xCoord;
		this.particleGreen = (float) MathHelper.clamp_double(rgb.yCoord + 0.25, 0.25, 1);
		this.particleBlue = (float) MathHelper.clamp_double(rgb.zCoord + 0.5, 0.5, 1);

		if (this.particleGreen > 1)
			particleGreen = 1;
		if (this.particleBlue > 1)
			particleBlue = 1;
	}

	@Override
	public void moveEntity(double x, double y, double z) {
		double X = x;
		double Y = y;
		double Z = z;

		List list = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.addCoord(x, y, z));

		for (int i = 0; i < list.size(); ++i) {
			y = ((AxisAlignedBB) list.get(i)).calculateYOffset(this.boundingBox, y);
		}

		this.boundingBox.offset(0.0D, y, 0.0D);

		for (int j = 0; j < list.size(); ++j) {
			x = ((AxisAlignedBB) list.get(j)).calculateXOffset(this.boundingBox, x);
		}

		this.boundingBox.offset(x, 0.0D, 0.0D);

		for (int j = 0; j < list.size(); ++j) {
			z = ((AxisAlignedBB) list.get(j)).calculateZOffset(this.boundingBox, z);
		}

		this.boundingBox.offset(0.0D, 0.0D, z);

		// RESET
		AxisAlignedBB axisalignedbb = this.boundingBox;
		this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
		this.posY = axisalignedbb.minY;
		this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;

		this.onGround = y != Y && Y < 0.0D;

		if (x != X)
			motionX *= 0.699999988079071D;
		if (z != Z)
			motionZ *= 0.699999988079071D;
	}

	@Override
	public void renderParticle(Tessellator tes, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
			float rotationXY, float rotationXZ) {
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
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

		int i = getBrightnessForRender(partialTicks);

		float alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);

		// SMOOTH TRANSITION
		float f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);
		float height = (float) (prevParticleHeight + (particleHeight - prevParticleHeight) * partialTicks);

		// RENDER
		par = new FBPVector3d[] { new FBPVector3d(f1, f3, 0), new FBPVector3d(f1, f2, 0), new FBPVector3d(f, f2, 0),
				new FBPVector3d(f, f3, 0) };

		FBPRenderUtil.renderCubeShaded_WH(tes, par, f5, f6 + height / 10, f7, f4 / 10, height / 10,
				new FBPVector3d(0, AngleY, 0), i, particleRed, particleGreen, particleBlue, alpha, FBP.cartoonMode);
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
}