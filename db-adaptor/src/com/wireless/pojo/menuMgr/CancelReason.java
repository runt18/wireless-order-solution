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
		if(pt == null)
			return;
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
	
	/**
	 * 
	 * @param pojo
	 * @param clazz
	 * @return
	 */
	public static Object changeToOther(CancelReason pojo, Class<?> clazz){
		Object obj = null;
		if(clazz.equals(com.wireless.protocol.CancelReason.class)){
			com.wireless.protocol.CancelReason pt = new com.wireless.protocol.CancelReason();
			pt.setId(pojo.getId());
			pt.setRestaurantId(pojo.getRestaurantID());
			pt.setReason(pojo.getReason());
			obj = pt;
		}
		return obj;
	}
}
