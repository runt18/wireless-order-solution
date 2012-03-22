package com.wireless.db;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;

public class QueryShift {
	
	public static class Result{
		public String onDuty;			//开始时间
		public String offDuty;			//结束时间
		public int orderAmount;			//账单数
		public Float totalCash;			//现金金额
		public Float totalCash2;		//现金实收
		public Float totalCreditCard;	//刷卡金额
		public Float totalCreditCard2;	//刷卡实收
		public Float totalMemberCard;	//会员卡金额
		public Float totalMemberCard2;	//会员卡实收
		public Float totalSign;			//签单金额
		public Float totalSign2;		//签单实收
		public Float totalHang;			//挂账金额
		public Float totalHang2;		//挂账实收
		public Float totalActual;		//合计实收金额
		public Float totalDiscount;		//合计折扣金额
		public Float totalGift;			//合计赠送金额
	}
	
	/**
	 * Perform to query the shift information through now to last shift.
	 * 
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @return the shift detail information
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The member to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result exec(long pin, short model) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, pin, model);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to query the shift information through now to last shift.
	 * Note that the database should be connected before invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @return the shift detail information
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The member to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result exec(DBCon dbCon, long pin, short model) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		
		/**
		 * Get the latest off duty date and make it as the on duty date to this duty shift
		 */
		String onDuty;
		String sql = "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurant_id +
					 " ORDER BY off_duty desc LIMIT 1";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbCon.rs.getTimestamp("off_duty"));
		}else{
			onDuty = "2011-07-30 00:00:00";
		}
		dbCon.rs.close();
		
		/**
		 * Make the current date as the off duty date
		 */
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		String offDuty = sdf.format(System.currentTimeMillis());
		
		return genShiftDetail(dbCon, term, onDuty, offDuty);

	}
	
	/**
	 * Perform to get the latest shift information. 
	 * @param dbCon
	 *            the database connection
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @return the shift detail information
	 * @return the shift detail information
	 * @throws BusinessException
	 * 			  throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The member to query does NOT exist.
	 *             - No shift record exist.
	 * @throws SQLException
	 * 				throws if fail to execute any SQL statement
	 */
	public static Result exec2(long pin, short model) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec2(dbCon, pin, model);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	/**
	 * Perform to get the latest shift information. 
	 * @param dbCon
	 *            the database connection
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @return the shift detail information
	 * @return the shift detail information
	 * @throws BusinessException
	 * 			  throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The member to query does NOT exist.
	 *             - No shift record exist.
	 * @throws SQLException
	 * 				throws if fail to execute any SQL statement
	 */
	public static Result exec2(DBCon dbCon, long pin, short model) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		
		/**
		 * Get the latest on & off duty date
		 */
		String onDuty;
		String offDuty;
		String sql = "SELECT on_duty, off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurant_id +
					 " ORDER BY off_duty desc LIMIT 1";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbCon.rs.getTimestamp("on_duty"));
			offDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbCon.rs.getTimestamp("off_duty"));
		}else{
			throw new BusinessException("No shift record to restaurant(id=" + term.restaurant_id + ") exist.");
		}
		dbCon.rs.close();
		
		return genShiftDetail(dbCon, term, onDuty, offDuty);
		
	}
	
	/**
	 * Generate the details to shift within the on & off duty date.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to request
	 * @param onDuty
	 * 			the date to be on duty
	 * @param offDuty
	 * 			the date to be off duty
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	private static Result genShiftDetail(DBCon dbCon, Terminal term, String onDuty, String offDuty) throws SQLException{
		/**
		 * Get the amount the order within this shift
		 */
		int orderAmount = 0;
		String sql = "SELECT COUNT(*) FROM " + Params.dbName + ".order WHERE restaurant_id=" + term.restaurant_id +
			  " AND total_price IS NOT NULL" +
			  " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			orderAmount = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		String prefix = "SELECT SUM(total_price * (1 + service_rate)), SUM(total_price_2) FROM " + Params.dbName + 
						".order WHERE restaurant_id=" + term.restaurant_id +
						" AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
						" AND type=";
		/**
		 * Get the total cash income within this shirt
		 */
		float totalCash = 0;
		float totalCash_2 = 0;
		sql = prefix + Order.MANNER_CASH;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			totalCash = dbCon.rs.getFloat(1);
			totalCash_2 = dbCon.rs.getFloat(2);
		}
		dbCon.rs.close();
		
		/**
		 * Get the total credit card income within this shift
		 */
		float totalCreditCard = 0;
		float totalCreditCard_2 = 0;
		sql = prefix + Order.MANNER_CREDIT_CARD;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			totalCreditCard = dbCon.rs.getFloat(1);
			totalCreditCard_2 = dbCon.rs.getFloat(2);
		}
		dbCon.rs.close();
		
		/**
		 * Get the total member card income within this shift
		 */
		float totalMemberCard = 0;
		float totalMemberCard_2 = 0;
		sql = prefix + Order.MANNER_MEMBER;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			totalMemberCard = dbCon.rs.getFloat(1);
			totalMemberCard_2 = dbCon.rs.getFloat(2);
		}
		dbCon.rs.close();
		
		/**
		 * Get the total sign income within this shift
		 */
		float totalSign = 0;
		float totalSign_2 = 0;
		sql = prefix + Order.MANNER_SIGN;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			totalSign = dbCon.rs.getFloat(1);
			totalSign_2 = dbCon.rs.getFloat(2);
		}
		dbCon.rs.close();
		
		/**
		 * Get the total hang income within this shift
		 */
		float totalHang = 0;
		float totalHang_2 = 0;
		sql = prefix + Order.MANNER_HANG;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			totalHang = dbCon.rs.getFloat(1);
			totalHang_2 = dbCon.rs.getFloat(2);
		}
		dbCon.rs.close();
		
		float totalActual = totalCash_2 + totalCreditCard_2 + totalMemberCard_2 + totalSign_2 + totalHang_2;			
		
		/**
		 * Calculate the price to all gifted food within this shift
		 */
		float totalGift = 0;
		sql = "SELECT SUM(unit_price * order_count * discount + taste_price) FROM " + Params.dbName + ".order_food WHERE order_id IN(" +
			  "SELECT id FROM " +Params.dbName + ".order WHERE restaurant_id=" + term.restaurant_id + 
			  " AND total_price IS NOT NULL" +
			  " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "')" +
			  " AND (food_status & " + Food.GIFT + ") <> 0"; 
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			totalGift = dbCon.rs.getFloat(1);
		}
		dbCon.rs.close();
		
		/**
		 * Calculate the price to all discount food within this shift
		 */
		float totalDiscount = 0;
		sql = "SELECT SUM(unit_price * order_count * (1-discount)) FROM " + Params.dbName + ".order_food WHERE order_id iN(" +
			  "SELECT id FROM " +Params.dbName + ".order WHERE restaurant_id=" + term.restaurant_id + 
			  " AND total_price IS NOT NULL" +
			  " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "')" +
			  " AND discount < 1.00";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			totalDiscount = dbCon.rs.getFloat(1);
		}
		dbCon.rs.close();
		
		totalGift = (float)Math.round(totalGift * 100) / 100;
		totalDiscount = (float)Math.round(totalDiscount * 100) / 100;
		
		Result result = new Result();
		result.onDuty = onDuty;
		result.offDuty = offDuty;
		result.orderAmount = orderAmount;
		result.totalActual = (float)Math.round(totalActual * 100) / 100;
		result.totalCash = (float)Math.round(totalCash * 100) / 100;
		result.totalCash2 = (float)Math.round(totalCash_2 * 100) / 100;
		result.totalCreditCard = (float)Math.round(totalCreditCard * 100) / 100;
		result.totalCreditCard2 = (float)Math.round(totalCreditCard_2 * 100) / 100;
		result.totalDiscount = (float)Math.round(totalDiscount * 100) / 100;
		result.totalGift = (float)Math.round(totalGift * 100) / 100;
		result.totalHang = (float)Math.round(totalHang * 100) / 100;
		result.totalHang2 = (float)Math.round(totalHang_2 * 100) / 100;
		result.totalMemberCard = (float)Math.round(totalMemberCard * 100) / 100;
		result.totalMemberCard2 = (float)Math.round(totalMemberCard_2 * 100) / 100;
		result.totalSign = (float)Math.round(totalSign * 100) / 100;
		result.totalSign2 = (float)Math.round(totalSign_2 * 100) / 100;
		return result;
	}
	
}
