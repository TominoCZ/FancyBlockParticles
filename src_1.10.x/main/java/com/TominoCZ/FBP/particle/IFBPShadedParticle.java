package com.TominoCZ.FBP.particle;

import net.minecraft.client.renderer.VertexBuffer;

public interface IFBPShadedParticle {
	public void renderShadedParticle(VertexBuffer buf, float partialTicks);
}
