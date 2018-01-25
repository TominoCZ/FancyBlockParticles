package com.TominoCZ.FBP.block;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.util.ForgeDirection;

public class FBPBlockPos {
	private int x, y, z;

	public FBPBlockPos(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public FBPBlockPos(double x, double y, double z) {
		this(MathHelper.floor_double(x), MathHelper.floor_double(y), MathHelper.floor_double(z));
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public FBPBlockPos offset(ForgeDirection fd) {
		return new FBPBlockPos(x + fd.offsetX, y + fd.offsetY, z + fd.offsetZ);
	}

	public FBPBlockPos add(int x, int y, int z) {
		return new FBPBlockPos(this.x + x, this.y + y, this.z + z);
	}

	public FBPBlockPos offset(EnumFacing f) {
		return new FBPBlockPos(x + f.getFrontOffsetX(), y + f.getFrontOffsetY(), z + f.getFrontOffsetZ());
	}
}
