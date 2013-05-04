package com.wireless.protocol;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.util.UnmodifiableList;

public class FoodList extends UnmodifiableList<Food>{
	
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
    private Map<Kitchen, FoodList> groupByKitchen(Comparator<Food> foodCompToKitchen) {
    	
    	Map<Kitchen, List<Food>> foodsByKitchen = new HashMap<Kitchen, List<Food>>();
    	
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
    	Map<Kitchen, FoodList> result = new HashMap<Kitchen, FoodList>();
    	for(Entry<Kitchen, List<Food>> entry : foodsByKitchen.entrySet()){
    		result.put(entry.getKey(), new FoodList(entry.getValue(), foodCompToKitchen));
    	}
    	
    	return result;
    }
    
    /**
     * Return a map containing the foods to each kitchen.
     * @return a map containing the foods to each kitchen
     */
    private Map<Kitchen, FoodList> groupByKitchen() {
    	return groupByKitchen(null);
    }
    
    /**
     * Return a map containing the foods to each department.
     * The foods to each department is sorted by comparator if defined.
     * @param foodCompToDept the comparator to each department.
     * @return a map containing the foods to each department
     */
    private Map<Department, FoodList> groupByDept(Comparator<Food> foodCompToDept){
    	
    	Map<Department, List<Food>> foodsByDept = new LinkedHashMap<Department, List<Food>>();
    	
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
    	Map<Department, FoodList> result = new HashMap<Department, FoodList>();
    	for(Entry<Department, List<Food>> entry : foodsByDept.entrySet()){
    		result.put(entry.getKey(), new FoodList(entry.getValue(), foodCompToDept));
    	}
    	
    	return result;
    }
    
    /**
     * Return a map containing the foods to each department.
     * @return a map containing the foods to each department
     */
    private Map<Department, FoodList> groupByDept(){
    	return groupByDept(null);
    }
    
    /**
     * Return the view of department tree {@link DepartmentTree} to this food list.
     * @return the view of department tree {@link DepartmentTree} to this food list
     */
    public DepartmentTree asDeptTree(){
		DepartmentTree.Builder treeBuilder = new DepartmentTree.Builder();
		for(Entry<Department, FoodList> entry : groupByDept().entrySet()){
			treeBuilder.addNode(entry.getKey(), entry.getValue().groupByKitchen());
		}
		return treeBuilder.build();
    }
}