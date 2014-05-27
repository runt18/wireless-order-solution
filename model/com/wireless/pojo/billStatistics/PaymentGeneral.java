package com.wireless.pojo.billStatistics;

import java.util.Map;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class PaymentGeneral implements Jsonable{
	private String onDuty;				//开始时间
	private String offDuty;				//结束时间
	
	private int restaurantId;			//餐厅编号
	private String staff;				//交款人
	
	public int getRestaurantId(){
		return this.restaurantId;
	}
	
	public void setRestaurantId(int restaurantId){
		this.restaurantId = restaurantId;
	}
	
	public String getStaffName(){
		return this.staff;
	}
	
	public void setStaffName(String staffName){
		this.staff = staffName;
	}
	
	public String getOnDuty() {
		return onDuty;
	}
	
	public void setOnDuty(String onDuty) {
		this.onDuty = onDuty;
	}
	
	public String getOffDuty() {
		return offDuty;
	}
	
	public void setOffDuty(String offDuty) {
		this.offDuty = offDuty;
	}
	
	public static enum Key4Json{
		STAFF_NAME("staffName", "交款人"),
		ON_DUTY("onDutyFormat", "开始时间"),
		OFF_DUTY("offDutyFormat", "结束时间");
		
		Key4Json(String key, String desc){
			this.key = key;
			this.desc = desc;
		}
		
		private final String key;
		private final String desc;
		
		@Override
		public String toString(){
			return "key = " + key + ",desc = " + desc;
		}
		
	}

	@Override
	public Map<String, Object> toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString(Key4Json.STAFF_NAME.key, this.getStaffName());
		jm.putString(Key4Json.ON_DUTY.key, this.getOnDuty());
		jm.putString(Key4Json.OFF_DUTY.key, this.getOffDuty());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
