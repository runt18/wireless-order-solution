package com.wireless.pojo.menuMgr;

import com.wireless.protocol.Food;

public class FoodMaterial extends Food{
	
	private int cateID;				// 食材种类编号
	private String cateName;		// 食材种类名称
	private int restaurantID;		// 餐厅编号
	private float consumption;		// 食材消耗数量
	private int materialID;			// 食材数据编号
	private int materialAliasID;	// 食材自定义编号
	private String materialName;	// 食材名称
	private float price;			// 食材价格
	
	
	public int getCateID() {
		return cateID;
	}
	public void setCateID(int cateID) {
		this.cateID = cateID;
	}
	public String getCateName() {
		return cateName;
	}
	public void setCateName(String cateName) {
		this.cateName = cateName;
	}
	public int getRestaurantId() {
		return restaurantID;
	}
	public void setRestaurantId(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public float getConsumption() {
		return consumption;
	}
	public void setConsumption(float consumption) {
		this.consumption = consumption;
	}
	public int getMaterialID() {
		return materialID;
	}
	public void setMaterialID(int materialID) {
		this.materialID = materialID;
	}
	public int getMaterialAliasID() {
		return materialAliasID;
	}
	public void setMaterialAliasID(int materialAliasID) {
		this.materialAliasID = materialAliasID;
	}
	public String getMaterialName() {
		return materialName;
	}
	public void setMaterialName(String materialName) {
		this.materialName = materialName;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
	
}
