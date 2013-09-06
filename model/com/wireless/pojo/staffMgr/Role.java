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
		MANAGER(4, "店长"),
		CASHIER(5, "收银"),
		WAITER(6, "服务员"),
		OTHER(7, "其他");
		
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
	
	//The helper insert builder for '管理员'
	public static class DefAdminBuilder extends InsertBuilder{
		public DefAdminBuilder(int restaurantId){
			super(restaurantId, Category.ADMIN.getDesc());
			setType(Type.RESERVED);
			setCategoty(Category.ADMIN);
			
			addPrivileges(new Privilege(Privilege.Code.FRONT_BUSINESS));
			addPrivileges(new Privilege(Privilege.Code.BASIC));
			addPrivileges(new Privilege(Privilege.Code.CANCEL_FOOD));
			addPrivileges(new Privilege(Privilege.Code.DISCOUNT));
			addPrivileges(new Privilege(Privilege.Code.GIFT));
			addPrivileges(new Privilege(Privilege.Code.HISTORY));
			addPrivileges(new Privilege(Privilege.Code.INVENTORY));
			addPrivileges(new Privilege(Privilege.Code.MEMBER));
			addPrivileges(new Privilege(Privilege.Code.RE_PAID));
			addPrivileges(new Privilege(Privilege.Code.SYSTEM));
			addPrivileges(new Privilege(Privilege.Code.PAY));
			addPrivileges(new Privilege(Privilege.Code.CHECK_ORDER));
		}
	}
	
	//The helper insert builder for '老板'
	public static class DefBossBuilder extends InsertBuilder{
		public DefBossBuilder(int restaurantId){
			super(restaurantId, Category.BOSS.getDesc());
			setType(Type.RESERVED);
			setCategoty(Category.BOSS);
			addPrivileges(new Privilege(Privilege.Code.FRONT_BUSINESS));
			addPrivileges(new Privilege(Privilege.Code.BASIC));
			addPrivileges(new Privilege(Privilege.Code.CANCEL_FOOD));
			addPrivileges(new Privilege(Privilege.Code.DISCOUNT));
			addPrivileges(new Privilege(Privilege.Code.GIFT));
			addPrivileges(new Privilege(Privilege.Code.HISTORY));
			addPrivileges(new Privilege(Privilege.Code.INVENTORY));
			addPrivileges(new Privilege(Privilege.Code.MEMBER));
			addPrivileges(new Privilege(Privilege.Code.RE_PAID));
			addPrivileges(new Privilege(Privilege.Code.SYSTEM));
			addPrivileges(new Privilege(Privilege.Code.PAY));
			addPrivileges(new Privilege(Privilege.Code.CHECK_ORDER));
		}
	}
	
	//The helper insert builder for '财务'
	public static class DefFinanceBuilder extends InsertBuilder{
		public DefFinanceBuilder(int restaurantId){
			super(restaurantId, Category.FINANCE.getDesc());
			setType(Type.NORMAL);
			setCategoty(Category.FINANCE);
			addPrivileges(new Privilege(Privilege.Code.FRONT_BUSINESS));
			addPrivileges(new Privilege(Privilege.Code.BASIC));
			addPrivileges(new Privilege(Privilege.Code.CANCEL_FOOD));
			addPrivileges(new Privilege(Privilege.Code.DISCOUNT));
			addPrivileges(new Privilege(Privilege.Code.GIFT));
			addPrivileges(new Privilege(Privilege.Code.HISTORY));
			addPrivileges(new Privilege(Privilege.Code.INVENTORY));
			addPrivileges(new Privilege(Privilege.Code.MEMBER));
			addPrivileges(new Privilege(Privilege.Code.RE_PAID));
			addPrivileges(new Privilege(Privilege.Code.PAY));
			addPrivileges(new Privilege(Privilege.Code.CHECK_ORDER));
		}
	}
	
	//The helper insert builder for '店长'
	public static class DefManagerBuilder extends InsertBuilder{
		public DefManagerBuilder(int restaurantId){
			super(restaurantId, Category.MANAGER.getDesc());
			setType(Type.NORMAL);
			setCategoty(Category.MANAGER);
			addPrivileges(new Privilege(Privilege.Code.FRONT_BUSINESS));
			addPrivileges(new Privilege(Privilege.Code.CANCEL_FOOD));
			addPrivileges(new Privilege(Privilege.Code.DISCOUNT));
			addPrivileges(new Privilege(Privilege.Code.GIFT));
			addPrivileges(new Privilege(Privilege.Code.RE_PAID));
			addPrivileges(new Privilege(Privilege.Code.PAY));
			addPrivileges(new Privilege(Privilege.Code.CHECK_ORDER));
			addPrivileges(new Privilege(Privilege.Code.BASIC));
		}
	}
	
	//The helper insert builder for '收银员'
	public static class DefCashierBuilder extends InsertBuilder{
		public DefCashierBuilder(int restaurantId){
			super(restaurantId, Category.CASHIER.getDesc());
			setType(Type.NORMAL);
			setCategoty(Category.CASHIER);
			addPrivileges(new Privilege(Privilege.Code.FRONT_BUSINESS));
			addPrivileges(new Privilege(Privilege.Code.CANCEL_FOOD));
			addPrivileges(new Privilege(Privilege.Code.DISCOUNT));
			addPrivileges(new Privilege(Privilege.Code.GIFT));
			addPrivileges(new Privilege(Privilege.Code.RE_PAID));
			addPrivileges(new Privilege(Privilege.Code.PAY));
			addPrivileges(new Privilege(Privilege.Code.CHECK_ORDER));
		}
	}
	
	//The helper insert builder for '服务员'
	public static class DefWaiterBuilder extends InsertBuilder{
		public DefWaiterBuilder(int restaurantId){
			super(restaurantId, Category.WAITER.getDesc());
			setType(Type.NORMAL);
			setCategoty(Category.WAITER);
			addPrivileges(new Privilege(Privilege.Code.FRONT_BUSINESS));
			addPrivileges(new Privilege(Privilege.Code.CANCEL_FOOD));
		}
	}
	
	public static class InsertBuilder{
		private final int restaurantId;
		private final String name;
		private Type type = Type.NORMAL;
		private Category categoty = Category.OTHER;
		private List<Privilege> privileges = SortedList.newInstance();
		
		public InsertBuilder(int restaurantId, String name){
			this.restaurantId = restaurantId;
			this.name = name;
		}
		
		public int getRestaurantId() {
			return restaurantId;
		}
		public String getName() {
			return name;
		}
		public Type getType() {
			return type;
		}
		public void setType(Type type) {
			this.type = type;
		}
		public Category getCategoty() {
			return categoty;
		}
		public void setCategoty(Category categoty) {
			this.categoty = categoty;
		}
		public List<Privilege> getPrivileges() {
			return privileges;
		}
		public void addPrivileges(Privilege privilege) {
			this.privileges.add(privilege);
		}
		
		public Role build(){
			return new Role(this);
		}
		
		
	}
	
	public static class UpdateRoleBuilder{
		private int roleId;
		private String name;
		private List<Privilege> privileges = SortedList.newInstance();
		public int getRoleId() {
			return roleId;
		}
		public void setRoleId(int roleId) {
			this.roleId = roleId;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public List<Privilege> getPrivileges() {
			return privileges;
		}
		public void addPrivileges(Privilege privilege) {
			this.privileges.add(privilege);
		}
		
		public Role build(){
			return new Role(this);
		}
		
	}
	
	
	
	public Role(int id){
		setId(id);
	}
	
	private Role(InsertBuilder builder){
		setCategory(builder.getCategoty());
		setName(builder.getName());
		setRestaurantId(builder.getRestaurantId());
		setType(builder.getType());
		this.privileges.addAll(builder.getPrivileges());
	}
	
	private Role(UpdateRoleBuilder updateBuilder){
		setId(updateBuilder.getRoleId());
		setName(updateBuilder.getName());
		this.privileges.addAll(updateBuilder.getPrivileges());
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
	
	public void clearPrivilege(){
		privileges.clear();
	}
	
	public void addAllPrivileges(List<Privilege> list){
		privileges.addAll(list);
	}

	public List<Privilege> getPrivileges(){
		return Collections.unmodifiableList(privileges);
	}
	
	public void addPrivilege(Privilege privilege){
		if(privilege != null && !hasPrivilege(privilege.getCode())){
			privileges.add(privilege);
		}
	}
	
	public boolean hasPrivilege(Privilege.Code code){
		return privileges.contains(new Privilege(0, code, 0));
	}
	

	
	@Override 
	public int hashCode(){
		return 31 * category.getVal() + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Role)){
			return false;
		}else{
			return category == ((Role)obj).getCategory();
		}
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
