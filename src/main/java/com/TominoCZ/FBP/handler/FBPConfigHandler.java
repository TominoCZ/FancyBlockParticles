package com.TominoCZ.FBP.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.TominoCZ.FBP.FBP;

import scala.reflect.io.Directory;

public class FBPConfigHandler {
	static FileInputStream fis;
	static InputStreamReader isr;
	static BufferedReader br;

	static File configFile;
	static File exceptionsFile;

	public static void init() {
		try {
			configFile = FBP.config;
			exceptionsFile = FBP.exceptionsFile;

			defaults(false);

			if (!configFile.exists()) {
				if (!Directory.apply(configFile.getParent()).exists())
					Directory.apply(configFile.getParent()).createDirectory(true, false);

				configFile.createNewFile();

				write();
			}

			if (!exceptionsFile.exists()) {
				if (!Directory.apply(exceptionsFile.getParent()).exists())
					Directory.apply(exceptionsFile.getParent()).createDirectory(true, false);

				exceptionsFile.createNewFile();

				writeExceptions();
			}

			read();
			readExceptions();

			write();
			writeExceptions();

			closeStreams();
		} catch (IOException e) {
			closeStreams();

			write();
		}
	}

	public static void write() {
		try {
			check();

			PrintWriter writer = new PrintWriter(configFile.getPath(), "UTF-8");
			writer.println("enabled=" + FBP.enabled);
			writer.println("fancyFlame=" + FBP.fancyFlame);
			writer.println("fancySmoke=" + FBP.fancySmoke);
			writer.println("fancyWeather=" + FBP.fancyWeather);
			writer.println("fancyPlaceAnim=" + FBP.fancyPlaceAnim);
			writer.println("smartBreaking=" + FBP.smartBreaking);
			writer.println("rollParticles=" + FBP.rollParticles);
			writer.println("bounceOffWalls=" + FBP.bounceOffWalls);
			writer.println("showInMillis=" + FBP.showInMillis);
			writer.println("randomRotation=" + FBP.randomRotation);
			writer.println("cartoonMode=" + FBP.cartoonMode);
			writer.println("entityCollision=" + FBP.entityCollision);
			writer.println("smoothTransitions=" + FBP.smoothTransitions);
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
		} catch (Exception e) {
			closeStreams();

			if (!configFile.exists()) {
				if (!Directory.apply(configFile.getParent()).exists())
					Directory.apply(configFile.getParent()).createDirectory(true, false);

				try {
					configFile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			write();
		}
	}

	public static void writeExceptions() {
		try {
			PrintWriter writer = new PrintWriter(exceptionsFile.getPath(), "UTF-8");
			for (int i = 0; i < FBP.blockExceptions.size() - 1; i++)
				writer.println(FBP.blockExceptions.get(i));

			writer.print(FBP.blockExceptions.get(FBP.blockExceptions.size() - 1));

			writer.close();
		} catch (Exception e) {
			closeStreams();

			if (!exceptionsFile.exists()) {
				if (!Directory.apply(exceptionsFile.getParent()).exists())
					Directory.apply(exceptionsFile.getParent()).createDirectory(true, false);

				try {
					exceptionsFile.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			write();
		}
	}

	static void read() {
		try {
			fis = new FileInputStream(configFile);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;

			while ((line = br.readLine()) != null) {
				if (line.contains("enabled="))
					FBP.enabled = Boolean.valueOf(line.replaceAll(" ", "").replace("enabled=", ""));
				else if (line.contains("fancyFlame="))
					FBP.fancyFlame = Boolean.valueOf(line.replaceAll(" ", "").replace("fancyFlame=", ""));
				else if (line.contains("fancySmoke="))
					FBP.fancySmoke = Boolean.valueOf(line.replaceAll(" ", "").replace("fancySmoke=", ""));
				else if (line.contains("fancyWeather="))
					FBP.fancyWeather = Boolean.valueOf(line.replaceAll(" ", "").replace("fancyWeather=", ""));
				else if (line.contains("fancyPlaceAnim="))
					FBP.fancyPlaceAnim = Boolean.valueOf(line.replaceAll(" ", "").replace("fancyPlaceAnim=", ""));
				else if (line.contains("smartBreaking="))
					FBP.smartBreaking = Boolean.valueOf(line.replaceAll(" ", "").replace("smartBreaking=", ""));
				else if (line.contains("rollParticles="))
					FBP.rollParticles = Boolean.valueOf(line.replaceAll(" ", "").replace("rollParticles=", ""));
				else if (line.contains("bounceOffWalls="))
					FBP.bounceOffWalls = Boolean.valueOf(line.replaceAll(" ", "").replace("bounceOffWalls=", ""));
				else if (line.contains("showInMillis="))
					FBP.showInMillis = Boolean.valueOf(line.replaceAll(" ", "").replace("showInMillis=", ""));
				else if (line.contains("randomRotation="))
					FBP.randomRotation = Boolean.valueOf(line.replaceAll(" ", "").replace("randomRotation=", ""));
				else if (line.contains("cartoonMode="))
					FBP.cartoonMode = Boolean.valueOf(line.replaceAll(" ", "").replace("cartoonMode=", ""));
				else if (line.contains("entityCollision="))
					FBP.entityCollision = Boolean.valueOf(line.replaceAll(" ", "").replace("entityCollision=", ""));
				else if (line.contains("randomFadingSpeed="))
					FBP.randomFadingSpeed = Boolean.valueOf(line.replaceAll(" ", "").replace("randomFadingSpeed=", ""));
				else if (line.contains("smoothTransitions="))
					FBP.smoothTransitions = Boolean.valueOf(line.replaceAll(" ", "").replace("smoothTransitions=", ""));
				else if (line.contains("spawnWhileFrozen="))
					FBP.spawnWhileFrozen = Boolean.valueOf(line.replaceAll(" ", "").replace("spawnWhileFrozen=", ""));
				else if (line.contains("spawnRedstoneBlockParticles="))
					FBP.spawnRedstoneBlockParticles = Boolean
							.valueOf(line.replaceAll(" ", "").replace("spawnRedstoneBlockParticles=", ""));
				else if (line.contains("infiniteDuration="))
					FBP.infiniteDuration = Boolean.valueOf(line.replaceAll(" ", "").replace("infiniteDuration=", ""));
				else if (line.contains("minAge="))
					FBP.minAge = Integer.valueOf(line.replaceAll(" ", "").replace("minAge=", ""));
				else if (line.contains("maxAge="))
					FBP.maxAge = Integer.valueOf(line.replaceAll(" ", "").replace("maxAge=", ""));

				else if (line.contains("scaleMult="))
					FBP.scaleMult = Double.valueOf(line.replaceAll(" ", "").replace("scaleMult=", ""));
				else if (line.contains("gravityMult="))
					FBP.gravityMult = Double.valueOf(line.replaceAll(" ", "").replace("gravityMult=", ""));
				else if (line.contains("rotationMult="))
					FBP.rotationMult = Double.valueOf(line.replaceAll(" ", "").replace("rotationMult=", ""));
			}

			closeStreams();

			check();
		} catch (Exception e) {
			closeStreams();

			check();

			write();
		}
	}

	static void readExceptions() {
		try {
			fis = new FileInputStream(exceptionsFile);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;

			List<Integer> toAdd = new ArrayList<Integer>();

			while ((line = br.readLine()) != null && !StringUtils.isEmpty(line)) {
				try {
					int ID = Integer.parseInt(line.replaceAll(" ", ""));
					toAdd.add(ID);
				} catch (Exception e) {

				}
			}

			if (toAdd.size() > 0) {
				FBP.blockExceptions.clear();
				FBP.blockExceptions.addAll(toAdd);
			}

			closeStreams();

			check();
		} catch (Exception e) {
			closeStreams();

			check();

			write();
		}
	}

	static void closeStreams() {
		try {
			br.close();
			isr.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void defaults(boolean write) {
		FBP.minAge = 10;
		FBP.maxAge = 65;
		FBP.scaleMult = 1.0;
		FBP.gravityMult = 1.0;
		FBP.rotationMult = 1.0;
		FBP.rollParticles = false;
		FBP.bounceOffWalls = true;
		FBP.randomRotation = true;
		FBP.cartoonMode = false;
		FBP.entityCollision = false;
		FBP.smoothTransitions = true;
		FBP.randomFadingSpeed = true;
		FBP.spawnRedstoneBlockParticles = false;
		FBP.infiniteDuration = false;
		FBP.spawnWhileFrozen = true;
		FBP.smartBreaking = true;
		FBP.fancyPlaceAnim = true;
		FBP.fancyWeather = true;
		FBP.fancySmoke = true;
		FBP.fancyFlame = true;
		
		FBP.blockExceptions.clear();
		FBP.blockExceptions.addAll(FBP.defaultBlockExceptions);

		if (write) {
			write();
			writeExceptions();
		}
	}

	public static void check() {
		FBP.scaleMult = Math.abs(FBP.scaleMult);
		FBP.minAge = Math.abs(FBP.minAge);
		FBP.maxAge = Math.abs(FBP.maxAge);
		FBP.gravityMult = Math.abs(FBP.gravityMult);
		FBP.rotationMult = Math.abs(FBP.rotationMult);

		if (FBP.minAge < 10)
			FBP.minAge = 10;
		if (FBP.maxAge < 10)
			FBP.maxAge = 10;
		else if (FBP.maxAge > 100)
			FBP.maxAge = 100;

		FBP.minAge = fix(FBP.minAge);
		FBP.maxAge = fix(FBP.maxAge);

		if (FBP.scaleMult > 1.25D)
			FBP.scaleMult = 1.25D;
		else if (FBP.scaleMult < 0.75D)
			FBP.scaleMult = 0.75D;

		if (FBP.gravityMult > 2.0D)
			FBP.gravityMult = 2.0D;
		else if (FBP.gravityMult < 0.5D)
			FBP.gravityMult = 0.5D;

		if (FBP.rotationMult > 1.5D)
			FBP.rotationMult = 1.5D;
		else if (FBP.rotationMult < 0)
			FBP.rotationMult = 0;

		// FINAL CHECK
		if (FBP.minAge > FBP.maxAge)
			FBP.minAge = FBP.maxAge;
	}

	private static int fix(int num) {
		for (int i = num; i > 0; i--) {
			if (i % 5 == 0)
				return i;
		}

		return num;
	}
}
