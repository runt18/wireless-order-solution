package com.wireless.pojo.menuMgr;

import java.util.Comparator;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.menuMgr.Department.DeptId;


public class Kitchen implements Parcelable, Comparable<Kitchen>, Jsonable{
	
	public final static Comparator<Kitchen> BY_DISPLAY = new Comparator<Kitchen>(){
		@Override
		public int compare(Kitchen k1, Kitchen k2) {
			if(k1.displayId > k2.displayId){
				return 1;
			}else if(k1.displayId < k2.displayId){
				return -1;
			}else{
				return 0;
			}
		}
	};
	
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
		private boolean isAllowTempChanged = false;
		
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
			isAllowTempChanged = true;
			return this;
		}
		
		public boolean isAllowTmpChanged(){
			return this.isAllowTempChanged;
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
	private final Department dept = new Department(0);
	
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
	
	public void copyFrom(Kitchen src){
		if(src != null && src != this){
			setId(src.getId());
			setRestaurantId(src.getRestaurantId());
			setName(src.getName());
			setDisplayId(src.getDisplayId());
			setAllowTemp(src.isAllowTemp());
			setType(src.getType());
			setDept(src.dept);
		}
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
	
	public Department getDept() {
		return this.dept;
	}
	
	public void setDept(Department dept) {
		if(dept != null){
			this.dept.copyFrom(dept);
		}
	}
	
	public void setDept(short deptId, String deptName) {
		this.dept.setRestaurantId(restaurantId);
		this.dept.setId(deptId);
		this.dept.setName(deptName);
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

	public final static Parcelable.Creator<Kitchen> CREATOR = new Parcelable.Creator<Kitchen>() {
		
		@Override
		public Kitchen[] newInstance(int size) {
			return new Kitchen[size];
		}
		
		@Override
		public Kitchen newInstance() {
			return new Kitchen(0);
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

	public static enum Key4Json{
		KITCHEN_ID("id", "厨房id"),
		DISPLAY_ID("alias", "显示id"),
		RESTAURANT_ID("rid", "餐厅id"),
		KITCHEN_NAME("name", "厨房名称"),
		IS_ALLOW_TEMP("isAllowTmp", "是否允许临时菜"),
		KITCHEN_TYPE("typeValue", "厨房类型"),
		ASSOCIATED_DEPT("dept", "所属部门");
		
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
	
	public final static int KITCHEN_JSONABLE_COMPLEX = 0;
	public final static int KITCHEN_JSONABLE_SIMPLE = 1;
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		
		if(flag == KITCHEN_JSONABLE_SIMPLE){
			jm.putInt(Key4Json.KITCHEN_ID.key, this.kitchenId);
			jm.putJsonable(Key4Json.ASSOCIATED_DEPT.key, this.dept, Department.DEPT_JSONABLE_SIMPLE);
		}else{
			jm.putInt(Key4Json.KITCHEN_ID.key, this.kitchenId);
			jm.putInt(Key4Json.DISPLAY_ID.key, this.displayId);
			jm.putInt(Key4Json.RESTAURANT_ID.key, this.restaurantId);
			jm.putString(Key4Json.KITCHEN_NAME.key, this.name);
			jm.putBoolean(Key4Json.IS_ALLOW_TEMP.key, this.isAllowTmp);
			jm.putInt(Key4Json.KITCHEN_TYPE.key, this.type.getVal());
			jm.putJsonable(Key4Json.ASSOCIATED_DEPT.key, this.dept, Department.DEPT_JSONABLE_COMPLEX);
		}		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		if(flag == KITCHEN_JSONABLE_SIMPLE){
			if(jsonMap.containsKey(Key4Json.KITCHEN_ID.key)){
				setId(jsonMap.getInt(Key4Json.KITCHEN_ID.key));
			}else{
				throw new IllegalStateException("厨房数据缺少(" + Key4Json.KITCHEN_ID.toString() + ")");
			}
		}
	}

	public static Jsonable.Creator<Kitchen> JSON_CREATOR = new Jsonable.Creator<Kitchen>() {
		@Override
		public Kitchen newInstance() {
			return new Kitchen(0);
		}
	};
	
}
