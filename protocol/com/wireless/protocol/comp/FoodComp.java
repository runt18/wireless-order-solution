package com.wireless.protocol.comp;

import java.util.Comparator;

import com.wireless.protocol.Food;

public class FoodComp implements Comparator<Food>{

	private static final FoodComp mInstance = new FoodComp();
	
	public static FoodComp instance(){
		return mInstance;
	}
	
	@Override
	public int compare(Food arg0, Food arg1) {
		return arg0.compareTo(arg1);
	}

}
