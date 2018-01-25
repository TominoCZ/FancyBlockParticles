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

	double particleHeight;

	double scaleAlpha, prevParticleScale, prevParticleHeight, prevParticleAlpha;

	boolean modeDebounce = false;

	double scaleMult = 1.45;

	double endMult = 1;

	double AngleY;

	float brightness = 1;

	FBPVector3d[] par;

	float partialTicks;

	public FBPParticleRain(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Blocks.snow, 0, 0);

		AngleY = FBP.random.nextDouble() * 45;

		this.motionX = xSpeedIn;
		this.motionY = -ySpeedIn;
		this.motionZ = zSpeedIn;

		this.particleGravity = 0.025f;

		mc = Minecraft.getMinecraft();

		particleMaxAge = (int) FBP.random.nextDouble(95, 115);

		scaleAlpha = particleScale * 0.75;

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
				this.isDead = true;

			if (this.particleAge < this.particleMaxAge) {
				if (!isCollided) {
					if (particleScale < FBP.scaleMult * 1.5f) {
						if (FBP.randomFadingSpeed)
							particleScale += 0.75F * endMult;
						else
							particleScale += 0.75F;

						if (particleScale > 1)
							particleScale = 1;

						particleHeight = particleScale;
					}

					if (particleAlpha < 0.625f) {
						if (FBP.randomFadingSpeed)
							particleAlpha += 0.085F * endMult;
						else
							particleAlpha += 0.085F;

						if (particleAlpha > 0.625f)
							particleAlpha = 0.625f;
					}
				}
			} else
				setDead();

			motionY -= 0.04D * this.particleGravity;

			moveEntity(motionX, motionY, motionZ);

			motionY *= 1.00025000190734863D;

			if (isCollided) {
				motionX = 0;
				motionY = -0.25f;
				motionZ = 0;

				if (particleHeight > 0.075f)
					particleHeight *= 0.8f;

				if (particleScale < FBP.scaleMult * 4.5f) {
					particleScale *= scaleMult;

					if (scaleMult > 1)
						scaleMult *= 0.95;
					if (scaleMult < 1)
						scaleMult = 1;
				}

				if (particleScale >= FBP.scaleMult * 2) {
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.75F * endMult;
					else
						particleAlpha *= 0.75F;
				}

				if (particleAlpha <= 0.001f)
					setDead();
			}
		}

		Vec3 rgb = mc.theWorld.getSkyColor(mc.thePlayer, partialTicks);

		this.particleRed = (float) rgb.xCoord;
		this.particleGreen = (float) MathHelper.clamp_double(rgb.yCoord + 0.25, 0.25, 1);
		this.particleBlue = (float) MathHelper.clamp_double(rgb.zCoord + 0.5, 0.5, 1);

		if (this.particleGreen > 1)
			particleGreen = 1;
		if (this.particleBlue > 1)
			particleBlue = 1;
	}

	public void moveEntity(double x, double y, double z) {
		double d6 = x;
		double d7 = y;
		double d8 = z;
		double d0 = y;

		List<AxisAlignedBB> list = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.addCoord(x, y, z));

		for (AxisAlignedBB aabb : list) {
			x = aabb.calculateXOffset(this.boundingBox, x);
			y = aabb.calculateYOffset(this.boundingBox, y);
			z = aabb.calculateZOffset(this.boundingBox, z);
		}

		this.boundingBox.setBB(boundingBox.offset(x, y, z));

		// RESET
		AxisAlignedBB axisalignedbb = this.boundingBox;
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

	@Override
	public void renderParticle(Tessellator tes, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
			float rotationXY, float rotationXZ) {
		this.partialTicks = partialTicks;

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
		float height = (float) (prevParticleHeight + (particleHeight - prevParticleHeight) * partialTicks);

		// RENDER
		par = new FBPVector3d[] { new FBPVector3d(f1, f3, 0), new FBPVector3d(f1, f2, 0), new FBPVector3d(f, f2, 0),
				new FBPVector3d(f, f3, 0) };

		FBPRenderUtil.renderCubeShaded_WH(tes, par, f5, f6, f7, f4 / 20, height / 20, new FBPVector3d(0, AngleY, 0), i,
				particleRed, particleGreen, particleBlue, alpha, FBP.cartoonMode);
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