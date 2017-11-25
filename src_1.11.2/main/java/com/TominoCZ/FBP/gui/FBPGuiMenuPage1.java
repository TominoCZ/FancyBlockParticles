package com.TominoCZ.FBP.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import javax.vecmath.Vector2d;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.util.FBPMathUtil;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiMenuPage1 extends GuiScreen {
	GuiButton Reload, Done, Defaults, Back, Next, ReportBug, Enable;
	FBPGuiSlider WeatherParticleDensity;

	Vector2d lastHandle = new Vector2d(0, 0);
	Vector2d lastSize = new Vector2d(0, 0);

	Vector2d handle = new Vector2d(0, 0);
	Vector2d size = new Vector2d(0, 0);

	long time, lastTime;

	int selected = 0;

	double offsetX = 0;

	int GUIOffsetY = 8;

	@Override
	public void initGui() {
		this.buttonList.clear();

		int x1 = this.width / 2 + 80;
		int x2 = this.width / 2 - 100;

		int X = this.width / 2 - 100;

		WeatherParticleDensity = new FBPGuiSlider(X, this.height / 5 - 10 + GUIOffsetY,
				(FBP.weatherParticleDensity - 0.75) / 4.25);

		int Y = WeatherParticleDensity.yPosition + WeatherParticleDensity.height + 2
				+ 4 * (WeatherParticleDensity.height + 1) + 5;

		Defaults = new FBPGuiButton(0, this.width / 2 + 2, Y + 20 + 24 - GUIOffsetY + 4, "Defaults", false, false);
		Done = new FBPGuiButton(-1, x2, Defaults.yPosition, "Done", false, false);
		Reload = new FBPGuiButton(-2, x2, Defaults.yPosition + Defaults.height + 1, "Reload Config", false,
				false);
		ReportBug = new FBPGuiButtonBugReport(-4, this.width - 27, 2, new Dimension(width, height),
				this.fontRendererObj);
		Enable = new FBPGuiButtonEnable(-6, (this.width - 25 - 27) - 4, 2, new Dimension(width, height),
				this.fontRendererObj);
		Defaults.width = Done.width = 98;
		Reload.width = 96 * 2 + 8;
		Back = new FBPGuiButton(-7, X - 44, Y + 10 - GUIOffsetY + 4, "<<", false, false);
		Next = new FBPGuiButton(-3, X + 200 + 25, Y + 10 - GUIOffsetY + 4, ">>", false, false);

		Back.width = Next.width = 20;

		this.buttonList.addAll(Arrays.asList(
				new GuiButton[] { WeatherParticleDensity, Defaults, Done, Reload, Back, Next, Enable, ReportBug }));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		boolean init = true;

		switch (button.id) {
		case -6:
			FBP.setEnabled(!FBP.enabled);
			break;
		case -4:
			try {
				Desktop.getDesktop().browse(new URI("https://github.com/TominoCZ/FancyBlockParticles/issues"));
			} catch (Exception e) {

			}
			break;
		case -7:
			this.mc.displayGuiScreen(new FBPGuiMenuPage0());
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
			init = false;
			break;
		}

		FBPConfigHandler.check();
		FBPConfigHandler.write();

		if (init)
			initGui();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return true;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		FBPGuiHelper.background(WeatherParticleDensity.yPosition - 6 - GUIOffsetY, Done.yPosition - 4, width, height);

		double sWeatherParticleDensity = FBPMathUtil.round(0.75 + 4.25 * WeatherParticleDensity.value, 2);

		FBP.weatherParticleDensity = sWeatherParticleDensity;

		drawMouseOverSelection(mouseX, mouseY, partialTicks);

		FBPGuiHelper.drawTitle(WeatherParticleDensity.yPosition - GUIOffsetY, width, height, fontRendererObj);

		drawInfo();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawMouseOverSelection(int mouseX, int mouseY, float partialTicks) {
		int posY = Done.yPosition - 18;

		if ((mouseX >= WeatherParticleDensity.xPosition - 2
				&& mouseX <= (WeatherParticleDensity.xPosition + WeatherParticleDensity.width + 2))
				&& mouseY >= WeatherParticleDensity.yPosition
				&& mouseY <= (WeatherParticleDensity.yPosition + WeatherParticleDensity.height - 2)) {
			handle.y = WeatherParticleDensity.yPosition;
			size = new Vector2d(WeatherParticleDensity.width, 18);
			selected = 1;
		}

		int step = 1;
		time = System.currentTimeMillis();

		if (lastTime > 0)
			step = (int) (time - lastTime);

		lastTime = time;

		if (lastHandle != new Vector2d(0, 0)) {
			if (lastHandle.y > handle.y) {
				if (lastHandle.y - handle.y <= step)
					lastHandle.y = handle.y;
				else
					lastHandle.y -= step;
			}

			if (lastHandle.y < handle.y) {
				if (handle.y - lastHandle.y <= step)
					lastHandle.y = handle.y;
				else
					lastHandle.y += step;
			}

			lastHandle.x = WeatherParticleDensity.xPosition;
		}

		if (lastSize != new Vector2d(0, 0)) {
			if (lastSize.y > size.y)
				if (lastSize.y - size.y <= step)
					lastSize.y = size.y;
				else
					lastSize.y -= step;

			if (lastSize.y < size.y)
				if (size.y - lastSize.y <= step)
					lastSize.y = size.y;
				else
					lastSize.y += step;

			if (lastSize.x > size.x)
				lastSize.x -= step;
			if (lastSize.x < size.x)
				lastSize.x += step;

			lastSize.x = WeatherParticleDensity.width;
		}

		String text = "";

		switch (selected) {
		case 1:
			text = "Sets the \u00A76weather particle density \u00A7ato \u00A76"
					+ (int) (FBP.weatherParticleDensity * 100) + "%\u00A7a.";
			break;
		default:
			text = "";
		}

		if (((mouseX >= WeatherParticleDensity.xPosition - 2
				&& mouseX <= WeatherParticleDensity.xPosition + WeatherParticleDensity.width + 2)
				&& (mouseY < WeatherParticleDensity.yPosition + WeatherParticleDensity.height
						&& mouseY >= WeatherParticleDensity.yPosition)
				&& (lastSize.y <= 20 || (lastSize.y < 50 && lastSize.y > 20))
				&& lastHandle.y >= WeatherParticleDensity.yPosition)) {
			moveText(text);

			if (selected <= 5)
				FBPGuiHelper.drawRect(lastHandle.x - 2, lastHandle.y + 2, lastSize.x + 4, lastSize.y - 2, 200, 200, 200,
						35);

			this.drawCenteredString(fontRendererObj, text, (int) (this.width / 2 + offsetX), posY,
					fontRendererObj.getColorCode('a'));
		}
	}

	private void drawInfo() {
		int posY = Done.yPosition - 18;

		String s = "Weather Particle Density [\u00A76" + (int) (FBP.weatherParticleDensity * 100) + "%\u00A7f]";
		WeatherParticleDensity.displayString = s;
	}

	private void moveText(String text) {
		int textWidth = this.fontRendererObj.getStringWidth(text);
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

	@Override
	public void onGuiClosed() {
		FBPConfigHandler.check();
		FBPConfigHandler.write();
	}
}
