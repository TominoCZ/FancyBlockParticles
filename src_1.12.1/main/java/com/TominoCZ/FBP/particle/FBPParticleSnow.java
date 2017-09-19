package com.TominoCZ.FBP.particle;

import java.util.List;

import javax.annotation.Nullable;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
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
public class FBPParticleSnow extends ParticleDigging {
	private final IBlockState sourceState;

	Minecraft mc;

	double scaleAlpha, prevParticleScale, prevParticleAlpha;

	FBPVector3d rotStep;

	FBPVector3d prevRot;
	FBPVector3d rot;

	boolean modeDebounce = false;

	double endMult = 1;

	float brightness = 1;

	Vec2f[] par;

	public FBPParticleSnow(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, IBlockState state) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);

		try {
			FBP.setSourcePos.invokeExact((ParticleDigging) this, new BlockPos(xCoordIn, yCoordIn, zCoordIn));
		} catch (Throwable e1) {
			e1.printStackTrace();
		}

		rot = new FBPVector3d();
		prevRot = new FBPVector3d();

		createRotationMatrix();

		this.motionX = xSpeedIn;
		this.motionY = -ySpeedIn;
		this.motionZ = zSpeedIn;
		this.particleGravity = 1f;
		sourceState = state;

		mc = Minecraft.getMinecraft();

		particleScale *= FBP.random.nextDouble(FBP.scaleMult - 0.25f, FBP.scaleMult + 0.25f);
		particleMaxAge = (int) FBP.random.nextDouble(120, 150);
		this.particleRed = this.particleGreen = this.particleBlue = 0.7F + (0.25F * mc.gameSettings.gammaSetting);

		scaleAlpha = particleScale * 0.75;

		this.particleAlpha = 0f;
		this.particleScale = 0f;

		this.canCollide = true;
		
		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.7, 1);
	}

	private void createRotationMatrix() {
		double rx = FBP.random.nextDouble();
		double ry = FBP.random.nextDouble();
		double rz = FBP.random.nextDouble();

		rotStep = new FBPVector3d(rx > 0.5 ? 1 : -1, ry > 0.5 ? 1 : -1, rz > 0.5 ? 1 : -1);

		rot.copyFrom(rotStep);
	}

	@Override
	public void setParticleTextureIndex(int particleTextureIndex) {

	}

	public Particle MultiplyVelocity(float multiplier) {
		this.motionX *= (double) multiplier;
		this.motionY = (this.motionY - 0.10000000149011612D) * (multiplier / 2) + 0.10000000149011612D;
		this.motionZ *= (double) multiplier;
		return this;
	}

	protected void multiplyColor(@Nullable BlockPos p_187154_1_) {
		int i = mc.getBlockColors().colorMultiplier(this.sourceState, this.world, p_187154_1_, 0);
		this.particleRed *= (float) (i >> 16 & 255) / 255.0F;
		this.particleGreen *= (float) (i >> 8 & 255) / 255.0F;
		this.particleBlue *= (float) (i & 255) / 255.0F;
	}

	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		prevRot.copyFrom(rot);

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;

		if (!mc.isGamePaused()) {
			particleAge++;

			rot.add(rotStep.multiply(FBP.rotationMult * 5));

			if (this.particleAge >= this.particleMaxAge) {
				if (FBP.randomFadingSpeed)
					particleScale *= 0.75F * endMult;
				else
					particleScale *= 0.75F;

				if (particleAlpha > 0.01 && particleScale <= scaleAlpha) {
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.65F * endMult;
					else
						particleAlpha *= 0.65F;
				}

				if (particleAlpha <= 0.01)
					setExpired();
			} else {
				if (particleScale < 1) {
					if (FBP.randomFadingSpeed)
						particleScale += 0.075F * endMult;
					else
						particleScale += 0.075F;

					if (particleScale > 1)
						particleScale = 1;
				}

				if (particleAlpha < 1) {
					if (FBP.randomFadingSpeed)
						particleAlpha += 0.045F * endMult;
					else
						particleAlpha += 0.045F;

					if (particleAlpha > 1)
						particleAlpha = 1;
				}
			}

			motionY -= 0.04D * (double) this.particleGravity;
			moveEntity(motionX, motionY, motionZ);

			motionX *= 0.9800000190734863D;

			if (motionY < -0.2) // minimal motionY
				motionY *= 0.7500000190734863D;

			motionZ *= 0.9800000190734863D;

			if (onGround) {
				motionX *= 0.680000190734863D;
				motionZ *= 0.6800000190734863D;
				
				rotStep = rotStep.multiply(0.85);

				this.particleAge += 2;
			}
		}
	}
	
	public void moveEntity(double x, double y, double z) {
		double X = x;
		double Y = y;
		double Z = z;
		double d0 = y;

		if (this.canCollide) {
			List<AxisAlignedBB> list = this.world.getCollisionBoxes(null,
					this.getBoundingBox().offset(x, y, z));

			for (AxisAlignedBB aabb : list) {
				x = aabb.calculateXOffset(this.getBoundingBox(), x);
				y = aabb.calculateYOffset(this.getBoundingBox(), y);
				z = aabb.calculateZOffset(this.getBoundingBox(), z);
			}

			this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
		} else
			this.setBoundingBox(this.getBoundingBox().offset(x, y, z));

		this.resetPositionToBB();

		this.onGround = y != Y && d0 < 0.0D;

		if (!FBP.rollParticles && !FBP.bounceOffWalls) {
			if (x != X)
				motionX *= 0.699999988079071D;
			if (z != Z)
				motionZ *= 0.699999988079071D;
		}
	}

	public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;

		float f = 0, f1 = 0, f2 = 0, f3 = 0;

		if (particleTexture != null) {
			if (!FBP.cartoonMode) {
				f = particleTexture.getInterpolatedU((double) (particleTextureJitterX / 4 * 16));
				f2 = particleTexture.getInterpolatedV((double) (particleTextureJitterY / 4 * 16));
			}

			f1 = particleTexture.getInterpolatedU((double) ((particleTextureJitterX + 1) / 4 * 16));
			f3 = particleTexture.getInterpolatedV((double) ((particleTextureJitterY + 1) / 4 * 16));
		} else {
			f = ((float) particleTextureIndexX + particleTextureJitterX / 4) / 16;
			f1 = f + 0.015609375F;
			f2 = ((float) particleTextureIndexY + particleTextureJitterY / 4) / 16;
			f3 = f2 + 0.015609375F;
		}

		float f5 = (float) (prevPosX + (posX - prevPosX) * (double) partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY) + 0.01275F;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);

		int i = getBrightnessForRender(partialTicks);

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		float f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

		FBPVector3d smoothRot = new FBPVector3d(0, 0, 0);

		if (FBP.rotationMult > 0) {
			smoothRot.y = rot.y;
			smoothRot.z = rot.z;

			if (!FBP.randomRotation)
				smoothRot.x = rot.x;

			// SMOOTH ROTATION
			if (FBP.smoothTransitions && !FBP.frozen) {
				FBPVector3d vec = rot.partialVec(prevRot, partialTicks);

				if (FBP.randomRotation) {
					smoothRot.y = vec.y;
					smoothRot.z = vec.z;
				} else {
					smoothRot.x = vec.x;
				}
			}
		}

		// RENDER
		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();

		par = new Vec2f[] { new Vec2f(f1, f3), new Vec2f(f1, f2), new Vec2f(f, f2), new Vec2f(f, f3) };

		worldRendererIn.setTranslation(f5, f6, f7);
		putCube(worldRendererIn, f4 / 20, smoothRot, i >> 16 & 65535, i & 65535, particleRed, particleGreen,
				particleBlue, alpha, FBP.cartoonMode);

		worldRendererIn.setTranslation(0, 0, 0);
	}

	public void putCube(BufferBuilder buff, double scale, FBPVector3d rotVec, int j, int k, float r, float g, float b,
			float a, boolean cartoon) {
		brightness = 1;

		float R = 0;
		float G = 0;
		float B = 0;

		float radsX = (float) Math.toRadians(rotVec.x);
		float radsY = (float) Math.toRadians(rotVec.y);
		float radsZ = (float) Math.toRadians(rotVec.z);

		for (int i = 0; i < FBP.CUBE.length; i += 4) {
			Vec3d v1 = FBP.CUBE[i];
			Vec3d v2 = FBP.CUBE[i + 1];
			Vec3d v3 = FBP.CUBE[i + 2];
			Vec3d v4 = FBP.CUBE[i + 3];

			v1 = rotatef(v1, radsX, radsY, radsZ);
			v2 = rotatef(v2, radsX, radsY, radsZ);
			v3 = rotatef(v3, radsX, radsY, radsZ);
			v4 = rotatef(v4, radsX, radsY, radsZ);

			R = r * brightness;
			G = g * brightness;
			B = b * brightness;

			brightness *= 0.935;

			if (!cartoon) {
				addVt(buff, scale, v1, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(buff, scale, v2, par[1].x, par[1].y, j, k, R, G, B, a);
				addVt(buff, scale, v3, par[2].x, par[2].y, j, k, R, G, B, a);
				addVt(buff, scale, v4, par[3].x, par[3].y, j, k, R, G, B, a);
			} else {
				addVt(buff, scale, v1, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(buff, scale, v2, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(buff, scale, v3, par[0].x, par[0].y, j, k, R, G, B, a);
				addVt(buff, scale, v4, par[0].x, par[0].y, j, k, R, G, B, a);
			}
		}
	}

	private void addVt(BufferBuilder buff, double scale, Vec3d pos, double u, double v, int j, int k, float r, float g,
			float b, float a) {
		buff.pos(pos.x * scale, pos.y * scale, pos.z * scale).tex(u, v).color(r, g, b, a).lightmap(j, k)
				.endVertex();
	}

	Vec3d rotatef(Vec3d vec, float AngleX, float AngleY, float AngleZ) {
		FBPVector3d sin = new FBPVector3d(MathHelper.sin(AngleX), MathHelper.sin(AngleY), MathHelper.sin(AngleZ));
		FBPVector3d cos = new FBPVector3d(MathHelper.cos(AngleX), MathHelper.cos(AngleY), MathHelper.cos(AngleZ));

		vec = new Vec3d(vec.x, vec.y * cos.x - vec.z * sin.x, vec.y * sin.x + vec.z * cos.x);
		vec = new Vec3d(vec.x * cos.y + vec.z * sin.y, vec.y, vec.x * sin.y - vec.z * cos.y);
		vec = new Vec3d(vec.x * cos.z - vec.y * sin.z, vec.x * sin.z + vec.y * cos.z, vec.z);

		return vec;
	}

	public int getBrightnessForRender(float p_189214_1_) {
		int i = super.getBrightnessForRender(p_189214_1_);
		int j = 0;

		if (this.world.isBlockLoaded(new BlockPos(posX, posY, posZ))) {
			j = this.world.getCombinedLight(new BlockPos(posX, posY, posZ), 0);
		}

		return i == 0 ? j : i;
	}
}