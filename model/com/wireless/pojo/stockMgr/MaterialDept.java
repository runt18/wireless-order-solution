package com.wireless.pojo.stockMgr;

import java.util.HashMap;
import java.util.Map;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Department;

public class MaterialDept implements Jsonable{

	private int restaurantId;
	private float stock;
	
	private Material material = new Material();
	private Department dept = new Department(0);
	private float cost;
	
	private Map<Department, Float> deptStock = new HashMap<Department, Float>(); 
	
	public MaterialDept(int materialId, int deptId, int restaurantId, float stock){
		this.material.setId(materialId);
		this.dept.setId((short) deptId);
		this.restaurantId = restaurantId;
		this.stock = stock;
	}
	
	public float getCost() {
		return cost;
	}
	
	public void setCost(float cost) {
		this.cost = cost;
	}
	
	public float totalCost(){
		float total = material.getPrice() * this.getStock();
		return (float)(Math.round(total * 100)) / 100;
	}
	
	public Material getMaterial() {
		return material;
	}
	
	public void setMaterial(Material material) {
		this.material = material;
	}
	
	public Department getDept() {
		return dept;
	}
	
	public void setDept(Department dept) {
		this.dept = dept;
	}
	
	public int getMaterialId() {
		return material.getId();
	}
	
	public void setMaterialId(int materialId) {
		this.material.setId(materialId);
	}
	
	public int getDeptId() {
		return dept.getId();
	}
	
	public void setDeptId(int deptId) {
		this.dept.setId((short) deptId);
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public float getStock() {
		return stock;
	}
	
	public void setStock(float stock) {
		this.stock = stock;
	}
	
	public void addStock(float count){
		this.stock = stock + count;
	}
	
	public void cutStock(float count){
		this.stock = stock - count;
	}
	
	public Map<Department, Float> getDeptStock() {
		return deptStock;
	}
	
	public void setDeptStock(Map<Department, Float> deptStock) {
		this.deptStock = deptStock;
	}
	
	public MaterialDept(){}
	
	@Override
	public String toString(){
		return "materialDept : materialId = " + material.getId() +
				" ,deptId = " + dept.getId() +
				" ,restaurantId" + restaurantId +
				" ,stock" + stock;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof MaterialDept)) {
			return false;
		} else {
			return material.getId() == ((MaterialDept) obj).material.getId()
					&& dept.getId() == ((MaterialDept) obj).dept.getId();
		}
	}	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + material.getId();
		result = result * 31 + dept.getId();
		return result;
	}
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		
		jm.putFloat("price", this.material.getPrice());
		jm.putInt("restaurantId", this.getRestaurantId());
		jm.putFloat("stock", this.getStock());
		jm.putFloat("cost", this.totalCost());
		jm.putJsonable("dept", this.dept, Department.DEPT_JSONABLE_COMPLEX);
		if(this.material != null){
			jm.putJsonable("material", this.material, 0);
			jm.putInt("materialId", this.material.getId());
			jm.putString("materialName", this.material.getName());
			jm.putString("materialPinyin", this.material.getPinyin());
		}
		jm.putInt("alarmAmoaunt", this.material.getAlarmAmount());
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	
	
}
