package com.wireless.pojo.util;

public final class NumericUtil {
	
	public final static String CURRENCY_SIGN = "￥";
	
	/**
	 * Convert the float to string.
	 * Note that the value accurate to two decimal parts.<br>
	 * For example as below.<br>
	 * "1" shown as "1.00".<br>
	 * "1.1" shown as "1.10".<br>
	 * "1.23" shown as "1.23".<br>
	 * @param floatValue the float value
	 * @return the result string
	 */
	public static String float2String(float floatValue){
		return int2String(float2Int(floatValue));
	}
	
	/**
	 * Convert the float to string.
	 * Note that the value accurate to two decimal parts.<br>
	 * For example as below.<br>
	 * "1" shown as "1".<br>
	 * "1.1" shown as "1.1".<br>
	 * "1.23" shown as "1.23".<br>
	 * @param floatValue the float value
	 * @return the result string
	 */
	public static String float2String2(float floatValue){
		return int2String2(float2Int(floatValue));
	}
	
	/**
	 * Convert the integer to string.
	 * Note that the value accurate to two decimal parts.<br>
	 * For example as below.<br>
	 * "1" shown as "1.00".<br>
	 * "1.1" shown as "1.10".<br>
	 * "1.23" shown as "1.23".<br>
	 * @param intValue the int value
	 * @return the result string
	 */
	static String int2String(int intValue){
		int integer = intValue / 100;
		int decimal = intValue % 100;
		if(decimal < 10){
			return integer + ".0" + decimal;			
		}else{
			return integer + "." + decimal;			
		}
	}
	
	/**
	 * Convert the integer to string.
	 * Note that the value accurate to two decimal parts.<br>
	 * For example as below.<br>
	 * "1" shown as "1".<br>
	 * "1.1" shown as "1.1".<br>
	 * "1.23" shown as "1.23".<br>
	 * @param intValue the int value
	 * @return the result string
	 */
	static String int2String2(int intValue){
		int integer = intValue / 100;
		int decimal = intValue % 100;
		if(decimal == 0){
			return Integer.toString(integer);
			
		}else if(decimal < 10){
			return integer + ".0" + decimal;
			
		}else if(decimal % 10 == 0){
			return integer + "." + (decimal / 10); 
			
		}else{
			return integer + "." + decimal;			
		}
	}
	
	/**
	 * Convert the int to Float.
	 * value = integer * 100 + decimal
	 * e.g. 255.50 would be represented as 25550.
	 * @param intValue the int value
	 * @return the float object
	 */
	public static Float int2Float(int intValue){
		return new Float((float)intValue / 100);
	}
	
	/**
	 * Convert the int to Double.
	 * value = integer * 100 + decimal
	 * e.g. 255.50 would be represented as 25550.
	 * @param intValue the int value
	 * @return the double object
	 */
	public static Double int2Double(int intValue){
		return new Double((double)intValue / 100);
	}
	
	/**
	 * Convert the Float to int.
	 * The integer value is calculated as below.
	 * value = integer * 100 + decimal
	 * e.g. 255.50 would be represented as 25550.
	 * @param value the Float value
	 * @return the int value
	 */
	public static int float2Int(float value){
		return Math.round(value * 100);
	}
	
	
}
