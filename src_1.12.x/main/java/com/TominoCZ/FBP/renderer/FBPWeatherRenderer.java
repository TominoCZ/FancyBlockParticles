package com.TominoCZ.FBP.renderer;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.particle.FBPParticleRain;
import com.TominoCZ.FBP.particle.FBPParticleSnow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.IRenderHandler;

public class FBPWeatherRenderer extends IRenderHandler {
	int tickCounter;

	Minecraft mc;

	public FBPWeatherRenderer() {
		mc = Minecraft.getMinecraft();
	}

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {

	}

	public void onUpdate() {
		if (FBP.fancyWeather && mc.world.isRaining()) {
			if (tickCounter++ >= 2) {
				int r = 35;

				double mX = mc.player.motionX * 26;
				double mZ = mc.player.motionZ * 26;
				double mT = MathHelper.sqrt(mX * mX + mZ * mZ) / 25;

				BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

				for (int i = 0; i < 8 * FBP.weatherParticleDensity; i++) {
					// get random position within radius of a little over the player's render
					// distance
					double angle = mc.world.rand.nextDouble() * Math.PI * 2;
					double radius = MathHelper.sqrt(mc.world.rand.nextDouble()) * r;
					double X = mc.player.posX + mX + radius * Math.cos(angle);
					double Z = mc.player.posZ + mZ + radius * Math.sin(angle);

					if (mc.player.getDistance(X, mc.player.posY, Z) > mc.gameSettings.renderDistanceChunks * 16)
						continue;

					// check if position is within a snow biome
					blockpos$mutableblockpos.setPos(X, mc.player.posY, Z);
					Biome biome = mc.world.getBiome(blockpos$mutableblockpos);

					int surfaceHeight = mc.world.getPrecipitationHeight(blockpos$mutableblockpos).getY();

					int Y = (int) (mc.player.posY + 15 + FBP.random.nextDouble() * 10 + (mc.player.motionY * 6));

					if (Y <= surfaceHeight + 2)
						Y = surfaceHeight + 10;

					if (biome.canRain() || biome.getEnableSnow()) {
						float temp = biome.getTemperature(blockpos$mutableblockpos);
						float finalTemp = mc.world.getBiomeProvider().getTemperatureAtHeight(temp, surfaceHeight);

						if (finalTemp >= 0.15F) {
							mc.effectRenderer.addEffect(new FBPParticleRain(mc.world, X, Y, Z, 0.1,
									FBP.random.nextDouble(0.75, 0.99) + mT / 2, 0.1, Blocks.SNOW.getDefaultState()));
						} else {
							mc.effectRenderer.addEffect(new FBPParticleSnow(mc.world, X, Y, Z,
									FBP.random.nextDouble(-0.5, 0.5), FBP.random.nextDouble(0.25, 1) + mT * 1.5f,
									FBP.random.nextDouble(-0.5, 0.5), Blocks.SNOW.getDefaultState()));
						}
					}
				}
				tickCounter = 0;
			}

			tickCounter++;
		}
	}
}
