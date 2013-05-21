package com.wireless.pojo.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.wireless.pojo.client.MemberOperation;

/**
 * 
 * @author WuZY
 *
 */
public class DateUtil {
	public static final String patternToMOSeq = "yyyyMMddHHmmss";
	public static final String patternToLocalhost = "yyyy-MM-dd HH:mm:ss";
	public static final String patternToDate = "yyyy-MM-dd";
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String format(Date date){
		return new SimpleDateFormat(patternToLocalhost, Locale.getDefault()).format(date);
	}
	
	/**
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(Date date, String pattern){
		return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
	}
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String format(long date){
		return new SimpleDateFormat(patternToLocalhost, Locale.getDefault()).format(date);
	}
	
	/**
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String format(long date, String pattern){
		return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
	}
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String formatToDate(Date date){
		return DateUtil.format(date, DateUtil.patternToDate);
	}
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	public static String formatToDate(long date){
		return DateUtil.format(date, DateUtil.patternToDate);
	}
	
	/**
	 * 
	 * @param date
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static long parseDate(String date){
		if(date == null || date.trim().length() == 0)
			return 0;
		else
			return Date.parse(date.trim().replaceAll("-", "/"));
	}
	
	/**
	 * 
	 * @param ot
	 * @return
	 */
	public static String createMOSeq(MemberOperation.OperationType ot){
		return ot.getPrefix().concat(DateUtil.format(new Date(), DateUtil.patternToMOSeq));
	}
	
	/**
	 * 
	 * @param date
	 * @param ot
	 * @return
	 */
	public static String createMOSeq(Date date, MemberOperation.OperationType ot){
		return ot.getPrefix().concat(DateUtil.format(date, DateUtil.patternToMOSeq));
	}
	
}
