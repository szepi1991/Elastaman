package com.szepesvari.david.elastaman;

public class Utils {

	public static void normalize(double[] toNorm) {
		double length = 0;
		for (double c : toNorm) {
			length += c*c;
		}
		length = Math.sqrt(length);
		for (double c : toNorm) {
			c /= length;
		}
	}
}
