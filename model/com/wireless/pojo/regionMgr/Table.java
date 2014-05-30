package com.wireless.pojo.regionMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.dishesOrder.Order.Category;
import com.wireless.pojo.util.NumericUtil;

public class Table implements Parcelable, Comparable<Table>, Jsonable{
	
	public final static byte TABLE_PARCELABLE_COMPLEX = 0;
	public final static byte TABLE_PARCELABLE_SIMPLE = 1;
	
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
		
		private short regionId = Region.RegionId.REGION_1.getId();
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
	
	/**
	 * 餐台状态
	 * 1 - 空闲, 2 - 就餐
	 */
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
	private float minimumCost;
	private int customNum;
	private Category category = Category.NORMAL;;
	private Status status = Status.IDLE;
	private float serviceRate;
	private Region region;
	
	public Table(){
		
	}
	
	public Table(int tableAlias){
		this.tableAlias = tableAlias;
	}
	
	private Table(InsertBuilder builder){
		setTableAlias(builder.getTableAlias());
		setRestaurantId(builder.getRestaurantId());
		setRegion(new Region(builder.getRegionId(), null));
		setMinimumCost(builder.getMiniCost());
		setServiceRate(builder.getServiceRate());
		setTableName(builder.getTableName());
	}
	
	private Table(UpdateBuilder builder){
		setMinimumCost(builder.getMiniCost());
		setRegion(new Region(builder.getRegionId(), null));
		setServiceRate(builder.getServiceRate());
		setTableId(builder.getTableId());
		setTableName(builder.getTableName());
	}
	
	public int getTableId() {
		return tableId;
	}
	
	public void setTableId(int tableId) {
		this.tableId = tableId;
	}
	
	public int getAliasId() {
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
	
	public String getName() {
		if(tableName == null){
			return "";
		}
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public float getMinimumCost() {
		return minimumCost;
	}
	
	public void setMinimumCost(float miniCost) {
		if(miniCost < 0 || miniCost > 65535){
			throw new IllegalArgumentException("The minimum cost(val = " + miniCost + ") exceed the range.");
		}
		this.minimumCost = miniCost;
	}
	
	public int getCustomNum() {
		return customNum;
	}
	
	public void setCustomNum(int customNum) {
		this.customNum = customNum;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public void setCategory(Category category){
		this.category = category;
	}
	
	public void setCategory(int category) {
		this.category = Category.valueOf(category);
	}
	
	public boolean isNormal(){
		return category == Category.NORMAL;
	}
	
	public boolean isMerged(){
		return category == Category.MERGER_TBL;
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
	
	public boolean isIdle(){
		return this.status == Status.IDLE;
	}
	
	public boolean isBusy(){
		return this.status == Status.BUSY;
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
		if(region == null){
			region = new Region(Region.RegionId.REGION_1.getId());
		}
		return region;
	}
	
	public void setRegion(Region region) {
		this.region = region;
	}

	@Override
	public String toString(){
		return "table(" +
			   "id = " + getTableId() + 
			   ", alias_id = " + getAliasId() +
			   ", restaurant_id = " + getRestaurantId() +
			   ", name = " + (tableName != null ? tableName : "") + ")";
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + getAliasId();
		result = result * 31 + getRestaurantId();
		return result;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Table)){
			return false;
		}else{
			return getAliasId() == ((Table)obj).getAliasId() && getRestaurantId() == ((Table)obj).getRestaurantId();
		}
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == TABLE_PARCELABLE_SIMPLE){
			dest.writeShort(this.tableAlias);
			
		}else if(flag == TABLE_PARCELABLE_COMPLEX){
			dest.writeShort(this.tableAlias);
			dest.writeString(this.tableName);
			dest.writeParcel(this.region, Region.REGION_PARCELABLE_SIMPLE);
			dest.writeShort(NumericUtil.float2Int(this.serviceRate));
			dest.writeInt(NumericUtil.float2Int(this.minimumCost));
			dest.writeByte(this.status.getVal());
			dest.writeByte(this.category.getVal());
			dest.writeShort(this.customNum);
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == TABLE_PARCELABLE_SIMPLE){
			this.tableAlias = source.readShort();
			
		}else if(flag == TABLE_PARCELABLE_COMPLEX){
			this.tableAlias = source.readShort();
			this.tableName = source.readString();
			this.region = source.readParcel(Region.CREATOR);
			this.serviceRate = NumericUtil.int2Float(source.readShort());
			this.minimumCost = NumericUtil.int2Float(source.readInt());
			this.status = Status.valueOf(source.readByte());
			this.category = Category.valueOf(source.readByte());
			this.customNum = source.readShort();
		}
	}
	
	public final static Parcelable.Creator<Table> CREATOR = new Parcelable.Creator<Table>() {
		
		@Override
		public Table[] newInstance(int size) {
			return new Table[size];
		}
		
		@Override
		public Table newInstance() {
			return new Table();
		}
	};

	@Override
	public int compareTo(Table another) {
		if(getAliasId() > another.getAliasId()){
			return 1;
		}else if(getAliasId() < another.getAliasId()){
			return -1;
		}else{
			return 0;
		}
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.tableId);
		jm.putInt("alias", this.tableAlias);
		jm.putInt("rid", this.restaurantId);
		jm.putString("name", this.tableName);
		jm.putInt("customNum", this.customNum);
		jm.putFloat("minimumCost", this.minimumCost);
		jm.putFloat("serviceRate", this.serviceRate);
		jm.putInt("categoryValue", this.category.getVal());
		jm.putString("categoryText", this.category.getDesc());
		jm.putInt("statusValue", this.status.getVal());
		jm.putString("statusText", this.status.getDesc());
		jm.putJsonable("region", this.region, 0);
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

}
