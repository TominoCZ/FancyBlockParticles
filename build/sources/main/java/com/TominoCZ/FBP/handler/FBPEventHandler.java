package com.TominoCZ.FBP.handler;

import java.util.HashMap;

import com.TominoCZ.FBP.BlockNode;
import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.block.FBPPlaceAnimationDummyBlock;
import com.TominoCZ.FBP.particle.FBPBlockPlaceAnimationDummyParticle;
import com.TominoCZ.FBP.particle.FBPParticleManager;

import jline.internal.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.ParticleDigging.Factory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameType;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FBPEventHandler {
	Minecraft mc;

	BlockPos lastPos;

	IWorldEventListener listener;

	public FBPEventHandler() {
		mc = Minecraft.getMinecraft();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onEntityJoinWorldEvent(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof EntityPlayerSP)
			mc.effectRenderer = new FBPParticleManager(e.getWorld(), mc.getTextureManager(), new Factory());
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load e) {

	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onBreakBlockEvent(BlockEvent.BreakEvent e) {
		IBlockState state = mc.theWorld.getBlockState(e.getPos());

		BlockNode node = FBP.FBPBlock.blockNodes.get(e.getPos());

		if (node == null)
			return;

		if (state.getBlock() == node.originalBlock) {
			mc.effectRenderer.addBlockDestroyEffects(e.getPos(), node.originalBlock.getDefaultState());
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerPlaceBlockEvent(BlockEvent.PlaceEvent e) {
		if (e.getPlacedBlock().getBlock() == FBP.FBPBlock)
			e.setCanceled(true);
		/*
		 * else if (FBP.enabled && FBP.fancyPlaceAnim) { if (mc.getIntegratedServer() !=
		 * null) return;
		 * 
		 * BlockPos pos = e.getPos(); IBlockState bs = e.getPlacedBlock();
		 * 
		 * long seed = MathHelper.getPositionRandom(pos);
		 * 
		 * World w = e.getWorld();
		 * 
		 * AxisAlignedBB bb1 = bs.getBoundingBox(w, pos).offset(pos); AxisAlignedBB bb2
		 * = e.getPlayer().getEntityBoundingBox();
		 * 
		 * if (bs.getBlock().canPlaceBlockAt(w, pos) && !bb1.intersectsWith(bb2) &&
		 * FBP.canBlockBeAnimated(bs.getBlock())) mc.effectRenderer.addEffect(new
		 * FBPBlockPlaceAnimationDummyParticle(mc.theWorld, pos.getX() + 0.5f,
		 * pos.getY() + 0.5f, pos.getZ() + 0.5f, bs, e.getPlayer(), seed)); }
		 */
	}

	@SubscribeEvent
	public void onInteractionEvent(RightClickBlock e) {
		EnumFacing facing = e.getFace();
		EnumHand hand = e.getHand();

		EntityPlayer plr = e.getEntityPlayer();

		ItemStack itemStack = e.getItemStack();
		World w = e.getWorld();
		BlockPos pos = e.getPos().offset(facing);

		Block b = null;
		Block clickedBlock = w.getBlockState(pos.offset(facing.getOpposite())).getBlock();

		if (w instanceof WorldClient && itemStack != null) {
			b = Block.getBlockFromItem(itemStack.getItem());
			Vec3d hitVec = e.getHitVec();

			if (b != null && canBlockBePlaced(plr, w, e.getPos(), facing, hand, e.getUseBlock(), e.getUseItem(),
					e.getItemStack(), hitVec)) {

				if (b == FBP.FBPBlock) {
					e.setCanceled(true);
					return;
				}

				if (!FBP.fancyPlaceAnim)
					return;

				int itemBlockMeta = ((ItemBlock) Item.getItemFromBlock(b)).getMetadata(itemStack.getMetadata());

				IBlockState bs = b.getStateForPlacement(w, pos, facing.getOpposite(), (float) hitVec.xCoord,
						(float) hitVec.yCoord, (float) hitVec.zCoord, itemBlockMeta, plr, itemStack);

				bs = b.onBlockPlaced(w, pos, facing, (float) hitVec.xCoord, (float) hitVec.yCoord,
						(float) hitVec.zCoord, itemBlockMeta, plr);

				bs = b.getActualState(bs, w, pos);

				/*
				 * boolean becomesDoubleSlab = false;
				 * 
				 * if (b instanceof BlockSlab) { ItemSlab is = (ItemSlab)
				 * Item.getItemFromBlock(b);
				 * 
				 * // TODO check if placing a single slab
				 * 
				 * // BlockSlab toPlace = ((BlockSlab) b);
				 * 
				 * IProperty<?> iproperty = ((BlockSlab) b).getVariantProperty();
				 * 
				 * BlockSlab singleSlab = null; BlockSlab doubleSlab = null;
				 * 
				 * try { singleSlab = (BlockSlab) ReflectionHelper .findField(ItemSlab.class,
				 * "field_150949_c", "singleSlab").get(is); doubleSlab = (BlockSlab)
				 * ReflectionHelper .findField(ItemSlab.class, "field_179226_c",
				 * "doubleSlab").get(is); // bs = makeState(doubleSlab, iproperty, comparable1);
				 * 
				 * if (itemStack.stackSize != 0 && plr.canPlayerEdit(pos.offset(facing), facing,
				 * itemStack)) { Comparable<?> comparable =
				 * singleSlab.getTypeForItem(itemStack);
				 * 
				 * boolean single = false;
				 * 
				 * if (clickedBlock == singleSlab) { iproperty =
				 * singleSlab.getVariantProperty(); comparable = bs.getValue(iproperty);
				 * BlockSlab.EnumBlockHalf blockslab$enumblockhalf = (BlockSlab.EnumBlockHalf)
				 * bs .getValue(BlockSlab.HALF);
				 * 
				 * Comparable<?> comparable1 =
				 * clickedBlock.getActualState(clickedBlock.getDefaultState(), w,
				 * pos.offset(facing.getOpposite())).getValue(iproperty);
				 * 
				 * if ((facing == EnumFacing.UP && blockslab$enumblockhalf ==
				 * BlockSlab.EnumBlockHalf.BOTTOM || facing == EnumFacing.DOWN &&
				 * blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.TOP) && comparable1 ==
				 * comparable) { bs = makeState(doubleSlab, iproperty, comparable1);
				 * becomesDoubleSlab = true; } } } } catch (Exception e1) {
				 * e1.printStackTrace(); }
				 * 
				 * if (!becomesDoubleSlab && facing != EnumFacing.UP && facing !=
				 * EnumFacing.DOWN) { bs = bs.withProperty(BlockSlab.HALF, hitVec.yCoord - (int)
				 * hitVec.yCoord > 0.5 ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM); } //
				 * IBlockState iblockstate1 = this.makeState(iproperty, comparable1); }
				 * 
				 * if (itemStack.stackSize != 0 && plr.canPlayerEdit(pos.offset(facing), facing,
				 * stack)) { Comparable<?> comparable = this.singleSlab.getTypeForItem(stack);
				 * IBlockState iblockstate = worldIn.getBlockState(pos);
				 * 
				 * if (iblockstate.getBlock() == this.singleSlab) { IProperty<?> iproperty =
				 * this.singleSlab.getVariantProperty(); Comparable<?> comparable1 =
				 * iblockstate.getValue(iproperty); BlockSlab.EnumBlockHalf
				 * blockslab$enumblockhalf =
				 * (BlockSlab.EnumBlockHalf)iblockstate.getValue(BlockSlab.HALF);
				 * 
				 * if ((facing == EnumFacing.UP && blockslab$enumblockhalf ==
				 * BlockSlab.EnumBlockHalf.BOTTOM || facing == EnumFacing.DOWN &&
				 * blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.TOP) && comparable1 ==
				 * comparable) { IBlockState iblockstate1 = this.makeState(iproperty,
				 * comparable1); AxisAlignedBB axisalignedbb =
				 * iblockstate1.getCollisionBoundingBox(worldIn, pos);
				 * 
				 * if (axisalignedbb != Block.NULL_AABB &&
				 * worldIn.checkNoEntityCollision(axisalignedbb.offset(pos)) &&
				 * worldIn.setBlockState(pos, iblockstate1, 11)) { SoundType soundtype =
				 * this.doubleSlab.getSoundType(iblockstate1, worldIn, pos,playerIn);
				 * worldIn.playSound(playerIn, pos, soundtype.getPlaceSound(),
				 * SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F,
				 * soundtype.getPitch() * 0.8F); --stack.stackSize; }
				 * 
				 * return EnumActionResult.SUCCESS; } }
				 * 
				 * return this.tryPlace(playerIn, stack, worldIn, pos.offset(facing),
				 * comparable) ? EnumActionResult.SUCCESS : super.onItemUse(stack, playerIn,
				 * worldIn, pos, hand, facing, hitX, hitY, hitZ); } else { return
				 * EnumActionResult.FAIL; }
				 */

				long seed = MathHelper.getPositionRandom(pos);

				AxisAlignedBB bb1 = bs.getBoundingBox(w, pos).offset(pos);
				AxisAlignedBB bb2 = plr.getEntityBoundingBox();

				if (b.canPlaceBlockAt(w, pos) && !bb1.intersectsWith(bb2) && FBP.canBlockBeAnimated(bs.getBlock()))
					mc.effectRenderer.addEffect(new FBPBlockPlaceAnimationDummyParticle(mc.theWorld, pos.getX() + 0.5f,
							pos.getY() + 0.5f/* + (becomesDoubleSlab ? -1 : 0) */, pos.getZ() + 0.5f, bs,
							e.getEntityPlayer(), seed));
			}
		}
	}

	@SubscribeEvent
	public void onBlockBreakEvent(BlockEvent.BreakEvent e) {
		IBlockState state = e.getWorld().getBlockState(e.getPos());

		e.setCanceled(state != null && state.getBlock() == FBP.FBPBlock);
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

			IBlockState iblockstate = w.getBlockState(pos);
			boolean bypass = true;
			for (ItemStack s : new ItemStack[] { plr.getHeldItemMainhand(), plr.getHeldItemOffhand() })
				bypass = bypass && (s == null || s.getItem().doesSneakBypassUse(s, w, pos, plr));

			if (!plr.isSneaking() || bypass || getUseBlock == Result.ALLOW) {
				if (getUseBlock != Result.DENY)
					flag = iblockstate.getBlock().onBlockActivated(w, pos, iblockstate, plr, hand, stack, fc, f, f1,
							f2);
				if (flag)
					return false;
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

	protected <T extends Comparable<T>> IBlockState makeState(BlockSlab doubleSlab, IProperty<T> p_185055_1_,
			Comparable<?> p_185055_2_) {
		return doubleSlab.getDefaultState().withProperty(p_185055_1_, (T) p_185055_2_);
	}
}