package com.TominoCZ.FBP.gui;

import java.io.IOException;

import com.TominoCZ.FBP.handler.FBPConfigHandler;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiYesNo extends GuiScreen {

	GuiButton yes, no;

	GuiScreen backTo;

	public FBPGuiYesNo(GuiScreen s) {
		backTo = s;
	}

	public void initGui() {
		this.buttonList.clear();

		yes = new GuiButton(1, this.width / 2 - 75, (int) (this.height / 1.85), "Yes");
		no = new GuiButton(0, this.width / 2 + 26, (int) (this.height / 1.85), "No");

		yes.setWidth(50);
		no.setWidth(50);

		this.buttonList.add(yes);
		this.buttonList.add(no);
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 1:
			FBPConfigHandler.defaults(true);
			break;
		}
		this.mc.displayGuiScreen(backTo);

		FBPConfigHandler.write();
	}

	public void updateScreen() {

	}

	public boolean doesGuiPauseGame() {
		return false;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawBackground(0);

		this.drawCenteredString(fontRendererObj, "Are you sure?", this.width / 2, yes.yPosition - 30,
				Integer.parseInt("FFAA00", 16));
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
}