package com.TominoCZ.FBP.node;

import com.TominoCZ.FBP.particle.FBPBlockPlaceAnimationDummyParticle;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;

public class BlockNode {
	public IBlockState state;
	public Block originalBlock;
	public int meta;
	
	public FBPBlockPlaceAnimationDummyParticle particle;
	
	public BlockNode(IBlockState s, FBPBlockPlaceAnimationDummyParticle p) {
		particle = p;
		state = s;
		originalBlock = s.getBlock();
		meta = originalBlock.getMetaFromState(s);
	}
}
