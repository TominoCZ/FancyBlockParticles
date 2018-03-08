package com.TominoCZ.FBP.gui;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.particle.FBPParticleDigging;
import com.TominoCZ.FBP.particle.FBPParticleManager;
import com.TominoCZ.FBP.renderer.FBPWeatherRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class FBPGuiButtonEnable extends GuiButton {
	FontRenderer _fr;
	Dimension _screen;
	Minecraft mc;
	
	boolean lastEnabled;
	
	public FBPGuiButtonEnable(int buttonID, int xPos, int yPos, Dimension screen, FontRenderer fr) {
		super(buttonID, xPos, yPos, 25, 25, "");

		_screen = screen;
		_fr = fr;
		
		mc = Minecraft.getMinecraft();
		
		lastEnabled = FBP.enabled;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible) {
			mc.getTextureManager().bindTexture(FBP.FBP_FBP);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			int centerX = xPosition + 25 / 2;
			int centerY = yPosition + 25 / 2;

			double distance = Math
					.sqrt((mouseX - centerX) * (mouseX - centerX) + (mouseY - centerY) * (mouseY - centerY));
			double radius = Math.sqrt(2 * Math.pow(16, 2));

			boolean flag = distance <= (radius / 2);
			int i = FBP.isEnabled() ? 0 : 50;

			if (hovered = flag)
				i += 25;

			Gui.drawModalRectWithCustomSizedTexture(this.xPosition, this.yPosition, 0, i, 25, 25, 25, 100);

			String text = (FBP.isEnabled() ? "Disable" : "Enable") + " FBP";

			if (flag)
				this.drawString(_fr, text, mouseX - _fr.getStringWidth(text) - 25, mouseY - 3, _fr.getColorCode('a'));
			
			if (lastEnabled != FBP.enabled) {
				enabledChanged();
				
				lastEnabled = enabled;
			}
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible && hovered) {
			playPressSound(mc.getSoundHandler());
			return true;
		} else
			return false;
	}

	private void enabledChanged(){
		FBP.fancyEffectRenderer = new FBPParticleManager(mc.theWorld, mc.renderEngine, new FBPParticleDigging.Factory());
        FBP.fancyWeatherRenderer = new FBPWeatherRenderer();

        IRenderHandler currentWeatherRenderer = mc.theWorld.provider.getCloudRenderer();

        if (FBP.originalWeatherRenderer == null || (FBP.originalWeatherRenderer != currentWeatherRenderer
                && currentWeatherRenderer != FBP.fancyWeatherRenderer))
            FBP.originalWeatherRenderer = currentWeatherRenderer;
        if (FBP.originalEffectRenderer == null || (FBP.originalEffectRenderer != mc.effectRenderer
                && FBP.originalEffectRenderer != FBP.fancyEffectRenderer))
            FBP.originalEffectRenderer = mc.effectRenderer;

        if (FBP.enabled) {
            mc.effectRenderer = FBP.fancyEffectRenderer;

            if (FBP.fancyRain || FBP.fancySnow)
                mc.theWorld.provider.setWeatherRenderer(FBP.fancyWeatherRenderer);
        }
    }
}