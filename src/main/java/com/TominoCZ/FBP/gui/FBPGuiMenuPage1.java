package com.TominoCZ.FBP.gui;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import javax.vecmath.Vector2d;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.math.FBPMathHelper;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FBPGuiMenuPage1 extends GuiScreen {
	GuiButton Reload, Done, Defaults, Next, ReportBug, Enable, b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12;

	boolean mouseOver = false;

	Vector2d lastHandle = new Vector2d(0, 0);
	Vector2d lastSize = new Vector2d(0, 0);

	Vector2d handle = new Vector2d(0, 0);
	Vector2d size = new Vector2d(0, 0);

	long time, lastTime;

	int selected = 0;

	double offsetX = 0;

	public void initGui() {
		this.buttonList.clear();

		b1 = new GuiButton(1, this.width / 2 + 78, (int) (this.height / 5) - 10, "+");
		b2 = new GuiButton(2, this.width / 2 - 100, (int) b1.yPosition, "-");

		b3 = new GuiButton(3, b1.xPosition, (int) b2.yPosition + b2.height + 1, "+");
		b4 = new GuiButton(4, this.width / 2 - 100, (int) b2.yPosition + b2.height + 1, "-");

		b5 = new GuiButton(5, b3.xPosition, (int) b3.yPosition + b3.height + 6, "+");
		b6 = new GuiButton(6, this.width / 2 - 100, (int) b3.yPosition + b3.height + 6, "-");

		b7 = new GuiButton(7, b5.xPosition, (int) b5.yPosition + b5.height + 1, "+");
		b8 = new GuiButton(8, this.width / 2 - 100, (int) b5.yPosition + b5.height + 1, "-");

		b9 = new GuiButton(9, b7.xPosition, (int) b8.yPosition + b8.height + 6, "+");
		b10 = new GuiButton(10, this.width / 2 - 100, (int) b8.yPosition + b8.height + 6, "-");

		b11 = new GuiButton(11, b9.xPosition, (int) b10.yPosition + b10.height + 1, "+");
		b12 = new GuiButton(12, this.width / 2 - 100, (int) b10.yPosition + b10.height + 1, "-");

		Defaults = new GuiButton(0, this.width / 2 + 2, b12.yPosition + b12.height + 24, "Defaults");
		Done = new GuiButton(-1, this.width / 2 - 100, (int) Defaults.yPosition, "Done");
		Reload = new GuiButton(-2, this.width / 2 - 100, (int) Defaults.yPosition + Defaults.height + 1,
				"Reload Config");
		ReportBug = new FBPGuiButtonBugReport(-4, this.width - 27, 2, new Dimension(width, height),
				this.fontRendererObj);
		Enable = new FBPGuiButtonEnable(-6, (this.width - 25 - 27) - 4, 2, new Dimension(width, height),
				this.fontRendererObj);
		Defaults.width = Done.width = 98;
		Reload.width = 96 * 2 + 8;
		Next = new GuiButton(-3, b12.xPosition + b12.width + 3, (int) b12.yPosition, ">>");

		b1.width = b2.width = b3.width = b4.width = b5.width = b6.width = b7.width = b8.width = b9.width = b10.width = b11.width = b12.width = Next.width = 22;

		this.buttonList.addAll(Arrays.asList(new GuiButton[] { b1, b2, b3, b4, b5, b6, b7, b8, b9, b10, b11, b12,
				Defaults, Done, Reload, Next, Enable, ReportBug }));
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case -6:
			FBP.enabled = !FBP.enabled;
			break;
		case -5:
			FBP.showInMillis = !FBP.showInMillis;
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
			if (FBP.minScale == FBP.maxScale)
				FBP.maxScale = FBPMathHelper.round(FBP.maxScale += 0.1D);

			FBP.minScale = FBPMathHelper.round(FBP.minScale += 0.1D);
			break;
		case 2:
			FBP.minScale = FBPMathHelper.round(FBP.minScale -= 0.1D);
			break;
		case 3:
			FBP.maxScale = FBPMathHelper.round(FBP.maxScale += 0.1D);
			break;
		case 4:
			if (FBP.minScale == FBP.maxScale)
				FBP.minScale = FBPMathHelper.round(FBP.minScale -= 0.1D);

			FBP.maxScale = FBPMathHelper.round(FBP.maxScale -= 0.1D);
			break;
		case 5:
			if (FBP.minAge == FBP.maxAge)
				FBP.maxAge += 5;

			FBP.minAge += 5;
			break;
		case 6:
			FBP.minAge -= 5;
			break;
		case 7:
			FBP.maxAge += 5;
			break;
		case 8:
			if (FBP.minAge == FBP.maxAge)
				FBP.minAge -= 5;
			FBP.maxAge -= 5;
			break;
		case 9:
			FBP.gravityMult = FBPMathHelper.round(FBP.gravityMult += 0.1D);
			break;
		case 10:
			FBP.gravityMult = FBPMathHelper.round(FBP.gravityMult -= 0.1D);
			break;
		case 11:
			FBP.rotationMult = FBPMathHelper.round(FBP.rotationMult += 0.1D);
			break;
		case 12:
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

		FBPGuiHelper.background(b1.yPosition - 6, Done.yPosition - 4, width, height);

		drawInfo();

		drawMouseOverSelection(mouseX, mouseY, partialTicks);

		FBPGuiHelper.drawTitle(b1.yPosition, width, height, fontRendererObj);

		update();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawMouseOverSelection(int mouseX, int mouseY, float partialTicks) {
		mouseOver = false;

		int posY = Done.yPosition - 18;

		if (((mouseX >= b2.xPosition + b2.width) && (mouseX <= b1.xPosition)) && (mouseY >= b2.yPosition)
				&& (mouseY <= b4.yPosition + b4.height - 2)) {
			handle.x = b2.xPosition + b2.width;
			handle.y = b1.yPosition;
			size.x = b1.xPosition - (b2.xPosition + b2.width);
			size.y = 39;

			selected = 1;
		} else if (((mouseX >= b6.xPosition + b6.width) && (mouseX <= b7.xPosition)) && (mouseY >= (b5.yPosition + 1))
				&& (mouseY <= (b7.yPosition + b7.height - 1) - 1)) {
			mouseOver = true;
			handle = new Vector2d(b6.xPosition + b6.width, b5.yPosition);
			size = new Vector2d(b7.xPosition - (b6.xPosition + b6.width), 39);
			selected = 2;
		} else if (((mouseX >= b10.xPosition + b10.width) && (mouseX <= b9.xPosition)) && (mouseY >= b10.yPosition + 1)
				&& (mouseY <= b9.yPosition + b9.height - 1)) {
			handle = new Vector2d(b10.xPosition + b10.width, b10.yPosition);
			size = new Vector2d(b9.xPosition - (b10.xPosition + b10.width), 18);
			selected = 3;
		} else if (((mouseX >= b12.xPosition + b12.width) && (mouseX <= b11.xPosition)) && (mouseY >= b12.yPosition + 1)
				&& (mouseY <= b11.yPosition + b11.height - 1)) {
			handle = new Vector2d(b12.xPosition + b12.width, b12.yPosition);
			size = new Vector2d(b11.xPosition - (b12.xPosition + b12.width), 18);
			selected = 4;
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

			lastHandle.x = b2.xPosition + b1.width;
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

			lastSize.x = b9.xPosition - (b10.xPosition + b10.width);
		}

		String text = "";

		switch (selected) {
		case 1:
			text = "Sets the \u00A76particle scale " + (FBP.minScale != FBP.maxScale
					? ("range\u00A7a to between \u00A76" + FBP.minScale + "\u00A7a and \u00A76" + FBP.maxScale)
					: "\u00A7ato\u00A76 " + FBP.maxScale) + "\u00A7a.";
			break;
		case 2:
			String _text = (FBP.minAge != FBP.maxAge
					? ("range\u00A7a to between \u00A76" + (FBP.showInMillis ? FBP.minAge * 50 : FBP.minAge)
							+ "\u00A7a and \u00A76" + (FBP.showInMillis ? FBP.maxAge * 50 : FBP.maxAge)
							+ (FBP.showInMillis ? "ms" : (FBP.maxAge > 1 ? " ticks" : " tick")))
					: ("\u00A7ato \u00A76" + (FBP.showInMillis ? FBP.maxAge * 50 : FBP.maxAge)
							+ (FBP.showInMillis ? "ms" : (FBP.maxAge > 1 ? " ticks" : " tick"))));

			text = "Sets the \u00A76particle life duration " + _text + "\u00A7a.";
			break;
		case 3:
			text = "Multiplies the \u00A76default particle gravity\u00A7a force by \u00A76" + FBP.gravityMult
					+ "\u00A7a.";
			break;
		case 4:
			text = "Multiplies \u00A76particle rotation\u00A7a by \u00A76" + FBP.rotationMult + "\u00A7a.";
			break;
		default:
			text = "";
		}

		if ((mouseX >= b2.xPosition + b2.width && mouseX <= b1.xPosition)
				&& (mouseY < b12.yPosition + b12.height && mouseY >= b2.yPosition)
				&& (lastSize.y <= 20 || (lastSize.y < 50 && lastSize.y > 20)) && lastHandle.y >= b1.yPosition) {
			moveText(text);

			if (selected == 2)
				this.drawCenteredString(fontRendererObj, !FBP.showInMillis ? "show in ms" : "show in ticks",
						this.width / 2, b6.yPosition + b6.width - 5, fontRendererObj.getColorCode('c'));

			FBPGuiHelper.drawRect(lastHandle.x, lastHandle.y + 1, lastSize.x, lastSize.y, 200, 200, 200, 35);

			this.drawCenteredString(fontRendererObj, text, (int) (this.width / 2 + offsetX), posY,
					fontRendererObj.getColorCode('a'));
		}
	}

	private void drawInfo() {
		int posY = Done.yPosition - 18;

		this.drawCenteredString(fontRendererObj, "Min. Scale [\u00A76" + FBPMathHelper.round(FBP.minScale) + "\u00A7f]",
				this.width / 2, b1.yPosition + 6, fontRendererObj.getColorCode('f'));
		this.drawCenteredString(fontRendererObj, "Max. Scale [\u00A76" + FBPMathHelper.round(FBP.maxScale) + "\u00A7f]",
				this.width / 2, b3.yPosition + 6, fontRendererObj.getColorCode('f'));

		this.drawCenteredString(fontRendererObj,
				"Min. Duration [\u00A76" + (FBP.showInMillis ? ((FBP.minAge * 50) + "ms")
						: (FBP.minAge + (FBP.minAge > 1 ? " ticks" : " tick"))) + "\u00A7f]",
				this.width / 2, b5.yPosition + 6, fontRendererObj.getColorCode('f'));

		this.drawCenteredString(fontRendererObj,
				"Max. Duration [\u00A76" + (FBP.showInMillis ? ((FBP.maxAge * 50) + "ms")
						: (FBP.maxAge + (FBP.maxAge > 1 ? " ticks" : " tick"))) + "\u00A7f]",
				this.width / 2, b7.yPosition + 6, fontRendererObj.getColorCode('f'));

		this.drawCenteredString(fontRendererObj,
				"Gravity Force Mult. [\u00A76" + FBPMathHelper.round(FBP.gravityMult) + "\u00A7f]", this.width / 2,
				b9.yPosition + 6, fontRendererObj.getColorCode('f'));
		this.drawCenteredString(fontRendererObj,
				"Rotation Speed Mult. [\u00A76" + (FBP.rotationMult != 0
						? String.valueOf(FBPMathHelper.round(FBP.rotationMult)) : FBPGuiHelper.off) + "\u00A7f]",
				this.width / 2, b11.yPosition + 6, fontRendererObj.getColorCode('f'));
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

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			for (int i = 0; i < this.buttonList.size(); ++i) {
				GuiButton guibutton = (GuiButton) this.buttonList.get(i);

				if (guibutton.mousePressed(this.mc, mouseX, mouseY)) {
					net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre event = new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Pre(
							this, guibutton, this.buttonList);
					if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event))
						break;
					guibutton = event.getButton();
					guibutton.playPressSound(this.mc.getSoundHandler());
					this.actionPerformed(guibutton);
					if (this.equals(this.mc.currentScreen))
						net.minecraftforge.common.MinecraftForge.EVENT_BUS
								.post(new net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent.Post(this,
										event.getButton(), this.buttonList));
				}
			}
		}

		if (mouseOver)
			this.actionPerformed(new GuiButton(-5, 0, 0, 0, 0, ""));
	}

	void update() {
		b1.enabled = FBP.minScale < 2.0D;
		b2.enabled = FBP.minScale > 0.5D;

		b3.enabled = FBP.maxScale < 2.0D;
		b4.enabled = FBP.maxScale > 0.5D;

		b5.enabled = FBP.minAge < 100;
		b6.enabled = FBP.minAge > 10;

		b7.enabled = FBP.maxAge < 100;
		b8.enabled = FBP.maxAge > 10;

		b9.enabled = FBP.gravityMult < 2.0D;
		b10.enabled = FBP.gravityMult > 0.1D;
		b11.enabled = FBP.rotationMult < 1.5D;
		b12.enabled = FBP.rotationMult > 0;
	}
}
