package com.TominoCZ.FBP.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.TominoCZ.FBP.BlockNode;
import com.TominoCZ.FBP.FBP;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FBPPlaceAnimationDummyBlock extends Block {
	// HashMap<BlockPos, AxisAlignedBB> bbs_c = new HashMap<BlockPos,
	// AxisAlignedBB>();
	// HashMap<BlockPos, AxisAlignedBB> bbs_b = new HashMap<BlockPos,
	// AxisAlignedBB>();
	// HashMap<BlockPos, AxisAlignedBB> bbs_s = new HashMap<BlockPos,
	// AxisAlignedBB>();

	// HashMap<BlockPos, Float> blockHardnesses = new HashMap<BlockPos, Float>();
	// HashMap<BlockPos, SoundType> soundTypes = new HashMap<BlockPos, SoundType>();
	public HashMap<BlockPos, BlockNode> blockNodes = new HashMap<BlockPos, BlockNode>();

	public FBPPlaceAnimationDummyBlock() {
		super(Material.BARRIER);

		this.setRegistryName("FBPPlaceAnimationCollisionBoundingBoxPlaceholderBlock");
	}

	public void copyState(World w, BlockPos pos, IBlockState state) {
		if (blockNodes.containsKey(pos))
			return;

		FBP.FBPBlock.cleanHashMap();

		// bbs_c.put(pos, state.getCollisionBoundingBox(w, pos));
		// bbs_b.put(pos, state.getBoundingBox(w, pos));
		// bbs_s.put(pos, state.getSelectedBoundingBox(w, pos));

		// blockHardnesses.put(pos, state.getBlockHardness(w, pos));

		// soundTypes.put(pos, state.getBlock().getSoundType(state, w, pos, null));

		//BlockSlab.EnumBlockHalf half = EnumBlockHalf.TOP;

		//if (state.getBlock() instanceof BlockSlab)
			//half = (EnumBlockHalf) state.getProperties().get(BlockSlab.HALF);

		blockNodes.put(pos, new BlockNode(state));
	}

	public void cleanHashMap() {
		// Minecraft mc = Minecraft.getMinecraft();
		// World w = mc.theWorld;

		// if (w == null)
		// return;

		List<BlockPos> toRemove = new ArrayList<BlockPos>();
		Iterator it = blockNodes.keySet().stream().iterator();

		it.forEachRemaining(bp -> {
			// BlockNode bn = blockNodes.get(bp);

			if (Minecraft.getMinecraft().theWorld.getBlockState((BlockPos) bp).getBlock() != FBP.FBPBlock)
				toRemove.add((BlockPos) bp);
		});

		for (BlockPos bp : toRemove) {
			blockNodes.remove(bp);
			// bbs_b.remove(bp);
			// bbs_s.remove(bp);
			// blockHardnesses.remove(bp);
			// soundTypes.remove(bp);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		if (pos == null || !blockNodes.containsKey(pos))
			return this.FULL_BLOCK_AABB;
		BlockNode n = blockNodes.get(pos);

		return n.state.getCollisionBoundingBox(worldIn, pos);//.addCoord(0, n.half == EnumBlockHalf.BOTTOM ? -0.5 : 0, 0);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		if (pos == null || !blockNodes.containsKey(pos))
			return this.FULL_BLOCK_AABB;
		BlockNode n = blockNodes.get(pos);

		return n.state.getBoundingBox(worldIn, pos);//.addCoord(0, n.half == EnumBlockHalf.BOTTOM ? -0.5 : 0, 0);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		if (pos == null || !blockNodes.containsKey(pos))
			return this.FULL_BLOCK_AABB.offset(pos);
		BlockNode n = blockNodes.get(pos);

		return n.state.getSelectedBoundingBox(worldIn, pos);//.addCoord(0, n.half == EnumBlockHalf.BOTTOM ? -0.5 : 0, 0);
	}

	@Override
	public float getBlockHardness(IBlockState blockState, World w, BlockPos pos) {
		if (!blockNodes.containsKey(pos))
			return blockState.getBlockHardness(w, pos);

		BlockNode n = blockNodes.get(pos);

		return n.state.getBlockHardness(w, pos);
	}

	@Override
	public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity) {
		if (!blockNodes.containsKey(pos))
			return SoundType.STONE;

		BlockNode n = blockNodes.get(pos);

		return n.state.getBlock().getSoundType(state, world, pos, entity);
	}

	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return null;
	}

	@Override
	public Item getItemDropped(IBlockState state, Random r, int i) {
		return null;
	}

	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@SideOnly(Side.CLIENT)
	public float getAmbientOcclusionLightValue(IBlockState state) {
		return 1.0F;
	}
}
