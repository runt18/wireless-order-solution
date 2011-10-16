package com.wireless.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Taste;

public class AppContext extends Application {
	private String notice;
	private String username;
	private String restaurant;
	private static FoodMenu foodMenu;
	private List<Food> foods;
	private List<Taste> tastes;
	private List<Taste> stytles;
	private List<Taste> speces;
	public static List<Activity> activityList=new ArrayList<Activity>();;
    
    

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

     
	public static FoodMenu getFoodMenu() {
		return foodMenu;
	}

	public static void setFoodMenu(FoodMenu foodMenu) {
		AppContext.foodMenu = foodMenu;
	}

	public List<Food> getFoods() {
		foods = new ArrayList<Food>();
		for (int i = 0; i < getFoodMenu().foods.length; i++) {
			foods.add(getFoodMenu().foods[i]);
		}
		return foods;
	}
    
	public List<Taste> getTastes(){
		tastes=new ArrayList<Taste>();
		for(int i=0;i<getFoodMenu().tastes.length;i++){
			tastes.add(getFoodMenu().tastes[i]);
		}
		return tastes;
	}
	
	public List<Taste> getStyles(){
		stytles=new ArrayList<Taste>();
		for(int i=0;i<getFoodMenu().styles.length;i++){
			stytles.add(getFoodMenu().styles[i]);
		}
		return stytles;
	}
	
	public List<Taste> getSpecs(){
		speces=new ArrayList<Taste>();
		for(int i=0;i<getFoodMenu().specs.length;i++){
			speces.add(getFoodMenu().specs[i]);
		}
		return speces;
	}
	
	  /*
	    * ÍË³ö¿Í»§¶Ë
	    * 
	    * */
		public static void exitClient(Context context){
			for(int i=0;i<activityList.size();i++){
				if(null!=activityList.get(i)){
					activityList.get(i).finish();
				}
				
			}
			ActivityManager activityManager =(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
			activityManager.restartPackage("com.wireless.ui");
			        System.exit(0);
		}
	
	
}
