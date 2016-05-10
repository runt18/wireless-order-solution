package com.wireless.pojo.billStatistics.gift;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class GiftDetail implements Jsonable{

	private float totalGift;
	private float totalAmount;
	private String restaurantName;
	private int orderId;
	private long orderDateFormat;
	private String name;
	private float count;
	private String waiter;
	private int restaurantId;
	
	
	public GiftDetail(){
		
	}
	
	public float getTotalAmount(){
		return totalAmount;
	}
	
	public float getTotalGift(){
		return totalGift;
	}
	
	public int getOrderId(){
		return orderId;
	}
	
	public long getOrderDateFormat(){
		return orderDateFormat;
	}
	
	public String getName(){
		return name;
	}
	
	public String getWaiter(){
		return waiter;
	}
	
	public void setTotalAmount(float count){
		this.totalAmount = count;
	}
	
	public void setTotalGift(float totalGift){
		this.totalGift = totalGift;
	}
	
	public void setRestaurantName(String restaurantName){
		this.restaurantName = restaurantName;
	}
	
	public void setOrderId(int orderId){
		this.orderId = orderId;
	}
	
	public void setOrderDateFormat(long orderDareFormat){
		this.orderDateFormat = orderDareFormat;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setCount(float count){
		this.count = count;
	}
	
	public void setWaiter(String waiter){
		this.waiter = waiter;
	}
	
	public void setRid(int restaurantId){
		this.restaurantId = restaurantId;
	}
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("restaurantName", this.restaurantName);
		jm.putInt("orderId", this.orderId);
		jm.putString("orderDateFormat", DateUtil.format(this.orderDateFormat));
		jm.putString("name", this.name);
		jm.putFloat("count", this.count);
		jm.putString("waiter", this.waiter);
		jm.putInt("rid", this.restaurantId);
		jm.putFloat("totalAmount", this.totalAmount);
		jm.putFloat("totalGift", this.totalGift);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}

}
