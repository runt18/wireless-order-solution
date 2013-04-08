package com.wireless.pojo.system;

public class Region {
	private int regionID;
	private String regionName;
	private int restaurantID;
	
	public Region(){}
	
	public Region(int regionID, String regionName){
		this.regionID = regionID;
		this.regionName = regionName;
	}
	
	public Region(int regionID, String regionName, int restaurantID){
		this.regionID = regionID;
		this.regionName = regionName;
		this.restaurantID = restaurantID;
	}
	
	public int getRestaurantID() {
		return restaurantID;
	}
	
	public int getRegionID() {
		return regionID;
	}
	
	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}
	
	public String getRegionName() {
		return regionName;
	}
	
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	
	@Override
	public int hashCode(){
		return regionID * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Region)){
			return false;
		}else{
			return regionID == ((Region)obj).regionID;
		}
	}
	
	@Override
	public String toString(){
		return "region(id = " + regionID + ", name = " + regionName + ")";
	}
	
}
