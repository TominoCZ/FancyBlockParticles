package com.TominoCZ.FBP.gui;

import java.awt.Dimension;

import com.TominoCZ.FBP.FBP;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiButtonEnable extends GuiButton {
	FontRenderer _fr;
	Dimension _screen;

	public FBPGuiButtonEnable(int buttonID, int xPos, int yPos, Dimension screen, FontRenderer fr) {
		super(buttonID, xPos, yPos, 25, 25, "");

		_screen = screen;
		_fr = fr;
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			mc.getTextureManager().bindTexture(new ResourceLocation(FBP.MODID + ":textures/gui/fbp.png"));
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			int centerX = x + 25 / 2;
			int centerY = y + 25 / 2;

			double distance = Math
					.sqrt((mouseX - centerX) * (mouseX - centerX) + (mouseY - centerY) * (mouseY - centerY));
			double radius = Math.sqrt(2 * Math.pow(16, 2));

			boolean flag = distance <= (radius / 2);
			int i = FBP.isEnabled() ? 0 : 50;

			if (hovered = enabled = flag)
				i += 25;

			this.drawModalRectWithCustomSizedTexture(this.x, this.y, 0, i, 25, 25, 25, 100);

			String text = (FBP.isEnabled() ? "Disable" : "Enable") + " FBP";

			if (flag)
				this.drawString(_fr, text, mouseX - _fr.getStringWidth(text) - 25, mouseY - 3, _fr.getColorCode('a'));
		}
	}
}