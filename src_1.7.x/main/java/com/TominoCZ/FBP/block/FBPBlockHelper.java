package com.TominoCZ.FBP.block;

import net.minecraft.block.Block;

public class FBPBlockHelper {
	public static boolean isModelValid(Block b) {
		int l = b.getRenderType();

		switch (l) {
		case 0:
		case 4:
		case 31:
		case 1:
		case 40:
		case 2:
		case 20:
		case 11:
		case 39:
		case 5:
		case 13:
		case 9:
		case 19:
		case 23:
		case 6:
		case 3:
		case 8:
		case 7:
		case 10:
		case 27:
		case 32:
		case 12:
		case 29:
		case 30:
		case 14:
		case 15:
		case 36:
		case 37:
		case 16:
		case 17:
		case 18:
		case 41:
		case 21:
		case 24:
		case 33:
		case 35:
		case 25:
		case 26:
		case 28:
		case 34:
		case 38:
			return true;
		case -1:
		default:
			return false;
		}
	}
}
