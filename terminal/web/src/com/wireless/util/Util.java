package com.wireless.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;

public class Util {
	public static String toOrderCate(int type) {
		if (type == Order.CATE_NORMAL) {
			return "一般";
		} else if (type == Order.CATE_JOIN_TABLE) {
			return "并台";
		} else if (type == Order.CATE_MERGER_TABLE) {
			return "拼台";
		} else if (type == Order.CATE_TAKE_OUT) {
			return "外卖";
		} else {
			return "一般";
		}
	}

	public static String toPayManner(int manner) {
		if (manner == Order.MANNER_CASH) {
			return "现金";
		} else if (manner == Order.MANNER_CREDIT_CARD) {
			return "刷卡";
		} else if (manner == Order.MANNER_HANG) {
			return "挂账";
		} else if (manner == Order.MANNER_MEMBER) {
			return "会员卡";
		} else if (manner == Order.MANNER_SIGN) {
			return "签单";
		} else {
			return "现金";
		}
	}

	/**
	 * Convert the foods string submitted by terminal into the array of class
	 * food.
	 * 
	 * @param submitFoods
	 *            the submitted string looks like below.<br>
	 *            {[是否临时菜(false),菜品1编号,菜品1数量,口味1编号,厨房1编号,菜品1折扣,2nd口味1编号,3rd口味1编号,是否临时口味,临时口味,临时口味价钱,临时口味编号]，
	 *             [是否临时菜(false),菜品2编号,菜品2数量,口味2编号,厨房2编号,菜品2折扣,2nd口味1编号,3rd口味1编号,是否临时口味,临时口味,临时口味价钱,临时口味编号]，... 
	 *             [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价]，
	 *             [是否临时菜(true),临时菜1编号,临时菜1名称,临时菜1数量,临时菜1单价]...}
	 * @return the class food array
	 */
	public static OrderFood[] toFoodArray(String submitFoods)
			throws NumberFormatException {
		// remove the "{}"
		submitFoods = submitFoods.substring(1, submitFoods.length() - 1);
		// extract each food item string
		String[] foodItems = submitFoods.split("，");
		OrderFood[] foods = new OrderFood[foodItems.length];
		for (int i = 0; i < foodItems.length; i++) {
			// remove the "[]"
			String foodItem = foodItems[i].substring(1,	foodItems[i].length() - 1);
			foods[i] = new OrderFood();
			// extract each food detail information string
			String[] values = foodItem.split(",");
			// extract the temporary flag
			if (Boolean.parseBoolean(values[0])) {
				// set the temporary flag
				foods[i].isTemporary = true;
				// extract the alias id to this temporary food
				int aliasID = Integer.parseInt(values[1]);
				if(aliasID < 0){
					//generate an unique food id to temporary food if it is new 
					int tmpFoodID;
					boolean isUnique;
					do{
						tmpFoodID = com.wireless.protocol.Util.genTempFoodID();
						isUnique = true;
						for(int j = 0; j < i; j++){
							if(foods[j].isTemporary){
								if(tmpFoodID == foods[j].aliasID){
									isUnique = false;
									break;
								}
							}
						}						
					}while(!isUnique);
					
					foods[i].aliasID = tmpFoodID;
					
				}else{			
					//assign the alias id
					foods[i].aliasID = aliasID;					
				}
				// extract the name to this temporary food
				foods[i].name = values[2];
				// extract the amount to this temporary food
				foods[i].setCount(Float.parseFloat(values[3]));
				// extract the unit price to this temporary food
				foods[i].setPrice(Float.parseFloat(values[4]));
			} else {
				// extract the food alias id
				foods[i].aliasID = Integer.parseInt(values[1]);
				// extract the amount to order food
				foods[i].setCount(Float.parseFloat(values[2]));
				// extract the taste alias id
				foods[i].tastes[0].aliasID = Short.parseShort(values[3]);
				// extract the kitchen number
				foods[i].kitchen.aliasID = Short.parseShort(values[4]);
				// extract the discount
				foods[i].setDiscount(Float.parseFloat(values[5]));
				// extract the 2nd taste alias id
				foods[i].tastes[1].aliasID = Short.parseShort(values[6]);
				// extract the 3rd taste alias id
				foods[i].tastes[2].aliasID = Short.parseShort(values[7]);
				//check to see whether containing temporary taste
				if(Boolean.parseBoolean(values[8])){
					foods[i].tmpTaste = new Taste();
					//extract the value to temporary taste
					foods[i].tmpTaste.preference = values[9];
					//extract the price to temporary taste
					foods[i].tmpTaste.setPrice(Float.parseFloat(values[10]));
					//extract the alias to temporary taste
					foods[i].tmpTaste.aliasID = Integer.parseInt(values[11]);
				}else{
					foods[i].tmpTaste = null;
				}
			}
		}

		/**
		 * Combine the amount of the same food.
		 */
		ArrayList<OrderFood> tmpFoods = new ArrayList<OrderFood>();
		for (int i = 0; i < foods.length; i++) {

			int index = tmpFoods.indexOf(foods[i]);
			if (index != -1) {
				OrderFood food = tmpFoods.get(index);
				float count = food.getCount().floatValue()
						+ foods[i].getCount().floatValue();
				food.setCount(count);
			} else {
				tmpFoods.add(foods[i]);
			}

		}

		return tmpFoods.toArray(new OrderFood[tmpFoods.size()]);
	}

	public static String getMD5Str(String str) {
		MessageDigest messageDigest = null;

		try {
			messageDigest = MessageDigest.getInstance("MD5");

			messageDigest.reset();

			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			System.out.println("NoSuchAlgorithmException caught!");
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}

		return md5StrBuff.toString();
	}

	public static long getNewPIN(DBCon dbCon) throws Exception {
		String sql;
		sql = "SELECT MAX(pin) FROM " + Params.dbName
				+ ".terminal WHERE model_id = 255";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		dbCon.rs.next();
		long maxPin = dbCon.rs.getLong(1);

		long newPin = maxPin + 1;

		return newPin;
	}

}
