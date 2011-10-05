package com.wireless.protocol;

public class SKitchen {
	
	public final static short S_KITCHEN_1 = 0;
	public final static short S_KITCHEN_2 = 1;
	public final static short S_KITCHEN_3 = 2;
	public final static short S_KITCHEN_4 = 3;
	public final static short S_KITCHEN_5 = 4;
	public final static short S_KITCHEN_6 = 5;
	public final static short S_KITCHEN_7 = 6;
	public final static short S_KITCHEN_8 = 7;
	public final static short S_KITCHEN_9 = 8;
	public final static short S_KITCHEN_10 = 9;
	
	public short alias_id;
	public String name;
	
	public SKitchen(String name, short alias_id){
		this.name = name;
		this.alias_id = alias_id;
	}
}
