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
public class FBPGuiMenuPage0 extends GuiScreen
{
	GuiButton Reload, Done, Defaults, Next, ReportBug, Enable, InfiniteDuration, TimeUnit;

	FBPGuiSlider MinDurationSlider, MaxDurationSlider, ParticleCountBase, ScaleMultSlider, GravitiyForceSlider,
			RotSpeedSlider;

	Vector2d lastHandle = new Vector2d(0, 0);
	Vector2d lastSize = new Vector2d(0, 0);

	Vector2d handle = new Vector2d(0, 0);
	Vector2d size = new Vector2d(0, 0);

	long time, lastTime;

	int selected = 0;

	double offsetX = 0;

	boolean canChangeTimeUnit = true;

	int GUIOffsetY = 8;

	@Override
	public void initGui()
	{
		this.buttonList.clear();

		int x1 = this.width / 2 + 80;
		int x2 = this.width / 2 - 100;

		int X = this.width / 2 - 100;

		MinDurationSlider = new FBPGuiSlider(X, this.height / 5 - 10 + GUIOffsetY, (FBP.minAge - 10) / 90.0);
		MaxDurationSlider = new FBPGuiSlider(X, MinDurationSlider.y + MinDurationSlider.height + 1,
				(FBP.maxAge - 10) / 90.0);

		ParticleCountBase = new FBPGuiSlider(X, MaxDurationSlider.y + 6 + MaxDurationSlider.height,
				(FBP.particlesPerAxis - 2) / 3.0);
		ScaleMultSlider = new FBPGuiSlider(X, ParticleCountBase.y + ParticleCountBase.height + 1,
				(FBP.scaleMult - 0.75) / 0.5);
		GravitiyForceSlider = new FBPGuiSlider(X, ScaleMultSlider.y + ScaleMultSlider.height + 6,
				(FBP.gravityMult - 0.05) / 2.95);
		RotSpeedSlider = new FBPGuiSlider(X, GravitiyForceSlider.y + GravitiyForceSlider.height + 1,
				FBP.rotationMult / 1.5);
		InfiniteDuration = new FBPGuiButton(11, x1 + 25, MinDurationSlider.y + 10,
				(FBP.infiniteDuration ? "\u00A7a" : "\u00A7c") + "\u221e", false, false);

		TimeUnit = new FBPGuiButton(12, x2 - 25, MinDurationSlider.y + 10,
				"\u00A7a\u00A7L" + (FBP.showInMillis ? "ms" : "ti"), false, false);

		Defaults = new FBPGuiButton(0, this.width / 2 + 2, RotSpeedSlider.y + RotSpeedSlider.height + 24 - GUIOffsetY,
				"Defaults", false, false);
		Done = new FBPGuiButton(-1, x2, Defaults.y, "Done", false, false);
		Reload = new FBPGuiButton(-2, x2, Defaults.y + Defaults.height + 1, "Reload Config", false, false);
		ReportBug = new FBPGuiButtonBugReport(-4, this.width - 27, 2, new Dimension(width, height), this.fontRenderer);
		Enable = new FBPGuiButtonEnable(-6, (this.width - 25 - 27) - 4, 2, new Dimension(width, height),
				this.fontRenderer);
		Defaults.width = Done.width = 98;
		Reload.width = 96 * 2 + 8;

		Next = new FBPGuiButton(-3, RotSpeedSlider.x + RotSpeedSlider.width + 25, RotSpeedSlider.y + 10 - GUIOffsetY,
				">>", false, false);

		InfiniteDuration.width = TimeUnit.width = Next.width = 20;

		this.buttonList.addAll(Arrays.asList(new GuiButton[] { MinDurationSlider, MaxDurationSlider, ParticleCountBase,
				ScaleMultSlider, GravitiyForceSlider, RotSpeedSlider, InfiniteDuration, TimeUnit, Defaults, Done,
				Reload, Next, Enable, ReportBug }));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException
	{
		boolean init = true;

		switch (button.id)
		{
		case -6:
			FBP.setEnabled(!FBP.enabled);
			break;
		case -5:
			if (!canChangeTimeUnit)
				return;

			FBP.showInMillis = !FBP.showInMillis;
			init = false;
			break;
		case -4:
			try
			{
				Desktop.getDesktop().browse(new URI("https://github.com/TominoCZ/FancyBlockParticles/issues"));
			} catch (Exception e)
			{

			}
			break;
		case -3:
			this.mc.displayGuiScreen(new FBPGuiMenuPage1());
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
		case 11:
			InfiniteDuration.displayString = ((FBP.infiniteDuration = !FBP.infiniteDuration) ? "\u00A7a" : "\u00A7c")
					+ "\u221e";
			init = false;
			break;
		case 12:
			TimeUnit.displayString = "\u00A7a\u00A7L" + ((FBP.showInMillis = !FBP.showInMillis) ? "ms" : "ti");
			init = false;
			break;
		}

		FBPConfigHandler.check();
		FBPConfigHandler.write();

		if (init)
			initGui();
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return true;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		FBPGuiHelper.background(MinDurationSlider.y - 6 - GUIOffsetY, Done.y - 4, width, height);

		int sParticleCountBase = (int) Math.round(2 + 3 * ParticleCountBase.value);

		int sMinAge = (int) (10 + 90 * MinDurationSlider.value);
		int sMaxAge = (int) (10 + 90 * MaxDurationSlider.value);

		double sScaleMult = FBPMathUtil.round(0.75 + 0.5 * ScaleMultSlider.value, 2);
		double sGravityForce = FBPMathUtil.round(0.05 + 2.95 * GravitiyForceSlider.value, 2);
		double sRotSpeed = FBPMathUtil.round(1.5 * RotSpeedSlider.value, 2);

		if (FBP.maxAge < sMinAge)
		{
			FBP.maxAge = sMinAge;

			MaxDurationSlider.value = (FBP.maxAge - 10) / 90.0;
		}

		if (FBP.minAge > sMaxAge)
		{
			FBP.minAge = sMaxAge;

			MinDurationSlider.value = (FBP.minAge - 10) / 90.0;
		}

		FBP.minAge = sMinAge;
		FBP.maxAge = sMaxAge;

		FBP.scaleMult = sScaleMult;
		FBP.gravityMult = sGravityForce;
		FBP.rotationMult = sRotSpeed;
		FBP.particlesPerAxis = sParticleCountBase;

		ParticleCountBase.value = (FBP.particlesPerAxis - 2) / 3.0;

		canChangeTimeUnit = !isMouseOverSliders(mouseX, mouseY);

		drawMouseOverSelection(mouseX, mouseY, partialTicks);

		FBPGuiHelper.drawTitle(MinDurationSlider.y - GUIOffsetY, width, height, fontRenderer);

		update();
		drawInfo();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawMouseOverSelection(int mouseX, int mouseY, float partialTicks)
	{
		int posY = Done.y - 18;

		if ((mouseX >= MinDurationSlider.x - 2 && mouseX <= (MinDurationSlider.x + MinDurationSlider.width + 2))
				&& mouseY >= MinDurationSlider.y && mouseY <= (MaxDurationSlider.y + MaxDurationSlider.height - 2))
		{
			handle.y = MinDurationSlider.y;
			size = new Vector2d(MinDurationSlider.width, 39);
			selected = 1;
		} else if (((mouseX >= ParticleCountBase.x) && (mouseX <= ParticleCountBase.x + ParticleCountBase.width))
				&& (mouseY >= (ParticleCountBase.y + 1))
				&& (mouseY <= (ParticleCountBase.y + ParticleCountBase.height - 1) - 1))
		{
			handle.y = ParticleCountBase.y;
			size = new Vector2d(ParticleCountBase.width, 18);
			selected = 2;
		} else if (((mouseX >= ScaleMultSlider.x) && (mouseX <= ScaleMultSlider.x + ScaleMultSlider.width))
				&& (mouseY >= (ScaleMultSlider.y + 1))
				&& (mouseY <= (ScaleMultSlider.y + ScaleMultSlider.height - 1) - 1))
		{
			handle.y = ScaleMultSlider.y;
			size = new Vector2d(ScaleMultSlider.width, 18);
			selected = 3;
		} else if (((mouseX >= GravitiyForceSlider.x) && (mouseX <= GravitiyForceSlider.x + GravitiyForceSlider.width))
				&& (mouseY >= GravitiyForceSlider.y + 1)
				&& (mouseY <= GravitiyForceSlider.y + GravitiyForceSlider.height - 1))
		{
			handle.y = GravitiyForceSlider.y;
			size = new Vector2d(GravitiyForceSlider.width, 18);
			selected = 4;
		} else if (((mouseX >= RotSpeedSlider.x) && (mouseX <= RotSpeedSlider.x + RotSpeedSlider.width))
				&& (mouseY >= RotSpeedSlider.y + 1) && (mouseY <= RotSpeedSlider.y + RotSpeedSlider.height - 1))
		{
			handle.y = RotSpeedSlider.y;
			size = new Vector2d(RotSpeedSlider.x - (RotSpeedSlider.x + RotSpeedSlider.width), 18);
			selected = 5;
		} else if (InfiniteDuration.isMouseOver())
			selected = 6;
		else if (TimeUnit.isMouseOver())
			selected = 7;

		int step = 1;
		time = System.currentTimeMillis();

		if (lastTime > 0)
			step = (int) (time - lastTime);

		lastTime = time;

		if (lastHandle != new Vector2d(0, 0))
		{
			if (lastHandle.y > handle.y)
			{
				if (lastHandle.y - handle.y <= step)
					lastHandle.y = handle.y;
				else
					lastHandle.y -= step;
			}

			if (lastHandle.y < handle.y)
			{
				if (handle.y - lastHandle.y <= step)
					lastHandle.y = handle.y;
				else
					lastHandle.y += step;
			}

			lastHandle.x = MinDurationSlider.x;
		}

		if (lastSize != new Vector2d(0, 0))
		{
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

			lastSize.x = GravitiyForceSlider.width;
		}

		String text = "";

		switch (selected)
		{
		case 1:
			if (!FBP.infiniteDuration)
			{
				String _text = (FBP.minAge != FBP.maxAge
						? ("range\u00A7a to between \u00A76" + (FBP.showInMillis ? FBP.minAge * 50 : FBP.minAge)
								+ "\u00A7a and \u00A76" + (FBP.showInMillis ? FBP.maxAge * 50 : FBP.maxAge)
								+ (FBP.showInMillis ? "ms" : (FBP.maxAge > 1 ? " ticks" : " tick")))
						: ("\u00A7ato \u00A76" + (FBP.showInMillis ? FBP.maxAge * 50 : FBP.maxAge)
								+ (FBP.showInMillis ? "ms" : (FBP.maxAge > 1 ? " ticks" : " tick"))));

				text = "Sets \u00A76particle life duration " + _text + "\u00A7a.";
			} else
			{
				text = "Sets \u00A76particle life duration \u00A7ato \u00A76infinity\u00A7a.";
			}
			break;
		case 2:
			text = "Sets the \u00A76block destroy particle count\u00A7a to \u00A76"
					+ (int) Math.pow(FBP.particlesPerAxis, 3) + " \u00A7c[\u00A76" + FBP.particlesPerAxis
					+ "^3\u00A7c]\u00A7a.";
			break;
		case 3:
			text = "Sets \u00A76particle scale multiplier \u00A7ato \u00A76" + FBP.scaleMult + "\u00A7a.";
			break;
		case 4:
			text = "Multiplies \u00A76default particle gravity force\u00A7a by \u00A76" + FBP.gravityMult + "\u00A7a.";
			break;
		case 5:
			text = "Multiplies \u00A76particle rotation\u00A7a by \u00A76" + FBP.rotationMult + "\u00A7a.";
			break;
		case 6:
			text = (FBP.infiniteDuration ? "\u00A7cDisable" : "Enable")
					+ " \u00A76infinite particle life duration\u00A7a.";
			break;
		case 7:
			text = "Show the time in \u00A76" + (!FBP.showInMillis ? "milliseconds" : "ticks") + "\u00A7a.";
			break;
		default:
			text = "";
		}

		if (((mouseX >= MinDurationSlider.x - 2 && mouseX <= MinDurationSlider.x + MinDurationSlider.width + 2)
				&& (mouseY < RotSpeedSlider.y + RotSpeedSlider.height && mouseY >= MinDurationSlider.y)
				&& (lastSize.y <= 20 || (lastSize.y < 50 && lastSize.y > 20)) && lastHandle.y >= MinDurationSlider.y)
				|| InfiniteDuration.isMouseOver() || TimeUnit.isMouseOver())
		{
			moveText(text);

			if (selected <= 5)
				FBPGuiHelper.drawRect(lastHandle.x - 2, lastHandle.y + 2, lastSize.x + 4, lastSize.y - 2, 200, 200, 200,
						35);

			this.drawCenteredString(fontRenderer, text, (int) (this.width / 2 + offsetX), posY,
					fontRenderer.getColorCode('a'));
		}
	}

	private void drawInfo()
	{
		int posY = Done.y - 18;

		String s = "Destroy Particle Count [\u00A76" + (int) Math.pow(FBP.particlesPerAxis, 3) + "\u00A7f]";
		ParticleCountBase.displayString = s;

		if (FBP.infiniteDuration)
			s = "Min. Duration" + " [\u00A76" + "\u221e" + (FBP.showInMillis ? " ms" : " ticks") + "\u00A7f]";
		else
			s = "Min. Duration [\u00A76" + (FBP.showInMillis ? ((FBP.minAge * 50) + "ms")
					: (FBP.minAge + (FBP.minAge > 1 ? " ticks" : " tick"))) + "\u00A7f]";

		MinDurationSlider.displayString = s;

		if (FBP.infiniteDuration)
			s = "Max. Duration" + " [\u00A76" + "\u221e" + (FBP.showInMillis ? " ms" : " ticks") + "\u00A7f]";
		else
			s = "Max. Duration [\u00A76" + (FBP.showInMillis ? ((FBP.maxAge * 50) + "ms")
					: (FBP.maxAge + (FBP.maxAge > 1 ? " ticks" : " tick"))) + "\u00A7f]";

		MaxDurationSlider.displayString = s;

		ScaleMultSlider.displayString = "Scale Mult. [\u00A76" + FBP.scaleMult + "\u00A7f]";

		GravitiyForceSlider.displayString = "Gravity Force Mult. [\u00A76" + FBP.gravityMult + "\u00A7f]";

		RotSpeedSlider.displayString = "Rotation Speed Mult. [\u00A76"
				+ (FBP.rotationMult != 0 ? FBP.rotationMult : FBPGuiHelper.off) + "\u00A7f]";
	}

	boolean isMouseOverSliders(int mouseX, int mouseY)
	{
		return MinDurationSlider.isMouseOver(mouseX, mouseY) || MaxDurationSlider.isMouseOver(mouseX, mouseY);
	}

	private void moveText(String text)
	{
		int textWidth = this.fontRenderer.getStringWidth(text);
		int outsideSizeX = textWidth - this.width;

		if (textWidth > width)
		{
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
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
	{
		if (mouseButton == 0)
		{
			for (int i = 0; i < this.buttonList.size(); ++i)
			{
				GuiButton guibutton = this.buttonList.get(i);

				if (guibutton.mousePressed(this.mc, mouseX, mouseY))
				{
					if (!guibutton.isMouseOver())
						return;

					this.actionPerformed(guibutton);
				}
			}
		}
	}

	void update()
	{
		MinDurationSlider.enabled = !FBP.infiniteDuration;

		MaxDurationSlider.enabled = !FBP.infiniteDuration;
	}

	@Override
	public void onGuiClosed()
	{
		FBPConfigHandler.check();
		FBPConfigHandler.write();
	}
}
