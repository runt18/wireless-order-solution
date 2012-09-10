package com.wireless.util;

import android.content.Intent;
import android.os.Bundle;

import com.wireless.parcel.FoodParcel;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;

public class PackOrderFoods {
	public static Intent pack(OrderFood oldFood, Intent intent)
	{
		float count = oldFood.getCount();
		
		OrderFood newFood = new OrderFood(oldFood);
		newFood.setCount(count);
		newFood.tmpTaste = new Taste();
		newFood.tmpTaste.setPreference("");
		
		Bundle bundle = new Bundle();
		bundle.putParcelable(FoodParcel.KEY_VALUE, new FoodParcel(newFood));
		intent.putExtras(bundle);
		return intent;
	}
}
