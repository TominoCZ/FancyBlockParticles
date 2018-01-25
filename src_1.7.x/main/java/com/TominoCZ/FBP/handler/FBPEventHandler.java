package com.TominoCZ.FBP.handler;

import java.util.Iterator;
import java.util.List;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.block.FBPBlockHelper;
import com.TominoCZ.FBP.block.FBPBlockPos;
import com.TominoCZ.FBP.node.BlockNode;
import com.TominoCZ.FBP.node.BlockPosNode;
import com.TominoCZ.FBP.particle.FBPParticleBlock;
import com.TominoCZ.FBP.particle.FBPParticleManager;
import com.TominoCZ.FBP.renderer.FBPWeatherRenderer;
import com.TominoCZ.FBP.util.FBPRenderUtil;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockTorch;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;

public class FBPEventHandler {
	Minecraft mc;

	IWorldAccess listener;

	ConcurrentSet<BlockPosNode> list;

	public FBPEventHandler() {
		mc = Minecraft.getMinecraft();

		list = new ConcurrentSet<BlockPosNode>();
		listener = new IWorldAccess() {

			@Override
			public void spawnParticle(String p_72708_1_, double p_72708_2_, double p_72708_4_, double p_72708_6_,
					double p_72708_8_, double p_72708_10_, double p_72708_12_) {
			}

			@Override
			public void playSoundToNearExcept(EntityPlayer p_85102_1_, String p_85102_2_, double p_85102_3_,
					double p_85102_5_, double p_85102_7_, float p_85102_9_, float p_85102_10_) {
			}

			@Override
			public void playSound(String p_72704_1_, double p_72704_2_, double p_72704_4_, double p_72704_6_,
					float p_72704_8_, float p_72704_9_) {
			}

			@Override
			public void playRecord(String p_72702_1_, int p_72702_2_, int p_72702_3_, int p_72702_4_) {
			}

			@Override
			public void playAuxSFX(EntityPlayer p_72706_1_, int p_72706_2_, int p_72706_3_, int p_72706_4_,
					int p_72706_5_, int p_72706_6_) {
			}

			@Override
			public void onStaticEntitiesChanged() {
			}

			@Override
			public void onEntityDestroy(Entity p_72709_1_) {
			}

			@Override
			public void onEntityCreate(Entity p_72703_1_) {
			}

			@Override
			public void markBlockRangeForRenderUpdate(int p_147585_1_, int p_147585_2_, int p_147585_3_,
					int p_147585_4_, int p_147585_5_, int p_147585_6_) {
			}

			@Override
			public void markBlockForUpdate(int posX, int posY, int posZ) {
				Block b = mc.theWorld.getBlock(posX, posY, posZ);
				int meta = mc.theWorld.getBlockMetadata(posX, posY, posZ);

				FBPBlockPos pos = new FBPBlockPos(posX, posY, posZ);

				if (FBP.fancyPlaceAnim) {
					BlockPosNode node = getNodeWithPos(pos);

					if (node != null) {
						if (!node.checked && !node.updated) {
							if (b == Blocks.ladder || b == Blocks.air || b instanceof BlockDoublePlant) {
								removePosEntry(pos);

								return;
							}

							boolean isNotFalling = true;

							if (b instanceof BlockFalling) {
								BlockFalling bf = (BlockFalling) b;
								FBPBlockPos under = pos.offset(EnumFacing.DOWN);

								if (bf.func_149831_e(mc.theWorld, under.getX(), under.getY(), under.getZ())
										|| bf.fallInstantly)
									isNotFalling = false;
							}
							if (!FBP.INSTANCE.isInExceptions(b, false) && isNotFalling) {
								node.checked = true;
								node.updated = true;

								node.particle = new FBPParticleBlock(mc.theWorld, posX + 0.5f, posY + 0.5f, posZ + 0.5f,
										b, meta);
								// NEEDS TO BE HERE
								mc.theWorld.setLightValue(EnumSkyBlock.Block, posX, posY, posZ, 0);
							}
						} else if (b == Blocks.air) {
							if (mc.gameSettings.keyBindAttack.getIsKeyPressed()) {
								MovingObjectPosition over = mc.objectMouseOver;

								if (over != null) {
									if (over.blockX == posX && over.blockY == posY && over.blockZ == posZ) {
										if (node != null && node.checked) {
											if (node.particle != null)
												node.particle.setDead();
											else {
												FBP.FBPBlock.removeNode(pos);
												removePosEntry(pos);
											}
										}
									}
								}
							}
						}
					}
				}
			}

			@Override
			public void markBlockForRenderUpdate(int x, int y, int z) {
				FBPBlockPos pos = new FBPBlockPos(x, y, z);

				BlockPosNode node = getNodeWithPos(pos);

				if (node != null && node.checked && node.updated && node.particle != null) {
					node.updated = false;

					Block b = mc.theWorld.getBlock(x, y, z);

					b.onBlockPlacedBy(mc.theWorld, x, y, z, mc.thePlayer, mc.thePlayer.getHeldItem());
					int meta = mc.theWorld.getBlockMetadata(x, y, z);

					node.particle.block = b;
					node.particle.meta = meta;

					mc.effectRenderer.addEffect(node.particle);

					FBP.FBPBlock.copyState(pos, b, meta, node.particle);
					mc.theWorld.setBlock(x, y, z, Blocks.ladder, meta, 2);

					Chunk c = mc.theWorld.getChunkFromBlockCoords(x, z);
					c.resetRelightChecks();
					c.isLightPopulated = true;

					FBPRenderUtil.markBlockForRender(pos);
				}
			}

			@Override
			public void destroyBlockPartially(int p_147587_1_, int p_147587_2_, int p_147587_3_, int p_147587_4_,
					int p_147587_5_) {
			}

			@Override
			public void broadcastSound(int p_82746_1_, int p_82746_2_, int p_82746_3_, int p_82746_4_, int p_82746_5_) {
			}
		};
	}

	@SubscribeEvent
	public void onDrawSelectionBox(DrawBlockHighlightEvent e) {
		if (e.player == mc.thePlayer) {
			MovingObjectPosition obj = mc.objectMouseOver;

			if (obj != null) {
				FBPBlockPos pos = new FBPBlockPos(obj.blockX, obj.blockY, obj.blockZ);

				BlockPosNode n = getNodeWithPos(pos);

				if (n != null) {
					if (n.checked)
						e.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onInteractionEvent(PlayerInteractEvent e) {
		if (e.world.isRemote) {
			if (e.action == Action.RIGHT_CLICK_BLOCK) {
				if (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock))
					return;

				Vec3 hitVec = mc.objectMouseOver.hitVec;

				ForgeDirection facing = ForgeDirection.getOrientation(e.face);

				FBPBlockPos pos = new FBPBlockPos(e.x, e.y, e.z);
				FBPBlockPos pos_o = pos.offset(facing);

				ForgeDirection side;

				Block inHand = null;

				Block atPos = mc.theWorld.getBlock(pos.getX(), pos.getY(), pos.getZ());
				Block offset = mc.theWorld.getBlock(pos_o.getX(), pos_o.getY(), pos_o.getZ());

				boolean bool = false;

				float f = (float) (hitVec.xCoord - pos.getX());
				float f1 = (float) (hitVec.yCoord - pos.getY());
				float f2 = (float) (hitVec.zCoord - pos.getZ());

				if (atPos == Blocks.ladder) {
					BlockNode n = FBP.FBPBlock.getNode(pos);

					if (n != null && n.block != null) {
						atPos = n.block;
					}
				}
				if (offset == Blocks.ladder) {
					BlockNode n = FBP.FBPBlock.getNode(pos_o);

					if (n != null && n.block != null)
						offset = n.block;
				}

				boolean activated = atPos.onBlockActivated(mc.theWorld, e.x, e.y, e.z, mc.thePlayer, e.face,
						(float) hitVec.xCoord, (float) hitVec.yCoord, (float) hitVec.zCoord) && !(atPos instanceof BlockFence) && !mc.thePlayer.isSneaking();
				if (activated)
					return;

				if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() != null)
					inHand = Block.getBlockFromItem(mc.thePlayer.getHeldItem().getItem());

				boolean addedOffset = false;

				BlockPosNode node = new BlockPosNode();

				try {
					if (!bool && (inHand != null && offset.getMaterial().isReplaceable()
							&& !atPos.isReplaceable(mc.theWorld, e.x, e.y, e.z)
							&& inHand.canPlaceBlockAt(mc.theWorld, pos_o.getX(), pos_o.getY(), pos_o.getZ()))) {
						node.add(pos_o);
						addedOffset = true;
					} else
						node.add(pos);

					FBPBlockPos tmpPos = addedOffset ? pos_o : pos;
					Block tmpBlock = addedOffset ? offset : atPos;

					boolean okToAdd = inHand != null && inHand != Blocks.air
							&& inHand.canPlaceBlockAt(mc.theWorld, tmpPos.getX(), tmpPos.getY(), tmpPos.getZ());

					// do torch check
					if (inHand != null && inHand instanceof BlockTorch) {
						BlockTorch bt = (BlockTorch) inHand;

						if (!bt.canPlaceBlockAt(mc.theWorld, pos_o.getX(), pos_o.getY(), pos_o.getZ()))
							okToAdd = false;

						if (atPos == Blocks.torch) {
							for (ForgeDirection fc : ForgeDirection.values()) {
								FBPBlockPos p = pos_o.offset(fc);
								Block bl = mc.theWorld.getBlock(p.getX(), p.getY(), p.getY());

								if (bl != Blocks.torch && bl != Blocks.ladder
										&& bl.isSideSolid(mc.theWorld, p.getX(), p.getY(), p.getZ(), fc)) {
									okToAdd = true;
									break;
								} else
									okToAdd = false;
							}
						}
					}

					BlockPosNode last = getNodeWithPos(pos);
					BlockPosNode last_o = getNodeWithPos(pos_o);

					// add if all ok
					if (okToAdd && FBPBlockHelper.isModelValid(inHand)) {
						if (!tmpBlock.getBlocksMovement(mc.theWorld, tmpPos.getX(), tmpPos.getY(), tmpPos.getZ())) {
							Iterator it = mc.theWorld.playerEntities.iterator();

							while (it.hasNext()) {
								EntityPlayer p = (EntityPlayer) it.next();

								Block B = addedOffset ? offset : atPos;

								AxisAlignedBB box = AxisAlignedBB
										.getBoundingBox(B.getBlockBoundsMinX(), B.getBlockBoundsMinY(),
												B.getBlockBoundsMinZ(), B.getBlockBoundsMaxX(), B.getBlockBoundsMaxY(),
												B.getBlockBoundsMaxZ())
										.offset(tmpPos.getX(), tmpPos.getY(), tmpPos.getZ());

								if (p.boundingBox.intersectsWith(box))
									return;
							}
						}
						boolean replaceable = (addedOffset ? offset : atPos).isReplaceable(mc.theWorld, tmpPos.getX(),
								tmpPos.getY(), tmpPos.getZ());

						if (last != null && !addedOffset && last.checked) // replace
							return;
						if (last_o != null && addedOffset && (last_o.checked || replaceable)) // place on side
							return;

						Chunk c = mc.theWorld.getChunkFromBlockCoords(tmpPos.getX(), tmpPos.getZ());
						c.resetRelightChecks();
						c.isLightPopulated = true;

						list.add(node);
					}
				} catch (Throwable t) {
					list.clear();
				}
			}
		}
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent e) {
		if (!mc.isGamePaused() && mc.theWorld != null) {
			if (mc.theWorld.provider.getWeatherRenderer() == FBP.fancyWeatherRenderer && FBP.enabled)
				((FBPWeatherRenderer) FBP.fancyWeatherRenderer).onUpdate();

			MovingObjectPosition moo = mc.objectMouseOver;
			int x = 0;
			int z = 0;

			if (moo != null) {
				x = moo.blockX;
				z = moo.blockZ;
			} else {
				x = (int) mc.thePlayer.posX;
				z = (int) mc.thePlayer.posZ;
			}
			Chunk c = mc.theWorld.getChunkFromBlockCoords(x, z);

			List l = mc.theWorld.getPendingBlockUpdates(c, false);
			if (l != null)
				System.out.println(l);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load e) {
		e.world.addWorldAccess(listener);
		list.clear();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityJoinWorldEvent(EntityJoinWorldEvent e) {
		if (e.entity == mc.thePlayer) {
			FBP.fancyEffectRenderer = new FBPParticleManager(mc.theWorld, mc.renderEngine);
			FBP.fancyWeatherRenderer = new FBPWeatherRenderer();

			IRenderHandler currentWeatherRenderer = mc.theWorld.provider.getCloudRenderer();

			if (FBP.originalWeatherRenderer == null || (FBP.originalWeatherRenderer != currentWeatherRenderer
					&& currentWeatherRenderer != FBP.fancyWeatherRenderer))
				FBP.originalWeatherRenderer = currentWeatherRenderer;
			if (FBP.originalEffectRenderer == null || (FBP.originalEffectRenderer != mc.effectRenderer
					&& FBP.originalEffectRenderer != FBP.fancyEffectRenderer))
				FBP.originalEffectRenderer = mc.effectRenderer;

			if (FBP.enabled) {
				mc.effectRenderer = FBP.fancyEffectRenderer;

				if (FBP.fancyRain || FBP.fancySnow)
					mc.theWorld.provider.setWeatherRenderer(FBP.fancyWeatherRenderer);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerPlaceBlockEvent(BlockEvent.PlaceEvent e) {
		if (e.block == FBP.FBPBlock)
			e.setCanceled(true);
	}

	public BlockPosNode getNodeWithPos(FBPBlockPos pos) {
		for (BlockPosNode n : list) {
			if (n.hasPos(pos))
				return n;
		}
		return null;
	}

	public void removePosEntry(FBPBlockPos pos) {
		for (int i = 0; i < list.size(); i++) {
			BlockPosNode n = getNodeWithPos(pos);

			if (n != null)
				list.remove(n);
		}
	}
}