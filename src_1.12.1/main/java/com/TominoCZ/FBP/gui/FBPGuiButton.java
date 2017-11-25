package com.TominoCZ.FBP.gui;

import com.TominoCZ.FBP.FBP;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;

public class FBPGuiButton extends GuiButton {
	boolean toggleButton = false;

	boolean toggle;

	int offsetX;

	public int size;

	public FBPGuiButton(int buttonId, int x, int y, String buttonText, boolean toggle, boolean toggleButton) {
		super(buttonId, x, y, buttonText);

		switch (buttonText) {
		case "+":
			this.displayString = "\u00A7a\u00A7L" + this.displayString;
			offsetX = (this.height - 5) / 2;
			break;
		case "-":
			this.displayString = "\u00A7c\u00A7L" + this.displayString;
			offsetX = (this.height - 5) / 2;
			break;
		case ">>":
			this.displayString = "\u00A76" + this.displayString;
			offsetX = (this.height - 7) / 2;
			break;
		case "<<":
			this.displayString = "\u00A76" + this.displayString;
			offsetX = (this.height - 10) / 2;
			break;
		case "...":
			this.displayString = "\u00A7a\u00A7L...";// "\u00A7a\u2699";
			offsetX = 6;
			break;
		default:
			offsetX = -1;
			break;
		}

		if (this.toggleButton = toggleButton)
			this.toggle = toggle;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			int centerX1 = x + this.height / 2;
			int centerY1 = y + this.height / 2 - 1;

			int centerX2 = x + this.width - this.height / 2;
			int centerY2 = y + this.height / 2;

			double distance1 = Math
					.sqrt((mouseX - centerX1) * (mouseX - centerX1) + (mouseY - centerY1) * (mouseY - centerY1));
			double radius = (this.height - 1) / 2;

			double distance2 = Math
					.sqrt((mouseX - centerX2) * (mouseX - centerX2) + (mouseY - centerY2) * (mouseY - centerY2));

			boolean isOverRectangle = mouseX >= this.x + this.height / 2 - 2 && mouseY >= this.y + 1
					&& mouseX < this.x + this.width - this.height / 2 + 3 && mouseY < this.y + this.height;

			hovered = (distance1 <= radius || distance2 <= radius) || isOverRectangle;

			FontRenderer fontrenderer = mc.fontRenderer;
			mc.getTextureManager().bindTexture(FBP.FBP_WIDGETS);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			int i = this.getHoverState(this.hovered);

			GlStateManager.enableBlend();

			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

			this.drawTexturedModalRect(this.x, this.y, 0, i * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, i * 20, this.width / 2,
					this.height);

			this.mouseDragged(mc, mouseX, mouseY);
			int j = 14737632;

			if (packedFGColour != 0) {
				j = packedFGColour;
			} else if (!this.enabled) {
				j = 10526880;
			} else if (this.hovered) {
				j = 16777120;
			}

			if (!toggleButton) {
				if (offsetX == -1)
					this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2,
							this.y + (this.height - 8) / 2, j);
				else
					this.drawString(fontrenderer, this.displayString, this.x + offsetX, this.y + (this.height - 8) / 2,
							j);
			} else {
				this.drawString(fontrenderer, this.displayString, this.x + 8, this.y + (this.height - 8) / 2, j);

				this.drawString(fontrenderer, toggle ? FBPGuiHelper.on : FBPGuiHelper.off, this.x + this.width - 25,
						this.y + (this.height - 8) / 2, j);
			}
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
				&& mouseY < this.y + this.height) {
			toggle = !toggle;
			return true;
		} else
			return false;
	}
}
