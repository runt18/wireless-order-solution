package com.wireless.common;

import java.util.List;

import com.wireless.protocol.FoodMenuEx;
import com.wireless.protocol.FoodMenuEx.FoodList;
import com.wireless.protocol.Pager;
import com.wireless.protocol.Region;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Table;

public class WirelessOrder {
	public static long pin;
	public static FoodMenuEx foodMenu;
	public static FoodList foods;
	public static Restaurant restaurant;
	public static StaffTerminal[] staffs;
	public static Region[] regions;
	public static Table[] tables;
	public static List<Pager> pagers;
}
