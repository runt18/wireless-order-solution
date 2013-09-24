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
	
	
	public static enum RequestSource{
		BASIC(1, "后台", "/pages/PersonLoginTimeout.html"),
		FRONT(2, "前台",  ""),
		TOUCH(3, "触摸屏", "touch");
		
		private final int val;
		private final String desc;
		private final String redirect;
		
		RequestSource(int val, String desc, String redirect){
			this.val = val;
			this.desc = desc;
			this.redirect = redirect;
		}
		
		public static RequestSource valueOf(int val){
			for(RequestSource from : values()){
				if(from.val == val){
					return from;
				}
			}
			throw new IllegalArgumentException("The ComeFrom(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
		
		public String  getRedirect() {
			return redirect;
		}
		
		@Override
		public String toString(){
			return "(val : " + this.val + ",desc : " + this.desc + ",redirect : " + this.redirect + ")";
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
	

	public static class DefAdminBuilder extends StaffInsertBuilder{
		
		public final static String ADMIN = "管理员";
		
		public DefAdminBuilder(String pwd, int restaurantId, Role role){
			super(ADMIN, pwd, restaurantId, role);
			setType(Type.RESERVED);
		}
	}
	
	public static class StaffInsertBuilder{
		private final int restaurantId;
		private final Role role;
		private final String name;
		private final String pwd;
		private String mobile;
		private Type type = Type.NORMAL;

		public StaffInsertBuilder(String name, String pwd, int restaurantId, Role role){
			this.name = name;
			this.restaurantId = restaurantId;
			this.role = role;
			this.pwd = pwd;
		}
		
		public int getRestaurantId() {
			return restaurantId;
		}
		public Role getRole() {
			return role;
		}
		public String getMobile() {
			if(mobile == null){
				mobile = "";
			}
			return mobile;
		}
		public void setMobile(String mobile) {
			this.mobile = mobile;
		}
		public String getName() {
			return name;
		}
		public String getPwd() {
			return pwd;
		}
		public Type getType() {
			return type;
		}
		public void setType(Type type) {
			this.type = type;
		}
		
		public Staff build(){
			return new Staff(this);
		}
		
	}
	
	
	public static class StaffUpdateBuilder{
		private final int staffId;
		private String staffName;
		private String staffPwd;
		private String mobile;
		private int roleId;

		public StaffUpdateBuilder(int staffId){
			this.staffId = staffId;
		}
		
		public int getStaffId() {
			return staffId;
		}
		
		public int getRoleId() {
			return roleId;
		}
		public StaffUpdateBuilder setRoleId(int roleId) {
			this.roleId = roleId;
			return this;
		}
		
		public String getStaffName() {
			return staffName;
		}
		
		public StaffUpdateBuilder setStaffName(String staffName) {
			this.staffName = staffName;
			return this;
		}
		
		public String getStaffPwd() {
			return staffPwd;
		}
		
		public StaffUpdateBuilder setStaffPwd(String pwd) {
			if(pwd != null){
				if(pwd.trim().length() > 0){
					this.staffPwd = pwd;
				}
			}
			return this;
		}
		
		public String getMobile() {
			return mobile;
		}
		
		public StaffUpdateBuilder setMobile(String mobile) {
			this.mobile = mobile;
			return this;
		}
		
		public Staff build(){
			return new Staff(this);
		}
		
	}
	
	
	public Staff(){
		
	}
	
	public Staff(int id){
		setId(id);
	}
	
	public Staff(String name, int restaurantId){
		setName(name);
		setRestaurantId(restaurantId);
	}
	
	private Staff(StaffInsertBuilder builder){
		setRestaurantId(builder.getRestaurantId());
		setRole(builder.getRole());
		setMobile(builder.getMobile());
		setName(builder.getName());
		setPwd(builder.getPwd());
		setType(builder.getType());
	}
	
	private Staff(StaffUpdateBuilder builder){
		setMobile(builder.getMobile());
		setName(builder.getStaffName());
		setPwd(builder.getStaffPwd());
		setId(builder.getStaffId());
		setRole(new Role(builder.getRoleId()));
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
		if(mobile == null){
			mobile = "";
		}
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPwd() {
		if(pwd == null){
			pwd = "";
		}
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
		jm.put("typeValue", this.getType().getVal());
		jm.put("typeText", this.getType().getDesc());
		jm.put("role", this.getRole());
		jm.put("roleName", this.getRole().getName());
		jm.put("staffPassword", this.getPwd());
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
}
