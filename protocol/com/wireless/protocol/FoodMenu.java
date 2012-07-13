package com.wireless.protocol;

public class FoodMenu {
	public Food[] foods = null;			 	//菜品
	public Taste[] tastes = null;		 	//口味
	public Taste[] styles = null;			//做法
	public Taste[] specs = null;			//规格
	public Kitchen[] kitchens = null;		//厨房
	public Department[] depts = null;		//部门
	
	public FoodMenu(){}
	
	public FoodMenu(Food[] foods, Taste[] tastes, Taste[] styles, Taste[] specs, Kitchen[] kitchens, Department[] depts){
		this.foods = foods;
		this.tastes = tastes;
		this.styles = styles;
		this.specs = specs;
		this.kitchens = kitchens;
		this.depts = depts;
	}
}
