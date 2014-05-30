package com.wireless.pojo.sms;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class SMSDetail implements Jsonable{

	public static enum Operation{
		USE_VERIFY(1, "使用-验证"),
		USE_CONSUME(2, "使用-消费"),
		USE_CHARGE(3, "使用-充值"),
		ADD(4, "增加"),
		DEDUCT(5, "减少");
		
		
		private final int val;
		private final String desc;
		
		Operation(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		public static Operation valueOf(int val){
			for(Operation operation : values()){
				if(operation.val == val){
					return operation;
				}
			}
			throw new IllegalArgumentException("The operation(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
		
	}
	
	private int id;
	private int restaurantId;
	private long modified;
	private int delta;
	private int remaining;
	private Operation operation;
	private String staff;
	
	public SMSDetail(int id){
		this.id = id;
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
	
	public long getModified() {
		return modified;
	}
	
	public void setModified(long modified) {
		this.modified = modified;
	}
	
	public int getDelta() {
		return delta;
	}
	
	public void setDelta(int delta) {
		this.delta = delta;
	}
	
	public int getRemaining() {
		return remaining;
	}
	
	public void setRemaining(int remaining) {
		this.remaining = remaining;
	}
	
	public Operation getOperation() {
		return operation;
	}
	
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	
	public String getStaff() {
		if(staff == null){
			return "";
		}
		return staff;
	}
	
	public void setStaff(String staff) {
		this.staff = staff;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putString("modifiedFormad", DateUtil.format(this.modified));
		jm.putInt("operationValue", this.operation.getVal());
		jm.putString("operationText", this.operation.getDesc());
		jm.putInt("delta", this.delta);
		jm.putInt("remaining", this.remaining);
		jm.putString("staffName", this.staff);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
