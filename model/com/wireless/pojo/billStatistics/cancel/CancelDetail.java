package com.wireless.pojo.billStatistics.cancel;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class CancelDetail implements Jsonable{
	private float totalAmount;
	private float totalCancel;
	private long orderDateFormat;
	private String name;
	private String dept;
	private int orderId;
	private float unitPrice;
	private float cancelCount;
	private String waiter;
	private String cancelReason;
	private int rid;
	private String restaurantName;
	
	public String getRestaurantName() {
		return restaurantName;
	}

	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}

	public CancelDetail(){
		
	}
	
	public int getRid(){
		return rid;
	}
	
	public void setRid(int rid){
		this.rid = rid;
	}
	
	public float getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(float totalAmount) {
		this.totalAmount = totalAmount;
	}

	public float getTotalCancel() {
		return totalCancel;
	}

	public void setTotalCancel(float totalCancel) {
		this.totalCancel = totalCancel;
	}

	public long getOrderDateFormat() {
		return orderDateFormat;
	}

	public void setOrderDateFormat(long orderDateFormat) {
		this.orderDateFormat = orderDateFormat;
	}

	public String getName() {
		if(this.name == null){
			return "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDept() {
		if(this.dept == null){
			return "";
		}
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public float getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}

	public float getCancelCount() {
		return cancelCount;
	}

	public void setCancelCount(float cancelCount) {
		this.cancelCount = cancelCount;
	}

	public String getWaiter() {
		if(this.waiter == null){
			return "";
		}
		return waiter;
	}

	public void setWaiter(String waiter) {
		this.waiter = waiter;
	}

	public String getCancelReason() {
		if(this.cancelReason == null){
			return "";
		}
		return cancelReason;
	}

	public void setCancelReason(String cancelReason) {
		this.cancelReason = cancelReason;
	}

	
	
	@Override
	public JsonMap toJsonMap(int flag){
		JsonMap jm = new JsonMap();
		jm.putFloat("totalAmount", this.totalAmount);
		jm.putFloat("totalCancel", this.totalCancel);
		jm.putString("orderDateFormat", DateUtil.format(this.orderDateFormat));
		jm.putString("name", this.name);
		jm.putString("dept", this.dept);
		jm.putInt("orderId", this.orderId);
		jm.putFloat("unitPrice", this.unitPrice);
		jm.putFloat("cancelCount", this.cancelCount);
		jm.putString("waiter", this.waiter);
		jm.putString("cancelReason", this.cancelReason);
		jm.putInt("rid", this.rid);
		jm.putString("restaurantName", this.restaurantName);
		return jm;
	}
	
	@Override
	public void fromJsonMap(JsonMap jm, int flag){
		
	}
}
