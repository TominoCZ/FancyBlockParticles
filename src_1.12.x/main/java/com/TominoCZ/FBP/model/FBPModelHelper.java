package com.TominoCZ.FBP.model;

import com.TominoCZ.FBP.model.FBPModelTransformer.IVertexTransformer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class FBPModelHelper {
	static int vertexes = 0;

	static boolean isAllCorruptedTexture = true;

	public static boolean isModelValid(IBlockState state) {
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
				.getModelForState(state);

		TextureAtlasSprite s = model.getParticleTexture();

		if (s == null || s.getIconName().equals("missingno"))
			return false;

		vertexes = 0;

		try {
			FBPModelTransformer.transform(model, state, 0, new IVertexTransformer() {
				@Override
				public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
					if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION)
						vertexes++;

					TextureAtlasSprite s = quad.getSprite();

					if (s != null && !s.getIconName().equals("missingno"))
						isAllCorruptedTexture = false;

					return data;
				}
			});
		} catch (Throwable t) {

		}

		return (vertexes >= 3) && !isAllCorruptedTexture;
	}
}
