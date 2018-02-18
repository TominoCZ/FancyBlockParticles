package com.TominoCZ.FBP;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;

import com.TominoCZ.FBP.handler.FBPEventHandler;
import com.TominoCZ.FBP.handler.FBPGuiHandler;
import com.TominoCZ.FBP.handler.FBPKeyInputHandler;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.TominoCZ.FBP.vector.FBPVector3d;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;;

@Mod(modid = FBP.MODID, acceptedMinecraftVersions = "[1.8.9)")
public class FBP {
	@Instance(FBP.MODID)
	public static FBP INSTANCE;

	public final static String MODID = "fbp";

	public static final ResourceLocation LOCATION_PARTICLE_TEXTURE = new ResourceLocation(
			"textures/particle/particles.png");

	public static final ResourceLocation FBP_BUG = new ResourceLocation(FBP.MODID + ":textures/gui/bug.png");
	public static final ResourceLocation FBP_FBP = new ResourceLocation(FBP.MODID + ":textures/gui/fbp.png");
	public static final ResourceLocation FBP_WIDGETS = new ResourceLocation(FBP.MODID + ":textures/gui/widgets.png");

	public static File particleExceptionsFile = null;
	public static File config = null;

	public static int minAge, maxAge, particlesPerAxis;

	public static double scaleMult, gravityMult, rotationMult, weatherParticleDensity;

	public static boolean isServer = false;

	public static boolean enabled = true;
	public static boolean showInMillis = false;
	public static boolean infiniteDuration = false;
	public static boolean randomRotation, cartoonMode, spawnWhileFrozen, spawnRedstoneBlockParticles, smoothTransitions,
			randomFadingSpeed, entityCollision, bounceOffWalls, lowTraction, smartBreaking, fancyRain, fancySnow,
			fancyFlame, fancySmoke, frozen;

	public List<String> blockParticleExceptions;

	public static ThreadLocalRandom random = ThreadLocalRandom.current();

	public static final FBPVector3d[] CUBE = {
			// TOP
			new FBPVector3d(1, 1, -1), new FBPVector3d(1, 1, 1), new FBPVector3d(-1, 1, 1), new FBPVector3d(-1, 1, -1),

			// BOTTOM
			new FBPVector3d(-1, -1, -1), new FBPVector3d(-1, -1, 1), new FBPVector3d(1, -1, 1),
			new FBPVector3d(1, -1, -1),

			// FRONT
			new FBPVector3d(-1, -1, 1), new FBPVector3d(-1, 1, 1), new FBPVector3d(1, 1, 1), new FBPVector3d(1, -1, 1),
			// BACK
			new FBPVector3d(1, -1, -1), new FBPVector3d(1, 1, -1), new FBPVector3d(-1, 1, -1),
			new FBPVector3d(-1, -1, -1),

			// LEFT
			new FBPVector3d(-1, -1, -1), new FBPVector3d(-1, 1, -1), new FBPVector3d(-1, 1, 1),
			new FBPVector3d(-1, -1, 1),

			// RIGHT
			new FBPVector3d(1, -1, 1), new FBPVector3d(1, 1, 1), new FBPVector3d(1, 1, -1),
			new FBPVector3d(1, -1, -1) };

	public static final FBPVector3d[] CUBE_NORMALS = { new FBPVector3d(0, 1, 0), new FBPVector3d(0, -1, 0),

			new FBPVector3d(0, 0, 1), new FBPVector3d(0, 0, -1),

			new FBPVector3d(-1, 0, 0), new FBPVector3d(1, 0, 0) };

	public static IRenderHandler fancyWeatherRenderer, originalWeatherRenderer;
	public static EffectRenderer fancyEffectRenderer, originalEffectRenderer;

	public FBPEventHandler eventHandler = new FBPEventHandler();
	public FBPGuiHandler guiHandler = new FBPGuiHandler();

	public FBP() {
		INSTANCE = this;

		blockParticleExceptions = Collections.synchronizedList(new ArrayList<String>());
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		if (evt.getSide().isServer())
			isServer = true;

		config = new File(evt.getModConfigurationDirectory() + "/FBP/Particle.properties");
		particleExceptionsFile = new File(evt.getModConfigurationDirectory() + "/FBP/ParticleBlockExceptions.txt");

		FBPKeyBindings.init();

		FMLCommonHandler.instance().bus().register(new FBPKeyInputHandler());
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(eventHandler);
		FMLCommonHandler.instance().bus().register(eventHandler);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(guiHandler);
	}

	public static boolean isEnabled() {
		boolean result = enabled;

		if (!result)
			frozen = false;

		return result;
	}

	public static void setEnabled(boolean enabled) {
		if (FBP.enabled != enabled) {
			if (enabled) {
				Minecraft.getMinecraft().effectRenderer = FBP.fancyEffectRenderer;
				if (fancyRain || fancySnow) // just to ensure compatibility once more..
					Minecraft.getMinecraft().theWorld.provider.setWeatherRenderer(FBP.fancyWeatherRenderer);
			} else {
				Minecraft.getMinecraft().effectRenderer = FBP.originalEffectRenderer;
				Minecraft.getMinecraft().theWorld.provider.setWeatherRenderer(FBP.originalWeatherRenderer);
			}
		}
		FBP.enabled = enabled;
	}

	public static boolean isDev() {
		return (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
	}

	@SuppressWarnings("unlikely-arg-type")
	public boolean isInExceptions(Block b) {
		if (b == null)
			return true;

		return blockParticleExceptions.contains(Block.blockRegistry.getNameForObject(b));
	}

	@SuppressWarnings("unlikely-arg-type")
	public void addException(Block b) {
		if (b == null)
			return;

		ResourceLocation name = Block.blockRegistry.getNameForObject(b);

		if (!blockParticleExceptions.contains(name))
			blockParticleExceptions.add(name.toString());
	}

	@SuppressWarnings("unlikely-arg-type")
	public void removeException(Block b) {
		if (b == null)
			return;

		ResourceLocation name = Block.blockRegistry.getNameForObject(b);

		if (blockParticleExceptions.contains(name))
			blockParticleExceptions.remove(name);
	}

	public void addException(String name) {
		if (StringUtils.isEmpty(name))
			return;

		Block b = (Block) Block.blockRegistry.getObject(new ResourceLocation(name));

		addException(b);
	}

	public void resetExceptions() {
		blockParticleExceptions.clear();
	}
}