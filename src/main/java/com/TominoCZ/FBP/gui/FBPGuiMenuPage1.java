package com.TominoCZ.FBP.gui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.math.FBPMathHelper;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiMenuPage1 extends GuiScreen {
	GuiButton Reload, Done, Defaults, Next, ReportBug, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12;

	public void initGui() {
		this.buttonList.clear();

		b1 = new GuiButton(1, this.width / 2 + 76, (int) (this.height / 5), "+");
		b2 = new GuiButton(2, this.width / 2 - 100, (int) (this.height / 5), "-");

		b3 = new GuiButton(3, this.width / 2 + 76, (int) b1.yPosition + b1.height + 1, "+");
		b4 = new GuiButton(4, this.width / 2 - 100, (int) b1.yPosition + b1.height + 1, "-");

		b5 = new GuiButton(5, this.width / 2 + 76, (int) b3.yPosition + b3.height + 6, "+");
		b6 = new GuiButton(6, this.width / 2 - 100, (int) b3.yPosition + b3.height + 6, "-");

		b7 = new GuiButton(7, this.width / 2 + 76, (int) b5.yPosition + b5.height + 1, "+");
		b8 = new GuiButton(8, this.width / 2 - 100, (int) b5.yPosition + b5.height + 1, "-");

		b9 = new GuiButton(9, this.width / 2 + 76, (int) b8.yPosition + b8.height + 6, "+");
		b10 = new GuiButton(10, this.width / 2 - 100, (int) b8.yPosition + b8.height + 6, "-");

		b11 = new GuiButton(11, this.width / 2 + 76, (int) b10.yPosition + b10.height + 1, "+");
		b12 = new GuiButton(12, this.width / 2 - 100, (int) b10.yPosition + b10.height + 1, "-");

		ReportBug = new FBPGuiButtonBugReport(-4, this.width - 20, 4, this.fontRendererObj);

		Next = new GuiButton(-3, this.width / 2 + 101, (int) b12.yPosition, ">>");
		Defaults = new GuiButton(0, this.width / 2 + 4, (int) b12.yPosition + b12.height + 10, "Defaults");
		Done = new GuiButton(-1, this.width / 2 - 100, (int) b12.yPosition + b12.height + 10, "Done");

		Reload = new GuiButton(-2, this.width / 2 - 100, (int) Done.yPosition + Done.height + 5, "Reload Config");

		Defaults.width = Done.width = 96;
		Reload.width = Defaults.width * 2 + 8;

		b1.width = b2.width = b3.width = b4.width = b5.width = b6.width = b7.width = b8.width = b9.width = b10.width = b11.width = b12.width = Next.width = 25;

		this.buttonList.addAll(Arrays.asList(new GuiButton[] { b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12,
				Defaults, Done, Reload, Next, ReportBug }));
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
			this.mc.displayGuiScreen(new FBPGuiMenuPage2());
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
			if (FBP.minScale < FBP.maxScale)
				FBP.minScale = FBPMathHelper.round(FBP.minScale += 0.1D);
			break;
		case 2:
			if (FBP.minScale > 0.1D)
				FBP.minScale = FBPMathHelper.round(FBP.minScale -= 0.1D);
			break;
		case 3:
			if (FBP.maxScale < 2.0D)
				FBP.maxScale = FBPMathHelper.round(FBP.maxScale += 0.1D);
			break;
		case 4:
			if (FBP.maxScale > FBP.minScale)
				FBP.maxScale = FBPMathHelper.round(FBP.maxScale -= 0.1D);
			break;
		case 5:
			if (FBP.minAge < FBP.maxAge)
				FBP.minAge += 1;
			break;
		case 6:
			if (FBP.minAge > 1)
				FBP.minAge -= 1;
			break;
		case 7:
			if (FBP.maxAge < 50)
				FBP.maxAge += 1;
			break;
		case 8:
			if (FBP.maxAge > FBP.minAge)
				FBP.maxAge -= 1;
			break;
		case 9:
			if (FBP.gravityMult < 2.0D)
				FBP.gravityMult = FBPMathHelper.round(FBP.gravityMult += 0.1D);
			break;
		case 10:
			if (FBP.gravityMult > 0.1D)
				FBP.gravityMult = FBPMathHelper.round(FBP.gravityMult -= 0.1D);
			break;
		case 11:
			if (FBP.rotationMult < 1.5D)
				FBP.rotationMult = FBPMathHelper.round(FBP.rotationMult += 0.1D);
			break;
		case 12:
			if (FBP.rotationMult > 0)
				FBP.rotationMult = FBPMathHelper.round(FBP.rotationMult -= 0.1D);
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

		this.drawCenteredString(fontRendererObj, "Min. Scale [" + FBPMathHelper.round(FBP.minScale) + "]",
				this.width / 2, b1.yPosition + 6, fontRendererObj.getColorCode('A'));
		this.drawCenteredString(fontRendererObj, "Max. Scale [" + FBPMathHelper.round(FBP.maxScale) + "]",
				this.width / 2, b3.yPosition + 6, fontRendererObj.getColorCode('A'));
		this.drawCenteredString(fontRendererObj, "Min. Age [" + FBP.minAge + "]", this.width / 2, b5.yPosition + 6,
				fontRendererObj.getColorCode('A'));
		this.drawCenteredString(fontRendererObj, "Max. Age [" + FBP.maxAge + "]", this.width / 2, b7.yPosition + 6,
				fontRendererObj.getColorCode('A'));
		this.drawCenteredString(fontRendererObj, "Gravity Force Mult. [" + FBPMathHelper.round(FBP.gravityMult) + "]",
				this.width / 2, b9.yPosition + 6, fontRendererObj.getColorCode('A'));

		if (FBP.rotationMult != 0)
			this.drawCenteredString(fontRendererObj,
					"Rotation Speed Mult. [" + FBPMathHelper.round(FBP.rotationMult) + "]", this.width / 2,
					b11.yPosition + 7, fontRendererObj.getColorCode('A'));
		else
			this.drawCenteredString(fontRendererObj, "Rotation Speed Mult. [OFF]", this.width / 2, b11.yPosition + 7,
					fontRendererObj.getColorCode('A'));

		update();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	void update() {
		b1.enabled = FBP.minScale < FBP.maxScale;
		b2.enabled = FBP.minScale > 0.1D;
		b3.enabled = FBP.maxScale < 2.0D;
		b4.enabled = FBP.maxScale > FBP.minScale;
		b5.enabled = FBP.minAge < FBP.maxAge;
		b6.enabled = FBP.minAge > 1;
		b7.enabled = FBP.maxAge < 50;
		b8.enabled = FBP.maxAge > FBP.minAge;
		b9.enabled = FBP.gravityMult < 2.0D;
		b10.enabled = FBP.gravityMult > 0.1D;
		b11.enabled = FBP.rotationMult < 1.5D;
		b12.enabled = FBP.rotationMult > 0;
	}
}
