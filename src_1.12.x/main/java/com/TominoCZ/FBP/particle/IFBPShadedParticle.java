package com.TominoCZ.FBP.particle;

import net.minecraft.client.renderer.BufferBuilder;

public interface IFBPShadedParticle {
	public void renderShadedParticle(BufferBuilder buf, float partialTicks);
}
