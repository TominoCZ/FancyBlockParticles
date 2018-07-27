package com.TominoCZ.FBP.gui;

import org.lwjgl.input.Mouse;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;

public class FBPGuiSlider extends GuiButton
{
	public double value;

	double sliderPosX;
	double mouseGap;

	boolean dragging = false;

	boolean mouseDown = false;

	public FBPGuiSlider(int x, int y, double value)
	{
		super(Integer.MIN_VALUE, x, y, "");
		this.value = value;
		this.width = 200;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY)
	{
		FontRenderer fontrenderer = mc.fontRendererObj;

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		int i = enabled ? 1 : 0;
		int j = enabled ? (isMouseOverSlider(mouseX, mouseY) || dragging ? 2 : 1) : 0;

		GlStateManager.enableBlend();

		// text
		this.drawCenteredString(fontrenderer, displayString, this.xPosition + width / 2, this.yPosition + 6 - 9,
				fontrenderer.getColorCode('f'));

		mc.getTextureManager().bindTexture(FBP.FBP_WIDGETS);

		// bar
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 60 + i * 20, this.width / 2, this.height);
		this.drawTexturedModalRect(this.xPosition + this.width / 2, this.yPosition, 200 - this.width / 2, 60 + i * 20,
				this.width / 2, this.height);

		// slider
		boolean tmpMouseDown = Mouse.isButtonDown(0);

		if (!tmpMouseDown && mouseDown && dragging)
		{
			dragging = false;

			FBPConfigHandler.check();
			FBPConfigHandler.write();
		}

		mouseDown = tmpMouseDown;

		sliderPosX = this.xPosition + (15 + (value * (width - 30)));

		if (dragging)
		{
			double max = this.xPosition + width - 15;
			double min = this.xPosition + 15;

			sliderPosX = MathHelper.clamp(mouseX - mouseGap, min, max);

			double val = sliderPosX - min;

			value = MathHelper.clamp(MathHelper.abs((float) (val / (width - 30))), 0, 1);
		}

		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

		this.drawTexturedModalRect((float) sliderPosX - 15, this.yPosition, 0, 100 + j * 20, 15, this.height);
		this.drawTexturedModalRect((float) sliderPosX, this.yPosition, 185, 100 + j * 20, 15, this.height);
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
	{
		if (!enabled)
			return false;

		if (dragging = isMouseOverSlider(mouseX, mouseY))
			mouseGap = mouseX - sliderPosX;
		else
		{
			if (isMouseOverBar(mouseX, mouseY))
			{
				float posX = MathHelper.clamp(mouseX - (this.xPosition + 4), 0, width - 5);

				value = MathHelper.clamp(posX / (width - 10), 0, 1);

				dragging = true;

				mouseGap = 0;
			}
		}

		return false;
	}

	boolean isMouseOverBar(int mouseX, int mouseY)
	{
		int X1 = this.xPosition + 4;
		int X2 = this.xPosition + width - 6;

		int Y1 = this.yPosition + 4;
		int Y2 = this.yPosition + 15;

		boolean inRectangle = mouseX > X1 && mouseX < X2 && mouseY > Y1 && mouseY <= Y2;

		boolean inCircle1 = FBPGuiHelper.isMouseInsideCircle(mouseX, mouseY, X1, Y1 + 5, 5);
		boolean inCircle2 = FBPGuiHelper.isMouseInsideCircle(mouseX, mouseY, X2, Y1 + 5, 5);

		return inRectangle || inCircle1 || inCircle2;
	}

	boolean isMouseOverSlider(int mouseX, int mouseY)
	{
		int X1 = (int) (sliderPosX - 15 + 5);
		int X2 = (int) (sliderPosX + 15 - 5);

		int Y1 = this.yPosition + 4;
		int Y2 = this.yPosition + 15;

		boolean inRectangle = mouseX > X1 && mouseX < X2 && mouseY > Y1 && mouseY <= Y2;

		boolean inCircle1 = FBPGuiHelper.isMouseInsideCircle(mouseX, mouseY, X1, Y1 + 5, 5);
		boolean inCircle2 = FBPGuiHelper.isMouseInsideCircle(mouseX, mouseY, X2, Y1 + 5, 5);

		return inRectangle || inCircle1 || inCircle2;
	}

	public boolean isMouseOver(int mouseX, int mouseY)
	{
		return isMouseOverBar(mouseX, mouseY) || isMouseOverSlider(mouseX, mouseY);
	}
}
