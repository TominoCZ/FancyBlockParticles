package com.TominoCZ.FBP.gui;

import org.lwjgl.opengl.GL11;

import com.TominoCZ.FBP.FBP;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class FBPGuiHelper extends GuiScreen {
	public static final String on = "\u00A7a ON";
	public static final String off = "\u00A7cOFF";

	public static void background(int top, int bottom, int width, int height) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer BufferBuilder = tessellator.getBuffer();

		drawContainerBackground(tessellator, top, bottom, 0, width);

		GlStateManager.disableDepth();
		overlayBackground(0, top, 255, 255, top, bottom, 0, width);
		overlayBackground(bottom, height, 255, 255, top, bottom, 0, width);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO,
				GlStateManager.DestFactor.ONE);
		GlStateManager.disableAlpha();
		GlStateManager.shadeModel(7425);
		GlStateManager.disableTexture2D();
		int i1 = 4;
		BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		BufferBuilder.pos(0, top + 4, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
		BufferBuilder.pos(width, top + 4, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
		BufferBuilder.pos(width, top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
		BufferBuilder.pos(0, top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
		tessellator.draw();
		BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		BufferBuilder.pos(0, bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
		BufferBuilder.pos(width, bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
		BufferBuilder.pos(width, bottom - 4, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
		BufferBuilder.pos(0, bottom - 4, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
		tessellator.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(7424);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
	}

	public static void drawRect(double x, double y, double x2, double y2, int red, int green, int blue, int alpha) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer BufferBuilder = tessellator.getBuffer();

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();

		BufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		BufferBuilder.pos(x, y + y2, 0.0D).color(red, green, blue, alpha).endVertex();
		BufferBuilder.pos(x + x2, y + y2, 0.0D).color(red, green, blue, alpha).endVertex();
		BufferBuilder.pos(x + x2, y, 0.0D).color(red, green, blue, alpha).endVertex();
		BufferBuilder.pos(x, y, 0.0D).color(red, green, blue, alpha).endVertex();
		tessellator.draw();

		GlStateManager.enableTexture2D();
	}

	public static void drawTitle(int y, int screenWidth, int screenHeight, FontRenderer fr) {
		if (!FBP.isEnabled())
			_drawCenteredString(fr, "= disabled =", screenWidth / 2, y - 35, fr.getColorCode('c'));

		_drawCenteredString(fr, "= \u00A7LFancy Block Particles =", screenWidth / 2, y - 27, fr.getColorCode('6'));
		String version = FMLCommonHandler.instance().findContainerFor(FBP.INSTANCE).getVersion();

		_drawCenteredString(fr, "= \u00A7L" + version + " =", screenWidth / 2, y - 17, fr.getColorCode('a'));
	}

	protected static void _drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		fontRendererIn.drawStringWithShadow(text, x - fontRendererIn.getStringWidth(text) / 2, y, color);
	}

	protected static void overlayBackground(int startY, int endY, int startAlpha, int endAlpha, int top, int bottom,
			int left, int right) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer BufferBuilder = tessellator.getBuffer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;

		BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		BufferBuilder.pos(left, endY, 0.0D).tex(0.0D, endY / 32.0F).color(64, 64, 64, endAlpha).endVertex();
		BufferBuilder.pos(left + right, endY, 0.0D).tex(right / 32.0F, endY / 32.0F).color(64, 64, 64, endAlpha)
				.endVertex();
		BufferBuilder.pos(left + right, startY, 0.0D).tex(right / 32.0F, startY / 32.0F).color(64, 64, 64, startAlpha)
				.endVertex();
		BufferBuilder.pos(left, startY, 0.0D).tex(0.0D, startY / 32.0F).color(64, 64, 64, startAlpha).endVertex();
		tessellator.draw();
	}

	protected static void drawContainerBackground(Tessellator tessellator, int top, int bottom, int left, int right) {
		VertexBuffer buffer = tessellator.getBuffer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		float f = 32.0F;
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		buffer.pos(left, bottom, 0.0D).tex(left / f, bottom / f).color(32, 32, 32, 255).endVertex();
		buffer.pos(right, bottom, 0.0D).tex(right / f, bottom / f).color(32, 32, 32, 255).endVertex();
		buffer.pos(right, top, 0.0D).tex(right / f, top / f).color(32, 32, 32, 255).endVertex();
		buffer.pos(left, top, 0.0D).tex(left / f, top / f).color(32, 32, 32, 255).endVertex();

		tessellator.draw();
	}

	public static boolean isMouseInsideCircle(int mouseX, int mouseY, double d, double e, double radius) {
		double X = d - mouseX;
		double Y = e - mouseY;

		return Math.sqrt(X * X + Y * Y) <= radius;
	}
}
