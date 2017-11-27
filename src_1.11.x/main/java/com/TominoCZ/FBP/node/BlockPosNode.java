package com.TominoCZ.FBP.node;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

public class BlockPosNode {
	ConcurrentSet<BlockPos> possible = new ConcurrentSet<BlockPos>();

	public void add(BlockPos pos, IBlockState stateAtPos) {
		possible.add(pos);
	}

	public boolean hasPos(BlockPos pos) {
		return possible.contains(pos);
	}
}
