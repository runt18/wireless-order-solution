package com.wireless.pojo.system;

import java.util.ArrayList;
import java.util.List;

public class SystemSetting {
	
	public static final int TAIL_NO_ACTION = 0;			//小数部分不处理
	public static final int TAIL_DECIMAL_CUT = 1;		//小数抹零
	public static final int TAIL_DECIMAL_ROUND = 2;		//小数四舍五入	
	
	private Restaurant restaurant;
	private Setting setting;
	private List<Staff> staff;   // 餐厅员工(暂未使用该字段)
	
	public SystemSetting(){
		this.restaurant = new Restaurant();
		this.setting = new Setting();
		this.staff = new ArrayList<Staff>();
	}
	
	public SystemSetting(Restaurant restaurant){
		this(restaurant, null, null);
	}
	
	public SystemSetting(Restaurant restaurant, Setting setting){
		this(restaurant, setting, null);
	}
	
	public SystemSetting(Restaurant restaurant, Setting setting, List<Staff> staff){
		this.restaurant = restaurant;
		this.setting = setting;
		this.staff = staff;
	}
	
	public int getRestaurantID() {
		return this.getRestaurant() != null ? this.getRestaurant().getRestaurantID(): 0;
	}
	public void setRestaurantID(int restaurantID) {
		if(this.getRestaurant() != null)
			this.getRestaurant().setRestaurantID(restaurantID);
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
	public List<Staff> getStaff() {
		return staff;
	}
	public void setStaff(List<Staff> staff) {
		this.staff = staff;
	}

	/**
	 * 金额尾数处理方式显示信息
	 * @return
	 */
	public String getPriceTailDisplay() {
		String display;
		if(this.getSetting().getPriceTail() == SystemSetting.TAIL_NO_ACTION){
			display = "不处理";
		}else if(this.getSetting().getPriceTail() == SystemSetting.TAIL_DECIMAL_CUT){
			display = "抹零";
		}else if(this.getSetting().getPriceTail() == SystemSetting.TAIL_DECIMAL_ROUND){
			display = "四舍五入";
		}else{
			display = "不处理";
		}
		return display;
	}
	
}
