package com.TominoCZ.FBP.gui;

import java.awt.Dimension;

import com.TominoCZ.FBP.FBP;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiButtonBugReport extends GuiButton {
	FontRenderer _fr;
	String _textOnHover = "Found a bug? Click to report it!";

	Dimension _screen;

	long lastTime, time;

	int fadeAmmount = 0;

	public FBPGuiButtonBugReport(int buttonID, int xPos, int yPos, Dimension screen, FontRenderer fr) {
		super(buttonID, xPos, yPos, 25, 25, "");
		_screen = screen;
		_fr = fr;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			mc.getTextureManager().bindTexture(FBP.FBP_BUG);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			int centerX = xPosition + 25 / 2;
			int centerY = yPosition + 25 / 2;

			double distance = Math
					.sqrt((mouseX - centerX) * (mouseX - centerX) + (mouseY - centerY) * (mouseY - centerY));
			double radius = Math.sqrt(2 * Math.pow(16, 2));

			boolean flag = distance <= (radius / 2);

			int i = 0;

			if (hovered = flag)
				i += 25;

			int step = 1;
			time = System.currentTimeMillis();

			if (lastTime > 0)
				step = (int) (time - lastTime);

			lastTime = time;

			if (fadeAmmount < step)
				fadeAmmount = step;

			if (fadeAmmount <= 160 && fadeAmmount >= step)
				fadeAmmount += (flag ? step : -step);

			if (fadeAmmount > 150)
				if (flag)
					fadeAmmount = 150;
				else
					fadeAmmount = 0;

			if (fadeAmmount > 0)
				FBPGuiHelper.drawRect(0, 0, _screen.width, _screen.height, 0, 0, 0, fadeAmmount);
			else
				fadeAmmount = 0;

			Gui.drawModalRectWithCustomSizedTexture(this.xPosition, this.yPosition, 0, i, 25, 25, 25, 50);

			if (flag)
				this.drawString(_fr, _textOnHover, mouseX - _fr.getStringWidth(_textOnHover) - 25, mouseY - 3,
						_fr.getColorCode('a'));
		}
	}
}