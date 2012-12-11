package com.wireless.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static final SimpleDateFormat formatToLocalhost = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static SimpleDateFormat formatToUserSetting = new SimpleDateFormat();
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String formatHandler(Date date){
		return DateUtil.formatToLocalhost.format(date);
	}
	
	/**
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String formatHandler(Date date, String pattern){
		formatToUserSetting.applyPattern(pattern);
		return formatToUserSetting.format(date);
	}
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String formatHandler(long date){
		return DateUtil.formatToLocalhost.format(date);
	}
	
	/**
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String formatHandler(long date, String pattern){
		formatToUserSetting.applyPattern(pattern);
		return formatToUserSetting.format(date);
	}
}
