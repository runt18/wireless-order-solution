package com.wireless.pojo.billStatistics;

public class PaymentGeneral {
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
	
}
