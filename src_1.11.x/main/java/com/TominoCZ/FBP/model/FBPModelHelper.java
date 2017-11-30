package com.TominoCZ.FBP.model;

import com.TominoCZ.FBP.model.FBPModelTransformer.IVertexTransformer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public class FBPModelHelper {
	static int vertexes = 0;

	public static boolean isModelValid(IBlockState state) {
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes()
				.getModelForState(state);

		if (model.getParticleTexture() != null && model.getParticleTexture().getIconName().equals("missingno"))
			return false;

		vertexes = 0;

		try {
			FBPModelTransformer.transform(model, state, 0, new IVertexTransformer() {
				@Override
				public float[] transform(BakedQuad quad, VertexFormatElement element, float... data) {
					if (element.getUsage() == VertexFormatElement.EnumUsage.POSITION)
						vertexes++;

					return data;
				}
			});
		} catch (Throwable t) {

		}

		return (vertexes >= 3);
	}
}
