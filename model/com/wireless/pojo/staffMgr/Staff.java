package com.wireless.pojo.staffMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class Staff implements Parcelable, Jsonable{ 

	public final static byte ST_PARCELABLE_COMPLEX = 0;
	public final static byte ST_PARCELABLE_SIMPLE = 1;
	
	public static enum Type{
		NORMAL(1, "普通"),
		RESERVED(2, "系统保留");
		
		private final int val;
		private final String desc;
		
		Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The type(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
	}
	
	//the id to this staff
	private int id = 0;
	//the restaurant id this staff
	private int restaurantId = 0;
	//the name to this staff
	private String name;
	//the mobile to this staff
	private String mobile;
	//the type to this staff
	private Type type = Type.NORMAL;
	//the password to this staff
	private String pwd;
	//the role to this staff
	private Role role;
	
	public Staff(){
		
	}
	
	public Staff(int id){
		setId(id);
	}
	
	public Staff(String name, int restaurantId){
		setName(name);
		setRestaurantId(restaurantId);
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
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

	public void setType(Type type){
		this.type = type;
	}
	
	public Type getType(){
		return this.type;
	}
	
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	public Role getRole(){
		return this.role;
	}
	
	public void setRole(Role role){
		if(role != null){
			this.role = role;
		}
	}
	
	@Override 
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Staff)){
			return false;
		}else{
			return id == ((Staff)obj).id;
		}
	}
	
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == ST_PARCELABLE_SIMPLE){
			
		}else if(flag == ST_PARCELABLE_COMPLEX){
			dest.writeString(this.name);
			dest.writeInt(this.id);
			dest.writeString(this.pwd);
			dest.writeInt(this.restaurantId);
			dest.writeParcel(this.role, 0);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == ST_PARCELABLE_SIMPLE){
			
		}else if(flag == ST_PARCELABLE_COMPLEX){
			this.name = source.readString();
			this.id = source.readInt();
			this.pwd = source.readString();
			this.restaurantId = source.readInt();
			this.role = source.readParcel(Role.CREATOR);
		}
	}
	
	public final static Parcelable.Creator<Staff> ST_CREATOR = new Parcelable.Creator<Staff>() {
		
		public Staff[] newInstance(int size) {
			return new Staff[size];
		}
		
		public Staff newInstance() {
			return new Staff();
		}
	};

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("staffID", this.getId());
		jm.put("staffName", this.getName());
		jm.put("restaurantId", this.getRestaurantId());
		jm.put("mobile", this.getMobile());
		jm.put("staffPassword", this.getPwd());
		jm.put("typeValue", this.getType().getVal());
		jm.put("typeText", this.getType().getDesc());
		jm.put("role", this.getRole());
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
}
