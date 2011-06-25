package com.wireless.protocol;

public class Taste {
	
	public final static short NO_TASTE = 0;
	public final static String NO_PREFERENCE = "无口味"; 
	
	public short alias_id;
	public String preference;

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
	 * The price to this taste preference.
	 * Here we use an integer to represent the unit price of the food.
	 */
	int price = 0;		
	
	public void setPrice(Float _price){
		price = Util.float2Int(_price);
	}	

	public Float getPrice(){
		return Util.int2Float(price);
	}
}
