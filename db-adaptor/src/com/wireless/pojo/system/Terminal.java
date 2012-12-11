package com.wireless.pojo.system;

public class Terminal {
	public final static short MODEL_BB = 0x0000; 
	public final static short MODEL_ANDROID = 0x0001;
	public final static short MODEL_STAFF = 0x00FF;
	public final static short MODEL_ADMIN = 0xFE;
	private int id;
	private long pin;
	private short modelID = Terminal.MODEL_STAFF;
	private int restaurantID;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getPin() {
		return pin;
	}
	public void setPin(long pin) {
		this.pin = pin;
	}
	public short getModelID() {
		return modelID;
	}
	public void setModelID(short modelID) {
		this.modelID = modelID;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	
}
