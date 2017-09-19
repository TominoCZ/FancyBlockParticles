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

	GuiButton Reload, Done, Defaults, Back, ReportBug, Enable, b1, b2;

	String b1Text = "Fancy Flame";
	String b2Text = "Fancy Smoke";

	String description = "";

	double offsetX = 0;

	public void initGui() {
		this.buttonList.clear();

		int x = this.width / 2 - (96 * 2 + 8) / 2;

		b1 = new FBPGuiButton(1, x, (this.height / 5) - 10, b1Text, FBP.fancyFlame, true);
		b2 = new FBPGuiButton(2, x, b1.yPosition + b1.height + 1, b2Text, FBP.fancySmoke, true);
		
		Back = new FBPGuiButton(-3, this.width / 2 - 125 - 19, (int) 6 * b1.height + b1.yPosition - 5 + 10, "<<", false, false);
		Defaults = new FBPGuiButton(0, this.width / 2 + 2, Back.yPosition + Back.height + 24 - 10, "Defaults", false, false);
		Done = new FBPGuiButton(-1, this.width / 2 - 100, (int) Defaults.yPosition, "Done", false, false);
		Reload = new FBPGuiButton(-2, Done.xPosition, (int) Defaults.yPosition + Defaults.height + 1, "Reload Config",
				false, false);
		ReportBug = new FBPGuiButtonBugReport(-4, this.width - 27, 2, new Dimension(width, height),
				this.fontRendererObj);
		Enable = new FBPGuiButtonEnable(-5, ReportBug.xPosition - 25 - 4, 2, new Dimension(width, height),
				this.fontRendererObj);

		Defaults.width = Done.width = 98;
		Reload.width = b1.width = 200;

		Back.width = 20;

		this.buttonList.addAll(java.util.Arrays.asList(new GuiButton[] { b1, b2, Defaults,
				Done, Reload, Back, Enable, ReportBug }));
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case -5:
			FBP.enabled = !FBP.enabled;
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
		}

		FBPConfigHandler.check();
		FBPConfigHandler.write();

		initGui();
	}

	public boolean doesGuiPauseGame() {
		return true;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		FBPGuiHelper.background(b1.yPosition - 6, Done.yPosition - 4, width, height);

		int posY = Done.yPosition - 18;

		getDescription();

		if ((mouseX >= b1.xPosition && mouseX < b1.xPosition + b1.width)
				&& (mouseY >= b1.yPosition && mouseY < b2.yPosition + b1.height)) {

			moveText();

			this.drawCenteredString(fontRendererObj, description, (int) (this.width / 2 + offsetX), posY,
					fontRendererObj.getColorCode('a'));
		}

		FBPGuiHelper.drawTitle(b1.yPosition, width, height, fontRendererObj);

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
				}
			}
		}
	}

	private void moveText() {
		int textWidth = this.fontRendererObj.getStringWidth(description);
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
}
