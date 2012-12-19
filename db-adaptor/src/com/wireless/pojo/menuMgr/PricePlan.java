package com.wireless.pojo.menuMgr;

public class PricePlan {
	public static final short STATUS_ACTIVITY = 1;
	public static final short STATUS_NORMAL = 0;
	public static final String STATUS_ACTIVITY_TEXT = "活动";
	public static final String STATUS_NORMAL_TEXT = "普通";
	
	private int id;
	private int restaurantID;
	private String name;
	private short status;
	
	public PricePlan(){
		this.status = PricePlan.STATUS_NORMAL;
	}
	public PricePlan(int restaurantID, int id, String name, short status){
		this.id = id;
		this.restaurantID = restaurantID;
		this.name = name;
		this.status = status;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public short getStatus() {
		return status;
	}
	public void setStatus(short status) {
		this.status = status;
	}
	
	
}
