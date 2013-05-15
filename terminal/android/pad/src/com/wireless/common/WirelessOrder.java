package com.wireless.common;

import com.wireless.pack.req.PinGen;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.StaffTerminal;

public class WirelessOrder {
	public static PinGen pinGen;
	public static FoodMenu foodMenu;
	public static Restaurant restaurant;
	public static StaffTerminal[] staffs;
	public static Region[] regions;
	public static Table[] tables;
}
