package com.wireless.protocol;

import com.wireless.util.NumericUtil;

public class StaffTerminal {
	//the id to this staff
	public long id;
	//the alias id to this staff
	public int aliasID;
	//the name to this staff
	public String name;
	//the password to this staff
	public String pwd;
	//the terminal pin this staff attached to
	public long pin;
	//the terminal id this staff attached to
	public long terminalID;
	//the gift quota represented as an integer
	int giftQuota = -1;
	
	public int type;
	
	public void setGiftQuota(Float quota){
		giftQuota = NumericUtil.float2Int(quota);
	}
	
	/**
	 * Get the gift quota.
	 * @return null if no quota limit, otherwise an Float object
	 */
	public Float getGiftQuota(){
		return giftQuota < 0 ? new Float(giftQuota) : NumericUtil.int2Float(giftQuota);
	}
	
	//the gift amount represented as an integer
	int giftAmount = 0;
	
	public void setGiftAmount(Float amount){
		giftAmount = NumericUtil.float2Int(amount);
	}
	
	public Float getGiftAmount(){
		return NumericUtil.int2Float(giftAmount);
	}
	
	public StaffTerminal(){
		
	}
	

}
