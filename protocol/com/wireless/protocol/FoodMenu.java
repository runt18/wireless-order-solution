package com.wireless.protocol;

public class FoodMenu {
	public Food[] foods = null;			//菜品信息
	public Taste[] tastes = null;		//口味信息
	public Taste[] styles = null;		//做法信息
	public Taste[] specs = null;		//规格信息
	public Kitchen[] kitchens = null;	//厨房信息
	public SKitchen[] sKitchens = null;//大厨房信息
	
	public FoodMenu(){}
	
	public FoodMenu(Food[] foods, Taste[] tastes, Taste[] styles, Taste[] specs, Kitchen[] kitchens, SKitchen[] sKitchens){
		this.foods = foods;
		this.tastes = tastes;
		this.styles = styles;
		this.specs = specs;
		this.kitchens = kitchens;
		this.sKitchens = sKitchens;
	}
}
