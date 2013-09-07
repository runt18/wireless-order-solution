package com.wireless.pojo.menuMgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;


public class Department implements Parcelable, Comparable<Department>, Jsonable{
	
	public final static byte DEPT_PARCELABLE_COMPLEX = 0;
	public final static byte DEPT_PARCELABLE_SIMPLE = 1;
	
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
		DEPT_TMP(253, "临时部门", Type.RESERVED),
		DEPT_ALL(254, "全部部门", Type.RESERVED),
		DEPT_NULL(255, "空部门", Type.RESERVED);


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
		RESERVED(1, "保留");
		
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

	private Department(InsertBuilder builder){
		this.restaurantId = builder.restaurantId;
		this.deptId = builder.deptId;
		this.deptName = builder.deptName;
		this.deptType = builder.deptType;
	}
	
	public Department(){
		
	}
	
	public Department(int restaurantId, short deptId, String deptName){
		this.restaurantId = restaurantId;
		this.deptId = deptId;
		this.deptName = deptName;
	}
	
	public Department(String deptName, short deptId, int restaurantId, Type type){
		this.restaurantId = restaurantId;
		this.deptId = deptId;
		this.deptName = deptName;
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
	
	public String getName() {
		if(deptName == null){
			deptName = "";
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
	
	public boolean isReserved(){
		return this.deptType == Type.RESERVED;
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
		return "department(dept_id = " + getId() + ",restaurant_id = " + getRestaurantId() + ")";
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == DEPT_PARCELABLE_SIMPLE){
			dest.writeByte(this.deptId);
			
		}else if(flag == DEPT_PARCELABLE_COMPLEX){
			dest.writeByte(this.deptId);
			dest.writeByte(this.deptType.getVal());
			dest.writeString(this.deptName);
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
		}
	}

	public final static Parcelable.Creator<Department> DEPT_CREATOR = new Parcelable.Creator<Department>(){

		public Department newInstance() {
			return new Department();
		}

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

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("rid", this.restaurantId);
		jm.put("id", this.deptId);
		jm.put("name", this.deptName);
		jm.put("typeValue", this.deptType.getVal());
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

}
