package com.wireless.protocol;

public class Taste {
	
	public final static short NO_TASTE = 0;
	public final static String NO_PREFERENCE = "无口味"; 
	
	public short alias_id;
	public String preference;
	/**
	 * The price to this taste preference.
	 * Here we use an integer to represent the unit price of the food.
	 * In Java, an integer is 4-byte long.
	 * And we use 3-byte to represent the value, as below
	 * 00 BB BB CC
	 * BB BB - 2-byte indicates the fixed-point, range from 0 through 65535
	 * CC - 1-byte indicates the float-point, range from 0 through 99
	 */
	public int price = 0;		
	
	public Taste(){
		alias_id = Taste.NO_TASTE;
		preference = Taste.NO_PREFERENCE;
	}
	
	public Taste(short id, String pref){
		alias_id = id;
		preference = pref;
	}
	
	public Taste(short id, String pref, int _price){
		this(id, pref);
		price = _price;
	}
	
	public Taste(short id, String pref, Float _price){
		this(id, pref);
		setPrice(_price);
	}
	
	/**
	 * Since the price is represented as an integer,
	 * and float data type is NOT supported under BlackBerry OS 4.5
	 * We use class Float instead of the primitive float type.
	 * @param _price the price to taste preference represented by Float
	 */
	public void setPrice(Float _price){
		//split the price into 3-byte value
		//get the float point from the price
		String floatPoint = _price.toString();
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
		
		byte decimal = Byte.parseByte(floatPoint);
		int integer = (int)_price.floatValue();
		price = 0x00FFFFFF & (((integer & 0x0000FFFF) << 8) | (decimal & 0x000000FF));
	}	

}
