package com.TominoCZ.FBP.particle;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.math.FBPMathHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class FBPParticleDigging extends Particle {
	private final IBlockState sourceState;

	int j2, k2, vecIndex;

	double endScale, scaleAlpha, prevParticleScale, prevParticleAlpha, prevMotionX, prevMotionZ;

	double angleX, angleY, angleZ, prevAngleX, prevAngleY, prevAngleZ, randomXd, randomYd, randomZd;

	boolean modeDebounce = false, wasFrozen = false;

	boolean dying = false;
	boolean spawned = false;
	double[][] par;

	double speedMult = 1;
	double endMult = 1;

	double[] legacySpeed;

	public FBPParticleDigging(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, IBlockState state) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		sourceState = state;
		particleGravity = (float) (state.getBlock().blockParticleGravity * FBP.gravityMult);
		particleScale = (float) FBP.random.nextDouble(FBP.minScale + 0.399D, FBP.maxScale + 0.4D);
		particleMaxAge = (int) FBP.random.nextDouble(FBP.minAge, FBP.maxAge + 0.5);

		endScale = particleScale / 3;
		scaleAlpha = particleScale / 1.65;

		// GET THE TOP TEXTURE OF THE BLOCK
		if (FBP.inheritBlockTopTexture && posY - ((int) posY) <= 0.105 && posY - ((int) posY) >= 0) {
			List<BakedQuad> quads = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
					.getModelForState(state).getQuads(state, EnumFacing.UP, rand.nextLong());

			if (!quads.isEmpty()) {
				this.particleTexture = quads.get(0).getSprite();
				multiplyColor(new BlockPos(xCoordIn, yCoordIn, zCoordIn));
			}
		}

		if (((particleTexture == null) ? true : (particleTexture.getIconName() == "missingno")))
			particleTexture = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
					.getTexture(state);

		if (!FBP.legacyMode) {
			// IF PARTICLE IS 1/10 OF A BLOCK ABOVE THE SOURCE BLOCK

			double blockHeight;

			if ((blockHeight = state.getBlock().getBoundingBox(state, worldIn,
					new BlockPos(posX, posY, posZ)).maxY) != 1) {
				if (posY - ((int) posY) - 0.1F == blockHeight)
					setMotion();
			} else if ((posY - ((int) posY) <= 0.105 && posY - ((int) posY) - 0.1F == 0) || posY - ((int) posY) > 1)
				setMotion();

			// ROTATE ABOUT DIRECTION AXIS
			calculateYAngle();
		}

		if (!state.isNormalCube())
			multiplyColor(new BlockPos(xCoordIn, yCoordIn, zCoordIn));

		if (FBP.randomFadingSpeed)
			endMult = FBP.random.nextDouble(0.88, 1.025);

		if (FBP.legacyMode) {
			randomXd = FBP.random.nextDouble();
			randomYd = FBP.random.nextDouble();
			randomZd = FBP.random.nextDouble();

			double speed = (randomZd + 0.5) * FBP.rotationMult;

			legacySpeed = new double[] { 8 * speed, 15 * speed, 4 * speed, 10 * speed, 5 * speed, 12 * speed };
		}

		modeDebounce = !FBP.legacyMode;
	}

	protected void multiplyColor(@Nullable BlockPos p_187154_1_) {
		int i = Minecraft.getMinecraft().getBlockColors().colorMultiplier(sourceState, worldObj, p_187154_1_, 0);
		particleRed *= (float) (i >> 16 & 255) / 255.0F;
		particleGreen *= (float) (i >> 8 & 255) / 255.0F;
		particleBlue *= (float) (i & 255) / 255.0F;
	}

	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		if (!FBP.frozen && FBP.bounceOffWalls && !Minecraft.getMinecraft().isGamePaused()) {
			if (!wasFrozen) {
				if (prevPosX == posX && prevMotionX != 0)
					motionX = -prevMotionX * 0.875;
				if (prevPosZ == posZ && prevMotionZ != 0)
					motionZ = -prevMotionZ * 0.875;

				if (!FBP.legacyMode && prevMotionX != 0 && prevMotionZ != 0)
					calculateYAngle();
			} else
				wasFrozen = false;
		}
		if (FBP.frozen && FBP.bounceOffWalls && !wasFrozen)
			wasFrozen = true;

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevAngleX = angleX;
		prevAngleY = angleY;
		prevAngleZ = angleZ;

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;

		if (!Minecraft.getMinecraft().isGamePaused() && !FBP.frozen) {
			if (!FBP.legacyMode) {
				if (!modeDebounce) {
					modeDebounce = true;
					angleX = 0;
					angleY = 0;
					angleZ = 0;

					calculateYAngle();
				}

				if (!isCollided) {
					if (motionX > 0) { // CHANGE ANGLES
						if (motionZ > 0)
							angleX -= FBP.rotationMult * speedMult;
						else if (motionZ < 0)
							angleX += FBP.rotationMult * speedMult;
					} else if (motionX < 0) {
						if (motionZ < 0)
							angleX += FBP.rotationMult * speedMult;
						else if (motionZ > 0) {
							angleX -= FBP.rotationMult * speedMult;
						}
					}
				}
			} else {
				if (randomXd == 0 && randomYd == 0 && randomZd == 0) {
					randomXd = FBP.random.nextDouble();
					randomYd = FBP.random.nextDouble();
					randomZd = FBP.random.nextDouble();

					if (legacySpeed == null || legacySpeed.length < 6) {
						double speed = (randomZd + 0.5) * FBP.rotationMult;
						legacySpeed = new double[] { 8 * speed, 15 * speed, 4 * speed, 10 * speed, 5 * speed,
								12 * speed };
					}
				}

				if (!isCollided) {
					if (randomXd <= randomZd)
						angleX += legacySpeed[0];
					else
						angleX -= legacySpeed[1];

					if (randomYd <= randomXd)
						angleY += legacySpeed[2];
					else
						angleY -= legacySpeed[3];

					if (randomZd <= randomYd)
						angleZ += legacySpeed[4];
					else
						angleZ -= legacySpeed[5];
				}
			}

			if (particleAge++ >= particleMaxAge) {
				if (!dying)
					dying = true;

				if (FBP.randomFadingSpeed)
					particleScale *= 0.825F * endMult;
				else
					particleScale *= 0.825F;

				if (particleScale < endScale)
					setExpired();
				else if (particleScale < scaleAlpha) {// scale / 1.65) {
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.565F * endMult;
					else
						particleAlpha *= 0.565F;
				}
			}

			motionY -= 0.04D * (double) particleGravity;

			moveEntity(motionX, motionY, motionZ); // <<-- THIS CAN SET MOTION
													// TO ZERO
			if (motionX != 0)
				prevMotionX = motionX;
			if (motionZ != 0)
				prevMotionZ = motionZ;

			motionX *= 0.9800000190734863D;
			motionY *= 0.9800000190734863D;
			motionZ *= 0.9800000190734863D;

			// PHYSICS
			if (FBP.entityCollision) {
				worldObj.getEntitiesInAABBexcluding(null, this.getEntityBoundingBox(), null).forEach(entityIn -> {
					if (!entityIn.noClip) {
						double d0 = this.posX - entityIn.posX;
						double d1 = this.posZ - entityIn.posZ;
						double d2 = MathHelper.abs_max(d0, d1);

						if (d2 >= 0.009999999776482582D) {
							d2 = (double) MathHelper.sqrt_double(d2);
							d0 = d0 / d2;
							d1 = d1 / d2;

							double d3 = 1.0D / d2;

							if (d3 > 1.0D) {
								d3 = 1.0D;
							}

							this.motionX += d0 * d3 / 20;
							this.motionZ += d1 * d3 / 20;

							if (!FBP.legacyMode)
								calculateYAngle(); // ROTATE THE PARTICLE
													// DEPENDING
													// ON THE NEW MOTION
													// DIRECTION
							if (!FBP.frozen)
								this.isCollided = false;
						}
					}
				});
			}
		}

		if (isCollided) {
			if (FBP.bounceOffWalls) {
				motionX *= 0.819152044289D;
				motionZ *= 0.819152044289D;
			} else {
				motionX *= 0.699999988079071D;
				motionZ *= 0.699999988079071D;
			}
		}

		if (modeDebounce && FBP.legacyMode)
			modeDebounce = false;
	}

	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;

		float f = 0, f1 = 0, f2 = 0, f3 = 0;

		float f4 = particleScale;

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
		float f6 = (float) (prevPosY + (posY - prevPosY) * (double) partialTicks - interpPosY) + 0.0125F;
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * (double) partialTicks - interpPosZ);

		int i = getBrightnessForRender(partialTicks);

		if (!spawned && !FBP.cartoonMode) {
			spawned = true;

			par = new double[][] { { f1, f3 }, { f1, f2 }, { f, f2 }, { f, f3 } };
		}

		double AngleX = 0, AngleY = 0, AngleZ = 0;
		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		if (dying && FBP.smoothTransitions && !FBP.frozen) {
			f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

			alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);
		}

		if (FBP.rotationMult > 0) {
			AngleX = angleX;
			AngleY = angleY;
			AngleZ = angleZ;

			// SMOOTH ROTATION
			if (FBP.smoothTransitions && !FBP.frozen) {
				AngleX = prevAngleX + (angleX - prevAngleX) * partialTicks;

				if (FBP.legacyMode) {
					AngleY = prevAngleY + (angleY - prevAngleY) * partialTicks;

					AngleZ = prevAngleZ + (angleZ - prevAngleZ) * partialTicks;
				}
			}
		}

		// RENDER
		worldRendererIn.setTranslation(f5, f6, f7);

		if (FBP.cartoonMode) {
			renderCartonQuads(worldRendererIn, FBPMathHelper.rotateCubeXYZ(AngleX, AngleY, AngleZ, f4 / 10),
					i >> 16 & 65535, i & 65535, f1, f3, alpha);
		} else {
			renderQuads(worldRendererIn, FBPMathHelper.rotateCubeXYZ(AngleX, AngleY, AngleZ, f4 / 10), i >> 16 & 65535,
					i & 65535, alpha);
		}

		worldRendererIn.setTranslation(0, 0, 0);
	}

	public int getBrightnessForRender(float p_189214_1_) {
		int i = super.getBrightnessForRender(p_189214_1_);
		int j = 0;

		if (worldObj.isBlockLoaded(new BlockPos(posX, posY, posZ))) {
			j = (int) worldObj.getCombinedLight(new BlockPos(posX, posY, posZ), 0);
		}

		return i == 0 ? j : i;
	}

	void renderCartonQuads(VertexBuffer buf, ArrayList<double[]> vec, int j, int k, float f1, float f2, float alpha) {
		j2 = (int) (j * 0.9D);
		k2 = (int) (k * 0.75D);

		vec.forEach(vector -> {
			if (vecIndex == 4) {
				j2 *= 0.965D;
				k2 *= 0.975D;

				vecIndex = 0;
			}

			buf.pos(vector[0], vector[1], vector[2]).tex(f1, f2).color(particleRed, particleGreen, particleBlue, alpha)
					.lightmap(j2, k2).endVertex();

			vecIndex++;
		});

		vecIndex = 0;
	}

	void renderQuads(VertexBuffer buf, ArrayList<double[]> vec, int j, int k, float alpha) {
		j2 = (int) (j * 0.925D);
		k2 = (int) (k * 0.65D);

		vec.forEach(vector -> {
			if (vecIndex == 4) {
				j2 *= 0.975D;
				k2 *= 0.975D;

				vecIndex = 0;
			}

			buf.pos(vector[0], vector[1], vector[2]).tex(par[vecIndex][0], par[vecIndex][1])
					.color(particleRed, particleGreen, particleBlue, alpha).lightmap(j2, k2).endVertex();

			vecIndex++;
		});

		vecIndex = 0;
	}

	private void calculateYAngle() {
		double angleSin = Math.toDegrees(
				Math.asin(motionX / (speedMult = MathHelper.sqrt_double(motionX * motionX + motionZ * motionZ))));

		speedMult *= 200;

		if (motionX > 0) {
			if (motionZ > 0)
				angleY = -angleSin;
			else
				angleY = angleSin;

		} else {
			if (motionZ > 0)
				angleY = -angleSin;
			else
				angleY = angleSin;
		}
	}

	private void setMotion() {
		double x1 = MathHelper.abs((float) FBPMathHelper.add((int) posX, 0.5)), x2 = MathHelper.abs((float) posX);
		double z1 = MathHelper.abs((float) FBPMathHelper.add((int) posZ, 0.5)), z2 = MathHelper.abs((float) posZ);

		double mX = MathHelper.abs((float) motionX), mY = MathHelper.abs((float) motionY),
				mZ = MathHelper.abs((float) motionZ);

		if (x2 > x1) {
			if (posX > 0)
				motionX = mX;
			else if (posX < 0)
				motionX = -mX;
		} else {
			if (posX > 0)
				motionX = -mX;
			else if (posX < 0)
				motionX = mX;
		}

		if (z2 > z1) {
			if (posZ > 0)
				motionZ = mZ;
			else if (posZ < 0)
				motionZ = -mZ;
		} else {
			if (posZ > 0)
				motionZ = -mZ;
			else if (posZ < 0)
				motionZ = mZ;
		}
	}
}