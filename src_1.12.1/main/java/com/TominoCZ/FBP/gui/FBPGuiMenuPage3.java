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
public class FBPGuiMenuPage3 extends GuiScreen {

	GuiButton Reload, Done, Defaults, Back, Next, ReportBug, Enable, b1, b2, b3, b4, b5, b6;// , b5_settings;

	String b1Text = "Collide With Entities";
	String b2Text = "Bounce Off Walls";
	String b3Text = "Low Traction";
	String b4Text = "Smart Breaking";
	String b5Text = "Fancy Place Animation";
	String b6Text = "Spawn Place Particles";

	String description = "";

	double offsetX = 0;

	int GUIOffsetY = 4;

	@Override
	public void initGui() {
		this.buttonList.clear();

		int x = this.width / 2 - (96 * 2 + 8) / 2;

		b1 = new FBPGuiButton(1, x, (this.height / 5) - 10 + GUIOffsetY, b1Text, FBP.entityCollision, true);
		b2 = new FBPGuiButton(2, x, b1.y + b1.height + 1, b2Text, FBP.bounceOffWalls, true);
		b3 = new FBPGuiButton(3, x, b2.y + b1.height + 6, b3Text, FBP.lowTraction, true);
		b4 = new FBPGuiButton(4, x, b3.y + b1.height + 1, b4Text, FBP.smartBreaking, true);
		b5 = new FBPGuiButton(5, x, b4.y + b1.height + 6, b5Text, FBP.fancyPlaceAnim, true);
		b6 = new FBPGuiButton(6, x, b5.y + b1.height + 1, b6Text, FBP.spawnPlaceParticles, true);

		// b5_settings = new FBPGuiButton(7, x + b1.width + 5, b5.y, "...", false,
		// false);

		Back = new FBPGuiButton(-3, b6.x - 44, 6 * b1.height + b1.y - 5 + 10 - GUIOffsetY, "<<", false, false);
		Next = new FBPGuiButton(-6, b6.x + b6.width + 25, b6.y + 10 - GUIOffsetY, ">>", false, false);

		Defaults = new FBPGuiButton(0, this.width / 2 + 2, b6.y + Back.height + 24 - GUIOffsetY, "Defaults", false,
				false);
		Done = new FBPGuiButton(-1, this.width / 2 - 100, Defaults.y, "Done", false, false);
		Reload = new FBPGuiButton(-2, Done.x, Defaults.y + Defaults.height + 1, "Reload Config", false, false);
		ReportBug = new FBPGuiButtonBugReport(-4, this.width - 27, 2, new Dimension(width, height), this.fontRenderer);
		Enable = new FBPGuiButtonEnable(-5, ReportBug.x - 25 - 4, 2, new Dimension(width, height), this.fontRenderer);

		Defaults.width = Done.width = 98;
		Reload.width = b1.width = 200;

		Back.width = Next.width = 20;

		this.buttonList.addAll(java.util.Arrays.asList(
				new GuiButton[] { b1, b2, b3, b4, b5, b6, Defaults, Done, Reload, Back, Next, Enable, ReportBug }));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case -6:
			this.mc.displayGuiScreen(new FBPGuiMenuPage4());
			break;
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
			FBP.entityCollision = !FBP.entityCollision;
			break;
		case 2:
			FBP.bounceOffWalls = !FBP.bounceOffWalls;
			break;
		case 3:
			FBP.lowTraction = !FBP.lowTraction;
			break;
		case 4:
			FBP.smartBreaking = !FBP.smartBreaking;
			break;
		case 5:
			FBP.fancyPlaceAnim = !FBP.fancyPlaceAnim;
			break;
		case 6:
			FBP.spawnPlaceParticles = !FBP.spawnPlaceParticles;
			break;
		}

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

		if ((mouseX >= b1.x && mouseX < b1.x + b1.width) && (mouseY >= b1.y && mouseY < b6.y + b1.height)) {
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
					description = "Enables \u00A76entity collisions \u00A7awith the particles.";
					break;
				case 2:
					description = "Makes the particles \u00A76ricochet/bounce\u00A7a off walls.";
					break;
				case 3:
					description = "Lowers the \u00A76traction deceleration\u00A7a on the ground.";
					break;
				case 4:
					description = "Smart particle \u00A76motion\u00A7a and \u00A76scaling\u00A7a.";
					break;
				case 5:
					description = "Adds a \u00A76fancy block placing\u00A7a animation \u00A76[\u00A7cALPHA\u00A76]\u00A7a.";
					break;
				case 6:
					description = "Enables\u00A76 block place particles\u00A7a.";
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
