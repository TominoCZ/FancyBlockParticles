package com.TominoCZ.FBP.particle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nullable;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.math.FBPMathHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class FBPParticleDigging extends Particle {
	private final IBlockState sourceState;
	
	private int j2, k2, vecIndex;
	
	long thisTime, lastTime;

	double aplhaMult = 0.85;

	double scale;

	double angleX, angleY, angleZ;

	double stepXZ = 1;

	double randomXd, randomYd, randomZd;

	private boolean spawned = false, small = false;
	private double[][] par;

	public FBPParticleDigging(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn,
			double ySpeedIn, double zSpeedIn, IBlockState state) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn / 2, ySpeedIn / 2, zSpeedIn / 2);
		this.sourceState = state;
		this.particleGravity = (float) (state.getBlock().blockParticleGravity * FBP.gravityMult);
		this.particleScale = (float) ThreadLocalRandom.current().nextDouble(FBP.minScale, FBP.maxScale + 0.5);
		this.particleMaxAge = (int) ThreadLocalRandom.current().nextDouble(FBP.minAge, FBP.maxAge + 0.5);

		scale = this.particleScale;

		this.particleTexture = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
				.getTexture(state);

		double blockHeight = state.getBlock().getBoundingBox(state, worldIn, new BlockPos(posX, posY, posZ)).maxY;

		// GET THE TOP TEXTURE OF THE BLOCK
		if (posY - ((int) posY) <= 0.105 && posY - ((int) posY) >= 0) {
			List<BakedQuad> quads = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
					.getModelForState(state).getQuads(state, EnumFacing.UP, rand.nextLong());
			if (!(quads = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
					.getModelForState(state).getQuads(state, EnumFacing.UP, rand.nextLong())).isEmpty())
				this.particleTexture = quads.get(0).getSprite();
		}

		if (!FBP.oldMode) {
			// IF PARTICLE IS 1/10 OF A BLOCK ABOVE THE SOURCE BLOCK
			double x1 = FBPMathHelper.add((int) posX, 0.5), x2 = (posX >= 0 ? posX : -posX);
			double z1 = FBPMathHelper.add((int) posZ, 0.5), z2 = (posZ >= 0 ? posZ : -posZ);

			x1 = (x1 >= 0 ? x1 : -x1);
			z1 = (z1 >= 0 ? z1 : -z1);

			double mX = motionX >= 0.0D ? motionX : -motionX, mY = motionY >= 0.0D ? motionY : -motionY,
					mZ = motionZ >= 0.0D ? motionZ : -motionZ;

			if (blockHeight != 1) {
				if (state.getBlock() != Blocks.CARPET) {
					if (posY - ((int) posY) - 0.1F == blockHeight) {
						if (x2 > x1)
							if (posX > 0)
								motionX = mX;
							else
								motionX = -mX;
						else if (x2 < x1)
							if (posX > 0)
								motionX = -mX;
							else
								motionX = mX;
						if (z2 > z1)
							if (posZ > 0)
								motionZ = mZ;
							else
								motionZ = -mZ;
						else if (z2 < z1)
							if (posZ > 0)
								motionZ = -mZ;
							else
								motionZ = mZ;
					}
				} else {
					if (posY - ((int) posY) - 0.1F == blockHeight) {
						if (x2 > x1)
							if (posX > 0)
								motionX = mX;
							else
								motionX = -mX;
						else if (x2 < x1)
							if (posX > 0)
								motionX = -mX;
							else
								motionX = mX;
						if (z2 > z1)
							if (posZ > 0)
								motionZ = mZ;
							else
								motionZ = -mZ;
						else if (z2 < z1)
							if (posZ > 0)
								motionZ = -mZ;
							else
								motionZ = mZ;
					}
				}
			} else {
				if ((posY - ((int) posY) <= 0.105 && posY - ((int) posY) - 0.1F == 0) || posY - ((int) posY) > 1) {
					if (x2 > x1)
						if (posX > 0)
							motionX = mX;
						else
							motionX = -mX;
					else if (x2 < x1)
						if (posX > 0)
							motionX = -mX;
						else
							motionX = mX;
					if (z2 > z1)
						if (posZ > 0)
							motionZ = mZ;
						else
							motionZ = -mZ;
					else if (z2 < z1)
						if (posZ > 0)
							motionZ = -mZ;
						else
							motionZ = mZ;
				}
			}

			// ROTATE ABOUT DIRECTION AXIS
			double angleSin = Math.toDegrees(Math.asin(motionX / Math.sqrt(motionX * motionX + motionZ * motionZ)));

			motionX += 0.001;
			motionZ += 0.001;

			if (motionX > 0) {
				if (motionZ > 0) {
					angleY = -angleSin;
					stepXZ = (90 - angleY) / 12;
				} else if (motionZ < 0) {
					angleY = angleSin;
					stepXZ = (90 - angleY) / 4;
				}
			} else {
				if (motionZ > 0) {
					angleY = -angleSin;
					stepXZ = (90 - angleY) / 4;
				} else if (motionZ < 0) {
					angleY = angleSin;
					stepXZ = (90 - angleY) / 12;
				}
			}
		}

		if (!state.isNormalCube() || this.particleTexture != Minecraft.getMinecraft().getBlockRendererDispatcher()
				.getBlockModelShapes().getTexture(state))
			multiplyColor(new BlockPos(xCoordIn, yCoordIn, zCoordIn));

		randomXd = Math.random();
		randomYd = Math.random();
		randomZd = Math.random();
	}

	protected void multiplyColor(@Nullable BlockPos p_187154_1_) {
		int i = Minecraft.getMinecraft().getBlockColors().colorMultiplier(this.sourceState, this.worldObj, p_187154_1_,
				0);
		this.particleRed *= (float) (i >> 16 & 255) / 255.0F;
		this.particleGreen *= (float) (i >> 8 & 255) / 255.0F;
		this.particleBlue *= (float) (i & 255) / 255.0F;
	}

	public int getFXLayer() {
		return 1;
	}

	@Override
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (!Minecraft.getMinecraft().isGamePaused() && !FBP.frozen) {
			if (this.particleAge++ >= this.particleMaxAge) {
				if (particleScale > scale * 0.5) {
					particleScale -= 0.07 * scale;

					if (particleScale < scale * 0.725F) {
						particleAlpha *= aplhaMult;
						aplhaMult *= 0.885;
					}
				} else
					setExpired();
			}

			this.motionY -= 0.04D * (double) this.particleGravity;
			this.moveEntity(this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.9800000190734863D;
			this.motionY *= 0.9800000190734863D;
			this.motionZ *= 0.9800000190734863D;

			if (this.isCollided) {
				this.motionX *= 0.699999988079071D;
				this.motionZ *= 0.699999988079071D;
			}
		}
	}

	/**
	 * Renders the particle
	 */
	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		if (!FBP.isEnabled())
			this.setMaxAge(1);

		float f = ((float) this.particleTextureIndexX + this.particleTextureJitterX / 4.0F) / 16.0F;
		float f1 = f + 0.015609375F;
		float f2 = ((float) this.particleTextureIndexY + this.particleTextureJitterY / 4.0F) / 16.0F;
		float f3 = f2 + 0.015609375F;
		float f4 = 0.1F * this.particleScale;

		if (this.particleTexture != null) {
			f = this.particleTexture.getInterpolatedU((double) (this.particleTextureJitterX / 4.0F * 16.0F));
			f1 = this.particleTexture.getInterpolatedU((double) ((this.particleTextureJitterX + 1.0F) / 4.0F * 16.0F));
			f2 = this.particleTexture.getInterpolatedV((double) (this.particleTextureJitterY / 4.0F * 16.0F));
			f3 = this.particleTexture.getInterpolatedV((double) ((this.particleTextureJitterY + 1.0F) / 4.0F * 16.0F));
		}

		float f5 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
		float f6 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY) + 0.0125F;
		float f7 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
		
		int i = this.getBrightnessForRender(partialTicks);

		if (!spawned) {
			spawned = true;
			
			par = new double[][] { { f1, f3 }, { f1, f2 }, { f, f2 }, { f, f3 },

					{ f, f2 }, { f, f3 }, { f1, f3 }, { f1, f2 },

					{ f1, f2 }, { f, f2 }, { f, f3 }, { f1, f3 },

					{ f, f3 }, { f1, f3 }, { f1, f2 }, { f, f2 },

					{ f1, f3 }, { f1, f2 }, { f, f2 }, { f, f3 },

					{ f, f2 }, { f, f3 }, { f1, f3 }, { f1, f2 } };
		}
		
		int j = i >> 16 & 65535;
		int k = i & 65535;
		// ROTATION CALCULATION PER 20ms
		thisTime = System.currentTimeMillis();

		if (!Minecraft.getMinecraft().isGamePaused() && FBP.rotationMult > 0 && !FBP.frozen) {
			if ((thisTime - lastTime) >= 20 && !this.isCollided) {
				lastTime = thisTime;

				if (!FBP.oldMode) {
					if (motionX > 0) {
						if (motionZ > 0)
							angleX -= (stepXZ * (FBP.rotationMult * 0.75)); // 1.
						else if (motionZ < 0)
							angleX += (stepXZ * (FBP.rotationMult * 0.75)); // 2.
					} else {
						if (motionZ < 0)
							angleX += (stepXZ * (FBP.rotationMult * 0.75)); // 3.
						else if (motionZ > 0)
							angleX -= (stepXZ * (FBP.rotationMult * 0.75)); // 4.
					}
				} else {
					if (randomXd < 0.7)
						angleX += 2.25 * FBP.rotationMult * 2;
					else
						angleX -= 2.25 * FBP.rotationMult * 2;

					if (randomYd < 0.15)
						angleY += 0.72 * FBP.rotationMult * 2;
					else
						angleY -= 0.72 * FBP.rotationMult * 2;

					if (randomZd < 0.6)
						angleZ += 2.22 * FBP.rotationMult * 2;
					else
						angleZ -= 2.22 * FBP.rotationMult * 2;
				}
			}
		}

		// RENDER
		worldRendererIn.setTranslation(f5, f6, f7);

		renderQuads(worldRendererIn, FBPMathHelper.rotateCubeXYZ(angleX, angleY, angleZ, f4), par, j, k);

		worldRendererIn.setTranslation(0, 0, 0);
	}

	public int getBrightnessForRender(float p_189214_1_) {
		int i = super.getBrightnessForRender(p_189214_1_);
		int j = 0;

		if (this.worldObj.isBlockLoaded(new BlockPos(posX, posY, posZ))) {
			j = (int) this.worldObj.getCombinedLight(new BlockPos(posX, posY, posZ), 0);
		}

		return i == 0 ? j : i;
	}
	
	void renderQuads(VertexBuffer buf, List<double[]> vec, double[][] pars, int j, int k) {
		j2 = (int) ((j / 1.1) * 1.0045);
		k2 = (int) ((k / 1.1) * 1.0045);
		
		vec.forEach(vector -> {
			j2 /= 1.0045;
			k2 /= 1.0045;
			
			vecIndex = vec.indexOf(vector);
			
			buf.pos(vector[0], vector[1], vector[2]).tex(pars[vecIndex][0], pars[vecIndex][1])
					.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j2, k2)
					.endVertex();
		});
		
		vecIndex = 0;
		/*
		for (int index = 0; index < vec.size(); index++) {
			j2 /= 1.0045;
			k2 /= 1.0045;

			buf.pos(vec[index][0], vec[index][1], vec[index][2]).tex(pars[index][0], pars[index][1])
					.color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(j2, k2)
					.endVertex();
		}
		*/
	}
}