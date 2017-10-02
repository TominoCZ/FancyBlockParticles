package com.TominoCZ.FBP.handler;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.node.BlockNode;
import com.TominoCZ.FBP.node.BlockPosNode;
import com.TominoCZ.FBP.particle.FBPParticleBlock;
import com.TominoCZ.FBP.particle.FBPParticleManager;
import com.TominoCZ.FBP.renderer.FBPEntityRenderer;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.ParticleDigging.Factory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FBPEventHandler {
	Minecraft mc;

	IWorldEventListener listener;

	ConcurrentSet<BlockPosNode> list;

	public FBPEventHandler() {
		mc = Minecraft.getMinecraft();

		list = new ConcurrentSet<BlockPosNode>();

		listener = new IWorldEventListener() {
			public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
			}

			public void broadcastSound(int soundID, BlockPos pos, int data) {
			}

			public void onEntityAdded(Entity entityIn) {
			}

			public void spawnParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z,
					double xSpeed, double ySpeed, double zSpeed, int... parameters) {
			}

			public void spawnParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord,
					double xSpeed, double ySpeed, double zSpeed, int... parameters) {
			}

			public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
			}

			public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category,
					double x, double y, double z, float volume, float pitch) {
			}

			public void playRecord(SoundEvent soundIn, BlockPos pos) {
			}

			public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
			}

			public void onEntityRemoved(Entity entityIn) {
			}

			public void notifyLightSet(BlockPos pos) {
			}

			@Override
			public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState,
					int flags) {
				if (!FBP.enabled || !FBP.fancyPlaceAnim)
					return;

				BlockPosNode node = getNodeWithPos(pos);

				if ((flags == 2 || flags == 3 || flags == 10 || flags == 11) && !(oldState.equals(newState))
						&& oldState.getBlock() != newState.getBlock() // TODO TESTING...
						&& node != null && !node.checked) {
					if (node.isSame(pos) || newState.getBlock() == FBP.FBPBlock || newState.getBlock() == Blocks.AIR) {
						node.checked = true;
						list.remove(node);
						return;
					}

					list.remove(node);

					node.checked = true;

					long seed = MathHelper.getPositionRandom(pos);

					IBlockState state = newState.getActualState(worldIn, pos);

					boolean isNotFalling = true;

					if (state.getBlock() instanceof BlockFalling) {
						BlockFalling bf = (BlockFalling) state.getBlock();
						if (bf.canFallThrough(worldIn.getBlockState(pos.offset(EnumFacing.DOWN))))
							isNotFalling = false;
					}

					if (FBP.INSTANCE.canBlockBeAnimated(state.getBlock()) && isNotFalling) {
						FBPParticleBlock p = new FBPParticleBlock(mc.world, pos.getX() + 0.5f, pos.getY() + 0.5f,
								pos.getZ() + 0.5f, state, seed);

						FBP.FBPBlock.copyState(worldIn, pos, state, p);

						mc.effectRenderer.addEffect(p);
					}

					// clear list
					for (BlockPosNode n : list) {
						if (!n.isSame(pos)) {
							list.remove(n);
							return;
						}
					}
				}
			}
		};
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load e) {
		e.getWorld().addEventListener(listener);
		list.clear();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onEntityJoinWorldEvent(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof EntityPlayerSP) {
			mc.effectRenderer = new FBPParticleManager(e.getWorld(), mc.getTextureManager(), new Factory());

			mc.entityRenderer = new FBPEntityRenderer(mc, mc.getResourceManager());
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerPlaceBlockEvent(BlockEvent.PlaceEvent e) {
		IBlockState bs = e.getPlacedBlock();
		Block placed = bs.getBlock();

		if (placed == FBP.FBPBlock)
			e.setCanceled(true);
	}

	BlockPosNode getNodeWithPos(BlockPos p) {
		for (BlockPosNode n : list) {
			if (n.hasPos(p))
				return n;
		}
		return null;
	}

	public void removeEntry(BlockPos pos) {
		BlockPosNode node;

		if ((node = getNodeWithPos(pos)) != null)
			list.remove(node);
	}

	@SubscribeEvent
	public void onInteractionEvent(RightClickBlock e) {
		if (e.getWorld().isRemote) {
			if (e.getItemStack() == null || !(e.getItemStack().getItem() instanceof ItemBlock))
				return;
			
			BlockPos pos = e.getPos();
			BlockPos pos_o = e.getPos().offset(e.getFace());

			Block inHand = null;

			IBlockState atPos = e.getWorld().getBlockState(pos);
			IBlockState _atPos = atPos;
			IBlockState offset = e.getWorld().getBlockState(pos_o);

			boolean bool = false;

			float f = (float) (e.getHitVec().xCoord - pos.getX());
			float f1 = (float) (e.getHitVec().yCoord - pos.getY());
			float f2 = (float) (e.getHitVec().zCoord - pos.getZ());

			if (atPos.getBlock() == FBP.FBPBlock) {
				BlockNode n = FBP.FBPBlock.blockNodes.get(pos);

				if (n != null && n.state.getBlock() != null) {
					boolean activated = n.originalBlock.onBlockActivated(e.getWorld(), pos, n.state, mc.player,
							EnumHand.MAIN_HAND, e.getFace(), f, f1, f2);

					if (activated)
						return;

					atPos = n.state;
				}

				// if placed quicky atop each other
				if (atPos.getBlock() instanceof BlockSlab) {
					BlockSlab.EnumBlockHalf half = atPos.getValue(BlockSlab.HALF);

					if (e.getFace() == EnumFacing.UP) {
						if (half == EnumBlockHalf.BOTTOM) {
							bool = true;
						}
					} else if (e.getFace() == EnumFacing.DOWN) {
						if (half == EnumBlockHalf.TOP) {
							bool = true;
						}
					}
				}
			}
			if (offset.getBlock() == FBP.FBPBlock) {
				BlockNode n = FBP.FBPBlock.blockNodes.get(pos_o);

				if (n != null && n.state.getBlock() != null)
					offset = n.state;
			}

			if (e.getItemStack() != null && e.getItemStack().getItem() != null)
				inHand = Block.getBlockFromItem(e.getItemStack().getItem());

			boolean addedOffset = false;

			if (getNodeWithPos(pos) == null && getNodeWithPos(pos_o) == null) {
				BlockPosNode node = new BlockPosNode();

				try {
					if (!bool && (inHand != null && offset.getMaterial().isReplaceable()
							&& !atPos.getBlock().isReplaceable(e.getWorld(), pos)
							&& inHand.canPlaceBlockAt(e.getWorld(), pos_o))) {
						node.add(pos_o, offset);
						addedOffset = true;
					} else
						node.add(pos, atPos);

					boolean okToAdd = inHand != null && inHand != Blocks.AIR
							&& inHand.canPlaceBlockAt(e.getWorld(), addedOffset ? pos_o : pos);

					// do torch check
					if (inHand != null && inHand instanceof BlockTorch) {
						BlockTorch bt = (BlockTorch) inHand;

						if (!bt.canPlaceBlockAt(e.getWorld(), pos_o))
							okToAdd = false;

						if (atPos.getBlock() == Blocks.TORCH) {
							for (EnumFacing fc : EnumFacing.VALUES) {
								BlockPos p = pos_o.offset(fc);
								Block bl = e.getWorld().getBlockState(p).getBlock();

								if (bl != Blocks.TORCH && bl != FBP.FBPBlock
										&& bl.isSideSolid(bl.getDefaultState(), e.getWorld(), p, fc)) {
									okToAdd = true;
									break;
								} else
									okToAdd = false;
							}
						}
					}

					// add if all ok
					if (okToAdd)
						list.add(node);
				} catch (Throwable t) {

				}
			}
		}
	}
}