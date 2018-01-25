package com.TominoCZ.FBP.particle;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.block.FBPBlockPos;
import com.TominoCZ.FBP.vector.FBPVector3d;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class FBPParticleFlame extends EntityFlameFX {
	Minecraft mc;

	double startScale;

	double scaleAlpha, prevParticleScale, prevParticleAlpha;

	double endMult = 1;

	float AngleY;

	float _brightnessForRender = 1;

	FBPVector3d[] cube;

	FBPVector3d par;

	FBPVector3d startPos;

	boolean spawnAnother = true;

	protected FBPParticleFlame(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double mX, double mY,
			double mZ, boolean spawnAnother) {
		super(worldIn, xCoordIn, yCoordIn - 0.1, zCoordIn, mX, mY, mZ);
		FBPBlockPos pos = new FBPBlockPos(xCoordIn, yCoordIn, zCoordIn);

		Block b = worldIn.getBlock(pos.getX(), pos.getY(), pos.getZ());
		
		this.spawnAnother = spawnAnother;
		this.particleIcon = Blocks.snow.getIcon(1, 0);

		if (b != Blocks.torch)
			spawnAnother = false;

		startPos = new FBPVector3d(xCoordIn, yCoordIn, zCoordIn);

		mc = Minecraft.getMinecraft();

		this.motionY = -0.00085f * 2.5f;
		this.particleGravity = -0.05f;

		particleScale *= FBP.scaleMult * 2.5f;
		particleMaxAge = FBP.random.nextInt(3, 5);

		this.particleRed = 1f;
		this.particleGreen = 1f;
		this.particleBlue = 0f;

		scaleAlpha = particleScale * 0.35;

		AngleY = rand.nextFloat() * 80;

		cube = new FBPVector3d[FBP.CUBE.length];

		for (int i = 0; i < FBP.CUBE.length; i++) {
			FBPVector3d vec = FBP.CUBE[i];
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
						&& worldObj.getBlock((int) posX, (int) posY, (int) posZ) == Blocks.torch) {
					spawnAnother = false;

					mc.effectRenderer.addEffect(new FBPParticleFlame(worldObj, startPos.x, startPos.y - 0.065f,
							startPos.z, 0, 0, 0, spawnAnother));
				}
			}

			motionY -= 0.02D * this.particleGravity;
			moveEntity(0, motionY, 0);
			motionY *= 0.95D;
		}
	}

	@Override
	public void renderParticle(Tessellator tes, float partialTicks, float f_0, float f_1, float f_2, float f_3,
			float f_4) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;
		float f = particleIcon.getInterpolatedU((0.1f + 1) / 4 * 16);
		float f1 = particleIcon.getInterpolatedV((0.1f + 1) / 4 * 16);

		float f5 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY) + 0.01275F;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

		int i = ((Entity) this).getBrightnessForRender(partialTicks) >> 16 & 65535;

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		float f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

		// RENDER
		tes.draw();
		mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		tes.startDrawingQuads();

		par = new FBPVector3d(f, f1, 0);

		tes.setTranslation(f5, f6, f7);
		putCube(tes, f4 / 80, i, particleRed, particleGreen, particleBlue, alpha);
		tes.setTranslation(0, 0, 0);

		tes.draw();
		Minecraft.getMinecraft().getTextureManager().bindTexture(FBP.LOCATION_PARTICLE_TEXTURE);
		tes.startDrawingQuads();
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

	public void putCube(Tessellator tes, double scale, int j, float r, float g, float b, float a) {
		float brightnessForRender = _brightnessForRender;

		float R = 0;
		float G = 0;
		float B = 0;

		for (int i = 0; i < cube.length; i += 4) {
			FBPVector3d v1 = cube[i];
			FBPVector3d v2 = cube[i + 1];
			FBPVector3d v3 = cube[i + 2];
			FBPVector3d v4 = cube[i + 3];

			R = r * brightnessForRender;
			G = g * brightnessForRender;
			B = b * brightnessForRender;

			brightnessForRender *= 0.95;

			addVt(tes, scale, v1, par.x, par.y, j, R, G, B, a);
			addVt(tes, scale, v2, par.x, par.y, j, R, G, B, a);
			addVt(tes, scale, v3, par.x, par.y, j, R, G, B, a);
			addVt(tes, scale, v4, par.x, par.y, j, R, G, B, a);
		}
	}

	private void addVt(Tessellator tes, double scale, FBPVector3d pos, double u, double v, int i, float r, float g,
			float b, float a) { // add vertex to buffer
		tes.setColorRGBA_F(r, g, b, a);
		tes.setBrightness(i);
		tes.addVertexWithUV(pos.x * scale, pos.y * scale, pos.z * scale, u, v);
	}

	FBPVector3d rotatef(FBPVector3d vec, float AngleX, float AngleY, float AngleZ) {
		double sinAngleX = MathHelper.sin(AngleX);
		double sinAngleY = MathHelper.sin(AngleY);
		double sinAngleZ = MathHelper.sin(AngleZ);

		double cosAngleX = MathHelper.cos(AngleX);
		double cosAngleY = MathHelper.cos(AngleY);
		double cosAngleZ = MathHelper.cos(AngleZ);

		vec = new FBPVector3d(vec.x, vec.y * cosAngleX - vec.z * sinAngleX, vec.y * sinAngleX + vec.z * cosAngleX);
		vec = new FBPVector3d(vec.x * cosAngleY + vec.z * sinAngleY, vec.y, vec.x * sinAngleY - vec.z * cosAngleY);
		vec = new FBPVector3d(vec.x * cosAngleZ - vec.y * sinAngleZ, vec.x * sinAngleZ + vec.y * cosAngleZ, vec.z);

		return vec;
	}
}
