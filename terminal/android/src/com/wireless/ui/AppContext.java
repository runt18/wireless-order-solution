package com.wireless.ui;

import com.wireless.protocol.FoodMenu;

import android.app.Application;

public class AppContext extends Application {
  private String notice;
  private String username;
  private String restaurant;
  private  FoodMenu foodMenu;
  
  
	public String getNotice() {
		return notice;
	}
	public void setNotice(String notice) {
		this.notice = notice;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getRestaurant() {
		return restaurant;
	}
	public void setRestaurant(String restaurant) {
		this.restaurant = restaurant;
	}
	public FoodMenu getFoodMenu() {
		return foodMenu;
	}
	public void setFoodMenu(FoodMenu foodMenu) {
		this.foodMenu = foodMenu;
	}
    
  
   
}
