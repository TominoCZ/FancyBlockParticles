package com.TominoCZ.FBP.gui;

import java.io.IOException;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiYesNo extends GuiScreen {

	GuiButton yes, no;

	GuiScreen parent;

	public FBPGuiYesNo(GuiScreen s) {
		parent = s;
	}

	public void initGui() {
		this.buttonList.clear();

		yes = new FBPGuiButton(1, this.width / 2 - 75, (int) (this.height / 1.85), "\u00A7aYes", false, false);
		no = new FBPGuiButton(0, this.width / 2 + 26, (int) (this.height / 1.85), "\u00A7cNo", false, false);

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
		this.mc.displayGuiScreen(parent);

		FBPConfigHandler.write();
	}

	public void updateScreen() {

	}

	public boolean doesGuiPauseGame() {
		return false;
	}
	
	@Override
	protected void keyTyped(char c, int keyCode) throws IOException {
		if (keyCode == 1) {
			closeGui();
			return;
		}

		super.keyTyped(c, keyCode);
	}
	
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		parent.width = this.width;
		parent.height = this.height;

		parent.initGui();
		parent.drawScreen(0, 0, partialTicks);

		this.drawDefaultBackground();

		this.drawCenteredString(fontRendererObj, "Are you sure?", this.width / 2, yes.yPosition - 30,
				Integer.parseInt("FFAA00", 16));
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	void closeGui() {
		mc.displayGuiScreen(parent);
	}
}