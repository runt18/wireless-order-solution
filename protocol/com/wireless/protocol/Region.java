package com.wireless.protocol;

public class Region {
	
	public final static short REGION_1 = 0;
	public final static short REGION_2 = 1;
	public final static short REGION_3 = 0;
	public final static short REGION_4 = 0;
	public final static short REGION_5 = 0;
	public final static short REGION_6 = 0;
	public final static short REGION_7 = 0;
	public final static short REGION_8 = 0;
	public final static short REGION_9 = 0;
	public final static short REGION_10 = 0;	
	
	public short regionID = REGION_1;
	public String name;
	
	public Region(short regionID, String name){
		this.regionID = regionID;
		this.name = name;
	}
	
}
