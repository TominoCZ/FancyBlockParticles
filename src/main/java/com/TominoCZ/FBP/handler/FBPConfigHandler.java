package com.TominoCZ.FBP.handler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import com.TominoCZ.FBP.FBP;

import scala.reflect.io.Directory;

public class FBPConfigHandler {
	static FileInputStream fis;
	static InputStreamReader isr;
	static BufferedReader br;

	static File f;

	public static void init() {
		try {
			f = FBP.config;

			defaults(false);
			
			if (!f.exists()) {
				if (!Directory.apply(f.getParent()).exists())
					Directory.apply(f.getParent()).createDirectory(true, false);

				f.createNewFile();

				write();
			}

			read();

			write();

			closeStreams();
		} catch (IOException e) {
			closeStreams();

			write();
		}
	}

	public static void write() {
		try {
			check();

			PrintWriter writer = new PrintWriter(f.getPath(), "UTF-8");
			writer.println("legacyMode=" + FBP.legacyMode);
			writer.println("spawnRedstoneBlockParticles=" + FBP.spawnRedstoneBlockParticles);
			writer.println("spawnWhileFrozen=" + FBP.spawnWhileFrozen);
			writer.println("minScale=" + FBP.minScale);
			writer.println("maxScale=" + FBP.maxScale);
			writer.println("minAge=" + FBP.minAge);
			writer.println("maxAge=" + FBP.maxAge);
			writer.println("gravityMult=" + FBP.gravityMult);
			writer.print("rotationMult=" + FBP.rotationMult);
			writer.close();
		} catch (Exception e) {
			closeStreams();

			if (!f.exists()) {
				if (!Directory.apply(f.getParent()).exists())
					Directory.apply(f.getParent()).createDirectory(true, false);

				try {
					f.createNewFile();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			write();
		}
	}

	static void read() {
		try {
			fis = new FileInputStream(f);
			isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
			br = new BufferedReader(isr);

			String line;

			while ((line = br.readLine()) != null) {
				if (line.contains("legacyMode="))
					FBP.legacyMode = Boolean.valueOf(line.replaceAll(" ", "").replace("legacyMode=", ""));
				else if (line.contains("spawnWhileFrozen="))
					FBP.spawnWhileFrozen = Boolean.valueOf(line.replaceAll(" ", "").replace("spawnWhileFrozen=", ""));
				else if (line.contains("spawnRedstoneBlockParticles="))
					FBP.spawnRedstoneBlockParticles = Boolean
							.valueOf(line.replaceAll(" ", "").replace("spawnRedstoneBlockParticles=", ""));
				else if (line.contains("minScale="))
					FBP.minScale = Double.valueOf(line.replaceAll(" ", "").replace("minScale=", ""));
				else if (line.contains("maxScale="))
					FBP.maxScale = Double.valueOf(line.replaceAll(" ", "").replace("maxScale=", ""));
				else if (line.contains("minAge="))
					FBP.minAge = Integer.valueOf(line.replaceAll(" ", "").replace("minAge=", ""));
				else if (line.contains("maxAge="))
					FBP.maxAge = Integer.valueOf(line.replaceAll(" ", "").replace("maxAge=", ""));
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
		FBP.maxAge = 25;
		FBP.minScale = 1.0;
		FBP.maxScale = 1.2;
		FBP.gravityMult = 1.0;
		FBP.rotationMult = 1.0;
		FBP.legacyMode = false;
		FBP.spawnRedstoneBlockParticles = true;
		FBP.spawnWhileFrozen = true;
		
		if (write)
			write();
	}

	public static void check() {
		FBP.minScale = Math.abs(FBP.minScale);
		FBP.maxScale = Math.abs(FBP.maxScale);
		FBP.minAge = Math.abs(FBP.minAge);
		FBP.maxAge = Math.abs(FBP.maxAge);
		FBP.gravityMult = Math.abs(FBP.gravityMult);
		FBP.rotationMult = Math.abs(FBP.rotationMult);

		if (FBP.minScale < 0.1D)
			FBP.minScale = 0.1D;
		if (FBP.maxScale > 2.0D)
			FBP.maxScale = 2.0D;
		else if (FBP.maxScale < 0.1D)
			FBP.maxScale = 0.1D;

		if (FBP.minAge < 1)
			FBP.minAge = 1;
		if (FBP.maxAge < 1)
			FBP.maxAge = 1;
		else if (FBP.maxAge > 50)
			FBP.maxAge = 50;

		if (FBP.gravityMult > 2.0D)
			FBP.gravityMult = 2.0D;
		else if (FBP.gravityMult < 0.1D)
			FBP.gravityMult = 0.1D;

		if (FBP.rotationMult > 1.5D)
			FBP.rotationMult = 1.5D;
		else if (FBP.rotationMult < 0)
			FBP.rotationMult = 0;

		// Final check
		if (FBP.minScale > FBP.maxScale)
			FBP.minScale = FBP.maxScale;

		if (FBP.minAge > FBP.maxAge)
			FBP.minAge = FBP.maxAge;
	}
}
