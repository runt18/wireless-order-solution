package com.wireless.protocol;

public class Region {
	
	public final static short REGION_1 = 0;
	public final static short REGION_2 = 1;
	public final static short REGION_3 = 2;
	public final static short REGION_4 = 3;
	public final static short REGION_5 = 4;
	public final static short REGION_6 = 5;
	public final static short REGION_7 = 6;
	public final static short REGION_8 = 7;
	public final static short REGION_9 = 8;
	public final static short REGION_10 = 9;	
	
	public short regionID = REGION_1;
	public String name;
	
	public Region(){
		
	}
	
	public Region(short regionID, String name){
		this.regionID = regionID;
		this.name = name;
	}
	
}
