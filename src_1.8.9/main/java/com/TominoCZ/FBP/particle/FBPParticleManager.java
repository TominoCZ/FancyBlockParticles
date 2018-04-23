package com.TominoCZ.FBP.particle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;
import com.google.common.base.Throwables;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFlameFX;
import net.minecraft.client.particle.EntityRainFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPParticleManager extends EffectRenderer {
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

	private List<EntityFX>[][] fxLayers;

	Minecraft mc;

	public FBPParticleManager(World worldIn, TextureManager rendererIn, IParticleFactory particleFactory) {
		super(worldIn, rendererIn);

		mc = Minecraft.getMinecraft();

		this.particleFactory = particleFactory;

		white = mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture((Blocks.snow.getDefaultState()));

		MethodHandles.Lookup lookup = MethodHandles.publicLookup();

		try {
			getParticleTypes = lookup.unreflectGetter(
					ReflectionHelper.findField(EffectRenderer.class, "field_178932_g", "particleTypes"));

			X = lookup.unreflectGetter(ReflectionHelper.findField(Entity.class, "field_70165_t", "posX"));
			Y = lookup.unreflectGetter(ReflectionHelper.findField(Entity.class, "field_70163_u", "posY"));
			Z = lookup.unreflectGetter(ReflectionHelper.findField(Entity.class, "field_70161_v", "posZ"));

			mX = lookup.unreflectGetter(ReflectionHelper.findField(Entity.class, "field_70159_w", "motionX"));
			mY = lookup.unreflectGetter(ReflectionHelper.findField(Entity.class, "field_70181_x", "motionY"));
			mZ = lookup.unreflectGetter(ReflectionHelper.findField(Entity.class, "field_70179_y", "motionZ"));

			getParticleScale = lookup
					.unreflectGetter(ReflectionHelper.findField(EntityFX.class, "field_70544_f", "particleScale"));
			getParticleTexture = lookup
					.unreflectGetter(ReflectionHelper.findField(EntityFX.class, "field_70550_a", "particleIcon"));
			getParticleMaxAge = lookup
					.unreflectGetter(ReflectionHelper.findField(EntityFX.class, "field_70547_e", "particleMaxAge"));

			getSourceState = lookup
					.unreflectGetter(ReflectionHelper.findField(EntityDiggingFX.class, "field_174847_a"));
			getBlockDamage = lookup
					.unreflectGetter(ReflectionHelper.findField(RenderGlobal.class, "field_72738_E", "damagedBlocks"));

			MethodHandle getFxLayers = lookup
					.unreflectGetter(ReflectionHelper.findField(EffectRenderer.class, "field_78876_b", "fxLayers"));

			fxLayers = (List<EntityFX>[][]) getFxLayers.invokeExact((EffectRenderer) this);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void addEffect(EntityFX efx) {
		Entity effect = efx;
		Entity toAdd = effect;

		if (FBP.enabled && toAdd != null && !(toAdd instanceof FBPParticleSnow)
				&& !(toAdd instanceof FBPParticleRain)) {
			if (FBP.fancyFlame && toAdd instanceof EntityFlameFX && !(toAdd instanceof FBPParticleFlame)
					&& Minecraft.getMinecraft().gameSettings.particleSetting < 2) {
				try {
					toAdd = new FBPParticleFlame(worldObj, (double) X.invokeExact(effect),
							(double) Y.invokeExact(effect), (double) Z.invokeExact(effect), 0,
							FBP.random.nextDouble() * 0.25, 0, true);
					effect.setDead();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else if (FBP.fancySmoke && toAdd instanceof EntitySmokeFX && !(toAdd instanceof FBPParticleSmokeNormal)
					&& Minecraft.getMinecraft().gameSettings.particleSetting < 2) {
				EntitySmokeFX p = (EntitySmokeFX) effect;

				try {
					toAdd = new FBPParticleSmokeNormal(worldObj, (double) X.invokeExact(effect),
							(double) Y.invokeExact(effect), (double) Z.invokeExact(effect),
							(double) mX.invokeExact(effect), (double) mY.invokeExact(effect),
							(double) mZ.invokeExact(effect), (float) getParticleScale.invokeExact((EntityFX) effect),
							true, white, p);

					((EntityFX) toAdd).setRBGColorF(
							MathHelper.clamp_float(((EntityFX) effect).getRedColorF() + 0.1f, 0.1f, 1),
							MathHelper.clamp_float(((EntityFX) effect).getGreenColorF() + 0.1f, 0.1f, 1),
							MathHelper.clamp_float(((EntityFX) effect).getBlueColorF() + 0.1f, 0.1f, 1));

					((FBPParticleSmokeNormal) toAdd).setMaxAge((int) getParticleMaxAge.invokeExact((EntityFX) effect));
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else if (FBP.fancyRain && toAdd instanceof EntityRainFX) {
				effect.setDead();
				return;
			} else if (toAdd instanceof EntityDiggingFX && !(toAdd instanceof FBPParticleDigging)) {
				try {
					blockState = (IBlockState) getSourceState.invokeExact((EntityDiggingFX) effect);

					if (blockState != null && !(FBP.frozen && !FBP.spawnWhileFrozen)
							&& (FBP.spawnRedstoneBlockParticles || blockState.getBlock() != Blocks.redstone_block)) {
						effect.setDead();

						if (!(blockState.getBlock() instanceof BlockLiquid)
								&& !FBP.INSTANCE.isInExceptions(blockState.getBlock())) {
							toAdd = new FBPParticleDigging(worldObj, (double) X.invokeExact(effect),
									(double) Y.invokeExact(effect) - 0.10000000149011612D,
									(double) Z.invokeExact(effect), 0, 0, 0, ((EntityFX) toAdd).getRedColorF(),
									((EntityFX) toAdd).getGreenColorF(), ((EntityFX) toAdd).getBlueColorF(), blockState,
									null, (float) getParticleScale.invokeExact((EntityFX) effect),
									(TextureAtlasSprite) getParticleTexture.invokeExact((EntityFX) effect));
						} else
							return;
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			} else if (toAdd instanceof FBPParticleDigging) {
				try {
					blockState = (IBlockState) getSourceState.invokeExact((EntityDiggingFX) effect);

					if (blockState != null && !(FBP.frozen && !FBP.spawnWhileFrozen)
							&& (FBP.spawnRedstoneBlockParticles || blockState.getBlock() != Blocks.redstone_block)) {

						if (blockState.getBlock() instanceof BlockLiquid
								|| FBP.INSTANCE.isInExceptions(blockState.getBlock())) {
							effect.setDead();
							return;
						}
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

		super.addEffect((EntityFX) toAdd);
	}

	@Override
	public void renderParticles(Entity e, float f) {
		if (e != null)
			super.renderParticles(e, f);

		renderShadedParticles(f);
	}

	private void renderShadedParticles(float partialTicks) {
		Tessellator tes = Tessellator.getInstance();
		WorldRenderer buff = tes.getWorldRenderer();

		mc.getRenderManager().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		buff.begin(GL11.GL_QUADS, FBP.POSITION_TEX_COLOR_LMAP_NORMAL);

		mc.entityRenderer.enableLightmap();

		GlStateManager.enableCull();
		GlStateManager.enableBlend();
		RenderHelper.enableStandardItemLighting();

		for (int list = 0; list < 2; list++)
			renderParticleArray(partialTicks, buff, fxLayers[1][list]);

		tes.draw();
	}

	private void renderParticleArray(float partialTicks, WorldRenderer buff, List<EntityFX> particles) {
		for (int i = 0; i < particles.size(); i++) {
			EntityFX p = particles.get(i);

			if (p instanceof IFBPShadedParticle)
				((IFBPShadedParticle) p).renderShadedParticle(buff, partialTicks);
		}
	}

	@Nullable
	@Override
	public EntityFX spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed,
			double ySpeed, double zSpeed, int... parameters) {
		IParticleFactory iparticlefactory = null;

		try {
			iparticlefactory = ((Map<Integer, IParticleFactory>) getParticleTypes.invokeExact((EffectRenderer) this))
					.get(Integer.valueOf(particleId));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (iparticlefactory != null) {
			EntityFX particle = iparticlefactory.getEntityFX(particleId, mc.theWorld, xCoord, yCoord, zCoord, xSpeed,
					ySpeed, zSpeed, parameters), toSpawn = particle;

			if (FBP.enabled) {
				if (particle instanceof EntityDiggingFX && !(particle instanceof FBPParticleDigging)) {
					blockState = Block.getStateById(parameters[0]);

					if (blockState != null && !(FBP.frozen && !FBP.spawnWhileFrozen)
							&& (FBP.spawnRedstoneBlockParticles || blockState.getBlock() != Blocks.redstone_block)) {
						if (!(blockState.getBlock() instanceof BlockLiquid)
								&& !FBP.INSTANCE.isInExceptions(blockState.getBlock())) {
							toSpawn = new FBPParticleDigging(mc.theWorld, xCoord, yCoord, zCoord, xSpeed, ySpeed,
									zSpeed, 1, 1, 1, blockState, EnumFacing.UP, -1, null).multipleParticleScaleBy(0.6F);
						} else
							toSpawn = null;
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

		if (!b.isAir(worldObj, pos) && !b.addDestroyEffects(worldObj, pos, this)) {
			state = state.getBlock().getActualState(state, worldObj, pos);
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
										&& (FBP.spawnRedstoneBlockParticles || b != Blocks.redstone_block)
										&& !FBP.INSTANCE.isInExceptions(b)) {
									FBPParticleDigging toSpawn = new FBPParticleDigging(worldObj, d0, d1, d2,
											d0 - pos.getX() - 0.5D, -0.001, d2 - pos.getZ() - 0.5D, 1, 1, 1, state,
											null, -1, texture);

									addEffect(toSpawn);
								}
							} else
								addEffect((particleFactory.getEntityFX(0, this.worldObj, d0, d1, d2,
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
		IBlockState iblockstate = worldObj.getBlockState(pos);

		if (iblockstate.getBlock().getRenderType() != -1) {
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			float f = 0.1F;

			AxisAlignedBB axisalignedbb = iblockstate.getBlock().getSelectedBoundingBox(worldObj, pos);

			double d0 = 0, d1 = 0, d2 = 0;

			MovingObjectPosition obj = Minecraft.getMinecraft().objectMouseOver;

			if (obj == null || obj.hitVec == null)
				obj = new MovingObjectPosition(null, new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

			if (FBP.enabled && FBP.smartBreaking && iblockstate != null
					&& (!(iblockstate.getBlock() instanceof BlockLiquid) && !(FBP.frozen && !FBP.spawnWhileFrozen))
					&& (FBP.spawnRedstoneBlockParticles || iblockstate.getBlock() != Blocks.redstone_block)) {
				d0 = obj.hitVec.xCoord
						+ FBP.random.nextDouble(-0.21, 0.21) * Math.abs(axisalignedbb.maxX - axisalignedbb.minX);
				d1 = obj.hitVec.yCoord
						+ FBP.random.nextDouble(-0.21, 0.21) * Math.abs(axisalignedbb.maxY - axisalignedbb.minY);
				d2 = obj.hitVec.zCoord
						+ FBP.random.nextDouble(-0.21, 0.21) * Math.abs(axisalignedbb.maxZ - axisalignedbb.minZ);
			} else {
				d0 = i + worldObj.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D)
						+ 0.10000000149011612D + axisalignedbb.minX;
				d1 = j + worldObj.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D)
						+ 0.10000000149011612D + axisalignedbb.minY;
				d2 = k + worldObj.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D)
						+ 0.10000000149011612D + axisalignedbb.minZ;
			}

			switch (side) {
			case DOWN:
				d1 = j + iblockstate.getBlock().getBlockBoundsMinY() - f;
				break;
			case EAST:
				d0 = i + iblockstate.getBlock().getBlockBoundsMaxX() + f;
				break;
			case NORTH:
				d2 = k + iblockstate.getBlock().getBlockBoundsMinZ() - f;
				break;
			case SOUTH:
				d2 = k + iblockstate.getBlock().getBlockBoundsMaxZ() + f;
				break;
			case UP:
				d1 = j + iblockstate.getBlock().getBlockBoundsMaxY() + f;
				break;
			case WEST:
				d0 = i + iblockstate.getBlock().getBlockBoundsMinX() - f;
				break;
			default:
				break;
			}

			try {
				if (FBP.enabled) {
					if (iblockstate != null
							&& (!(iblockstate.getBlock() instanceof BlockLiquid)
									&& !(FBP.frozen && !FBP.spawnWhileFrozen))
							&& (FBP.spawnRedstoneBlockParticles || iblockstate.getBlock() != Blocks.redstone_block)) {

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

						EntityFX toSpawn;

						if (!FBP.INSTANCE.isInExceptions(iblockstate.getBlock())) {
							toSpawn = new FBPParticleDigging(worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, 1, 1, 1,
									iblockstate, side, -2, null);

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
							.getEntityFX(0, worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getStateId(iblockstate))
							.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}