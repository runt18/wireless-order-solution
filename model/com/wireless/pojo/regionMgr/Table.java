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
		private final Region.RegionId regionId;
		private String tableName;
		private int miniCost;
		
		InsertBuilder(int tableAlias, short regionId){
			this.tableAlias = tableAlias;
			this.regionId = Region.RegionId.valueOf(regionId);
		}

		public InsertBuilder(int tableAlias, Region.RegionId regionId){
			this.tableAlias = tableAlias;
			this.regionId = regionId;
		}

		
		public Table build(){
			return new Table(this);
		}
		
		public InsertBuilder setTableName(String tableName) {
			this.tableName = tableName;
			return this;
		}

		public InsertBuilder setMiniCost(int miniCost) {
			this.miniCost = miniCost;
			return this;
		}
	}
	
	/**
	 * The helper class to create the table object used in update {@link TableDao#}
	 */
	public static class UpdateBuilder{
		private final int tableId;
		
		private Region.RegionId regionId;
		private String tableName;
		private int miniCost = -1;
		
		public Table build(){
			return new Table(this);
		}
		
		public UpdateBuilder(int tableId){
			this.tableId = tableId;
		}

		public boolean isRegionChanged(){
			return this.regionId != null;
		}
		
		public UpdateBuilder setRegionId(Region.RegionId regionId) {
			this.regionId = regionId;
			return this;
		}

		public boolean isNameChanged(){
			return this.tableName != null;
		}
		
		public UpdateBuilder setTableName(String name) {
			this.tableName = name;
			return this;
		}

		public boolean isMiniCostChanged(){
			return this.miniCost >= 0;
		}
		
		public UpdateBuilder setMiniCost(int miniCost) {
			this.miniCost = miniCost;
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
	private Region region;
	
	public Table(){
		
	}
	
	public Table(int tableAlias){
		this.tableAlias = tableAlias;
	}
	
	private Table(InsertBuilder builder){
		setTableAlias(builder.tableAlias);
		setRegion(new Region(builder.regionId.getId(), null));
		setMinimumCost(builder.miniCost);
		setTableName(builder.tableName);
	}
	
	private Table(UpdateBuilder builder){
		setMinimumCost(builder.miniCost);
		setRegion(new Region(builder.regionId.getId(), null));
		setTableId(builder.tableId);
		setTableName(builder.tableName);
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
	
	public boolean isIdle(){
		return this.status == Status.IDLE;
	}
	
	public boolean isBusy(){
		return this.status == Status.BUSY;
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
