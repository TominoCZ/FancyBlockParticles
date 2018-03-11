package com.TominoCZ.FBP.particle;

import net.minecraft.client.renderer.WorldRenderer;

public interface IFBPShadedParticle {
	void renderShadedParticle(WorldRenderer buf, float partialTicks);
}
