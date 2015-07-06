package com.wireless.pojo.inventoryMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Food;

public class SaleOfMaterial implements Jsonable{

	private Food food;
	private int amount;
	private float rate;
	private float consume;
	public Food getFood() {
		return food;
	}
	public void setFood(Food food) {
		this.food = food;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public float getRate() {
		return rate;
	}
	public void setRate(float rate) {
		this.rate = rate;
	}
	public float getConsume() {
		return consume;
	}
	public void setConsume(float consume) {
		this.consume = consume;
	}
	
	@Override
	public String toString(){
		return food.getName();
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof SaleOfMaterial)) {
			return false;
		} else {
			return food.getName().equals(((SaleOfMaterial)obj).getFood().getName());
					
		}
	}	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + this.food.getName().hashCode();
		return result;
	}
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("foodName", this.food.getName());
		jm.putInt("amount", this.amount);
		jm.putFloat("rate", this.rate);
		jm.putFloat("consume", this.consume);
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
	}
	
}
