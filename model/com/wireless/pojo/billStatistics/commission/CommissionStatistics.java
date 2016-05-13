package com.wireless.pojo.billStatistics.commission;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.NumericUtil;

public class CommissionStatistics implements Jsonable{

	private int id;				//
	private long orderDate ;	//
	private String foodName;
	private Department dept;
	private int orderId;
	private String restaurantName;
	private float unitPrice;
	private float amount;
	private float totalPrice;
	private float commission;
	private String waiter;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRestaurantName() {
		return restaurantName;
	}
	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}
	public long getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}

	public Department getDept() {
		return dept;
	}
	public void setDept(Department dept) {
		this.dept = dept;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int order_id) {
		this.orderId = order_id;
	}
	public float getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public float getTotalPrice() {
		totalPrice = NumericUtil.roundFloat(totalPrice);
		return totalPrice;
	}
	public void setTotalPrice(float totalPrice) {
		this.totalPrice = totalPrice;
	}
	public float getCommission() {
		return commission;
	}
	public void setCommission(float commission) {
		this.commission = commission;
	}
	public String getFoodName() {
		return foodName;
	}
	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}
	public String getWaiter() {
		return waiter;
	}
	public void setWaiter(String waiter) {
		this.waiter = waiter;
	}
	@Override
	public int hashCode(){
		return 17 + 31 * id;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof CommissionStatistics)){
			return false;
		}else{
			return id == ((CommissionStatistics)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return "CommissionStatistics(orderId = " + orderId + ")";
	}
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		if(flag == 0){
			jm.putString("orderDateFormat", DateUtil.format(orderDate));
			jm.putString("foodName", this.foodName);
			jm.putString("dept", getDept() != null ? getDept().getName() : "");
			jm.putInt("orderId", this.orderId);
			jm.putFloat("unitPrice", this.unitPrice);
			jm.putFloat("amount", this.amount);
		}
		jm.putFloat("totalPrice", this.getTotalPrice());
		jm.putFloat("commission", this.commission);
		jm.putString("staffName",this.waiter);
		jm.putString("restaurantName", this.restaurantName);
		return jm;
	}
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	

}
