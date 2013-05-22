package com.wireless.pojo.menuMgr;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.Food;

public class FoodCombo extends Food implements Jsonable{
	private long parentId;			// 套菜所属菜品编号
	private String parentName;		// 套菜所属菜品名称
	private int amount;				// 菜品份数
	
	public long getParentId() {
		return parentId;
	}
	public void setParentId(long parentId) {
		this.parentId = parentId;
	}
	public String getParentName() {
		return parentName;
	}
	public void setParentName(String parentName) {
		this.parentName = parentName;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = super.toJsonMap();
		jm.put("parentId", this.parentId);
		jm.put("parentName", this.parentName);
		jm.put("amount", this.amount);
		return Collections.unmodifiableMap(jm);
	}
	
	@Override
	public List<Object> toJsonList(int flag) {
		return super.toJsonList(flag);
	}
	
}
