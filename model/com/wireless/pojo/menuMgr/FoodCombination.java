package com.wireless.pojo.menuMgr;

public class FoodCombination extends FoodBasic{
	
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
