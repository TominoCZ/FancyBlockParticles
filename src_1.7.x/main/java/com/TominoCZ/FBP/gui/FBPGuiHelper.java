package com.TominoCZ.FBP.gui;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;

public class FBPGuiHelper extends GuiScreen {

	public static void background(int top, int bottom, int width, int height) {
		Tessellator tes = Tessellator.instance;

		drawContainerBackground(tes, top, bottom, 0, width);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_FOG);

		GL11.glDisable(GL11.GL_DEPTH_TEST);

		overlayBackground(0, top, 255, 255, top, bottom, 0, width);
		overlayBackground(bottom, height, 255, 255, top, bottom, 0, width);

		GL11.glShadeModel(7425);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDisable(GL11.GL_ALPHA_TEST);

		GL11.glDisable(GL11.GL_TEXTURE_2D);

		tes.startDrawingQuads();
		tes.setColorRGBA(0, 0, 0, 0);
		tes.addVertexWithUV(0, top + 4, 0.0D, 0.0D, 1.0D);
		tes.setColorRGBA(0, 0, 0, 0);
		tes.addVertexWithUV(width, top + 4, 0.0D, 1.0D, 1.0D);
		tes.setColorRGBA(0, 0, 0, 255);
		tes.addVertexWithUV(width, top, 0.0D, 1.0D, 0.0D);
		tes.setColorRGBA(0, 0, 0, 255);
		tes.addVertexWithUV(0, top, 0.0D, 0.0D, 0.0D);
		tes.draw();

		tes.startDrawingQuads();
		tes.setColorRGBA(0, 0, 0, 255);
		tes.addVertexWithUV(0, bottom, 0.0D, 0.0D, 1.0D);
		tes.setColorRGBA(0, 0, 0, 255);
		tes.addVertexWithUV(width, bottom, 0.0D, 1.0D, 1.0D);
		tes.setColorRGBA(0, 0, 0, 0);
		tes.addVertexWithUV(width, bottom - 4, 0.0D, 1.0D, 0.0D);
		tes.setColorRGBA(0, 0, 0, 0);
		tes.addVertexWithUV(0, bottom - 4, 0.0D, 0.0D, 0.0D);
		tes.draw();

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(7424);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	protected static void overlayBackground(int startY, int endY, int startAlpha, int endAlpha, int top, int bottom,
			int left, int right) {
		Tessellator tes = Tessellator.instance;
		Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.optionsBackground);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;

		tes.startDrawingQuads();
		tes.setColorRGBA(64, 64, 64, endAlpha);
		tes.addVertexWithUV(left, endY, 0.0D, 0.0D, endY / 32.0F);
		tes.setColorRGBA(64, 64, 64, endAlpha);
		tes.addVertexWithUV(left + right, endY, 0.0D, right / 32.0F, endY / 32.0F);
		tes.setColorRGBA(64, 64, 64, startAlpha);
		tes.addVertexWithUV(left + right, startY, 0.0D, right / 32.0F, startY / 32.0F);
		tes.setColorRGBA(64, 64, 64, startAlpha);
		tes.addVertexWithUV(left, startY, 0.0D, 0.0D, startY / 32.0F);
		tes.draw();
	}

	public static void drawRect(double x, double y, double x2, double y2, int red, int green, int blue, int alpha) {
		Tessellator tessellator = Tessellator.instance;
		Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.optionsBackground);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA(red, green, blue, alpha);
		tessellator.addVertex((double) x, (double) (y + y2), 0.0D);
		tessellator.addVertex((double) (x + x2), (double) (y + y2), 0.0D);
		tessellator.addVertex((double) (x + x2), (double) y, 0.0D);
		tessellator.addVertex((double) x, (double) y, 0.0D);
		tessellator.draw();

		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void drawTitle(int y, int screenWidth, int screenHeight, FontRenderer fr) {
		if (!FBP.isEnabled())
			_drawCenteredString(fr, "\u00A7c" + "= disabled =", screenWidth / 2, y - 35, 0xC);

		_drawCenteredString(fr, "\u00A76" + "\u00A7L= Fancy Block Particles =", screenWidth / 2, y - 27, 0x6);
		String version = FMLCommonHandler.instance().findContainerFor(FBP.INSTANCE).getVersion();

		_drawCenteredString(fr, "\u00A7a" + "\u00A7L= " + version + " =", screenWidth / 2, y - 17, 0x02);
	}

	public static void _drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		fontRendererIn.drawStringWithShadow(text, x - fontRendererIn.getStringWidth(text) / 2, y, color);
	}

	protected static void drawContainerBackground(Tessellator tessellator, int top, int bottom, int left, int right) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.optionsBackground);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		float f1 = 32.0F;
		tessellator.startDrawingQuads();
		tessellator.setColorOpaque_I(2105376);
		tessellator.addVertexWithUV((double) left, (double) bottom, 0.0D, (double) left / f1, (double) bottom / f1);
		tessellator.addVertexWithUV((double) right, (double) bottom, 0.0D, (double) right / f1, (double) bottom / f1);
		tessellator.addVertexWithUV((double) right, (double) top, 0.0D, (double) right / f1, (double) top / f1);
		tessellator.addVertexWithUV((double) left, (double) top, 0.0D, (double) left / f1, (double) top / f1);
		tessellator.draw();
	}

	public static boolean isMouseInsideCircle(int mouseX, int mouseY, double d, double e, double radius) {
		double X = d - mouseX;
		double Y = e - mouseY;

		return Math.sqrt(X * X + Y * Y) <= radius;
	}

	public static String getToggleString(boolean on, boolean enabled)
	{
		return on ? getONString(enabled) : getOFFString(enabled);
	}

	static String getONString(boolean enabled) {
		return (enabled ? "\u00A7a" : "\u00A72") + " ON";
	}

	static String getOFFString(boolean enabled) {
		return (enabled ? "\u00A7c" : "\u00A74") + "OFF";
	}
}
