package com.wireless.pojo.menuMgr;

import java.util.Collections;
import java.util.List;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteCategory;
import com.wireless.pojo.util.SortedList;

public class FoodMenu implements Parcelable{
	public FoodList foods;			 		//菜品
	public List<TasteCategory> categorys;	//口味类型
	public List<Taste> tastes;			 	//口味
	public List<Kitchen> kitchens;			//厨房
	public List<Department> depts;			//部门
	public List<Discount> discounts;		//折扣方案
	public List<CancelReason> reasons;		//退菜原因
	
	private FoodMenu(){}
	
	public FoodMenu(List<Food> foods, List<TasteCategory> categorys, List<Taste> tastes, List<Kitchen> kitchens, List<Department> depts, List<Discount> discounts, List<CancelReason> reasons){
		this.foods = new FoodList(foods);
		this.categorys = categorys;
		this.tastes = tastes;
		this.kitchens = kitchens;
		this.depts = depts;
		this.discounts = discounts;
		this.reasons = reasons;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeParcelList(this.foods, Food.FOOD_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.categorys, TasteCategory.TASTE_CATE_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.tastes, Taste.TASTE_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.kitchens, Kitchen.KITCHEN_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.depts, Department.DEPT_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.discounts, Discount.DISCOUNT_PARCELABLE_COMPLEX);
		dest.writeParcelList(this.reasons, CancelReason.CR_PARCELABLE_COMPLEX);
	}

	@Override
	public void createFromParcel(Parcel source) {
		foods = new FoodList(source.readParcelList(Food.CREATOR));
		categorys = source.readParcelList(TasteCategory.CREATOR);
		tastes = source.readParcelList(Taste.CREATOR);
		kitchens = source.readParcelList(Kitchen.CREATOR);
		depts = source.readParcelList(Department.DEPT_CREATOR);
		discounts = source.readParcelList(Discount.CREATOR);
		reasons = source.readParcelList(CancelReason.CREATOR);
		deal();
	}

	private void deal(){
		
		this.kitchens = Collections.unmodifiableList(SortedList.newInstance(kitchens));
		
		this.depts = Collections.unmodifiableList(SortedList.newInstance(depts));
		
		//Set the department detail to associated kitchen.
		for(Kitchen eachKitchen : kitchens){
			eachKitchen.setDept(depts.get(depts.indexOf(eachKitchen.getDept())));
		}
		
		this.categorys = Collections.unmodifiableList(categorys);
		
		//Set the category to each taste.
		for(Taste t : tastes){
			t.setCategory(categorys.get(categorys.indexOf(t.getCategory())));
		}
		this.tastes = Collections.unmodifiableList(SortedList.newInstance(tastes));
		
		for(Food eachFood : foods){
			
			//Get the detail to each child food in case of combo.
			if(eachFood.isCombo()){
				for(ComboFood eachChildFood : eachFood.getChildFoods()){
					eachChildFood.asFood().copyFrom(foods.find(eachChildFood.asFood()));
				}
			}
			
			//Get the detail to each popular taste 
			if(eachFood.hasPopTastes()){
				for(Taste popTaste : eachFood.getPopTastes()){
					popTaste.copyFrom(tastes.get(tastes.indexOf(popTaste)));
				}
			}

			//Get the detail to associated kitchen
			eachFood.setKitchen(kitchens.get(kitchens.indexOf(eachFood.getKitchen())));
			
		}
		
		this.discounts = Collections.unmodifiableList(this.discounts);
		this.reasons = Collections.unmodifiableList(this.reasons);
	}
	
	public final static Parcelable.Creator<FoodMenu> CREATOR = new Parcelable.Creator<FoodMenu>(){

		@Override
		public FoodMenu newInstance() {
			return new FoodMenu();
		}
		
		@Override
		public FoodMenu[] newInstance(int size){
			return new FoodMenu[size];
		}
		
	};
	
}
