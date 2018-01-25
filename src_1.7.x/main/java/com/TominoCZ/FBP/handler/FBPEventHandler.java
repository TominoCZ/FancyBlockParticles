package com.TominoCZ.FBP.handler;

import java.util.List;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.particle.FBPParticleManager;
import com.TominoCZ.FBP.renderer.FBPWeatherRenderer;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;

public class FBPEventHandler {
	Minecraft mc;

	public FBPEventHandler() {
		mc = Minecraft.getMinecraft();
	}

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent e) {
		if (!mc.isGamePaused() && mc.theWorld != null) {
			if (mc.theWorld.provider.getWeatherRenderer() == FBP.fancyWeatherRenderer && FBP.enabled)
				((FBPWeatherRenderer) FBP.fancyWeatherRenderer).onUpdate();

			MovingObjectPosition moo = mc.objectMouseOver;
			int x = 0;
			int z = 0;

			if (moo != null) {
				x = moo.blockX;
				z = moo.blockZ;
			} else {
				x = (int) mc.thePlayer.posX;
				z = (int) mc.thePlayer.posZ;
			}
			Chunk c = mc.theWorld.getChunkFromBlockCoords(x, z);

			List l = mc.theWorld.getPendingBlockUpdates(c, false);
			if (l != null)
				System.out.println(l);
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onEntityJoinWorldEvent(EntityJoinWorldEvent e) {

		if (e.entity == mc.thePlayer) {
			FBP.fancyEffectRenderer = new FBPParticleManager(mc.theWorld, mc.renderEngine);
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

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load e) {
		FBPConfigHandler.init();
	}

	@SubscribeEvent
	public void onRenderWorldLastEvent(RenderWorldLastEvent e) {
		if (mc.effectRenderer instanceof FBPParticleManager) {
			FBPParticleManager pm = (FBPParticleManager) mc.effectRenderer;

			pm.renderShadedParticles(e.partialTicks);
		}
	}
}