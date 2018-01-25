package com.TominoCZ.FBP.gui;

import java.awt.Dimension;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

@SideOnly(Side.CLIENT)
public class FBPGuiButtonEnable extends GuiButton {
	FontRenderer _fr;
	Dimension _screen;

	public FBPGuiButtonEnable(int buttonID, int xPos, int yPos, Dimension screen, FontRenderer fr) {
		super(buttonID, xPos, yPos, 25, 25, "");

		_screen = screen;
		_fr = fr;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			mc.getTextureManager().bindTexture(FBP.FBP_FBP);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

			int centerX = xPosition + 25 / 2;
			int centerY = yPosition + 25 / 2;

			double distance = Math
					.sqrt((mouseX - centerX) * (mouseX - centerX) + (mouseY - centerY) * (mouseY - centerY));
			double radius = Math.sqrt(2 * Math.pow(16, 2));

			boolean flag = distance <= (radius / 2);
			int i = FBP.isEnabled() ? 0 : 50;

			if (field_146123_n = enabled = flag)
				i += 25;

			Gui.func_146110_a(this.xPosition, this.yPosition, 0, i, 25, 25, 25, 100);

			String text = (FBP.isEnabled() ? "Disable" : "Enable") + " FBP";

			if (flag)
				this.drawString(_fr, "\u00A7a" + text, mouseX - _fr.getStringWidth(text) - 25, mouseY - 3, 0);
		}
	}
}