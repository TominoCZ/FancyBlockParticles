package com.TominoCZ.FBP.util;

public class FBPMathUtil {
	public static double add(double d, double add) {
		if (d < 0.0D)
			return d - add;

		return d + add;
	}

	public static double round(double d, int decimals) {
		int i = (int) Math.round(d * Math.pow(10, decimals));
		return (i) / Math.pow(10, decimals);
	}

	public static int clamp(int num, int min, int max) {
		return num < min ? min : (num > max ? max : num);
	}

	public static double clamp(double num, double min, double max) {
		return num < min ? min : (num > max ? max : num);
	}

	public static float clamp(float num, float min, float max) {
		return num < min ? min : (num > max ? max : num);
	}
}
