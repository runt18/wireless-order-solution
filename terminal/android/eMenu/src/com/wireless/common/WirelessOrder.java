package com.wireless.common;

import java.util.List;

import com.wireless.pojo.foodGroup.Pager;
import com.wireless.pojo.menuMgr.FoodList;
import com.wireless.pojo.menuMgr.FoodMenu;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;

public class WirelessOrder {
	public static Staff loginStaff;
	public static FoodMenu foodMenu;
	public static FoodList foods;
	public static Restaurant restaurant;
	public static List<Staff> staffs;
	public static Region[] regions;
	public static List<Table> tables;
	public static List<Pager> pagers;
}
