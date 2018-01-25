package com.TominoCZ.FBP;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;

import com.TominoCZ.FBP.block.FBPAnimationDummyBlock;
import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.handler.FBPEventHandler;
import com.TominoCZ.FBP.handler.FBPGuiHandler;
import com.TominoCZ.FBP.handler.FBPKeyInputHandler;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.TominoCZ.FBP.vector.FBPVector3d;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.MinecraftForge;;

@Mod(modid = FBP.MODID, acceptedMinecraftVersions = "1.7.10")
public class FBP {
	@Instance(FBP.MODID)
	public static FBP INSTANCE;

	public final static String MODID = "fbp";

	public static final ResourceLocation LOCATION_PARTICLE_TEXTURE = new ResourceLocation(
			"textures/particle/particles.png");

	public static final ResourceLocation FBP_BUG = new ResourceLocation(FBP.MODID + ":textures/gui/bug.png");
	public static final ResourceLocation FBP_FBP = new ResourceLocation(FBP.MODID + ":textures/gui/fbp.png");
	public static final ResourceLocation FBP_WIDGETS = new ResourceLocation(FBP.MODID + ":textures/gui/widgets.png");

	public static File animExceptionsFile = null, particleExceptionsFile = null;
	public static File config = null;

	public static int minAge, maxAge, particlesPerAxis;

	public static double scaleMult, gravityMult, rotationMult, weatherParticleDensity;

	public static boolean isServer = false;

	public static boolean enabled = true;
	public static boolean showInMillis = false;
	public static boolean infiniteDuration = false;
	public static boolean randomRotation = true, cartoonMode = false, spawnWhileFrozen = true,
			spawnRedstoneBlockParticles = false, smoothTransitions = true, randomFadingSpeed = true,
			entityCollision = false, bounceOffWalls = true, lowTraction = false, smartBreaking = true,
			fancyPlaceAnim = true, spawnPlaceParticles = true, fancyRain = true, fancySnow = true, fancyFlame = true,
			fancySmoke = true, frozen = false;

	public List<String> blockParticleExceptions;
	public List<String> blockAnimExceptions;

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

	public static FBPAnimationDummyBlock FBPBlock = new FBPAnimationDummyBlock();

	public static IRenderHandler fancyWeatherRenderer, originalWeatherRenderer;
	public static EffectRenderer fancyEffectRenderer, originalEffectRenderer;

	public FBPEventHandler eventHandler = new FBPEventHandler();
	public FBPGuiHandler guiHandler = new FBPGuiHandler();

	public FBP() {
		INSTANCE = this;

		blockParticleExceptions = Collections.synchronizedList(new ArrayList<String>());
		blockAnimExceptions = Collections.synchronizedList(new ArrayList<String>());
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		if (evt.getSide().isServer())
			isServer = true;

		config = new File(evt.getModConfigurationDirectory() + "/FBP/Particle.properties");
		animExceptionsFile = new File(evt.getModConfigurationDirectory() + "/FBP/AnimBlockExceptions.txt");
		particleExceptionsFile = new File(evt.getModConfigurationDirectory() + "/FBP/ParticleBlockExceptions.txt");

		FBPConfigHandler.init();

		FBPKeyBindings.init();

		FMLCommonHandler.instance().bus().register(new FBPKeyInputHandler());
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(eventHandler);
		FMLCommonHandler.instance().bus().register(eventHandler);

		GameRegistry.registerBlock(FBPBlock, "FBPBlock");
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

	public boolean isInExceptions(Block b, boolean particle) {
		if (b == null)
			return true;

		return (particle ? blockParticleExceptions : blockAnimExceptions).contains(b.blockRegistry.getNameForObject(b));
	}

	public void addException(Block b, boolean particle) {
		if (b == null)
			return;

		String name = Block.blockRegistry.getNameForObject(b);

		if (!(particle ? blockParticleExceptions : blockAnimExceptions).contains(name))
			(particle ? blockParticleExceptions : blockAnimExceptions).add(name);
	}

	public void removeException(Block b, boolean particle) {
		if (b == null)
			return;

		String name = Block.blockRegistry.getNameForObject(b);

		if ((particle ? blockParticleExceptions : blockAnimExceptions).contains(name))
			(particle ? blockParticleExceptions : blockAnimExceptions).remove(name);
	}

	public void addException(String name, boolean particle) {
		if (StringUtils.isEmpty(name))
			return;

		Block b = (Block) Block.blockRegistry.getObject(name);

		addException(b, particle);
	}

	public void resetExceptions(boolean particle) {
		if (particle)
			blockParticleExceptions.clear();
		else
			blockAnimExceptions.clear();
	}
}