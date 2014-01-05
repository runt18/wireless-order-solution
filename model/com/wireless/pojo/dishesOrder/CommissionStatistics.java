package com.wireless.pojo.dishesOrder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	public long getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}

	public Department getDept() {
		if(dept == null){
			dept = new Department();
		}
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
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		if(flag == 0){
			jm.put("orderDateFormat", DateUtil.format(orderDate));
			jm.put("foodName", this.foodName);
			jm.put("dept", getDept().getName());
			jm.put("orderId", this.orderId);
			jm.put("unitPrice", this.unitPrice);
			jm.put("amount", this.amount);
		}
		jm.put("totalPrice", this.getTotalPrice());
		jm.put("commission", this.commission);
		jm.put("staffName",this.waiter);
		
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	
	

}
