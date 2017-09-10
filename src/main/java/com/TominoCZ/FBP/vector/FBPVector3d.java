package com.TominoCZ.FBP.vector;

import net.minecraft.client.renderer.Vector3d;

public class FBPVector3d extends Vector3d {
	
	public FBPVector3d() {
		
	}

	public FBPVector3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void copyFrom(Vector3d vec) {
		this.x = vec.x;
		this.y = vec.y;
		this.z = vec.z;
	}

	public void add(Vector3d vec) {
		this.x += vec.x;
		this.y += vec.y;
		this.z += vec.z;
	}

	public void add(double d) {
		this.x += d;
		this.y += d;
		this.z += d;
	}
	
	public void zero() {
		this.x = 0;
		this.y = 0;
		this.z = 0;
	}

	public FBPVector3d partialVec(FBPVector3d prevRot, float partialTicks) {
		FBPVector3d v = new FBPVector3d();

		v.x = prevRot.x + (this.x - prevRot.x) * partialTicks;
		v.y = prevRot.y + (this.y - prevRot.y) * partialTicks;
		v.z = prevRot.z + (this.z - prevRot.z) * partialTicks;

		return v;
	}
}
