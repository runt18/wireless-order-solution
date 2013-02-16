package com.wireless.protocol.parcel;

import junit.framework.Assert;

import org.junit.Test;

import com.wireless.protocol.Food;
import com.wireless.protocol.FoodStatistics;
import com.wireless.protocol.Taste;

public class TestParcel {

	@Test
	public void testComplexFoodParcel(){
		Food foodToParcel = new Food();
		
		foodToParcel.setAliasId(1);
		foodToParcel.setPrice(2.45f);
		foodToParcel.getKitchen().setAliasId((short)1);
		foodToParcel.setHot(true);
		foodToParcel.setSpecial(true);
		foodToParcel.setName("测试菜品");
		foodToParcel.setImage("238f91a1.jpg");
		foodToParcel.setStatistics(new FoodStatistics(15367));
		foodToParcel.setPopTastes(new Taste[]{
				new Taste(0, 1, 0), 
				new Taste(0, 2, 0),
				new Taste(0, 3, 0),
				new Taste(0, 4, 0),
				new Taste(0, 5, 0),
				new Taste(0, 6, 0),
				new Taste(0, 7, 0),
				new Taste(0, 8, 0),
				new Taste(0, 9, 0),
				new Taste(0, 10, 0)
			});
		foodToParcel.setChildFoods(new Food[]{
				new Food(0, 1, 0),
				new Food(0, 2, 0),
				new Food(0, 3, 0),
				new Food(0, 4, 0),
				new Food(0, 5, 0),
				new Food(0, 6, 0),
				new Food(0, 7, 0),
				new Food(0, 8, 0),
				new Food(0, 9, 0),
				new Food(0, 10, 0)
		});
		
		Parcel p = new Parcel();
		foodToParcel.writeToParcel(p, Food.FOOD_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		Food parcelableFood = new Food();
		parcelableFood.createFromParcel(p2);
		
		// Check the food alias id 
		Assert.assertEquals("food alias id", foodToParcel.getAliasId(), parcelableFood.getAliasId());
		
		// Check the food unit price
		Assert.assertEquals("food unit price", foodToParcel.getPrice(), parcelableFood.getPrice());
		
		// Check the kitchen to food
		Assert.assertEquals("kitchen to food", foodToParcel.getKitchen(), parcelableFood.getKitchen());
		
		// Check the status to food
		Assert.assertEquals("hot flag to food", foodToParcel.isHot(), parcelableFood.isHot());
		Assert.assertEquals("special flag to food", foodToParcel.isSpecial(), parcelableFood.isSpecial());
		Assert.assertEquals("combo flag to food", foodToParcel.isCombo(), parcelableFood.isCombo());
		Assert.assertEquals("current price flag to food", foodToParcel.isCurPrice(), parcelableFood.isCurPrice());
		Assert.assertEquals("gift flag to food", foodToParcel.isGift(), parcelableFood.isGift());
		Assert.assertEquals("recommend flag to food", foodToParcel.isRecommend(), parcelableFood.isRecommend());
		Assert.assertEquals("sell out flag to food", foodToParcel.isSellOut(), parcelableFood.isSellOut());
		Assert.assertEquals("weight flag to food", foodToParcel.isWeigh(), parcelableFood.isWeigh());
		
		// Check the name to food
		Assert.assertEquals("food name", foodToParcel.getName(), parcelableFood.getName());
		
		// Check the image to food
		Assert.assertEquals("food image", foodToParcel.getImage(), parcelableFood.getImage());

		// Check the the order count
		Assert.assertEquals("order count to food", foodToParcel.statistics.getOrderCnt(), parcelableFood.statistics.getOrderCnt());
		
		// Check the pop tastes
		Assert.assertEquals(foodToParcel.getPopTastes().length, parcelableFood.getPopTastes().length);
		for(int i = 0; i < foodToParcel.getPopTastes().length; i++){
			Assert.assertEquals("pop tastes to food", foodToParcel.getPopTastes()[i], parcelableFood.getPopTastes()[i]);
		}
		
		// Check the child foods
		Assert.assertEquals(foodToParcel.getChildFoods().length, parcelableFood.getChildFoods().length);
		for(int i = 0; i < foodToParcel.getChildFoods().length; i++){
			Assert.assertEquals("children to food", foodToParcel.getChildFoods()[i], parcelableFood.getChildFoods()[i]);
		}
	}
	
	@Test
	public void testComplexTasteParcel(){
		Taste tasteToParcel = new Taste();
		
		tasteToParcel.setAliasId(1);
		tasteToParcel.setCategory(Taste.CATE_STYLE);
		tasteToParcel.setCalc(Taste.CALC_RATE);
		tasteToParcel.setType(Taste.TYPE_RESERVED);
		tasteToParcel.setPrice(2.3f);
		tasteToParcel.setRate(0.2f);
		tasteToParcel.setPreference("测试口味");
		
		Parcel p = new Parcel();
		tasteToParcel.writeToParcel(p, Taste.TASTE_PARCELABLE_COMPLEX);
		
		Parcel p2 = new Parcel();
		p2.unmarshall(p.marshall());
		
		Taste parcelableTaste = new Taste();
		parcelableTaste.createFromParcel(p2);
		
		//Check the taste alias id
		Assert.assertEquals("taste alias id", tasteToParcel.getAliasId(), parcelableTaste.getAliasId());
		
		//Check the taste category
		Assert.assertEquals("taste category", tasteToParcel.getCategory(), parcelableTaste.getCategory());
		
		//Check the taste calculate type
		Assert.assertEquals("taste calculate type", tasteToParcel.getCalc(), parcelableTaste.getCalc());

		//Check the taste type
		Assert.assertEquals("taste type", tasteToParcel.getType(), parcelableTaste.getType());

		//Check the taste price
		Assert.assertEquals("taste price", tasteToParcel.getPrice(), parcelableTaste.getPrice());
		
		//Check the taste rate
		Assert.assertEquals("taste rate", tasteToParcel.getRate(), parcelableTaste.getRate());

		//Check the taste preference
		Assert.assertEquals("taste preference", tasteToParcel.getPreference(), parcelableTaste.getPreference());

	}
}
