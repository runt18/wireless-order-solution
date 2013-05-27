package com.wireless.pojo.inventoryMgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;

public class FoodMaterial implements Jsonable{
	private int restaurantId;
	private int foodId;
	private int materialId;
	private float consumption;
	private String foodName;
	private String materialName;
	private String materialCateName;
	
	/**
	 * init
	 * @param restaurantId
	 * @param foodId
	 * @param materialId
	 * @param consumption
	 * @param foodName
	 */
	void init(int restaurantId, int foodId, int materialId, float consumption, String foodName, String materialName, String materialCateName){
		this.restaurantId = restaurantId;
		this.foodId = foodId;
		this.materialId = materialId;
		this.consumption = consumption;
		this.foodName = foodName;
		this.materialName = materialName;
		this.materialCateName = materialCateName;
	}
	
	public FoodMaterial(){}
	/**
	 * delete model
	 * @param restaurantId
	 * @param foodId
	 * @param materialId
	 */
	public FoodMaterial(int restaurantId, int foodId, int materialId){
		init(restaurantId, foodId, materialId, 0, null, null, null);
	}
	/**
	 * update model
	 * @param restaurantId
	 * @param foodId
	 * @param materialId
	 * @param consumption
	 */
	public FoodMaterial(int restaurantId, int foodId, int materialId, float consumption){
		init(restaurantId, foodId, materialId, consumption, null, null, null);
	}
	/**
	 * insert model
	 * @param restaurantId
	 * @param foodId
	 * @param materialId
	 * @param consumption
	 * @param foodName
	 */
	public FoodMaterial(int restaurantId, int foodId, int materialId, float consumption, String foodName){
		init(restaurantId, foodId, materialId, consumption, foodName, null, null);
	}
	/**
	 * insert model
	 * @param restaurantId
	 * @param foodId
	 * @param materialId
	 * @param consumption
	 * @param foodName
	 * @param materialName
	 * @param materialCateName
	 */
	public FoodMaterial(int restaurantId, int foodId, int materialId, float consumption, String foodName, String materialName, String materialCateName){
		init(restaurantId, foodId, materialId, consumption, foodName, materialName, materialCateName);
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	public int getFoodId() {
		return foodId;
	}
	public void setFoodId(int foodId) {
		this.foodId = foodId;
	}
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	public float getConsumption() {
		return consumption;
	}
	public void setConsumption(float consumption) {
		this.consumption = consumption;
	}
	public String getFoodName() {
		return foodName;
	}
	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}
	public String getMaterialName() {
		return materialName;
	}
	public void setMaterialName(String materialName) {
		this.materialName = materialName;
	}
	public String getMaterialCateName() {
		return materialCateName;
	}
	public void setMaterialCateName(String materialCateName) {
		this.materialCateName = materialCateName;
	}
	@Override
	public String toString() {
		return "restaurantId=" + this.restaurantId + ", foodId=" + this.foodId + ", materialId=" + this.materialId + ", consumption=" + this.consumption;
	}
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("rid", this.restaurantId);
		jm.put("foodId", this.foodId);
		jm.put("foodName", this.foodName);
		jm.put("materialId", this.materialId);
		jm.put("consumption", this.consumption);
		jm.put("materialName", this.materialName);
		jm.put("materialCateName", this.materialCateName);
		
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	
}
