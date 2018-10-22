package com.TominoCZ.FBP.particle;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.util.FBPRenderUtil;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FBPParticleFlame extends ParticleFlame
{
	Minecraft mc;

	double startScale;

	double scaleAlpha, prevParticleScale, prevParticleAlpha;

	double endMult = 1;

	float AngleY;

	float _brightnessForRender = 1;

	Vec3d[] cube;

	Vec2f par;

	Vec3d startPos;

	boolean spawnAnother = true;

	protected FBPParticleFlame(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double mX, double mY,
			double mZ, boolean spawnAnother)
	{
		super(worldIn, xCoordIn, yCoordIn - 0.06, zCoordIn, mX, mY, mZ);
		IBlockState bs = worldIn.getBlockState(new BlockPos(posX, posY, posZ));

		this.spawnAnother = spawnAnother;

		if (bs.getBlock() != Blocks.TORCH)
			spawnAnother = false;

		if (bs == Blocks.TORCH.getDefaultState())
			prevPosY = posY = posY + 0.04f;

		startPos = new Vec3d(posX, posY, posZ);

		mc = Minecraft.getMinecraft();

		this.motionY = -0.00085f;
		this.particleGravity = -0.05f;

		this.particleTexture = mc.getBlockRendererDispatcher().getBlockModelShapes()
				.getTexture(Blocks.SNOW.getDefaultState());

		particleScale *= FBP.scaleMult * 2.5f;
		particleMaxAge = FBP.random.nextInt(3, 5);

		this.particleRed = 1f;
		this.particleGreen = 1f;
		this.particleBlue = 0f;

		AngleY = rand.nextFloat() * 80;

		cube = new Vec3d[FBP.CUBE.length];

		for (int i = 0; i < FBP.CUBE.length; i++)
		{
			Vec3d vec = FBP.CUBE[i];
			cube[i] = FBPRenderUtil.rotatef_d(vec, 0, AngleY, 0);
		}

		particleAlpha = 1f;

		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.9875, 1);

		multipleParticleScaleBy(1);
	}

	@Override
	public Particle multipleParticleScaleBy(float scale)
	{
		Particle p = super.multipleParticleScaleBy(scale);

		startScale = particleScale;
		scaleAlpha = particleScale * 0.35;

		float f = particleScale / 80;

		this.setBoundingBox(new AxisAlignedBB(posX - f, posY - f, posZ - f, posX + f, posY + f, posZ + f));

		return p;
	}

	@Override
	public int getFXLayer()
	{
		return 0;
	}

	@Override
	public void onUpdate()
	{
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;

		if (!FBP.fancyFlame)
			this.isExpired = true;

		if (!mc.isGamePaused())
		{
			particleAge++;

			if (this.particleAge >= this.particleMaxAge)
			{
				if (FBP.randomFadingSpeed)
					particleScale *= 0.95F * endMult;
				else
					particleScale *= 0.95F;

				if (particleAlpha > 0.01 && particleScale <= scaleAlpha)
				{
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.9F * endMult;
					else
						particleAlpha *= 0.9F;
				}

				if (particleAlpha <= 0.01)
					setExpired();
				else if (particleAlpha <= 0.325 && spawnAnother
						&& world.getBlockState(new BlockPos(posX, posY, posZ)).getBlock() == Blocks.TORCH)
				{
					spawnAnother = false;

					mc.effectRenderer.addEffect(
							new FBPParticleFlame(world, startPos.x, startPos.y, startPos.z, 0, 0, 0, spawnAnother));
				}
			}

			motionY -= 0.02D * this.particleGravity;
			move(0, motionY, 0);
			motionY *= 0.95D;
		}
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

		// RESET
		resetPositionToBB();
		this.onGround = y != Y;
	}

	@Override
	protected void resetPositionToBB()
	{
		AxisAlignedBB axisalignedbb = this.getBoundingBox();
		this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
		this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
		this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;
	}

	@Override
	public void renderParticle(BufferBuilder buf, Entity entityIn, float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ)
	{
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;

		float f = particleTexture.getInterpolatedU((0.1f + 1) / 4 * 16);
		float f1 = particleTexture.getInterpolatedV((0.1f + 1) / 4 * 16);

		float f5 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

		int i = getBrightnessForRender(partialTicks);

		float alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);

		// SMOOTH TRANSITION
		float f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

		if (this.particleAge >= this.particleMaxAge)
			this.particleGreen = (float) (f4 / startScale);

		GlStateManager.enableCull();

		par = new Vec2f(f, f1);

		// FBPRenderUtil.renderCubeShaded_S(buf, new Vec2f[] {par, par, par, par}, f5,
		// f6, f7, f4 / 80, FBPVector3d.UnitY.multiply(AngleY), i >> 16 & 65535, i &
		// 65535, particleRed, particleGreen, particleBlue, alpha, true);

		Tessellator.getInstance().draw();
		mc.getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		buf.setTranslation(f5, f6, f7);
		putCube(buf, f4 / 80, i >> 16 & 65535, i & 65535, particleRed, particleGreen, particleBlue, alpha);
		buf.setTranslation(0, 0, 0);

		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().getTextureManager().bindTexture(FBP.LOCATION_PARTICLE_TEXTURE);
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}

	public void putCube(BufferBuilder worldRendererIn, double scale, int j, int k, float r, float g, float b, float a)
	{
		float brightnessForRender = _brightnessForRender;

		float R = 0;
		float G = 0;
		float B = 0;

		for (int i = 0; i < cube.length; i += 4)
		{
			Vec3d v1 = cube[i];
			Vec3d v2 = cube[i + 1];
			Vec3d v3 = cube[i + 2];
			Vec3d v4 = cube[i + 3];

			R = r * brightnessForRender;
			G = g * brightnessForRender;
			B = b * brightnessForRender;

			brightnessForRender *= 0.95;

			addVt(worldRendererIn, scale, v1, par.x, par.y, j, k, R, G, B, a);
			addVt(worldRendererIn, scale, v2, par.x, par.y, j, k, R, G, B, a);
			addVt(worldRendererIn, scale, v3, par.x, par.y, j, k, R, G, B, a);
			addVt(worldRendererIn, scale, v4, par.x, par.y, j, k, R, G, B, a);
		}
	}

	private void addVt(BufferBuilder worldRendererIn, double scale, Vec3d pos, double u, double v, int j, int k,
			float r, float g, float b, float a)
	{ // add vertex to buffer
		worldRendererIn.pos(pos.x * scale, pos.y * scale, pos.z * scale).tex(u, v).color(r, g, b, a).lightmap(j, k)
				.endVertex();
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
