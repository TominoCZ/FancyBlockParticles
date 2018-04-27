package com.TominoCZ.FBP;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

import org.apache.commons.lang3.StringUtils;

import com.TominoCZ.FBP.handler.FBPEventHandler;
import com.TominoCZ.FBP.handler.FBPGuiHandler;
import com.TominoCZ.FBP.handler.FBPKeyInputHandler;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.google.common.base.Throwables;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.Blocks;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod(clientSideOnly = true, modid = FBP.MODID)
public class FBP {
	public final static String MODID = "fbp";
	public static final ResourceLocation LOCATION_PARTICLE_TEXTURE = new ResourceLocation(
			"textures/particle/particles.png");
	public static final ResourceLocation FBP_BUG = new ResourceLocation(FBP.MODID + ":textures/gui/bug.png");
	public static final ResourceLocation FBP_FBP = new ResourceLocation(FBP.MODID + ":textures/gui/fbp.png");
	public static final ResourceLocation FBP_WIDGETS = new ResourceLocation(FBP.MODID + ":textures/gui/widgets.png");
	public static final Vec3[] CUBE = {
			// TOP
			new Vec3(1, 1, -1), new Vec3(1, 1, 1), new Vec3(-1, 1, 1), new Vec3(-1, 1, -1),

			// BOTTOM
			new Vec3(-1, -1, -1), new Vec3(-1, -1, 1), new Vec3(1, -1, 1), new Vec3(1, -1, -1),

			// FRONT
			new Vec3(-1, -1, 1), new Vec3(-1, 1, 1), new Vec3(1, 1, 1), new Vec3(1, -1, 1),

			// BACK
			new Vec3(1, -1, -1), new Vec3(1, 1, -1), new Vec3(-1, 1, -1), new Vec3(-1, -1, -1),

			// LEFT
			new Vec3(-1, -1, -1), new Vec3(-1, 1, -1), new Vec3(-1, 1, 1), new Vec3(-1, -1, 1),

			// RIGHT
			new Vec3(1, -1, 1), new Vec3(1, 1, 1), new Vec3(1, 1, -1), new Vec3(1, -1, -1) };
	public static final Vec3[] CUBE_NORMALS = { new Vec3(0, 1, 0), new Vec3(0, -1, 0),

			new Vec3(0, 0, 1), new Vec3(0, 0, -1),

			new Vec3(-1, 0, 0), new Vec3(1, 0, 0) };
	@Instance(FBP.MODID)
	public static FBP INSTANCE;
	public static File particleExceptionsFile = null;
	public static File floatingMaterialsFile = null;
	public static File config = null;
	public static int minAge, maxAge, particlesPerAxis;
	public static double scaleMult, gravityMult, rotationMult, weatherParticleDensity;

	public static boolean enabled = true;
	public static boolean showInMillis = false;
	public static boolean infiniteDuration = false;
	public static boolean randomRotation, cartoonMode, spawnWhileFrozen, spawnRedstoneBlockParticles, smoothTransitions,
			randomFadingSpeed, entityCollision, bounceOffWalls, lowTraction, smartBreaking, fancyRain, fancySnow,
			fancyFlame, fancySmoke, waterPhysics, restOnFloor, frozen;

	public static SplittableRandom random = new SplittableRandom();

	public static VertexFormat POSITION_TEX_COLOR_LMAP_NORMAL;
	public static MethodHandle setSourcePos;
	public static IRenderHandler fancyWeatherRenderer, originalWeatherRenderer;
	public static EffectRenderer fancyEffectRenderer, originalEffectRenderer;

	public List<String> blockParticleExceptions;
	public List<Material> floatingMaterials;

	public FBPEventHandler eventHandler = new FBPEventHandler();
	public FBPGuiHandler guiHandler = new FBPGuiHandler();

	public FBP() {
		INSTANCE = this;

		POSITION_TEX_COLOR_LMAP_NORMAL = new VertexFormat();

		POSITION_TEX_COLOR_LMAP_NORMAL.addElement(DefaultVertexFormats.POSITION_3F);
		POSITION_TEX_COLOR_LMAP_NORMAL.addElement(DefaultVertexFormats.TEX_2F);
		POSITION_TEX_COLOR_LMAP_NORMAL.addElement(DefaultVertexFormats.COLOR_4UB);
		POSITION_TEX_COLOR_LMAP_NORMAL.addElement(DefaultVertexFormats.TEX_2S);
		POSITION_TEX_COLOR_LMAP_NORMAL.addElement(DefaultVertexFormats.NORMAL_3B);

		blockParticleExceptions = Collections.synchronizedList(new ArrayList<String>());
		floatingMaterials = Collections.synchronizedList(new ArrayList<Material>());
	}

	public static boolean isEnabled() {
		if (!enabled)
			frozen = false;

		return enabled;
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

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		config = new File(evt.getModConfigurationDirectory() + "/FBP/Particle.properties");
		particleExceptionsFile = new File(evt.getModConfigurationDirectory() + "/FBP/ParticleBlockExceptions.txt");
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

		MethodHandles.Lookup lookup = MethodHandles.publicLookup();

		try {
			setSourcePos = lookup.unreflectSetter(
					ReflectionHelper.findField(EntityDiggingFX.class, "field_181019_az", "field_181019_az"));
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public boolean isInExceptions(Block b) {
		if (b == null)
			return true;

		return blockParticleExceptions.contains(b.getRegistryName().toString());
	}

	public boolean doesMaterialFloat(Material mat) {
		return floatingMaterials.contains(mat);
	}

	public void addException(Block b) {
		if (b == null)
			return;

		String name = b.getRegistryName().toString();

		if (!blockParticleExceptions.contains(name))
			blockParticleExceptions.add(name);
	}

	public void removeException(Block b) {
		if (b == null)
			return;

		String name = b.getRegistryName().toString();

		if (blockParticleExceptions.contains(name))
			blockParticleExceptions.remove(name);
	}

	public void addException(String name) {
		if (StringUtils.isEmpty(name))
			return;

		Block b = Block.getBlockFromName(name);

		if (b == null || b == Blocks.redstone_block)
			return;

		addException(b);
	}

	public void resetExceptions() {
		blockParticleExceptions.clear();
	}
}