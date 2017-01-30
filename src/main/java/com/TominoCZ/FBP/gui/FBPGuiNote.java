package com.TominoCZ.FBP.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class FBPGuiNote extends Gui {
	Minecraft mc;

	public FBPGuiNote() {
		this.mc = Minecraft.getMinecraft();

		ScaledResolution scaledResolutionIn = new ScaledResolution(mc);
		int width = scaledResolutionIn.getScaledWidth();

		drawCenteredString(mc.fontRendererObj, "Freeze Effect ON", width / 2, 5, Integer.parseInt("FFAA00", 16));
	}
}