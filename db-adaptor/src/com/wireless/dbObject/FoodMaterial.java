package com.wireless.dbObject;

import com.wireless.protocol.OrderFood;

public class FoodMaterial {
	public OrderFood food;
	public Material material;
	public float consumption;
	
	public FoodMaterial(OrderFood _food, Material _material, float _consume){
		food = _food;
		material = _material;
		consumption = _consume;
	}
	
}
