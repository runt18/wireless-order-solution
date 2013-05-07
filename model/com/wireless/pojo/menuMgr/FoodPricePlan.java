package com.wireless.pojo.menuMgr;

import com.wireless.pojo.ppMgr.PricePlan;

public class FoodPricePlan {
	private int planID;
	private int foodID;
	private int restaurantID;
	private float unitPrice;
	private PricePlan pricePlan;
	// 菜品部分基础信息
	private int foodAlias;
	private String foodName;
	private int kitchenID;
	private int kitchenAlias;
	private String kitchenName;
	
	public FoodPricePlan(){
		this.pricePlan = new PricePlan();
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
	public PricePlan getPricePlan() {
		return pricePlan;
	}
	public void setPricePlan(PricePlan pricePlan) {
		this.pricePlan = pricePlan;
	}
	public int getFoodAlias() {
		return foodAlias;
	}
	public void setFoodAlias(int foodAlias) {
		this.foodAlias = foodAlias;
	}
	public String getFoodName() {
		return foodName;
	}
	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}
	public int getKitchenID() {
		return kitchenID;
	}
	public void setKitchenID(int kitchenID) {
		this.kitchenID = kitchenID;
	}
	public int getKitchenAlias() {
		return kitchenAlias;
	}
	public void setKitchenAlias(int kitchenAlias) {
		this.kitchenAlias = kitchenAlias;
	}
	public String getKitchenName() {
		return kitchenName;
	}
	public void setKitchenName(String kitchenName) {
		this.kitchenName = kitchenName;
	}
	
}
