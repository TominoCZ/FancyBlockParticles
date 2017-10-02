package com.TominoCZ.FBP.particle;

import com.TominoCZ.FBP.FBP;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FBPParticleSmokeNormal extends ParticleSmokeNormal {
	Minecraft mc;

	double startScale;

	double scaleAlpha, prevParticleScale, prevParticleAlpha;

	double endMult = 1;

	float AngleY;

	float _brightnessForRender = 1;

	Vec3d[] cube;

	Vec2f par;

	protected FBPParticleSmokeNormal(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double mX,
			double mY, double mZ, float R, float G, float B, float scale, boolean b, TextureAtlasSprite tex) {
		super(worldIn, xCoordIn, yCoordIn - 0.11f, zCoordIn, mX, mY, mZ, 1);
		// this.motionY = 0.035f;
		// this.particleGravity *= 1.85f;
		this.particleRed = R;
		this.particleGreen = G;
		this.particleBlue = B;

		mc = Minecraft.getMinecraft();
		this.particleTexture = tex;

		// particleMaxAge *= (int) FBP.random.nextInt(2, 4);

		// this.particleRed = 0.75f;
		// this.particleGreen = particleRed;
		// this.particleBlue = particleRed;

		scaleAlpha = particleScale * 0.85;

		if (worldIn.getBlockState(new BlockPos(xCoordIn, yCoordIn, zCoordIn)).getBlock() == Blocks.FIRE) {
			particleScale *= 1.35f;
			this.particleGravity *= 0.25f;

			this.motionX = FBP.random.nextDouble(-0.05, 0.05);
			this.motionY = FBP.random.nextDouble() * 0.5;
			this.motionZ = FBP.random.nextDouble(-0.05, 0.05);

			this.motionY *= 0.35f;

			scaleAlpha = particleScale * 0.5;

			particleMaxAge = (int) FBP.random.nextInt(7, 18);
		} else if (worldIn.getBlockState(new BlockPos(xCoordIn, yCoordIn, zCoordIn)).getBlock() == Blocks.TORCH) {
			particleScale *= 0.5f;

			this.motionX = FBP.random.nextDouble(-0.05, 0.05);
			this.motionY = FBP.random.nextDouble() * 0.5;
			this.motionZ = FBP.random.nextDouble(-0.05, 0.05);

			this.motionX *= 0.925f;
			this.motionY = 0.01f;
			this.motionZ *= 0.925f;

			this.particleRed = 0.275f;
			this.particleGreen = 0.275f;
			this.particleBlue = 0.275f;

			scaleAlpha = particleScale * 0.75;

			particleMaxAge = (int) FBP.random.nextInt(5, 10);
		} else {
			particleScale = scale;

			this.motionY *= 0.975f;
		}

		particleScale *= FBP.scaleMult;

		startScale = particleScale;

		AngleY = rand.nextFloat() * 80;

		cube = new Vec3d[FBP.CUBE.length];

		for (int i = 0; i < FBP.CUBE.length; i++) {
			Vec3d vec = FBP.CUBE[i];
			cube[i] = rotatef(vec, 0, AngleY, 0);
		}

		particleAlpha = 1f;

		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.975, 1);
	}

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

		if (!FBP.fancySmoke)
			this.isExpired = true;

		// if (!mc.isGamePaused()) {
		particleAge++;

		if (this.particleAge >= this.particleMaxAge) {
			if (FBP.randomFadingSpeed)
				particleScale *= 0.85F * endMult;
			else
				particleScale *= 0.85F;

			if (particleAlpha > 0.01 && particleScale <= scaleAlpha) {
				if (FBP.randomFadingSpeed)
					particleAlpha *= 0.65F * endMult;
				else
					particleAlpha *= 0.65F;
			}

			if (particleAlpha <= 0.01)
				setExpired();
		}

		// int index = 7 - this.particleAge * 8 / this.particleMaxAge;

		// this.particleTextureIndexX = index % 16;
		// this.particleTextureIndexY = index / 16;

		this.motionY += 0.004D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);

		if (this.posY == this.prevPosY) {
			this.motionX *= 1.1D;
			this.motionZ *= 1.1D;
		}

		this.motionX *= 0.9599999785423279D;
		this.motionY *= 0.9599999785423279D;
		this.motionZ *= 0.9599999785423279D;

		if (this.isCollided) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}
	}

	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;

		float f = particleTexture.getInterpolatedU((double) ((0.1f + 1) / 4 * 16));
		float f1 = particleTexture.getInterpolatedV((double) ((0.1f + 1) / 4 * 16));

		float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY) + 0.01275F;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);

		int i = getBrightnessForRender(partialTicks);

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		float f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

		// float f = (float) this.particleTextureIndexX / 16.0F;
		// float f1 = f + 0.0624375F;

		// if (this.particleTexture != null) {
		// f = this.particleTexture.getMinU();
		// f1 = this.particleTexture.getMaxU();
		// }

		// RENDER
		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();

		par = new Vec2f(f, f1);

		worldRendererIn.setTranslation(f5, f6, f7);
		putCube(worldRendererIn, f4 * 0.05F, i >> 16 & 65535, i & 65535, particleRed, particleGreen, particleBlue,
				alpha);
		worldRendererIn.setTranslation(0, 0, 0);
	}

	public void putCube(VertexBuffer worldRendererIn, double scale, int j, int k, float r, float g, float b, float a) {
		float brightnessForRender = _brightnessForRender;

		float R = 0;
		float G = 0;
		float B = 0;

		for (int i = 0; i < cube.length; i += 4) {
			Vec3d v1 = cube[i];
			Vec3d v2 = cube[i + 1];
			Vec3d v3 = cube[i + 2];
			Vec3d v4 = cube[i + 3];

			R = r * brightnessForRender;
			G = g * brightnessForRender;
			B = b * brightnessForRender;

			brightnessForRender *= 0.875;

			addVt(worldRendererIn, scale, v1, par.x, par.y, j, k, R, G, B, a);
			addVt(worldRendererIn, scale, v2, par.x, par.y, j, k, R, G, B, a);
			addVt(worldRendererIn, scale, v3, par.x, par.y, j, k, R, G, B, a);
			addVt(worldRendererIn, scale, v4, par.x, par.y, j, k, R, G, B, a);
		}
	}

	private void addVt(VertexBuffer worldRendererIn, double scale, Vec3d pos, double u, double v, int j, int k,
			float r, float g, float b, float a) { // add vertex to buffer
		worldRendererIn.pos(pos.xCoord * scale, pos.yCoord * scale, pos.zCoord * scale).tex(u, v).color(r, g, b, a).lightmap(j, k)
				.endVertex();
	}

	Vec3d rotatef(Vec3d vec, float AngleX, float AngleY, float AngleZ) {
		double sinAngleX = MathHelper.sin(AngleX);
		double sinAngleY = MathHelper.sin(AngleY);
		double sinAngleZ = MathHelper.sin(AngleZ);

		double cosAngleX = MathHelper.cos(AngleX);
		double cosAngleY = MathHelper.cos(AngleY);
		double cosAngleZ = MathHelper.cos(AngleZ);

		vec = new Vec3d(vec.xCoord, vec.yCoord * cosAngleX - vec.zCoord * sinAngleX, vec.yCoord * sinAngleX + vec.zCoord * cosAngleX);
		vec = new Vec3d(vec.xCoord * cosAngleY + vec.zCoord * sinAngleY, vec.yCoord, vec.xCoord * sinAngleY - vec.zCoord * cosAngleY);
		vec = new Vec3d(vec.xCoord * cosAngleZ - vec.yCoord * sinAngleZ, vec.xCoord * sinAngleZ + vec.yCoord * cosAngleZ, vec.zCoord);

		return vec;
	}

	public int getBrightnessForRender(float p_189214_1_) {
		int i = super.getBrightnessForRender(p_189214_1_);
		int j = 0;

		if (this.worldObj.isBlockLoaded(new BlockPos(posX, posY, posZ))) {
			j = this.worldObj.getCombinedLight(new BlockPos(posX, posY, posZ), 0);
		}

		return i == 0 ? j : i;
	}
}
