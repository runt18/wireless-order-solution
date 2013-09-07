package com.wireless.pojo.regionMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public static enum RegionId{
		REGION_1(0, "大厅"),
		REGION_2(1, "区域2"),
		REGION_3(2, "区域3"),
		REGION_4(3, "区域4"),
		REGION_5(4, "区域5"),
		REGION_6(5, "区域6"),
		REGION_7(6, "区域7"),
		REGION_8(7, "区域8"),
		REGION_9(8, "区域9"),
		REGION_10(9, "区域10");
		
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
		private final int restaurantId;
		private final short regionId;
		
		public InsertBuilder(int restaurantId, RegionId regionId){
			this(restaurantId, regionId, regionId.name);
		}
		
		public InsertBuilder(int restaurantId, RegionId regionId, String name){
			this.restaurantId = restaurantId;
			this.name = name;
			this.regionId = regionId.getId();
		}
		
		public Region build(){
			return new Region(this);
		}
	}
	
	//The helper class to update region
	public static class UpdateBuilder{
		private final String name;
		private final int restaurantId;
		private final short regionId;
		
		public UpdateBuilder(int restaurantId, RegionId regionId, String name){
			this.restaurantId = restaurantId;
			this.name = name;
			this.regionId = regionId.getId();
		}
		
		public Region build(){
			return new Region(this);
		}
	}
	
	private Region(InsertBuilder builder){
		this.restaurantId = builder.restaurantId;
		this.name = builder.name;
		this.id = builder.regionId;
	}
	
	private Region(UpdateBuilder builder){
		this.restaurantId = builder.restaurantId;
		this.name = builder.name;
		this.id = builder.regionId;
	}
	
	public Region(){}
	
	public Region(short id){
		this.id = id;
	}
	
	public Region(short id, String name){
		this.id = id;
		this.name = name;
	}
	
	public Region(short id, String name, int restaurantID){
		this.id = id;
		this.name = name;
		this.restaurantId = restaurantID;
	}
	
	public short getRegionId() {
		return id;
	}
	
	public void setRegionId(short id) {
		this.id = id;
	}
	
	public String getName() {
		if(name == null){
			name = "";
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
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> map = new HashMap<String, Object>();
		if(flag == REGION_JSONABLE_LEAF){
			map.put("leaf", true);
		}else{
			map.put("leaf", false);
		}
		map.put("id", getRegionId());
		map.put("name", getName());
		map.put("rid", getRestaurantId());
		map.put("text", getName());
		
		return Collections.unmodifiableMap(map);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

	@Override
	public int compareTo(Region o) {
		if(getRegionId() > o.getRegionId()){
			return 1;
		}else if(getRegionId() < o.getRegionId()){
			return -1;
		}else{
			return 0;
		}
	}

}
