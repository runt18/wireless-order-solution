package com.wireless.protocol;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wireless.protocol.comp.DeptComp;
import com.wireless.protocol.comp.FoodComp;
import com.wireless.protocol.comp.KitchenComp;
import com.wireless.protocol.comp.TasteComp;
import com.wireless.util.PinyinUtil;

public class FoodMenuEx {
	
	/**
	 * This class is a List implementation which is unmodifiable.
	 * Optionally the element can be sorts by a comparator {@link Comparator} as list construction.
	 * Once the elements are sorted, 
	 * the method {@link #find(Object)} & {@link #containsElement(Object)}can be more faster since based on binary search.
	 * 
	 * @param <T>
	 */
	public static class UnmodifiableList<T> extends AbstractList<T> {
		
		private final List<T> mList;
		
		private final Comparator<? super T> mComparator;
		
		public UnmodifiableList(List<T> listElem){
			this.mComparator = null;
			this.mList = new ArrayList<T>(listElem);
		}
		
		public UnmodifiableList(T[] arrayElem){
			this.mComparator = null;
			this.mList = Arrays.asList(arrayElem);
		}
		
		/**
		 * Construct a new instance using list and sorted elements by a given comparator.
		 * @param listElem
		 * @param comparator
		 */
	    public UnmodifiableList(List<T> listElem, Comparator<? super T> comparator) {
	    	this.mComparator = comparator;
	    	this.mList = new ArrayList<T>(listElem);
	    	if(this.mComparator != null){
	    		Collections.sort(this.mList, this.mComparator);
	    	}
	    }
	    
		/**
		 * Construct a new instance using array and sorted elements by a given comparator.
		 * @param listElem
		 * @param comparator
		 */
	    public UnmodifiableList(T[] arrayElem, Comparator<? super T> comparator){
	    	this.mList = new ArrayList<T>(Arrays.asList(arrayElem));
	    	this.mComparator = comparator;
	    	if(this.mComparator != null){
	    		Collections.sort(this.mList, this.mComparator);
	    	}
	    }
	    
	    /**
	     * Check if this list contains the given element. 
	     * Using binary search if the comparator is defined, 
	     * otherwise check each element in turn for equality with the specified element.
	     * 
	     * @param paramT
	     * @return <code>true</code>, if the element is contained in this list;
	     * <code>false</code>, otherwise.
	     */
	    public boolean containsElement(T paramT) {
	    	if(mComparator != null){
	    		return (Collections.binarySearch(this, paramT, mComparator) > -1);
	    	}else{
	    		return contains(paramT);
	    	}
	    }
	    
	    /**
	     * Find the specified element according to a key element.
	     * Using binary search if the comparator is defined, otherwise check each element in turn for equality with the specified element.
	     * @param key the key to find the specified element
	     * @return the element corresponding to the key or <code>null<code> if NOT found 
	     */
	    public T find(T key){
	    	if(mComparator != null){
		    	int index = Collections.binarySearch(this, key, mComparator);
		    	if(index >= 0){
		    		return get(index);
		    	}else{
		    		return null;
		    	}
	    	}else{
	    		for(T elem : mList){
	    			if(elem.equals(key)){
	    				return elem;
	    			}
	    		}
	    		return null;
	    	}
	    }
	    
		@Override
		public T get(int location) {
			return mList.get(location);
		}
		
		@Override
		public int size() {
			return mList.size();
		}
	}
	
	public static class FoodList extends UnmodifiableList<Food>{
		
		public FoodList(List<Food> listElem){
			super(listElem);
		}
		
		public FoodList(Food[] arrayElem){
			super(arrayElem);
		}
		
		/**
		 * Construct a new instance using list and sorted elements by a given comparator.
		 * @param listElem
		 * @param comparator
		 */
	    public FoodList(List<Food> listElem, Comparator<? super Food> comparator) {
	    	super(listElem, comparator);
	    }
	    
		/**
		 * Construct a new instance using array and sorted elements by a given comparator.
		 * @param listElem
		 * @param comparator
		 */
	    public FoodList(Food[] arrayElem, Comparator<? super Food> comparator){
	    	super(arrayElem, comparator);
	    }
	    
	    /**
	     * Return a map containing the foods to each kitchen.
	     * The foods to each kitchen is sorted by comparator if defined.
	     * @param foodCompToKitchen the comparator to each kitchen.
	     * @return a map containing the foods to each kitchen
	     */
	    public Map<PKitchen, FoodList> groupByKitchen(Comparator<Food> foodCompToKitchen) {
	    	
	    	Map<PKitchen, List<Food>> foodsByKitchen = new HashMap<PKitchen, List<Food>>();
	    	
			//Group the foods by kitchen and put it to a map.
	    	Iterator<Food> iter = iterator();
	    	while(iter.hasNext()){
	    		Food f = iter.next();
				List<Food> foodsToEachKitchen = foodsByKitchen.get(f.getKitchen());
				if(foodsToEachKitchen != null){
					foodsToEachKitchen.add(f);
				}else{
					foodsToEachKitchen = new ArrayList<Food>();
					foodsToEachKitchen.add(f);
					foodsByKitchen.put(f.getKitchen(), foodsToEachKitchen);
				}
			}
	    	
	    	//Sort the foods to each kitchen by the specified comparator
	    	Map<PKitchen, FoodList> result = new HashMap<PKitchen, FoodList>();
	    	for(Entry<PKitchen, List<Food>> entry : foodsByKitchen.entrySet()){
	    		result.put(entry.getKey(), new FoodList(entry.getValue(), foodCompToKitchen));
	    	}
	    	
	    	return result;
	    }
	    
	    /**
	     * Return a map containing the foods to each kitchen.
	     * @return a map containing the foods to each kitchen
	     */
	    public Map<PKitchen, FoodList> groupByKitchen() {
	    	return groupByKitchen(null);
	    }
	    
	    /**
	     * Return a map containing the foods to each department.
	     * The foods to each department is sorted by comparator if defined.
	     * @param foodCompToDept the comparator to each department.
	     * @return a map containing the foods to each department
	     */
	    public Map<PDepartment, FoodList> groupByDept(Comparator<Food> foodCompToDept){
	    	
	    	Map<PDepartment, List<Food>> foodsByDept = new LinkedHashMap<PDepartment, List<Food>>();
	    	
	    	//Group the foods by department and put it to a map.
	    	Iterator<Food> iter = iterator();
	    	while(iter.hasNext()){
	    		Food f = iter.next();
	    		List<Food> foodsToEachDept = foodsByDept.get(f.getKitchen().getDept());
	    		if(foodsToEachDept != null){
	    			foodsToEachDept.add(f);
	    		}else{
	    			foodsToEachDept = new ArrayList<Food>();
	    			foodsToEachDept.add(f);
	    			foodsByDept.put(f.getKitchen().getDept(), foodsToEachDept);
	    		}
	    	}
	    	
	    	//Sort the foods to each department by the specified comparator.
	    	Map<PDepartment, FoodList> result = new HashMap<PDepartment, FoodList>();
	    	for(Entry<PDepartment, List<Food>> entry : foodsByDept.entrySet()){
	    		result.put(entry.getKey(), new FoodList(entry.getValue(), foodCompToDept));
	    	}
	    	
	    	return result;
	    }
	    
	    /**
	     * Return a map containing the foods to each department.
	     * @return a map containing the foods to each department
	     */
	    public Map<PDepartment, FoodList> groupByDept(){
	    	return groupByDept(null);
	    }
	}
	
	public final FoodList foods;			 				//菜品
	public final UnmodifiableList<Taste> tastes;		 	//口味
	public final UnmodifiableList<Taste> styles;			//做法
	public final UnmodifiableList<Taste> specs;				//规格
	public final UnmodifiableList<PKitchen> kitchens;		//厨房
	public final UnmodifiableList<PDepartment> depts;		//部门
	public final UnmodifiableList<PDiscount> discounts;		//折扣方案
	public final UnmodifiableList<CancelReason> reasons;	//退菜原因
	
	public FoodMenuEx(FoodMenu foodMenu){
		
		this.foods = new FoodList(foodMenu.foods, FoodComp.DEFAULT);
		
		//Sort the foods by kitchen.
		UnmodifiableList<Food> foodsByKitchen = new UnmodifiableList<Food>(foodMenu.foods, FoodComp.BY_KITCHEN);
		//Remove the kitchen which does NOT contain any foods using binary search.
		List<PKitchen> tmpKitchen = new ArrayList<PKitchen>(Arrays.asList(foodMenu.kitchens));
		Iterator<PKitchen> iterKitchen = tmpKitchen.iterator();
		while(iterKitchen.hasNext()){
			Food key = new Food();
			key.setKitchen(iterKitchen.next());
			if(!foodsByKitchen.containsElement(key)){
				iterKitchen.remove();
			}
		}
		this.kitchens = new UnmodifiableList<PKitchen>(tmpKitchen, KitchenComp.DEFAULT);
		
		//Remove the department which does NOT contain any foods.
		List<PDepartment> tmpDept = new ArrayList<PDepartment>();
		for(PDepartment d : foodMenu.depts){
			for(PKitchen k : kitchens){
				if(k.getDept().equals(d)){
					tmpDept.add(d);
					break;
				}
			}
		}
		this.depts = new UnmodifiableList<PDepartment>(tmpDept, DeptComp.DEFAULT);
		
		//Set the department detail to associated kitchen.
		for(PKitchen eachKitchen : kitchens){
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
		
		this.discounts = new UnmodifiableList<PDiscount>(foodMenu.discounts);
		this.reasons = new UnmodifiableList<CancelReason>(foodMenu.reasons);
	}
	
}
