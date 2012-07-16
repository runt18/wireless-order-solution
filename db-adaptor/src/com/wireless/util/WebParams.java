package com.wireless.util;

import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;

public class WebParams {
	
	/**
	 * 
	 */
	public static final int FOOD_NORMAL = 0;		/* 正常 */
	public static final int FOOD_HANG_UP = 1;		/* 叫起 */
	public static final int FOOD_IMMEDIATE = 2;		/* 即起 */
	
	/**
	 * The payment type is as below
	 */
	public final static int PAY_NORMAL = 1;
	public final static int PAY_MEMBER = 2;

	/**
	 * The discount type is as below
	 */
	public final static int DISCOUNT_1 = 1;
	public final static int DISCOUNT_2 = 2;
	public final static int DISCOUNT_3 = 3;

	/**
	 * The pay manner is as below
	 */
	public final static int MANNER_CASH = 1;		//现金
	public final static int MANNER_CREDIT_CARD = 2;	//刷卡
	public final static int MANNER_MEMBER = 3;		//会员卡
	public final static int MANNER_SIGN = 4;		//签单
	public final static int MANNER_HANG = 5;		//挂账

	/**
	 * The category is as below. 	
	 */
	public final static short CATE_NORMAL = 1;			//一般
	public final static short CATE_TAKE_OUT = 2;		//外卖
	public final static short CATE_JOIN_TABLE = 3;		//并台
	public final static short CATE_MERGER_TABLE = 4;	//拼台
	
	/**
	 * 
	 */
	public final static short NO_TASTE = Taste.NO_TASTE;
	public final static String NO_PREFERENCE = Taste.NO_PREFERENCE; 
	
	/**
	 * The status of the food.
	 * It can be the combination of values below.
	 */
	public final static byte SPECIAL = OrderFood.SPECIAL;		/* 特价 */
	public final static byte RECOMMEND = OrderFood.RECOMMEND;	/* 推荐 */ 
	public final static byte SELL_OUT = OrderFood.SELL_OUT;		/* 售完 */
	public final static byte GIFT = OrderFood.GIFT;				/* 赠送 */
	public final static byte CUR_PRICE = OrderFood.CUR_PRICE;	/* 时价 */
	
}
