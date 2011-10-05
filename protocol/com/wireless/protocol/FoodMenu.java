package com.wireless.protocol;

public class FoodMenu {
	public Food[] foods = null;			//��Ʒ��Ϣ
	public Taste[] tastes = null;		//��ζ��Ϣ
	public Taste[] styles = null;		//������Ϣ
	public Taste[] specs = null;		//�����Ϣ
	public Kitchen[] kitchens = null;	//������Ϣ
	public SKitchen[] sKitchens = null;//�������Ϣ
	
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
