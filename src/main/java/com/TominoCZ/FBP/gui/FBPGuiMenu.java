package com.TominoCZ.FBP.gui;

import java.io.IOException;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.math.FBPMathHelper;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiMenu extends GuiScreen {
	GuiButton Reload, Done, Defaults, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12;

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

		Defaults = new GuiButton(0, this.width / 2 + 4, (int) b12.yPosition + b12.height + 10, "Defaults");
		Done = new GuiButton(-1, this.width / 2 - 100, (int) b12.yPosition + b12.height + 10, "Done");

		Reload = new GuiButton(13, this.width / 2 - 100, (int) Done.yPosition + Done.height + 5, "Reload Config");

		Defaults.setWidth(96);
		Done.setWidth(96);
		Reload.setWidth(96 * 2 + 8);

		b1.setWidth(25);
		b2.setWidth(25);
		b3.setWidth(25);
		b4.setWidth(25);
		b5.setWidth(25);
		b6.setWidth(25);
		b7.setWidth(25);
		b8.setWidth(25);
		b7.setWidth(25);
		b8.setWidth(25);
		b9.setWidth(25);
		b10.setWidth(25);
		b11.setWidth(25);
		b12.setWidth(25);

		this.buttonList.add(b1);
		this.buttonList.add(b2);
		this.buttonList.add(b3);
		this.buttonList.add(b4);
		this.buttonList.add(b5);
		this.buttonList.add(b6);
		this.buttonList.add(b7);
		this.buttonList.add(b8);
		this.buttonList.add(b9);
		this.buttonList.add(b10);
		this.buttonList.add(b11);
		this.buttonList.add(b12);
		this.buttonList.add(Defaults);
		this.buttonList.add(Done);
		this.buttonList.add(Reload);
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case -1:
			this.mc.displayGuiScreen((GuiScreen) null);
			break;
		case 0:
			this.mc.displayGuiScreen(new FBPGuiYesNo());
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
		case 13:
			FBPConfigHandler.init();
		}

		FBPConfigHandler.check();
		FBPConfigHandler.write();
	}

	public boolean doesGuiPauseGame() {
		return false;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		this.drawCenteredString(fontRendererObj, "Fancy Block Particles", this.width / 2, b1.yPosition - 25,
				Integer.parseInt("FFAA00", 16));
		this.drawCenteredString(fontRendererObj, "Min. Scale [" + FBPMathHelper.round(FBP.minScale) + "]", this.width / 2,
				b1.yPosition + 6, fontRendererObj.getColorCode('A'));
		this.drawCenteredString(fontRendererObj, "Max. Scale [" + FBPMathHelper.round(FBP.maxScale) + "]", this.width / 2,
				b3.yPosition + 6, fontRendererObj.getColorCode('A'));
		this.drawCenteredString(fontRendererObj, "Min. Age [" + FBP.minAge + "]", this.width / 2, b5.yPosition + 6,
				fontRendererObj.getColorCode('A'));
		this.drawCenteredString(fontRendererObj, "Max. Age [" + FBP.maxAge + "]", this.width / 2, b7.yPosition + 6,
				fontRendererObj.getColorCode('A'));
		this.drawCenteredString(fontRendererObj, "Gravity Force Mult. [" + FBPMathHelper.round(FBP.gravityMult) + "]",
				this.width / 2, b9.yPosition + 6, fontRendererObj.getColorCode('A'));

		if (FBP.rotationMult != 0)
			this.drawCenteredString(fontRendererObj, "Rotation Speed Mult. [" + FBPMathHelper.round(FBP.rotationMult) + "]",
					this.width / 2, b11.yPosition + 7, fontRendererObj.getColorCode('A'));
		else
			this.drawCenteredString(fontRendererObj, "Rotation Speed Mult. [OFF]", this.width / 2, b11.yPosition + 7,
					fontRendererObj.getColorCode('A'));

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}
