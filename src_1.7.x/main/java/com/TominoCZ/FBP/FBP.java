package com.TominoCZ.FBP;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

import org.apache.commons.lang3.StringUtils;

import com.TominoCZ.FBP.handler.FBPEventHandler;
import com.TominoCZ.FBP.handler.FBPGuiHandler;
import com.TominoCZ.FBP.handler.FBPKeyInputHandler;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.TominoCZ.FBP.particle.FBPParticleManager;
import com.TominoCZ.FBP.vector.FBPVector3d;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.MinecraftForge;;

@Mod(modid = FBP.MODID, acceptedMinecraftVersions = "[1.7,1.8)")
public class FBP {
	@Instance(FBP.MODID)
	public static FBP INSTANCE;

	public final static String MODID = "fbp";

	public static final ResourceLocation LOCATION_PARTICLE_TEXTURE = new ResourceLocation(
			"textures/particle/particles.png");

	public static final ResourceLocation FBP_BUG = new ResourceLocation(FBP.MODID + ":textures/gui/bug.png");
	public static final ResourceLocation FBP_FBP = new ResourceLocation(FBP.MODID + ":textures/gui/fbp.png");
	public static final ResourceLocation FBP_WIDGETS = new ResourceLocation(FBP.MODID + ":textures/gui/widgets.png");

	public static File particleBlacklistFile = null;
	public static File floatingMaterialsFile = null;
	public static File config = null;

	public static int minAge, maxAge, particlesPerAxis;

	public static double scaleMult, gravityMult, rotationMult, weatherParticleDensity;

	public static boolean enabled = true;
	public static boolean showInMillis = false;
	public static boolean infiniteDuration = false;
	public static boolean randomRotation, cartoonMode, spawnWhileFrozen, spawnRedstoneBlockParticles, randomizedScale,
			randomFadingSpeed, entityCollision, bounceOffWalls, lowTraction, smartBreaking, fancyRain, fancySnow,
			fancyFlame, fancySmoke, waterPhysics, restOnFloor, frozen;

	public List<String> blockParticleBlacklist;
	public List<Material> floatingMaterials;

	public static SplittableRandom random = new SplittableRandom();

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
	public static FBPParticleManager fancyEffectRenderer;
	public static EffectRenderer originalEffectRenderer;

	public FBPEventHandler eventHandler = new FBPEventHandler();
	public FBPGuiHandler guiHandler = new FBPGuiHandler();

	public FBP() {
		INSTANCE = this;

		blockParticleBlacklist = Collections.synchronizedList(new ArrayList<String>());
		floatingMaterials = Collections.synchronizedList(new ArrayList<Material>());
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		config = new File(evt.getModConfigurationDirectory() + "/FBP/Particle.properties");
		particleBlacklistFile = new File(evt.getModConfigurationDirectory() + "/FBP/ParticleBlockBlacklist.txt");
		floatingMaterialsFile = new File(evt.getModConfigurationDirectory() + "/FBP/FloatingMaterials.txt");

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
				FBP.fancyEffectRenderer.carryOver();

				Minecraft.getMinecraft().effectRenderer = FBP.fancyEffectRenderer;
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

	public boolean isBlacklisted(Block b) {
		if (b == null)
			return true;

		return blockParticleBlacklist.contains(b.blockRegistry.getNameForObject(b));
	}

	public boolean doesMaterialFloat(Material mat) {
		return floatingMaterials.contains(mat);
	}

	public void addToBlacklist(String name) {
		if (StringUtils.isEmpty(name))
			return;

		Block b = (Block) Block.blockRegistry.getObject(name);

		addToBlacklist(b);
	}

	public void addToBlacklist(Block b) {
		if (b == null)
			return;

		String name = Block.blockRegistry.getNameForObject(b);

		if (!blockParticleBlacklist.contains(name))
			blockParticleBlacklist.add(name);
	}

	public void removeFromBlacklist(Block b) {
		if (b == null)
			return;

		String name = Block.blockRegistry.getNameForObject(b);

		if (blockParticleBlacklist.contains(name))
			blockParticleBlacklist.remove(name);
	}

	public void resetBlacklist() {
		blockParticleBlacklist.clear();
	}
}