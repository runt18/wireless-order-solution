package com.wireless.pojo.staffMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.util.SortedList;



public class Role implements Jsonable, Parcelable{

	public final static int ROLE_PARCELABLE_SIMPLE = 0;
	public final static int ROLE_PARCELABLE_COMPLEX = 1;
	
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
	
	public static enum Category{
		ADMIN(1, "管理员"),
		BOSS(2, "老板"),
		FINANCE(3, "财务"),
		MANAGER(4, "部长"),
		WAITER(5, "服务员"),
		OTHER(6, "其他");
		
		private final int val;
		private final String desc;
		
		Category(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Category valueOf(int val){
			for(Category type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The category(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
	}
	
	private int id;
	private int restaurantId;
	private String name;
	private Category category = Category.OTHER;
	private Type type = Type.NORMAL;
	private List<Privilege> privileges = SortedList.newInstance();
	
	Role(){
		
	}
	
	public Role(int id){
		setId(id);
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
	
	public Category getCategory() {
		return category;
	}
	
	public void setCategory(Category category) {
		this.category = category;
	}
	
	public Type getType(){
		return this.type;
	}
	
	public void setType(Type type){
		this.type = type;
	}

	public List<Privilege> getPrivileges(){
		return Collections.unmodifiableList(privileges);
	}
	
	public void addPrivilege(Privilege privilege){
		if(privilege != null){
			privileges.add(privilege);
		}
	}
	
	public boolean hasPrivilege(Privilege.Code code){
		return privileges.contains(new Privilege(0, code, 0));
	}
	
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("id", this.getId());
		jm.put("name", this.getName());
		jm.put("restaurantId", this.getRestaurantId());
		jm.put("categoryValue", this.getCategory().getVal());
		jm.put("categoryText", this.getCategory().getDesc());
		jm.put("typeValue", this.getType().getVal());
		jm.put("typeText", this.getType().getDesc());
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeString(getName());
		dest.writeInt(getCategory().getVal());
		dest.writeInt(getType().getVal());
		dest.writeParcelList(privileges, 0);
	}

	@Override
	public void createFromParcel(Parcel source) {
		setName(source.readString());
		setCategory(Category.valueOf(source.readInt()));
		setType(Type.valueOf(source.readInt()));
		privileges.clear();
		privileges.addAll(source.readParcelList(Privilege.CREATOR));
	}	
	
	public final static Parcelable.Creator<Role> CREATOR = new Parcelable.Creator<Role>(){

		@Override
		public Role newInstance() {
			return new Role();
		}
		
		@Override
		public Role[] newInstance(int size){
			return new Role[size];
		}
		
	};
}
