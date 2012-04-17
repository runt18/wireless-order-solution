package com.wireless.db;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;

import com.wireless.dbObject.Setting;
import com.wireless.dbObject.SingleOrderFood;
import com.wireless.dbReflect.SingleOrderFoodReflector;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Department;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;

public class QueryShift {
	
	public final static int QUERY_TODAY = 0;
	public final static int QUERY_HISTORY = 1;
	
	public static class DeptIncome{
		public DeptIncome(Department dept){
			this.dept = dept;
		}
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
		
		return exec(dbCon, term, onDuty, offDuty, QUERY_TODAY);

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
//	public static Result execLatest(long pin, short model) throws BusinessException, SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return execLatest(dbCon, pin, model);
//		}finally{
//			dbCon.disconnect();
//		}
//		
//	}
	
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
//	public static Result execLatest(DBCon dbCon, long pin, short model) throws BusinessException, SQLException{
//		
//		Terminal term = VerifyPin.exec(dbCon, pin, model);
//		
//		/**
//		 * Get the latest on & off duty date
//		 */
//		String onDuty;
//		String offDuty;
//		String sql = "SELECT on_duty, off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurant_id +
//					 " ORDER BY off_duty desc LIMIT 1";
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		if(dbCon.rs.next()){
//			onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbCon.rs.getTimestamp("on_duty"));
//			offDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dbCon.rs.getTimestamp("off_duty"));
//		}else{
//			throw new BusinessException("No shift record to restaurant(id=" + term.restaurant_id + ") exist.");
//		}
//		dbCon.rs.close();
//		
//		return exec(dbCon, term, onDuty, offDuty, false);
//		
//	}
	
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
	 * @param queryType
	 * 			indicate which query type should use
	 * 			it is one of values below.
	 * 			- QUERY_TODAY
	 * 		    - QUERY_HISTORY
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Result exec(long pin, short model, String onDuty, String offDuty, int queryType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, pin, model, onDuty, offDuty, queryType);
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
	 * @param queryType
	 * 			indicate which query type should use
	 * 			it is one of values below.
	 * 			- QUERY_TODAY
	 * 		    - QUERY_HISTORY
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Result exec(DBCon dbCon, long pin, short model, String onDuty, String offDuty, int queryType) throws SQLException, BusinessException{
		return exec(dbCon, VerifyPin.exec(dbCon, pin, model), onDuty, offDuty, queryType);
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
	 * @param queryType
	 * 			indicate which query type should use
	 * 			it is one of values below.
	 * 			- QUERY_TODAY
	 * 		    - QUERY_HISTORY
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Result exec(Terminal term, String onDuty, String offDuty, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, onDuty, offDuty, queryType);
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
	 * @param queryType
	 * 			indicate which query type should use
	 * 			it is one of values below.
	 * 			- QUERY_TODAY
	 * 		    - QUERY_HISTORY
	 * @return the shift detail information
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Result exec(DBCon dbCon, Terminal term, String onDuty, String offDuty, int queryType) throws SQLException{
		
		Result result = new Result();
		result.onDuty = onDuty;
		result.offDuty = offDuty;
		
		SingleOrderFood[] orderFoods = new SingleOrderFood[0];
		if(queryType == QUERY_HISTORY){
			orderFoods = SingleOrderFoodReflector.getDetailHistory(dbCon, 
							"AND B.restaurant_id=" + term.restaurant_id + " " + 
							"AND B.order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'", 
							null);		
			
		}else if(queryType == QUERY_TODAY){
			orderFoods = SingleOrderFoodReflector.getDetailToday(dbCon, 
							"AND B.total_price IS NOT NULL " + 
							"AND B.restaurant_id=" + term.restaurant_id + " " + 
							"AND B.order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'", 
							null);
		}
		
	
		HashSet<Long> orderID = new HashSet<Long>();
		HashSet<Long> cashOrderID = new HashSet<Long>();
		HashSet<Long> creditOrderID = new HashSet<Long>();
		HashSet<Long> memberOrderID = new HashSet<Long>();
		HashSet<Long> hangOrderID = new HashSet<Long>();
		HashSet<Long> signOrderID = new HashSet<Long>();
		HashSet<Long> cancelOrderID = new HashSet<Long>();
		HashSet<Long> giftOrderID = new HashSet<Long>();
		HashSet<Long> discountOrderID = new HashSet<Long>();
		HashSet<Long> paidOrderID = new HashSet<Long>();
		HashSet<Long> serviceOrderID = new HashSet<Long>();
		
		HashMap<Short, DeptIncome> deptIncome = new HashMap<Short, DeptIncome>();
		for(Department dept : QueryMenu.queryDepartments(dbCon, term.restaurant_id, null, null)){
			deptIncome.put(dept.deptID, new DeptIncome(dept));
		}
		for(SingleOrderFood orderFood : orderFoods){
			
			orderID.add(orderFood.orderID);
			
			/**
			 * Calculate the total cash income during this period
			 */	
			if(!orderFood.food.isGift() && orderFood.payManner == Order.MANNER_CASH){
				result.cashIncome += orderFood.calcPriceWithService();
				cashOrderID.add(orderFood.orderID);
			}
			
			/**
			 * Calculate the total credit card income during this period
			 */	
			if(!orderFood.food.isGift() && orderFood.payManner == Order.MANNER_CREDIT_CARD){
				result.creditCardIncome += orderFood.calcPriceWithService();
				creditOrderID.add(orderFood.orderID);
			}
			
			/**
			 * Calculate the total member card income during this period
			 */	
			if(!orderFood.food.isGift() && orderFood.payManner == Order.MANNER_MEMBER){
				result.memberCardIncome += orderFood.calcPriceWithService();
				memberOrderID.add(orderFood.orderID);
			}
			
			/**
			 * Calculate the total hang income during this period
			 */	
			if(!orderFood.food.isGift() && orderFood.payManner == Order.MANNER_HANG){
				result.hangIncome += orderFood.calcPriceWithService();
				hangOrderID.add(orderFood.orderID);
			}
			
			/**
			 * Calculate the total sign income during this period
			 */	
			if(!orderFood.food.isGift() && orderFood.payManner == Order.MANNER_SIGN){
				result.signIncome += orderFood.calcPriceWithService();
				signOrderID.add(orderFood.orderID);
			}
			
			/**
			 * Calculate the gift, discount, income to each department during this period
			 */
			DeptIncome income = deptIncome.get(orderFood.kitchen.deptID);
			if(income != null){
				if(orderFood.food.isGift()){
					income.gift += orderFood.calcPriceWithTaste();
				}else{
					income.income += orderFood.calcPriceWithTaste();
				}
				
				if(orderFood.discount < 1){
					income.discount += orderFood.calcDiscountPrice();
				}
				
				deptIncome.put(orderFood.kitchen.deptID, income);
			}
			
			/**
			 * Calculate the price to all cancelled food during this period
			 */
			if(orderFood.orderCount < 0){
				result.cancelIncome += Math.abs(orderFood.calcPriceWithTaste());
				cancelOrderID.add(orderFood.orderID);
			}
			
			/**
			 * Calculate the price to all gifted food during this period
			 */
			if(orderFood.food.isGift()){
				result.giftIncome += orderFood.calcPriceWithTaste();
				giftOrderID.add(orderFood.orderID);
			}
			
			/**
			 * Calculate the price to all discount food during this period
			 */
			if(orderFood.discount < 1){
				result.discountIncome += orderFood.calcDiscountPrice();
				discountOrderID.add(orderFood.orderID);
			}
			
			/**
			 * Calculate the price to all paid income during this period
			 */
			if(orderFood.isPaid){
				result.paidIncome += orderFood.calcPriceWithTaste();
				paidOrderID.add(orderFood.orderID);
			}
			
			/**
			 * Calculate the price to service income during this period
			 */
			if(orderFood.serviceRate > 0){
				result.serviceIncome += orderFood.calcPriceWithTaste() * orderFood.serviceRate;
				serviceOrderID.add(orderFood.orderID);
			}
		}
		
		/**
		 * Assign the amount to all order
		 */
		result.orderAmount = orderID.size();
		
		//get the setting to this restaurant
		Setting setting = QuerySetting.exec(dbCon, term.restaurant_id);
		
		/**
		 * Assign the total cash income and amount
		 */
		result.cashIncome = (float)Math.round(result.cashIncome * 100) / 100;
		result.cashIncome2 = Util.calcByTail(setting.priceTail, result.cashIncome);
		result.cashAmount = cashOrderID.size();
		
		/**
		 * Assign the total credit card income and amount
		 */
		result.creditCardIncome = (float)Math.round(result.creditCardIncome * 100) / 100;
		result.creditCardIncome2 = Util.calcByTail(setting.priceTail, result.creditCardIncome);
		result.creditCardAmount = creditOrderID.size();
		
		/**
		 * Assign the total member card income and amount
		 */
		result.memberCardIncome = (float)Math.round(result.memberCardIncome * 100) / 100;
		result.memberCardIncome2 = Util.calcByTail(setting.priceTail, result.memberCardIncome);
		result.memeberCardAmount = memberOrderID.size();
		
		/**
		 * Assign the total hang income and amount
		 */
		result.hangIncome = (float)Math.round(result.hangIncome * 100) / 100;
		result.hangIncome2 = Util.calcByTail(setting.priceTail, result.hangIncome);
		result.hangAmount = hangOrderID.size();		
		
		/**
		 * Assign the total sign income and amount
		 */
		result.signIncome = (float)Math.round(result.signIncome * 100) / 100;
		result.signIncome2 = Util.calcByTail(setting.priceTail, result.signIncome);
		result.signAmount = signOrderID.size();
		
		/**
		 * Assign the total actual income
		 */
		result.totalActual = result.cashIncome2 + result.creditCardIncome2 + result.memberCardIncome2 + result.signIncome2 + result.hangIncome2;
		
		/**
		 * Assign the cancel food income and amount
		 */
		result.cancelIncome = (float)Math.round(result.cancelIncome * 100) / 100;
		result.cancelAmount = cancelOrderID.size();
		
		/**
		 * Assign the gift income and amount
		 */
		result.giftIncome = (float)Math.round(result.giftIncome * 100) / 100;
		result.giftAmount = giftOrderID.size();
		
		/**
		 * Assign the discount income and amount
		 */
		result.discountIncome = (float)Math.round(result.discountIncome * 100) / 100;
		result.discountAmount = discountOrderID.size();
		
		/**
		 * Assign the paid income and amount
		 */
		result.paidIncome = (float)Math.round(result.paidIncome * 100) / 100;
		result.paidAmount = paidOrderID.size();
		
		/**
		 * Assign the service income and amount
		 */
		result.serviceIncome = (float)Math.round(result.serviceIncome * 100) / 100;
		result.serviceAmount = serviceOrderID.size();
		
		/**
		 * Assign all the department income
		 */
		ArrayList<DeptIncome> validDeptIncomes = new ArrayList<DeptIncome>();
		for(DeptIncome income : deptIncome.values()){
			if(income.discount != 0 || income.gift != 0 || income.income != 0){
				income.discount = (float)Math.round(income.discount * 100) / 100;
				income.gift = (float)Math.round(income.gift * 100) / 100;
				income.income = (float)Math.round(income.income * 100) / 100;
				validDeptIncomes.add(income);
			}
		}
		result.deptIncome = validDeptIncomes.toArray(new DeptIncome[validDeptIncomes.size()]);
		
		return result;
	}		

}
