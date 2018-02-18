package com.TominoCZ.FBP.handler;

import java.util.List;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.particle.FBPParticleManager;
import com.TominoCZ.FBP.renderer.FBPWeatherRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
			Chunk c = mc.theWorld.getChunkFromChunkCoords(x, z);

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
}