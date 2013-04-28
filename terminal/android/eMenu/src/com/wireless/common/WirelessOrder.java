package com.wireless.common;

import java.util.List;

import com.wireless.pack.req.PinGen;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.FoodList;
import com.wireless.protocol.FoodMenuEx;
import com.wireless.protocol.PRestaurant;
import com.wireless.protocol.Pager;
import com.wireless.protocol.StaffTerminal;

public class WirelessOrder {
	public static PinGen pinGen;
	public static FoodMenuEx foodMenu;
	public static FoodList foods;
	public static PRestaurant restaurant;
	public static StaffTerminal[] staffs;
	public static Region[] regions;
	public static Table[] tables;
	public static List<Pager> pagers;
}
