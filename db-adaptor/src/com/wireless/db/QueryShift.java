package com.wireless.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;

public class QueryShift {
	
	static class DeptIncome{
		public float gift;				//某个部门的赠送额
		public float discount;			//某个部门的折扣额
		public float income;			//某个部门的营业额
	}
	
	public static class Result{
		public String onDuty;			//开始时间
		public String offDuty;			//结束时间
		public int orderAmount;			//账单数
		public float totalCash;			//现金金额
		public float totalCash2;		//现金实收
		public float totalCreditCard;	//刷卡金额
		public float totalCreditCard2;	//刷卡实收
		public float totalMemberCard;	//会员卡金额
		public float totalMemberCard2;	//会员卡实收
		public float totalSign;			//签单金额
		public float totalSign2;		//签单实收
		public float totalHang;			//挂账金额
		public float totalHang2;		//挂账实收
		public float totalActual;		//合计实收金额
		public int discountAmount;		//折扣账单数
		public float totalDiscount;		//合计折扣金额
		public int giftAmount;			//赠送账单数
		public float totalGift;			//合计赠送金额
		public int cancelAmount;		//退菜账单数
		public float totalCancel;		//合计退菜金额
		
		public DeptIncome[] deptIncome;	//所有部门营业额
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
		
		Result result = new Result();
		result.onDuty = onDuty;
		result.offDuty = offDuty;
		
		/**
		 * Get the amount the order within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
							"COUNT(*)",
							null, null,
							onDuty, offDuty, false);
		if(dbCon.rs.next()){
			result.orderAmount = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		String calcItem = "SUM(total_price), SUM(total_price_2)";
		/**
		 * Get the total cash income within this shirt
		 */		
		dbCon.rs = calcOrder(dbCon, term,
							 calcItem, 
							 "AND type=" + Order.MANNER_CASH,
							 null,
							 onDuty, offDuty, false); 
		
		if(dbCon.rs.next()){
			result.totalCash = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.totalCash2 = (float)Math.round(dbCon.rs.getFloat(2) * 100) / 100;
		}
		dbCon.rs.close();
		
		/**
		 * Get the total credit card income within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
							 calcItem,
							 "AND type=" + Order.MANNER_CREDIT_CARD,
							 null,
							 onDuty, offDuty, false);
		if(dbCon.rs.next()){
			result.totalCreditCard = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.totalCreditCard2 = (float)Math.round(dbCon.rs.getFloat(2) * 100) / 100;
		}
		dbCon.rs.close();
		
		/**
		 * Get the total member card income within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
						 	 calcItem,
						 	 "AND type=" + Order.MANNER_MEMBER,
						 	 null,
						 	 onDuty, offDuty, false);	
		if(dbCon.rs.next()){
			result.totalMemberCard = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.totalMemberCard2 = (float)Math.round(dbCon.rs.getFloat(2) * 100) / 100;
		}
		dbCon.rs.close();
		
		/**
		 * Get the total sign income within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
							 calcItem,
							 "AND type=" + Order.MANNER_SIGN,
							 null,
							 onDuty, offDuty, false);
		if(dbCon.rs.next()){
			result.totalSign = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.totalSign2 = (float)Math.round(dbCon.rs.getFloat(2) * 100) / 100;
		}
		dbCon.rs.close();
		
		/**
		 * Get the total hang income within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
							 calcItem,
							 "AND type=" + Order.MANNER_HANG,
							 null,
							 onDuty, offDuty, false);
		if(dbCon.rs.next()){
			result.totalHang = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.totalHang2 = (float)Math.round(dbCon.rs.getFloat(2) * 100) / 100;
		}
		dbCon.rs.close();
		
		float totalActual = result.totalCash2 + result.totalCreditCard2 + result.totalMemberCard2 + result.totalSign2 + result.totalHang2;			
		result.totalActual = (float)Math.round(totalActual * 100) / 100;

		/**
		 * Calculate the price to all cancelled food within this shift
		 */
		dbCon.rs = calcOrderFood(dbCon, term, 
								 "SUM(unit_price * order_count * discount + taste_price), COUNT(distinct order_id)",
								 "AND order_count < 0",
								 null,
								 onDuty, offDuty, false);
		if(dbCon.rs.next()){
			result.totalCancel = (float)Math.round(Math.abs(dbCon.rs.getFloat(1)) * 100) / 100;
			result.cancelAmount = dbCon.rs.getInt(2);
		}
		dbCon.rs.close();
		
		/**
		 * Calculate the price to all gifted food within this shift
		 */
		dbCon.rs = calcOrderFood(dbCon, term, 
								 "SUM(unit_price * order_count * discount + taste_price), COUNT(distinct order_id)",
								 "AND (food_status & " + Food.GIFT + ") <> 0",
								 null,
								 onDuty, offDuty, false);
		if(dbCon.rs.next()){
			result.totalGift = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.giftAmount = dbCon.rs.getInt(2);
		}
		dbCon.rs.close();
		
		/**
		 * Calculate the price to all discount food within this shift
		 */
		dbCon.rs = calcOrderFood(dbCon, term, 
				 				 "SUM(unit_price * order_count * (1 - discount)), COUNT(distinct order_id)",
				 				 "AND discount < 1.00",
				 				 null,
				 				 onDuty, offDuty, false);
		if(dbCon.rs.next()){
			result.totalDiscount = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.discountAmount = dbCon.rs.getInt(2);
		}
		dbCon.rs.close();
		
		ArrayList<DeptIncome> depts = new ArrayList<DeptIncome>();
		
		for(int i = 0; i < 10; i++){
			DeptIncome deptIncome = new DeptIncome();
			
			dbCon.rs = calcOrderFood(dbCon, term, 
									 "SUM(unit_price * order_count * discount + taste_price)",
									 "AND (food_status & " + Food.GIFT + ") <> 0 AND dept_id=" + i,
									 null,
									 onDuty, offDuty, false);
			if(dbCon.rs.next()){
				deptIncome.gift = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			}
			dbCon.rs.close();
			
			dbCon.rs = calcOrderFood(dbCon, term, 
					 				 "SUM(unit_price * order_count * (1 - discount))",
					 				 "AND discount < 1.00 AND dept_id=" + i,
					 				 null,
					 				 onDuty, offDuty, false);
			if(dbCon.rs.next()){
				deptIncome.discount = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			}
			dbCon.rs.close();
			
			dbCon.rs = calcOrderFood(dbCon, term,
									 "SUM(unit_price * order_count * discount + taste_price)",
									 "AND (food_status & " + Food.GIFT + ") = 0 AND dept_id=" + i,
									 null,
									 onDuty, offDuty, false);
			if(dbCon.rs.next()){
				deptIncome.income = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			}
			
			dbCon.rs.close();
			
			depts.add(deptIncome);
			
		}
		result.deptIncome = depts.toArray(new DeptIncome[depts.size()]);
		
		return result;
	}
		
	private static ResultSet calcOrder(DBCon dbCon, Terminal term,
								  String calcItem, String extraCond, String orderClause,
								  String onDuty, String offDuty, boolean isHistory) throws SQLException{
		
		String orderTbl = isHistory ? "order_history" : "order";
		
		String sql = "SELECT " + calcItem + " FROM " + Params.dbName + "." + orderTbl + " WHERE " +
					 "restaurant_id=" + term.restaurant_id + " AND " +
					 "total_price IS NOT NULL" + " " +
					 ((onDuty == null || offDuty == null) ? "" : "AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'") +
					 ((extraCond == null) ? "" : extraCond) + " " +
					 ((orderClause == null) ? "" : orderClause);
		
		return dbCon.stmt.executeQuery(sql);
	}
	
	private static ResultSet calcOrderFood(DBCon dbCon, Terminal term, 
								  String calcItem, String extraCond, String orderClause, 
								  String onDuty, String offDuty, boolean isHistory) throws SQLException{
		
		String orderTbl = isHistory ? "order_history" : "order";
		String orderFoodTbl = isHistory ? "order_food_history" : "order_food";
		
		String sql = "SELECT " + calcItem + " FROM " + Params.dbName + "." + orderFoodTbl + " WHERE order_id IN (" +
					 "SELECT id FROM " + Params.dbName + "." + orderTbl + " WHERE restaurant_id=" + term.restaurant_id +  
					 " AND " + "total_price IS NOT NULL" + " " +
					 ((onDuty == null || offDuty == null) ? "" : "AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'") + ") " +
					 ((extraCond == null) ? "" : extraCond) + " " +
					 ((orderClause == null) ? "" : orderClause);
		
		return dbCon.stmt.executeQuery(sql);		
	}
	
}
