package com.wireless.protocol;

import java.util.HashMap;

public class Discount {
	public int discountID;
	public String name;
	public int restaurantID;
	public int level;
	public HashMap<Kitchen, Float> plan = new HashMap<Kitchen, Float>();
}
