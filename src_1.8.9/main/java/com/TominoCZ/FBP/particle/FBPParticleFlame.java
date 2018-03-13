package com.TominoCZ.FBP.particle;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class FBPParticleFlame extends EntityFlameFX {
	Minecraft mc;

	double startScale;

	double scaleAlpha, prevParticleScale, prevParticleAlpha;

	double endMult = 1;

	float AngleY;

	float _brightnessForRender = 1;

	Vec3[] cube;

	Vector2f par;

	Vec3 startPos;

	boolean spawnAnother = true;

	protected FBPParticleFlame(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double mX, double mY,
			double mZ, boolean spawnAnother) {
		super(worldIn, xCoordIn, yCoordIn - 0.11f, zCoordIn, mX, mY, mZ);
		IBlockState bs = worldIn.getBlockState(new BlockPos(xCoordIn, yCoordIn, zCoordIn));

		this.spawnAnother = spawnAnother;

		if (bs.getBlock() != Blocks.torch)
			spawnAnother = false;

		if (bs == Blocks.torch.getDefaultState())
			yCoordIn += 0.11f;

		startPos = new Vec3(xCoordIn, yCoordIn, zCoordIn);

		mc = Minecraft.getMinecraft();

		this.motionY = -0.00085f;
		this.particleGravity = -0.05f;

		this.particleIcon = mc.getBlockRendererDispatcher().getBlockModelShapes()
				.getTexture(Blocks.snow.getDefaultState());

		particleScale *= FBP.scaleMult * 2.5f;
		particleMaxAge = FBP.random.nextInt(3, 5);

		this.particleRed = 1f;
		this.particleGreen = 1f;
		this.particleBlue = 0f;

		scaleAlpha = particleScale * 0.35;

		AngleY = rand.nextFloat() * 80;

		cube = new Vec3[FBP.CUBE.length];

		for (int i = 0; i < FBP.CUBE.length; i++) {
			Vec3 vec = FBP.CUBE[i];
			cube[i] = rotatef(vec, 0, AngleY, 0);
		}

		particleAlpha = 1f;
		startScale = particleScale;

		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.9875, 1);
	}

	@Override
	public int getFXLayer() {
		return 0;
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;

		if (!FBP.fancyFlame)
			this.isDead = true;

		if (!mc.isGamePaused()) {
			particleAge++;

			if (this.particleAge >= this.particleMaxAge) {
				this.particleGreen = (float) (particleScale / startScale);

				if (FBP.randomFadingSpeed)
					particleScale *= 0.95F * endMult;
				else
					particleScale *= 0.95F;

				if (particleAlpha > 0.01 && particleScale <= scaleAlpha) {
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.9F * endMult;
					else
						particleAlpha *= 0.9F;
				}

				if (particleAlpha <= 0.01)
					setDead();
				else if (particleAlpha <= 0.325 && spawnAnother
						&& worldObj.getBlockState(new BlockPos(posX, posY, posZ)).getBlock() == Blocks.torch) {
					spawnAnother = false;

					mc.effectRenderer.addEffect(new FBPParticleFlame(worldObj, startPos.xCoord,
							startPos.yCoord - 0.065f, startPos.zCoord, 0, 0, 0, spawnAnother));
				}
			}

			motionY -= 0.02D * this.particleGravity;
			moveEntity(0, motionY, 0);
			motionY *= 0.95D;
		}
	}

	@Override
	public void renderParticle(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;

		float f = particleIcon.getInterpolatedU((0.1f + 1) / 4 * 16);
		float f1 = particleIcon.getInterpolatedV((0.1f + 1) / 4 * 16);

		float f5 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY) + 0.01275F;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

		int i = getBrightnessForRender(partialTicks);

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		float f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

		GlStateManager.enableCull();

		par = new Vector2f(f, f1);

		Tessellator.getInstance().draw();
		mc.getRenderManager().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		worldRendererIn.setTranslation(f5, f6, f7);
		putCube(worldRendererIn, f4 / 80, i >> 16 & 65535, i & 65535, particleRed, particleGreen, particleBlue, alpha);
		worldRendererIn.setTranslation(0, 0, 0);

		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().getTextureManager().bindTexture(FBP.LOCATION_PARTICLE_TEXTURE);
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}

	public void putCube(WorldRenderer worldRendererIn, double scale, int j, int k, float r, float g, float b, float a) {
		float brightnessForRender = _brightnessForRender;

		float R = 0;
		float G = 0;
		float B = 0;

		for (int i = 0; i < cube.length; i += 4) {
			Vec3 v1 = cube[i];
			Vec3 v2 = cube[i + 1];
			Vec3 v3 = cube[i + 2];
			Vec3 v4 = cube[i + 3];

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

	private void addVt(WorldRenderer worldRendererIn, double scale, Vec3 pos, double u, double v, int j, int k, float r,
			float g, float b, float a) { // add vertex to buffer
		worldRendererIn.pos(pos.xCoord * scale, pos.yCoord * scale, pos.zCoord * scale).tex(u, v).color(r, g, b, a)
				.lightmap(j, k).endVertex();
	}

	Vec3 rotatef(Vec3 vec, float AngleX, float AngleY, float AngleZ) {
		double sinAngleX = MathHelper.sin(AngleX);
		double sinAngleY = MathHelper.sin(AngleY);
		double sinAngleZ = MathHelper.sin(AngleZ);

		double cosAngleX = MathHelper.cos(AngleX);
		double cosAngleY = MathHelper.cos(AngleY);
		double cosAngleZ = MathHelper.cos(AngleZ);

		vec = new Vec3(vec.xCoord, vec.yCoord * cosAngleX - vec.zCoord * sinAngleX,
				vec.yCoord * sinAngleX + vec.zCoord * cosAngleX);
		vec = new Vec3(vec.xCoord * cosAngleY + vec.zCoord * sinAngleY, vec.yCoord,
				vec.xCoord * sinAngleY - vec.zCoord * cosAngleY);
		vec = new Vec3(vec.xCoord * cosAngleZ - vec.yCoord * sinAngleZ, vec.xCoord * sinAngleZ + vec.yCoord * cosAngleZ,
				vec.zCoord);

		return vec;
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
}
