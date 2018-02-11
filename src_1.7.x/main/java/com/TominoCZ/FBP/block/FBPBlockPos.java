package com.TominoCZ.FBP.block;

import net.minecraft.util.MathHelper;

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

	public boolean isSame(FBPBlockPos pos) {
		return x == pos.getX() && y == pos.getY() && z == pos.getZ();
	}
}