package com.wireless.protocol;

import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class FoodMenu implements Parcelable{
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

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeParcelArray(this.foods, Food.FOOD_PARCELABLE_COMPLEX);
		dest.writeParcelArray(this.tastes, Taste.TASTE_PARCELABLE_COMPLEX);
		dest.writeParcelArray(this.styles, Taste.TASTE_PARCELABLE_COMPLEX);
		dest.writeParcelArray(this.specs, Taste.TASTE_PARCELABLE_COMPLEX);
		dest.writeParcelArray(this.kitchens, Kitchen.KITCHEN_PARCELABLE_COMPLEX);
		dest.writeParcelArray(this.depts, Department.DEPT_PARCELABLE_COMPLEX);
		dest.writeParcelArray(this.discounts, Discount.DISCOUNT_PARCELABLE_COMPLEX);
		dest.writeParcelArray(this.reasons, CancelReason.CR_PARCELABLE_COMPLEX);
	}

	public void createFromParcel(Parcel source) {
		Parcelable[] parcelables;
		
		parcelables = source.readParcelArray(Food.FOOD_CREATOR);
		if(parcelables != null){
			this.foods = new Food[parcelables.length];
			for(int i = 0; i < foods.length; i++){
				foods[i] = (Food)parcelables[i];
			}
		}
		
		parcelables = source.readParcelArray(Taste.TASTE_CREATOR);
		if(parcelables != null){
			this.tastes = new Taste[parcelables.length];
			for(int i = 0; i < tastes.length; i++){
				tastes[i] = (Taste)parcelables[i];
			}
		}
		
		parcelables = source.readParcelArray(Taste.TASTE_CREATOR);
		if(parcelables != null){
			this.styles = new Taste[parcelables.length];
			for(int i = 0; i < styles.length; i++){
				styles[i] = (Taste)parcelables[i];
			}
		}
		
		parcelables = source.readParcelArray(Taste.TASTE_CREATOR);
		if(parcelables != null){
			this.specs = new Taste[parcelables.length];
			for(int i = 0; i < specs.length; i++){
				specs[i] = (Taste)parcelables[i];
			}
		}
		
		kitchens = source.readParcelArray(Kitchen.KITCHEN_CREATOR);
		
		depts = source.readParcelArray(Department.DEPT_CREATOR);
		
		discounts = source.readParcelArray(Discount.DISCOUNT_CREATOR);
		
		reasons = source.readParcelArray(CancelReason.CR_CREATOR);
	}

}
