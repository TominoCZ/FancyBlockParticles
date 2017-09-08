package com.TominoCZ.FBP.handler;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.block.FBPPlaceAnimationDummyBlock;
import com.TominoCZ.FBP.entity.renderer.FBPEntityRenderer;
import com.TominoCZ.FBP.node.BlockNode;
import com.TominoCZ.FBP.particle.FBPBlockPlaceAnimationDummyParticle;
import com.TominoCZ.FBP.particle.FBPParticleManager;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleDigging.Factory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FBPEventHandler {
	Minecraft mc;

	BlockPos lastPos;

	public FBPEventHandler() {
		mc = Minecraft.getMinecraft();
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
		if (e.getPlacedBlock().getBlock() == FBP.FBPBlock)
			e.setCanceled(true);
	}

	@SubscribeEvent
	public void onInteractionEvent(RightClickBlock e) {
		if (!FBP.enabled)
			return;

		EnumFacing facing = e.getFace();
		EnumHand hand = e.getHand();

		EntityPlayer plr = e.getEntityPlayer();

		ItemStack itemStack = e.getItemStack();
		World w = e.getWorld();
		BlockPos pos = e.getPos().offset(facing);

		Vec3d vec = e.getHitVec();

		Block b = null;
		IBlockState clickedBlockState = w.getBlockState(pos.offset(facing.getOpposite()));

		if (w instanceof WorldClient && itemStack != null) {
			b = Block.getBlockFromItem(itemStack.getItem());

			if (b instanceof BlockSlab && w.getBlockState(pos.offset(facing.getOpposite()))
					.getBlock() instanceof FBPPlaceAnimationDummyBlock)
				return;

			if (b != null && canBlockBePlaced(plr, w, e.getPos(), facing, hand, e.getUseBlock(), e.getUseItem(),
					e.getItemStack(), vec)) {

				if (b == FBP.FBPBlock) {
					mc.thePlayer.inventory.deleteStack(itemStack);
					mc.thePlayer.inventory.markDirty();
					e.setCanceled(true);
					return;
				}

				if (!FBP.fancyPlaceAnim)
					return;

				int itemBlockMeta = ((ItemBlock) Item.getItemFromBlock(b)).getMetadata(itemStack.getMetadata());

				float f = (float) (vec.xCoord - (double) pos.getX());
				float f1 = (float) (vec.yCoord - (double) pos.getY());
				float f2 = (float) (vec.zCoord - (double) pos.getZ());

				IBlockState bs = b.getStateForPlacement(w, pos, facing, f, f1, f2, itemBlockMeta, plr, itemStack);

				IBlockState stateAtPos;
				boolean becomesDoubleSlab = false;
				boolean isSlabAtPos = (stateAtPos = w.getBlockState(pos)).getBlock() instanceof BlockSlab;

				if (b instanceof BlockSlab || isSlabAtPos) {
					ItemSlab is = (ItemSlab) Item.getItemFromBlock(b);

					BlockSlab toPlace = ((BlockSlab) b);

					IProperty<?> iproperty = ((BlockSlab) b).getVariantProperty();

					BlockSlab singleSlab = null;
					BlockSlab doubleSlab = null;

					try {
						singleSlab = (BlockSlab) ReflectionHelper.findField(ItemSlab.class,

								"field_150949_c", "singleSlab").get(is);
						doubleSlab = (BlockSlab) ReflectionHelper
								.findField(ItemSlab.class, "field_179226_c", "doubleSlab").get(is);
						EnumBlockHalf half;

						if (isSlabAtPos) {
							half = stateAtPos.getValue(BlockSlab.HALF);

							if (stateAtPos.getValue(iproperty) == bs.getValue(iproperty)
									&& ((half == EnumBlockHalf.TOP && f1 < 0.5)
											|| half == EnumBlockHalf.BOTTOM && f1 > 0.5)) {
								bs = doubleSlab.getStateFromMeta(itemBlockMeta);

								b = bs.getBlock();

								becomesDoubleSlab = true;
							}
						} else {
							half = clickedBlockState.getValue(BlockSlab.HALF);
							if (clickedBlockState.getValue(iproperty) == bs.getValue(iproperty)
									&& ((facing == EnumFacing.DOWN && half == EnumBlockHalf.TOP)
											|| (facing == EnumFacing.UP && half == EnumBlockHalf.BOTTOM))) {
								bs = doubleSlab.getStateFromMeta(itemBlockMeta);

								b = bs.getBlock();
								pos = pos.offset(facing.getOpposite());

								becomesDoubleSlab = true;
							}
						}
					} catch (Exception ex) {

					}
				}

				bs = bs.getActualState(w, pos);

				long seed = MathHelper.getPositionRandom(pos);

				AxisAlignedBB bb1 = bs.getBoundingBox(w, pos).offset(pos);
				AxisAlignedBB bb2 = plr.getEntityBoundingBox();

				if (b instanceof BlockFalling) {
					BlockFalling bf = (BlockFalling) b;
					if (bf.canFallThrough(w.getBlockState(pos.offset(EnumFacing.DOWN))))
						return;
				}

				if ((b.canPlaceBlockAt(w, pos) || becomesDoubleSlab) && !bb1.intersectsWith(bb2)
						&& FBP.canBlockBeAnimated(bs.getBlock())) {
					FBPBlockPlaceAnimationDummyParticle p = new FBPBlockPlaceAnimationDummyParticle(mc.theWorld,
							pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, bs, e.getEntityPlayer(), seed);

					mc.effectRenderer.addEffect(p);
				}
			}
		}
	}

	boolean canBlockBePlaced(EntityPlayer plr, World w, BlockPos pos, EnumFacing fc, EnumHand hand, Result getUseBlock,
			Result getUseItem, ItemStack stack, Vec3d vec) {
		float f = (float) (vec.xCoord - (double) pos.getX());
		float f1 = (float) (vec.yCoord - (double) pos.getY());
		float f2 = (float) (vec.zCoord - (double) pos.getZ());
		boolean flag = false;
		EnumActionResult result = EnumActionResult.PASS;

		if (mc.playerController.getCurrentGameType() != GameType.SPECTATOR) {
			Item item = stack == null ? null : stack.getItem();
			EnumActionResult ret = item == null ? EnumActionResult.PASS
					: item.onItemUseFirst(stack, plr, w, pos, fc, f, f1, f2, hand);
			if (ret != EnumActionResult.PASS)
				return false;

			BlockNode node = FBP.FBPBlock.blockNodes.get(pos);

			IBlockState iblockstate = w.getBlockState(pos);

			boolean bypass = true;
			for (ItemStack s : new ItemStack[] { plr.getHeldItemMainhand(), plr.getHeldItemOffhand() })
				bypass = bypass && (s == null || s.getItem().doesSneakBypassUse(s, w, pos, plr));

			if ((!plr.isSneaking() || bypass || getUseBlock == Result.ALLOW)) {
				if (getUseBlock != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
					flag = iblockstate.getBlock().onBlockActivated(w, pos, iblockstate, plr, hand, stack, fc, f, f1,
							f2);
				if (flag)
					return !(iblockstate.getBlock() instanceof BlockCommandBlock);
			}

			if (!flag && stack != null && stack.getItem() instanceof ItemBlock) {
				ItemBlock itemblock = (ItemBlock) stack.getItem();

				if (!itemblock.canPlaceBlockOnSide(w, pos, fc, plr, stack))
					return false;
			}
		}

		if (stack != null && !flag && mc.playerController.getCurrentGameType() != GameType.SPECTATOR
				|| getUseItem == Result.ALLOW) {
			if (stack.getItem() instanceof ItemBlock && !plr.canUseCommandBlock()) {
				Block block = ((ItemBlock) stack.getItem()).getBlock();

				if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
					return false;
				}
			}

			if (mc.playerController.getCurrentGameType().isCreative()) {
				if (result == EnumActionResult.FAIL)
					return false;
			}
		}
		return true;
	}
}