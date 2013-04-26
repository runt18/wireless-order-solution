package com.wireless.pojo.menuMgr;

import com.wireless.protocol.PDepartment;


public class Department {
	
	public static enum Type{
		NORMAL(PDepartment.TYPE_NORMAL),
		RESERVED(PDepartment.TYPE_RESERVED);
		
		private final short val;
		private Type(short val){
			this.val = val;
		}
		
		public short getVal(){
			return this.val;
		}
		
		public static Type valueOf(short val){
			for(Type status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The department type(value = " + val + ") passed is invaild.");
		}
		
		@Override
		public String toString(){
			if(this == NORMAL){
				return "normal department";
			}else{
				return "reserved department";
			}
		}
	}
	
	private int restaurantId;
	private short deptId;
	private String deptName;
	private Type deptType;
	
	
	public Department(){
		
	}
	
	public Department(PDepartment protocolObj){
		copyFrom(protocolObj);
	}
	
	public Department(int restaurantId, short deptId, String deptName){
		setRestaurantID(restaurantId);
		setDeptID(deptId);
		setDeptName(deptName);
		setType(Type.NORMAL);
	}
	
	public final PDepartment toProtocol(){
		PDepartment protocolObj = new PDepartment();
		
		protocolObj.setId(this.getDeptID());
		protocolObj.setName(this.getDeptName());
		protocolObj.setRestaurantId(this.getRestaurantID());
		protocolObj.setType(this.getType().getVal());
		
		return protocolObj;
	}
	
	public final void copyFrom(PDepartment protocolObj){
		
		if(protocolObj != null){
			setRestaurantID(protocolObj.getRestaurantId());
			setDeptID(protocolObj.getId());
			setDeptName(protocolObj.getName());
			setType(Type.valueOf(protocolObj.getType()));
		}		
	}
	
	public int getRestaurantID() {
		return this.restaurantId;
	}
	
	public void setRestaurantID(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public short getDeptID() {
		return this.deptId;
	}
	
	public void setDeptID(short deptId) {
		this.deptId = deptId;
	}
	
	public String getDeptName() {
		return this.deptName;
	}
	
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	
	public Type getType() {
		return this.deptType;
	}
	
	public void setType(Type type){
		this.deptType = type;
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
		return "department(dept_id = " + getDeptID() + ",restaurant_id = " + getRestaurantID() + ")";
	}
	
}
