package com.wireless.util;

import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;

public class WebParams {
	
	/**
	 * 
	 */
	public final static int FOOD_NORMAL = OrderFood.FOOD_NORMAL;		/* 正常 */
	public final static int FOOD_HANG_UP = OrderFood.FOOD_HANG_UP;		/* 叫起 */
	public final static int FOOD_IMMEDIATE = OrderFood.FOOD_IMMEDIATE;	/* 即起 */
	
	/**
	 * The payment type is as below
	 */
	public final static int PAY_NORMAL = Order.PAY_NORMAL;
	public final static int PAY_MEMBER = Order.PAY_MEMBER;

	/**
	 * The discount type is as below
	 */
	public final static int DISCOUNT_1 = Order.DISCOUNT_1;
	public final static int DISCOUNT_2 = Order.DISCOUNT_2;
	public final static int DISCOUNT_3 = Order.DISCOUNT_3;

	/**
	 * The pay manner is as below
	 */
	public final static int MANNER_CASH = Order.MANNER_CASH;				//现金
	public final static int MANNER_CREDIT_CARD = Order.MANNER_CREDIT_CARD;	//刷卡
	public final static int MANNER_MEMBER = Order.MANNER_MEMBER;			//会员卡
	public final static int MANNER_SIGN = Order.MANNER_SIGN;				//签单
	public final static int MANNER_HANG = Order.MANNER_HANG;				//挂账

	/**
	 * The category is as below. 	
	 */
	public final static int CATE_NORMAL = Order.CATE_NORMAL;				//一般
	public final static int CATE_TAKE_OUT = Order.CATE_TAKE_OUT;			//外卖
	public final static int CATE_JOIN_TABLE = Order.CATE_JOIN_TABLE;		//并台
	public final static int CATE_MERGER_TABLE = Order.CATE_MERGER_TABLE;	//拼台
	
	/**
	 * 
	 */
	public final static int NO_TASTE = Taste.NO_TASTE;
	public final static int CATE_TASTE = Taste.CATE_TASTE;		/* 口味 */
	public final static int CATE_STYLE = Taste.CATE_STYLE;		/* 做法 */
	public final static int CATE_SPEC = Taste.CATE_SPEC;		/* 规格 */
	public final static int CALC_PRICE = Taste.CALC_PRICE;		/* 按价格计算  */
	public final static int CALC_RATE = Taste.CALC_RATE;		/* 按比例计算  */
	public final static String NO_PREFERENCE = Taste.NO_PREFERENCE; 
	public final static String CATE_TASTE_TEXT = "口味"; 
	public final static String CATE_STYLE_TEXT = "做法"; 
	public final static String CATE_SPEC_TEXT = "规格"; 
	public final static String CALC_PRICE_TEXT = "按价格"; 
	public final static String CALC_RATE_TEXT = "按比例"; 
	public final static int TASTE_SMART_REF = Food.TASTE_SMART_REF;
	public final static int TASTE_MANUAL_REF = Food.TASTE_MANUAL_REF;
	
	/**
	 * The status of the food.
	 * It can be the combination of values below.
	 */
	public final static byte FS_SPECIAL = Food.SPECIAL;			/* 特价 */
	public final static byte FS_RECOMMEND = Food.RECOMMEND;		/* 推荐 */ 
	public final static byte FS_STOP = Food.SELL_OUT;			/* 停售 */
	public final static byte FS_GIFT = Food.GIFT;				/* 赠送 */
	public final static byte FS_CUR_PRICE = Food.CUR_PRICE;		/* 时价 */
	public final static byte FS_COMBO = Food.COMBO;				/* 套菜 */
	
	/**
	 * 
	 */
	public final static int ERROR_CODE = WebParams.TIP_CODE_DEFAULT;
	public final static String ERROR_MSG = "";
	public final static String ERROR_TITLE = WebParams.TIP_TITLE_DEFAULT;
	public final static int ERROR_LV = 0;
	
	public final static int TIP_CODE_ERROE = 3333;
	public final static int TIP_CODE_EXCEPTION = 9999;
	public final static int TIP_CODE_WARNING = 2222;
	public final static int TIP_CODE_DEFAULT = 1111;
	
	public final static String TIP_TITLE_ERROE = "错误";
	public final static String TIP_TITLE_EXCEPTION = "异常";
	public final static String TIP_TITLE_WARNING = "警告";
	public final static String TIP_TITLE_DEFAULT = "提示";
	
	/* 尾数处理的方式 */
	public final static int TAIL_NO_ACTION = 0;			//小数部分不处理
	public final static int TAIL_DECIMAL_CUT = 1;		//小数抹零
	public final static int TAIL_DECIMAL_ROUND = 2;		//小数四舍五入	
}
