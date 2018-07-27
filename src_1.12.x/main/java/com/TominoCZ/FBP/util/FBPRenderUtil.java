package com.TominoCZ.FBP.util;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class FBPRenderUtil
{
	public static void renderCubeShaded_S(BufferBuilder buf, Vec2f[] par, float f5, float f6, float f7, double scale,
			FBPVector3d rotVec, int j, int k, float r, float g, float b, float a, boolean cartoon)
	{
		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		buf.begin(GL11.GL_QUADS, FBP.POSITION_TEX_COLOR_LMAP_NORMAL);

		// switch to vertex format that supports normals
		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		buf.begin(GL11.GL_QUADS, FBP.POSITION_TEX_COLOR_LMAP_NORMAL);

		// some GL commands
		GlStateManager.enableCull();
		RenderHelper.enableStandardItemLighting();

		// render particle
		buf.setTranslation(f5, f6, f7);

		putCube_S(buf, par, scale, rotVec, j, k, r, g, b, a, FBP.cartoonMode);

		buf.setTranslation(0, 0, 0);

		// continue with the regular vertex format
		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		RenderHelper.disableStandardItemLighting();
	}

	public static void renderCubeShaded_WH(BufferBuilder buf, Vec2f[] par, float f5, float f6, float f7, double width,
			double height, FBPVector3d rotVec, int j, int k, float r, float g, float b, float a, boolean cartoon)
	{

		// switch to vertex format that supports normals
		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		buf.begin(GL11.GL_QUADS, FBP.POSITION_TEX_COLOR_LMAP_NORMAL);

		// switch to vertex format that supports normals
		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		buf.begin(GL11.GL_QUADS, FBP.POSITION_TEX_COLOR_LMAP_NORMAL);

		// some GL commands
		GlStateManager.enableCull();
		RenderHelper.enableStandardItemLighting();

		// render particle
		buf.setTranslation(f5, f6, f7);

		putCube_WH(buf, par, width, height, rotVec, j, k, r, g, b, a, FBP.cartoonMode);

		buf.setTranslation(0, 0, 0);

		// continue with the regular vertex format
		Tessellator.getInstance().draw();
		Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

		RenderHelper.disableStandardItemLighting();
	}

	static void putCube_S(BufferBuilder worldRendererIn, Vec2f[] par, double scale, FBPVector3d rotVec, int j, int k,
			float r, float g, float b, float a, boolean cartoon)
	{
		float radsX = (float) Math.toRadians(rotVec.x);
		float radsY = (float) Math.toRadians(rotVec.y);
		float radsZ = (float) Math.toRadians(rotVec.z);

		for (int i = 0; i < FBP.CUBE.length; i += 4)
		{
			Vec3d v1 = FBP.CUBE[i];
			Vec3d v2 = FBP.CUBE[i + 1];
			Vec3d v3 = FBP.CUBE[i + 2];
			Vec3d v4 = FBP.CUBE[i + 3];

			v1 = rotatef_d(v1, radsX, radsY, radsZ);
			v2 = rotatef_d(v2, radsX, radsY, radsZ);
			v3 = rotatef_d(v3, radsX, radsY, radsZ);
			v4 = rotatef_d(v4, radsX, radsY, radsZ);

			Vec3d normal = rotatef_d(FBP.CUBE_NORMALS[i / 4], radsX, radsY, radsZ);

			if (!cartoon)
			{
				addVt_S(worldRendererIn, scale, v1, par[0].x, par[0].y, j, k, r, g, b, a, normal);
				addVt_S(worldRendererIn, scale, v2, par[1].x, par[1].y, j, k, r, g, b, a, normal);
				addVt_S(worldRendererIn, scale, v3, par[2].x, par[2].y, j, k, r, g, b, a, normal);
				addVt_S(worldRendererIn, scale, v4, par[3].x, par[3].y, j, k, r, g, b, a, normal);
			} else
			{
				addVt_S(worldRendererIn, scale, v1, par[0].x, par[0].y, j, k, r, g, b, a, normal);
				addVt_S(worldRendererIn, scale, v2, par[0].x, par[0].y, j, k, r, g, b, a, normal);
				addVt_S(worldRendererIn, scale, v3, par[0].x, par[0].y, j, k, r, g, b, a, normal);
				addVt_S(worldRendererIn, scale, v4, par[0].x, par[0].y, j, k, r, g, b, a, normal);
			}
		}
	}

	static void putCube_WH(BufferBuilder worldRendererIn, Vec2f[] par, double width, double height, FBPVector3d rotVec,
			int j, int k, float r, float g, float b, float a, boolean cartoon)
	{
		float radsX = (float) Math.toRadians(rotVec.x);
		float radsY = (float) Math.toRadians(rotVec.y);
		float radsZ = (float) Math.toRadians(rotVec.z);

		for (int i = 0; i < FBP.CUBE.length; i += 4)
		{
			Vec3d v1 = FBP.CUBE[i];
			Vec3d v2 = FBP.CUBE[i + 1];
			Vec3d v3 = FBP.CUBE[i + 2];
			Vec3d v4 = FBP.CUBE[i + 3];

			v1 = rotatef_d(v1, radsX, radsY, radsZ);
			v2 = rotatef_d(v2, radsX, radsY, radsZ);
			v3 = rotatef_d(v3, radsX, radsY, radsZ);
			v4 = rotatef_d(v4, radsX, radsY, radsZ);

			Vec3d normal = rotatef_d(FBP.CUBE_NORMALS[i / 4], radsX, radsY, radsZ);

			if (!cartoon)
			{
				addVt_WH(worldRendererIn, width, height, v1, par[0].x, par[0].y, j, k, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v2, par[1].x, par[1].y, j, k, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v3, par[2].x, par[2].y, j, k, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v4, par[3].x, par[3].y, j, k, r, g, b, a, normal);
			} else
			{
				addVt_WH(worldRendererIn, width, height, v1, par[0].x, par[0].y, j, k, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v2, par[0].x, par[0].y, j, k, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v3, par[0].x, par[0].y, j, k, r, g, b, a, normal);
				addVt_WH(worldRendererIn, width, height, v4, par[0].x, par[0].y, j, k, r, g, b, a, normal);
			}
		}
	}

	static void addVt_S(BufferBuilder worldRendererIn, double scale, Vec3d pos, double u, double v, int j, int k,
			float r, float g, float b, float a, Vec3d n)
	{
		worldRendererIn.pos(pos.x * scale, pos.y * scale, pos.z * scale).tex(u, v).color(r, g, b, a).lightmap(j, k)
				.normal((float) n.x, (float) n.y, (float) n.z).endVertex();
	}

	static void addVt_WH(BufferBuilder worldRendererIn, double width, double height, Vec3d pos, double u, double v,
			int j, int k, float r, float g, float b, float a, Vec3d n)
	{
		worldRendererIn.pos(pos.x * width, pos.y * height, pos.z * width).tex(u, v).color(r, g, b, a).lightmap(j, k)
				.normal((float) n.x, (float) n.y, (float) n.z).endVertex();
	}

	public static Vec3d rotatef_d(Vec3d vec, float AngleX, float AngleY, float AngleZ)
	{
		FBPVector3d sin = new FBPVector3d(MathHelper.sin(AngleX), MathHelper.sin(AngleY), MathHelper.sin(AngleZ));
		FBPVector3d cos = new FBPVector3d(MathHelper.cos(AngleX), MathHelper.cos(AngleY), MathHelper.cos(AngleZ));

		vec = new Vec3d(vec.x, vec.y * cos.x - vec.z * sin.x, vec.y * sin.x + vec.z * cos.x);
		vec = new Vec3d(vec.x * cos.z - vec.y * sin.z, vec.x * sin.z + vec.y * cos.z, vec.z);
		vec = new Vec3d(vec.x * cos.y + vec.z * sin.y, vec.y, vec.x * sin.y - vec.z * cos.y);

		return vec;
	}

	public static Vector3f rotatef_f(Vector3f pos, float AngleX, float AngleY, float AngleZ, EnumFacing facing)
	{
		FBPVector3d sin = new FBPVector3d(MathHelper.sin(AngleX), MathHelper.sin(AngleY), MathHelper.sin(AngleZ));
		FBPVector3d cos = new FBPVector3d(MathHelper.cos(AngleX), MathHelper.cos(AngleY), MathHelper.cos(AngleZ));

		FBPVector3d pos1 = new FBPVector3d(pos.x, pos.y, pos.z);
		FBPVector3d pos2;

		if (facing == EnumFacing.EAST)
		{
			pos1.x -= 1.0f;
		} else if (facing == EnumFacing.WEST)
		{
			pos1.x += 1.0f;
		} else if (facing == EnumFacing.SOUTH)
		{
			pos1.z -= 1.0f;
			pos1.x -= 1.0f;
		}

		pos2 = new FBPVector3d(pos1.x, pos1.y * cos.x - pos1.z * sin.x, pos1.y * sin.x + pos1.z * cos.x);
		pos2 = new FBPVector3d(pos2.x * cos.z - pos2.y * sin.z, pos2.x * sin.z + pos2.y * cos.z, pos2.z);
		pos2 = new FBPVector3d(pos2.x * cos.y + pos2.z * sin.y, pos2.y, pos2.x * sin.y - pos2.z * cos.y);

		if (facing == EnumFacing.EAST)
		{
			pos2.x += 1.0f;
		} else if (facing == EnumFacing.WEST)
		{
			pos2.x -= 1.0f;
		} else if (facing == EnumFacing.SOUTH)
		{
			pos2.z += 1.0f;
			pos2.x += 1.0f;
		}

		return new Vector3f((float) pos2.x, (float) pos2.y, (float) pos2.z);
	}

	public static void markBlockForRender(BlockPos pos)
	{
		BlockPos bp1, bp2;
		bp1 = pos.add(1, 1, 1);
		bp2 = pos.add(-1, -1, -1);

		Minecraft.getMinecraft().renderGlobal.markBlockRangeForRenderUpdate(bp1.getX(), bp1.getY(), bp1.getZ(),
				bp2.getX(), bp2.getY(), bp2.getZ());
	}
}
