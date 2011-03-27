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
}
