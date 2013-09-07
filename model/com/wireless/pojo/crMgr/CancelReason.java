package com.wireless.pojo.crMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class CancelReason implements Parcelable, Jsonable{
	
	public final static byte CR_PARCELABLE_COMPLEX = 0;
	public final static byte CR_PARCELABLE_SIMPLE = 1;
	
	public final static int NO_REASON = 1;
	
	private int id = NO_REASON;
	private int restaurantId;
	private String reason;

	public static enum DefaultCR{
		DEF_CR_1("上菜慢"),
		DEF_CR_2("点错菜"),
		DEF_CR_3("菜品停售"),
		DEF_CR_4("菜品问题"),
		DEF_CR_5("客人问题");
		
		private final String reason;
		
		DefaultCR(String reason){
			this.reason = reason; 
		}
		
		public String getReason(){
			return reason;
		}
		
		@Override
		public String toString(){
			return reason;
		}
	}
	
	//The helper class to insert a new cancel reason
	public static class InsertBuilder{
		private final int restaurantId;
		private final String reason;
		
		public InsertBuilder(int restaurantId, String reason){
			this.restaurantId = restaurantId;
			this.reason = reason;
		}
		
		public CancelReason build(){
			return new CancelReason(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private final String reason;
		
		public UpdateBuilder(int id, String reason){
			this.id = id;
			this.reason = reason;
		}
		
		public CancelReason build(){
			return new CancelReason(this);
		}
	}
	
	private CancelReason(InsertBuilder builder){
		this.restaurantId = builder.restaurantId;
		this.reason = builder.reason;
	}
	
	private CancelReason(UpdateBuilder builder){
		this.id = builder.id;
		this.reason = builder.reason;
	}
	
	public CancelReason(){}
	
	public CancelReason(int id){
		this.id = id;
	}
	public CancelReason(String reason, int restaurantId){
		this.reason = reason;
		this.restaurantId = restaurantId;
	}
	public CancelReason(int id, String reason, int restaurantId){
		this.restaurantId = restaurantId;
		this.id = id;
		this.reason = reason;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRestaurantID() {
		return restaurantId;
	}
	public void setRestaurantID(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	public String getReason() {
		if(reason == null){
			reason = "";
		}
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	public boolean hasReason(){
		return id != NO_REASON;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof CancelReason)){
			return false;
		}else{
			return id == ((CancelReason)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return getReason() + "(id = " + id + ",restaurantId = " + restaurantId + ")";
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == CR_PARCELABLE_SIMPLE){
			dest.writeInt(this.id);
			
		}else if(flag == CR_PARCELABLE_COMPLEX){
			dest.writeInt(this.id);
			dest.writeString(this.reason);
		}
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == CR_PARCELABLE_SIMPLE){
			this.id = source.readInt();
			
		}else if(flag == CR_PARCELABLE_COMPLEX){
			this.id = source.readInt();
			this.reason = source.readString();
		}
	}

	public final static Parcelable.Creator<CancelReason> CR_CREATOR = new Parcelable.Creator<CancelReason>() {
		
		public CancelReason[] newInstance(int size) {
			return new CancelReason[size];
		}
		
		public CancelReason newInstance() {
			return new CancelReason();
		}
	};

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		HashMap<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.id);
		jm.put("reason", this.reason);
		jm.put("rid", this.restaurantId);
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
}
