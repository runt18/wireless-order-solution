package com.wireless.common;

import java.util.List;

import com.wireless.pack.req.PinGen;
import com.wireless.protocol.FoodList;
import com.wireless.protocol.FoodMenuEx;
import com.wireless.protocol.PRegion;
import com.wireless.protocol.PRestaurant;
import com.wireless.protocol.PTable;
import com.wireless.protocol.Pager;
import com.wireless.protocol.StaffTerminal;

public class WirelessOrder {
	public static PinGen pinGen;
	public static FoodMenuEx foodMenu;
	public static FoodList foods;
	public static PRestaurant restaurant;
	public static StaffTerminal[] staffs;
	public static PRegion[] regions;
	public static PTable[] tables;
	public static List<Pager> pagers;
}
