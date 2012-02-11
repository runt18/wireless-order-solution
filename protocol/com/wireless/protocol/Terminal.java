package com.wireless.protocol;


public class Terminal { 
	
	/**
	 * The model id to the terminal
	 */
	public final static short MODEL_BB = 0x0000; 
	public final static short MODEL_ANDROID = 0x0001;
	public final static short MODEL_STAFF = 0x00FF;
	
	//the id to this terminal
	public long id = 0;
	//the restaurant id this terminal is attached to
	public int restaurant_id = 0;
	//the pin to this terminal
	public long pin = 0;
	//the model id to this terminal
	public short modelID = MODEL_BB;
	//the model name to this terminal
	public String modelName = null;
	//the owner name to this terminal
	public String owner = null;
	//the expired date to this terminal
	//this value can be null, means never expire
	public java.util.Date expireDate = null;
	//the gift quota represented as an integer
	int giftQuota = -1;
	
	public void setGiftQuota(Float quota){
		giftQuota = Util.float2Int(quota);
	}
	
	/**
	 * Get the gift quota.
	 * @return null if no quota limit, otherwise an Float object
	 */
	public Float getGiftQuota(){
		return giftQuota < 0 ? new Float(giftQuota) : Util.int2Float(giftQuota);
	}
	
	//the gift amount represented as an integer
	int giftAmount = 0;
	
	public void setGiftAmount(Float amount){
		giftAmount = Util.float2Int(amount);
	}
	
	public Float getGiftAmount(){
		return Util.int2Float(giftAmount);
	}
	
}
