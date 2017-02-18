package com.TominoCZ.FBP.particle;

import java.util.List;

import javax.annotation.Nullable;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPKeyInputHandler;
import com.TominoCZ.FBP.math.FBPMathHelper;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticle extends Particle {
	private final IBlockState sourceState;

	Minecraft mc;

	int vecIndex;

	double endScale, scaleAlpha, prevParticleScale, prevParticleAlpha, prevMotionX, prevMotionZ;

	double angleX, angleY, angleZ, prevAngleX, prevAngleY, prevAngleZ, randomXd, randomYd, randomZd;

	boolean modeDebounce = false, wasFrozen = false, destroyed = false;

	boolean spawned = false, dying = false, killToggle = false;
	double[][] par;

	double endMult = 1;

	float brightness = 1;

	protected FBPParticle(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, IBlockState state, @Nullable EnumFacing facing) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		this.sourceState = state;

		Block b = state.getBlock();

		mc = Minecraft.getMinecraft();

		particleGravity = (float) (b.blockParticleGravity * FBP.gravityMult);
		particleScale *= FBP.scaleMult;
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
				if (prevPosX == posX && Math.abs(prevMotionX) > 0.000001D)
					motionX = -prevMotionX;
				if (prevPosZ == posZ && Math.abs(prevMotionZ) > 0.000001D)
					motionZ = -prevMotionZ;

				if (!FBP.legacyMode && Math.abs(prevMotionX) > 0.000001D && Math.abs(prevMotionX) > 0.000001D)
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

		if (!mc.isGamePaused() && (!FBP.frozen || (FBP.frozen && killToggle))) {
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
				} else {
					if (randomXd == 0 && randomYd == 0 && randomZd == 0) {
						randomXd = FBP.random.nextDouble();
						randomYd = FBP.random.nextDouble();
						randomZd = FBP.random.nextDouble();
					}

					if (!isCollided || FBP.rollParticles) {
						double step = FBP.rotationMult * getMult();

						if (randomXd <= 0.333)
							angleX += step;
						else
							angleX -= step;

						if (randomYd <= 0.666)
							angleY += step;
						else
							angleY -= step;

						if (randomZd <= 1)
							angleZ += step;
						else
							angleZ -= step;
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

				moveEntity(motionX, motionY, motionZ); // <<-- THIS CAN SET
														// MOTION
														// TO ZERO
				if (motionX != 0)
					prevMotionX = motionX;
				if (motionZ != 0)
					prevMotionZ = motionZ;

				if (Math.abs(prevMotionX) > 0.000001D)
					motionX *= 0.9800000190734863D;
				motionY *= 0.9800000190734863D;
				if (Math.abs(prevMotionZ) > 0.000001D)
					motionZ *= 0.9800000190734863D;

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

		if (modeDebounce && FBP.legacyMode)
			modeDebounce = false;
	}

	public void moveEntity(double x, double y, double z) {
		double X = x, Y = y, Z = z, d0 = y;

		if (this.canCollide) {
			List<AxisAlignedBB> list = this.worldObj.getCollisionBoxes((Entity) null,
					this.getEntityBoundingBox().addCoord(x, y, z));

			for (AxisAlignedBB axisalignedbb : list) {
				y = axisalignedbb.calculateYOffset(this.getEntityBoundingBox(), y);
			}

			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));

			for (AxisAlignedBB axisalignedbb1 : list) {
				x = axisalignedbb1.calculateXOffset(this.getEntityBoundingBox(), x);
			}

			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));

			for (AxisAlignedBB axisalignedbb2 : list) {
				z = axisalignedbb2.calculateZOffset(this.getEntityBoundingBox(), z);
			}

			this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
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
		if (FBPKeyInputHandler.sweep)
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

		double AngleX = 0, AngleY = 0, AngleZ = 0;
		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		if ((dying && FBP.smoothTransitions && !FBP.frozen) || (FBP.frozen && killToggle && FBP.smoothTransitions)) {
			f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

			alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);
		}

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

		if (FBP.cartoonMode)
			renderCartonQuads(worldRendererIn, FBPMathHelper.rotateCubeXYZ(AngleX, AngleY, AngleZ, f4 / 20),
					i >> 16 & 65535, i & 65535, f1, f3, alpha);
		else
			renderQuads(worldRendererIn, FBPMathHelper.rotateCubeXYZ(AngleX, AngleY, AngleZ, f4 / 20), i >> 16 & 65535,
					i & 65535, alpha);

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

		double[] d;

		for (int i = 0; i < 24; i++) {
			if (i % 4 == 0)
				brightness *= 0.95;

			d = list.get(i);

			buf.pos(d[0], d[1], d[2]).tex(f1, f2)
					.color(particleRed * brightness, particleGreen * brightness, particleBlue * brightness, alpha)
					.lightmap(j, k).endVertex();
		}
	}

	void renderQuads(VertexBuffer buf, List<double[]> list, int j, int k, float alpha) {
		brightness = 1;
		vecIndex = 0;

		double[] d;

		for (int i = 0; i < 24; i++) {
			if (vecIndex == 4) {
				brightness *= 0.95;
				vecIndex = 0;
			}

			d = list.get(i);

			buf.pos(d[0], d[1], d[2]).tex(par[vecIndex][0], par[vecIndex][1])
					.color(particleRed * brightness, particleGreen * brightness, particleBlue * brightness, alpha)
					.lightmap(j, k).endVertex();

			vecIndex++;
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
}