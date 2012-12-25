package com.wireless.db;


import com.wireless.dbObject.Setting;

public class Util {
	/**
	 * Calculate the total price according to the type of price tail
	 * @param priceTail
	 * @param totalPrice
	 * @return
	 */
	public static float calcByTail(Setting setting, float totalPrice){
		if(setting.isTailDecimalCut()){
			//小数抹零
			return Float.valueOf(totalPrice).intValue();
		}else if(setting.isTailDecimalRound()){
			//四舍五入
			return Math.round(totalPrice);
		}else{
			//不处理
			return totalPrice;
		}
	}
}
