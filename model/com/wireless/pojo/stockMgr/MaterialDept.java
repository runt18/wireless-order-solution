package com.wireless.pojo.stockMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Department;

public class MaterialDept implements Jsonable{

	private int restaurantId;
	private float stock;
	
	private Material material = new Material();
	private Department dept = new Department();
	private float cost;
	
	
	
	
	public float getCost() {
		return cost;
	}
	public void setCost(float cost) {
		this.cost = cost;
	}
	public float totalCost(){
		return material.getPrice() * this.getStock();
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
	
	public void plusStock(float count){
		this.stock = stock + count;
	}
	public void cutStock(float count){
		this.stock = stock - count;
	}
	
	public MaterialDept(){}
	
	public MaterialDept(int materialId, int deptId, int restaurantId, float stock){
		this.material.setId(materialId);
		this.dept.setId((short) deptId);
		this.restaurantId = restaurantId;
		this.stock = stock;
	}
	
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
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		
		jm.put("price", this.material.getPrice());
		jm.put("restaurantId", this.getRestaurantId());
		jm.put("stock", this.getStock());
		jm.put("cost", this.totalCost());
		if(this.dept != null){
			jm.put("dept", this.dept);
		}
		if(this.material != null){
			jm.put("material", this.material);
			jm.put("materialId", this.material.getId());
			jm.put("materialName", this.material.getName());
			jm.put("materialPinyin", this.material.getPinyin());
		}
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	
	
	
}
