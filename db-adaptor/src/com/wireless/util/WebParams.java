package com.wireless.util;

import com.wireless.protocol.Order;

public class WebParams {
	
	/**
	 * The payment type is as below
	 */
	public static final int PAY_NORMAL = Order.SettleType.NORMAL.getVal();
	public static final int PAY_MEMBER = Order.SettleType.MEMBER.getVal();

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
	
	public static final int IMAGE_UPLOAD_MAX_SIZE_DEFAULT = 300;
	public static final String IMAGE_UPLOAD_MAX_SIZE = "imageUploadMaxSize";
	public static final String IMAGE_UPLOAD_TYPE = "imageUploadType";
	public static final String IMAGE_UPLOAD_PATH = "imageUploadPath";
	public static final String IMAGE_BROWSE_DEFAULT_FILE = "imageBrowseDefaultFile";
	public static final String IMAGE_BROWSE_PATH = "imageBrowsePath";
	
}
