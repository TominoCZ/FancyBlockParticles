package com.TominoCZ.FBP.handler;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.gui.FBPGuiMenuPage1;
import com.TominoCZ.FBP.keys.FBPKeyBindings;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

public class FBPKeyInputHandler {

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if (FBPKeyBindings.FBPMenu.isPressed())
			Minecraft.getMinecraft().displayGuiScreen(new FBPGuiMenuPage1());

		if (FBPKeyBindings.FBPFreeze.isPressed())
			if (FBP.isEnabled())
				FBP.frozen = !FBP.frozen;
			else
				FBP.frozen = false;
	}
}