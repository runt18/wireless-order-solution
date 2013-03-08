package com.wireless.pojo.system;

public class Table {
	private int tableID;
	private int tableAlias;
	private int restaurantID;
	private String tableName;
	private float mimnmuCost;
	private int enabled;
	private int customNum;
	private int category;
	private int status;
	private float serviceRate;
	private Region region;
	
	public Table(){ }
	
	public Table(com.wireless.protocol.Table pt){
		this.tableID = pt.getTableId();
		this.tableAlias = pt.getAliasId();
		this.restaurantID = pt.getRestaurantId();
		this.tableName = pt.getName();
		this.mimnmuCost = pt.getMinimumCost();
		this.customNum = pt.getCustomNum();
		this.category = pt.getCategory();
		this.status = pt.getStatus();
		this.serviceRate = pt.getServiceRate();
		this.region = new Region(pt.regionID, "");//************
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
	public float getMimnmuCost() {
		return mimnmuCost;
	}
	public void setMimnmuCost(float mimnmuCost) {
		this.mimnmuCost = mimnmuCost;
	}
	public int getEnabled() {
		return enabled;
	}
	public void setEnabled(int enabled) {
		this.enabled = enabled;
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
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
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
	
	
	
}
