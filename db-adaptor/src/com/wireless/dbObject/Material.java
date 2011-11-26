package com.wireless.dbObject;


public class Material {
	public long materialID;
	public int restaurantID;
	public int aliasID;
	public String name;
	
	public float warningThreshold;	
	
	public float dangerThreshold;
	
	public Material(long materialID){
		this.materialID = materialID;
	}
	
}
