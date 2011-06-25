package com.wireless.protocol;

public class Util {
	
	/**
	 * Convert the price to string.
	 * Note that the value accurate to two decimal parts 
	 * and add the "￥" character in front of the converted string.<br>
	 * For example as below.<br>
	 * "1" shown as "￥1.00".<br>
	 * "1.1" shown as "￥1.10".<br>
	 * "1.23" shown as "￥1.23".<br>	 
	 * @param priceInt the price represented as an integer
	 * @param intMask indicates how many bytes are used to represent the integer part
	 * @return the converted string
	 */
	public static String price2String(int priceInt){		
		return "￥" + int2String(priceInt);
	}
	
	/**
	 * Convert the integer to string.
	 * Note that the value accurate to two decimal parts.<br>
	 * For example as below.<br>
	 * "1" shown as "1.00".<br>
	 * "1.1" shown as "1.10".<br>
	 * "1.23" shown as "1.23".<br>
	 * @param intValue the int value
	 * @return the string
	 */
	public static String int2String(int intValue){
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
	 * @return the string
	 */
	public static String int2String2(int intValue){
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
	 * Since the float data type does NOT support for BlackBerry 81xx serials,
	 * use an integer instead of float.
	 * The integer value is calculated as below.
	 * value = integer * 100 + decimal
	 * e.g. 255.50 would be represented as 25550.
	 * @param intValue the int value
	 * @return the Float object
	 */
	public static Float int2Float(int intValue){
		return new Float((float)intValue / 100);
	}
	
	/**
	 * Convert the Float to int.
	 * Since the float data type does NOT support for BlackBerry 81xx serials,
	 * use an integer instead of float.
	 * The integer value is calculated as below.
	 * value = integer * 100 + decimal
	 * e.g. 255.50 would be represented as 25550.
	 * @param floatValue the Float value
	 * @return the int value
	 */
	public static int float2Int(Float floatValue){
		String floatPoint = floatValue.toString();
		floatPoint = floatPoint.substring(floatPoint.indexOf(".") + 1);

		//make sure the count reserved two decimals 
		if(floatPoint.length() == 1){
			//in the case only the tenth digit exist,
			//append the "0" to the end 
			floatPoint = floatPoint + "0";
			
		}else if(floatPoint.length() == 2){
			//in the case the tenth digit is "0"
			//cut this tenth digit
			if(floatPoint.charAt(0) == '0'){
				floatPoint = floatPoint.substring(1);
			}
			
		}else{
			throw new NumberFormatException();
		}
		
		int decimal = Byte.parseByte(floatPoint);
		int integer = floatValue.intValue();
		return integer * 100 + decimal;
	}
}
