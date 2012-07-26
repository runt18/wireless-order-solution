package com.wireless.util;

import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;

public class WebParams {
	
	/**
	 * 
	 */
	public static final int FOOD_NORMAL = OrderFood.FOOD_NORMAL;		/* 正常 */
	public static final int FOOD_HANG_UP = OrderFood.FOOD_HANG_UP;		/* 叫起 */
	public static final int FOOD_IMMEDIATE = OrderFood.FOOD_IMMEDIATE;	/* 即起 */
	
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
	public final static short CATE_TASTE = Taste.CATE_TASTE;	/* 口味 */
	public final static short CATE_STYLE = Taste.CATE_STYLE;	/* 做法 */
	public final static short CATE_SPEC = Taste.CATE_SPEC;		/* 规格 */
	public final static short CALC_PRICE = Taste.CALC_PRICE;	/* 按价格计算  */
	public final static short CALC_RATE = Taste.CALC_RATE;		/* 按比例计算  */
	public final static String NO_PREFERENCE = Taste.NO_PREFERENCE; 
	public final static String CATE_TASTE_TEXT = "口味"; 
	public final static String CATE_STYLE_TEXT = "做法"; 
	public final static String CATE_SPEC_TEXT = "规格"; 
	public final static String CALC_PRICE_TEXT = "按价格"; 
	public final static String CALC_RATE_TEXT = "按比例"; 
	public final static long TASTE_SMART_REF = Food.TASTE_SMART_REF;
	public final static long TASTE_MANUAL_REF = Food.TASTE_MANUAL_REF;
	
	/**
	 * The status of the food.
	 * It can be the combination of values below.
	 */
	public final static byte SPECIAL = OrderFood.SPECIAL;		/* 特价 */
	public final static byte RECOMMEND = OrderFood.RECOMMEND;	/* 推荐 */ 
	public final static byte SELL_OUT = OrderFood.SELL_OUT;		/* 售完 */
	public final static byte GIFT = OrderFood.GIFT;				/* 赠送 */
	public final static byte CUR_PRICE = OrderFood.CUR_PRICE;	/* 时价 */
	
	/**
	 * 
	 */
	public final static long ERROR_CODE = 1111;
	public final static String ERROR_MSG = "";
	public final static String ERROR_TITLE = WebParams.TIP_TITLE_DEFAULT;
	public final static long ERROR_LV = 0;
	
	public static final String TIP_TITLE_ERROE = "错误";
	public static final String TIP_TITLE_EXCEPTION = "异常";
	public static final String TIP_TITLE_WARNING = "警告";
	public static final String TIP_TITLE_DEFAULT = "提示";
}
