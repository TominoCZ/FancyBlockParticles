package com.TominoCZ.FBP.renderer;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.particle.FBPParticleRain;
import com.TominoCZ.FBP.particle.FBPParticleSnow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

public class FBPEntityRenderer extends EntityRenderer {

	private Minecraft mc;

	long snowTick;
	long rainTick;

	public FBPEntityRenderer(Minecraft mcIn, IResourceManager resourceManagerIn) {
		super(mcIn, resourceManagerIn);
		mc = mcIn;
	}

	@Override
	public void updateRenderer() {
		super.updateRenderer();

		if (FBP.fancyWeather && mc.world.isRaining()) {
			BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
			Biome biome;

			double mX;
			double mZ;
			double mT;
			
			double offsetY;

			double angle;
			double radius;
			double X;
			double Z;

			int r;

			if (++snowTick >= 13) {
				r = 25;
				
				mX = mc.player.motionX * 26;
				mZ = mc.player.motionZ * 26;

				mT = (Math.sqrt(mX * mX + mZ * mZ) / 25);

				offsetY = mc.player.motionY * 6;

				for (int i = 0; i < 25 * (2 - mc.gameSettings.particleSetting); i++) {
					// get random position within radius of a little over the player's render
					// distance
					angle = mc.world.rand.nextDouble() * Math.PI * 2;
					radius = Math.sqrt(mc.world.rand.nextDouble()) * r;
					X = mc.player.posX + mX + radius * Math.cos(angle);
					Z = mc.player.posZ + mZ + radius * Math.sin(angle);

					// check if position is within a snow biome
					blockpos$mutableblockpos.setPos(X, 0, Z);
					biome = mc.world.getBiome(blockpos$mutableblockpos);

					if (biome.getEnableSnow()) {
						mc.effectRenderer
								.addEffect(new FBPParticleSnow(mc.world, X, mc.player.posY + 17 + offsetY, Z,
										FBP.random.nextDouble(-0.5, 0.5), FBP.random.nextDouble(0.25, 0.8) + mT * 1.5f,
										FBP.random.nextDouble(-0.5, 0.5), Blocks.SNOW.getDefaultState()));
					}
				}

				snowTick = (int) (mT * 2 + offsetY / 2);
			}

			if (++rainTick >= 8) {
				r = 40;
				
				mX = mc.player.motionX * 26;
				mZ = mc.player.motionZ * 26;

				mT = (Math.sqrt(mX * mX + mZ * mZ) / 25);

				offsetY = mc.player.motionY * 6;

				for (int i = 0; i < 60 * (2 - mc.gameSettings.particleSetting); i++) {
					// get random position within radius of a little over the player's render
					// distance
					angle = mc.world.rand.nextDouble() * Math.PI * 2;
					radius = Math.sqrt(mc.world.rand.nextDouble()) * r;
					X = mc.player.posX - 5 + mX + radius * Math.cos(angle);
					Z = mc.player.posZ - 5 + mZ + radius * Math.sin(angle);

					// check if position is NOT within a snow biome
					blockpos$mutableblockpos.setPos(X, 0, Z);
					biome = mc.world.getBiome(blockpos$mutableblockpos);

					if (biome.canRain() && !biome.getEnableSnow()) {
						mc.effectRenderer.addEffect(new FBPParticleRain(mc.world, X, mc.player.posY + 15, Z, 0.25,
								FBP.random.nextDouble(0.75, 2.25f) + mT / 2, 0.25, Blocks.SNOW.getDefaultState()));
					}
				}

				rainTick = (int) (mT * 2 + offsetY / 2);
			}
		}
	}

	@Override
	protected void renderRainSnow(float partialTicks) {
		if (!FBP.enabled || !FBP.fancyWeather)
			super.renderRainSnow(partialTicks);
	}
}
