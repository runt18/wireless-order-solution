package com.wireless.dbObject;


public class Material {
	private long materialID;
	private int restaurantID;
	private int aliasID;
	private String name;
	
	private float warningThreshold;	
	
	private float dangerThreshold;
	
	public long getMaterialID() {
		return materialID;
	}

	public void setMaterialID(long materialID) {
		this.materialID = materialID;
	}

	public int getRestaurantID() {
		return restaurantID;
	}

	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}

	public int getAliasID() {
		return aliasID;
	}

	public void setAliasID(int aliasID) {
		this.aliasID = aliasID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getWarningThreshold() {
		return warningThreshold;
	}

	public void setWarningThreshold(float warningThreshold) {
		this.warningThreshold = warningThreshold;
	}

	public float getDangerThreshold() {
		return dangerThreshold;
	}

	public void setDangerThreshold(float dangerThreshold) {
		this.dangerThreshold = dangerThreshold;
	}

	public Material(long materialID){
		this.materialID = materialID;
	}
	
}
