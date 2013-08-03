package com.wireless.pojo.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;

public class SystemSetting implements Jsonable{
	
	private Restaurant restaurant;
	private Setting setting;
	private List<SystemStaff> staff;   // 餐厅员工(暂未使用该字段)
	
	public SystemSetting(){
		this.restaurant = new Restaurant();
		this.setting = new Setting();
		this.staff = new ArrayList<SystemStaff>();
	}
	
	public SystemSetting(Restaurant restaurant){
		this(restaurant, null, null);
	}
	
	public SystemSetting(Restaurant restaurant, Setting setting){
		this(restaurant, setting, null);
	}
	
	public SystemSetting(Restaurant restaurant, Setting setting, List<SystemStaff> staff){
		this.restaurant = restaurant;
		this.setting = setting;
		this.staff = staff;
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
	public List<SystemStaff> getStaff() {
		return staff;
	}
	public void setStaff(List<SystemStaff> staff) {
		this.staff = staff;
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
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("restaurant", this.restaurant);
		jm.put("setting", this.setting);
		jm.put("staff", this.staff);
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	
}
