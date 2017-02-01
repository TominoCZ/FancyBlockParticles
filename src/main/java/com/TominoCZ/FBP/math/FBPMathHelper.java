package com.TominoCZ.FBP.math;

import java.util.ArrayList;

import net.minecraft.util.math.MathHelper;

public class FBPMathHelper {
	static ArrayList<double[]> newVec = new ArrayList();

	static double[] cube;

	static double sinAngleX;;
	static double sinAngleY;
	static double sinAngleZ;

	static double cosAngleX;
	static double cosAngleY;
	static double cosAngleZ;

	static float radsX;
	static float radsY;
	static float radsZ;

	public static ArrayList<double[]> rotateCubeXYZ(double AngleX, double AngleY, double AngleZ, double size) {
		double center = (size / 2);

		cube = new double[] { center, -center, center, center, center, center, -center, center, center, -center,
				-center, center, -center, -center, -center, -center, center, -center, center, center, -center, center,
				-center, -center, -center, -center, center, -center, center, center, -center, center, -center, -center,
				-center, -center, center, -center, -center, center, center, -center, center, center, center, center,
				-center, center, -center, center, -center, -center, center, center, center, center, center, center,
				center, -center, -center, -center, center, -center, -center, -center, center, -center, -center, center,
				-center, center };

		radsX = (float) Math.toRadians(AngleX);
		radsY = (float) Math.toRadians(AngleY);
		radsZ = (float) Math.toRadians(AngleZ);

		sinAngleX = MathHelper.sin(radsX);
		sinAngleY = MathHelper.sin(radsY);
		sinAngleZ = MathHelper.sin(radsZ);

		cosAngleX = MathHelper.cos(radsX);
		cosAngleY = MathHelper.cos(radsY);
		cosAngleZ = MathHelper.cos(radsZ);

		newVec.clear();

		for (int i = 0; i < cube.length; i += 3) {
			double[] d = { cube[i], cube[i + 1] * cosAngleX - cube[i + 2] * sinAngleX,
					cube[i + 1] * sinAngleX + cube[i + 2] * cosAngleX };
			d = new double[] { d[0] * cosAngleY + d[2] * sinAngleY, d[1], d[0] * sinAngleY - d[2] * cosAngleY };
			d = new double[] { d[0] * cosAngleZ - d[1] * sinAngleZ, d[0] * sinAngleZ + d[1] * cosAngleZ, d[2] };

			newVec.add(d);
		}

		return newVec;
	}

	public static double round(double d) {
		int i = (int) Math.round(d * 10);
		return ((double) i) / 10;
	}

	public static double add(double d, double add) {
		double _d = d;

		if (d < 0)
			_d -= add;
		else
			_d += add;

		return _d;
	}
}
