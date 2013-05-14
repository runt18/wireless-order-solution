package com.wireless.protocol;

import java.util.List;

import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class FoodMenu implements Parcelable{
	public List<Food> foods;			 	//菜品
	public List<Taste> tastes;			 	//口味
	public List<Taste> styles;				//做法
	public List<Taste> specs;				//规格
	public List<Kitchen> kitchens;			//厨房
	public List<Department> depts;			//部门
	public List<Discount> discounts;		//折扣方案
	public List<CancelReason> reasons;		//退菜原因
	
	public FoodMenu(){}
	
	public FoodMenu(List<Food> foods, List<Taste> tastes, List<Taste> styles, List<Taste> specs, List<Kitchen> kitchens, List<Department> depts, List<Discount> discounts, List<CancelReason> reasons){
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
		dest.writeParcelList(this.foods, Food.FOOD_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.tastes, Taste.TASTE_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.styles, Taste.TASTE_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.specs, Taste.TASTE_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.kitchens, Kitchen.KITCHEN_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.depts, Department.DEPT_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.discounts, Discount.DISCOUNT_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.reasons, CancelReason.CR_PARCELABLE_COMPLEX);
	}

	public void createFromParcel(Parcel source) {
		foods = source.readParcelList(Food.FOOD_CREATOR);
		
		tastes = source.readParcelList(Taste.TASTE_CREATOR);
		
		styles = source.readParcelList(Taste.TASTE_CREATOR);
		
		specs = source.readParcelList(Taste.TASTE_CREATOR);
		
		kitchens = source.readParcelList(Kitchen.KITCHEN_CREATOR);
		
		depts = source.readParcelList(Department.DEPT_CREATOR);
		
		discounts = source.readParcelList(Discount.DISCOUNT_CREATOR);
		
		reasons = source.readParcelList(CancelReason.CR_CREATOR);
	}

}
