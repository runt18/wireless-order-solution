package com.wireless.pojo.regionMgr;

import com.wireless.protocol.PRegion;

public class Region {
	
	private short id;
	private String name;
	private int restaurantId;
	
	public final static short REGION_1 = PRegion.REGION_1;
	public final static short REGION_2 = PRegion.REGION_2;
	public final static short REGION_3 = PRegion.REGION_3;
	public final static short REGION_4 = PRegion.REGION_4;
	public final static short REGION_5 = PRegion.REGION_5;
	public final static short REGION_6 = PRegion.REGION_6;
	public final static short REGION_7 = PRegion.REGION_7;
	public final static short REGION_8 = PRegion.REGION_8;
	public final static short REGION_9 = PRegion.REGION_9;
	public final static short REGION_10 = PRegion.REGION_10;	
	
	public Region(){}
	
	public Region(short id, String name){
		this.id = id;
		this.name = name;
	}
	
	public Region(short id, String name, int restaurantID){
		this.id = id;
		this.name = name;
		this.restaurantId = restaurantID;
	}
	
	public Region(PRegion protocolObj){
		copyFrom(protocolObj);
	}
	
	public final void copyFrom(PRegion protcolObj){
		setId(protcolObj.getRegionId());
		setName(protcolObj.getName());
		setRestaurantId(protcolObj.getRestaurantId());
	}
	
	public final PRegion toProtocol(){
		PRegion protocolObj = new PRegion();
		
		protocolObj.setRegionId((short)getId());
		protocolObj.setName(getName());
		protocolObj.setRestaurantId(getRestaurantId());
		
		return protocolObj;
	}
	
	public short getId() {
		return id;
	}
	
	public void setId(short id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantID) {
		this.restaurantId = restaurantID;
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Region)){
			return false;
		}else{
			return id == ((Region)obj).id && restaurantId == ((Region)obj).restaurantId;
		}
	}
	
	@Override
	public String toString(){
		return "region(" +
			   "id = " + id + 
			   ", restaurant_id = " + restaurantId +
			   ", name = " + (name != null ? name : "") + ")";
	}
	
}
