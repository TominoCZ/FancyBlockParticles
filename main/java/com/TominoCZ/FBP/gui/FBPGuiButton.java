package com.TominoCZ.FBP.gui;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

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
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			int centerX1 = xPosition + this.height / 2;
			int centerY1 = yPosition + this.height / 2 - 1;

			int centerX2 = xPosition + this.width - this.height / 2;
			int centerY2 = yPosition + this.height / 2;

			double distance1 = Math
					.sqrt((mouseX - centerX1) * (mouseX - centerX1) + (mouseY - centerY1) * (mouseY - centerY1));
			double radius = (this.height - 1) / 2;

			double distance2 = Math
					.sqrt((mouseX - centerX2) * (mouseX - centerX2) + (mouseY - centerY2) * (mouseY - centerY2));

			boolean isOverRectangle = mouseX >= this.xPosition + this.height / 2 - 2 && mouseY >= this.yPosition + 1
					&& mouseX < this.xPosition + this.width - this.height / 2 + 3
					&& mouseY < this.yPosition + this.height;

			hovered = (distance1 <= radius || distance2 <= radius) || isOverRectangle;

			FontRenderer fontrenderer = mc.fontRendererObj;
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			int i = this.getHoverState(this.hovered);

			GL11.glEnable(GL11.GL_BLEND);

			mc.getTextureManager().bindTexture(FBP.FBP_WIDGETS);

			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, i * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, i * 20,
					this.width / 2, this.height);

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
					this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + this.width / 2,
							this.yPosition + (this.height - 8) / 2, j);
				else
					this.drawString(fontrenderer, this.displayString, this.xPosition + offsetX,
							this.yPosition + (this.height - 8) / 2, j);
			} else {
				this.drawString(fontrenderer, this.displayString, this.xPosition + 8,
						this.yPosition + (this.height - 8) / 2, j);

				this.drawString(fontrenderer, FBPGuiHelper.getToggleString(toggle, enabled),
						this.xPosition + this.width - 25, this.yPosition + (this.height - 8) / 2, j);
			}
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (this.enabled && this.visible && hovered) {
			playPressSound(mc.getSoundHandler());
			toggle = !toggle;
			return true;
		} else
			return false;
	}
}
