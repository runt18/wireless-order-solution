package com.wireless.common;

import java.util.List;

import com.wireless.protocol.FoodList;
import com.wireless.protocol.FoodMenuEx;
import com.wireless.protocol.PRegion;
import com.wireless.protocol.Pager;
import com.wireless.protocol.PRestaurant;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;

public class WirelessOrder {
	public static long pin;
	public static FoodMenuEx foodMenu;
	public static FoodList foods;
	public static PRestaurant restaurant;
	public static StaffTerminal[] staffs;
	public static PRegion[] regions;
	public static Table[] tables;
	public static List<Pager> pagers;
}
