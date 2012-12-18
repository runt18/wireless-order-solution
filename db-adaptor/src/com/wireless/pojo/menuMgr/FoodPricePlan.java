package com.wireless.pojo.menuMgr;

public class FoodPricePlan {
	private int planID;
	private int foodID;
	private int restaurantID;
	private float unitPrice;
	
	public FoodPricePlan(){
		
	}
	public FoodPricePlan(int planID, int foodID, int restaurantID, float unitPrice){
		this.planID = planID;
		this.foodID = foodID;
		this.restaurantID = restaurantID;
		this.unitPrice = unitPrice;
	}
	
	public int getPlanID() {
		return planID;
	}
	public void setPlanID(int planID) {
		this.planID = planID;
	}
	public int getFoodID() {
		return foodID;
	}
	public void setFoodID(int foodID) {
		this.foodID = foodID;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public float getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}
	
}
