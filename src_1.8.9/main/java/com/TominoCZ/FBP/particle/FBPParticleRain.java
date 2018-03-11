package com.TominoCZ.FBP.particle;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.util.FBPPartialTicksUtil;
import com.TominoCZ.FBP.util.FBPRenderUtil;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticleRain extends EntityDiggingFX implements IFBPShadedParticle {
	private final IBlockState sourceState;

	Minecraft mc;

	double particleHeight;

	double scaleAlpha, prevParticleScale, prevParticleHeight, prevParticleAlpha;

	double scaleMult = 1.45;

	double endMult = 1;

	double AngleY;

	float brightness = 1;

	Vector2f[] par;

	public FBPParticleRain(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, IBlockState state) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);

		try {
			FBP.setSourcePos.invokeExact((EntityDiggingFX) this, new BlockPos(xCoordIn, yCoordIn, zCoordIn));
		} catch (Throwable e1) {
			e1.printStackTrace();
		}

		AngleY = FBP.random.nextDouble() * 45;

		this.motionX = xSpeedIn;
		this.motionY = -ySpeedIn;
		this.motionZ = zSpeedIn;

		this.particleGravity = 0.025f;

		sourceState = state;

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
	public void setParticleTextureIndex(int particleTextureIndex) {

	}

	public EntityFX MultiplyVelocity(float multiplier) {
		this.motionX *= multiplier;
		this.motionY = (this.motionY - 0.10000000149011612D) * (multiplier / 2) + 0.10000000149011612D;
		this.motionZ *= multiplier;
		return this;
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

			if (this.particleAge < this.particleMaxAge) {
				if (!onGround) {
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

			if (worldObj.getBlockState(getPosition()).getBlock().getMaterial().isLiquid())
				setDead();

			motionY -= 0.04D * this.particleGravity;

			moveEntity(motionX, motionY, motionZ);

			motionY *= 1.00025000190734863D;

			if (onGround) {
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

		Vec3 rgb = mc.theWorld.getSkyColor(mc.thePlayer, FBPPartialTicksUtil.partialTicks);

		this.particleRed = (float) rgb.xCoord;
		this.particleGreen = (float) MathHelper.clamp_double(rgb.yCoord + 0.25, 0.25, 1);
		this.particleBlue = (float) MathHelper.clamp_double(rgb.zCoord + 0.5, 0.5, 1);

		if (this.particleGreen > 1)
			particleGreen = 1;
		if (this.particleBlue > 1)
			particleBlue = 1;
	}

	public void moveEntity(double x, double y, double z) {
		double X = x;
		double Y = y;
		double Z = z;

		List<AxisAlignedBB> list1 = this.worldObj.getCollidingBoundingBoxes(this,
				this.getEntityBoundingBox().addCoord(x, y, z));

		for (AxisAlignedBB axisalignedbb1 : list1) {
			y = axisalignedbb1.calculateYOffset(this.getEntityBoundingBox(), y);
		}

		this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));

		for (AxisAlignedBB axisalignedbb2 : list1) {
			x = axisalignedbb2.calculateXOffset(this.getEntityBoundingBox(), x);
		}

		this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));

		for (AxisAlignedBB axisalignedbb13 : list1) {
			z = axisalignedbb13.calculateZOffset(this.getEntityBoundingBox(), z);
		}

		this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));

		this.resetPositionToBB();

		this.onGround = y != Y && Y < 0.0D;

		if (x != X)
			motionX *= 0.699999988079071D;
		if (z != Z)
			motionZ *= 0.699999988079071D;
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

	@Override
	public int getBrightnessForRender(float p_189214_1_) {
		int i = super.getBrightnessForRender(p_189214_1_);
		int j = 0;

		if (this.worldObj.isBlockLoaded(new BlockPos(posX, posY, posZ))) {
			j = this.worldObj.getCombinedLight(new BlockPos(posX, posY, posZ), 0);
		}

		return i == 0 ? j : i;
	}

	@Override
	public void renderShadedParticle(WorldRenderer buf, float partialTicks) {
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
		par = new Vector2f[] { new Vector2f(f1, f3), new Vector2f(f1, f2), new Vector2f(f, f2), new Vector2f(f, f3) };

		FBPRenderUtil.renderCubeShaded_WH(buf, par, f5, f6, f7, f4 / 20, height / 20, new FBPVector3d(0, AngleY, 0),
				i >> 16 & 65535, i & 65535, particleRed, particleGreen, particleBlue, alpha);
	}
}