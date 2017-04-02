package com.TominoCZ.FBP.math;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.math.MathHelper;

public class FBPMathHelper {
	public static List<double[]> rotateCubeXYZ(float AngleX, float AngleY, float AngleZ, double halfSize) {
		List<double[]> newVec = new ArrayList(24);

		double[] cube = new double[] { -halfSize, -halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize,
				halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, -halfSize, -halfSize, halfSize, halfSize,
				-halfSize, -halfSize, halfSize, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize,
				-halfSize, -halfSize, halfSize, -halfSize, -halfSize, halfSize, halfSize, -halfSize, -halfSize,
				halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, halfSize, -halfSize,
				halfSize, -halfSize, -halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, halfSize, -halfSize,
				halfSize, halfSize, -halfSize, halfSize, -halfSize, -halfSize, -halfSize, -halfSize, -halfSize,
				-halfSize, halfSize, halfSize, -halfSize, halfSize, halfSize, -halfSize, -halfSize };

		double sinAngleX = MathHelper.sin(AngleX);
		double sinAngleY = MathHelper.sin(AngleY);
		double sinAngleZ = MathHelper.sin(AngleZ);

		double cosAngleX = MathHelper.cos(AngleX);
		double cosAngleY = MathHelper.cos(AngleY);
		double cosAngleZ = MathHelper.cos(AngleZ);

		double[] d = new double[3];

		for (int i = 0; i < 72; i += 3) {
			d = new double[] { cube[i], cube[i + 1] * cosAngleX - cube[i + 2] * sinAngleX,
					cube[i + 1] * sinAngleX + cube[i + 2] * cosAngleX };

			d = new double[] { d[0] * cosAngleY + d[2] * sinAngleY, d[1], d[0] * sinAngleY - d[2] * cosAngleY };

			newVec.add(new double[] { d[0] * cosAngleZ - d[1] * sinAngleZ, d[0] * sinAngleZ + d[1] * cosAngleZ, d[2] });
		}

		return newVec;
	}

	public static double add(double d, double add) {
		if (d < 0.0D)
			return d - add;

		return d + add;
	}

	public static double abs(double d)
	{
		if (d < 0.0D)
			return -d;
		
		return d;
	}
	
	public static double round(double d, int decimals) {
		int i = (int) Math.round(d * Math.pow(10, decimals));
		return ((double) i) / Math.pow(10, decimals);
	}
}
