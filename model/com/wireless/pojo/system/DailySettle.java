package com.wireless.pojo.system;

import com.wireless.pojo.util.DateUtil;

public class DailySettle {
	private int id;
	private int restaurantID;
	private String name;
	private long onDuty;
	private long offDuty;
	
	public String getOnDutyFormat() {
		return DateUtil.format(onDuty);
	}
	public String getOffDutyFormat() {
		return DateUtil.format(offDuty);
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
	public long getOnDuty() {
		return onDuty;
	}
	public void setOnDuty(long onDuty) {
		this.onDuty = onDuty;
	}
	public long getOffDuty() {
		return offDuty;
	}
	public void setOffDuty(long offDuty) {
		this.offDuty = offDuty;
	}

}
