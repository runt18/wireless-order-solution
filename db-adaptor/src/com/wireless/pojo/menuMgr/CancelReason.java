package com.wireless.pojo.menuMgr;

public class CancelReason {
	private int id;
	private int restaurantID;
	private String reason;
	
	public CancelReason(){}
	public CancelReason(int restaurantID, int id, String reason){
		this.restaurantID = restaurantID;
		this.id = id;
		this.reason = reason;
	}
	public CancelReason(com.wireless.protocol.CancelReason pt){
		this.restaurantID = pt.getRestaurantId();
		this.id = pt.getId();
		this.reason = pt.getReason();
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
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
