package com.wireless.pojo.menuMgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.menuMgr.Department.DeptId;


public class Kitchen implements Parcelable, Comparable<Kitchen>, Jsonable{
	
	public static class SwapDisplayBuilder{
		private final int idA;
		private final int idB;
		public SwapDisplayBuilder(int idA, int idB){
			this.idA = idA;
			this.idB = idB;
		}
		
		public int getIdA(){
			return this.idA;
		}
		
		public int getIdB(){
			return this.idB;
		}
	}
	
	public static class AddBuilder{
		private final String kitchenName;
		private final Department.DeptId deptId;
		private boolean isAllowTemp = false;
		
		public AddBuilder(String kitchenName, Department.DeptId deptId){
			this.kitchenName = kitchenName;
			if(deptId.getType() == Department.Type.NORMAL){
				this.deptId = deptId;
			}else{
				throw new IllegalArgumentException("The dept id should belong to normal.");
			}
		}
		
		public AddBuilder setAllowTmp(boolean onOff){
			this.isAllowTemp = onOff;
			return this;
		}
		
		public Kitchen build(){
			return new Kitchen(this); 
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private String kitchenName;
		private Department.DeptId deptId;
		private boolean isAllowTemp = false;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setName(String name){
			this.kitchenName = name;
			return this;
		}
		
		public boolean isNameChanged(){
			return this.kitchenName != null;
		}
		
		public UpdateBuilder setDeptId(Department.DeptId deptId){
			if(deptId.getType() == Department.Type.NORMAL){
				this.deptId = deptId;
			}else{
				throw new IllegalArgumentException("The dept id should belong to normal.");
			}
			return this;
		}
		
		public boolean isDeptChanged(){
			return this.deptId != null;
		}
		
		public UpdateBuilder setAllowTmp(boolean onOff){
			this.isAllowTemp = onOff;
			return this;
		}
		
		public boolean isAllowTmpChanged(){
			return this.isAllowTemp != false;
		}
		
		public Kitchen build(){
			return new Kitchen(this); 
		}
	}
	
	public static class InsertBuilder{
		private final String kitchenName;
		private final DeptId deptId;
		private final Type type;
		
		public InsertBuilder(String name, DeptId deptId, Type type){
			this.kitchenName = name;
			this.deptId = deptId;
			this.type = type;
		}
		
		public Kitchen build(){
			return new Kitchen(this);
		}
	}
	
	public static class QueryBuilder{
		private final String kitchenName;
		private final int kitchenId;
		
		private int restaurantId;
		private boolean isAllowTemp;
		private Type type = Type.NORMAL;
		private Department dept;
		private int displayId;
		
		public Kitchen build(){
			return new Kitchen(this);
		}
		
		public QueryBuilder(int id, String kitchenName){
			this.kitchenName = kitchenName;
			this.kitchenId = id;
		}

		public QueryBuilder setDisplayId(int displayId){
			this.displayId = displayId;
			return this;
		}
		
		public QueryBuilder setRestaurantId(int restaurantId) {
			this.restaurantId = restaurantId;
			return this;
		}

		public QueryBuilder setAllowTemp(boolean isAllowTemp) {
			this.isAllowTemp = isAllowTemp;
			return this;
		}

		public QueryBuilder setType(Type type) {
			this.type = type;
			return this;
		}

		public QueryBuilder setType(int typeVal){
			this.type = Type.valueOf(typeVal);
			return this;
		}
		
		public QueryBuilder setDept(Department dept) {
			this.dept = dept;
			return this;
		}

	}
	
	public final static byte KITCHEN_PARCELABLE_COMPLEX = 0;
	public final static byte KITCHEN_PARCELABLE_SIMPLE = 1;
	
	/**
	 * @deprecated
	 */
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
		KITCHEN_TEMP(253, "临时厨房", Type.TEMP),
		//KITCHEN_FULL(254, "全部厨房", Type.RESERVED),
		KITCHEN_NULL(255, "空厨房", Type.NULL);
		
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
		NORMAL(0, "普通厨房"),
		IDLE(1, "空闲厨房"),
		TEMP(2, "临时厨房"),
		NULL(3, "空厨房");
		
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
	
	private int kitchenId;
	private int restaurantId;
	private String name;
	private int displayId;
	private boolean isAllowTmp;
	private Type type = Type.NORMAL;
	private Department dept;
	
	public Kitchen(){
		this.dept = new Department();
	}
	
	public Kitchen(int id){
		setId(id);
	}
	
	private Kitchen(QueryBuilder builder){
		setId(builder.kitchenId);
		setName(builder.kitchenName);
		setRestaurantId(builder.restaurantId);
		setAllowTemp(builder.isAllowTemp);
		setType(builder.type);
		setDept(builder.dept);
		setDisplayId(builder.displayId);
	}
	
	private Kitchen(InsertBuilder builder){
		setName(builder.kitchenName);
		setType(builder.type);
		setDept(builder.deptId.getVal(), builder.deptId.getDesc());
	}
	
	private Kitchen(AddBuilder builder){
		setName(builder.kitchenName);
		setAllowTemp(builder.isAllowTemp);
		setDept(builder.deptId.getVal(), builder.deptId.getDesc());
	}
	
	private Kitchen(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.kitchenName);
		setAllowTemp(builder.isAllowTemp);
		setDept(builder.deptId.getVal(), builder.deptId.getDesc());
	}
	
	public int getId() {
		return this.kitchenId;
	}
	
	public void setId(int kitchenId) {
		this.kitchenId = kitchenId;
	}
	
	public String getName() {
		if(name == null){
			return "";
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
	
	public void setDisplayId(int displayId){
		this.displayId = displayId;
	}
	
	public int getDisplayId(){
		return this.displayId;
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
	
	public boolean idIdle(){
		return this.type == Type.IDLE;
	}
	
	public boolean isTemp(){
		return this.type == Type.TEMP;
	}
	
	public boolean isNull(){
		return this.type == Type.NULL;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Kitchen)){
			return false;
		}else{
			return this.kitchenId == ((Kitchen)obj).kitchenId;
		}
	}
	
	@Override
	public int hashCode(){
		return 17 * 31 + kitchenId;
	}
	
	@Override
	public String toString(){
		return "kitchen(id = " + getId() + ",name = " + getName() + ")";
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == KITCHEN_PARCELABLE_SIMPLE){
			dest.writeInt(this.getId());
			
		}else if(flag == KITCHEN_PARCELABLE_COMPLEX){
			dest.writeInt(this.getId());
			dest.writeInt(this.getDisplayId());
			dest.writeParcel(this.dept, Department.DEPT_PARCELABLE_SIMPLE);
			dest.writeByte(this.isAllowTmp ? 1 : 0);
			dest.writeByte(this.getType().getVal());
			dest.writeString(this.getName());
		}
	}
	
	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == KITCHEN_PARCELABLE_SIMPLE){
			setId(source.readInt());
			
		}else if(flag == KITCHEN_PARCELABLE_COMPLEX){
			setId(source.readInt());
			setDisplayId(source.readInt());
			setDept(source.readParcel(Department.DEPT_CREATOR));
			setAllowTemp(source.readByte() == 1 ? true : false);
			setType(Type.valueOf(source.readByte()));
			setName(source.readString());
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
		if(getId() > kitchen.getId()){
			return 1;
		}else if(getId() < kitchen.getId()){
			return -1;
		}else{
			return 0;
		}
	}

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.kitchenId);
		jm.put("alias", this.displayId);
		jm.put("rid", this.restaurantId);
		jm.put("name", this.name);
		jm.put("isAllowTmp", this.isAllowTmp);
		jm.put("typeValue", this.type.getVal());
		if(this.dept != null){
			jm.put("dept", this.dept.toJsonMap(0));
		}
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

	
}
