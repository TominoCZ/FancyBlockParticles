package com.TominoCZ.FBP.renderer;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.particle.FBPParticleRain;
import com.TominoCZ.FBP.particle.FBPParticleSnow;
import com.TominoCZ.FBP.util.FBPPartialTicksUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class FBPWeatherRenderer extends IRenderHandler {

    private static final ResourceLocation RAIN_TEXTURES = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation SNOW_TEXTURES = new ResourceLocation("textures/environment/snow.png");

    private final Random random = new Random();

    private final float[] rainXCoords = new float[1024];
    private final float[] rainYCoords = new float[1024];

    private int rendererUpdateCount;

    int tickCounter;

    Minecraft mc;

    public FBPWeatherRenderer() {
        mc = Minecraft.getMinecraft();

        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = (float) (j - 16);
                float f1 = (float) (i - 16);
                float f2 = MathHelper.sqrt_float(f * f + f1 * f1);
                this.rainXCoords[i << 5 | j] = -f1 / f2;
                this.rainYCoords[i << 5 | j] = f / f2;
            }
        }
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        if (FBP.fancySnow && FBP.fancyRain)
            return;

        float f = this.mc.theWorld.getRainStrength(partialTicks);


        if (f > 0.0F) {
            mc.entityRenderer.enableLightmap();
            Entity entity = this.mc.getRenderViewEntity();
            int i = MathHelper.floor_double(entity.posX);
            int j = MathHelper.floor_double(entity.posY);
            int k = MathHelper.floor_double(entity.posZ);
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldrenderer = tessellator.getWorldRenderer();
            GlStateManager.disableCull();
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.alphaFunc(516, 0.1F);
            double d0 = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
            double d1 = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
            double d2 = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;
            int l = MathHelper.floor_double(d1);
            int i1 = 5;

            if (this.mc.gameSettings.fancyGraphics) {
                i1 = 10;
            }

            int j1 = -1;
            float f1 = (float) this.rendererUpdateCount + partialTicks;
            worldrenderer.setTranslation(-d0, -d1, -d2);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int k1 = k - i1; k1 <= k + i1; ++k1) {
                for (int l1 = i - i1; l1 <= i + i1; ++l1) {
                    int i2 = (k1 - k + 16) * 32 + l1 - i + 16;
                    double d3 = (double) this.rainXCoords[i2] * 0.5D;
                    double d4 = (double) this.rainYCoords[i2] * 0.5D;
                    blockpos$mutableblockpos.set(l1, 0, k1);
                    BiomeGenBase biomegenbase = world.getBiomeGenForCoords(blockpos$mutableblockpos);

                    if (biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow()) {
                        int j2 = world.getPrecipitationHeight(blockpos$mutableblockpos).getY();
                        int k2 = j - i1;
                        int l2 = j + i1;

                        if (k2 < j2) {
                            k2 = j2;
                        }

                        if (l2 < j2) {
                            l2 = j2;
                        }

                        int i3 = j2;

                        if (j2 < l) {
                            i3 = l;
                        }

                        if (k2 != l2) {
                            this.random.setSeed((long) (l1 * l1 * 3121 + l1 * 45238971 ^ k1 * k1 * 418711 + k1 * 13761));
                            blockpos$mutableblockpos.set(l1, k2, k1);
                            float f2 = biomegenbase.getFloatTemperature(blockpos$mutableblockpos);

                            if (world.getWorldChunkManager().getTemperatureAtHeight(f2, j2) >= 0.15F) {
                                if (!FBP.fancyRain) {
                                    if (j1 != 0) {
                                        if (j1 >= 0) {
                                            tessellator.draw();
                                        }

                                        j1 = 0;
                                        this.mc.getTextureManager().bindTexture(RAIN_TEXTURES);
                                        worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                    }

                                    double d5 = ((double) (this.rendererUpdateCount + l1 * l1 * 3121 + l1 * 45238971 + k1 * k1 * 418711 + k1 * 13761 & 31) + (double) partialTicks) / 32.0D * (3.0D + this.random.nextDouble());
                                    double d6 = (double) ((float) l1 + 0.5F) - entity.posX;
                                    double d7 = (double) ((float) k1 + 0.5F) - entity.posZ;
                                    float f3 = MathHelper.sqrt_double(d6 * d6 + d7 * d7) / (float) i1;
                                    float f4 = ((1.0F - f3 * f3) * 0.5F + 0.5F) * f;
                                    blockpos$mutableblockpos.set(l1, i3, k1);
                                    int j3 = world.getCombinedLight(blockpos$mutableblockpos, 0);
                                    int k3 = j3 >> 16 & 65535;
                                    int l3 = j3 & 65535;
                                    worldrenderer.pos((double) l1 - d3 + 0.5D, (double) k2, (double) k1 - d4 + 0.5D).tex(0.0D, (double) k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                                    worldrenderer.pos((double) l1 + d3 + 0.5D, (double) k2, (double) k1 + d4 + 0.5D).tex(1.0D, (double) k2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                                    worldrenderer.pos((double) l1 + d3 + 0.5D, (double) l2, (double) k1 + d4 + 0.5D).tex(1.0D, (double) l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                                    worldrenderer.pos((double) l1 - d3 + 0.5D, (double) l2, (double) k1 - d4 + 0.5D).tex(0.0D, (double) l2 * 0.25D + d5).color(1.0F, 1.0F, 1.0F, f4).lightmap(k3, l3).endVertex();
                                }
                            } else if (!FBP.fancySnow) {
                                if (j1 != 1) {
                                    if (j1 >= 0) {
                                        tessellator.draw();
                                    }

                                    j1 = 1;
                                    this.mc.getTextureManager().bindTexture(SNOW_TEXTURES);
                                    worldrenderer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                double d8 = (double) (((float) (this.rendererUpdateCount & 511) + partialTicks) / 512.0F);
                                double d9 = this.random.nextDouble() + (double) f1 * 0.01D * (double) ((float) this.random.nextGaussian());
                                double d10 = this.random.nextDouble() + (double) (f1 * (float) this.random.nextGaussian()) * 0.001D;
                                double d11 = (double) ((float) l1 + 0.5F) - entity.posX;
                                double d12 = (double) ((float) k1 + 0.5F) - entity.posZ;
                                float f6 = MathHelper.sqrt_double(d11 * d11 + d12 * d12) / (float) i1;
                                float f5 = ((1.0F - f6 * f6) * 0.3F + 0.5F) * f;
                                blockpos$mutableblockpos.set(l1, i3, k1);
                                int i4 = (world.getCombinedLight(blockpos$mutableblockpos, 0) * 3 + 15728880) / 4;
                                int j4 = i4 >> 16 & 65535;
                                int k4 = i4 & 65535;
                                worldrenderer.pos((double) l1 - d3 + 0.5D, (double) k2, (double) k1 - d4 + 0.5D).tex(0.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                                worldrenderer.pos((double) l1 + d3 + 0.5D, (double) k2, (double) k1 + d4 + 0.5D).tex(1.0D + d9, (double) k2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                                worldrenderer.pos((double) l1 + d3 + 0.5D, (double) l2, (double) k1 + d4 + 0.5D).tex(1.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                                worldrenderer.pos((double) l1 - d3 + 0.5D, (double) l2, (double) k1 - d4 + 0.5D).tex(0.0D + d9, (double) l2 * 0.25D + d8 + d10).color(1.0F, 1.0F, 1.0F, f5).lightmap(j4, k4).endVertex();
                            }
                        }
                    }
                }
            }

            if (j1 >= 0) {
                tessellator.draw();
            }

            worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
            mc.entityRenderer.disableLightmap();
        }
    }

    public void onUpdate() {
        if (FBP.fancySnow || FBP.fancyRain) {
            float f = this.mc.theWorld.getRainStrength(FBPPartialTicksUtil.partialTicks);

            if (f > 0.0F) {
                if (tickCounter++ >= 2) {
                    int r = 35;

                    double mX = mc.thePlayer.motionX * 26;
                    double mZ = mc.thePlayer.motionZ * 26;
                    double mT = MathHelper.sqrt_double(mX * mX + mZ * mZ) / 25;

                    BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

                    int j = 0;

                    for (int i = 0; i < 8 * FBP.weatherParticleDensity; i++) {
                        // get random position within radius of a little over the thePlayer's render
                        // distance
                        double angle = FBP.random.nextDouble() * Math.PI * 2;
                        double radius = MathHelper.sqrt_double(FBP.random.nextDouble()) * r;
                        double X = mc.thePlayer.posX + mX + radius * Math.cos(angle);
                        double Z = mc.thePlayer.posZ + mZ + radius * Math.sin(angle);

                        if (mc.thePlayer.getDistance(X, mc.thePlayer.posY, Z) > mc.gameSettings.renderDistanceChunks * 16)
                            continue;

                        // check if position is within a snow biome
                        blockpos$mutableblockpos.set((int) X, (int) mc.thePlayer.posY, (int) Z);
                        BiomeGenBase biome = mc.theWorld.getBiomeGenForCoords(blockpos$mutableblockpos);

                        int surfaceHeight = mc.theWorld.getPrecipitationHeight(blockpos$mutableblockpos).getY();

                        int Y = (int) (mc.thePlayer.posY + 15 + FBP.random.nextDouble() * 10 + (mc.thePlayer.motionY * 6));

                        if (Y <= surfaceHeight + 2)
                            Y = surfaceHeight + 10;

                        if (biome.canSpawnLightningBolt() || biome.getEnableSnow()) {
                            float temp = biome.getFloatTemperature(blockpos$mutableblockpos);
                            float finalTemp = mc.theWorld.getWorldChunkManager().getTemperatureAtHeight(temp, surfaceHeight);

                            if (finalTemp < 0.15F) {
                                if (FBP.fancySnow && i % 2 == 0) {
                                    mc.effectRenderer.addEffect(
                                            new FBPParticleSnow(mc.theWorld, X, Y, Z, FBP.random.nextDouble(-0.5, 0.5),
                                                    FBP.random.nextDouble(0.25, 1) + mT * 1.5f,
                                                    FBP.random.nextDouble(-0.5, 0.5), Blocks.snow.getDefaultState()));
                                }
                            } else if (FBP.fancyRain) {
                                mc.effectRenderer.addEffect(new FBPParticleRain(mc.theWorld, X, Y, Z, 0.1,
                                        FBP.random.nextDouble(0.75, 0.99) + mT / 2, 0.1,
                                        Blocks.snow.getDefaultState()));
                            }

                            j++;
                        }
                    }
                    tickCounter = 0;
                }

                tickCounter++;
            }
        }

        if (!FBP.fancySnow || !FBP.fancyRain)
            ++rendererUpdateCount;
        else
            rendererUpdateCount = 0;
    }
}
