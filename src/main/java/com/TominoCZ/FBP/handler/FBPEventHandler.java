package com.TominoCZ.FBP.handler;

import java.util.Queue;

import com.TominoCZ.FBP.particle.FBPParticleEmitter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEmitter;
import net.minecraft.client.particle.ParticleManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class FBPEventHandler {
	boolean result = false;

	@SubscribeEvent
	public void onEntityJoinWorldEvent(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof EntityPlayerSP) {
			ParticleManager particleManager = Minecraft.getMinecraft().effectRenderer;
			try {
				Queue<ParticleEmitter> particleEmitters = (Queue<ParticleEmitter>) ReflectionHelper
						.findField(ParticleManager.class, "particleEmitters", "field_178933_d").get(particleManager);

				if (!exists(particleEmitters))
					particleEmitters.add(new FBPParticleEmitter(e.getWorld(), (Queue<Particle>) ReflectionHelper
							.findField(ParticleManager.class, "queueEntityFX", "field_187241_h").get(particleManager)));

			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	boolean exists(Queue<ParticleEmitter> pe) {
		result = false;

		pe.forEach(p -> {
			if (p instanceof FBPParticleEmitter)
				result = true;
		});

		return result;
	}
}