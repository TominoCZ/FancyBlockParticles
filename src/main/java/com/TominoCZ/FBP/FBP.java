package com.TominoCZ.FBP;

import java.io.File;

import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.handler.FBPEventHandler;
import com.TominoCZ.FBP.handler.FBPKeyInputHandler;
import com.TominoCZ.FBP.handler.FBPRenderGuiHandler;
import com.TominoCZ.FBP.keys.FBPKeyBindings;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(clientSideOnly = true, modid = FBP.MODID)
public class FBP {
	@Instance(FBP.MODID)
	public static FBP instance;

	protected final static String MODID = "fbp";

	public static File config;

	public static int minAge, maxAge;

	public static double minScale, maxScale, gravityMult, rotationMult;

	public static boolean legacyMode = false, spawnWhileFrozen = true, spawnRedstoneBlockParticles = true, frozen = false;

	public static FBPEventHandler eventHandler = new FBPEventHandler();

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		config = new File(evt.getModConfigurationDirectory() + "/FBP/Particle.properties");
		
		FBPConfigHandler.init();

		MinecraftForge.EVENT_BUS.register(new FBPRenderGuiHandler());

		FMLCommonHandler.instance().bus().register(new FBPKeyInputHandler());
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(eventHandler);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(new FBPRenderGuiHandler());
	}

	public static boolean isEnabled() {
		boolean result = (Minecraft.getMinecraft().gameSettings.particleSetting != 2);

		if (!result)
			frozen = false;

		return result;
	}

	public static boolean isDev() {
		return (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}
}