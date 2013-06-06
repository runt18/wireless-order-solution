package com.wireless.pojo.stockMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;

public class MaterialDept implements Jsonable{

	private int materialId;
	private int deptId;
	private int restaurantId;
	private float stock;
	
	public int getMaterialId() {
		return materialId;
	}
	public void setMaterialId(int materialId) {
		this.materialId = materialId;
	}
	public int getDeptId() {
		return deptId;
	}
	public void setDeptId(int deptId) {
		this.deptId = deptId;
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
		this.materialId = materialId;
		this.deptId= deptId;
		this.restaurantId = restaurantId;
		this.stock = stock;
	}
	
	@Override
	public String toString(){
		return "materialDept : materialId = " + materialId +
				" ,deptId = " + deptId +
				" ,restaurantId" + restaurantId +
				" ,stock" + stock;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof MaterialDept)) {
			return false;
		} else {
			return materialId == ((MaterialDept) obj).materialId
					&& deptId == ((MaterialDept) obj).deptId
					&& stock == ((MaterialDept) obj).stock;
		}
	}	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + materialId;
		result = result * 31 + deptId;
		return result;
	}
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("materialId", this.getMaterialId());
		jm.put("deptId", this.getDeptId());
		jm.put("restaurantId", this.getRestaurantId());
		jm.put("stock", this.getStock());
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
}
