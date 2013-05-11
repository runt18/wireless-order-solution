package com.wireless.pojo.inventoryMgr;

public class FoodMaterial {
	private int id;
	private int restaurantId;
	private int foodId;
	private int materialId;
	private float consumption;
	
	/**
	 * 
	 * @param id
	 * @param restaurantId
	 * @param foodId
	 * @param materialId
	 * @param consumption
	 */
	void init(int id, int restaurantId, int foodId, int materialId, float consumption){
		this.id = id;
		this.restaurantId = restaurantId;
		this.foodId = foodId;
		this.materialId = materialId;
		this.consumption = consumption;
	}
	public FoodMaterial(){}
	public FoodMaterial(int restaurantId, int foodId, int materialId, float consumption){
		init(0, restaurantId, foodId, materialId, consumption);
	}
	public FoodMaterial(int id, int restaurantId, int foodId, int materialId, float consumption){
		init(id, restaurantId, foodId, materialId, consumption);
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	@Override
	public String toString() {
		return "restaurantId=" + this.restaurantId + ", foodId=" + this.foodId + ", materialId=" + this.materialId + ", consumption=" + this.consumption;
	}
	
}
