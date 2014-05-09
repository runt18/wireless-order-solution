package com.wireless.pojo.menuMgr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wireless.json.Jsonable;
import com.wireless.pojo.util.SortedList;

public class DepartmentTree{

	public static class KitchenNode implements Entry<Kitchen, FoodList>, Jsonable{

		private final Kitchen key;
		private final FoodList value;
		
		private KitchenNode(Kitchen key, List<Food> value){
			this.key = key;
			if(value != null){
				this.value = new FoodList(value, new Comparator<Food>(){
					//每个厨房的菜品重新排序，"热销"菜品排在最前，其他的按编号排序
					@Override
					public int compare(Food f0, Food f1) {
						if(f0.isHot() && !f1.isHot()){
							return -1;
						}else if(!f0.isHot() && f1.isHot()){
							return 1;
						}else{
							return Food.BY_ALIAS.compare(f0, f1);
						}
					}
					
				});
			}else{
				this.value = null;
			}
		}
		
		@Override
		public Kitchen getKey() {
			return this.key;
		}

		@Override
		public FoodList getValue() {
			return this.value;
		}

		@Override
		public FoodList setValue(FoodList value) {
			throw new UnsupportedOperationException("The kitchen node is immutable");
		}

		@Override
		public Map<String, Object> toJsonMap(int flag) {
			Map<String, Object> jm = new HashMap<String, Object>();
			jm.put("kitchenNodeKey", getKey());
			jm.put("kitchenNodeValue", getValue());
			return Collections.unmodifiableMap(jm);
		}

		@Override
		public List<Object> toJsonList(int flag) {
			return null;
		}
		
	}
	
	public static class DeptNode implements Entry<Department, List<KitchenNode>>, Jsonable{

		private final Department key;
		private final List<KitchenNode> value;
		
		private DeptNode(Department key, List<KitchenNode> value){
			this.key = key;
			if(value != null){
				this.value =  SortedList.newInstance(value, new Comparator<KitchenNode>(){
					//厨房按displayId排序
					@Override
					public int compare(KitchenNode lhs, KitchenNode rhs) {
						if(lhs.getKey().getDisplayId() > rhs.getKey().getDisplayId()){
							return 1;
						}else if(lhs.getKey().getDisplayId() < rhs.getKey().getDisplayId()){
							return -1;
						}else{
							return 0;
						}
					}
					
				});
			}else{
				this.value = null;
			}
		}
		
		@Override
		public Department getKey() {
			return key;
		}

		@Override
		public List<KitchenNode> getValue() {
			return value;
		}

		@Override
		public List<KitchenNode> setValue(List<KitchenNode> value) {
			throw new UnsupportedOperationException("The dept node is immutable");
		}

		@Override
		public Map<String, Object> toJsonMap(int flag) {
			Map<String, Object> jm = new LinkedHashMap<String, Object>();
			jm.put("deptNodeKey", getKey());
			jm.put("deptNodeValue", getValue());
			return Collections.unmodifiableMap(jm);
		}

		@Override
		public List<Object> toJsonList(int flag) {
			return null;
		}
		
	}
	
	public static class Builder{
		
		private final List<DeptNode> mNodesToBuild = new ArrayList<DeptNode>();
		
		public Builder(List<Department> depts, List<Kitchen> kitchens){
			for(Department d : depts){
				if(kitchens != null){
					List<KitchenNode> kitchenNodes = new ArrayList<KitchenNode>();
					for(Kitchen k : kitchens){
						if(k.getDept().equals(d)){
							kitchenNodes.add(new KitchenNode(k, null));
						}
					}
					mNodesToBuild.add(new DeptNode(d, kitchenNodes));
				}else{
					mNodesToBuild.add(new DeptNode(d, null));
				}
			}
		}
		
		public Builder(List<Food> foodList){
			for(Entry<Department, List<Food>> entry : groupByDept(foodList).entrySet()){
				addNode(entry.getKey(), groupByKitchen(entry.getValue()));
			}
		}
		
	    /**
	     * Return a map containing the foods to each department.
	     * @return a map containing the foods to each department
	     */
	    private Map<Department, List<Food>> groupByDept(List<Food> foodList){
	    	Map<Department, List<Food>> foodsByDept = new HashMap<Department, List<Food>>();
	    	
	    	//Group the foods by department and put it to a map.
	    	for(Food f : foodList){
	    		List<Food> foodsToEachDept = foodsByDept.get(f.getKitchen().getDept());
	    		if(foodsToEachDept != null){
	    			foodsToEachDept.add(f);
	    		}else{
	    			foodsToEachDept = new ArrayList<Food>();
	    			foodsToEachDept.add(f);
	    			foodsByDept.put(f.getKitchen().getDept(), foodsToEachDept);
	    		}
	    	}
	    	
	    	return foodsByDept;
	    	
	    }
		
	    /**
	     * Return a map containing the foods to each kitchen.
	     * @return a map containing the foods to each kitchen
	     */
	    private Map<Kitchen, List<Food>> groupByKitchen(List<Food> foodList) {
	    	Map<Kitchen, List<Food>> foodsByKitchen = new HashMap<Kitchen, List<Food>>();
	    	
			//Group the foods by kitchen and put it to a map.
	    	for(Food f : foodList){
				List<Food> foodsToEachKitchen = foodsByKitchen.get(f.getKitchen());
				if(foodsToEachKitchen != null){
					foodsToEachKitchen.add(f);
				}else{
					foodsToEachKitchen = new ArrayList<Food>();
					foodsToEachKitchen.add(f);
					foodsByKitchen.put(f.getKitchen(), foodsToEachKitchen);
				}
			}
	    	
	    	return foodsByKitchen;
	    }
	    
		private Builder addNode(Department dept, Map<Kitchen, List<Food>> foodsByKitchen){

			List<KitchenNode> kitchenNodes = new ArrayList<KitchenNode>();

			for(Entry<Kitchen, List<Food>> entry : foodsByKitchen.entrySet()){
				kitchenNodes.add(new KitchenNode(entry.getKey(), entry.getValue()));
			}
			
			mNodesToBuild.add(new DeptNode(dept, kitchenNodes));
			return this;
		}
		
		public DepartmentTree build(){
			return new DepartmentTree(mNodesToBuild);
		}
	}

	private final List<DeptNode> mDeptNodes = SortedList.newInstance(new Comparator<DeptNode>(){
		//部门按displayId排序
		@Override
		public int compare(DeptNode lhs, DeptNode rhs) {
			if(lhs.getKey().getDisplayId() > rhs.getKey().getDisplayId()){
				return 1;
			}else if(lhs.getKey().getDisplayId() < rhs.getKey().getDisplayId()){
				return -1;
			}else{
				return 0;
			}
		}
	});

	private DepartmentTree(List<DeptNode> deptNodes){
		this.mDeptNodes.addAll(deptNodes);
	}
	
	public List<Food> asFoodList(){
		
		List<Food> foodList = new ArrayList<Food>();
		
		for(DeptNode deptNode : mDeptNodes){
			for(Entry<Kitchen, FoodList> kitchenNode : deptNode.getValue()){
				foodList.addAll(kitchenNode.getValue());
			}
		}
		return foodList;
	}
	
	public List<Kitchen> asKitchenList(){
		List<Kitchen> kitchenList = new ArrayList<Kitchen>();
		
		for(DeptNode deptNode : mDeptNodes){
			for(KitchenNode kitchenNode : deptNode.getValue()){
				kitchenList.add(kitchenNode.getKey());
			}
		}
		return kitchenList;
	}
	
	public List<Department> asDeptList(){

		List<Department> deptList = new ArrayList<Department>();
		
		for(DeptNode deptNode : mDeptNodes){
			deptList.add(deptNode.getKey());
		}
		
		return deptList;
	}
	
	/**
	 * Return the view represented as department nodes to the department tree.
	 * @return the view represented as department nodes to the department tree
	 */
	public List<DeptNode> asDeptNodes(){
		return asDeptNodes(null);
	}
	
	/**
	 * Return the view represented as department nodes to the department tree.
	 * The food to each kitchen would be sorted by comparator if defined.
	 * @param foodComp the food comparator of foods to each kitchen
	 * @return the view represented as department nodes to the department tree
	 */
	private List<DeptNode> asDeptNodes(Comparator<Food> foodComp){
		if(foodComp != null){
			List<DeptNode> deptNodes = new ArrayList<DeptNode>();
			for(DeptNode deptNode : mDeptNodes){
				
				List<KitchenNode> newKitchenNodes = new ArrayList<KitchenNode>();
				
				for(KitchenNode kitchenNode : deptNode.getValue()){
					newKitchenNodes.add(new KitchenNode(kitchenNode.getKey(), new FoodList(kitchenNode.value, foodComp)));
				}
				
				deptNodes.add(new DeptNode(deptNode.getKey(), newKitchenNodes));
			}
			
			return deptNodes;
			
		}else{
			return new ArrayList<DeptNode>(mDeptNodes);
		}
	}
	
	public List<KitchenNode> asKitchenNodes(){
		List<KitchenNode> kitchenNodes = new ArrayList<KitchenNode>();
		for(DeptNode deptNode : mDeptNodes){
			for(KitchenNode kitchenNode : deptNode.getValue()){
				kitchenNodes.add(kitchenNode);
			}
		}
		return kitchenNodes;
	}
}