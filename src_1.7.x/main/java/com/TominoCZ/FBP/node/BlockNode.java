package com.TominoCZ.FBP.node;

import com.TominoCZ.FBP.particle.FBPParticleBlock;

import net.minecraft.block.Block;

public class BlockNode {
	public Block block;
	public int meta;

	public FBPParticleBlock particle;

	public BlockNode(Block block, int meta, FBPParticleBlock particle) {
		this.particle = particle;
		this.block = block;
		this.meta = meta;
	}
}
