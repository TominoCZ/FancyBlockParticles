package com.TominoCZ.FBP.particle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;
import com.google.common.base.Throwables;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRain;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticleManager extends ParticleManager {
	private static MethodHandle getBlockDamage;
	private static MethodHandle getParticleScale;
	private static MethodHandle getParticleTexture;
	private static MethodHandle getParticleTypes;
	private static MethodHandle getSourceState;
	private static MethodHandle getParticleMaxAge;

	private static MethodHandle X, Y, Z;
	private static MethodHandle mX, mY, mZ;

	private static IParticleFactory particleFactory;
	private static IBlockState blockState;
	private static TextureAtlasSprite white;

	private ArrayDeque<Particle>[][] fxLayers;

	Minecraft mc;

	public FBPParticleManager(World worldIn, TextureManager rendererIn, IParticleFactory particleFactory) {
		super(worldIn, rendererIn);

		this.particleFactory = particleFactory;

		mc = Minecraft.getMinecraft();

		white = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture((Blocks.SNOW.getDefaultState()));

		MethodHandles.Lookup lookup = MethodHandles.publicLookup();

		try {
			getParticleTypes = lookup.unreflectGetter(
					ReflectionHelper.findField(ParticleManager.class, "field_178932_g", "particleTypes"));

			X = lookup.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187126_f", "posX"));
			Y = lookup.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187127_g", "posY"));
			Z = lookup.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187128_h", "posZ"));

			mX = lookup.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187129_i", "motionX"));
			mY = lookup.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187130_j", "motionY"));
			mZ = lookup.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187131_k", "motionZ"));

			getParticleScale = lookup
					.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_70544_f", "particleScale"));
			getParticleTexture = lookup
					.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187119_C", "particleTexture"));
			getParticleMaxAge = lookup
					.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_70547_e", "particleMaxAge"));

			getSourceState = lookup.unreflectGetter(
					ReflectionHelper.findField(ParticleDigging.class, "field_174847_a", "sourceState"));
			getBlockDamage = lookup
					.unreflectGetter(ReflectionHelper.findField(RenderGlobal.class, "field_72738_E", "damagedBlocks"));

			MethodHandle getFxLayers = lookup
					.unreflectGetter(ReflectionHelper.findField(ParticleManager.class, "field_78876_b", "fxLayers"));

			fxLayers = (ArrayDeque<Particle>[][]) getFxLayers.invokeExact((ParticleManager) this);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void addEffect(Particle effect) {
		Particle toAdd = effect;

		if (FBP.enabled && toAdd != null && !(toAdd instanceof FBPParticleSnow)
				&& !(toAdd instanceof FBPParticleRain)) {
			if (FBP.fancyFlame && toAdd instanceof ParticleFlame && !(toAdd instanceof FBPParticleFlame)
					&& Minecraft.getMinecraft().gameSettings.particleSetting < 2) {
				ParticleFlame p = (ParticleFlame) effect;

				try {
					toAdd = new FBPParticleFlame(world, (double) X.invokeExact(effect), (double) Y.invokeExact(effect),
							(double) Z.invokeExact(effect), 0, FBP.random.nextDouble() * 0.25, 0, true);
					effect.setExpired();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else if (FBP.fancySmoke && toAdd instanceof ParticleSmokeNormal
					&& !(toAdd instanceof FBPParticleSmokeNormal)
					&& Minecraft.getMinecraft().gameSettings.particleSetting < 2) {
				ParticleSmokeNormal p = (ParticleSmokeNormal) effect;

				try {
					toAdd = new FBPParticleSmokeNormal(world, (double) X.invokeExact(effect),
							(double) Y.invokeExact(effect), (double) Z.invokeExact(effect),
							(double) mX.invokeExact(effect), (double) mY.invokeExact(effect),
							(double) mZ.invokeExact(effect), (float) getParticleScale.invokeExact(effect), true, white,
							p);

					toAdd.setRBGColorF(MathHelper.clamp(effect.getRedColorF() + 0.1f, 0.1f, 1),
							MathHelper.clamp(effect.getGreenColorF() + 0.1f, 0.1f, 1),
							MathHelper.clamp(effect.getBlueColorF() + 0.1f, 0.1f, 1));

					toAdd.setMaxAge((int) getParticleMaxAge.invokeExact(effect));
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else if (FBP.fancyRain && toAdd instanceof ParticleRain) {
				effect.setExpired();
				return;
			} else if (toAdd instanceof ParticleDigging && !(toAdd instanceof FBPParticleDigging)) {
				try {
					blockState = (IBlockState) getSourceState.invokeExact((ParticleDigging) effect);

					if (blockState != null && !(FBP.frozen && !FBP.spawnWhileFrozen)
							&& (FBP.spawnRedstoneBlockParticles || blockState.getBlock() != Blocks.REDSTONE_BLOCK)) {
						effect.setExpired();

						if (!(blockState.getBlock() instanceof BlockLiquid)
								&& !FBP.INSTANCE.isInExceptions(blockState.getBlock(), true)) {
							toAdd = new FBPParticleDigging(world, (double) X.invokeExact(effect),
									(double) Y.invokeExact(effect) - 0.10000000149011612D,
									(double) Z.invokeExact(effect), 0, 0, 0, toAdd.getRedColorF(),
									toAdd.getGreenColorF(), toAdd.getBlueColorF(), blockState, null,
									(float) getParticleScale.invokeExact(effect),
									(TextureAtlasSprite) getParticleTexture.invokeExact(effect));
						} else
							return;
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else if (toAdd instanceof FBPParticleDigging) {
				try {
					blockState = (IBlockState) getSourceState.invokeExact((ParticleDigging) effect);

					if (blockState != null && !(FBP.frozen && !FBP.spawnWhileFrozen)
							&& (FBP.spawnRedstoneBlockParticles || blockState.getBlock() != Blocks.REDSTONE_BLOCK)) {

						if (blockState.getBlock() instanceof BlockLiquid
								|| FBP.INSTANCE.isInExceptions(blockState.getBlock(), true)) {
							effect.setExpired();
							return;
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

		super.addEffect(toAdd);
	}

	@Override
	public void renderParticles(Entity e, float f) {
		if (e != null)
			super.renderParticles(e, f);

		renderShadedParticles(f);
	}

	private void renderShadedParticles(float partialTicks) {
		if (fxLayers.length < 2 || fxLayers[1].length < 2 || fxLayers[1][1].size() == 0)
			return;

		Tessellator tes = Tessellator.getInstance();
		VertexBuffer buff = tes.getBuffer();

		Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		buff.begin(GL11.GL_QUADS, FBP.POSITION_TEX_COLOR_LMAP_NORMAL);

		mc.entityRenderer.enableLightmap();

		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		RenderHelper.enableStandardItemLighting();

		Object[] particles = (Object[]) fxLayers[1][1].toArray();

		for (int i = 0; i < particles.length; i++) {
			Particle p = (Particle) particles[i];

			if (p instanceof IFBPShadedParticle)
				((IFBPShadedParticle) p).renderShadedParticle(buff, partialTicks);
		}

		tes.draw();

		GlStateManager.disableBlend();
		RenderHelper.disableStandardItemLighting();
	}

	@Nullable
	@Override
	public Particle spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed,
			double ySpeed, double zSpeed, int... parameters) {
		IParticleFactory iparticlefactory = null;

		try {
			iparticlefactory = ((Map<Integer, IParticleFactory>) getParticleTypes.invokeExact((ParticleManager) this))
					.get(Integer.valueOf(particleId));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (iparticlefactory != null) {
			Particle particle = iparticlefactory.createParticle(particleId, this.world, xCoord, yCoord, zCoord, xSpeed,
					ySpeed, zSpeed, parameters), toSpawn = particle;

			if (FBP.enabled) {
				if (particle instanceof ParticleDigging && !(particle instanceof FBPParticleDigging)) {
					blockState = Block.getStateById(parameters[0]);

					if (blockState != null && !(FBP.frozen && !FBP.spawnWhileFrozen)
							&& (FBP.spawnRedstoneBlockParticles || blockState.getBlock() != Blocks.REDSTONE_BLOCK)) {
						if (!(blockState.getBlock() instanceof BlockLiquid)
								&& !FBP.INSTANCE.isInExceptions(blockState.getBlock(), true)) {
							toSpawn = new FBPParticleDigging(this.world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed,
									1, 1, 1, blockState, EnumFacing.UP, -1, null).multipleParticleScaleBy(0.6F);
						} else
							toSpawn = particle;
					}
				}
			}

			this.addEffect(toSpawn);
			return toSpawn;
		}
		return null;
	}

	@Override
	public void addBlockDestroyEffects(BlockPos pos, IBlockState state) {
		Block b = state.getBlock();

		if (!b.isAir(state, world, pos) && !b.addDestroyEffects(world, pos, this) && b != FBP.FBPBlock) {
			state = state.getActualState(world, pos);
			b = state.getBlock();
			int i = 4;

			TextureAtlasSprite texture = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(state);

			for (int j = 0; j < FBP.particlesPerAxis; ++j) {
				for (int k = 0; k < FBP.particlesPerAxis; ++k) {
					for (int l = 0; l < FBP.particlesPerAxis; ++l) {
						double d0 = pos.getX() + ((j + 0.5D) / FBP.particlesPerAxis);
						double d1 = pos.getY() + ((k + 0.5D) / FBP.particlesPerAxis);
						double d2 = pos.getZ() + ((l + 0.5D) / FBP.particlesPerAxis);

						try {
							if (FBP.enabled) {
								if (state != null
										&& (!(b instanceof BlockLiquid) && !(FBP.frozen && !FBP.spawnWhileFrozen))
										&& (FBP.spawnRedstoneBlockParticles || b != Blocks.REDSTONE_BLOCK)
										&& !FBP.INSTANCE.isInExceptions(b, true)) {
									float scale = (float) FBP.random.nextDouble(0.75, 1);

									FBPParticleDigging toSpawn = new FBPParticleDigging(world, d0, d1, d2,
											d0 - pos.getX() - 0.5D, -0.001, d2 - pos.getZ() - 0.5D, 1, 1, 1, state,
											null, scale, texture);

									addEffect(toSpawn);
								}
							} else
								addEffect((particleFactory.createParticle(0, this.world, d0, d1, d2,
										d0 - pos.getX() - 0.5D, d1 - pos.getY() - 0.5D, d2 - pos.getZ() - 0.5D,
										Block.getStateId(state))));
						} catch (Throwable e) {

						}
					}
				}
			}
		}
	}

	@Override
	public void addBlockHitEffects(BlockPos pos, EnumFacing side) {
		IBlockState iblockstate = world.getBlockState(pos);

		if (iblockstate.getBlock() == FBP.FBPBlock)
			return;

		if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			float f = 0.1F;
			AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(world, pos);

			double d0 = 0, d1 = 0, d2 = 0;

			RayTraceResult obj = Minecraft.getMinecraft().objectMouseOver;

			if (obj == null || obj.hitVec == null)
				obj = new RayTraceResult(null, new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

			if (FBP.enabled && FBP.smartBreaking && iblockstate != null
					&& (!(iblockstate.getBlock() instanceof BlockLiquid) && !(FBP.frozen && !FBP.spawnWhileFrozen))
					&& (FBP.spawnRedstoneBlockParticles || iblockstate.getBlock() != Blocks.REDSTONE_BLOCK)) {
				d0 = obj.hitVec.xCoord
						+ FBP.random.nextDouble(-0.21, 0.21) * Math.abs(axisalignedbb.maxX - axisalignedbb.minX);
				d1 = obj.hitVec.yCoord
						+ FBP.random.nextDouble(-0.21, 0.21) * Math.abs(axisalignedbb.maxY - axisalignedbb.minY);
				d2 = obj.hitVec.zCoord
						+ FBP.random.nextDouble(-0.21, 0.21) * Math.abs(axisalignedbb.maxZ - axisalignedbb.minZ);
			} else {
				d0 = i + world.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D)
						+ 0.10000000149011612D + axisalignedbb.minX;
				d1 = j + world.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D)
						+ 0.10000000149011612D + axisalignedbb.minY;
				d2 = k + world.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D)
						+ 0.10000000149011612D + axisalignedbb.minZ;
			}

			switch (side) {
			case DOWN:
				d1 = j + axisalignedbb.minY - 0.10000000149011612D;
				break;
			case EAST:
				d0 = i + axisalignedbb.maxX + 0.10000000149011612D;
				break;
			case NORTH:
				d2 = k + axisalignedbb.minZ - 0.10000000149011612D;
				break;
			case SOUTH:
				d2 = k + axisalignedbb.maxZ + 0.10000000149011612D;
				break;
			case UP:
				d1 = j + axisalignedbb.maxY + 0.08000000119D;
				break;
			case WEST:
				d0 = i + axisalignedbb.minX - 0.10000000149011612D;
				break;
			default:
				break;
			}

			try {
				if (FBP.enabled) {
					if (iblockstate != null
							&& (!(iblockstate.getBlock() instanceof BlockLiquid)
									&& !(FBP.frozen && !FBP.spawnWhileFrozen))
							&& (FBP.spawnRedstoneBlockParticles || iblockstate.getBlock() != Blocks.REDSTONE_BLOCK)) {

						int damage = 0;

						try {
							DestroyBlockProgress progress = null;
							Map mp = (Map<Integer, DestroyBlockProgress>) getBlockDamage
									.invokeExact(Minecraft.getMinecraft().renderGlobal);

							if (!mp.isEmpty()) {
								Iterator it = mp.values().iterator();

								while (it.hasNext()) {
									progress = (DestroyBlockProgress) it.next();

									if (progress.getPosition().equals(pos)) {
										damage = progress.getPartialBlockDamage();
										break;
									}
								}
							}
						} catch (Throwable e) {

						}

						Particle toSpawn;

						if (!FBP.INSTANCE.isInExceptions(iblockstate.getBlock(), true)) {
							toSpawn = new FBPParticleDigging(world, d0, d1, d2, 0.0D, 0.0D, 0.0D, 1, 1, 1, iblockstate,
									side, -2, null);

							if (FBP.smartBreaking) {
								toSpawn = ((FBPParticleDigging) toSpawn)
										.MultiplyVelocity(side == EnumFacing.UP ? 0.7F : 0.15F);
								toSpawn = toSpawn.multipleParticleScaleBy(0.325F + (damage / 10f) * 0.5F);
							} else {
								toSpawn = ((FBPParticleDigging) toSpawn).MultiplyVelocity(0.2F);
								toSpawn = toSpawn.multipleParticleScaleBy(0.6F);
							}

							addEffect(toSpawn);
						}
					}
				} else
					addEffect(particleFactory
							.createParticle(0, world, d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getStateId(iblockstate))
							.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}