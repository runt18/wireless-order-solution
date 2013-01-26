package com.wireless.protocol;

public class Math {
	
	public static int round(float value){
		return new Float(value).intValue();
	}
	
	public static int abs(int value){
		return java.lang.Math.abs(value);
	}
}
