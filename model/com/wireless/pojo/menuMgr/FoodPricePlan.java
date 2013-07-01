package com.wireless.pojo.menuMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.ppMgr.PricePlan;

public class FoodPricePlan implements Jsonable{
	private int planId;
	private int foodId;
	private int restaurantId;
	private float unitPrice;
	private PricePlan pricePlan;
	// 菜品部分基础信息
	private int foodAlias;
	private String foodName;
	private int kitchenId;
	private int kitchenAlias;
	private String kitchenName;
	
	public FoodPricePlan(){
		this.pricePlan = new PricePlan();
	}
	public FoodPricePlan(int planId, int foodId, int restaurantID, float unitPrice){
		this.planId = planId;
		this.foodId = foodId;
		this.restaurantId = restaurantID;
		this.unitPrice = unitPrice;
	}
	
	public int getPlanId() {
		return planId;
	}
	public void setPlanId(int planId) {
		this.planId = planId;
	}
	public int getFoodId() {
		return foodId;
	}
	public void setFoodId(int foodId) {
		this.foodId = foodId;
	}
	public int getRestaurantId() {
		return restaurantId;
	}
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
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
	public int getKitchenId() {
		return kitchenId;
	}
	public void setKitchenId(int kitchenId) {
		this.kitchenId = kitchenId;
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
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		HashMap<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("planId", planId);
		jm.put("foodId", foodId);
		jm.put("rid", restaurantId);
		jm.put("unitPrice", unitPrice);
		jm.put("pricePlan", pricePlan);
		
		jm.put("foodAlias", foodAlias);
		jm.put("foodName", foodName);
		jm.put("kitchenId", kitchenId);
		jm.put("kitchenAlias", kitchenAlias);
		jm.put("kitchenName", kitchenName);
		
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	@Override
	public void fromJsonMap(Map<String, Object> map) {
		
	}
	
}
