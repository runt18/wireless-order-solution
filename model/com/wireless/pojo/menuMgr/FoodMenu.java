package com.wireless.pojo.menuMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.util.SortedList;

public class FoodMenu implements Parcelable{
	public FoodList foods;			 		//菜品
	public List<Taste> tastes;			 	//口味
	public List<Taste> styles;				//做法
	public List<Taste> specs;				//规格
	public List<Kitchen> kitchens;			//厨房
	public List<Department> depts;			//部门
	public List<Discount> discounts;		//折扣方案
	public List<CancelReason> reasons;		//退菜原因
	
	private FoodMenu(){}
	
	public FoodMenu(List<Food> foods, List<Taste> tastes, List<Taste> styles, List<Taste> specs, List<Kitchen> kitchens, List<Department> depts, List<Discount> discounts, List<CancelReason> reasons){
		this.foods = new FoodList(foods);
		this.tastes = tastes;
		this.styles = styles;
		this.specs = specs;
		this.kitchens = kitchens;
		this.depts = depts;
		this.discounts = discounts;
		this.reasons = reasons;
	}

	@Override
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

	@Override
	public void createFromParcel(Parcel source) {
		foods = new FoodList(source.readParcelList(Food.CREATOR));
		tastes = source.readParcelList(Taste.TASTE_CREATOR);
		styles = source.readParcelList(Taste.TASTE_CREATOR);
		specs = source.readParcelList(Taste.TASTE_CREATOR);
		kitchens = source.readParcelList(Kitchen.KITCHEN_CREATOR);
		depts = source.readParcelList(Department.DEPT_CREATOR);
		discounts = source.readParcelList(Discount.DISCOUNT_CREATOR);
		reasons = source.readParcelList(CancelReason.CR_CREATOR);
		deal();
	}

	private void deal(){
		
		//Sort the foods by kitchen.
		SortedList<Food> foodsByKitchen = SortedList.newInstance(foods, Food.BY_KITCHEN);
		//Remove the kitchen which does NOT contain any foods using binary search.
		List<Kitchen> tmpKitchen = new ArrayList<Kitchen>(kitchens);
		Iterator<Kitchen> iterKitchen = tmpKitchen.iterator();
		while(iterKitchen.hasNext()){
			Food key = new Food();
			key.setKitchen(iterKitchen.next());
			if(!foodsByKitchen.containsElement(key)){
				iterKitchen.remove();
			}
		}
		this.kitchens = Collections.unmodifiableList(SortedList.newInstance(tmpKitchen));
		
		//Remove the department which does NOT contain any foods.
		List<Department> tmpDept = new ArrayList<Department>();
		for(Department d : depts){
			for(Kitchen k : kitchens){
				if(k.getDept().equals(d)){
					tmpDept.add(d);
					break;
				}
			}
		}
		this.depts = Collections.unmodifiableList(SortedList.newInstance(tmpDept));
		
		//Set the department detail to associated kitchen.
		for(Kitchen eachKitchen : kitchens){
			eachKitchen.setDept(depts.get(depts.indexOf(eachKitchen.getDept())));
		}
		
		this.tastes = Collections.unmodifiableList(SortedList.newInstance(tastes));
		this.styles = Collections.unmodifiableList(SortedList.newInstance(styles));
		this.specs = Collections.unmodifiableList(SortedList.newInstance(specs));
		
		for(Food eachFood : foods){
			
			//Get the detail to each child food in case of combo.
			if(eachFood.isCombo()){
				for(Food eachChildFood : eachFood.getChildFoods()){
					eachChildFood.copyFrom(foods.find(eachChildFood));
				}
			}
			
			//Get the detail to each popular taste 
			if(eachFood.hasPopTastes()){
				for(Taste popTaste : eachFood.getPopTastes()){
					popTaste.copyFrom(tastes.get(tastes.indexOf(popTaste)));
					popTaste.copyFrom(styles.get(styles.indexOf(popTaste)));
					popTaste.copyFrom(specs.get(specs.indexOf(popTaste)));
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
