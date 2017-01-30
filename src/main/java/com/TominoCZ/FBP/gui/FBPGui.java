package com.TominoCZ.FBP.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class FBPGui {

	public static  void background(int top, int bottom, int width, int height) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer vertexbuffer = tessellator.getBuffer();
		// Forge: background rendering moved into separate method.
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

	protected static void overlayBackground(int startY, int endY, int startAlpha, int endAlpha, int top, int bottom, int left,
			int right) {
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
