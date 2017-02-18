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
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		
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
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		vertexbuffer.pos(0, (double) (top + 4), 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 0).endVertex();
		vertexbuffer.pos((double) width, (double) (top + 4), 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 0).endVertex();
		vertexbuffer.pos((double) width, (double) top, 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 255).endVertex();
		vertexbuffer.pos(0, (double) top, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 255).endVertex();
		tessellator.draw();
		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		vertexbuffer.pos(0, (double) bottom, 0.0D).tex(0.0D, 1.0D).color(0, 0, 0, 255).endVertex();
		vertexbuffer.pos((double) width, (double) bottom, 0.0D).tex(1.0D, 1.0D).color(0, 0, 0, 255).endVertex();
		vertexbuffer.pos((double) width, (double) (bottom - 4), 0.0D).tex(1.0D, 0.0D).color(0, 0, 0, 0).endVertex();
		vertexbuffer.pos(0, (double) (bottom - 4), 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, 0).endVertex();
		tessellator.draw();

		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(7424);
		GlStateManager.enableAlpha();
		GlStateManager.disableBlend();
	}

	public static void drawRect(double x, double y, double x2, double y2, int red, int green, int blue, int alpha) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();

		vertexbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		vertexbuffer.pos((double) x, (double) (y + y2), 0.0D).color(red, green, blue, alpha).endVertex();
		vertexbuffer.pos((double) (x + x2), (double) (y + y2), 0.0D).color(red, green, blue, alpha).endVertex();
		vertexbuffer.pos((double) (x + x2), (double) y, 0.0D).color(red, green, blue, alpha).endVertex();
		vertexbuffer.pos((double) x, (double) y, 0.0D).color(red, green, blue, alpha).endVertex();
		tessellator.draw();

		GlStateManager.enableTexture2D();
	}

	public static void drawTitle(int y, int screenWidth, int screenHeight, FontRenderer fr) {
		if (!FBP.isEnabled())
			_drawCenteredString(fr, "\u00A7a(\u00A7cdisabled\u00A7a)", screenWidth / 2, y - 32, fr.getColorCode('c'));

		_drawCenteredString(fr, "\u00A7LFancy Block Particles", screenWidth / 2, y - 23, fr.getColorCode('6'));
		String version = FMLCommonHandler.instance().findContainerFor(FBP.instance).getVersion();

		fr.drawStringWithShadow("\u00A76Version \u00A7L" + version, 2, screenHeight - 10, fr.getColorCode('6'));
	}

	protected static void _drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		fontRendererIn.drawStringWithShadow(text, (float) (x - fontRendererIn.getStringWidth(text) / 2), (float) y,
				color);
	}

	protected static void overlayBackground(int startY, int endY, int startAlpha, int endAlpha, int top, int bottom,
			int left, int right) {
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;

		vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		vertexbuffer.pos((double) left, (double) endY, 0.0D).tex(0.0D, (double) ((float) endY / 32.0F))
				.color(64, 64, 64, endAlpha).endVertex();
		vertexbuffer.pos((double) (left + right), (double) endY, 0.0D)
				.tex((double) ((float) right / 32.0F), (double) ((float) endY / 32.0F)).color(64, 64, 64, endAlpha)
				.endVertex();
		vertexbuffer.pos((double) (left + right), (double) startY, 0.0D)
				.tex((double) ((float) right / 32.0F), (double) ((float) startY / 32.0F)).color(64, 64, 64, startAlpha)
				.endVertex();
		vertexbuffer.pos((double) left, (double) startY, 0.0D).tex(0.0D, (double) ((float) startY / 32.0F))
				.color(64, 64, 64, startAlpha).endVertex();
		tessellator.draw();
	}

	protected static void drawContainerBackground(Tessellator tessellator, int top, int bottom, int left, int right) {
		VertexBuffer buffer = tessellator.getBuffer();
		Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		float f = 32.0F;
		buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		buffer.pos((double) left, (double) bottom, 0.0D).tex((double) ((float) left / f), (double) (bottom / f))
				.color(32, 32, 32, 255).endVertex();
		buffer.pos((double) right, (double) bottom, 0.0D).tex((double) ((float) right / f), (double) (bottom / f))
				.color(32, 32, 32, 255).endVertex();
		buffer.pos((double) right, (double) top, 0.0D).tex((double) ((float) right / f), (double) (top / f))
				.color(32, 32, 32, 255).endVertex();
		buffer.pos((double) left, (double) top, 0.0D).tex((double) ((float) left / f), (double) (top / f))
				.color(32, 32, 32, 255).endVertex();

		tessellator.draw();
	}
}
