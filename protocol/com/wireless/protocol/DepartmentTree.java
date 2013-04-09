package com.wireless.protocol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;



public class DepartmentTree{

	public static class KitchenNode implements Entry<PKitchen, FoodList>{

		private final PKitchen key;
		private final FoodList value;
		
		private KitchenNode(PKitchen key, FoodList value){
			this.key = key;
			this.value = value;
		}
		
		@Override
		public PKitchen getKey() {
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
		
	}
	
	public static class DeptNode implements Entry<PDepartment, List<KitchenNode>>{

		private final PDepartment key;
		private final List<KitchenNode> value;
		
		private DeptNode(PDepartment key, List<KitchenNode> value){
			this.key = key;
			this.value = value;
		}
		
		@Override
		public PDepartment getKey() {
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
		
	}
	
	public static class Builder{
		
		private final List<DeptNode> mNodesToBuild = new ArrayList<DeptNode>();
		
		public Builder addNode(PDepartment dept, Map<PKitchen, FoodList> foodsByKitchen){

			final List<KitchenNode> kitchenNodes = new ArrayList<KitchenNode>();

			for(Entry<PKitchen, FoodList> entry : foodsByKitchen.entrySet()){
				
				//每个厨房的菜品重新排序，"热销"菜品排在最前，其他的按编号排序
				FoodList sorted = new FoodList(entry.getValue(), new Comparator<Food>(){

					@Override
					public int compare(Food lhs, Food rhs) {
						if(lhs.isHot() && !rhs.isHot()){
							return -1;
						}else if(!lhs.isHot() && rhs.isHot()){
							return 1;
						}else{
							if(lhs.getAliasId() > rhs.getAliasId()){
								return 1;
							}else if(lhs.getAliasId() < rhs.getAliasId()){
								return -1;
							}else{
								return 0;
							}
						}
					}
					
				});
				
				kitchenNodes.add(new KitchenNode(entry.getKey(), sorted));
			}
			
			//厨房按编号排序
			Collections.sort(kitchenNodes, new Comparator<KitchenNode>(){

				@Override
				public int compare(KitchenNode lhs, KitchenNode rhs) {
					if(lhs.getKey().getAliasId() > rhs.getKey().getAliasId()){
						return 1;
					}else if(lhs.getKey().getAliasId() < rhs.getKey().getAliasId()){
						return -1;
					}else{
						return 0;
					}
				}
				
			});
			
			mNodesToBuild.add(new DeptNode(dept, kitchenNodes));
			return this;
		}
		
		public DepartmentTree build(){
			//部门按编号排序
			Collections.sort(mNodesToBuild, new Comparator<DeptNode>(){

				@Override
				public int compare(DeptNode lhs, DeptNode rhs) {
					if(lhs.getKey().getId() > rhs.getKey().getId()){
						return 1;
					}else if(lhs.getKey().getId() < rhs.getKey().getId()){
						return -1;
					}else{
						return 0;
					}
				}
				
			});
			return new DepartmentTree(mNodesToBuild);
		}
	}

	private List<DeptNode> mDeptNodes;

	private DepartmentTree(List<DeptNode> deptNodes){
		this.mDeptNodes = deptNodes;
	}
	
	public List<Food> asFoodList(){
		
		List<Food> foodList = new ArrayList<Food>();
		
		for(DeptNode deptNode : mDeptNodes){
			for(Entry<PKitchen, FoodList> kitchenNode : deptNode.getValue()){
				foodList.addAll(kitchenNode.getValue());
			}
		}
		return foodList;
	}
	
	public List<PKitchen> asKitchenList(){
		List<PKitchen> kitchenList = new ArrayList<PKitchen>();
		
		for(DeptNode deptNode : mDeptNodes){
			for(KitchenNode kitchenNode : deptNode.getValue()){
				kitchenList.add(kitchenNode.getKey());
			}
		}
		return kitchenList;
	}
	
	public List<PDepartment> asDeptList(){

		List<PDepartment> deptList = new ArrayList<PDepartment>();
		
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
	public List<DeptNode> asDeptNodes(Comparator<Food> foodComp){
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