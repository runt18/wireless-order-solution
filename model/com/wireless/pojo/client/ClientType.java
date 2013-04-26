package com.wireless.pojo.client;

public class ClientType {
	
	private int typeID;
	private String name;
	private int parentID;
	private int restaurantID;
	
	public ClientType(){}
	
	public ClientType(int typeID, String name, int parentID, int restaurantID){
		this.typeID = typeID;
		this.name = name;
		this.parentID = parentID;
		this.restaurantID = restaurantID;
	}
	
	public int getTypeID() {
		return typeID;
	}
	public void setTypeID(int typeID) {
		this.typeID = typeID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getParentID() {
		return parentID;
	}
	public void setParentID(int parentID) {
		this.parentID = parentID;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	
}
