package com.wireless.pojo.system;

import java.text.SimpleDateFormat;

public class Shift {
	private int id;
	private int restaurantID;
	private String name;
	private long onDuft;
	private long offDuft;
	
	public String getOnDuftFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(onDuft);
	}
	public String getOffDuftFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDuft);
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
	public long getOnDuft() {
		return onDuft;
	}
	public void setOnDuft(long onDuft) {
		this.onDuft = onDuft;
	}
	public long getOffDuft() {
		return offDuft;
	}
	public void setOffDuft(long offDuft) {
		this.offDuft = offDuft;
	}
	
}
