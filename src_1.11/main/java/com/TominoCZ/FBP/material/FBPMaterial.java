package com.TominoCZ.FBP.material;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class FBPMaterial extends Material {

	public FBPMaterial() {
		super(MapColor.AIR);
	}

	@Override
	public boolean isSolid() {
		return false;
	}

	@Override
	public boolean blocksLight() {
		return false;
	}

	@Override
	public boolean blocksMovement() {
		return true;
	}
}