package com.TominoCZ.FBP.math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.math.MathHelper;

public class FBPMathHelper {
	public static List<double[]> rotateCubeXYZ(double AngleX, double AngleY, double AngleZ, double size) {
		List<double[]> Vec = Arrays.asList(new double[][]{ 
			{ (size / 2), -(size / 2), (size / 2) },
			{ (size / 2), (size / 2), (size / 2) },
			{ -(size / 2), (size / 2), (size / 2) }, 
			{ -(size / 2), -(size / 2), (size / 2) },

			{ -(size / 2), -(size / 2), -(size / 2) },
			{ -(size / 2), (size / 2), -(size / 2) },
			{ (size / 2), (size / 2), -(size / 2) },
			{ (size / 2), -(size / 2), -(size / 2) },

			{ -(size / 2), -(size / 2), (size / 2) },
			{ -(size / 2), (size / 2), (size / 2) },
			{ -(size / 2), (size / 2), -(size / 2) },
			{ -(size / 2), -(size / 2), -(size / 2) },

			{ (size / 2), -(size / 2), -(size / 2) },
			{ (size / 2), (size / 2), -(size / 2) },
			{ (size / 2), (size / 2), (size / 2) }, 
			{ (size / 2), -(size / 2), (size / 2) },

			{ -(size / 2), (size / 2), -(size / 2) },
			{ -(size / 2), (size / 2), (size / 2) },
			{ (size / 2), (size / 2), (size / 2) }, 
			{ (size / 2), (size / 2), -(size / 2) },

			{ -(size / 2), -(size / 2), (size / 2) },
			{ -(size / 2), -(size / 2), -(size / 2) },
			{ (size / 2), -(size / 2), -(size / 2) },
			{ (size / 2), -(size / 2), (size / 2) } });
		
		ArrayList<double[]> newVec = new ArrayList<double[]>();
		
		float[] XYZ = { 
				(float) Math.toRadians(AngleX),
				(float) Math.toRadians(AngleY),
				(float) Math.toRadians(AngleZ) };

		double sinAngleX = MathHelper.sin(XYZ[0]);
		double sinAngleY = MathHelper.sin(XYZ[1]);
		double sinAngleZ = MathHelper.sin(XYZ[2]);
		
		double cosAngleX = MathHelper.cos(XYZ[0]);
		double cosAngleY = MathHelper.cos(XYZ[1]);
		double cosAngleZ = MathHelper.cos(XYZ[2]);

		if (sinAngleX + sinAngleY + sinAngleZ != 0){
			Vec.forEach(vec -> {
				double[] d = new double[] { 
							vec[0],
							vec[1] * cosAngleX - vec[2] * sinAngleX,
							vec[1] * sinAngleX + vec[2] * cosAngleX };
	
				d = new double[] { 
							d[0] * cosAngleY + d[2] * sinAngleY, 
							d[1],
							d[0] * sinAngleY - d[2] * cosAngleY };
	
				d = new double[] {
							d[0] * cosAngleZ - d[1] * sinAngleZ,
							d[0] * sinAngleZ + d[1] * cosAngleZ,
							d[2] };
				
				newVec.add(d);
			});
			/*
			for (int i = 0; i < newVec.length; i++) {
				newVec[i] = new double[] { 
				 newVec[i][0],
				 newVec[i][1] * cosAngleX - newVec[i][2] * sinAngleX,
				 newVec[i][1] * sinAngleX + newVec[i][2] * cosAngleX };

				newVec[i] = new double[] { 
				 newVec[i][0] * cosAngleY + newVec[i][2] * sinAngleY,
				 newVec[i][1],
				 newVec[i][0] * sinAngleY - newVec[i][2] * cosAngleY };

				newVec[i] = new double[] {
				 newVec[i][0] * cosAngleZ - newVec[i][1] * sinAngleZ,
				 newVec[i][0] * sinAngleZ + newVec[i][1] * cosAngleZ,
				  newVec[i][2] };
			}*/
		}
		
		return newVec;
	}

	public static double round(double d) {
		int i = (int) Math.round(d * 10);
		return ((double) i) / 10;
	}

	public static double add(double d, double add) {
		return d < 0 ? d - add : d + add;
	}
}
