package com.TominoCZ.FBP.handler;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.gui.FBPGuiNote;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;

public class FBPGuiHandler {
	@SubscribeEvent
	public void onRenderGui(RenderGameOverlayEvent.Post evt) {
		if (evt.type != ElementType.EXPERIENCE)
			return;

		if (FBP.frozen && FBP.isEnabled())
			new FBPGuiNote();
	}
}