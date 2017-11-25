package com.TominoCZ.FBP.node;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;

public class BlockPosNode {
	ConcurrentHashMap<BlockPos, IBlockState> possible = new ConcurrentHashMap<BlockPos, IBlockState>();

	public boolean checked = false;

	public void add(BlockPos pos, IBlockState stateAtPos) {
		possible.put(pos, stateAtPos);
	}

	public boolean hasPos(BlockPos pos) {
		return possible.containsKey(pos);
	}

	public boolean hasState(IBlockState state) {
		return possible.containsValue(state);
	}

	public IBlockState stateAt(BlockPos pos) {
		return possible.get(pos);
	}

	public boolean isSame(BlockPos pos) {
		if (pos == null)
			return false;

		return Minecraft.getMinecraft().theWorld.getBlockState(pos) == stateAt(pos);
	}

	public void removeAllExcept(BlockPos pos) {
		if (!possible.containsKey(pos))
			return;

		for (BlockPos p : possible.keySet())
			if (!p.equals(pos))
				possible.remove(p);
	}
}
