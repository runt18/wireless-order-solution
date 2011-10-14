package com.wireless.util;

import java.util.ArrayList;

import com.wireless.protocol.Food;
import com.wireless.protocol.Order;

public class Util {
	public static String toOrderCate(int type){
		if(type == Order.CATE_NORMAL){
			return "一般";
		}else if(type == Order.CATE_JOIN_TABLE){
			return "并台";
		}else if(type == Order.CATE_MERGER_TABLE){
			return "拼台";
		}else if(type == Order.CATE_TAKE_OUT){
			return "外卖";
		}else{
			return "一般";
		}
	}
	
	public static String toPayManner(int manner){
		if(manner == Order.MANNER_CASH){
			return "现金";
		}else if(manner == Order.MANNER_CREDIT_CARD){
			return "刷卡";
		}else if(manner == Order.MANNER_HANG){
			return "挂账";
		}else if(manner == Order.MANNER_MEMBER){
			return "会员卡";
		}else if(manner == Order.MANNER_SIGN){
			return "签单";
		}else{
			return "现金";
		}
	}
	
	/**
	 * Convert the foods string submitted by terminal into the array of class food.
	 * @param submitFoods the submitted string looks like below.<br>
	 * 			{[是否临时菜(false),菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号]，
	 * 			 [是否临时菜(false),菜品2编号,菜品2数量,口味2编号,厨房2编号,菜品2折扣,2nd口味1编号,3rd口味1编号]，...
	 * 			 [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价]，
	 * 			 [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价]...}
	 * @return the class food array
	 */
	public static Food[] toFoodArray(String submitFoods) throws NumberFormatException{
		//remove the "{}"
		submitFoods = submitFoods.substring(1, submitFoods.length() - 1);
		//extract each food item string
		String[] foodItems = submitFoods.split("，");
		Food[] foods = new Food[foodItems.length];
		for(int i = 0; i < foodItems.length; i++){
			//remove the "[]"
			String foodItem = foodItems[i].substring(1, foodItems[i].length() - 1);
			foods[i] = new Food();
			//extract each food detail information string			
			String[] values = foodItem.split(",");		
			//extract the food alias id
			foods[i].alias_id = Integer.parseInt(values[0]);
			//extract the amount to order food
			foods[i].setCount(Float.parseFloat(values[1]));
			//extract the taste alias id
			foods[i].tastes[0].alias_id = Short.parseShort(values[2]);
			//extract the kitchen number
			foods[i].kitchen = Short.parseShort(values[3]);
			//extract the discount
			if(values.length > 4){
				//foods[i].discount = (byte)(Float.parseFloat(values[4]) * 100);
				foods[i].setDiscount(Float.parseFloat(values[4]));
			}
			if(values.length > 5){
				//extract the 2nd taste alias id
				foods[i].tastes[1].alias_id = Short.parseShort(values[5]);
				//extract the 3rd taste alias id
				foods[i].tastes[2].alias_id = Short.parseShort(values[6]);
			}
		}
		
		/**
		 * Combine the amount of the same food.
		 */
		ArrayList<Food> tmpFoods = new ArrayList<Food>();
		for(int i = 0; i < foods.length; i++){
			
			int index = tmpFoods.indexOf(foods[i]);
			if(index != -1){
				Food food = tmpFoods.get(index);
				float count = food.getCount().floatValue() + foods[i].getCount().floatValue();
				food.setCount(count);
			}else{
				tmpFoods.add(foods[i]);
			}
			
		}
		
		return tmpFoods.toArray(new Food[tmpFoods.size()]);
	}
}
