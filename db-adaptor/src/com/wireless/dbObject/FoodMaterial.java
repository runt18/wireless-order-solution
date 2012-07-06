package com.wireless.dbObject;

import com.wireless.protocol.Food;

public class FoodMaterial {
	public Food food;
	public Material material;
	public float consumption;
	
	public FoodMaterial(){
		food = new Food();
		material = new Material();
		consumption = 0;
	}
	
	public FoodMaterial(Food food, Material material, float consume){
		this.food = food;
		this.material = material;
		this.consumption = consume;
	}
	
}
