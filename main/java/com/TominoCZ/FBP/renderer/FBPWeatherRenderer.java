package com.TominoCZ.FBP.renderer;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.particle.FBPParticleRain;
import com.TominoCZ.FBP.particle.FBPParticleSnow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.IRenderHandler;

public class FBPWeatherRenderer extends IRenderHandler {

	private static final ResourceLocation RAIN_TEXTURES = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation SNOW_TEXTURES = new ResourceLocation("textures/environment/snow.png");
	private static ResourceLocation LMAP_TEXTURES;

	private final Random random = new Random();

	private float[] rainXCoords = new float[1024];
	private float[] rainYCoords = new float[1024];

	private long rendererUpdateCount;
	private short weatherTickCount;

	Minecraft mc;

	public FBPWeatherRenderer() {
		mc = Minecraft.getMinecraft();

		for (int i = 0; i < 32; ++i) {
			for (int j = 0; j < 32; ++j) {
				float f = (float) (j - 16);
				float f1 = (float) (i - 16);
				float f2 = (float) Math.sqrt(f * f + f1 * f1);
				this.rainXCoords[i << 5 | j] = -f1 / f2;
				this.rainYCoords[i << 5 | j] = f / f2;
			}
		}

		LMAP_TEXTURES = mc.getTextureManager().getDynamicTextureLocation("lightMap", new DynamicTexture(16, 16));
	}

	@Override
	public void render(float partialTicks, WorldClient world, Minecraft mc) {
		if (FBP.fancySnow && FBP.fancyRain)
			return;

		float f1 = this.mc.theWorld.getRainStrength(partialTicks);

		if (f1 > 0.0F) {
			OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);

			if (this.rainXCoords == null) {
				this.rainXCoords = new float[1024];
				this.rainYCoords = new float[1024];

				for (int i = 0; i < 32; ++i) {
					for (int j = 0; j < 32; ++j) {
						float f2 = (float) (j - 16);
						float f3 = (float) (i - 16);
						float f4 = MathHelper.sqrt_float(f2 * f2 + f3 * f3);
						this.rainXCoords[i << 5 | j] = -f3 / f4;
						this.rainYCoords[i << 5 | j] = f2 / f4;
					}
				}
			}

			Entity entitylivingbase = this.mc.getRenderViewEntity();
			WorldClient worldclient = this.mc.theWorld;
			int k2 = MathHelper.floor_double(entitylivingbase.posX);
			int l2 = MathHelper.floor_double(entitylivingbase.posY);
			int i3 = MathHelper.floor_double(entitylivingbase.posZ);
			Tessellator tessellator = Tessellator.getInstance();
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			double d0 = entitylivingbase.lastTickPosX
					+ (entitylivingbase.posX - entitylivingbase.lastTickPosX) * (double) partialTicks;
			double d1 = entitylivingbase.lastTickPosY
					+ (entitylivingbase.posY - entitylivingbase.lastTickPosY) * (double) partialTicks;
			double d2 = entitylivingbase.lastTickPosZ
					+ (entitylivingbase.posZ - entitylivingbase.lastTickPosZ) * (double) partialTicks;
			int k = MathHelper.floor_double(d1);
			byte b0 = 5;

			if (this.mc.gameSettings.fancyGraphics) {
				b0 = 10;
			}

			boolean flag = false;
			byte b1 = -1;
			float f5 = (float) this.rendererUpdateCount + partialTicks;

			if (this.mc.gameSettings.fancyGraphics) {
				b0 = 10;
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			flag = false;

			for (int l = i3 - b0; l <= i3 + b0; ++l) {
				for (int i1 = k2 - b0; i1 <= k2 + b0; ++i1) {
					int j1 = (l - i3 + 16) * 32 + i1 - k2 + 16;
					float f6 = this.rainXCoords[j1] * 0.5F;
					float f7 = this.rainYCoords[j1] * 0.5F;
					BiomeGenBase biomegenbase = worldclient.getBiomeGenForCoords(i1, l);

					if (biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow()) {
						int k1 = worldclient.getPrecipitationHeight(i1, l);
						int l1 = l2 - b0;
						int i2 = l2 + b0;

						if (l1 < k1) {
							l1 = k1;
						}

						if (i2 < k1) {
							i2 = k1;
						}

						float f8 = 1.0F;
						int j2 = k1;

						if (k1 < k) {
							j2 = k;
						}

						if (l1 != i2) {
							this.random.setSeed((long) (i1 * i1 * 3121 + i1 * 45238971 ^ l * l * 418711 + l * 13761));
							float f9 = biomegenbase.getFloatTemperature(i1, l1, l);
							float f10;
							double d4;

							if (worldclient.getWorldChunkManager().getTemperatureAtHeight(f9, k1) >= 0.15F) {
								if (!FBP.fancyRain) {
									if (b1 != 0) {
										if (b1 >= 0) {
											tessellator.draw();
										}

										b1 = 0;
										this.mc.getTextureManager().bindTexture(RAIN_TEXTURES);
										tessellator.startDrawingQuads();
									}

									f10 = ((float) (this.rendererUpdateCount + i1 * i1 * 3121 + i1 * 45238971
											+ l * l * 418711 + l * 13761 & 31) + partialTicks) / 32.0F
											* (3.0F + this.random.nextFloat());
									double d3 = (double) ((float) i1 + 0.5F) - entitylivingbase.posX;
									d4 = (double) ((float) l + 0.5F) - entitylivingbase.posZ;
									float f12 = MathHelper.sqrt_double(d3 * d3 + d4 * d4) / (float) b0;
									float f13 = 1.0F;
									tessellator.setBrightness(worldclient.getLightBrightnessForSkyBlocks(i1, j2, l, 0));
									tessellator.setColorRGBA_F(f13, f13, f13, ((1.0F - f12 * f12) * 0.5F + 0.5F) * f1);
									tessellator.setTranslation(-d0 * 1.0D, -d1 * 1.0D, -d2 * 1.0D);
									tessellator.addVertexWithUV((double) ((float) i1 - f6) + 0.5D, (double) l1,
											(double) ((float) l - f7) + 0.5D, (double) (0.0F * f8),
											(double) ((float) l1 * f8 / 4.0F + f10 * f8));
									tessellator.addVertexWithUV((double) ((float) i1 + f6) + 0.5D, (double) l1,
											(double) ((float) l + f7) + 0.5D, (double) (1.0F * f8),
											(double) ((float) l1 * f8 / 4.0F + f10 * f8));
									tessellator.addVertexWithUV((double) ((float) i1 + f6) + 0.5D, (double) i2,
											(double) ((float) l + f7) + 0.5D, (double) (1.0F * f8),
											(double) ((float) i2 * f8 / 4.0F + f10 * f8));
									tessellator.addVertexWithUV((double) ((float) i1 - f6) + 0.5D, (double) i2,
											(double) ((float) l - f7) + 0.5D, (double) (0.0F * f8),
											(double) ((float) i2 * f8 / 4.0F + f10 * f8));
									tessellator.setTranslation(0.0D, 0.0D, 0.0D);
								}
							} else if (!FBP.fancySnow) {
								if (b1 != 1) {
									if (b1 >= 0) {
										tessellator.draw();
									}

									b1 = 1;
									this.mc.getTextureManager().bindTexture(SNOW_TEXTURES);
									tessellator.startDrawingQuads();
								}

								f10 = ((float) (this.rendererUpdateCount & 511) + partialTicks) / 512.0F;
								float f16 = this.random.nextFloat() + f5 * 0.01F * (float) this.random.nextGaussian();
								float f11 = this.random.nextFloat() + f5 * (float) this.random.nextGaussian() * 0.001F;
								d4 = (double) ((float) i1 + 0.5F) - entitylivingbase.posX;
								double d5 = (double) ((float) l + 0.5F) - entitylivingbase.posZ;
								float f14 = MathHelper.sqrt_double(d4 * d4 + d5 * d5) / (float) b0;
								float f15 = 1.0F;
								tessellator.setBrightness(
										(worldclient.getLightBrightnessForSkyBlocks(i1, j2, l, 0) * 3 + 15728880) / 4);
								tessellator.setColorRGBA_F(f15, f15, f15, ((1.0F - f14 * f14) * 0.3F + 0.5F) * f1);
								tessellator.setTranslation(-d0 * 1.0D, -d1 * 1.0D, -d2 * 1.0D);
								tessellator.addVertexWithUV((double) ((float) i1 - f6) + 0.5D, (double) l1,
										(double) ((float) l - f7) + 0.5D, (double) (0.0F * f8 + f16),
										(double) ((float) l1 * f8 / 4.0F + f10 * f8 + f11));
								tessellator.addVertexWithUV((double) ((float) i1 + f6) + 0.5D, (double) l1,
										(double) ((float) l + f7) + 0.5D, (double) (1.0F * f8 + f16),
										(double) ((float) l1 * f8 / 4.0F + f10 * f8 + f11));
								tessellator.addVertexWithUV((double) ((float) i1 + f6) + 0.5D, (double) i2,
										(double) ((float) l + f7) + 0.5D, (double) (1.0F * f8 + f16),
										(double) ((float) i2 * f8 / 4.0F + f10 * f8 + f11));
								tessellator.addVertexWithUV((double) ((float) i1 - f6) + 0.5D, (double) i2,
										(double) ((float) l - f7) + 0.5D, (double) (0.0F * f8 + f16),
										(double) ((float) i2 * f8 / 4.0F + f10 * f8 + f11));
								tessellator.setTranslation(0.0D, 0.0D, 0.0D);
							}
						}
					}
				}
			}

			if (b1 >= 0) {
				tessellator.draw();
			}

			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

			OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
		}
	}

	public void onUpdate() {
		if (FBP.fancySnow || FBP.fancyRain) {
			float f = this.mc.theWorld.getRainStrength(0);

			if (f > 0.0F) {
				if (weatherTickCount++ >= 2) {
					int r = 35;

					double mX = mc.thePlayer.motionX * 26;
					double mZ = mc.thePlayer.motionZ * 26;
					double mT = MathHelper.sqrt_double(mX * mX + mZ * mZ) / 25;

					int j = 0;

					for (int i = 0; i < 8 * FBP.weatherParticleDensity; i++) {
						// get random position within radius of a little over the player's render
						// distance
						double angle = FBP.random.nextDouble() * Math.PI * 2;
						double radius = MathHelper.sqrt_double(FBP.random.nextDouble()) * r;
						double X = mc.thePlayer.posX + mX + radius * Math.cos(angle);
						double Z = mc.thePlayer.posZ + mZ + radius * Math.sin(angle);

						if (mc.thePlayer.getDistance(X, mc.thePlayer.posY, Z) > mc.gameSettings.renderDistanceChunks
								* 16)
							continue;

						// check if position is within a snow biome
						BiomeGenBase biome = mc.theWorld.getBiomeGenForCoords((int) X, (int) Z);

						int surfaceHeight = mc.theWorld.getPrecipitationHeight((int) X, (int) Z);

						int Y = (int) (mc.thePlayer.posY + 15 + FBP.random.nextDouble() * 10
								+ (mc.thePlayer.motionY * 6));

						if (Y <= surfaceHeight + 2)
							Y = surfaceHeight + 10;

						if (biome.canSpawnLightningBolt() || biome.getEnableSnow()) {
							float temp = biome.getFloatTemperature((int) X, (int) mc.thePlayer.posY, (int) Z);
							float finalTemp = mc.theWorld.getWorldChunkManager().getTemperatureAtHeight(temp,
									surfaceHeight);

							if (finalTemp < 0.15F) {
								if (FBP.fancySnow && i % 2 == 0) {
									mc.effectRenderer.addEffect(
											new FBPParticleSnow(mc.theWorld, X, Y, Z, FBP.random.nextDouble(-0.5, 0.5),
													FBP.random.nextDouble(0.25, 1) + mT * 1.5f,
													FBP.random.nextDouble(-0.5, 0.5)));
								}
							} else if (FBP.fancyRain) {
								mc.effectRenderer.addEffect(new FBPParticleRain(mc.theWorld, X, Y, Z, 0.1,
										FBP.random.nextDouble(0.75, 0.99) + mT / 2, 0.1));
							}

							j++;
						}
					}
					weatherTickCount = 0;
				}
				weatherTickCount++;
			}
		}

		if (!FBP.fancySnow || !FBP.fancyRain)
			++rendererUpdateCount;
		else
			rendererUpdateCount = 0;
	}
}
