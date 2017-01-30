package com.TominoCZ.FBP.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiMenuPage2 extends GuiScreen {

	GuiButton Reload, Done, Defaults, Back, ReportBug, b1, b2, b3;

	String b1Text = "Legacy Mode: ";
	String b2Text = "Spawn Redstone Block Particles: ";
	String b3Text = "Spawn Particles in Freeze Mode: ";

	public void initGui() {
		this.buttonList.clear();

		// b1 = new GuiButton(1, this.width / 2 + 76, (int) (this.height / 5),
		// "+");
		// b2 = new GuiButton(2, this.width / 2 - 100, (int) (this.height / 5),
		// "-");

		b1 = new GuiButton(1, this.width / 2 - (96 * 2 + 8) / 2, (int) (this.height / 5),
				b1Text + (FBP.legacyMode ? "ON" : "OFF"));
		b2 = new GuiButton(2, this.width / 2 - (96 * 2 + 8) / 2, (int) b1.yPosition + b1.height + 1,
				b2Text + (FBP.spawnRedstoneBlockParticles ? "ON" : "OFF"));
		b3 = new GuiButton(3, this.width / 2 - (96 * 2 + 8) / 2, (int) b2.yPosition + b2.height + 1,
				b3Text + (FBP.spawnWhileFrozen ? "ON" : "OFF"));

		ReportBug = new FBPGuiButtonBugReport(-4, this.width - 20, 4, this.fontRendererObj);

		Back = new GuiButton(0, this.width / 2 - 125, (int) 5 * b1.height + b1.yPosition + 15, "<<");

		Defaults = new GuiButton(-1, this.width / 2 + 4, (int) Back.yPosition + 30, "Defaults");
		Done = new GuiButton(-2, this.width / 2 - 100, (int) Defaults.yPosition, "Done");

		Reload = new GuiButton(-3, this.width / 2 - 100, (int) Done.yPosition + Done.height + 5, "Reload Config");

		Defaults.width = Done.width = 96;
		Reload.width = b1.width = b2.width = b3.width = Defaults.width * 2 + 8;

		Back.width = 25;

		this.buttonList.addAll(
				java.util.Arrays.asList(new GuiButton[] { b1, b2, b3, Defaults, Done, Reload, Back, ReportBug }));
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case -4:
			try {
				Desktop.getDesktop().browse(new URI("https://github.com/TominoCZ/FancyBlockParticles/issues"));
			} catch (Exception e) {

			}
			break;
		case -3:
			FBPConfigHandler.init();
			break;
		case -2:
			this.mc.displayGuiScreen((GuiScreen) null);
			break;
		case -1:
			this.mc.displayGuiScreen(new FBPGuiYesNo(this));
			break;
		case 0:
			this.mc.displayGuiScreen(new FBPGuiMenuPage1());
			break;
		case 1:
			b1.displayString = b1Text + ((FBP.legacyMode = !FBP.legacyMode) ? "ON" : "OFF");
			break;
		case 2:
			b2.displayString = b2Text
					+ ((FBP.spawnRedstoneBlockParticles = !FBP.spawnRedstoneBlockParticles) ? "ON" : "OFF");
			break;
		case 3:
			b3.displayString = b3Text + ((FBP.spawnWhileFrozen = !FBP.spawnWhileFrozen) ? "ON" : "OFF");
			break;
		}

		FBPConfigHandler.check();
		FBPConfigHandler.write();
	}

	public boolean doesGuiPauseGame() {
		return true;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawBackground(0);

		FBPGui.background(b1.yPosition - 6, Done.yPosition - 4, width, height);

		this.drawCenteredString(fontRendererObj, "Fancy Block Particles", this.width / 2, b1.yPosition - 25,
				Integer.parseInt("FFAA00", 16));

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
