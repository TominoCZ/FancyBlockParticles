package com.TominoCZ.FBP.handler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import com.TominoCZ.FBP.FBP;
import com.TominoCZ.FBP.util.FBPObfUtil;

import net.minecraft.block.material.Material;

public class FBPConfigHandler
{
	static FileInputStream fis;
	static InputStreamReader isr;
	static BufferedReader br;

	public static void init()
	{
		try
		{
			defaults(false);

			if (!Paths.get(FBP.config.getParent()).toFile().exists())
				Paths.get(FBP.config.getParent()).toFile().mkdirs();

			if (!FBP.config.exists())
			{
				FBP.config.createNewFile();

				write();
			}

			if (!FBP.animBlacklistFile.exists())
				FBP.animBlacklistFile.createNewFile();

			if (!FBP.particleBlacklistFile.exists())
				FBP.particleBlacklistFile.createNewFile();

			if (!FBP.floatingMaterialsFile.exists())
			{
				FBP.floatingMaterialsFile.createNewFile();

				FBP.INSTANCE.floatingMaterials.clear();

				FBP.INSTANCE.floatingMaterials.add(Material.LEAVES);
				FBP.INSTANCE.floatingMaterials.add(Material.PLANTS);
				FBP.INSTANCE.floatingMaterials.add(Material.ICE);
				FBP.INSTANCE.floatingMaterials.add(Material.PACKED_ICE);
				FBP.INSTANCE.floatingMaterials.add(Material.CARPET);
				FBP.INSTANCE.floatingMaterials.add(Material.WOOD);
				FBP.INSTANCE.floatingMaterials.add(Material.WEB);
			} else
				readFloatingMaterials();

			read();
			readAnimBlacklist();
			readParticleBlacklist();

			write();
			writeAnimBlacklist();
			writeParticleBlacklist();
			writeFloatingMaterials();

			closeStreams();
		} catch (IOException e)
		{
			closeStreams();

			write();
		}
	}

	public static void write()
	{
		try
		{
			PrintWriter writer = new PrintWriter(FBP.config.getPath(), "UTF-8");
			writer.println("enabled=" + FBP.enabled);
			writer.println("weatherParticleDensity=" + FBP.weatherParticleDensity);
			writer.println("particlesPerAxis=" + FBP.particlesPerAxis);
			writer.println("restOnFloor=" + FBP.restOnFloor);
			writer.println("waterPhysics=" + FBP.waterPhysics);
			writer.println("fancyFlame=" + FBP.fancyFlame);
			writer.println("fancySmoke=" + FBP.fancySmoke);
			writer.println("fancyRain=" + FBP.fancyRain);
			writer.println("fancySnow=" + FBP.fancySnow);
			writer.println("spawnPlaceParticles=" + FBP.spawnPlaceParticles);
			writer.println("fancyPlaceAnim=" + FBP.fancyPlaceAnim);
			writer.println("animSmoothLighting=" + FBP.animSmoothLighting);
			writer.println("smartBreaking=" + FBP.smartBreaking);
			writer.println("lowTraction=" + FBP.lowTraction);
			writer.println("bounceOffWalls=" + FBP.bounceOffWalls);
			writer.println("showInMillis=" + FBP.showInMillis);
			writer.println("randomRotation=" + FBP.randomRotation);
			writer.println("cartoonMode=" + FBP.cartoonMode);
			writer.println("entityCollision=" + FBP.entityCollision);
			writer.println("randomizedScale=" + FBP.randomizedScale);
			writer.println("randomFadingSpeed=" + FBP.randomFadingSpeed);
			writer.println("spawnRedstoneBlockParticles=" + FBP.spawnRedstoneBlockParticles);
			writer.println("spawnWhileFrozen=" + FBP.spawnWhileFrozen);
			writer.println("infiniteDuration=" + FBP.infiniteDuration);
			writer.println("minAge=" + FBP.minAge);
			writer.println("maxAge=" + FBP.maxAge);
			writer.println("scaleMult=" + FBP.scaleMult);
			writer.println("gravityMult=" + FBP.gravityMult);
			writer.print("rotationMult=" + FBP.rotationMult);
			writer.close();
		} catch (Exception e)
		{
			closeStreams();

			if (!FBP.config.exists())
			{
				if (!Paths.get(FBP.config.getParent()).toFile().exists())
					Paths.get(FBP.config.getParent()).toFile().mkdirs();

				try
				{
					FBP.config.createNewFile();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}

			write();
		}
	}

	public static void writeAnimBlacklist()
	{
		try
		{
			PrintWriter writer = new PrintWriter(FBP.animBlacklistFile.getPath(), "UTF-8");

			for (String ex : FBP.INSTANCE.blockAnimBlacklist)
				writer.println(ex);

			writer.close();
		} catch (Exception e)
		{
			closeStreams();

			if (!FBP.animBlacklistFile.exists())
			{
				if (!Paths.get(FBP.animBlacklistFile.getParent()).toFile().exists())
					Paths.get(FBP.animBlacklistFile.getParent()).toFile().mkdirs();

				try
				{
					FBP.animBlacklistFile.createNewFile();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}

	public static void writeParticleBlacklist()
	{
		try
		{
			PrintWriter writer = new PrintWriter(FBP.particleBlacklistFile.getPath(), "UTF-8");

			for (String ex : FBP.INSTANCE.blockParticleBlacklist)
				writer.println(ex);

			writer.close();
		} catch (Exception e)
		{
			closeStreams();

			if (!FBP.particleBlacklistFile.exists())
			{
				if (!Paths.get(FBP.particleBlacklistFile.getParent()).toFile().exists())
					Paths.get(FBP.particleBlacklistFile.getParent()).toFile().mkdirs();

				try
				{
					FBP.particleBlacklistFile.createNewFile();
				} catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}
	}

	static void writeFloatingMaterials()
	{
		try
		{
			PrintWriter writer = new PrintWriter(FBP.floatingMaterialsFile.getPath(), "UTF-8");

			Field[] materials = Material.class.getDeclaredFields();

			for (Field f : materials)
			{
				String fieldName = f.getName();

				if (f.getType() == Material.class)
				{
					String translated = FBPObfUtil.translateObfMaterialName(fieldName).toLowerCase();
					try
					{
						Material mat = (Material) f.get(null);
						if (mat == Material.AIR)
							continue;

						boolean flag = FBP.INSTANCE.doesMaterialFloat(mat);

						writer.println(translated + "=" + flag);
					} catch (Exception ex)
					{

					}
				}
			}

			writer.close();
		} catch (Exception e)
		{
			closeStreams();
		}
	}

	static void read()
	{
		try
		{
			fis = new FileInputStream(FBP.config);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;

			while ((line = br.readLine()) != null)
			{
				line = line.replaceAll(" ", "");

				if (line.contains("enabled="))
					FBP.enabled = Boolean.valueOf(line.replace("enabled=", ""));
				else if (line.contains("weatherParticleDensity="))
					FBP.weatherParticleDensity = Double.valueOf(line.replace("weatherParticleDensity=", ""));
				else if (line.contains("particlesPerAxis="))
					FBP.particlesPerAxis = Integer.valueOf(line.replace("particlesPerAxis=", ""));
				else if (line.contains("restOnFloor="))
					FBP.restOnFloor = Boolean.valueOf(line.replace("restOnFloor=", ""));
				else if (line.contains("waterPhysics="))
					FBP.waterPhysics = Boolean.valueOf(line.replace("waterPhysics=", ""));
				else if (line.contains("fancyFlame="))
					FBP.fancyFlame = Boolean.valueOf(line.replace("fancyFlame=", ""));
				else if (line.contains("fancySmoke="))
					FBP.fancySmoke = Boolean.valueOf(line.replace("fancySmoke=", ""));
				else if (line.contains("fancyRain="))
					FBP.fancyRain = Boolean.valueOf(line.replace("fancyRain=", ""));
				else if (line.contains("fancySnow="))
					FBP.fancySnow = Boolean.valueOf(line.replace("fancySnow=", ""));
				else if (line.contains("spawnPlaceParticles="))
					FBP.spawnPlaceParticles = Boolean.valueOf(line.replace("spawnPlaceParticles=", ""));
				else if (line.contains("fancyPlaceAnim="))
					FBP.fancyPlaceAnim = Boolean.valueOf(line.replace("fancyPlaceAnim=", ""));
				else if (line.contains("animSmoothLighting="))
					FBP.animSmoothLighting = Boolean.valueOf(line.replace("animSmoothLighting=", ""));
				else if (line.contains("smartBreaking="))
					FBP.smartBreaking = Boolean.valueOf(line.replace("smartBreaking=", ""));
				else if (line.contains("lowTraction="))
					FBP.lowTraction = Boolean.valueOf(line.replace("lowTraction=", ""));
				else if (line.contains("bounceOffWalls="))
					FBP.bounceOffWalls = Boolean.valueOf(line.replace("bounceOffWalls=", ""));
				else if (line.contains("showInMillis="))
					FBP.showInMillis = Boolean.valueOf(line.replace("showInMillis=", ""));
				else if (line.contains("randomRotation="))
					FBP.randomRotation = Boolean.valueOf(line.replace("randomRotation=", ""));
				else if (line.contains("cartoonMode="))
					FBP.cartoonMode = Boolean.valueOf(line.replace("cartoonMode=", ""));
				else if (line.contains("entityCollision="))
					FBP.entityCollision = Boolean.valueOf(line.replace("entityCollision=", ""));
				else if (line.contains("randomFadingSpeed="))
					FBP.randomFadingSpeed = Boolean.valueOf(line.replace("randomFadingSpeed=", ""));
				else if (line.contains("randomizedScale="))
					FBP.randomizedScale = Boolean.valueOf(line.replace("randomizedScale=", ""));
				else if (line.contains("spawnWhileFrozen="))
					FBP.spawnWhileFrozen = Boolean.valueOf(line.replace("spawnWhileFrozen=", ""));
				else if (line.contains("spawnRedstoneBlockParticles="))
					FBP.spawnRedstoneBlockParticles = Boolean.valueOf(line.replace("spawnRedstoneBlockParticles=", ""));
				else if (line.contains("infiniteDuration="))
					FBP.infiniteDuration = Boolean.valueOf(line.replace("infiniteDuration=", ""));
				else if (line.contains("minAge="))
					FBP.minAge = Integer.valueOf(line.replace("minAge=", ""));
				else if (line.contains("maxAge="))
					FBP.maxAge = Integer.valueOf(line.replace("maxAge=", ""));
				else if (line.contains("scaleMult="))
					FBP.scaleMult = Double.valueOf(line.replace("scaleMult=", ""));
				else if (line.contains("gravityMult="))
					FBP.gravityMult = Double.valueOf(line.replace("gravityMult=", ""));
				else if (line.contains("rotationMult="))
					FBP.rotationMult = Double.valueOf(line.replace("rotationMult=", ""));
			}

			closeStreams();
		} catch (Exception e)
		{
			closeStreams();

			write();
		}
	}

	static void readAnimBlacklist()
	{
		try
		{
			fis = new FileInputStream(FBP.animBlacklistFile);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;

			FBP.INSTANCE.resetBlacklist(false);

			while ((line = br.readLine()) != null && !(line = line.replaceAll(" ", "").toLowerCase()).equals(""))
				FBP.INSTANCE.addToBlacklist(line, false);
		} catch (Exception e)
		{

		}

		closeStreams();
	}

	static void readParticleBlacklist()
	{
		try
		{
			fis = new FileInputStream(FBP.particleBlacklistFile);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;

			FBP.INSTANCE.resetBlacklist(true);

			while ((line = br.readLine()) != null && !(line = line.replaceAll(" ", "").toLowerCase()).equals(""))
				FBP.INSTANCE.addToBlacklist(line, true);
		} catch (Exception e)
		{

		}

		closeStreams();
	}

	static void readFloatingMaterials()
	{
		try
		{
			fis = new FileInputStream(FBP.floatingMaterialsFile);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;

			FBP.INSTANCE.floatingMaterials.clear();

			Field[] materials = Material.class.getDeclaredFields();

			while ((line = br.readLine()) != null)
			{
				line = line.trim().toLowerCase();

				String[] split = line.split("=");

				if (split.length < 2)
					continue;

				String materialName = split[0].replace("_", "");
				boolean flag = Boolean.parseBoolean(split[1]);

				if (!flag)
					continue;

				boolean found = false;

				for (Field f : materials)
				{
					String fieldName = f.getName();

					if (f.getType() == Material.class)
					{
						String translated = FBPObfUtil.translateObfMaterialName(fieldName).toLowerCase().replace("_",
								"");

						if (materialName.equals(translated))
						{
							try
							{
								Material mat = (Material) f.get(null);

								if (!FBP.INSTANCE.floatingMaterials.contains(mat))
									FBP.INSTANCE.floatingMaterials.add(mat);

								found = true;
								break;
							} catch (Exception ex)
							{

							}
						}
					}
				}

				if (!found)
					System.out.println("[FBP]: Material not recognized: " + materialName);
			}

			closeStreams();
		} catch (Exception e)
		{
			closeStreams();

			write();
		}
	}

	static void closeStreams()
	{
		try
		{
			br.close();
			isr.close();
			fis.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void defaults(boolean write)
	{
		FBP.minAge = 10;
		FBP.maxAge = 55;
		FBP.scaleMult = 0.75;
		FBP.gravityMult = 1.0;
		FBP.rotationMult = 1.0;
		FBP.particlesPerAxis = 4;
		FBP.weatherParticleDensity = 1.0;
		FBP.lowTraction = false;
		FBP.bounceOffWalls = true;
		FBP.randomRotation = true;
		FBP.cartoonMode = false;
		FBP.entityCollision = false;
		FBP.randomizedScale = true;
		FBP.randomFadingSpeed = true;
		FBP.spawnRedstoneBlockParticles = false;
		FBP.infiniteDuration = false;
		FBP.spawnWhileFrozen = true;
		FBP.smartBreaking = true;
		FBP.fancyPlaceAnim = false;
		FBP.animSmoothLighting = false;
		FBP.spawnPlaceParticles = true;
		FBP.fancyRain = true;
		FBP.fancySnow = true;
		FBP.fancySmoke = true;
		FBP.fancyFlame = true;
		FBP.waterPhysics = true;
		FBP.restOnFloor = true;

		if (write)
			write();
	}
}
