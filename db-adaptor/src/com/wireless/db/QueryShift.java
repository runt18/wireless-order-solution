package com.wireless.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;

public class QueryShift {
	
	public static class DeptIncome{
		public Department dept;			//某个部门的信息
		public float gift;				//某个部门的赠送额
		public float discount;			//某个部门的折扣额
		public float income;			//某个部门的营业额
	}
	
	public static class Result{
		public String onDuty;			//开始时间
		public String offDuty;			//结束时间
		
		public int orderAmount;			//总账单数
		
		public int cashAmount;			//现金账单数
		public float cashIncome;		//现金金额
		public float cashIncome2;		//现金实收
		
		public int creditCardAmount;	//刷卡账单数
		public float creditCardIncome;	//刷卡金额
		public float creditCardIncome2;	//刷卡实收
		
		public int memeberCardAmount;	//会员卡账单数
		public float memberCardIncome;	//会员卡金额
		public float memberCardIncome2;	//会员卡实收
		
		public int signAmount;			//签单账单数
		public float signIncome;		//签单金额
		public float signIncome2;		//签单实收
		
		public int hangAmount;			//挂账账单数
		public float hangIncome;		//挂账金额
		public float hangIncome2;		//挂账实收
		
		public float totalActual;		//合计实收金额
		
		public int discountAmount;		//折扣账单数
		public float discountIncome;	//合计折扣金额
		
		public int giftAmount;			//赠送账单数
		public float giftIncome;		//合计赠送金额
		
		public int cancelAmount;		//退菜账单数
		public float cancelIncome;		//合计退菜金额
		
		public int serviceAmount;		//服务费账单数
		public float serviceIncome;		//服务费金额
		
		public int paidAmount;			//反结帐账单数
		public float paidIncome;		//反结帐金额
		
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
	public static Result execByNow(long pin, short model) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByNow(dbCon, pin, model);
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
	public static Result execByNow(DBCon dbCon, long pin, short model) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		
		/**
		 * Get the latest off duty date from both shift and shift_history,
		 * and make it as the on duty date to this duty shift
		 */
		String onDuty;
//		String sql = "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurant_id +
//					 " ORDER BY off_duty desc LIMIT 1";
		String sql = "SELECT MAX(off_duty) FROM (" +
					 "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurant_id + " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".shift_history WHERE restaurant_id=" + term.restaurant_id + ") AS all_off_duty";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			Timestamp offDuty = dbCon.rs.getTimestamp(1);
			if(offDuty == null){
				onDuty = "2011-07-30 00:00:00";
			}else{
				onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDuty);
			}
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
		
		return exec(dbCon, term, onDuty, offDuty, false);

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
	public static Result execLatest(long pin, short model) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execLatest(dbCon, pin, model);
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
	public static Result execLatest(DBCon dbCon, long pin, short model) throws BusinessException, SQLException{
		
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
		
		return exec(dbCon, term, onDuty, offDuty, false);
		
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
	 * @param isHistory
	 * 			indicate whether to check history record
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Result exec(long pin, short model, String onDuty, String offDuty, boolean isHistory) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, pin, model, onDuty, offDuty, isHistory);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Generate the details to shift within the on & off duty date.
	 * Note that database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to request
	 * @param onDuty
	 * 			the date to be on duty
	 * @param offDuty
	 * 			the date to be off duty
	 * @param isHistory
	 * 			indicate whether to check history record
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Result exec(DBCon dbCon, long pin, short model, String onDuty, String offDuty, boolean isHistory) throws SQLException, BusinessException{
		return exec(dbCon, VerifyPin.exec(dbCon, pin, model), onDuty, offDuty, isHistory);
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
	 * @param isHistory
	 * 			indicate whether to check history record
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Result exec(Terminal term, String onDuty, String offDuty, boolean isHistory) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, onDuty, offDuty, isHistory);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Generate the details to shift within the on & off duty date.
	 * Note that database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to request
	 * @param onDuty
	 * 			the date to be on duty
	 * @param offDuty
	 * 			the date to be off duty
	 * @param isHistory
	 * 			indicate whether to check history record
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Result exec(DBCon dbCon, Terminal term, String onDuty, String offDuty, boolean isHistory) throws SQLException{
		
		Result result = new Result();
		result.onDuty = onDuty;
		result.offDuty = offDuty;
		
		/**
		 * Get the order amount within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
							"COUNT(*)",
							null, null,
							onDuty, offDuty, isHistory);
		if(dbCon.rs.next()){
			result.orderAmount = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		String calcItem = "SUM(total_price * (1 + service_rate)), SUM(total_price_2), COUNT(*)";
		/**
		 * Get the total cash income within this shirt
		 */		
		dbCon.rs = calcOrder(dbCon, term,
							 calcItem, 
							 "AND type=" + Order.MANNER_CASH,
							 null,
							 onDuty, offDuty, isHistory); 
		
		if(dbCon.rs.next()){
			result.cashIncome = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.cashIncome2 = (float)Math.round(dbCon.rs.getFloat(2) * 100) / 100;
			result.cashAmount = dbCon.rs.getInt(3);
		}
		dbCon.rs.close();
		
		/**
		 * Get the total credit card income within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
							 calcItem,
							 "AND type=" + Order.MANNER_CREDIT_CARD,
							 null,
							 onDuty, offDuty, isHistory);
		if(dbCon.rs.next()){
			result.creditCardIncome = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.creditCardIncome2 = (float)Math.round(dbCon.rs.getFloat(2) * 100) / 100;
			result.creditCardAmount = dbCon.rs.getInt(3);
		}
		dbCon.rs.close();
		
		/**
		 * Get the total member card income within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
						 	 calcItem,
						 	 "AND type=" + Order.MANNER_MEMBER,
						 	 null,
						 	 onDuty, offDuty, isHistory);	
		if(dbCon.rs.next()){
			result.memberCardIncome = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.memberCardIncome2 = (float)Math.round(dbCon.rs.getFloat(2) * 100) / 100;
			result.memeberCardAmount = dbCon.rs.getInt(3);
		}
		dbCon.rs.close();
		
		/**
		 * Get the total sign income within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
							 calcItem,
							 "AND type=" + Order.MANNER_SIGN,
							 null,
							 onDuty, offDuty, isHistory);
		if(dbCon.rs.next()){
			result.signIncome = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.signIncome2 = (float)Math.round(dbCon.rs.getFloat(2) * 100) / 100;
			result.signAmount = dbCon.rs.getInt(3);
		}
		dbCon.rs.close();
		
		/**
		 * Get the total hang income within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
							 calcItem,
							 "AND type=" + Order.MANNER_HANG,
							 null,
							 onDuty, offDuty, isHistory);
		if(dbCon.rs.next()){
			result.hangIncome = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.hangIncome2 = (float)Math.round(dbCon.rs.getFloat(2) * 100) / 100;
			result.hangAmount = dbCon.rs.getInt(3);
		}
		dbCon.rs.close();
		
		float totalActual = result.cashIncome2 + result.creditCardIncome2 + result.memberCardIncome2 + result.signIncome2 + result.hangIncome2;			
		result.totalActual = (float)Math.round(totalActual * 100) / 100;

		/**
		 * Calculate the price to all cancelled food within this shift
		 */
		dbCon.rs = calcOrderFood(dbCon, term, 
								 "SUM(unit_price * order_count * discount + taste_price), COUNT(distinct order_id)",
								 "AND order_count < 0",
								 null,
								 onDuty, offDuty, isHistory);
		if(dbCon.rs.next()){
			result.cancelIncome = (float)Math.round(Math.abs(dbCon.rs.getFloat(1)) * 100) / 100;
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
								 onDuty, offDuty, isHistory);
		if(dbCon.rs.next()){
			result.giftIncome = (float)Math.round(Math.abs(dbCon.rs.getFloat(1)) * 100) / 100;
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
				 				 onDuty, offDuty, isHistory);
		if(dbCon.rs.next()){
			result.discountIncome = (float)Math.round(Math.abs(dbCon.rs.getFloat(1)) * 100) / 100;
			result.discountAmount = dbCon.rs.getInt(2);
		}
		dbCon.rs.close();
		
		/**
		 * Calculate the price to all paid income within this shift
		 */
		dbCon.rs = calcOrderFood(dbCon, term, 
				 				 "SUM(unit_price * order_count * discount + taste_price), COUNT(distinct order_id)",
				 				 "AND is_paid = 1",
				 				 null,
				 				 onDuty, offDuty, isHistory);
		if(dbCon.rs.next()){
			result.paidIncome = (float)Math.round(dbCon.rs.getFloat(1) * 100) / 100;
			result.paidAmount = dbCon.rs.getInt(2);
		}
		dbCon.rs.close();
		
		/**
		 * Calculate the price to all service income within this shift
		 */
		dbCon.rs = calcOrder(dbCon, term,
							 "SUM(total_price_2 / (1 + service_rate) * service_rate), COUNT(*)",
							 "AND service_rate > 0",
							 null,
							 onDuty, offDuty, isHistory);
		if(dbCon.rs.next()){
			result.serviceIncome = (float)Math.round(Math.abs(dbCon.rs.getFloat(1)) * 100) / 100;
			result.serviceAmount = dbCon.rs.getInt(2);
		}
		dbCon.rs.close();
		
		//Department[] depts = QueryMenu.queryDepartments(dbCon, term.restaurant_id, null, null);
		ArrayList<DeptIncome> deptIncomes = new ArrayList<DeptIncome>();
		/**
		 * Calculate the income to every department.
		 */
		for(Department dept : QueryMenu.queryDepartments(dbCon, term.restaurant_id, null, null)){

			DeptIncome deptIncome = new DeptIncome();
			
			deptIncome.dept = dept;
			
			/**
			 * Calculate the gift income to this department.
			 */
			dbCon.rs = calcOrderFood(dbCon, term, 
									 "SUM(unit_price * order_count * discount + taste_price)",
									 "AND (food_status & " + Food.GIFT + ") <> 0 AND dept_id=" + dept.deptID,
									 null,
									 onDuty, offDuty, isHistory);
			if(dbCon.rs.next()){
				deptIncome.gift = (float)Math.round(Math.abs(dbCon.rs.getFloat(1)) * 100) / 100;
			}
			dbCon.rs.close();
			
			/**
			 * Calculate the discount income to this department.
			 */
			dbCon.rs = calcOrderFood(dbCon, term, 
					 				 "SUM(unit_price * order_count * (1 - discount))",
					 				 "AND discount < 1.00 AND dept_id=" + dept.deptID,
					 				 null,
					 				 onDuty, offDuty, isHistory);
			if(dbCon.rs.next()){
				deptIncome.discount = (float)Math.round(Math.abs(dbCon.rs.getFloat(1)) * 100) / 100;
			}
			dbCon.rs.close();
			
			/**
			 * Calculate the income to this department.
			 */
			dbCon.rs = calcOrderFood(dbCon, term,
									 "SUM(unit_price * order_count * discount + taste_price)",
									 "AND (food_status & " + Food.GIFT + ") = 0 AND dept_id=" + dept.deptID,
									 null,
									 onDuty, offDuty, isHistory);
			if(dbCon.rs.next()){
				deptIncome.income = (float)Math.round(Math.abs(dbCon.rs.getFloat(1)) * 100) / 100;
			}
			
			dbCon.rs.close();
			
			if(deptIncome.discount != 0 || deptIncome.gift != 0 || deptIncome.income != 0){
				deptIncomes.add(deptIncome);			
			}
		}
		
		result.deptIncome = deptIncomes.toArray(new DeptIncome[deptIncomes.size()]);
		
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
