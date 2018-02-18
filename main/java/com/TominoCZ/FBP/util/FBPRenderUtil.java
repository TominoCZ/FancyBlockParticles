package com.TominoCZ.FBP.util;

import javax.vecmath.Vector3d;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

public class FBPRenderUtil {
	public static void renderCubeShaded_S(Tessellator tes, FBPVector3d[] par, double f5, double f6, double f7,
			double scale, FBPVector3d rotVec, int brightness, float r, float g, float b, float a, boolean cartoon) {
		// switch to vertex format that supports normals
		tes.draw();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		tes.startDrawingQuads();
		// some GL commands

		GL11.glDepthMask(true);
		RenderHelper.enableStandardItemLighting();

		// render particle
		tes.setTranslation(f5, f6, f7);

		putCube_S(tes, par, scale, rotVec, brightness, r, g, b, a, FBP.cartoonMode);

		tes.setTranslation(0, 0, 0);

		// continue with the regular vertex format
		tes.draw();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		tes.startDrawingQuads();

		GL11.glDisable(GL11.GL_LIGHTING);
	}

	public static void renderCubeShaded_WH(Tessellator tes, FBPVector3d[] par, double f5, double f6, double f7,
			double width, double height, FBPVector3d rotVec, int brightness, float r, float g, float b, float a,
			boolean cartoon) {
		// switch to vertex format that supports normals
		tes.draw();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		tes.startDrawingQuads();

		// switch to vertex format that supports normals

		// some GL commands
		GL11.glDepthMask(true);
		RenderHelper.enableStandardItemLighting();

		// render particle
		tes.setTranslation(f5, f6, f7);

		putCube_WH(tes, par, width, height, rotVec, brightness, r, g, b, a, FBP.cartoonMode);

		tes.setTranslation(0, 0, 0);

		// continue with the regular vertex format
		tes.draw();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		// tes.begin(GL11.GL_QUADS,
		// DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
		tes.startDrawingQuads();

		GL11.glEnable(GL11.GL_LIGHTING);
	}

	static void putCube_S(Tessellator tes, FBPVector3d[] par, double scale, FBPVector3d rotVec, int brightness, float r,
			float g, float b, float a, boolean cartoon) {
		float radsX = (float) Math.toRadians(rotVec.x);
		float radsY = (float) Math.toRadians(rotVec.y);
		float radsZ = (float) Math.toRadians(rotVec.z);

		for (int i = 0; i < FBP.CUBE.length; i += 4) {
			FBPVector3d v1 = FBP.CUBE[i];
			FBPVector3d v2 = FBP.CUBE[i + 1];
			FBPVector3d v3 = FBP.CUBE[i + 2];
			FBPVector3d v4 = FBP.CUBE[i + 3];

			v1 = rotatef_d(v1, radsX, radsY, radsZ);
			v2 = rotatef_d(v2, radsX, radsY, radsZ);
			v3 = rotatef_d(v3, radsX, radsY, radsZ);
			v4 = rotatef_d(v4, radsX, radsY, radsZ);

			FBPVector3d normal = rotatef_d(FBP.CUBE_NORMALS[i / 4], radsX, radsY, radsZ);

			if (!cartoon) {
				addVt_S(tes, scale, v1, par[0].x, par[0].y, brightness, r, g, b, a, normal);
				addVt_S(tes, scale, v2, par[1].x, par[1].y, brightness, r, g, b, a, normal);
				addVt_S(tes, scale, v3, par[2].x, par[2].y, brightness, r, g, b, a, normal);
				addVt_S(tes, scale, v4, par[3].x, par[3].y, brightness, r, g, b, a, normal);
			} else {
				addVt_S(tes, scale, v1, par[0].x, par[0].y, brightness, r, g, b, a, normal);
				addVt_S(tes, scale, v2, par[0].x, par[0].y, brightness, r, g, b, a, normal);
				addVt_S(tes, scale, v3, par[0].x, par[0].y, brightness, r, g, b, a, normal);
				addVt_S(tes, scale, v4, par[0].x, par[0].y, brightness, r, g, b, a, normal);
			}
		}
	}

	static void putCube_WH(Tessellator worldRendererIn, FBPVector3d[] par, double width, double height,
			FBPVector3d rotVec, int brightness, float r, float g, float b, float a, boolean cartoon) {
		float radsX = (float) Math.toRadians(rotVec.x);
		float radsY = (float) Math.toRadians(rotVec.y);
		float radsZ = (float) Math.toRadians(rotVec.z);

		for (int i = 0; i < FBP.CUBE.length; i += 4) {
			FBPVector3d v1 = FBP.CUBE[i];
			FBPVector3d v2 = FBP.CUBE[i + 1];
			FBPVector3d v3 = FBP.CUBE[i + 2];
			FBPVector3d v4 = FBP.CUBE[i + 3];

			v1 = rotatef_d(v1, radsX, radsY, radsZ);
			v2 = rotatef_d(v2, radsX, radsY, radsZ);
			v3 = rotatef_d(v3, radsX, radsY, radsZ);
			v4 = rotatef_d(v4, radsX, radsY, radsZ);

			FBPVector3d normal = rotatef_d(FBP.CUBE_NORMALS[i / 4], radsX, radsY, radsZ);

			if (!cartoon) {
				addVt_WH(worldRendererIn, width, height, v1, par[0].x, par[0].y, brightness, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v2, par[1].x, par[1].y, brightness, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v3, par[2].x, par[2].y, brightness, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v4, par[3].x, par[3].y, brightness, r, g, b, a, normal);
			} else {
				addVt_WH(worldRendererIn, width, height, v1, par[0].x, par[0].y, brightness, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v2, par[0].x, par[0].y, brightness, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v3, par[0].x, par[0].y, brightness, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v4, par[0].x, par[0].y, brightness, r, g, b, a, normal);
			}
		}
	}

	static void addVt_S(Tessellator tes, double scale, FBPVector3d pos, double u, double v, int brightness, float r,
			float g, float b, float a, FBPVector3d n) {
		tes.setColorRGBA_F(r, g, b, a);
		tes.setBrightness(brightness);
		tes.setNormal((float) n.x, (float) n.y, (float) n.z);
		tes.addVertexWithUV(pos.x * scale, pos.y * scale, pos.z * scale, u, v);
	}

	static void addVt_WH(Tessellator tes, double width, double height, FBPVector3d pos, double u, double v,
			int brightness, float r, float g, float b, float a, FBPVector3d n) {
		tes.setColorRGBA_F(r, g, b, a);
		tes.setBrightness(brightness);
		tes.addVertexWithUV(pos.x * width, pos.y * height, pos.z * width, u, v);
		tes.setNormal((float) n.x, (float) n.y, (float) n.z);
	}

	public static FBPVector3d rotatef_d(FBPVector3d vec, float AngleX, float AngleY, float AngleZ) {
		FBPVector3d sin = new FBPVector3d(MathHelper.sin(AngleX), MathHelper.sin(AngleY), MathHelper.sin(AngleZ));
		FBPVector3d cos = new FBPVector3d(MathHelper.cos(AngleX), MathHelper.cos(AngleY), MathHelper.cos(AngleZ));

		vec = new FBPVector3d(vec.x, vec.y * cos.x - vec.z * sin.x, vec.y * sin.x + vec.z * cos.x);
		vec = new FBPVector3d(vec.x * cos.y + vec.z * sin.y, vec.y, vec.x * sin.y - vec.z * cos.y);
		vec = new FBPVector3d(vec.x * cos.z - vec.y * sin.z, vec.x * sin.z + vec.y * cos.z, vec.z);

		return vec;
	}

	public static Vector3d rotatef_f(Vector3f pos, float AngleX, float AngleY, float AngleZ, EnumFacing facing) {
		FBPVector3d sin = new FBPVector3d(MathHelper.sin(AngleX), MathHelper.sin(AngleY), MathHelper.sin(AngleZ));
		FBPVector3d cos = new FBPVector3d(MathHelper.cos(AngleX), MathHelper.cos(AngleY), MathHelper.cos(AngleZ));

		FBPVector3d pos1 = new FBPVector3d(pos.x, pos.y, pos.z);
		FBPVector3d pos2;

		if (facing == EnumFacing.EAST) {
			pos1.x -= 1.0f;
		} else if (facing == EnumFacing.WEST) {
			pos1.x += 1.0f;
		} else if (facing == EnumFacing.SOUTH) {
			pos1.z -= 1.0f;
			pos1.x -= 1.0f;
		}

		pos2 = new FBPVector3d(pos1.x, pos1.y * cos.x - pos1.z * sin.x, pos1.y * sin.x + pos1.z * cos.x);
		pos2 = new FBPVector3d(pos2.x * cos.y + pos2.z * sin.y, pos2.y, pos2.x * sin.y - pos2.z * cos.y);
		pos2 = new FBPVector3d(pos2.x * cos.z - pos2.y * sin.z, pos2.x * sin.z + pos2.y * cos.z, pos2.z);

		if (facing == EnumFacing.EAST) {
			pos2.x += 1.0f;
		} else if (facing == EnumFacing.WEST) {
			pos2.x -= 1.0f;
		} else if (facing == EnumFacing.SOUTH) {
			pos2.z += 1.0f;
			pos2.x += 1.0f;
		}

		return new Vector3d(pos2.x, pos2.y, pos2.z);
	}
}
