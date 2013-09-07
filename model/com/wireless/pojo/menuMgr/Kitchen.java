package com.wireless.pojo.menuMgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;


public class Kitchen implements Parcelable, Comparable<Kitchen>, Jsonable{
	
	public static class InsertBuilder{
		private final short kitchenAlias;
		private final String kitchenName;
		private final int restaurantId;
		private Type type = Type.NORMAL;
		private Department.DeptId deptId = Department.DeptId.DEPT_1;
		
		public InsertBuilder(int restaurantId, String kitchenName, short kitchenAlias){
			this.restaurantId = restaurantId;
			this.kitchenName = kitchenName;
			this.kitchenAlias = kitchenAlias;
		}
		
		public InsertBuilder(int restaurantId, KitchenAlias alias){
			this(restaurantId, alias.getDesc(), alias.getAliasId());
			this.type = alias.getType();
		}
		
		public short getKitchenAlias(){
			return kitchenAlias;
		}
		
		public String getKitchenName(){
			if(kitchenName == null){
				return "";
			}else{
				return kitchenName;
			}
		}
		
		public int getRestaurantId(){
			return this.restaurantId;
		}
		
		public InsertBuilder setType(Type type){
			this.type = type;
			return this;
		}
		
		public Type getType(){
			return type;
		}
		
		public InsertBuilder setDeptId(Department.DeptId deptId){
			this.deptId = deptId;
			return this;
		}
		
		public Department.DeptId getDeptId(){
			return this.deptId;
		}
	}
	
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
	
//	public final static short KITCHEN_NULL = 255;
//	public final static short KITCHEN_FULL = 254;
//	public final static short KITCHEN_TEMP = 253;
	
	public static enum KitchenAlias{
		KITCHEN_1(0, "厨房1", Type.NORMAL), KITCHEN_2(1, "厨房2", Type.NORMAL), KITCHEN_3(2, "厨房3", Type.NORMAL), KITCHEN_4(3, "厨房4", Type.NORMAL), KITCHEN_5(4, "厨房5", Type.NORMAL),
		KITCHEN_6(5, "厨房6", Type.NORMAL), KITCHEN_7(6, "厨房7", Type.NORMAL), KITCHEN_8(7, "厨房8", Type.NORMAL), KITCHEN_9(8, "厨房9", Type.NORMAL), KITCHEN_10(9, "厨房10", Type.NORMAL),
		KITCHEN_11(10, "厨房11", Type.NORMAL), KITCHEN_12(11, "厨房12", Type.NORMAL), KITCHEN_13(12, "厨房13", Type.NORMAL), KITCHEN_14(13, "厨房14", Type.NORMAL), KITCHEN_15(14, "厨房15", Type.NORMAL),
		KITCHEN_16(15, "厨房16", Type.NORMAL), KITCHEN_17(16, "厨房17", Type.NORMAL), KITCHEN_18(17, "厨房18", Type.NORMAL), KITCHEN_19(18, "厨房19", Type.NORMAL), KITCHEN_20(19, "厨房20", Type.NORMAL),
		KITCHEN_21(20, "厨房21", Type.NORMAL), KITCHEN_22(21, "厨房22", Type.NORMAL), KITCHEN_23(22, "厨房23", Type.NORMAL), KITCHEN_24(23, "厨房24", Type.NORMAL), KITCHEN_25(24, "厨房25", Type.NORMAL),
		KITCHEN_26(25, "厨房26", Type.NORMAL), KITCHEN_27(26, "厨房27", Type.NORMAL), KITCHEN_28(27, "厨房28", Type.NORMAL), KITCHEN_29(28, "厨房29", Type.NORMAL), KITCHEN_30(29, "厨房30", Type.NORMAL),
		KITCHEN_31(30, "厨房31", Type.NORMAL), KITCHEN_32(31, "厨房32", Type.NORMAL), KITCHEN_33(32, "厨房33", Type.NORMAL), KITCHEN_34(33, "厨房34", Type.NORMAL), KITCHEN_35(34, "厨房35", Type.NORMAL),
		KITCHEN_36(35, "厨房36", Type.NORMAL), KITCHEN_37(36, "厨房37", Type.NORMAL), KITCHEN_38(37, "厨房38", Type.NORMAL), KITCHEN_39(38, "厨房39", Type.NORMAL), KITCHEN_40(39, "厨房40", Type.NORMAL),
		KITCHEN_41(40, "厨房41", Type.NORMAL), KITCHEN_42(41, "厨房42", Type.NORMAL), KITCHEN_43(42, "厨房43", Type.NORMAL), KITCHEN_44(43, "厨房44", Type.NORMAL), KITCHEN_45(44, "厨房45", Type.NORMAL),
		KITCHEN_46(45, "厨房46", Type.NORMAL), KITCHEN_47(46, "厨房47", Type.NORMAL), KITCHEN_48(47, "厨房48", Type.NORMAL), KITCHEN_49(48, "厨房49", Type.NORMAL), KITCHEN_50(49, "厨房50", Type.NORMAL),
		KITCHEN_TEMP(253, "临时厨房", Type.RESERVED),
		KITCHEN_FULL(254, "全部厨房", Type.RESERVED),
		KITCHEN_NULL(255, "空厨房", Type.RESERVED);
		
		private final int aliasId;
		private final String desc;
		private final Type type;
		
		KitchenAlias(int aliasId, String desc, Type type){
			this.aliasId = aliasId;
			this.desc = desc;
			this.type = type;
		}
		
		public short getAliasId(){
			return (short)aliasId;
		}
		
		public String getDesc(){
			return desc;
		}
		
		public Type getType(){
			return type;
		}
		
		public static KitchenAlias valueOf(int aliasId){
			for(KitchenAlias alias : values()){
				if(alias.aliasId == aliasId){
					return alias;
				}
			}
			throw new IllegalArgumentException("The alias id(" + aliasId + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return "(alias_id = " + aliasId + ", desc = " + desc + ", type = " + type.getDesc() + ")";
		}
	}
	
	public static enum Type{
		NORMAL(0, "普通"),
		RESERVED(1, "系统保留");
		
		private final int val;
		private final String desc;
		private Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
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
			return desc;
		}
	}
	
	private long kitchenId;
	private short aliasId;
	private int restaurantId;
	private String name;
	private boolean isAllowTmp;
	private Type type = Type.NORMAL;
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

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.kitchenId);
		jm.put("alias", this.aliasId);
		jm.put("rid", this.restaurantId);
		jm.put("name", this.name);
		jm.put("isAllowTmp", this.isAllowTmp);
		jm.put("typeValue", this.type.getVal());
		if(this.dept != null)
			jm.put("dept", this.dept.toJsonMap(0));
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

	
}
