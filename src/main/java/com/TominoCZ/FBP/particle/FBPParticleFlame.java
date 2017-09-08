package com.TominoCZ.FBP.particle;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.keys.FBPKeyBindings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FBPParticleFlame extends ParticleFlame {
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
			double mZ, boolean spawnAnother) {
		super(worldIn, xCoordIn, yCoordIn - 0.11f, zCoordIn, mX, mY, mZ);

		this.spawnAnother = spawnAnother;

		startPos = new Vec3d(xCoordIn, yCoordIn, zCoordIn);

		this.motionY = -0.00085f;
		this.particleGravity = -0.05f;

		mc = Minecraft.getMinecraft();

		this.particleTexture = mc.getBlockRendererDispatcher().getModelForState(Blocks.SNOW.getStateFromMeta(1))
				.getParticleTexture();

		particleScale *= FBP.scaleMult * 2.5f;
		particleMaxAge = (int) FBP.random.nextInt(3, 5);

		this.particleRed = 1f;
		this.particleGreen = 1f;
		this.particleBlue = 0f;

		scaleAlpha = particleScale * 0.35;

		AngleY = rand.nextFloat() * 80;

		cube = new Vec3d[FBP.CUBE.length];

		for (int i = 0; i < FBP.CUBE.length; i++) {
			Vec3d vec = FBP.CUBE[i];
			cube[i] = rotatef(vec, 0, AngleY, 0);
		}

		particleAlpha = 1f;
		startScale = particleScale;
	}

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
			this.isExpired = true;

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
					setExpired();
				else if (particleAlpha <= 0.325 && spawnAnother
						&& worldObj.getBlockState(new BlockPos(startPos)).getBlock() == Blocks.TORCH) {
					spawnAnother = false;
					mc.effectRenderer.addEffect(new FBPParticleFlame(worldObj, startPos.xCoord, startPos.yCoord,
							startPos.zCoord, 0, 0, 0, spawnAnother));
				}
			}

			motionY -= 0.02D * (double) this.particleGravity;
			moveEntity(0, motionY, 0);
			motionY *= 0.95D;
		}
	}

	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;
		if (FBPKeyBindings.FBPSweep.isKeyDown()) {
			this.isExpired = true;
			return;
		}

		float f = particleTexture.getInterpolatedU((double) ((0.1f + 1) / 4 * 16));
		float f1 = particleTexture.getInterpolatedV((double) ((0.1f + 1) / 4 * 16));

		float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY) + 0.01275F;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);

		int i = getBrightnessForRender(partialTicks);

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		float f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

		// RENDER
		Tessellator.getInstance().draw();
		mc.getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();

		par = new Vec2f(f, f1);

		worldRendererIn.setTranslation(f5, f6, f7);
		putCube(worldRendererIn, f4 / 80, i >> 16 & 65535, i & 65535, particleRed, particleGreen, particleBlue, alpha);
		worldRendererIn.setTranslation(0, 0, 0);

		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().getTextureManager()
				.bindTexture(new ResourceLocation("textures/particle/particles.png"));
		worldRendererIn.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
	}

	public void putCube(VertexBuffer buff, double scale, int j, int k, float r, float g, float b, float a) {
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

			brightnessForRender *= 0.95;// TODO

			addVt(buff, scale, v1, par.x, par.y, j, k, R, G, B, a);
			addVt(buff, scale, v2, par.x, par.y, j, k, R, G, B, a);
			addVt(buff, scale, v3, par.x, par.y, j, k, R, G, B, a);
			addVt(buff, scale, v4, par.x, par.y, j, k, R, G, B, a);
		}
	}

	private void addVt(VertexBuffer buff, double scale, Vec3d pos, double u, double v, int j, int k, float r, float g,
			float b, float a) { // add vertex to buffer
		buff.pos(pos.xCoord * scale, pos.yCoord * scale, pos.zCoord * scale).tex(u, v).color(r, g, b, a).lightmap(j, k)
				.endVertex();
	}

	Vec3d rotatef(Vec3d vec, float AngleX, float AngleY, float AngleZ) {
		double sinAngleX = MathHelper.sin(AngleX);
		double sinAngleY = MathHelper.sin(AngleY);
		double sinAngleZ = MathHelper.sin(AngleZ);

		double cosAngleX = MathHelper.cos(AngleX);
		double cosAngleY = MathHelper.cos(AngleY);
		double cosAngleZ = MathHelper.cos(AngleZ);

		vec = new Vec3d(vec.xCoord, vec.yCoord * cosAngleX - vec.zCoord * sinAngleX,
				vec.yCoord * sinAngleX + vec.zCoord * cosAngleX);
		vec = new Vec3d(vec.xCoord * cosAngleY + vec.zCoord * sinAngleY, vec.yCoord,
				vec.xCoord * sinAngleY - vec.zCoord * cosAngleY);
		vec = new Vec3d(vec.xCoord * cosAngleZ - vec.yCoord * sinAngleZ,
				vec.xCoord * sinAngleZ + vec.yCoord * cosAngleZ, vec.zCoord);

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
