package com.TominoCZ.FBP.handler;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.gui.FBPGuiNote;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FBPGuiHandler {
	@SubscribeEvent
	public void onRenderGui(RenderGameOverlayEvent.Post evt) {
		if (evt.type != ElementType.EXPERIENCE)
			return;

		if (FBP.frozen && FBP.isEnabled())
			new FBPGuiNote();
	}
}