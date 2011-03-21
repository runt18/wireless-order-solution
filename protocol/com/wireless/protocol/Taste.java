package com.wireless.protocol;

public class Taste {
	
	public final static short NO_TASTE = 0;
	
	public short alias_id;
	public String preference;
	
	public Taste(short id, String pref){
		alias_id = id;
		preference = pref;
	}
}
