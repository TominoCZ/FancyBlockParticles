package com.TominoCZ.FBP;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;

import com.TominoCZ.FBP.block.FBPAnimationDummyBlock;
import com.TominoCZ.FBP.handler.FBPConfigHandler;
import com.TominoCZ.FBP.handler.FBPEventHandler;
import com.TominoCZ.FBP.handler.FBPGuiHandler;
import com.TominoCZ.FBP.handler.FBPKeyInputHandler;
import com.TominoCZ.FBP.keys.FBPKeyBindings;
import com.google.common.base.Throwables;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.init.Blocks;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Mod(clientSideOnly = true, modid = FBP.MODID, acceptedMinecraftVersions = "[1.11,1.12)")
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
			fancyPlaceAnim = true, spawnPlaceParticles = true, fancyWeather = true, fancyFlame = true,
			fancySmoke = true, enableDing = true, frozen = false;

	public List<String> blockParticleExceptions;
	public List<String> blockAnimExceptions;

	public static ThreadLocalRandom random = ThreadLocalRandom.current();

	public static final Vec3d[] CUBE = {
			// TOP
			new Vec3d(1, 1, -1), new Vec3d(1, 1, 1), new Vec3d(-1, 1, 1), new Vec3d(-1, 1, -1),

			// BOTTOM
			new Vec3d(-1, -1, -1), new Vec3d(-1, -1, 1), new Vec3d(1, -1, 1), new Vec3d(1, -1, -1),

			// FRONT
			new Vec3d(-1, -1, 1), new Vec3d(-1, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, -1, 1),
			// BACK
			new Vec3d(1, -1, -1), new Vec3d(1, 1, -1), new Vec3d(-1, 1, -1), new Vec3d(-1, -1, -1),

			// LEFT
			new Vec3d(-1, -1, -1), new Vec3d(-1, 1, -1), new Vec3d(-1, 1, 1), new Vec3d(-1, -1, 1),

			// RIGHT
			new Vec3d(1, -1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, -1), new Vec3d(1, -1, -1) };

	public static final Vec3d[] CUBE_NORMALS = { new Vec3d(0, 1, 0), new Vec3d(0, -1, 0),

			new Vec3d(0, 0, 1), new Vec3d(0, 0, -1),

			new Vec3d(-1, 0, 0), new Vec3d(1, 0, 0) };

	public static VertexFormat POSITION_TEX_COLOR_LMAP_NORMAL;

	public static MethodHandle setSourcePos;

	public static FBPAnimationDummyBlock FBPBlock = new FBPAnimationDummyBlock();

	public static IRenderHandler fancyWeatherRenderer, originalWeatherRenderer;
	public static ParticleManager fancyEffectRenderer, originalEffectRenderer;

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
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		MinecraftForge.EVENT_BUS.register(guiHandler);

		MethodHandles.Lookup lookup = MethodHandles.publicLookup();

		try {
			setSourcePos = lookup
					.unreflectSetter(ReflectionHelper.findField(ParticleDigging.class, "field_181019_az", "sourcePos"));
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	@SubscribeEvent
	public static void registerItems(final RegistryEvent.Register<Block> event) {
		event.getRegistry().register(FBPBlock);
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
				if (fancyWeather)
					Minecraft.getMinecraft().world.provider.setWeatherRenderer(FBP.fancyWeatherRenderer);
			} else {
				Minecraft.getMinecraft().effectRenderer = FBP.originalEffectRenderer;
				Minecraft.getMinecraft().world.provider.setWeatherRenderer(FBP.originalWeatherRenderer);
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

		return (particle ? blockParticleExceptions : blockAnimExceptions).contains(b.getRegistryName().toString());
	}

	public void addException(Block b, boolean particle) {
		if (b == null)
			return;

		String name = b.getRegistryName().toString();

		if (!(particle ? blockParticleExceptions : blockAnimExceptions).contains(name))
			(particle ? blockParticleExceptions : blockAnimExceptions).add(name);
	}

	public void removeException(Block b, boolean particle) {
		if (b == null)
			return;

		String name = b.getRegistryName().toString();

		if ((particle ? blockParticleExceptions : blockAnimExceptions).contains(name))
			(particle ? blockParticleExceptions : blockAnimExceptions).remove(name);
	}

	public void addException(String name, boolean particle) {
		if (StringUtils.isEmpty(name))
			return;

		Iterator it = Block.REGISTRY.getKeys().iterator();

		while (it.hasNext()) {
			ResourceLocation rl = ((ResourceLocation) it.next());
			String s = rl.toString();

			if (s.equals(name)) {
				Block b = Block.REGISTRY.getObject(rl);

				if (b == Blocks.REDSTONE_BLOCK)
					break;

				addException(b, particle);
				break;
			}
		}
	}

	public void resetExceptions(boolean particle) {
		if (particle)
			blockParticleExceptions.clear();
		else
			blockAnimExceptions.clear();
	}
}