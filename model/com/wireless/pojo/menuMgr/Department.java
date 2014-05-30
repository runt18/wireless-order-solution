package com.wireless.pojo.menuMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;


public class Department implements Parcelable, Comparable<Department>, Jsonable{
	
	public final static byte DEPT_PARCELABLE_COMPLEX = 0;
	public final static byte DEPT_PARCELABLE_SIMPLE = 1;
	
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
	
	public static class AddBuilder{
		private final String name;
		public AddBuilder(String name){
			if(name.length() == 0){
				throw new IllegalArgumentException("The department name should NOT be empty.");
			}else{
				this.name = name;
			}
		}
		
		public Department build(){
			return new Department(this);
		}
	}
	
	//The helper class to build a new department
	public static class InsertBuilder{
		private final int restaurantId;
		private final short deptId;
		private final String deptName;
		private Type deptType = Type.NORMAL;
		
		public InsertBuilder(int restaurantId, DeptId deptId){
			this.restaurantId = restaurantId;
			this.deptId = deptId.getVal();
			this.deptName = deptId.getDesc();
			this.deptType = deptId.getType();
		}
		
		public InsertBuilder(int restaurantId, DeptId deptId, String deptName){
			this.restaurantId = restaurantId;
			this.deptId = deptId.getVal();
			this.deptName = deptName;
		}
		
		public InsertBuilder setType(Type type){
			this.deptType = type;
			return this;
		}
		
		public Department build(){
			return new Department(this);
		}
	}
	
	public static class UpdateBuilder{
		private final DeptId deptId;
		private final String name;
		public UpdateBuilder(DeptId deptId, String name){
			if(name.length() == 0){
				throw new IllegalArgumentException("The department name should NOT be empty.");
			}else{
				this.name = name;
			}
			if(deptId.getType() != Type.NORMAL){
				throw new IllegalArgumentException("The type to dept id should belong to normal.");
			}else{
				this.deptId = deptId;
			}
		}
		
		public Department build(){
			return new Department(this);
		}
	}
	
	public static enum DeptId{
		DEPT_1(0, "部门1", Type.NORMAL),
		DEPT_2(1, "部门2", Type.NORMAL),
		DEPT_3(2, "部门3", Type.NORMAL),
		DEPT_4(3, "部门4", Type.NORMAL),
		DEPT_5(4, "部门5", Type.NORMAL),
		DEPT_6(5, "部门6", Type.NORMAL),
		DEPT_7(6, "部门7", Type.NORMAL),
		DEPT_8(7, "部门8", Type.NORMAL),
		DEPT_9(8, "部门9", Type.NORMAL),
		DEPT_10(9, "部门10", Type.NORMAL),
		DEPT_11(10, "部门11", Type.NORMAL),
		DEPT_12(11, "部门12", Type.NORMAL),
		DEPT_13(13, "部门13", Type.NORMAL),
		DEPT_14(14, "部门14", Type.NORMAL),
		DEPT_15(15, "部门15", Type.NORMAL),
		DEPT_16(16, "部门16", Type.NORMAL),
		DEPT_17(17, "部门17", Type.NORMAL),
		DEPT_18(18, "部门18", Type.NORMAL),
		DEPT_19(19, "部门19", Type.NORMAL),
		DEPT_20(20, "部门20", Type.NORMAL),
		DEPT_WAREHOUSE(252, "总仓", Type.WARE_HOUSE),
		DEPT_TMP(253, "临时部门", Type.TEMP),
		//DEPT_ALL(254, "全部部门", Type.RESERVED),
		DEPT_NULL(255, "空部门", Type.NULL);


		private final int val;
		private final String desc;
		private final Type type;
		
		DeptId(int val, String desc, Type type){
			this.val = val;
			this.desc = desc;
			this.type = type;
		}
		
		public static DeptId valueOf(int val){
			for(DeptId deptId : values()){
				if(deptId.getVal() == val){
					return deptId;
				}
			}
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
		
		public short getVal(){
			return (short)this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		public Type getType(){
			return this.type;
		}
		
		@Override
		public String toString(){
			return "(val = " + val + ",desc = " + desc + ",type = " + type.getDesc() + ")";
		}
	}
	
	public static enum Type{
		NORMAL(0, "普通"),
		IDLE(1, "保留"),
		WARE_HOUSE(2, "总仓"),
		TEMP(3, "临时"),
		NULL(4, "空");
		
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
			for(Type type : values()){
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
	
	private int restaurantId;
	private short deptId;
	private String deptName;
	private Type deptType = Type.NORMAL;
	private int displayId;

	private Department(InsertBuilder builder){
		this.restaurantId = builder.restaurantId;
		this.deptId = builder.deptId;
		this.deptName = builder.deptName;
		this.deptType = builder.deptType;
	}
	
	private Department(UpdateBuilder builder){
		this.deptId = builder.deptId.getVal();
		this.deptName = builder.name;
	}
	
	private Department(AddBuilder builder){
		this.deptName = builder.name;
		this.deptType = Type.NORMAL;
	}
	
	public Department(int id){
		this.deptId = (short)id;
	}
	
	public Department(int restaurantId, short deptId, String deptName){
		this.restaurantId = restaurantId;
		this.deptId = deptId;
		this.deptName = deptName;
	}
	
	public Department(String deptName, short deptId, int restaurantId, Type type, int displayId){
		this.restaurantId = restaurantId;
		this.deptId = deptId;
		this.deptName = deptName;
		this.deptType = type;
		this.displayId = displayId;
	}
	
	public void copyFrom(Department src){
		if(src != null && src != this){
			setId(src.getId());
			setRestaurantId(src.getRestaurantId());
			setName(src.getName());
			setType(src.getType());
			setDisplayId(src.getDisplayId());
		}
	}
	
	public int getRestaurantId() {
		return this.restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public short getId() {
		return this.deptId;
	}
	
	public void setId(short deptId) {
		this.deptId = deptId;
	}
	
	public void setDisplayId(int displayId){
		this.displayId = displayId;
	}
	
	public int getDisplayId(){
		return this.displayId;
	}
	
	public String getName() {
		if(deptName == null){
			return "";
		}
		return this.deptName;
	}
	
	public void setName(String deptName) {
		this.deptName = deptName;
	}
	
	public Type getType() {
		return this.deptType;
	}
	
	public void setType(Type type){
		this.deptType = type;
	}
	
	public void setType(int type){
		this.deptType = Type.valueOf(type);
	}
	
	public boolean isNormal(){
		return this.deptType == Type.NORMAL;
	}
	
	public boolean isIdle(){
		return this.deptType == Type.IDLE;
	}
	
	public boolean isTemp(){
		return this.deptType == Type.TEMP;
	}
	
	public boolean isNull(){
		return this.deptType == Type.NULL;
	}
	
	public boolean isWare(){
		return this.deptType == Type.WARE_HOUSE;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Department)){
			return false;
		}else{
			Department dept = (Department)obj;
			return this.restaurantId == dept.restaurantId && this.deptId == dept.deptId;
		}
	}
	
	@Override
	public int hashCode(){
		int result = 17;
		result = result * 31 + restaurantId;
		result = result * 31 + deptId;
		return result;
	}
	
	@Override
	public String toString(){
		return "department(dept_id = " + getId() +
						   ",name = " + getName() +
						   ",restaurant_id = " + getRestaurantId() + ")";
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == DEPT_PARCELABLE_SIMPLE){
			dest.writeByte(this.deptId);
			
		}else if(flag == DEPT_PARCELABLE_COMPLEX){
			dest.writeByte(this.deptId);
			dest.writeByte(this.deptType.getVal());
			dest.writeString(this.deptName);
			dest.writeInt(this.displayId);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == DEPT_PARCELABLE_SIMPLE){
			this.deptId = source.readByte();
			
		}else if(flag == DEPT_PARCELABLE_COMPLEX){
			this.deptId = source.readByte();
			this.deptType = Type.valueOf(source.readByte());
			this.deptName = source.readString();
			this.displayId = source.readInt();
		}
	}

	public final static Parcelable.Creator<Department> DEPT_CREATOR = new Parcelable.Creator<Department>(){

		@Override
		public Department newInstance() {
			return new Department(0);
		}

		@Override
		public Department[] newInstance(int size) {
			return new Department[size];
		}
		
	};


	@Override
	public int compareTo(Department dept) {
		if(getId() > dept.getId()){
			return 1;
		}else if(getId() < dept.getId()){
			return -1;
		}else{
			return 0;
		}
	}

	public static enum Key4Json{
		DEPT_ID("id", "部门id"),
		DEPT_NAME("name", "部门名称"),
		RESTAURANT_ID("rid", "餐厅id"),
		DEPT_TYPE("typeValue", "部门类型");
		
		private final String key;
		private final String desc;
		
		Key4Json(String key, String desc){
			this.key = key;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "key = " + key + ",desc = " + desc;
		}
	}
	
	public final static int DEPT_JSONABLE_COMPLEX = 0;
	public final static int DEPT_JSONABLE_SIMPLE = 1;
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		if(flag == DEPT_JSONABLE_SIMPLE){
			jm.putShort(Key4Json.DEPT_ID.key, this.deptId);
		}else{
			jm.putInt(Key4Json.RESTAURANT_ID.key, this.restaurantId);
			jm.putShort(Key4Json.DEPT_ID.key, this.deptId);
			jm.putString(Key4Json.DEPT_NAME.key, this.deptName);
			jm.putInt(Key4Json.DEPT_TYPE.key, this.deptType.getVal());
		}		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

}
