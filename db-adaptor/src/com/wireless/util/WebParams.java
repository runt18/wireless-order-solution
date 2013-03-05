package com.wireless.util;

import com.wireless.protocol.Order;
import com.wireless.protocol.Taste;

public class WebParams {
	
	/**
	 * The payment type is as below
	 */
	public static final int PAY_NORMAL = Order.PAY_IN_NORMAL;
	public static final int PAY_MEMBER = Order.PAY_IN_MEMBER;

	/**
	 * 
	 */
	public static final int CATE_TASTE = Taste.CATE_TASTE;		/* 口味 */
	public static final int CATE_STYLE = Taste.CATE_STYLE;		/* 做法 */
	public static final int CATE_SPEC = Taste.CATE_SPEC;		/* 规格 */
	public static final int CALC_PRICE = Taste.CALC_PRICE;		/* 按价格计算  */
	public static final int CALC_RATE = Taste.CALC_RATE;		/* 按比例计算  */
	public static final String CATE_TASTE_TEXT = "口味"; 
	public static final String CATE_STYLE_TEXT = "做法"; 
	public static final String CATE_SPEC_TEXT = "规格"; 
	public static final String CALC_PRICE_TEXT = "按价格"; 
	public static final String CALC_RATE_TEXT = "按比例";
	
	/**
	 * 
	 */
	public static final int ERROR_CODE = WebParams.TIP_CODE_DEFAULT;
	public static final String ERROR_MSG = "";
	public static final String ERROR_TITLE = WebParams.TIP_TITLE_DEFAULT;
	public static final int ERROR_LV = 0;
	
	public static final int TIP_CODE_ERROE = 8888;
	public static final int TIP_CODE_EXCEPTION = 9999;
	public static final int TIP_CODE_WARNING = 7777;
	public static final int TIP_CODE_DEFAULT = 1111;
	
	public static final String TIP_TITLE_ERROE = "错误";
	public static final String TIP_TITLE_EXCEPTION = "异常";
	public static final String TIP_TITLE_WARNING = "警告";
	public static final String TIP_TITLE_DEFAULT = "提示";
	
	public static final String TIP_CONTENT_SQLEXCEPTION = "操作失败, 数据库操作请求发生异常.";
	
	/* 尾数处理的方式 */
	public static final int TAIL_NO_ACTION = 0;			//小数部分不处理
	public static final int TAIL_DECIMAL_CUT = 1;		//小数抹零
	public static final int TAIL_DECIMAL_ROUND = 2;		//小数四舍五入	
	
	public static final int IMAGE_UPLOAD_MAX_SIZE_DEFAULT = 300;
	public static final String IMAGE_UPLOAD_MAX_SIZE = "imageUploadMaxSize";
	public static final String IMAGE_UPLOAD_TYPE = "imageUploadType";
	public static final String IMAGE_UPLOAD_PATH = "imageUploadPath";
	public static final String IMAGE_BROWSE_DEFAULT_FILE = "imageBrowseDefaultFile";
	public static final String IMAGE_BROWSE_PATH = "imageBrowsePath";
	
}
