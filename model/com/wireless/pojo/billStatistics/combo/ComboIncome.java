package com.wireless.pojo.billStatistics.combo;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class ComboIncome implements Jsonable{
	
	private String comboFoodName;
	private String subFoodName;
	private float amount;
	private float totalPrice;
	public String getComboFoodName() {
		return comboFoodName;
	}
	public void setComboFoodName(String comboFoodName) {
		this.comboFoodName = comboFoodName;
	}
	public String getSubFoodName() {
		return subFoodName;
	}
	public void setSubFoodName(String subFoodName) {
		this.subFoodName = subFoodName;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	public float getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(float totalPrice) {
		this.totalPrice = totalPrice;
	}
    
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("comboFoodName" , this.comboFoodName);
		jm.putString("subFoodName", this.subFoodName);
		jm.putFloat("amount", this.amount);
		jm.putFloat("totalPrice", this.totalPrice);
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		// TODO Auto-generated method stub
		
	}
}
