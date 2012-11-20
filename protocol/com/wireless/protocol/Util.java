package com.wireless.protocol;



public class Util {
	

	
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
	public static String float2String(Float floatValue){
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
	public static String float2String2(Float floatValue){
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
			
		}else if(floatPoint.length() >= 2){
			//in the case the tenth digit is "0"
			//cut this tenth digit
			if(floatPoint.charAt(0) == '0'){
				floatPoint = floatPoint.substring(1, 2);
			}else{
				floatPoint = floatPoint.substring(0, 2);
			}
			
		}else{
			throw new NumberFormatException();
		}
		
		int decimal = Byte.parseByte(floatPoint);
		int integer = floatValue.intValue();
		if(integer < 0){
			return -(Math.abs(integer) * 100 + decimal);
		}else{
			return integer * 100 + decimal;
		}
	}
	
	/**
	 * Generate the alias id to temporary food.
	 * @return the alias id to temporary food
	 */
	public static int genTempFoodID(){
		return (int)(System.currentTimeMillis() % 65535);
	}	

	
}
