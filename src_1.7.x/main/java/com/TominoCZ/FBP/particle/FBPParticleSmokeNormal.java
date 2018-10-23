package com.TominoCZ.FBP.particle;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.block.FBPBlockPos;
import com.TominoCZ.FBP.util.FBPRenderUtil;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class FBPParticleSmokeNormal extends EntitySmokeFX {
	Minecraft mc;

	double startScale, scaleAlpha, prevParticleScale, prevParticleAlpha;
	double endMult = 0.75;

	FBPVector3d par;

	FBPVector3d[] cube;

	EntitySmokeFX original;

	protected FBPParticleSmokeNormal(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, final double mX,
			final double mY, final double mZ, float scale, EntitySmokeFX original) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, mX, mY, mZ, scale);

		this.original = original;

		this.motionX = mX;
		this.motionY = mY;
		this.motionZ = mZ;

		mc = Minecraft.getMinecraft();
		this.setParticleIcon(Blocks.snow.getIcon(0, 0));

		scaleAlpha = particleScale * 0.85;

		FBPBlockPos pos = new FBPBlockPos(xCoordIn, yCoordIn, zCoordIn);

		Block b = worldIn.getBlock(pos.getX(), pos.getY(), pos.getZ());

		if (b == Blocks.fire) {
			particleScale *= 0.65f;
			this.particleGravity *= 0.25f;

			this.motionX = FBP.random.nextDouble(-0.05, 0.05);
			this.motionY = FBP.random.nextDouble() * 0.5;
			this.motionZ = FBP.random.nextDouble(-0.05, 0.05);

			this.motionY *= 0.35f;

			scaleAlpha = particleScale * 0.5;

			particleMaxAge = FBP.random.nextInt(7, 18);
		} else if (b == Blocks.torch) {
			particleScale *= 0.45f;

			this.motionX = FBP.random.nextDouble(-0.05, 0.05);
			this.motionY = FBP.random.nextDouble() * 0.5;
			this.motionZ = FBP.random.nextDouble(-0.05, 0.05);

			this.motionX *= 0.925f;
			this.motionY = 0.005f;
			this.motionZ *= 0.925f;

			this.particleRed = 0.275f;
			this.particleGreen = 0.275f;
			this.particleBlue = 0.275f;

			scaleAlpha = particleScale * 0.75;

			particleMaxAge = FBP.random.nextInt(5, 10);
		} else {
			particleScale = scale;
			motionY *= 0.935;
		}

		particleScale *= FBP.scaleMult;

		startScale = particleScale;

		float angleY = rand.nextFloat() * 80;

		cube = new FBPVector3d[FBP.CUBE.length];

		for (int i = 0; i < FBP.CUBE.length; i++) {
			FBPVector3d vec = FBP.CUBE[i];
			cube[i] = FBPRenderUtil.rotatef_d(vec, 0, angleY, 0);
		}

		particleAlpha = 1f;

		if (FBP.randomFadingSpeed)
			endMult = MathHelper.clamp_double(FBP.random.nextDouble(0.425, 1.15), 0.5432, 1);

		multipleParticleScaleBy(1);
	}

	@Override
	public EntityFX multipleParticleScaleBy(float scale) {
		EntityFX p = super.multipleParticleScaleBy(scale);

		float f = particleScale / 20;

		this.boundingBox
				.setBB(AxisAlignedBB.getBoundingBox(posX - f, posY - f, posZ - f, posX + f, posY + f, posZ + f));

		return p;
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	public void setMaxAge(int age) {
		this.particleMaxAge = age;
	}

	@Override
	public void onUpdate() {
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;

		if (!FBP.fancySmoke)
			this.isDead = true;

		particleAge++;

		if (this.particleAge >= this.particleMaxAge) {
			if (FBP.randomFadingSpeed)
				particleScale *= 0.887654321F * endMult;
			else
				particleScale *= 0.887654321F;

			if (particleAlpha > 0.01 && particleScale <= scaleAlpha) {
				if (FBP.randomFadingSpeed)
					particleAlpha *= 0.7654321F * endMult;
				else
					particleAlpha *= 0.7654321F;
			}

			if (particleAlpha <= 0.01)
				setDead();
		}

		this.motionY += 0.004D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);

		if (this.posY == this.prevPosY) {
			this.motionX *= 1.1D;
			this.motionZ *= 1.1D;
		}

		this.motionX *= 0.9599999785423279D;
		this.motionY *= 0.9599999785423279D;
		this.motionZ *= 0.9599999785423279D;

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
	public void renderParticle(Tessellator tes, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
			float rotationXY, float rotationXZ) {
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

		// RENDER
		par = new FBPVector3d(f, f1, 0);

		GL11.glDepthMask(true);
		tes.setTranslation(f5, f6, f7);
		putCube(tes, f4 / 20, 240, particleRed, particleGreen, particleBlue, alpha);
		tes.setTranslation(0, 0, 0);
	}

	public void putCube(Tessellator tes, double scale, int brightness, float r, float g, float b, float a) {
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

			brightnessForRender *= 0.875;

			addVt(tes, scale, v1, par.x, par.y, brightness, R, G, B, a);
			addVt(tes, scale, v2, par.x, par.y, brightness, R, G, B, a);
			addVt(tes, scale, v3, par.x, par.y, brightness, R, G, B, a);
			addVt(tes, scale, v4, par.x, par.y, brightness, R, G, B, a);
		}
	}

	private void addVt(Tessellator tes, double scale, FBPVector3d pos, double u, double v, int brightness, float r,
			float g, float b, float a) { // add vertex to buffer
		tes.setColorRGBA_F(r, g, b, a);
		tes.setBrightness(brightness);
		tes.addVertexWithUV(pos.x * scale, pos.y * scale, pos.z * scale, u, v);
	}

	@Override
	public void setDead() {
		this.isDead = true;

		original.setDead();
	}
}
