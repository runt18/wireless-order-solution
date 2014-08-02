package com.wireless.pojo.regionMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class Region implements Parcelable, Jsonable, Comparable<Region>{
	
	public final static byte REGION_JSONABLE_LEAF = 1;
	public final static byte REGION_JSONABLE_ROOT = 2;
	
	public final static byte REGION_PARCELABLE_COMPLEX = 0;
	public final static byte REGION_PARCELABLE_SIMPLE = 1;
	
	private short id;
	private String name;
	private int restaurantId;
	private Status status = Status.BUSY;
	private int displayId;
	
	public static class MoveBuilder{
		private final int from;
		private final int to;
		
		public MoveBuilder(int from, int to){
			this.from = from;
			this.to = to;
		}
		
		public int from(){
			return this.from;
		}
		
		public int to(){
			return this.to;
		}
	}
	
	public static enum Status{
		BUSY(1, "使用"),
		IDLE(2, "空闲");
		
		private final int val;
		private final String desc;
		
		private Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		public static Status valueOf(int val){
			for(Status type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The department type(value = " + val + ") passed is invaild.");
		}
		
		@Override
		public String toString(){
			return "Type(code = " + val + ",desc = " + desc + ")";
		}
	}
	
	public static enum RegionId{
		REGION_NULL(Short.MAX_VALUE, "空区域"),
		REGION_1(0, "大厅"),
		REGION_2(1, "区域2"),
		REGION_3(2, "区域3"),
		REGION_4(3, "区域4"),
		REGION_5(4, "区域5"),
		REGION_6(5, "区域6"),
		REGION_7(6, "区域7"),
		REGION_8(7, "区域8"),
		REGION_9(8, "区域9"),
		REGION_10(9, "区域10"),
		REGION_11(10, "区域11"),
		REGION_12(11, "区域12"),
		REGION_13(12, "区域13"),
		REGION_14(13, "区域14"),
		REGION_15(14, "区域15"),
		REGION_16(15, "区域16"),
		REGION_17(16, "区域17"),
		REGION_18(17, "区域18"),
		REGION_19(18, "区域19"),
		REGION_20(19, "区域20");
		
		private final int id;
		private final String name;
		
		RegionId(int id, String name){
			this.id = id;
			this.name = name;
		}
		
		public short getId(){
			return (short)this.id;
		}
		
		public String getName(){
			return this.name;
		}
		
		public static RegionId valueOf(int id){
			for(RegionId regionId : values()){
				if(regionId.id == id){
					return regionId;
				}
			}
			throw new IllegalArgumentException("The region id(" + id + ")is invalid.");
		}
		
		@Override
		public String toString(){
			return "region(id = " + id + ", name = " + name + ")";
		}
	}
	
	//The helper class to insert a new region
	public static class InsertBuilder{
		private final String name;
		private final short regionId;
		private final Status status;
		
		public InsertBuilder(RegionId regionId){
			this(regionId, regionId.name, Status.IDLE);
		}
		
		public InsertBuilder(RegionId regionId, Status status){
			this(regionId, regionId.name, status);
		}
		
		public InsertBuilder(RegionId regionId, String name, Status status){
			this.name = name;
			this.regionId = regionId.getId();
			this.status = status;
		}
		
		public Region build(){
			return new Region(this);
		}
	}
	
	public static class AddBuilder{
		private final String name;
		
		public AddBuilder(String name){
			this.name = name;
		}
		
		public Region build(){
			return new Region(this);
		}
	}
	
	//The helper class to update region
	public static class UpdateBuilder{
		private final String name;
		private final short regionId;
		
		public UpdateBuilder(int regionId, String name){
			this.name = name;
			this.regionId = (short)regionId;
		}
		
		public Region build(){
			return new Region(this);
		}
	}
	
	private Region(InsertBuilder builder){
		this.name = builder.name;
		this.id = builder.regionId;
		this.status = builder.status;
	}
	
	private Region(AddBuilder builder){
		this.name = builder.name;
		this.status = Status.BUSY;
	}
	
	private Region(UpdateBuilder builder){
		this.name = builder.name;
		this.id = builder.regionId;
		this.status = Status.BUSY;
	}
	
	private Region(){}
	
	public Region(int id){
		this.id = (short)id;
	}
	
	public Region(short id, String name){
		this.id = id;
		this.name = name;
	}
	
	public Region(short id, String name, int restaurantId){
		this.id = id;
		this.name = name;
		this.restaurantId = restaurantId;
	}
	
	public short getId() {
		return id;
	}
	
	public void setRegionId(short id) {
		this.id = id;
	}
	
	public String getName() {
		if(name == null){
			return "";
		}
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
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public Status getStatus(){
		return this.status;
	}
	
	public boolean isIdle(){
		return this.status == Status.IDLE;
	}
	
	public boolean isBusy(){
		return this.status == Status.BUSY;
	}
	
	public void setDisplayId(int displayId){
		this.displayId = displayId;
	}
	
	public int getDisplayId(){
		return this.displayId;
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
			   ", name = " + getName() + ")";
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == REGION_PARCELABLE_SIMPLE){
			dest.writeByte(this.id);
			
		}else if(flag == REGION_PARCELABLE_COMPLEX){
			dest.writeByte(this.id);
			dest.writeString(this.name);
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == REGION_PARCELABLE_SIMPLE){
			this.id = source.readByte();
			
		}else if(flag == REGION_PARCELABLE_COMPLEX){
			this.id = source.readByte();
			this.name = source.readString();
		}
	}
	
	public final static Parcelable.Creator<Region> CREATOR = new Parcelable.Creator<Region>() {
		
		public Region[] newInstance(int size) {
			return new Region[size];
		}
		
		public Region newInstance() {
			return new Region();
		}
	};

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap map = new JsonMap();
		if(flag == REGION_JSONABLE_LEAF){
			map.putBoolean("leaf", true);
		}else{
			map.putBoolean("leaf", false);
		}
		map.putInt("id", getId());
		map.putString("name", getName());
		map.putInt("rid", getRestaurantId());
		map.putString("text", getName());
		
		return map;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	@Override
	public int compareTo(Region o) {
		if(getId() > o.getId()){
			return 1;
		}else if(getId() < o.getId()){
			return -1;
		}else{
			return 0;
		}
	}

}
