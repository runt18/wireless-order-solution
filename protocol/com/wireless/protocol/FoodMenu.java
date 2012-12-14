package com.wireless.protocol;

public class FoodMenu {
	public Food[] foods;			 	//菜品
	public Taste[] tastes;			 	//口味
	public Taste[] styles;				//做法
	public Taste[] specs;				//规格
	public Kitchen[] kitchens;			//厨房
	public Department[] depts;			//部门
	public Discount[] discounts;		//折扣方案
	public CancelReason[] reasons;		//退菜原因
	
	public FoodMenu(){}
	
	public FoodMenu(Food[] foods, Taste[] tastes, Taste[] styles, Taste[] specs, Kitchen[] kitchens, Department[] depts, Discount[] discounts, CancelReason[] reasons){
		this.foods = foods;
		this.tastes = tastes;
		this.styles = styles;
		this.specs = specs;
		this.kitchens = kitchens;
		this.depts = depts;
		this.discounts = discounts;
		this.reasons = reasons;
	}
}
