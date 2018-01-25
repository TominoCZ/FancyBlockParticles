package com.TominoCZ.FBP.particle;

import java.util.List;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.TominoCZ.FBP.util.FBPMathUtil;
import com.TominoCZ.FBP.util.FBPRenderUtil;
import com.TominoCZ.FBP.vector.FBPVector3d;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class FBPParticleDigging extends EntityDiggingFX implements IFBPShadedParticle {
	private final Block sourceBlock;
	private final int blockSide;

	Minecraft mc;

	int vecIndex;

	double scaleAlpha, prevParticleScale, prevParticleAlpha, prevMotionX, prevMotionZ;

	boolean modeDebounce = false, wasFrozen = false, destroyed = false;

	boolean spawned = false, dying = false, killToggle = false;

	FBPVector3d rotStep;

	FBPVector3d rot;
	FBPVector3d prevRot;

	FBPVector3d[] par;

	double endMult = 0.75;

	long tick = 0;

	protected FBPParticleDigging(World w, double X, double Y, double Z, double mx, double my, double mz, float R, float G,
			float B, float scale, Block b, int meta, int side) {
		super(w, X, Y, Z, mx, my, mz, b, meta < 0 ? 0 : meta, side);

		this.particleRed = R;
		this.particleGreen = G;
		this.particleBlue = B;

		mc = Minecraft.getMinecraft();

		rot = new FBPVector3d();
		prevRot = new FBPVector3d();

		createRotationMatrix();

		if (scale == -1) {
			if (side == 1 && FBP.smartBreaking) {
				motionX *= 1.5D;
				motionY *= 0.1D;
				motionZ *= 1.5D;

				double particleSpeed = Math.sqrt(motionX * motionX + motionZ * motionZ);

				double x = FBPMathUtil.add(mc.thePlayer.getLookVec().xCoord, 0.01D);
				double z = FBPMathUtil.add(mc.thePlayer.getLookVec().zCoord, 0.01D);

				motionX = x * particleSpeed;
				motionZ = z * particleSpeed;
			}
		} else
			particleScale = scale;

		if (modeDebounce = !FBP.randomRotation) {
			this.rot.zero();
			calculateYAngle();
		}

		particleGravity = (float) (b.blockParticleGravity * FBP.gravityMult);

		particleScale *= FBP.scaleMult * 2.0F;
		particleMaxAge = (int) FBP.random.nextDouble(FBP.minAge, FBP.maxAge + 0.5);

		scaleAlpha = particleScale * 0.82;

		this.sourceBlock = b;

		if (destroyed = side == -1)
			side = w.rand.nextInt(6);

		this.blockSide = side;

		this.particleIcon = b.getIcon(side, meta);

		if (FBP.randomFadingSpeed)
			endMult = MathHelper.clamp_double(FBP.random.nextDouble(0.4151, 0.9875), 0.63875, 0.9875);

		if (particleIcon == null || particleIcon.getIconName().equals("missingno"))
			this.isDead = true;
	}

	protected FBPParticleDigging(World w, double x, double y, double z, double mx, double my, double mz, float scale,
			float r, float g, float b, Block bl, int meta) {
		this(w, x, y, z, mx, my, mz, r, g, b, scale, bl, meta, -1);
	}

	@Override
	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		if (!spawned)
			tick++;

		if (!FBP.frozen && FBP.bounceOffWalls && !mc.isGamePaused()) {
			if (!wasFrozen && spawned && (MathHelper.abs((float) motionX) > 0.00001D)) {
				boolean xCollided = (prevPosX == posX);
				boolean zCollided = (prevPosZ == posZ);

				if (xCollided)
					motionX = -prevMotionX;
				if (zCollided)
					motionZ = -prevMotionZ;

				if (!FBP.randomRotation && (xCollided || zCollided))
					calculateYAngle();
			} else
				wasFrozen = false;
		}
		if (FBP.frozen && FBP.bounceOffWalls && !wasFrozen)
			wasFrozen = true;

		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		prevRot.copyFrom(rot);

		prevParticleAlpha = particleAlpha;
		prevParticleScale = particleScale;

		if (!mc.isGamePaused() && (!FBP.frozen || killToggle)) {
			boolean allowedToMove = MathHelper.abs((float) motionX) > 0.00001D;

			if (!killToggle) {
				if (!FBP.randomRotation) {
					if (!modeDebounce) {
						modeDebounce = true;

						rot.z = 0;

						calculateYAngle();
					}

					if (allowedToMove) {
						double x = MathHelper.abs((float) (rotStep.x * getMult() * FBP.rotationMult));

						if (motionX > 0) {
							if (motionZ > 0)
								rot.x -= x;
							else if (motionZ < 0)
								rot.x += x;
						} else if (motionX < 0) {
							if (motionZ < 0)
								rot.x += x;
							else if (motionZ > 0) {
								rot.x -= x;
							}
						}
					}
				} else {
					if (modeDebounce) {
						modeDebounce = false;

						createRotationMatrix();
					}

					if (allowedToMove)
						rot.add(rotStep.multiply(getMult() * FBP.rotationMult));
				}
			}

			if (!FBP.infiniteDuration)
				particleAge++;

			if (this.particleAge >= this.particleMaxAge || killToggle) {
				if (!dying)
					dying = true;

				particleScale *= 0.887654321F * endMult;

				if (particleAlpha > 0.01 && particleScale <= scaleAlpha)
					particleAlpha *= 0.68752F * endMult;

				if (particleAlpha <= 0.01)
					setDead();
			}

			if (!killToggle) {
				if (isCollided)
					motionY = -0.08322508594922069D;
				else
					motionY -= 0.04D * particleGravity;

				if (allowedToMove)
					moveEntity(motionX, motionY, motionZ, false);
				else
					moveEntity(0, motionY, 0, true);

				if (MathHelper.abs((float) motionX) > 0.00001D) {
					prevMotionX = motionX;
					prevMotionZ = motionZ;
				}

				if (allowedToMove) {
					motionX *= 0.9800000190734863D;
					motionZ *= 0.9800000190734863D;
				}

				motionY *= 0.9800000190734863D;

				// PHYSICS
				if (FBP.entityCollision) {
					AxisAlignedBB box = null;

					if ((box = this.getBoundingBox()) != null) {
						List<Entity> list = worldObj.getEntitiesWithinAABB(Entity.class, box);

						for (Entity entityIn : list) {
							if (!entityIn.noClip) {
								double d0 = this.posX - entityIn.posX;
								double d1 = this.posZ - entityIn.posZ;
								double d2 = MathHelper.abs_max(d0, d1);

								if (d2 >= 0.009999999776482582D) {
									d2 = Math.sqrt(d2);
									d0 /= d2;
									d1 /= d2;

									double d3 = 1.0D / d2;

									if (d3 > 1.0D)
										d3 = 1.0D;

									this.motionX += d0 * d3 / 20;
									this.motionZ += d1 * d3 / 20;

									if (!FBP.randomRotation)
										calculateYAngle();
									if (!FBP.frozen)
										this.isCollided = false;
								}
							}
						}
					}
				}

				if (isCollided) {
					if (FBP.lowTraction) {
						motionX *= 0.932515086137662D;
						motionZ *= 0.932515086137662D;
					} else {
						motionX *= 0.6654999988079071D;
						motionZ *= 0.6654999988079071D;
					}
				}
			}
		}

		if (destroyed || !spawned && tick >= 2)
			spawned = true;
	}

	public void moveEntity(double x, double y, double z, boolean YOnly) {
		double d6 = x;
		double d7 = y;
		double d8 = z;
		double d0 = y;

		List<AxisAlignedBB> list = this.worldObj.getCollidingBoundingBoxes(this, this.boundingBox.addCoord(x, y, z));

		for (AxisAlignedBB aabb : list) {
			y = aabb.calculateYOffset(this.boundingBox, y);

			if (!YOnly) {
				x = aabb.calculateXOffset(this.boundingBox, x);
				z = aabb.calculateZOffset(this.boundingBox, z);
			}
		}

		this.boundingBox.setBB(boundingBox.offset(YOnly ? 0.0D : x, y, YOnly ? 0.0D : z));

		// RESET
		AxisAlignedBB axisalignedbb = this.boundingBox;
		this.posX = (axisalignedbb.minX + axisalignedbb.maxX) / 2.0D;
		this.posY = axisalignedbb.minY;
		this.posZ = (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D;

		this.isCollided = y != d7 && d0 < 0.0D;

		if (!FBP.lowTraction && !FBP.bounceOffWalls) {
			if (x != d6)
				motionX *= 0.699999988079071D;
			if (y != d7)
				motionY = 0;
			if (z != d8)
				motionZ *= 0.699999988079071D;
		}
	}

	@Override
	public void renderParticle(Tessellator tes, float partialTicks, float rotationX, float rotationZ, float rotationYZ,
			float rotationXY, float rotationXZ) {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getBrightnessForRender(float partialTicks) {
		int i = MathHelper.floor_double(this.posX);
		int j = MathHelper.floor_double(this.posZ);

		if (this.worldObj.blockExists(i, 0, j)) {
			double d0 = (this.boundingBox.maxY - this.boundingBox.minY) * 0.66D + 0.08D;
			int k = MathHelper.floor_double(this.posY - (double) this.yOffset + d0);
			return this.worldObj.getLightBrightnessForSkyBlocks(i, k, j, 0);
		} else {
			return 0;
		}
	}

	@Override
	public FBPParticleDigging applyColourMultiplier(int p_70596_1_, int p_70596_2_, int p_70596_3_) {
		if (this.sourceBlock == Blocks.grass && this.blockSide != 1) {
			return this;
		} else {
			int l = this.sourceBlock.colorMultiplier(this.worldObj, p_70596_1_, p_70596_2_, p_70596_3_);
			this.particleRed *= (float) (l >> 16 & 255) / 255.0F;
			this.particleGreen *= (float) (l >> 8 & 255) / 255.0F;
			this.particleBlue *= (float) (l & 255) / 255.0F;
			return this;
		}
	}

	@Override
	public FBPParticleDigging multiplyVelocity(float p_70543_1_) {
		this.motionX *= (double) p_70543_1_;
		this.motionY = (this.motionY - 0.10000000149011612D) * (double) p_70543_1_ + 0.10000000149011612D;
		this.motionZ *= (double) p_70543_1_;
		return this;
	}

	@Override
	public FBPParticleDigging multipleParticleScaleBy(float p_70541_1_) {
		this.setSize(0.2F * p_70541_1_, 0.2F * p_70541_1_);
		this.particleScale *= p_70541_1_;
		return this;
	}

	private void createRotationMatrix() {
		double rx0 = FBP.random.nextDouble();
		double ry0 = FBP.random.nextDouble();
		double rz0 = FBP.random.nextDouble();

		rotStep = new FBPVector3d(rx0 > 0.5 ? 1 : -1, ry0 > 0.5 ? 1 : -1, rz0 > 0.5 ? 1 : -1);

		rot.copyFrom(rotStep);
	}

	private void calculateYAngle() {
		double angleSin = Math.toDegrees(Math.asin(motionX / Math.sqrt(motionX * motionX + motionZ * motionZ)));

		if (motionX > 0) {
			if (motionZ > 0)
				rot.y = -angleSin;
			else
				rot.y = angleSin;
		} else {
			if (motionZ > 0)
				rot.y = -angleSin;
			else
				rot.y = angleSin;
		}
	}

	double getMult() {
		if (FBP.randomRotation) {
			if (destroyed)
				return Math.sqrt(motionX * motionX + motionZ * motionZ) * 200;
			else
				return Math.sqrt(motionX * motionX + motionZ * motionZ) * 300;
		} else {
			if (FBP.lowTraction) {
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

	@Override
	public void renderShadedParticle(Tessellator tes, float partialTicks) {
		if (!FBP.isEnabled() && particleMaxAge != 0)
			particleMaxAge = 0;
		if (FBPKeyBindings.FBPSweep.isPressed() && !killToggle)
			killToggle = true;

		float f = 0, f1 = 0, f2 = 0, f3 = 0;

		float f4 = particleScale;

		if (particleIcon != null) {
			if (!FBP.cartoonMode) {
				f = particleIcon.getInterpolatedU(particleTextureJitterX / 4 * 16);
				f2 = particleIcon.getInterpolatedV(particleTextureJitterY / 4 * 16);
			}

			f1 = particleIcon.getInterpolatedU((particleTextureJitterX + 1) / 4 * 16);
			f3 = particleIcon.getInterpolatedV((particleTextureJitterY + 1) / 4 * 16);
		} else {
			f = (particleTextureIndexX + particleTextureJitterX / 4) / 16;
			f1 = f + 0.015609375F;
			f2 = (particleTextureIndexY + particleTextureJitterY / 4) / 16;
			f3 = f2 + 0.015609375F;
		}

		float f5 = (float) (prevPosX + (posX - prevPosX) * partialTicks - interpPosX);
		float f6 = (float) (prevPosY + (posY - prevPosY) * partialTicks - interpPosY);
		float f7 = (float) (prevPosZ + (posZ - prevPosZ) * partialTicks - interpPosZ);

		int i = getBrightnessForRender(partialTicks);

		par = new FBPVector3d[] { new FBPVector3d(f1, f3, 0), new FBPVector3d(f1, f2, 0), new FBPVector3d(f, f2, 0),
				new FBPVector3d(f, f3, 0) };

		float alpha = particleAlpha;

		// SMOOTH TRANSITION
		if ((dying && FBP.smoothTransitions && !FBP.frozen) || (FBP.frozen && killToggle && FBP.smoothTransitions)) {
			f4 = (float) (prevParticleScale + (particleScale - prevParticleScale) * partialTicks);

			alpha = (float) (prevParticleAlpha + (particleAlpha - prevParticleAlpha) * partialTicks);
		}

		FBPVector3d smoothRot = new FBPVector3d();

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
		if (spawned)
			FBPRenderUtil.renderCubeShaded_S(tes, par, f5, f6, f7, f4 / 20, smoothRot, i, particleRed, particleGreen,
					particleBlue, alpha, FBP.cartoonMode);
	}
}