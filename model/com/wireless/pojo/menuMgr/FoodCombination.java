package com.wireless.pojo.menuMgr;

import com.wireless.pojo.dishesOrder.Food;

public class FoodCombination extends Food{
	
	private long parentFoodID;			// 套菜所属菜品编号
	private String parentFoodName;		// 套菜所属菜品名称
	private int amount;					// 菜品份数
	
	public long getParentFoodID() {
		return parentFoodID;
	}
	public void setParentFoodID(long parentFoodID) {
		this.parentFoodID = parentFoodID;
	}
	public String getParentFoodName() {
		return parentFoodName;
	}
	public void setParentFoodName(String parentFoodName) {
		this.parentFoodName = parentFoodName;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	
	
}
