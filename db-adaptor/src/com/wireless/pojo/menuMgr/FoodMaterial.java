package com.wireless.pojo.menuMgr;

public class FoodMaterial {
	
	private long foodId;
	private long cateId;
	private long restaurantId;
	private float consumption;
	private long materialId;
	private long materialAlias;
	private String materialName;
	public long getFoodId() {
		return foodId;
	}
	public void setFoodId(long foodId) {
		this.foodId = foodId;
	}
	public long getCateId() {
		return cateId;
	}
	public void setCateId(long cateId) {
		this.cateId = cateId;
	}
	public long getRestaurantId() {
		return restaurantId;
	}
	public void setRestaurantId(long restaurantId) {
		this.restaurantId = restaurantId;
	}
	public float getConsumption() {
		return consumption;
	}
	public void setConsumption(float consumption) {
		this.consumption = consumption;
	}
	public long getMaterialId() {
		return materialId;
	}
	public void setMaterialId(long materialId) {
		this.materialId = materialId;
	}
	public long getMaterialAlias() {
		return materialAlias;
	}
	public void setMaterialAlias(long materialAlias) {
		this.materialAlias = materialAlias;
	}
	public String getMaterialName() {
		return materialName;
	}
	public void setMaterialName(String materialName) {
		this.materialName = materialName;
	}
	
	
	
}
