package com.wireless.pojo.system;

public class Restaurant {
	
	private int restaurantID;				// 餐厅编号
	private String name;		// 餐厅名称
	private String info;		// 餐厅公告
	private String account;		// 餐厅账号
	private long recordAlive;	// 
	private String address;		// 餐厅地址
	
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
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public long getRecordAlive() {
		return recordAlive;
	}
	public void setRecordAlive(long recordAlive) {
		this.recordAlive = recordAlive;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
}
