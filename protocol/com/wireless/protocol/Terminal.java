package com.wireless.protocol;

public class Terminal { 
	
	/**
	 * The model id to the terminal
	 */
	public final static short MODEL_BB = 0x0000; 
	public final static short MODEL_ANDROID = 0x0001;
	public final static short MODEL_STAFF = 0x00FF;
	
	//the restaurant id this terminal is attached to
	public int restaurant_id = 0;
	//the pin to this terminal
	public int pin = 0;
	//the model id to this terminal
	public short modelID = MODEL_BB;
	//the model name to this terminal
	public String modelName = null;
	//the owner name to this terminal
	public String owner = null;
	//the expired date to this terminal
	//this value can be null, means never expire
	public java.util.Date expireDate = null;
}
