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
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticleRain extends ParticleDigging
{
	private final IBlockState sourceState;

	Minecraft mc;

	double AngleY, particleHeight, prevParticleScale, prevParticleHeight, prevParticleAlpha;
	double scalar = FBP.scaleMult;
	double endMult = 1;

	Vec2f[] par;

	public FBPParticleRain(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, IBlockState state)
	{
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);

		try
		{
			FBP.setSourcePos.invokeExact((ParticleDigging) this, new BlockPos(xCoordIn, yCoordIn, zCoordIn));
		} catch (Throwable e1)
		{
			e1.printStackTrace();
		}

		AngleY = FBP.random.nextDouble() * 45;

		this.motionX = xSpeedIn;
		this.motionY = -ySpeedIn;
		this.motionZ = zSpeedIn;

		this.particleGravity = 0.025f;

		sourceState = state;

		mc = Minecraft.getMinecraft();

		particleMaxAge = (int) FBP.random.nextDouble(50, 70);

		this.particleAlpha = 0f;
		this.particleScale = 0f;

		this.canCollide = true;

		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.85, 1);
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex)
	{

	}

	public Particle MultiplyVelocity(float multiplier)
	{
		this.motionX *= multiplier;
		this.motionY = (this.motionY - 0.10000000149011612D) * (multiplier / 2) + 0.10000000149011612D;
		this.motionZ *= multiplier;
		return this;
	}

	@Override
	protected void multiplyColor(@Nullable BlockPos p_187154_1_)
	{
		int i = mc.getBlockColors().colorMultiplier(this.sourceState, this.world, p_187154_1_, 0);
		this.particleRed *= (i >> 16 & 255) / 255.0F;
		this.particleGreen *= (i >> 8 & 255) / 255.0F;
		this.particleBlue *= (i & 255) / 255.0F;
	}

	@Override
	public int getFXLayer()
	{
		return 1;
	}

	@Override
	public void onUpdate()
	{
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;
		prevParticleHeight = particleHeight;

		if (!mc.isGamePaused())
		{
			particleAge++;

			if (posY < mc.player.posY - (mc.gameSettings.renderDistanceChunks * 9))
				setExpired();

			if (!onGround)
			{
				if (this.particleAge < this.particleMaxAge)
				{
					double max = scalar * 0.5;

					if (particleScale < max)
					{
						if (FBP.randomFadingSpeed)
							particleScale += 0.05F * endMult;
						else
							particleScale += 0.05F;

						if (particleScale > max)
							particleScale = (float) max;

						particleHeight = particleScale;
					}

					if (particleAlpha < 0.65f)
					{
						if (FBP.randomFadingSpeed)
							particleAlpha += 0.085F * endMult;
						else
							particleAlpha += 0.085F;

						if (particleAlpha > 0.65f)
							particleAlpha = 0.65f;
					}
				} else
					setExpired();
			}

			if (world.getBlockState(new BlockPos(posX, posY, posZ)).getMaterial().isLiquid())
				setExpired();

			motionY -= 0.04D * this.particleGravity;

			move(motionX, motionY, motionZ);

			motionY *= 1.00025000190734863D;

			if (onGround)
			{
				motionX = 0;
				motionY = -0.25f;
				motionZ = 0;

				if (particleHeight > 0.075f)
					particleHeight *= 0.725f;

				float max = (float) scalar * 4.25f;

				if (particleScale < max)
				{
					particleScale += max / 10;

					if (particleScale > max)
						particleScale = max;
				}

				if (particleScale >= max / 2)
				{
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.75F * endMult;
					else
						particleAlpha *= 0.75F;

					if (particleAlpha <= 0.001f)
						setExpired();
				}
			}
		}

		Vec3d rgb = mc.world.getSkyColor(mc.player, 0);

		this.particleRed = (float) rgb.xCoord;
		this.particleGreen = (float) MathHelper.clamp(rgb.yCoord + 0.25, 0.25, 1);
		this.particleBlue = (float) MathHelper.clamp(rgb.zCoord + 0.5, 0.5, 1);

		if (this.particleGreen > 1)
			particleGreen = 1;
		if (this.particleBlue > 1)
			particleBlue = 1;
	}

	@Override
	public void move(double x, double y, double z)
	{
		double X = x;
		double Y = y;
		double Z = z;

		List<AxisAlignedBB> list = this.world.getCollisionBoxes((Entity) null, this.getBoundingBox().expand(x, y, z));

		for (AxisAlignedBB axisalignedbb : list)
		{
			y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
		}

		this.setBoundingBox(this.getBoundingBox().offset(0.0D, y, 0.0D));

		for (AxisAlignedBB axisalignedbb : list)
		{
			x = axisalignedbb.calculateXOffset(this.getBoundingBox(), x);
		}

		this.setBoundingBox(this.getBoundingBox().offset(x, 0.0D, 0.0D));

		for (AxisAlignedBB axisalignedbb : list)
		{
			z = axisalignedbb.calculateZOffset(this.getBoundingBox(), z);
		}

		this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, z));

		this.resetPositionToBB();

		this.onGround = y != Y && Y < 0.0D;

		if (x != X)
			motionX *= 0.699999988079071D;
		if (z != Z)
			motionZ *= 0.699999988079071D;
	}

	@Override
	public void renderParticle(VertexBuffer buf, Entity entityIn, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ)
	{
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;

		float f = 0, f1 = 0, f2 = 0, f3 = 0;

		if (particleTexture != null)
		{
			if (!FBP.cartoonMode)
			{
				f = particleTexture.getInterpolatedU(particleTextureJitterX / 4 * 16);
				f2 = particleTexture.getInterpolatedV(particleTextureJitterY / 4 * 16);
			}

			f1 = particleTexture.getInterpolatedU((particleTextureJitterX + 1) / 4 * 16);
			f3 = particleTexture.getInterpolatedV((particleTextureJitterY + 1) / 4 * 16);
		} else
		{
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
		par = new Vec2f[] { new Vec2f(f1, f3), new Vec2f(f1, f2), new Vec2f(f, f2), new Vec2f(f, f3) };

		FBPRenderUtil.renderCubeShaded_WH(buf, par, f5, f6 + height / 10, f7, f4 / 10, height / 10,
				new FBPVector3d(0, AngleY, 0), i >> 16 & 65535, i & 65535, particleRed, particleGreen, particleBlue,
				alpha, FBP.cartoonMode);
	}

	@Override
	public int getBrightnessForRender(float p_189214_1_)
	{
		int i = super.getBrightnessForRender(p_189214_1_);
		int j = 0;

		if (this.world.isBlockLoaded(new BlockPos(posX, posY, posZ)))
		{
			j = this.world.getCombinedLight(new BlockPos(posX, posY, posZ), 0);
		}

		return i == 0 ? j : i;
	}
}