package com.TominoCZ.FBP.gui;

import org.fusesource.jansi.Ansi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiButtonBugReport extends GuiButton {
	FontRenderer _fr;
	String _textOnHover = "Found a bug? Click to report it!";

	public FBPGuiButtonBugReport(int buttonID, int xPos, int yPos, FontRenderer fr) {
		super(buttonID, xPos, yPos, 16, 16, "");
		_fr = fr;
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			mc.getTextureManager().bindTexture(new ResourceLocation("fbp:textures/gui/bug.png"));
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			boolean flag = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width
					&& mouseY < this.yPosition + this.height;
			int i = 0;

			if (flag)
				i += 15;

			this.drawModalRectWithCustomSizedTexture(this.xPosition, this.yPosition, 0, i, 15, 15, 15, 30);

			if (flag)
				this.drawString(_fr, _textOnHover, mouseX - _fr.getStringWidth(_textOnHover) - 15, mouseY - 3,
						_fr.getColorCode('a'));
		}
	}
}