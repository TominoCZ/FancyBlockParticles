package com.TominoCZ.FBP.particle;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nullable;

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
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
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

	private static MethodHandle X, Y, Z;

	private static IParticleFactory particleFactory;
	private static IBlockState blockState;
	
	public FBPParticleManager(World worldIn, TextureManager rendererIn, IParticleFactory particleFactory) {
		super(worldIn, rendererIn);
		this.particleFactory = particleFactory;

		MethodHandles.Lookup lookup = MethodHandles.publicLookup();

		try {
			getParticleTypes = lookup.unreflectGetter(
					ReflectionHelper.findField(ParticleManager.class, "field_178932_g", "particleTypes"));

			X = lookup.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187126_f", "posX"));
			Y = lookup.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187127_g", "posY"));
			Z = lookup.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187128_h", "posZ"));

			getParticleScale = lookup
					.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_70544_f", "particleScale"));
			getParticleTexture = lookup
					.unreflectGetter(ReflectionHelper.findField(Particle.class, "field_187119_C", "particleTexture"));

			getSourceState = lookup.unreflectGetter(
					ReflectionHelper.findField(ParticleDigging.class, "field_174847_a", "sourceState"));
			getBlockDamage = lookup
					.unreflectGetter(ReflectionHelper.findField(RenderGlobal.class, "field_72738_E", "damagedBlocks"));
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void addEffect(Particle effect) {
		Particle toAdd = effect;
		
		if (FBP.enabled && toAdd != null && !(toAdd instanceof FBPParticleSnow)) {
			if (FBP.fancyFlame && toAdd instanceof ParticleFlame && !(toAdd instanceof FBPParticleFlame) && Minecraft.getMinecraft().gameSettings.particleSetting < 2) {
				ParticleFlame p = (ParticleFlame) effect;

				try {
					toAdd = new FBPParticleFlame(worldObj,
							(double) X.invokeExact(effect),
							(double) Y.invokeExact(effect),
							(double) Z.invokeExact(effect),
							0,
							FBP.random.nextDouble() * 0.25,
							0, true);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else if (FBP.fancySmoke && toAdd instanceof ParticleSmokeNormal && !(toAdd instanceof FBPParticleSmokeNormal) && Minecraft.getMinecraft().gameSettings.particleSetting < 2) {
				ParticleSmokeNormal p = (ParticleSmokeNormal) effect;

				try {
					toAdd = new FBPParticleSmokeNormal(worldObj,
							(double) X.invokeExact(effect),
							(double) Y.invokeExact(effect),
							(double) Z.invokeExact(effect),
							FBP.random.nextDouble(-0.05, 0.05),
							FBP.random.nextDouble() * 0.25,
							FBP.random.nextDouble(-0.05, 0.05), true);
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else if (FBP.fancyWeather && toAdd instanceof ParticleRain && !(toAdd instanceof FBPParticleRain)) {
				ParticleRain p = (ParticleRain) effect;

				try {
					toAdd = new FBPParticleRain(worldObj, (double) X.invokeExact(effect),
							(double) Y.invokeExact(effect), (double) Z.invokeExact(effect),
							FBP.random.nextDouble(-0.1, 0.1), FBP.random.nextDouble() * 0.25,
							FBP.random.nextDouble(-0.1, 0.1), Blocks.WATER.getDefaultState());
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else if (toAdd instanceof ParticleDigging && !(toAdd instanceof FBPParticle)) {
				try {
					blockState = (IBlockState) getSourceState.invokeExact((ParticleDigging) effect);

					if (blockState != null && !(FBP.frozen && !FBP.spawnWhileFrozen)
							&& (FBP.spawnRedstoneBlockParticles || blockState.getBlock() != Blocks.REDSTONE_BLOCK)) {
						if (!(blockState.getBlock() instanceof BlockLiquid)) {
							toAdd = new FBPParticle(worldObj, (double) X.invokeExact(effect),
									(double) Y.invokeExact(effect) - 0.10000000149011612D,
									(double) Z.invokeExact(effect), 0, 0, 0, blockState, null,
									(float) getParticleScale.invokeExact(effect));

							toAdd.setParticleTexture((TextureAtlasSprite) getParticleTexture.invokeExact(effect));
						} else
							toAdd = null;
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}

		super.addEffect(toAdd);
	}

	@Nullable
	@Override
	public Particle spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed,
			double ySpeed, double zSpeed, int... parameters) {
		IParticleFactory iparticlefactory = null;

		try {
			iparticlefactory = (IParticleFactory) ((Map<Integer, IParticleFactory>) getParticleTypes
					.invokeExact((ParticleManager) this)).get(Integer.valueOf(particleId));
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (iparticlefactory != null) {
			Particle particle = iparticlefactory.createParticle(particleId, this.worldObj, xCoord, yCoord, zCoord,
					xSpeed, ySpeed, zSpeed, parameters), toSpawn = particle;

			if (FBP.enabled) {
				if (particle instanceof ParticleDigging && !(particle instanceof FBPParticle)) {
					blockState = Block.getStateById(parameters[0]);

					if (blockState != null && !(FBP.frozen && !FBP.spawnWhileFrozen)
							&& (FBP.spawnRedstoneBlockParticles || blockState.getBlock() != Blocks.REDSTONE_BLOCK)) {
						if (!(blockState.getBlock() instanceof BlockLiquid))
							toSpawn = new FBPParticle(this.worldObj, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed,
									blockState, EnumFacing.UP, -1).multipleParticleScaleBy(0.6F);
						else
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

		if (!b.isAir(state, worldObj, pos) && !b.addDestroyEffects(worldObj, pos, this) && b != FBP.FBPBlock) {
			state = state.getActualState(worldObj, pos);
			b = state.getBlock();
			int i = 4;

			for (int j = 0; j < 4; ++j) {
				for (int k = 0; k < 4; ++k) {
					for (int l = 0; l < 4; ++l) {
						double d0 = (double) pos.getX() + ((double) j + 0.5D) / 4.0D;
						double d1 = (double) pos.getY() + ((double) k + 0.5D) / 4.0D;
						double d2 = (double) pos.getZ() + ((double) l + 0.5D) / 4.0D;

						try {
							if (FBP.enabled) {
								if (state != null
										&& (!(b instanceof BlockLiquid) && !(FBP.frozen && !FBP.spawnWhileFrozen))
										&& (FBP.spawnRedstoneBlockParticles || b != Blocks.REDSTONE_BLOCK))
									addEffect(new FBPParticle(worldObj, d0, d1, d2, d0 - (double) pos.getX() - 0.5D,
											d1 - (double) pos.getY() - 0.5D, d2 - (double) pos.getZ() - 0.5D, state,
											null, -1));
							} else
								addEffect((particleFactory.createParticle(0, this.worldObj, d0, d1, d2,
										d0 - (double) pos.getX() - 0.5D, d1 - (double) pos.getY() - 0.5D,
										d2 - (double) pos.getZ() - 0.5D, Block.getStateId(state))));
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

		if (iblockstate.getBlock() == FBP.FBPBlock)
			return;

		if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
			int i = pos.getX();
			int j = pos.getY();
			int k = pos.getZ();
			float f = 0.1F;
			AxisAlignedBB axisalignedbb = iblockstate.getBoundingBox(worldObj, pos);

			double d0 = 0, d1 = 0, d2 = 0;

			if (FBP.enabled && FBP.smartBreaking && iblockstate != null
					&& (!(iblockstate.getBlock() instanceof BlockLiquid) && !(FBP.frozen && !FBP.spawnWhileFrozen))
					&& (FBP.spawnRedstoneBlockParticles || iblockstate.getBlock() != Blocks.REDSTONE_BLOCK)) {
				d0 = Minecraft.getMinecraft().objectMouseOver.hitVec.xCoord
						+ FBP.random.nextDouble(-0.225, 0.2251) * (axisalignedbb.maxX - axisalignedbb.minX);
				d1 = Minecraft.getMinecraft().objectMouseOver.hitVec.yCoord
						+ FBP.random.nextDouble(-0.225, 0.2251) * (axisalignedbb.maxY - axisalignedbb.minY);
				d2 = Minecraft.getMinecraft().objectMouseOver.hitVec.zCoord
						+ FBP.random.nextDouble(-0.225, 0.2251) * (axisalignedbb.maxZ - axisalignedbb.minZ);
			} else {
				d0 = (double) i
						+ worldObj.rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.20000000298023224D)
						+ 0.10000000149011612D + axisalignedbb.minX;
				d1 = (double) j
						+ worldObj.rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.20000000298023224D)
						+ 0.10000000149011612D + axisalignedbb.minY;
				d2 = (double) k
						+ worldObj.rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.20000000298023224D)
						+ 0.10000000149011612D + axisalignedbb.minZ;
			}

			switch (side) {
			case DOWN:
				d1 = (double) j + axisalignedbb.minY - 0.10000000149011612D;
				break;
			case EAST:
				d0 = (double) i + axisalignedbb.maxX + 0.10000000149011612D;
				break;
			case NORTH:
				d2 = (double) k + axisalignedbb.minZ - 0.10000000149011612D;
				break;
			case SOUTH:
				d2 = (double) k + axisalignedbb.maxZ + 0.10000000149011612D;
				break;
			case UP:
				if (FBP.enabled && iblockstate != null
						&& (!(iblockstate.getBlock() instanceof BlockLiquid) && !(FBP.frozen && !FBP.spawnWhileFrozen))
						&& (FBP.spawnRedstoneBlockParticles || iblockstate.getBlock() != Blocks.REDSTONE_BLOCK))
					d1 = (double) j + axisalignedbb.maxY;
				else
					d1 = (double) j + axisalignedbb.maxY + 0.10000000149011612D;
				break;
			case WEST:
				d0 = (double) i + axisalignedbb.minX - 0.10000000149011612D;
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
								Iterator it = mp.entrySet().iterator();

								while (it.hasNext()) {
									if ((progress = (DestroyBlockProgress) ((Map.Entry) it.next()).getValue())
											.getPosition() == pos)
										break;
								}
							}
							if (progress != null)
								damage = progress.getPartialBlockDamage();
						} catch (Throwable e) {

						}

						if (FBP.smartBreaking)
							addEffect(new FBPParticle(worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, iblockstate, side, -2)
									.MultiplyVelocity(side == EnumFacing.UP ? 0.7F : 0.15F)
									.multipleParticleScaleBy((float) (0.325F + (damage / 8.125F) * 0.325F)));
						else
							addEffect(new FBPParticle(worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, iblockstate, side, -2)
									.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
					}
				} else
					addEffect(particleFactory
							.createParticle(0, worldObj, d0, d1, d2, 0.0D, 0.0D, 0.0D, Block.getStateId(iblockstate))
							.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
}