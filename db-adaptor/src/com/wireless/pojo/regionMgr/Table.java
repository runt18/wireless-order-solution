package com.wireless.pojo.regionMgr;

import com.wireless.protocol.PTable;

public class Table {
	
	public static enum Status{
		IDLE(0, "空闲"),
		BUSY(1, "就餐");
		
		private final int val;
		private final String desc;
		
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "status(" +
				   "val = " + val + 
				   ", desc = " + desc + ")";
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The table status(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
	}
	
	private int tableID;
	private int tableAlias;
	private int restaurantID;
	private String tableName;
	private float mimnmuCost;
	private int customNum;
	private int category;
	private Status status;
	private float serviceRate;
	private Region region;
	
	public Table(){
		
	}
	
	public Table(PTable protocolObj){
		copyFrom(protocolObj);
	}
	
	public final void copyFrom(PTable protocolObj){
		setTableID(protocolObj.getTableId());
		setTableAlias(protocolObj.getAliasId());
		setRestaurantID(protocolObj.getRestaurantId());
		setTableName(protocolObj.getName());
		setMimnmuCost(protocolObj.getMinimumCost());
		setCustomNum(protocolObj.getCustomNum());
		setCategory(protocolObj.getCategory());
		setStatus(protocolObj.getStatus());
		setServiceRate(protocolObj.getServiceRate());
		this.region = new Region(protocolObj.getRegionId(), "");
	}
	
	public PTable toProtocol(){
		PTable protocolObj = new PTable();
		
		protocolObj.setTableId(getTableID());
		protocolObj.setAliasId(getTableAlias());
		protocolObj.setRestaurantId(getRestaurantID());
		protocolObj.setName(getTableName());
		protocolObj.setMinimumCost(getMinimumCost());
		protocolObj.setCustomNum(getCustomNum());
		protocolObj.setCategory((short)getCategory());
		protocolObj.setStatus((short)getStatus().getVal());
		protocolObj.setServiceRate(getServiceRate());
		protocolObj.setRegionId(getRegion().getId());
		
		return protocolObj;
	}
	
	public int getTableID() {
		return tableID;
	}
	
	public void setTableID(int tableID) {
		this.tableID = tableID;
	}
	
	public int getTableAlias() {
		return tableAlias;
	}
	
	public void setTableAlias(int tableAlias) {
		this.tableAlias = tableAlias;
	}
	
	public int getRestaurantID() {
		return restaurantID;
	}
	
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public float getMinimumCost() {
		return mimnmuCost;
	}
	
	public void setMimnmuCost(float mimnmuCost) {
		this.mimnmuCost = mimnmuCost;
	}
	
	public int getCustomNum() {
		return customNum;
	}
	
	public void setCustomNum(int customNum) {
		this.customNum = customNum;
	}
	
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
		this.category = category;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(int statusVal) {
		this.status = Status.valueOf(statusVal);
	}
	
	public float getServiceRate() {
		return serviceRate;
	}
	
	public void setServiceRate(float serviceRate) {
		this.serviceRate = serviceRate;
	}
	
	public Region getRegion() {
		return region;
	}
	
	public void setRegion(Region region) {
		this.region = region;
	}

	@Override
	public String toString(){
		return "table(" +
			   "id = " + getTableID() + 
			   ", alias_id = " + getTableAlias() +
			   ", restaurant_id = " + getRestaurantID() +
			   ", name = " + (tableName != null ? tableName : "") + ")";
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + getTableAlias();
		result = result * 31 + getRestaurantID();
		return result;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Table)){
			return false;
		}else{
			return getTableAlias() == ((Table)obj).getTableAlias() && getRestaurantID() == ((Table)obj).getRestaurantID();
		}
	}
}
