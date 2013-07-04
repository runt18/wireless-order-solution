package com.wireless.pojo.dishesOrder;

import com.wireless.pojo.util.DateUtil;

public class CancelledFood {
	private long orderID;           // 账单号
	private long orderDate;			// 账单时间
	private long foodID;			// 食品编号
	private String foodName;		// 食品名称
	private int deptID;				// 部门编号
	private String deptName;		// 部门名称
	private float unitPrice;		// 退菜单价
	private float count;			// 退菜数量
	private float totalPrice;		// 退菜金额
	private String waiter;			// 操作服务员
	private String reason;			// 退菜原因
	
	public CancelledFood(){}
	public CancelledFood(OrderFood of){
		this.orderID = of.getOrderId();
		this.orderDate = of.getOrderDate();
		this.foodID = of.getFoodId();
		this.foodName = of.getName();
		this.deptID = of.getKitchen().getDept().getId();
		this.deptName = of.getKitchen().getDept().getName();
		this.unitPrice = Math.abs(of.getPrice());
		this.count = Math.abs(of.getCount());
		this.totalPrice = Math.abs(of.calcPriceWithTaste());
		this.waiter = of.getWaiter();
		this.reason = of.getCancelReason().getReason();
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
		return DateUtil.format(orderDate);
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
	public int getDeptID() {
		return deptID;
	}
	public void setDeptID(int deptID) {
		this.deptID = deptID;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public float getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}
	public float getCount() {
		return count;
	}
	public void setCount(float count) {
		this.count = count;
	}
	public float getTotalPrice() {
		if(totalPrice == 0){
			totalPrice = Math.abs(this.getCount()) * this.getUnitPrice();
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
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
	
}
