package com.wireless.protocol;

public class Util {
	
	public final static int INT_MASK_1 = 0x0000FF00;
	public final static int INT_MASK_2 = 0x00FFFF00;
	public final static int INT_MASK_3 = 0xFFFFFF00;
	
	/**
	 * Convert the price to string.
	 * Note that the value accurate to two decimal parts 
	 * and add the "￥" character in front of the converted string.<br>
	 * For example as below.<br>
	 * "1" shown as "￥1".<br>
	 * "1.1" shown as "￥1.10".<br>
	 * "1.23" shown as "￥1.23".<br>	 
	 * @param priceInt the price represented as an integer
	 * @param intMask indicates how many bytes are used to represent the integer part
	 * @return the converted string
	 */
	public static String price2String(int priceInt, int intMask){		
		String integer = new Integer((priceInt & intMask) >> 8).toString();
		String decimal = new Byte((byte)(priceInt & 0x000000FF)).toString();
		if((priceInt & 0x000000FF) < 10){
			return "￥" + integer + ".0" + decimal;			
		}else{
			return "￥" + integer + "." + decimal;			
		}
	}
	
	/**
	 * Convert the price to Float object
	 * @param priceInt the price of taste represented as an integer
	 * @param intMask indicates how many bytes are used to represent the integer part
	 * @return the Float object indicates the unit price of the ordered food
	 */
	public static Float price2Float(int priceInt, int intMask){
		return new Float(((priceInt & intMask) >> 8) + ((priceInt & 0x000000FF) * 0.01));
	}
}
