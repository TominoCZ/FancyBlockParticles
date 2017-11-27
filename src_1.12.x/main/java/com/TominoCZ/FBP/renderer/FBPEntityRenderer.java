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

	long tickCounter;

	public FBPEntityRenderer(Minecraft mcIn, IResourceManager resourceManagerIn) {
		super(mcIn, resourceManagerIn);
		mc = mcIn;
	}

	@Override
	public void updateRenderer() {
		super.updateRenderer();

		if (FBP.fancyWeather && mc.world.isRaining()) {
			if (++tickCounter >= 12) {
				int r = 35;
				BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
				Biome biome;

				double mX;
				double mZ;
				double mT;

				double offsetY;

				double angle;
				double radius;

				mX = mc.player.motionX * 26;
				mZ = mc.player.motionZ * 26;

				mT = (MathHelper.sqrt(mX * mX + mZ * mZ) / 25);

				offsetY = mc.player.motionY * 6;

				for (int i = 0; i < 80 * FBP.weatherParticleDensity; i++) {
					// get random position within radius of a little over the player's render
					// distance
					angle = mc.world.rand.nextDouble() * Math.PI * 2;
					radius = MathHelper.sqrt(mc.world.rand.nextDouble()) * r;
					double X = mc.player.posX + mX + radius * Math.cos(angle);
					double Z = mc.player.posZ + mZ + radius * Math.sin(angle);

					if (mc.player.getDistance(X, mc.player.posY, Z) > mc.gameSettings.renderDistanceChunks * 16)
						continue;

					// check if position is within a snow biome
					blockpos$mutableblockpos.setPos(X, mc.player.posY, Z);
					biome = mc.world.getBiome(blockpos$mutableblockpos);

					int height = mc.world.getPrecipitationHeight(blockpos$mutableblockpos).getY();

					int Y = (int) (mc.player.posY + 15 + FBP.random.nextDouble() * 10 + offsetY);

					if (Y <= height + 2)
						Y = height + 10;

					// System.out.println(biome.getTemperature(blockpos$mutableblockpos));

					if (biome.getEnableSnow() || biome.isSnowyBiome()
							|| biome.getTemperature(blockpos$mutableblockpos) < 0.15) {
						mc.effectRenderer.addEffect(new FBPParticleSnow(mc.world, X, Y, Z,
								FBP.random.nextDouble(-0.5, 0.5), FBP.random.nextDouble(0.25, 1) + mT * 1.5f,
								FBP.random.nextDouble(-0.5, 0.5), Blocks.SNOW.getDefaultState()));
					} else if (biome.canRain() && !biome.getEnableSnow()) {
						mc.effectRenderer.addEffect(new FBPParticleRain(mc.world, X, Y, Z, 0.1,
								FBP.random.nextDouble(0.75, 0.999f) + mT / 2, 0.1, Blocks.SNOW.getDefaultState()));
					}
				}
				tickCounter = 0;
			}

			tickCounter++;
		}
	}

	@Override
	protected void renderRainSnow(float partialTicks) {
		if (!FBP.enabled || !FBP.fancyWeather)
			super.renderRainSnow(partialTicks);
	}
}
