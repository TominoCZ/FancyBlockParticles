package com.TominoCZ.FBP.gui;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class FBPGuiButtonException extends FBPGuiButton {

	public boolean particle;
	public boolean isInExceptions;

	public FBPGuiButtonException(int buttonId, int x, int y, String buttonText, boolean particle,
			boolean isInExceptions) {
		super(buttonId, x, y, buttonText, false, false);

		this.particle = particle;
		this.isInExceptions = isInExceptions;

		this.width = 60;
		this.height = 60;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			int centerX1 = xPosition + this.height / 2;
			int centerY1 = yPosition + this.height / 2 - 1;

			double distance = Math
					.sqrt((mouseX - centerX1) * (mouseX - centerX1) + (mouseY - centerY1) * (mouseY - centerY1));
			double radius = (this.height - 1) / 2;

			hovered = distance <= radius;

			mc.getTextureManager().bindTexture(FBP.FBP_WIDGETS);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			GlStateManager.enableBlend();

			GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE,
					GL11.GL_ZERO);
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

			this.drawTexturedModalRect(xPosition, yPosition, enabled ? (isInExceptions ? 60 : 0) : 120, 196, 60, 60);

			if (!enabled)
				GlStateManager.color(0.25f, 0.25f, 0.25f);
			// render icon
			this.drawTexturedModalRect(xPosition + width / 2.0f - 22.5f + (particle ? 0 : 2),
					yPosition + height / 2.0f - 22.5f, 256 - 45, particle ? 45 : 0, 45, 45);

			this.mouseDragged(mc, mouseX, mouseY);
		}
	}
}
