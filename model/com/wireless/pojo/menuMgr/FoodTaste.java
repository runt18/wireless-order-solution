package com.wireless.pojo.menuMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.tasteMgr.Taste;

public class FoodTaste implements Jsonable{
	private Taste taste;
	private Food food;
	
	void init(Taste taste, Food food){
		this.taste = taste;
		this.food = food;
	}
	public FoodTaste(){
		init(new Taste(0), new Food(0));
	}
	public FoodTaste(Taste taste){
		init(taste, new Food(0));
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
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		this.food.setKitchen(null);
		jm.putJsonable("taste", this.taste, 0);
		jm.putJsonable("food", this.food, 0);
		
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
