package com.wireless.db;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
		 * Get the latest off duty date from the tables below.
		 * 1 - shift 
		 * 2 - shift_history
		 * 3 - daily_settle_history
		 * and make it as the on duty date to this duty shift
		 */
		String onDuty;
		String sql = "SELECT MAX(off_duty) FROM (" +
					 "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurantID + " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".shift_history WHERE restaurant_id=" + term.restaurantID + " UNION " +
					 "SELECT off_duty FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id=" + term.restaurantID +
					 ") AS all_off_duty";
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
							"AND B.restaurant_id=" + term.restaurantID + " " + 
							"AND B.order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'", 
							null);		
			
		}else if(queryType == QUERY_TODAY){
			orderFoods = SingleOrderFoodReflector.getDetailToday(dbCon, 
							"AND B.total_price IS NOT NULL " + 
							"AND B.restaurant_id=" + term.restaurantID + " " + 
							"AND B.order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'", 
							null);
		}
		
	
		HashSet<Long> orderID = new HashSet<Long>();
		HashSet<Long> cancelOrderID = new HashSet<Long>();
		HashSet<Long> giftOrderID = new HashSet<Long>();
		HashSet<Long> discountOrderID = new HashSet<Long>();
		HashSet<Long> paidOrderID = new HashSet<Long>();
		HashSet<Long> serviceOrderID = new HashSet<Long>();
		
		HashMap<Long, Float> cashIncomeByOrder = new HashMap<Long, Float>();
		HashMap<Long, Float> creditIncomeByOrder = new HashMap<Long, Float>();
		HashMap<Long, Float> memberCardIncomeByOrder = new HashMap<Long, Float>();
		HashMap<Long, Float> hangIncomeByOrder = new HashMap<Long, Float>();
		HashMap<Long, Float> signIncomeByOrder = new HashMap<Long, Float>();
		
		HashMap<Department, DeptIncome> deptIncome = new HashMap<Department, DeptIncome>();
		for(Department dept : QueryMenu.queryDepartments(dbCon, "AND restaurant_id=" + term.restaurantID, null)){
			deptIncome.put(dept, new DeptIncome(dept));
		}
		for(SingleOrderFood singleOrderFood : orderFoods){
			
			orderID.add(singleOrderFood.orderID);
			
			/**
			 * Calculate the total cash income during this period
			 */	
			if(!singleOrderFood.food.isGift() && singleOrderFood.payManner == Order.MANNER_CASH){
				result.cashIncome += singleOrderFood.calcPriceWithService();
				if(cashIncomeByOrder.containsKey(singleOrderFood.orderID)){
					cashIncomeByOrder.put(singleOrderFood.orderID, cashIncomeByOrder.get(singleOrderFood.orderID) + singleOrderFood.calcPriceWithService());
				}else{
					cashIncomeByOrder.put(singleOrderFood.orderID, singleOrderFood.calcPriceWithService());
				}
			}
			
			/**
			 * Calculate the total credit card income during this period
			 */	
			if(!singleOrderFood.food.isGift() && singleOrderFood.payManner == Order.MANNER_CREDIT_CARD){
				result.creditCardIncome += singleOrderFood.calcPriceWithService();
				if(creditIncomeByOrder.containsKey(singleOrderFood.orderID)){
					creditIncomeByOrder.put(singleOrderFood.orderID, creditIncomeByOrder.get(singleOrderFood.orderID) + singleOrderFood.calcPriceWithService());
				}else{
					creditIncomeByOrder.put(singleOrderFood.orderID, singleOrderFood.calcPriceWithService());
				}
			}
			
			/**
			 * Calculate the total member card income during this period
			 */	
			if(!singleOrderFood.food.isGift() && singleOrderFood.payManner == Order.MANNER_MEMBER){
				result.memberCardIncome += singleOrderFood.calcPriceWithService();
				if(memberCardIncomeByOrder.containsKey(singleOrderFood.orderID)){
					memberCardIncomeByOrder.put(singleOrderFood.orderID, memberCardIncomeByOrder.get(singleOrderFood.orderID) + singleOrderFood.calcPriceWithService());
				}else{
					memberCardIncomeByOrder.put(singleOrderFood.orderID, singleOrderFood.calcPriceWithService());
				}
			}
			
			/**
			 * Calculate the total hang income during this period
			 */	
			if(!singleOrderFood.food.isGift() && singleOrderFood.payManner == Order.MANNER_HANG){
				result.hangIncome += singleOrderFood.calcPriceWithService();
				if(hangIncomeByOrder.containsKey(singleOrderFood.orderID)){
					hangIncomeByOrder.put(singleOrderFood.orderID, hangIncomeByOrder.get(singleOrderFood.orderID) + singleOrderFood.calcPriceWithService());
				}else{
					hangIncomeByOrder.put(singleOrderFood.orderID, singleOrderFood.calcPriceWithService());
				}
			}
			
			/**
			 * Calculate the total sign income during this period
			 */	
			if(!singleOrderFood.food.isGift() && singleOrderFood.payManner == Order.MANNER_SIGN){
				result.signIncome += singleOrderFood.calcPriceWithService();
				if(signIncomeByOrder.containsKey(singleOrderFood.orderID)){
					signIncomeByOrder.put(singleOrderFood.orderID, signIncomeByOrder.get(singleOrderFood.orderID) + singleOrderFood.calcPriceWithService());
				}else{
					signIncomeByOrder.put(singleOrderFood.orderID, singleOrderFood.calcPriceWithService());
				}
			}
			
			/**
			 * Calculate the gift, discount, income to each department during this period
			 */
			DeptIncome income = deptIncome.get(singleOrderFood.kitchen.dept);
			if(income != null){
				if(singleOrderFood.food.isGift()){
					income.gift += singleOrderFood.calcPriceWithTaste();
				}else{
					income.income += singleOrderFood.calcPriceWithTaste();
				}
				
				if(singleOrderFood.discount < 1){
					income.discount += singleOrderFood.calcDiscountPrice();
				}
				
				deptIncome.put(singleOrderFood.kitchen.dept, income);
			}
			
			/**
			 * Calculate the price to all cancelled food during this period
			 */
			if(singleOrderFood.orderCount < 0){
				result.cancelIncome += Math.abs(singleOrderFood.calcPriceWithTaste());
				cancelOrderID.add(singleOrderFood.orderID);
			}
			
			/**
			 * Calculate the price to all gifted food during this period
			 */
			if(singleOrderFood.food.isGift()){
				result.giftIncome += singleOrderFood.calcPriceWithTaste();
				giftOrderID.add(singleOrderFood.orderID);
			}
			
			/**
			 * Calculate the price to all discount food during this period
			 */
			if(singleOrderFood.discount < 1){
				result.discountIncome += singleOrderFood.calcDiscountPrice();
				discountOrderID.add(singleOrderFood.orderID);
			}
			
			/**
			 * Calculate the price to all paid income during this period
			 */
			if(singleOrderFood.isPaid){
				result.paidIncome += singleOrderFood.calcPriceWithTaste();
				paidOrderID.add(singleOrderFood.orderID);
			}
			
			/**
			 * Calculate the price to service income during this period
			 */
			if(singleOrderFood.serviceRate > 0){
				result.serviceIncome += singleOrderFood.calcPriceWithTaste() * singleOrderFood.serviceRate;
				serviceOrderID.add(singleOrderFood.orderID);
			}
		}
		
		/**
		 * Assign the amount to all order
		 */
		result.orderAmount = orderID.size();
		
		//get the setting to this restaurant
		Setting setting = QuerySetting.exec(dbCon, term.restaurantID);
		
		/**
		 * Assign the total cash income and amount
		 */
		result.cashIncome = (float)Math.round(result.cashIncome * 100) / 100;
		for(float cashByEachOrder : cashIncomeByOrder.values()){
			result.cashIncome2 += Util.calcByTail(setting.priceTail, cashByEachOrder);
		}
		result.cashIncome2 = (float)Math.round(result.cashIncome2 * 100) / 100;
		result.cashAmount = cashIncomeByOrder.size();
		
		/**
		 * Assign the total credit card income and amount
		 */
		result.creditCardIncome = (float)Math.round(result.creditCardIncome * 100) / 100;
		for(float creditByEachOrder : creditIncomeByOrder.values()){
			result.creditCardIncome2 += Util.calcByTail(setting.priceTail, creditByEachOrder);
		}
		result.creditCardIncome2 = (float)Math.round(result.creditCardIncome2 * 100) / 100;
		result.creditCardAmount = creditIncomeByOrder.size();
		
		/**
		 * Assign the total member card income and amount
		 */
		result.memberCardIncome = (float)Math.round(result.memberCardIncome * 100) / 100;
		for(float memberCardByEachOrder : memberCardIncomeByOrder.values()){
			result.memberCardIncome2 += Util.calcByTail(setting.priceTail, memberCardByEachOrder);			
		}
		result.memberCardIncome2 = (float)Math.round(result.memberCardIncome2 * 100) / 100;
		result.memeberCardAmount = memberCardIncomeByOrder.size();
		
		/**
		 * Assign the total hang income and amount
		 */
		result.hangIncome = (float)Math.round(result.hangIncome * 100) / 100;
		for(float hangByEachOrder : hangIncomeByOrder.values()){
			result.hangIncome2 += Util.calcByTail(setting.priceTail, hangByEachOrder);
		}
		result.hangIncome2 = (float)Math.round(result.hangIncome2 * 100) / 100;
		result.hangAmount = hangIncomeByOrder.size();		
		
		/**
		 * Assign the total sign income and amount
		 */
		result.signIncome = (float)Math.round(result.signIncome * 100) / 100;
		for(float signByEachOrder : signIncomeByOrder.values()){
			result.signIncome2 += Util.calcByTail(setting.priceTail, signByEachOrder);
		}
		result.signIncome2 = (float)Math.round(result.signIncome2 * 100) / 100;
		result.signAmount = signIncomeByOrder.size();
		
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
		
		/**
		 * Sort the income in ascending order by department id. 
		 */
		Collections.sort(validDeptIncomes, new Comparator<DeptIncome>(){

			@Override
			public int compare(DeptIncome income1, DeptIncome income2) {
				if(income1.dept.deptID == income2.dept.deptID){
					return 0;
				}else if(income1.dept.deptID < income2.dept.deptID){
					return -1;
				}else{
					return 1;
				}
			}
			
		});
		
		result.deptIncome = validDeptIncomes.toArray(new DeptIncome[validDeptIncomes.size()]);
		
		return result;
	}		

}
