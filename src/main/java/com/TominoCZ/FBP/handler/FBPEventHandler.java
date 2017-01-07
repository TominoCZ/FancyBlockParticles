package com.TominoCZ.FBP.handler;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Queue;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.particle.FBPParticleEmitter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEmitter;
import net.minecraft.client.particle.ParticleManager;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FBPEventHandler {
	@SubscribeEvent
	public void onEntityJoinWorldEvent(EntityJoinWorldEvent e) {
		if (e.getEntity() instanceof EntityPlayerSP) {
			ParticleManager particleManager = Minecraft.getMinecraft().effectRenderer;

			Field field1, field2;

			try {
				if (FBP.isDev() == true) {
					field1 = ParticleManager.class.getDeclaredField("particleEmitters");
					field2 = ParticleManager.class.getDeclaredField("queueEntityFX");
				} else {
					field1 = ParticleManager.class.getDeclaredField("field_178933_d");
					field2 = ParticleManager.class.getDeclaredField("field_187241_h");
				}

				field1.setAccessible(true);
				field2.setAccessible(true);

				Queue<ParticleEmitter> particleEmitters = (Queue<ParticleEmitter>) field1.get(particleManager);

				if (!exists(particleEmitters))
					particleEmitters
							.add(new FBPParticleEmitter(e.getWorld(), (Queue<Particle>) field2.get(particleManager)));
			} catch (Exception x) {
				x.printStackTrace();
			}
		}
	}

	boolean exists(Queue<ParticleEmitter> pe) {
		Iterator it = pe.iterator();

		Object o;

		while (it.hasNext()) {
			o = it.next();

			if (o instanceof FBPParticleEmitter)
				return true;
		}

		return false;
	}
}