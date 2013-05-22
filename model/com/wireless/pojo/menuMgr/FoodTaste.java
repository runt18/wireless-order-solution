package com.wireless.pojo.menuMgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.tasteMgr.Taste;

public class FoodTaste implements Jsonable{
	private Taste taste;
	private Food food;
	
	void init(Taste taste, Food food){
		this.taste = taste;
		this.food = food;
	}
	public FoodTaste(){
		init(new Taste(), new Food());
	}
	public FoodTaste(Taste taste){
		init(taste, new Food());
	}
	public FoodTaste(Taste taste, Food food){
		init(taste, food);
	}
	
	public Taste getTaste() {
		return taste;
	}
	public void setTaste(Taste taste) {
		this.taste = taste;
	}
	public Food getFood() {
		return food;
	}
	public void setFood(Food food) {
		this.food = food;
	}
	public void setFood(int id, int alias, int rid, String name) {
		this.food = new Food(id, alias, rid);
		this.food.setName(name);
	}
	
	@Override
	public String toString() {
		return "id=" + this.taste.getTasteId() + ", name=" + this.food.getName() + ", foodId=" + this.getFood().getFoodId();
	}
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		this.food.setKitchen(null);
		jm.put("taste", this.taste.toJsonMap(0));
		jm.put("food", this.food.toJsonMap(0));
		
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	
}
