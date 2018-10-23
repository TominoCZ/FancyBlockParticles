package com.TominoCZ.FBP.particle;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.block.FBPBlockPos;
import com.TominoCZ.FBP.util.FBPRenderUtil;
import com.TominoCZ.FBP.vector.FBPVector3d;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class FBPParticleFlame extends EntityFlameFX {
	Minecraft mc;

	double startScale, scaleAlpha, prevParticleScale, prevParticleAlpha;
	double endMult = 1;

	boolean spawnAnother = true;

	FBPVector3d startPos;

	FBPVector3d[] cube;

	protected FBPParticleFlame(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double mX, double mY,
			double mZ, boolean spawnAnother) {
		super(worldIn, xCoordIn, yCoordIn - 0.06, zCoordIn, mX, mY, mZ);
		FBPBlockPos pos = new FBPBlockPos(xCoordIn, yCoordIn, zCoordIn);

		Block b = worldIn.getBlock(pos.getX(), pos.getY(), pos.getZ());

		this.spawnAnother = spawnAnother;
		this.particleIcon = Blocks.snow.getIcon(1, 0);

		if (b != Blocks.torch)
			spawnAnother = false;

		startPos = new FBPVector3d(posX, posY, posZ);

		mc = Minecraft.getMinecraft();

		this.motionY = 0.00585f;
		this.particleGravity = -0.05f;

		particleScale *= FBP.scaleMult * 2.5f;
		particleMaxAge = FBP.random.nextInt(3, 5);

		this.particleRed = 1f;
		this.particleGreen = 1f;
		this.particleBlue = 0f;

		float angleY = rand.nextFloat() * 80;

		cube = new FBPVector3d[FBP.CUBE.length];

		for (int i = 0; i < FBP.CUBE.length; i++) {
			FBPVector3d vec = FBP.CUBE[i];
			cube[i] = FBPRenderUtil.rotatef_d(vec, 0, angleY, 0);
		}

		particleAlpha = 1f;

		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.9875, 1);

		multipleParticleScaleBy(1);
	}

	@Override
	public EntityFX multipleParticleScaleBy(float scale) {
		EntityFX p = super.multipleParticleScaleBy(scale);

		startScale = particleScale;
		scaleAlpha = particleScale * 0.35;

		float f = particleScale / 80 / 2;

		this.boundingBox
				.setBB(AxisAlignedBB.getBoundingBox(posX - f, posY - f, posZ - f, posX + f, posY + f, posZ + f));

		return p;
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

		if (++this.particleAge >= this.particleMaxAge) {
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

				mc.effectRenderer.addEffect(
						new FBPParticleFlame(worldObj, startPos.x, startPos.y, startPos.z, 0, 0, 0, spawnAnother));
			}
		}

		motionY -= 0.02D * this.particleGravity;
		moveEntity(0, motionY, 0);
		motionY *= 0.95D;

		if (this.onGround) {
			this.motionX *= 0.899999988079071D;
			this.motionZ *= 0.899999988079071D;
		}
	}

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
		this.posY = (axisalignedbb.minY + axisalignedbb.maxY) / 2.0D;
		this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;

		this.onGround = y != Y;
	}

	@Override
	public void renderParticle(Tessellator tes, float partialTicks, float f_0, float f_1, float f_2, float f_3,
			float f_4) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;

		float f = particleIcon.getInterpolatedU((0.1f + 1) / 4 * 16);
		float f1 = particleIcon.getInterpolatedV((0.1f + 1) / 4 * 16);

		float f5 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

		float alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);

		// SMOOTH TRANSITION
		float f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

		if (this.particleAge >= this.particleMaxAge)
			this.particleGreen = (float) (f4 / startScale);

		// RENDER
		tes.draw();
		mc.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		tes.startDrawingQuads();

		GL11.glDepthMask(true);
		tes.setTranslation(f5, f6, f7);
		putCube(tes, f4 / 80, 240, particleRed, particleGreen, particleBlue, alpha, f, f1);
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

	public void putCube(Tessellator tes, double scale, int j, float r, float g, float b, float a, float f0, float f1) {
		float brightnessForRender = 1;

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

			addVt(tes, scale, v1, f0, f1, j, R, G, B, a);
			addVt(tes, scale, v2, f0, f1, j, R, G, B, a);
			addVt(tes, scale, v3, f0, f1, j, R, G, B, a);
			addVt(tes, scale, v4, f0, f1, j, R, G, B, a);
		}
	}

	private void addVt(Tessellator tes, double scale, FBPVector3d pos, double u, double v, int i, float r, float g,
			float b, float a) { // add vertex to buffer
		tes.setColorRGBA_F(r, g, b, a);
		tes.setBrightness(i);
		tes.addVertexWithUV(pos.x * scale, pos.y * scale, pos.z * scale, u, v);
	}
}
