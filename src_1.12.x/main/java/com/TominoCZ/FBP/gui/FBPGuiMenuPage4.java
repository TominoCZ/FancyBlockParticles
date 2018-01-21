package com.TominoCZ.FBP.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiMenuPage4 extends GuiScreen {

	GuiButton Reload, Done, Defaults, Back, ReportBug, Enable, b1, b2, b3, b4;

	String b1Text = "Fancy Flame";
	String b2Text = "Fancy Smoke";
	String b3Text = "Fancy Rain";
	String b4Text = "Fancy Snow";

	String description = "";

	double offsetX = 0;

	int GUIOffsetY = 4;

	@Override
	public void initGui() {
		this.buttonList.clear();

		int x = this.width / 2 - (96 * 2 + 8) / 2;

		b1 = new FBPGuiButton(1, x, (this.height / 5) - 10 + GUIOffsetY, b1Text, FBP.fancyFlame, true);
		b2 = new FBPGuiButton(2, x, b1.y + b1.height + 1, b2Text, FBP.fancySmoke, true);
		b3 = new FBPGuiButton(3, x, b2.y + b1.height + 6, b3Text, FBP.fancyRain, true);
		b4 = new FBPGuiButton(4, x, b3.y + b1.height + 1, b4Text, FBP.fancySnow, true);

		Back = new FBPGuiButton(-3, this.width / 2 - 125 - 19, (6 * b1.height + b1.y - 5 + 10 - GUIOffsetY), "<<",
				false, false);
		Defaults = new FBPGuiButton(0, this.width / 2 + 2, (6 * b1.height + b1.y - 5) + 24 + 20 - GUIOffsetY,
				"Defaults", false, false);
		Done = new FBPGuiButton(-1, this.width / 2 - 100, Defaults.y, "Done", false, false);
		Reload = new FBPGuiButton(-2, Done.x, Defaults.y + Defaults.height + 1, "Reload Config", false, false);
		ReportBug = new FBPGuiButtonBugReport(-4, this.width - 27, 2, new Dimension(width, height), this.fontRenderer);
		Enable = new FBPGuiButtonEnable(-5, ReportBug.x - 25 - 4, 2, new Dimension(width, height), this.fontRenderer);

		Defaults.width = Done.width = 98;
		Reload.width = b1.width = 200;

		Back.width = 20;

		this.buttonList.addAll(java.util.Arrays
				.asList(new GuiButton[] { b1, b2, b3, b4, Defaults, Done, Reload, Back, Enable, ReportBug }));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case -5:
			FBP.setEnabled(!FBP.enabled);
			break;
		case -4:
			try {
				Desktop.getDesktop().browse(new URI("https://github.com/TominoCZ/FancyBlockParticles/issues"));
			} catch (Exception e) {

			}
			break;
		case -3:
			this.mc.displayGuiScreen(new FBPGuiMenuPage3());
			break;
		case -2:
			FBPConfigHandler.init();
			break;
		case -1:
			this.mc.displayGuiScreen((GuiScreen) null);
			break;
		case 0:
			this.mc.displayGuiScreen(new FBPGuiYesNo(this));
			break;
		case 1:
			FBP.fancyFlame = !FBP.fancyFlame;
			break;
		case 2:
			FBP.fancySmoke = !FBP.fancySmoke;
			break;
		case 3:
			FBP.fancyRain = !FBP.fancyRain;
			break;
		case 4:
			FBP.fancySnow = !FBP.fancySnow;
			break;
		}

		if (FBP.fancyRain || FBP.fancySnow)
			mc.world.provider.setWeatherRenderer(FBP.fancyWeatherRenderer);
		else
			mc.world.provider.setWeatherRenderer(FBP.originalWeatherRenderer);

		FBPConfigHandler.check();
		FBPConfigHandler.write();

		initGui();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		FBPGuiHelper.background(b1.y - 6 - GUIOffsetY, Done.y - 4, width, height);

		int posY = Done.y - 18;

		getDescription();

		if ((mouseX >= b1.x && mouseX < b1.x + b1.width) && (mouseY >= b1.y && mouseY < b4.y + b1.height)) {

			moveText();

			this.drawCenteredString(fontRenderer, description, (int) (this.width / 2 + offsetX), posY,
					fontRenderer.getColorCode('a'));
		}

		FBPGuiHelper.drawTitle(b1.y - GUIOffsetY, width, height, fontRenderer);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void getDescription() {
		for (GuiButton b : this.buttonList) {
			if (b.isMouseOver()) {
				switch (b.id) {
				case 1:
					description = "Makes \u00A76flame particles\u00A7a fancy.";
					break;
				case 2:
					description = "Makes \u00A76smoke particles\u00A7a fancy.";
					break;
				case 3:
					description = "Makes \u00A76rain particles\u00A7a fancy.";
					break;
				case 4:
					description = "Makes \u00A76snow particles\u00A7a fancy.";
					break;
				}
			}
		}

	}

	private void moveText() {
		int textWidth = this.fontRenderer.getStringWidth(description);
		int outsideSizeX = textWidth - this.width;

		if (textWidth > width) {
			double speedOfSliding = 2400;
			long time = System.currentTimeMillis();

			float normalValue = (float) ((time / speedOfSliding) % 2);

			if (normalValue > 1)
				normalValue = 2 - normalValue;

			offsetX = (outsideSizeX * 2) * normalValue - outsideSizeX;
		} else
			offsetX = 0;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			for (int i = 0; i < this.buttonList.size(); ++i) {
				GuiButton guibutton = this.buttonList.get(i);

				if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
					if (!guibutton.isMouseOver())
						return;

					this.actionPerformed(guibutton);
				}
			}
		}
	}
}
