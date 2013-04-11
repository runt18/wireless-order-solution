package com.wireless.pojo.menuMgr;

import com.wireless.protocol.PKitchen;

public class Kitchen {
	
	public static enum Type{
		NORMAL(PKitchen.TYPE_NORMAL),
		RESERVED(PKitchen.TYPE_RESERVED);
		
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
			throw new IllegalArgumentException("The kitchen type(value = " + val + ") passed is invaild.");
		}
		
		@Override
		public String toString(){
			if(this == NORMAL){
				return "normal kitchen";
			}else if(this == RESERVED){
				return "reserved kitchen";
			}else{
				return "unknown type";
			}
		}
	}
	
	private long kitchenId;
	private short aliasId;
	private int restaurantId;
	private String name;
	private boolean isAllowTmp;
	private Type type;
	private Department dept;
	
	public Kitchen(){
		this.dept = new Department();
	}
	
	public Kitchen(PKitchen protocolObj){
		copyFrom(protocolObj);
	}
	
	public final PKitchen toProtocolObj(){
		PKitchen protocolObj = new PKitchen();
		protocolObj.setId(getKitchenID());
		protocolObj.setAliasId(getKitchenAliasID());
		protocolObj.setRestaurantId(getRestaurantID());
		protocolObj.setName(getKitchenName());
		protocolObj.setAllowTemp(isAllowTemp());
		protocolObj.setType(getType().getVal());
		protocolObj.setDept(getDept().toProtocol());
		
		return protocolObj;
	}
	
	public final void copyFrom(PKitchen protocolObj){
		if(protocolObj != null){
			setKitchenID(protocolObj.getId());
			setKitchenAliasID(protocolObj.getAliasId());
			setRestaurantID(protocolObj.getRestaurantId());
			setKitchenName(protocolObj.getName());
			setAllowTemp(protocolObj.isAllowTemp());
			setDept(new Department(protocolObj.getDept()));
			setType(Type.valueOf(protocolObj.getType()));
		}
	}
	
	public long getKitchenID() {
		return this.kitchenId;
	}
	
	public void setKitchenID(long kitchenId) {
		this.kitchenId = kitchenId;
	}
	
	public short getKitchenAliasID() {
		return this.aliasId;
	}
	
	public void setKitchenAliasID(short aliasId) {
		this.aliasId = aliasId;
	}
	
	public String getKitchenName() {
		return this.name;
	}
	
	public void setKitchenName(String kitchenName) {
		this.name = kitchenName;
	}
	
	public int getRestaurantID() {
		return this.restaurantId;
	}
	
	public void setRestaurantID(int restaurantId) {
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
	
	public Short getTypeValue(){
		return this.type != null ? this.type.getVal() : null;
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
		return "kitchen(alias_id = " + getKitchenAliasID() + ",restaurant_id = " + getRestaurantID() + ")";
	}
	
}
