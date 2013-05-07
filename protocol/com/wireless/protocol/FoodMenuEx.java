package com.wireless.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.protocol.comp.DeptComp;
import com.wireless.protocol.comp.FoodComp;
import com.wireless.protocol.comp.KitchenComp;
import com.wireless.protocol.comp.TasteComp;
import com.wireless.util.PinyinUtil;
import com.wireless.util.UnmodifiableList;

public class FoodMenuEx {
	
	public final FoodList foods;			 				//菜品
	public final UnmodifiableList<Taste> tastes;		 	//口味
	public final UnmodifiableList<Taste> styles;			//做法
	public final UnmodifiableList<Taste> specs;				//规格
	public final UnmodifiableList<Kitchen> kitchens;		//厨房
	public final UnmodifiableList<Department> depts;		//部门
	public final UnmodifiableList<Discount> discounts;		//折扣方案
	public final UnmodifiableList<CancelReason> reasons;	//退菜原因
	
	public FoodMenuEx(FoodMenu foodMenu){
		
		this.foods = new FoodList(foodMenu.foods, FoodComp.DEFAULT);
		
		//Sort the foods by kitchen.
		UnmodifiableList<Food> foodsByKitchen = new UnmodifiableList<Food>(foodMenu.foods, FoodComp.BY_KITCHEN);
		//Remove the kitchen which does NOT contain any foods using binary search.
		List<Kitchen> tmpKitchen = new ArrayList<Kitchen>(Arrays.asList(foodMenu.kitchens));
		Iterator<Kitchen> iterKitchen = tmpKitchen.iterator();
		while(iterKitchen.hasNext()){
			Food key = new Food();
			key.setKitchen(iterKitchen.next());
			if(!foodsByKitchen.containsElement(key)){
				iterKitchen.remove();
			}
		}
		this.kitchens = new UnmodifiableList<Kitchen>(tmpKitchen, KitchenComp.DEFAULT);
		
		//Remove the department which does NOT contain any foods.
		List<Department> tmpDept = new ArrayList<Department>();
		for(Department d : foodMenu.depts){
			for(Kitchen k : kitchens){
				if(k.getDept().equals(d)){
					tmpDept.add(d);
					break;
				}
			}
		}
		this.depts = new UnmodifiableList<Department>(tmpDept, DeptComp.DEFAULT);
		
		//Set the department detail to associated kitchen.
		for(Kitchen eachKitchen : kitchens){
			eachKitchen.setDept(depts.find(eachKitchen.getDept()));
		}
		
		this.tastes = new UnmodifiableList<Taste>(foodMenu.tastes, TasteComp.DEFAULT);
		this.styles = new UnmodifiableList<Taste>(foodMenu.styles, TasteComp.DEFAULT);
		this.specs = new UnmodifiableList<Taste>(foodMenu.specs, TasteComp.DEFAULT);
		
		for(Food eachFood : foods){
			//Generate the pinyin to each food
			eachFood.setPinyin(PinyinUtil.cn2Spell(eachFood.getName()));
			eachFood.setPinyinShortcut(PinyinUtil.cn2FirstSpell(eachFood.getName()));
			
			//Get the detail to each child food in case of combo.
			if(eachFood.isCombo()){
				for(Food eachChildFood : eachFood.getChildFoods()){
					eachChildFood.copyFrom(foods.find(eachChildFood));
				}
			}
			
			//Get the detail to each popular taste 
			if(eachFood.hasPopTastes()){
				for(Taste popTaste : eachFood.getPopTastes()){
					popTaste.copyFrom(tastes.find(popTaste));
					popTaste.copyFrom(styles.find(popTaste));
					popTaste.copyFrom(specs.find(popTaste));
				}
			}

			//Get the detail to associated kitchen
			eachFood.setKitchen(kitchens.find(eachFood.getKitchen()));
			
		}
		
		this.discounts = new UnmodifiableList<Discount>(foodMenu.discounts);
		this.reasons = new UnmodifiableList<CancelReason>(foodMenu.reasons);
	}
	
}
