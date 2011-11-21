package com.wireless.protocol;

public class Material {
	public long materialID;
	public int restaurantID;
	public int aliasID;
	public String name;
	
	int warningThreshold;
	
	public void setWarning(Float _warning){
		warningThreshold = Util.float2Int(_warning);
	}
	
	public Float getWarning(){
		return Util.int2Float(warningThreshold);
	}
	
	int dangerThreshold;
	
	public void setDanger(Float _danger){
		dangerThreshold = Util.float2Int(_danger);
	}
	
	public Float getDanger(){
		return Util.int2Float(dangerThreshold);
	}
	
}
