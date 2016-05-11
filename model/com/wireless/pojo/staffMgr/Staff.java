package com.wireless.pojo.staffMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.restaurantMgr.Restaurant;
public class Staff implements Parcelable, Jsonable{ 

	public final static byte ST_PARCELABLE_COMPLEX = 1;
	public final static byte ST_PARCELABLE_SIMPLE = 0;
	
	public static enum Type{
		NORMAL(1, "普通"),
		RESERVED(2, "系统保留"),
		WX(3, "微信客人");
		
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
	//the group id to this staff
	private int groupId;
	//the restaurant type to this staff
	private Restaurant.Type restaurantType;

	public static class AdminBuilder extends InsertBuilder{
		
		public final static String ADMIN = "管理员";
		
		public AdminBuilder(String pwd, Role role){
			super(ADMIN, pwd, role);
			super.type = Type.RESERVED;
		}
	}
	
	public static class InsertBuilder{
		private final Role role;
		private final String name;
		private final String pwd;
		private String mobile;
		private Type type = Type.NORMAL;

		public InsertBuilder(String name, String pwd, Role role){
			this.name = name;
			this.role = role;
			this.pwd = pwd;
		}
		
		public InsertBuilder setMobile(String mobile) {
			this.mobile = mobile;
			return this;
		}
		
		public Staff build(){
			return new Staff(this);
		}
		
		void setType(Type type){
			this.type = type;
		}
	}
	
	
	public static class UpdateBuilder{
		private final int staffId;
		private String staffName;
		private String staffPwd;
		private String mobile;
		private int roleId;

		public UpdateBuilder(int staffId){
			this.staffId = staffId;
		}
		
		public UpdateBuilder setRoleId(int roleId) {
			this.roleId = roleId;
			return this;
		}
		
		public boolean isRoleChanged(){
			return roleId != 0;
		}
		
		public UpdateBuilder setStaffName(String staffName) {
			this.staffName = staffName;
			return this;
		}
		
		public boolean isNameChanged(){
			return this.staffName != null;
		}
		
		public UpdateBuilder setStaffPwd(String pwd) {
			if(pwd != null){
				if(pwd.trim().length() > 0){
					this.staffPwd = pwd;
				}
			}
			return this;
		}
		
		public boolean isPwdChanged(){
			return this.staffPwd != null;
		}
		
		public UpdateBuilder setMobile(String mobile) {
			this.mobile = mobile;
			return this;
		}
		
		public boolean isMobileChanged(){
			return this.mobile != null;
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
	
	public Staff(int id, String name){
		setId(id);
		setName(name);
	}
	
	private Staff(InsertBuilder builder){
		setRole(builder.role);
		setMobile(builder.mobile);
		setName(builder.name);
		setPwd(builder.pwd);
		setType(builder.type);
	}
	
	private Staff(UpdateBuilder builder){
		setMobile(builder.mobile);
		setName(builder.staffName);
		setPwd(builder.staffPwd);
		setId(builder.staffId);
		setRole(new Role(builder.roleId));
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
			return "";
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
			return "";
		}
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPwd() {
		if(pwd == null){
			return "";
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
	
	public int getGroupId(){
		return this.groupId;
	}
	
	public void setGroupId(int groupId){
		this.groupId = groupId;
	}
	
	public void setRestaurantType(Restaurant.Type type){
		this.restaurantType = type;
	}
	
	public boolean isGroup(){
		return this.restaurantType == Restaurant.Type.GROUP;
	}
	
	public boolean isBranch(){
		return this.restaurantType == Restaurant.Type.BRANCE;
	}
	
	public boolean isRestaurant(){
		return this.restaurantType == Restaurant.Type.RESTAURANT;
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
	
	@Override
	public String toString(){
		return "staff(id = " + getId() + ",name = " + getName() + ")";
	}
	
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == ST_PARCELABLE_SIMPLE){
			dest.writeString(this.name);
			dest.writeInt(this.id);
			
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
			this.name = source.readString();
			this.id = source.readInt();
			
		}else if(flag == ST_PARCELABLE_COMPLEX){
			this.name = source.readString();
			this.id = source.readInt();
			this.pwd = source.readString();
			this.restaurantId = source.readInt();
			this.role = source.readParcel(Role.CREATOR);
		}
	}
	
	public final static Parcelable.Creator<Staff> CREATOR = new Parcelable.Creator<Staff>() {
		
		public Staff[] newInstance(int size) {
			return new Staff[size];
		}
		
		public Staff newInstance() {
			return new Staff();
		}
	};

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("staffID", this.getId());
		jm.putString("staffName", this.getName());		
		if(flag == ST_PARCELABLE_COMPLEX){
			jm.putInt("restaurantId", this.getRestaurantId());
			jm.putString("mobile", this.getMobile());
			jm.putInt("typeValue", this.getType().getVal());
			jm.putString("typeText", this.getType().getDesc());
			jm.putJsonable("role", this.getRole(), flag);
			jm.putString("roleName", this.getRole() != null ? this.getRole().getName() : "未知");
		}
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
