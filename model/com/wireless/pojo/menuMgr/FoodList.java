package com.wireless.pojo.menuMgr;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.SortedList;

public class FoodList extends AbstractList<Food> implements Jsonable{
	
	private final SortedList<Food> mFoods;
	private final SortedList<Food> mFoodsByAlias;
	
	FoodList(){
		mFoods = SortedList.newInstance();
		mFoodsByAlias = SortedList.newInstance(Food.BY_ALIAS);
	}
	
	public FoodList(List<Food> listElem){
		mFoods = SortedList.newInstance(listElem);
		mFoodsByAlias = SortedList.newInstance(listElem, Food.BY_ALIAS);
	}
	
	/**
	 * Construct a new instance using list and sorted elements by a given comparator.
	 * @param listElem
	 * @param comparator
	 */
    public FoodList(List<Food> listElem, Comparator<? super Food> comparator) {
    	mFoods = SortedList.newInstance(listElem, comparator);
    	mFoodsByAlias = SortedList.newInstance(listElem, Food.BY_ALIAS);
    }
    
    /**
     * Return the view of department tree {@link DepartmentTree} to this food list.
     * @return the view of department tree {@link DepartmentTree} to this food list
     */
    public DepartmentTree asDeptTree(){
		return new DepartmentTree.Builder(this).build();
    }
    
    /**
     * Find the specified element according to a key element.
     * Using binary search if the comparator is defined, otherwise check each element in turn for equality with the specified element.
     * @param key the key to find the specified element
     * @return the element corresponding to the key or <code>null<code> if NOT found 
     */
    public Food find(Food key){
    	return mFoods.get(mFoods.indexOfElement(key));
    }
    
    public List<Food> filter(String filterCond){
    	if(filterCond == null){
    		return mFoodsByAlias;
    		
    	}else if(filterCond.length() != 0){
			List<Food> tmpFoods = new ArrayList<Food>(this.mFoods);
			Iterator<Food> iter = tmpFoods.iterator();
			while(iter.hasNext()){
				Food f = iter.next();
				String cond = filterCond.toLowerCase(Locale.getDefault())
										.replace("，", "").replace(",", "")
										.replace("。", "").replace(".", "")
										.replace(" ", "");
				if(!(f.getName().toLowerCase(Locale.getDefault()).contains(cond) || 
				     f.getPinyin().contains(cond) || 
				     f.getPinyinShortcut().contains(cond) ||
				     String.valueOf(f.getAliasId()).startsWith(cond))){
					
					iter.remove();
				}				
			}	
			
			//Sort the food by sales
			Collections.sort(tmpFoods, Food.BY_SALES);
			return tmpFoods;
			
		}else{
			//return SortedList.newInstance(mFoods, Food.BY_ALIAS);
			return mFoodsByAlias;
		}
		
    }
    
    /**
     * Check to see whether the food is contained.
     * @param obj the food to check
     * @return true if the food is contained, otherwise false
     * @throws ClassCastException
     * 			throws if the food is NOT the correct instance
     */
    @Override
    public boolean contains(Object obj){
    	return mFoods.containsElement((Food)obj);
    }
    
	@Override
	public Food get(int location) {
		return mFoods.get(location);
	}
	
	@Override
	public int size() {
		return mFoods.size();
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonableList("foodList", this.mFoods, Food.FOOD_JSONABLE_SIMPLE);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}