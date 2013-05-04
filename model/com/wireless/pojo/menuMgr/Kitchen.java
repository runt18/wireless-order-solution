package com.wireless.pojo.menuMgr;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;


public class Kitchen implements Parcelable, Comparable<Kitchen>{
	
	public static class Builder{
		private final String kitchenName;
		private final short aliasId;
		private final int restaurantId;

		private long kitchenId;
		private boolean isAllowTemp;
		private Type type = Type.NORMAL;
		private Department dept;
		
		public Kitchen build(){
			return new Kitchen(this);
		}
		
		public Builder(short aliasId, String kitchenName, int restaurantId){
			this.kitchenName = kitchenName;
			this.aliasId = aliasId;
			this.restaurantId = restaurantId;
		}

		public long getKitchenId() {
			return kitchenId;
		}

		public Builder setKitchenId(long kitchenId) {
			this.kitchenId = kitchenId;
			return this;
		}

		public boolean isAllowTemp() {
			return isAllowTemp;
		}

		public Builder setAllowTemp(boolean isAllowTemp) {
			this.isAllowTemp = isAllowTemp;
			return this;
		}

		public Type getType() {
			return type;
		}

		public Builder setType(Type type) {
			this.type = type;
			return this;
		}

		public Builder setType(int typeVal){
			this.type = Type.valueOf(typeVal);
			return this;
		}
		
		public Department getDept() {
			return dept;
		}

		public Builder setDept(Department dept) {
			this.dept = dept;
			return this;
		}

		public String getKitchenName() {
			return kitchenName;
		}

		public short getAliasId() {
			return aliasId;
		}

		public int getRestaurantId() {
			return restaurantId;
		}
	}
	
	public final static byte KITCHEN_PARCELABLE_COMPLEX = 0;
	public final static byte KITCHEN_PARCELABLE_SIMPLE = 1;
	
	public final static short KITCHEN_NULL = 255;
	public final static short KITCHEN_FULL = 254;
	public final static short KITCHEN_TEMP = 253;
	
	public static enum Type{
		NORMAL(0),
		RESERVED(1);
		
		private final int val;
		private Type(int val){
			this.val = val;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static Type valueOf(int val){
			for(Type status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The kitchen type(value = " + val + ") passed is invaild.");
		}
		
		@Override
		public String toString(){
			if(this == NORMAL){
				return "normal kitchen";
			}else if(this == RESERVED){
				return "reserved kitchen";
			}else{
				return "unknown type";
			}
		}
	}
	
	private long kitchenId;
	private short aliasId;
	private int restaurantId;
	private String name;
	private boolean isAllowTmp;
	private Type type;
	private Department dept;
	
	public Kitchen(){
		this.dept = new Department();
	}
	
	private Kitchen(Builder builder){
		setId(builder.getKitchenId());
		setAliasId(builder.getAliasId());
		setName(builder.getKitchenName());
		setRestaurantId(builder.getRestaurantId());
		setAllowTemp(builder.isAllowTemp);
		setType(builder.getType());
		setDept(builder.getDept());
	}
	
	public long getId() {
		return this.kitchenId;
	}
	
	public void setId(long kitchenId) {
		this.kitchenId = kitchenId;
	}
	
	public short getAliasId() {
		return this.aliasId;
	}
	
	public void setAliasId(short aliasId) {
		this.aliasId = aliasId;
	}
	
	public String getName() {
		if(name == null){
			name = "";
		}
		return this.name;
	}
	
	public void setName(String kitchenName) {
		this.name = kitchenName;
	}
	
	public int getRestaurantId() {
		return this.restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public boolean isAllowTemp() {
		return this.isAllowTmp;
	}
	
	public void setAllowTemp(boolean isAllowTmp) {
		this.isAllowTmp = isAllowTmp;
	}
	
	public void setAllowTemp(String isAllowTemp) {
		this.isAllowTmp = (isAllowTemp != null && isAllowTemp.equals("1")) ? true : false;
	}
	
	public Department getDept() {
		return this.dept;
	}
	
	public void setDept(Department dept) {
		this.dept = dept;
	}
	
	public void setDept(short deptId, String deptName) {
		this.dept = new Department(restaurantId, deptId, deptName);
	}	
	
	public void setType(Type type){
		this.type = type;
	}

	public void setType(int typeVal){
		this.type = Type.valueOf(typeVal);
	}
	
	public Type getType(){
		return this.type;
	}
	
	public boolean isNormal(){
		return this.type == Type.NORMAL;
	}
	
	public boolean isReserved(){
		return this.type == Type.RESERVED;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Kitchen)){
			return false;
		}else{
			Kitchen kitchen = (Kitchen)obj;
			return this.restaurantId == kitchen.restaurantId && this.aliasId == kitchen.aliasId;
		}
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + restaurantId;
		result = result * 31 + aliasId;
		return result;
	}
	
	@Override
	public String toString(){
		return "kitchen(alias_id = " + getAliasId() + ",restaurant_id = " + getRestaurantId() + ")";
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == KITCHEN_PARCELABLE_SIMPLE){
			dest.writeByte(this.aliasId);
			
		}else if(flag == KITCHEN_PARCELABLE_COMPLEX){
			dest.writeByte(this.aliasId);
			dest.writeParcel(this.dept, Department.DEPT_PARCELABLE_SIMPLE);
			dest.writeByte(this.isAllowTmp ? 1 : 0);
			dest.writeByte(this.type.getVal());
			dest.writeString(this.name);
		}
	}
	
	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == KITCHEN_PARCELABLE_SIMPLE){
			this.aliasId = source.readByte();
			
		}else if(flag == KITCHEN_PARCELABLE_COMPLEX){
			this.aliasId = source.readByte();
			this.dept = source.readParcel(Department.DEPT_CREATOR);
			this.isAllowTmp = source.readByte() == 1 ? true : false;
			this.type = Type.valueOf(source.readByte());
			this.name = source.readString();
		}
	}

	public final static Parcelable.Creator<Kitchen> KITCHEN_CREATOR = new Parcelable.Creator<Kitchen>() {
		
		public Kitchen[] newInstance(int size) {
			return new Kitchen[size];
		}
		
		public Kitchen newInstance() {
			return new Kitchen();
		}
	};

	@Override
	public int compareTo(Kitchen kitchen) {
		if(getAliasId() > kitchen.getAliasId()){
			return 1;
		}else if(getAliasId() < kitchen.getAliasId()){
			return -1;
		}else{
			return 0;
		}
	}
	
}
