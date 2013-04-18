package com.wireless.pojo.regionMgr;

import com.wireless.db.regionMgr.TableDao;
import com.wireless.protocol.PTable;

public class Table {
	
	/**
	 * The helper class to create the table object to perform insert {@link TableDao#insert)}
	 */
	public static class InsertBuilder{
		private final int tableAlias;
		private final int restaurantId;
		private final short regionId;
		
		private String tableName;
		private float serviceRate;
		private int miniCost;
		
		public InsertBuilder(int tableAlias, int restaurantId, short regionId){
			this.tableAlias = tableAlias;
			this.restaurantId = restaurantId;
			this.regionId = regionId;
		}

		public Table build(){
			return new Table(this);
		}
		
		public int getTableAlias() {
			return tableAlias;
		}

		public int getRestaurantId() {
			return restaurantId;
		}

		public short getRegionId() {
			return regionId;
		}

		public String getTableName() {
			if(tableName == null){
				tableName = "";
			}
			return tableName;
		}

		public InsertBuilder setTableName(String tableName) {
			this.tableName = tableName;
			return this;
		}

		public float getServiceRate() {
			return serviceRate;
		}

		public InsertBuilder setServiceRate(float serviceRate) {
			this.serviceRate = serviceRate;
			return this;
		}

		public int getMiniCost() {
			return miniCost;
		}

		public InsertBuilder setMiniCost(int miniCost) {
			this.miniCost = miniCost;
			return this;
		}
	}
	
	/**
	 * The helper class to create the table object used in update {@link TableDao#updateById}
	 */
	public static class UpdateBuilder{
		private final int tableId;
		
		private short regionId = Region.REGION_1;
		private String tableName;
		private int miniCost;
		private float serviceRate;
		
		public Table build(){
			return new Table(this);
		}
		
		public UpdateBuilder(int tableId){
			this.tableId = tableId;
		}

		public int getTableId() {
			return tableId;
		}

		public short getRegionId() {
			return regionId;
		}

		public UpdateBuilder setRegionId(short regionId) {
			this.regionId = regionId;
			return this;
		}

		public String getTableName() {
			if(tableName == null){
				tableName = "";
			}
			return tableName;
		}

		public UpdateBuilder setTableName(String name) {
			this.tableName = name;
			return this;
		}

		public int getMiniCost() {
			return miniCost;
		}

		public UpdateBuilder setMiniCost(int miniCost) {
			this.miniCost = miniCost;
			return this;
		}

		public float getServiceRate() {
			return serviceRate;
		}

		public UpdateBuilder setServiceRate(float serviceRate) {
			this.serviceRate = serviceRate;
			return this;
		}
	}
	
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
	
	private int tableId;
	private int tableAlias;
	private int restaurantId;
	private String tableName;
	private float mimnmuCost;
	private int customNum;
	private int category;
	private Status status = Status.IDLE;
	private float serviceRate;
	private Region region;
	
	public Table(){
		
	}
	
	private Table(InsertBuilder builder){
		setTableAlias(builder.getTableAlias());
		setRestaurantId(builder.getRestaurantId());
		setRegion(new Region(builder.getRegionId(), null));
		setMimnmuCost(builder.getMiniCost());
		setServiceRate(builder.getServiceRate());
		setTableName(builder.getTableName());
	}
	
	private Table(UpdateBuilder builder){
		setMimnmuCost(builder.getMiniCost());
		setRegion(new Region(builder.getRegionId(), null));
		setServiceRate(builder.getServiceRate());
		setTableId(builder.getTableId());
		setTableName(builder.getTableName());
	}
	
	public Table(PTable protocolObj){
		copyFrom(protocolObj);
	}
	
	public final void copyFrom(PTable protocolObj){
		setTableId(protocolObj.getTableId());
		setTableAlias(protocolObj.getAliasId());
		setRestaurantId(protocolObj.getRestaurantId());
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
		
		protocolObj.setTableId(getTableId());
		protocolObj.setAliasId(getTableAlias());
		protocolObj.setRestaurantId(getRestaurantId());
		protocolObj.setName(getTableName());
		protocolObj.setMinimumCost(getMinimumCost());
		protocolObj.setCustomNum(getCustomNum());
		protocolObj.setCategory((short)getCategory());
		protocolObj.setStatus((short)getStatus().getVal());
		protocolObj.setServiceRate(getServiceRate());
		protocolObj.setRegionId(getRegion().getId());
		
		return protocolObj;
	}
	
	public int getTableId() {
		return tableId;
	}
	
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	
	public int getTableAlias() {
		return tableAlias;
	}
	
	public void setTableAlias(int tableAlias) {
		this.tableAlias = tableAlias;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
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
		if(mimnmuCost < 0 || mimnmuCost > 65535){
			throw new IllegalArgumentException("The minimum cost(val = " + mimnmuCost + ") exceed the range.");
		}
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
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public void setStatus(int statusVal) {
		this.status = Status.valueOf(statusVal);
	}
	
	public float getServiceRate() {
		return serviceRate;
	}
	
	public void setServiceRate(float serviceRate) {
		if(serviceRate < 0 || serviceRate > 1){
			throw new IllegalArgumentException("The service rate (val = " + serviceRate + ") exceed range.");
		}
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
			   "id = " + getTableId() + 
			   ", alias_id = " + getTableAlias() +
			   ", restaurant_id = " + getRestaurantId() +
			   ", name = " + (tableName != null ? tableName : "") + ")";
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + getTableAlias();
		result = result * 31 + getRestaurantId();
		return result;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Table)){
			return false;
		}else{
			return getTableAlias() == ((Table)obj).getTableAlias() && getRestaurantId() == ((Table)obj).getRestaurantId();
		}
	}
}
