package com.wireless.protocol;

public class FoodMenu {
	public Food[] foods = null;
	public Taste[] tastes = null;
	public Taste[] styles = null;
	public Taste[] specs = null;
	public Kitchen[] kitchens = null;
	
	public FoodMenu(){}
	
	public FoodMenu(Food[] foods, Taste[] tastes, Taste[] styles, Taste[] specs, Kitchen[] kitchens){
		this.foods = foods;
		this.tastes = tastes;
		this.styles = styles;
		this.specs = specs;
		this.kitchens = kitchens;
	}
}
