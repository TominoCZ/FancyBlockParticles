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

	GuiButton Reload, Done, Defaults, Back, Next, ReportBug, Enable, b1, b2, b3, b4, b5, b6, b5_settings;

	String b1Text = "Collide With Entities";
	String b2Text = "Bounce Off Walls";
	String b3Text = "Roll Particles";
	String b4Text = "Smart Breaking";
	String b5Text = "Fancy Place Animation";
	String b6Text = "Fancy Weather";

	String description = "";

	double offsetX = 0;

	public void initGui() {
		this.buttonList.clear();

		int x = this.width / 2 - (96 * 2 + 8) / 2;

		b1 = new FBPGuiButton(1, x, (this.height / 5) - 10, b1Text, FBP.entityCollision, true);
		b2 = new FBPGuiButton(2, x, b1.yPosition + b1.height + 1, b2Text, FBP.bounceOffWalls, true);
		b3 = new FBPGuiButton(3, x, b2.yPosition + b1.height + 6, b3Text, FBP.rollParticles, true);
		b4 = new FBPGuiButton(4, x, b3.yPosition + b1.height + 1, b4Text, FBP.smartBreaking, true);
		b5 = new FBPGuiButton(5, x, b4.yPosition + b1.height + 6, b5Text, FBP.fancyPlaceAnim, true);
		b6 = new FBPGuiButton(6, x, b5.yPosition + b1.height + 1, b6Text, FBP.fancyWeather, true);

		b5_settings = new FBPGuiButton(7, x + b1.width + 5, b5.yPosition, "cogwheel", false, false);

		Back = new FBPGuiButton(-3, b6.xPosition - (20 + 3 + 2 + 19), (int) 6 * b1.height + b1.yPosition - 5 + 10, "<<", false, false);
		Next = new FBPGuiButton(-6, b6.xPosition + b6.width + 3 + 2 + 20, (int) b6.yPosition + 10, ">>", false, false);
		
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

		Back.width = Next.width = b5_settings.width = 20;

		this.buttonList.addAll(java.util.Arrays.asList(new GuiButton[] { b1, b2, b3, b4, b5, b6, b5_settings, Defaults,
				Done, Reload, Back, Next, Enable, ReportBug }));
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case -6:
			this.mc.displayGuiScreen(new FBPGuiMenuPage4());
			break;
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
			FBP.rollParticles = !FBP.rollParticles;
			break;
		case 4:
			FBP.smartBreaking = !FBP.smartBreaking;
			break;
		case 5:
			FBP.fancyPlaceAnim = !FBP.fancyPlaceAnim;
			break;
		case 6:
			FBP.fancyWeather = !FBP.fancyWeather;
			break;
		case 7:
			mc.displayGuiScreen(new FBPGuiExceptionList(this));
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
				&& (mouseY >= b1.yPosition && mouseY < b6.yPosition + b1.height) || b5_settings.isMouseOver()) {

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
					description = "Enables \u00A76entity collisions \u00A7awith the particles.";
					break;
				case 2:
					description = "Makes the particles \u00A76ricochet/bounce\u00A7a off walls.";
					break;
				case 3:
					description = "Makes the particles \u00A76keep rolling\u00A7a on the ground \u00A76even when pushed\u00A7a by entities.";
					break;
				case 4:
					description = "Smart particle \u00A76motion\u00A7a and \u00A76scaling\u00A7a.";
					break;
				case 5:
					description = "Adds a \u00A76fancy block placing\u00A7a animation \u00A76[\u00A7cALPHA\u00A76]\u00A7a.";
					break;
				case 6:
					description = "Makes \u00A76weather particles\u00A7a fancy.";
					break;
				case 7:
					description = "Add \u00A76block exceptions \u00A7afor the \u00A76placement animation";
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
