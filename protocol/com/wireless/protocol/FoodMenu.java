package com.wireless.protocol;

public class FoodMenu {
	public Food[] foods = null;
	public Taste[] tastes = null;
	public Kitchen[] kitchens = null;
	
	public FoodMenu(){}
	
	public FoodMenu(Food[] foods, Taste[] tastes, Kitchen[] kitchens){
		this.foods = foods;
		this.tastes = tastes;
		this.kitchens = kitchens;
	}
}
