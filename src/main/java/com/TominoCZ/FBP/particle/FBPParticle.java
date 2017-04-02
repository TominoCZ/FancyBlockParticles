package com.TominoCZ.FBP.particle;

import java.util.List;

import javax.annotation.Nullable;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.TominoCZ.FBP.math.FBPMathHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticle extends ParticleDigging {
	private final IBlockState sourceState;

	Minecraft mc;

	int vecIndex;

	double endScale, scaleAlpha, prevParticleScale, prevParticleAlpha, prevMotionZ;

	double prevMotionX;

	double angleX, angleY, angleZ, prevAngleX, prevAngleY, prevAngleZ, randomXd, randomYd, randomZd;

	boolean modeDebounce = false, wasFrozen = false, destroyed = false;

	boolean spawned = false, dying = false, killToggle = false;
	double[][] par;

	double endMult = 1;

	float brightness = 1;

	protected FBPParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, IBlockState state, @Nullable EnumFacing facing, float scale) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, state);
		this.sourceState = state;

		ReflectionHelper.setPrivateValue(ParticleDigging.class, this, new BlockPos(xCoordIn, yCoordIn, zCoordIn),
				"field_181019_az", "sourcePos");

		Block b = state.getBlock();

		mc = Minecraft.getMinecraft();

		particleGravity = (float) (b.blockParticleGravity * FBP.gravityMult);

		if (scale > -1) {
			particleScale = scale;
			setMotion();
		}

		particleScale *= FBP.scaleMult * 2.0F;
		particleMaxAge = (int) FBP.random.nextDouble(FBP.minAge, FBP.maxAge + 0.5);
		this.particleRed = this.particleGreen = this.particleBlue = 0.75F + (0.2F * mc.gameSettings.gammaSetting);

		endScale = particleScale / 3.25;
		scaleAlpha = particleScale / 1.5; // 1.65

		BlockModelShapes blockModelShapes = mc.getBlockRendererDispatcher().getBlockModelShapes();

		// GET THE TOP TEXTURE OF THE BLOCK
		if (!(destroyed = (facing == null))) {
			try {
				List<BakedQuad> quads = blockModelShapes.getModelForState(state).getQuads(state, facing, 0);

				if (quads != null && !quads.isEmpty()) {
					this.particleTexture = quads.get(0).getSprite();

					if (!state.isNormalCube() || (b.equals(Blocks.GRASS) && facing.equals(EnumFacing.UP)))
						multiplyColor(new BlockPos(xCoordIn, yCoordIn, zCoordIn));
				}
			} catch (Exception e) {
			}
		} else
			this.setParticleTexture(blockModelShapes.getTexture(state));

		if (particleTexture == null || particleTexture.getIconName() == "missingno")
			particleTexture = blockModelShapes.getTexture(state);

		if (!state.isNormalCube())
			multiplyColor(new BlockPos(xCoordIn, yCoordIn, zCoordIn));

		if (FBP.randomFadingSpeed)
			endMult *= FBP.random.nextDouble(0.85, 1.1);

		if (!(modeDebounce = !FBP.legacyMode)) {
			randomXd = FBP.random.nextDouble();
			randomYd = FBP.random.nextDouble();
			randomZd = FBP.random.nextDouble();
		} else
			calculateYAngle();
	}

	protected void multiplyColor(@Nullable BlockPos p_187154_1_) {
		int i = mc.getBlockColors().colorMultiplier(this.sourceState, this.worldObj, p_187154_1_, 0);
		this.particleRed *= (float) (i >> 16 & 255) / 255.0F;
		this.particleGreen *= (float) (i >> 8 & 255) / 255.0F;
		this.particleBlue *= (float) (i & 255) / 255.0F;
	}

	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		if (!FBP.frozen && FBP.bounceOffWalls && !mc.isGamePaused()) {
			if (!wasFrozen) {
				if (prevPosX == posX && MathHelper.abs((float) prevMotionX) > 0.00005D)
					motionX = -prevMotionX;
				if (prevPosZ == posZ && MathHelper.abs((float) prevMotionZ) > 0.00005D)
					motionZ = -prevMotionZ;

				if (!FBP.legacyMode && MathHelper.abs((float) prevMotionX) > 0.00005D
						&& MathHelper.abs((float) prevMotionZ) > 0.00005D)
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

		if (!mc.isGamePaused() && (!FBP.frozen || killToggle)) {
			boolean allowedToMove = MathHelper.abs((float) motionX) > 0.00005D;

			if (!killToggle) {
				if (!FBP.legacyMode) {
					if (!modeDebounce) {
						modeDebounce = true;
						angleX = 0;
						angleY = 0;
						angleZ = 0;

						calculateYAngle();
					}
					if (!isCollided || FBP.rollParticles) {
						if (allowedToMove) {
							double step = FBP.rotationMult * getMult();

							if (motionX > 0) { // CHANGE ANGLES
								if (motionZ > 0)
									angleX -= step;
								else if (motionZ < 0)
									angleX += step;
							} else if (motionX < 0) {
								if (motionZ < 0)
									angleX += step;
								else if (motionZ > 0) {
									angleX -= step;
								}
							}
						}
					}
				} else {
					if (modeDebounce) {
						modeDebounce = false;
						randomXd = FBP.random.nextDouble();
						randomYd = FBP.random.nextDouble();
						randomZd = FBP.random.nextDouble();
					}

					if (!isCollided || FBP.rollParticles) {
						if (allowedToMove) {
							double step = FBP.rotationMult * getMult();

							if (randomXd <= 0.5)
								angleX += step;
							else
								angleX -= step;

							if (randomYd <= 0.5)
								angleY += step;
							else
								angleY -= step;

							if (randomZd <= 0.5)
								angleZ += step;
							else
								angleZ -= step;
						}
					}
				}
			}

			if (!FBP.infiniteDuration)
				particleAge++;

			if (this.particleAge >= this.particleMaxAge || killToggle) {
				if (!dying)
					dying = true;

				if (FBP.randomFadingSpeed)
					particleScale *= 0.825F * endMult;
				else
					particleScale *= 0.825F;

				if (particleScale < endScale)
					setExpired();
				else if (particleScale < scaleAlpha) {
					if (FBP.randomFadingSpeed)
						particleAlpha *= 0.565F * endMult;
					else
						particleAlpha *= 0.565F;
				}
			}

			if (!killToggle) {
				motionY -= 0.04D * (double) particleGravity;

				if (allowedToMove)
					moveEntity(motionX, motionY, motionZ, false); // <<-- THIS
				else
					moveEntity(0, motionY, 0, true);
				// CAN SET
				// MOTION
				// TO ZERO
				if (motionX != 0)
					prevMotionX = motionX;
				if (motionZ != 0)
					prevMotionZ = motionZ;

				if (allowedToMove) {
					motionX *= 0.9800000190734863D;
					motionZ *= 0.9800000190734863D;
				}

				motionY *= 0.9800000190734863D;

				// PHYSICS
				if (FBP.entityCollision) {
					worldObj.getEntitiesWithinAABB(Entity.class, this.getEntityBoundingBox()).forEach(entityIn -> {
						if (!entityIn.noClip) {
							double d0 = this.posX - entityIn.posX;
							double d1 = this.posZ - entityIn.posZ;
							double d2 = MathHelper.abs_max(d0, d1);

							if (d2 >= 0.009999999776482582D) {
								d2 = (double) Math.sqrt(d2);
								d0 /= d2;
								d1 /= d2;

								double d3 = 1.0D / d2;

								if (d3 > 1.0D)
									d3 = 1.0D;

								this.motionX += d0 * d3 / 20;
								this.motionZ += d1 * d3 / 20;

								if (!FBP.legacyMode)
									calculateYAngle();
								if (!FBP.frozen)
									this.isCollided = false;
							}
						}
					});
				}

				if (isCollided) {
					if (FBP.rollParticles) {
						motionX *= 0.932515086137662D;
						motionZ *= 0.932515086137662D;
					} else {
						motionX *= 0.699999988079071D;
						motionZ *= 0.699999988079071D;
					}
				}
			}
		}
	}

	public void moveEntity(double x, double y, double z, boolean YOnly) {
		double X = x;
		double Y = y;
		double Z = z;
		double d0 = y;

		if (this.canCollide) {
			List<AxisAlignedBB> list = this.worldObj.getCollisionBoxes(this.getEntityBoundingBox().addCoord(x, y, z));

			for (AxisAlignedBB aabb : list) {
				y = aabb.calculateYOffset(this.getEntityBoundingBox(), y);

				if (!YOnly) {
					x = aabb.calculateXOffset(this.getEntityBoundingBox(), x);
					z = aabb.calculateZOffset(this.getEntityBoundingBox(), z);
				}
			}
			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(YOnly ? 0.0D : x, y, YOnly ? 0.0D : z));
		} else
			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));

		this.resetPositionToBB();

		this.isCollided = y != Y && d0 < 0.0D;

		if (!FBP.rollParticles && !FBP.bounceOffWalls) {
			if (x != X)
				this.motionX = 0.0D;

			if (z != Z)
				this.motionZ = 0.0D;
		}
	}

	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;
		if (FBPKeyBindings.FBPSweep.isKeyDown() && !killToggle)
			killToggle = true;

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

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		if ((dying && FBP.smoothTransitions && !FBP.frozen) || (FBP.frozen && killToggle && FBP.smoothTransitions)) {
			f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

			alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);
		}

		double AngleX = 0, AngleY = 0, AngleZ = 0;

		if (FBP.rotationMult > 0) {
			AngleX = angleX;
			AngleY = angleY;
			AngleZ = angleZ;

			// SMOOTH ROTATION
			if (FBP.smoothTransitions && (!this.isCollided || FBP.rollParticles) && !FBP.frozen) {
				AngleX = prevAngleX + (angleX - prevAngleX) * partialTicks;

				if (FBP.legacyMode) {
					AngleY = prevAngleY + (angleY - prevAngleY) * partialTicks;

					AngleZ = prevAngleZ + (angleZ - prevAngleZ) * partialTicks;
				}
			}
		}

		// RENDER
		worldRendererIn.setTranslation(f5, f6, f7);

		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();

		List<double[]> list = FBPMathHelper.rotateCubeXYZ((float) (AngleX * (Math.PI / 180)),
				(float) (AngleY * (Math.PI / 180)), (float) (AngleZ * (Math.PI / 180)), f4 / 20);
		int j = i >> 16 & 65535, k = i & 65535;

		if (FBP.cartoonMode) {
			/*
			 * renderCartonQuads( worldRendererIn,
			 * FBPMathHelper.rotateCubeXYZ((float) (AngleX * (Math.PI / 180)),
			 * (float) (AngleY * (Math.PI / 180)), (float) (AngleZ * (Math.PI /
			 * 180)), f4 / 20), i >> 16 & 65535, i & 65535, f1, f3, alpha);
			 */

			brightness = 1;
			vecIndex = 0;

			for (double[] d : list) {
				if (vecIndex == 4) {
					brightness *= 0.95;
					vecIndex = 0;
				}

				worldRendererIn.pos(d[0], d[1], d[2]).tex(f1, f3)
						.color(particleRed * brightness, particleGreen * brightness, particleBlue * brightness, alpha)
						.lightmap(j, k).endVertex();

				vecIndex++;
			}
		} else {
			/*
			 * renderQuads( worldRendererIn, FBPMathHelper.rotateCubeXYZ((float)
			 * (AngleX * (Math.PI / 180)), (float) (AngleY * (Math.PI / 180)),
			 * (float) (AngleZ * (Math.PI / 180)), f4 / 20), i >> 16 & 65535, i
			 * & 65535, alpha);
			 */

			brightness = 1;
			vecIndex = 0;

			for (double[] d : list) {
				if (vecIndex == 4) {
					brightness *= 0.95;
					vecIndex = 0;
				}

				worldRendererIn.pos(d[0], d[1], d[2]).tex(par[vecIndex][0], par[vecIndex][1])
						.color(particleRed * brightness, particleGreen * brightness, particleBlue * brightness, alpha)
						.lightmap(j, k).endVertex();

				vecIndex++;
			}
		}
		worldRendererIn.setTranslation(0, 0, 0);
	}

	public int getBrightnessForRender(float p_189214_1_) {
		int i = super.getBrightnessForRender(p_189214_1_);
		int j = 0;

		if (this.worldObj.isBlockLoaded(new BlockPos(posX, posY, posZ))) {
			j = this.worldObj.getCombinedLight(new BlockPos(posX, posY, posZ), 0);
		}

		return i == 0 ? j : i;
	}

	void renderCartonQuads(VertexBuffer buf, List<double[]> list, int j, int k, float f1, float f2, float alpha) {
		brightness = 1;
		vecIndex = 0;

		for (double[] d : list) {
			if (vecIndex == 4) {
				brightness *= 0.95;
				vecIndex = 0;
			}

			buf.pos(d[0], d[1], d[2]).tex(f1, f2)
					.color(particleRed * brightness, particleGreen * brightness, particleBlue * brightness, alpha)
					.lightmap(j, k).endVertex();

			vecIndex++;
		}
	}

	void renderQuads(VertexBuffer buf, List<double[]> list, int j, int k, float alpha) {
		brightness = 1;
		vecIndex = 0;

		for (double[] d : list) {
			if (vecIndex == 4) {
				brightness *= 0.95;
				vecIndex = 0;
			}

			buf.pos(d[0], d[1], d[2]).tex(par[vecIndex][0], par[vecIndex][1])
					.color(particleRed * brightness, particleGreen * brightness, particleBlue * brightness, alpha)
					.lightmap(j, k).endVertex();

			vecIndex++;
		}
	}

	@SideOnly(Side.CLIENT)
	public static class Factory implements IParticleFactory {
		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn,
				double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return (new FBPParticle(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn,
					Block.getStateById(p_178902_15_[0]), null, -1)).init();
		}
	}

	private void calculateYAngle() {
		double angleSin = Math.toDegrees(Math.asin(motionX / Math.sqrt(motionX * motionX + motionZ * motionZ)));

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

	double getMult() {
		if (FBP.legacyMode) {
			if (destroyed)
				return Math.sqrt(motionX * motionX + motionZ * motionZ) * 200;
			else
				return Math.sqrt(motionX * motionX + motionZ * motionZ) * 300;
		} else {
			if (FBP.rollParticles) {
				if (destroyed)
					return Math.sqrt(motionX * motionX + motionZ * motionZ) * 300;
				else
					return Math.sqrt(motionX * motionX + motionZ * motionZ) * 1150;
			} else {
				if (destroyed)
					return Math.sqrt(motionX * motionX + motionZ * motionZ) * 300;
				else
					return Math.sqrt(motionX * motionX + motionZ * motionZ) * 1000;
			}
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