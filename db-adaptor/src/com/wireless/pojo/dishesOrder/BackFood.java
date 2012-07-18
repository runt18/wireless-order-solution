package com.wireless.pojo.dishesOrder;

import java.text.SimpleDateFormat;

public class BackFood {
	private long orderID;           // 账单号
	private long orderDate;			// 账单时间
	private long foodID;			// 食品编号
	private String foodName;		// 食品名称
	private long deptID;			// 部门编号
	private String deptName;		// 部门名称
	private float price;			// 退菜单价
	private float count;			// 退菜数量
	private float totalPrice;		// 退菜金额
	private String waiter;			// 操作服务员
	private long reasonID;			// 退菜原因编号
	private String reason;			// 退菜原因
	
	public BackFood(){}
	
	public BackFood(String deptName, String foodName){
		this.deptName = deptName;
		this.foodName = foodName;
	}
	
	public long getOrderID() {
		return orderID;
	}
	public void setOrderID(long orderID) {
		this.orderID = orderID;
	}
	public long getOrderDate() {
		return orderDate;
	}
	public String getOrderDateFormat() {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(orderDate);
	}
	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}
	public long getFoodID() {
		return foodID;
	}
	public void setFoodID(long foodID) {
		this.foodID = foodID;
	}
	public String getFoodName() {
		return foodName;
	}
	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}
	public long getDeptID() {
		return deptID;
	}
	public void setDeptID(long deptID) {
		this.deptID = deptID;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	public float getCount() {
		return count;
	}
	public void setCount(float count) {
		this.count = count;
	}
	public float getTotalPrice() {
		if(totalPrice == 0){
			totalPrice = Math.abs(this.getCount()) * this.getPrice();
		}		
		return totalPrice;
	}
	public void setTotalPrice(float totalPrice) {		
		this.totalPrice = totalPrice;
	}
	public String getWaiter() {
		return waiter;
	}
	public void setWaiter(String waiter) {
		this.waiter = waiter;
	}

	public long getReasonID() {
		return reasonID;
	}

	public void setReasonID(long reasonID) {
		this.reasonID = reasonID;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	
}
