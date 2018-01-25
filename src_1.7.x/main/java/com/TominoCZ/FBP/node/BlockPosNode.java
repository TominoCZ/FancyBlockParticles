package com.TominoCZ.FBP.node;

import java.util.Iterator;

import com.TominoCZ.FBP.block.FBPBlockPos;
import com.TominoCZ.FBP.particle.FBPParticleBlock;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.block.Block;

public class BlockPosNode {
	ConcurrentSet<FBPBlockPos> possible = new ConcurrentSet<FBPBlockPos>();

	public boolean checked = false;

	public boolean updated = false;

	public Block block;
	public int meta;

	public FBPParticleBlock particle;

	public void add(FBPBlockPos pos) {
		possible.add(pos);
	}

	public boolean hasPos(FBPBlockPos p1) {
		Iterator it = possible.iterator();

		while (it.hasNext()) {
			FBPBlockPos p2 = (FBPBlockPos) it.next();

			if (p1.getX() == p2.getX() && p1.getY() == p2.getY() && p1.getZ() == p2.getZ())
				return true;
		}

		return false;
	}
}
