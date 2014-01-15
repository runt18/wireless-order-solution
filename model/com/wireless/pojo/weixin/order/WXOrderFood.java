package com.wireless.pojo.weixin.order;

import com.wireless.pojo.dishesOrder.OrderFood;

public class WXOrderFood {
	private int orderId;
	private int foodId;
	private int foodCount;
	
	public WXOrderFood(){}
	public WXOrderFood(int orderId, int foodId, int foodCount){
		this.orderId = orderId;
		this.foodId = foodId;
		this.foodCount = foodCount;
	}
	public WXOrderFood(int foodId, int foodCount){
		this.foodId = foodId;
		this.foodCount = foodCount;
	}
	
	private OrderFood food;

	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public int getFoodId() {
		return foodId;
	}
	public void setFoodId(int foodId) {
		this.foodId = foodId;
	}
	public int getFoodCount() {
		return foodCount;
	}
	public void setFoodCount(int foodCount) {
		this.foodCount = foodCount;
	}
	public OrderFood getFood() {
		return food;
	}
	public void setFood(OrderFood food) {
		this.food = food;
	}

}
