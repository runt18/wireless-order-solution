package com.wireless.pojo.system;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;

public class SystemSetting implements Jsonable{
	
	private Restaurant restaurant;
	private Setting setting;
	
	public SystemSetting(){
		this.restaurant = new Restaurant();
		this.setting = new Setting();
	}
	
	public SystemSetting(Restaurant restaurant){
		this(restaurant, null);
	}
	
	public SystemSetting(Restaurant restaurant, Setting setting){
		this.restaurant = restaurant;
		this.setting = setting;
	}
	
	public int getRestaurantID() {
		return this.getRestaurant() != null ? this.getRestaurant().getId(): 0;
	}
	
	public void setRestaurantID(int rid) {
		if(this.getRestaurant() != null)
			this.getRestaurant().setId(rid);
	}
	
	public Restaurant getRestaurant() {
		return restaurant;
	}
	
	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}
	
	public Setting getSetting() {
		return setting;
	}
	
	public void setSetting(Setting setting) {
		this.setting = setting;
	}

	/**
	 * 金额尾数处理方式显示信息
	 * @return
	 */
	public String getPriceTailDisplay() {
		String display;
		if(this.getSetting().getPriceTail() == Setting.Tail.NO_ACTION){
			display = "不处理";
		}else if(this.getSetting().getPriceTail() == Setting.Tail.DECIMAL_CUT){
			display = "抹零";
		}else if(this.getSetting().getPriceTail() == Setting.Tail.DECIMAL_ROUND){
			display = "四舍五入";
		}else{
			display = "不处理";
		}
		return display;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("restaurant", this.restaurant, 0);
		jm.putJsonable("setting", this.setting, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
