package com.wireless.util;

import com.wireless.protocol.Food;
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
	public final static short TASTE_SMART_REF = Food.TASTE_SMART_REF;
	public final static short TASTE_MANUAL_REF = Food.TASTE_MANUAL_REF;
	
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
	public final static short TAIL_NO_ACTION = 0;			//小数部分不处理
	public final static short TAIL_DECIMAL_CUT = 1;			//小数抹零
	public final static short TAIL_DECIMAL_ROUND = 2;		//小数四舍五入	
}
