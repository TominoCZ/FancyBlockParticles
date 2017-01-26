/*package com.TominoCZ.FBP.gui;

import java.io.IOException;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.math.FBPMathHelper;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiMenuPage2 extends GuiScreen {
	
	GuiButton Reload, Done, Defaults, Back, b1, b2;
	
	public void initGui() {
		this.buttonList.clear();

		b1 = new GuiButton(1, this.width / 2 + 76, (int) (this.height / 5), "+");
		b2 = new GuiButton(2, this.width / 2 - 100, (int) (this.height / 5), "-");
		
		Back = new GuiButton(3, this.width / 2 - 125, (int) 5 * b1.height + b1.yPosition + 15, "<<"); //TODO 4x yPosition...

		Defaults = new GuiButton(0, this.width / 2 + 4, (int) Back.yPosition + 30 "Defaults");
		Done = new GuiButton(-1, this.width / 2 - 100, (int) Defaults.yPosition, "Done");

		Reload = new GuiButton(-2, this.width / 2 - 100, (int) Done.yPosition + Done.height + 5, "Reload Config");

		Back.setWidth(25);
		Defaults.setWidth(96);
		Done.setWidth(96);
		Reload.setWidth(96 * 2 + 8);

		b1.setWidth(25);
		b2.setWidth(25);
		
		this.buttonList.add(b1);
		this.buttonList.add(b2);
		
		this.buttonList.add(Back);
		this.buttonList.add(Defaults);
		this.buttonList.add(Done);
		this.buttonList.add(Reload);
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case -2:
			FBPConfigHandler.init();
			break;
		case -1:
			this.mc.displayGuiScreen((GuiScreen) null);
			break;
		case 0:
			this.mc.displayGuiScreen(new FBPGuiYesNo());
			break;
		case 1:
			if (FBP.XYZSpeedMult < 2)
				FBP.XYZSpeedMult = FBPMathHelper.round(FBP.XYZSpeedMult += 0.1D);
			break;
		case 2:
			if (FBP.XYZSpeedMult > 0.1)
				FBP.XYZSpeedMult = FBPMathHelper.round(FBP.XYZSpeedMult -= 0.1D);
			break;
		case 3:
			this.mc.displayGuiScreen(new FBPGuiMenuPage1());
			break;
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

		this.drawCenteredString(fontRendererObj, "Speed Mult. [" + FBPMathHelper.round(FBP.XYZSpeedMult) + "]",
				this.width / 2, b1.yPosition + 6, fontRendererObj.getColorCode('A'));

		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}*/
