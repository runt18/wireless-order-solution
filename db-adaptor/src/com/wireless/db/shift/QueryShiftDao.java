package com.wireless.db.shift;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;

public class QueryShiftDao {
	
	public final static int QUERY_TODAY = 0;
	public final static int QUERY_HISTORY = 1;
	
	public static class DeptIncome{
		public DeptIncome(Department dept, float gift, float discount, float income){
			this.dept = dept;
			this.gift = gift;
			this.discount = discount;
			this.income = income;
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
		
		public int eraseAmount;			//抹数账单数
		public float eraseIncome;		//抹数金额
		
		public DeptIncome[] deptIncome;	//所有部门营业额
	}
	
	/**
	 * Perform to query the shift information through now to last daily settlement.
	 * @param dbCon
	 * 			The database connection
	 * @return The daily detail record
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result execDailybyNow(Terminal term) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execDailyByNow(dbCon, term);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to query the shift information through now to last daily settlement.
	 * Note that the database base should be connected before invoking this method.
	 * @param dbCon
	 * 			The database connection
	 * @param term
	 * 			The terminal
	 * @return The daily detail record
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result execDailyByNow(DBCon dbCon, Terminal term) throws BusinessException, SQLException{
		/**
		 * Get the latest off duty date from daily settle history 
		 * and make it as the on duty date to this daily shift
		 */
		String onDuty;
		String sql = " SELECT MAX(off_duty) FROM " +
					 Params.dbName + ".daily_settle_history " +
					 " WHERE " +
					 " restaurant_id=" + term.restaurantID;
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
	
	public static Result execDailySettleByNow(long pin) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		dbCon.connect();
		
		Terminal term = VerifyPin.exec(dbCon, pin, Terminal.MODEL_STAFF);
		
		String onDuty;
		String sql = "SELECT MAX(off_duty) FROM (" +
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
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attached with any restaurant.<br>
	 * 							 - The terminal is expired.
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
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attached with any restaurant.<br>
	 * 							 - The terminal is expired.
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
		
		String orderTbl = null;
		String orderFoodTbl = null;
		String tasteGroupTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = "order_history";
			orderFoodTbl = "order_food_history";
			tasteGroupTbl = "taste_group_history";
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = "order";		
			orderFoodTbl = "order_food";
			tasteGroupTbl = "taste_group";
		}
		
		//Get total to each pay type during this period.
		String sql;
		sql = " SELECT " +
			  " type, COUNT(*) AS amount, ROUND(SUM(total_price), 2) AS total, ROUND(SUM(total_price_2), 2) AS actual " +
			  " FROM " +
			  Params.dbName + "." + orderTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + term.restaurantID + 
			  " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
			  " GROUP BY " +
			  " type ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			int payType = dbCon.rs.getInt("type");
			int amount = dbCon.rs.getInt("amount");
			float total = dbCon.rs.getFloat("total");
			float actual = dbCon.rs.getFloat("actual");
			if(payType == Order.MANNER_CASH){
				result.cashAmount = amount;
				result.cashIncome = total;
				result.cashIncome2 = actual;
				
			}else if(payType == Order.MANNER_CREDIT_CARD){
				result.creditCardAmount = amount;
				result.creditCardIncome = total;
				result.creditCardIncome2 = actual;
				
			}else if(payType == Order.MANNER_MEMBER){
				result.memeberCardAmount = amount;
				result.memberCardIncome = total;
				result.memberCardIncome2 = actual;
				
			}else if(payType == Order.MANNER_HANG){
				result.hangAmount = amount;
				result.hangIncome = total;
				result.hangIncome2 = actual;
				
			}else if(payType == Order.MANNER_SIGN){
				result.signAmount = amount;
				result.signIncome = total;
				result.signIncome2 = actual;
			}			
		}
		dbCon.rs.close();
		
		result.orderAmount = result.cashAmount + result.creditCardAmount + result.memeberCardAmount + result.hangAmount + result.signAmount;
		
		//Get the total & amount to erase price
		sql = " SELECT " +
			  " COUNT(*) AS amount, ROUND(SUM(erase_price), 2) AS total_erase " +
			  " FROM " +
			  Params.dbName + "." + orderTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + term.restaurantID +
			  " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
			  " AND erase_price > 0 ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.eraseAmount = dbCon.rs.getInt("amount");
			result.eraseIncome = dbCon.rs.getFloat("total_erase");
		}
		dbCon.rs.close();
		
		//Get the total & amount to discount price
		sql = " SELECT " +
			  " COUNT(*) AS amount, ROUND(SUM(discount_price), 2) AS total_discount " +
			  " FROM " +
			  Params.dbName + "." + orderTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + term.restaurantID +
			  " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
			  " AND discount_price > 0 ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.discountAmount = dbCon.rs.getInt("amount");
			result.discountIncome = dbCon.rs.getFloat("total_discount");
		}
		dbCon.rs.close();
		
		//Get the total & amount to gift price
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(gift_price), 2) AS total_gift " +
		      " FROM " +
		      Params.dbName + "." + orderTbl +
		      " WHERE 1 = 1 " +
		      " AND restaurant_id = " + term.restaurantID +
		      " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
			  " AND gift_price > 0 ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.giftAmount = dbCon.rs.getInt("amount");
			result.giftIncome = dbCon.rs.getFloat("total_gift");
		}
		dbCon.rs.close();
		
		//Get the total & amount to cancel price
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(cancel_price), 2) AS total_cancel " +
		      " FROM " +
		      Params.dbName + "." + orderTbl +
		      " WHERE 1 = 1 " +
		      " AND restaurant_id = " + term.restaurantID +
		      " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
			  " AND cancel_price > 0 ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.cancelAmount = dbCon.rs.getInt("amount");
			result.cancelIncome = dbCon.rs.getFloat("total_cancel");
		}
		dbCon.rs.close();
		
		//Get the total & amount to repaid order
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(repaid_price), 2) AS total_repaid " +
		      " FROM " +
		      Params.dbName + "." + orderTbl +
		      " WHERE 1 = 1 " +
		      " AND restaurant_id = " + term.restaurantID +
		      " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
			  " AND repaid_price <> 0 ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.paidAmount = dbCon.rs.getInt("amount");
			result.paidIncome = dbCon.rs.getFloat("total_repaid");
		}
		dbCon.rs.close();

		//Get the total & amount to order with service
		sql = " SELECT " +
			  " COUNT(*) AS amount, ROUND(SUM(total_price * service_rate), 2) AS total_service " +
			  " FROM " +
			  Params.dbName + "." + orderTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + term.restaurantID +
			  " AND order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
			  " AND service_rate > 0 ";
				
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.serviceAmount = dbCon.rs.getInt("amount");
			result.serviceIncome = dbCon.rs.getFloat("total_service");
		}
		dbCon.rs.close();
		
		
		//Get the gift, discount & total to each department during this period.
		sql = " SELECT " +
			  " DEPT.dept_id, DEPT.restaurant_id, DEPT.type, " +
			  " MAX(DEPT.name) AS dept_name, " +
			  " ROUND(SUM(CASE WHEN ((OF.food_status & " + Food.GIFT + ") <> 0) THEN ((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * OF.order_count) ELSE 0 END), 2) AS dept_gift," +
			  " ROUND(SUM((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * (1 - discount) * OF.order_count), 2) AS dept_discount, " +
			  " ROUND(SUM(CASE WHEN ((OF.food_status & " + Food.GIFT + ") = 0) THEN ((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * OF.order_count) ELSE 0 END), 2) AS dept_income " +
			  " FROM " +
			  Params.dbName + "." + orderFoodTbl + " OF " + 
			  " JOIN " + Params.dbName + "." + orderTbl + " O " + " ON " + " OF.order_id = O.id " +
			  " JOIN " + Params.dbName + "." + tasteGroupTbl + " TG " + " ON " + " OF.taste_group_id = TG.taste_group_id " +
			  " JOIN " + Params.dbName + ".department DEPT " + " ON " + " OF.dept_id = DEPT.dept_id AND OF.restaurant_id = DEPT.restaurant_id " +
			  " WHERE 1 = 1 " +
			  " AND O.restaurant_id = " + term.restaurantID +
			  " AND O.order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'" +
			  " GROUP BY " + " OF.dept_id " +
			  " ORDER BY " + " OF.dept_id ASC ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<DeptIncome> deptIncomes = new ArrayList<DeptIncome>();
		while(dbCon.rs.next()){
			deptIncomes.add(new DeptIncome(new Department(dbCon.rs.getString("dept_name"),
														  dbCon.rs.getShort("dept_id"),
														  dbCon.rs.getInt("restaurant_id"),
														  dbCon.rs.getShort("type")),
										   dbCon.rs.getFloat("dept_gift"),
										   dbCon.rs.getFloat("dept_discount"),
										   dbCon.rs.getFloat("dept_income")));
		}
		dbCon.rs.close();
		
		if(deptIncomes.size() == 0){
			result.deptIncome = new DeptIncome[0];
		}else{
			result.deptIncome = deptIncomes.toArray(new DeptIncome[deptIncomes.size()]);
		}
		
//		SingleOrderFood[] orderFoods = new SingleOrderFood[0];
//		if(queryType == QUERY_HISTORY){
//			orderFoods = SingleOrderFoodReflector.getDetailHistory(dbCon, 
//							"AND B.restaurant_id=" + term.restaurantID + " " + 
//							"AND B.order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'", 
//							null);		
//			
//		}else if(queryType == QUERY_TODAY){
//			orderFoods = SingleOrderFoodReflector.getDetailToday(dbCon, 
//							"AND B.total_price IS NOT NULL " + 
//							"AND B.restaurant_id=" + term.restaurantID + " " + 
//							"AND B.order_date BETWEEN '" + onDuty + "' AND '" + offDuty + "'", 
//							null);
//		}
//		
//	
//		HashSet<Long> orderID = new HashSet<Long>();
//		HashSet<Long> cancelOrderID = new HashSet<Long>();
//		HashSet<Long> giftOrderID = new HashSet<Long>();
//		HashSet<Long> discountOrderID = new HashSet<Long>();
//		HashSet<Long> eraseOrderID = new HashSet<Long>(); 
//		HashSet<Long> paidOrderID = new HashSet<Long>();
//		HashSet<Long> serviceOrderID = new HashSet<Long>();
//		
//		HashMap<Long, Float> cashIncomeByOrder = new HashMap<Long, Float>();
//		HashMap<Long, Float> creditIncomeByOrder = new HashMap<Long, Float>();
//		HashMap<Long, Float> memberCardIncomeByOrder = new HashMap<Long, Float>();
//		HashMap<Long, Float> hangIncomeByOrder = new HashMap<Long, Float>();
//		HashMap<Long, Float> signIncomeByOrder = new HashMap<Long, Float>();
//		
//		HashMap<Department, DeptIncome> deptIncome = new HashMap<Department, DeptIncome>();
//		for(Department dept : QueryMenu.queryDepartments(dbCon, "AND restaurant_id=" + term.restaurantID, null)){
//			deptIncome.put(dept, new DeptIncome(dept, 0, 0, 0));
//		}
//		for(SingleOrderFood singleOrderFood : orderFoods){
//			
//			orderID.add(singleOrderFood.orderID);
//			
//			/**
//			 * Calculate the total cash income during this period
//			 */	
//			if(!singleOrderFood.food.isGift() && singleOrderFood.payManner == Order.MANNER_CASH){
//				result.cashIncome += singleOrderFood.calcPriceWithService();
//				if(cashIncomeByOrder.containsKey(singleOrderFood.orderID)){
//					cashIncomeByOrder.put(singleOrderFood.orderID, cashIncomeByOrder.get(singleOrderFood.orderID) + singleOrderFood.calcPriceWithService());
//				}else{
//					cashIncomeByOrder.put(singleOrderFood.orderID, singleOrderFood.calcPriceWithService());
//				}
//			}
//			
//			/**
//			 * Calculate the total credit card income during this period
//			 */	
//			if(!singleOrderFood.food.isGift() && singleOrderFood.payManner == Order.MANNER_CREDIT_CARD){
//				result.creditCardIncome += singleOrderFood.calcPriceWithService();
//				if(creditIncomeByOrder.containsKey(singleOrderFood.orderID)){
//					creditIncomeByOrder.put(singleOrderFood.orderID, creditIncomeByOrder.get(singleOrderFood.orderID) + singleOrderFood.calcPriceWithService());
//				}else{
//					creditIncomeByOrder.put(singleOrderFood.orderID, singleOrderFood.calcPriceWithService());
//				}
//			}
//			
//			/**
//			 * Calculate the total member card income during this period
//			 */	
//			if(!singleOrderFood.food.isGift() && singleOrderFood.payManner == Order.MANNER_MEMBER){
//				result.memberCardIncome += singleOrderFood.calcPriceWithService();
//				if(memberCardIncomeByOrder.containsKey(singleOrderFood.orderID)){
//					memberCardIncomeByOrder.put(singleOrderFood.orderID, memberCardIncomeByOrder.get(singleOrderFood.orderID) + singleOrderFood.calcPriceWithService());
//				}else{
//					memberCardIncomeByOrder.put(singleOrderFood.orderID, singleOrderFood.calcPriceWithService());
//				}
//			}
//			
//			/**
//			 * Calculate the total hang income during this period
//			 */	
//			if(!singleOrderFood.food.isGift() && singleOrderFood.payManner == Order.MANNER_HANG){
//				result.hangIncome += singleOrderFood.calcPriceWithService();
//				if(hangIncomeByOrder.containsKey(singleOrderFood.orderID)){
//					hangIncomeByOrder.put(singleOrderFood.orderID, hangIncomeByOrder.get(singleOrderFood.orderID) + singleOrderFood.calcPriceWithService());
//				}else{
//					hangIncomeByOrder.put(singleOrderFood.orderID, singleOrderFood.calcPriceWithService());
//				}
//			}
//			
//			/**
//			 * Calculate the total sign income during this period
//			 */	
//			if(!singleOrderFood.food.isGift() && singleOrderFood.payManner == Order.MANNER_SIGN){
//				result.signIncome += singleOrderFood.calcPriceWithService();
//				if(signIncomeByOrder.containsKey(singleOrderFood.orderID)){
//					signIncomeByOrder.put(singleOrderFood.orderID, signIncomeByOrder.get(singleOrderFood.orderID) + singleOrderFood.calcPriceWithService());
//				}else{
//					signIncomeByOrder.put(singleOrderFood.orderID, singleOrderFood.calcPriceWithService());
//				}
//			}
//			
			/**
			 * Calculate the gift, discount, income to each department during this period
			 */
//			DeptIncome income = deptIncome.get(singleOrderFood.kitchen.dept);
//			if(income != null){
//				if(singleOrderFood.food.isGift()){
//					income.gift += singleOrderFood.calcPriceWithTaste();
//				}else{
//					income.income += singleOrderFood.calcPriceWithTaste();
//				}
//				
//				if(singleOrderFood.discount < 1){
//					income.discount += singleOrderFood.calcDiscountPrice();
//				}
//				
//				deptIncome.put(singleOrderFood.kitchen.dept, income);
//			}
//			
//			/**
//			 * Calculate the price to all cancelled food during this period
//			 */
//			if(singleOrderFood.orderCount < 0){
//				result.cancelIncome += Math.abs(singleOrderFood.calcPriceWithTaste());
//				cancelOrderID.add(singleOrderFood.orderID);
//			}
//			
//			/**
//			 * Calculate the price to all gifted food during this period
//			 */
//			if(singleOrderFood.food.isGift()){
//				result.giftIncome += singleOrderFood.calcPriceWithTaste();
//				giftOrderID.add(singleOrderFood.orderID);
//			}
//			
//			/**
//			 * Calculate the price to all discount food during this period
//			 */
//			if(singleOrderFood.discount < 1){
//				result.discountIncome += singleOrderFood.calcDiscountPrice();
//				discountOrderID.add(singleOrderFood.orderID);
//			}
//			
//			/**
//			 * Calculate the price to order containing the erase price
//			 */
//			if(singleOrderFood.erasePrice > 0){
//				if(!eraseOrderID.contains(singleOrderFood.orderID)){
//					result.eraseIncome += singleOrderFood.erasePrice;
//					eraseOrderID.add(singleOrderFood.orderID);
//				}
//			}
//			
//			/**
//			 * Calculate the price to all paid income during this period
//			 */
//			if(singleOrderFood.isPaid){
//				result.paidIncome += singleOrderFood.calcPriceWithTaste();
//				paidOrderID.add(singleOrderFood.orderID);
//			}
//			
//			/**
//			 * Calculate the price to service income during this period
//			 */
//			if(singleOrderFood.serviceRate > 0){
//				result.serviceIncome += singleOrderFood.calcPriceWithTaste() * singleOrderFood.serviceRate;
//				serviceOrderID.add(singleOrderFood.orderID);
//			}
//		}
//		
//		/**
//		 * Assign the amount to all order
//		 */
//		result.orderAmount = orderID.size();
//		
//		//get the setting to this restaurant
//		Setting setting = QuerySetting.exec(dbCon, term.restaurantID);
//		
//		/**
//		 * Assign the total cash income and amount
//		 */
//		result.cashIncome = (float)Math.round(result.cashIncome * 100) / 100;
//		for(float cashByEachOrder : cashIncomeByOrder.values()){
//			result.cashIncome2 += Util.calcByTail(setting, cashByEachOrder);
//		}
//		result.cashAmount = cashIncomeByOrder.size();
//		
//		/**
//		 * Assign the total credit card income and amount
//		 */
//		result.creditCardIncome = (float)Math.round(result.creditCardIncome * 100) / 100;
//		for(float creditByEachOrder : creditIncomeByOrder.values()){
//			result.creditCardIncome2 += Util.calcByTail(setting, creditByEachOrder);
//		}
//		result.creditCardAmount = creditIncomeByOrder.size();
//		
//		/**
//		 * Assign the total member card income and amount
//		 */
//		result.memberCardIncome = (float)Math.round(result.memberCardIncome * 100) / 100;
//		for(float memberCardByEachOrder : memberCardIncomeByOrder.values()){
//			result.memberCardIncome2 += Util.calcByTail(setting, memberCardByEachOrder);			
//		}
//		result.memeberCardAmount = memberCardIncomeByOrder.size();
//		
//		/**
//		 * Assign the total hang income and amount
//		 */
//		result.hangIncome = (float)Math.round(result.hangIncome * 100) / 100;
//		for(float hangByEachOrder : hangIncomeByOrder.values()){
//			result.hangIncome2 += Util.calcByTail(setting, hangByEachOrder);
//		}
//		result.hangAmount = hangIncomeByOrder.size();		
//		
//		/**
//		 * Assign the total sign income and amount
//		 */
//		result.signIncome = (float)Math.round(result.signIncome * 100) / 100;
//		for(float signByEachOrder : signIncomeByOrder.values()){
//			result.signIncome2 += Util.calcByTail(setting, signByEachOrder);
//		}
//		result.signAmount = signIncomeByOrder.size();
//		
//		/**
//		 * Assign the total actual income
//		 */
//		result.totalActual = result.cashIncome2 + result.creditCardIncome2 + result.memberCardIncome2 + result.signIncome2 + result.hangIncome2;
//		
//		/**
//		 * Assign the cancel food income and amount
//		 */
//		result.cancelIncome = (float)Math.round(result.cancelIncome * 100) / 100;
//		result.cancelAmount = cancelOrderID.size();
//		
//		/**
//		 * Assign the erase price income and amount
//		 */
//		result.eraseIncome = (float)Math.round(result.eraseIncome * 100) / 100;
//		result.eraseAmount = eraseOrderID.size();
//		
//		/**
//		 * Assign the gift income and amount
//		 */
//		result.giftIncome = (float)Math.round(result.giftIncome * 100) / 100;
//		result.giftAmount = giftOrderID.size();
//		
//		/**
//		 * Assign the discount income and amount
//		 */
//		result.discountIncome = (float)Math.round(result.discountIncome * 100) / 100;
//		result.discountAmount = discountOrderID.size();
//		
//		/**
//		 * Assign the paid income and amount
//		 */
//		result.paidIncome = (float)Math.round(result.paidIncome * 100) / 100;
//		result.paidAmount = paidOrderID.size();
//		
//		/**
//		 * Assign the service income and amount
//		 */
//		result.serviceIncome = (float)Math.round(result.serviceIncome * 100) / 100;
//		result.serviceAmount = serviceOrderID.size();
		
		/**
		 * Assign all the department income
		 */
//		ArrayList<DeptIncome> validDeptIncomes = new ArrayList<DeptIncome>();
//		for(DeptIncome income : deptIncome.values()){
//			if(income.discount != 0 || income.gift != 0 || income.income != 0){
//				income.discount = (float)Math.round(income.discount * 100) / 100;
//				income.gift = (float)Math.round(income.gift * 100) / 100;
//				income.income = (float)Math.round(income.income * 100) / 100;
//				validDeptIncomes.add(income);
//			}
//		}
//		
//		/**
//		 * Sort the income in ascending order by department id. 
//		 */
//		Collections.sort(validDeptIncomes, new Comparator<DeptIncome>(){
//
//			@Override
//			public int compare(DeptIncome income1, DeptIncome income2) {
//				if(income1.dept.deptID == income2.dept.deptID){
//					return 0;
//				}else if(income1.dept.deptID < income2.dept.deptID){
//					return -1;
//				}else{
//					return 1;
//				}
//			}
//			
//		});
//		
//		result.deptIncome = validDeptIncomes.toArray(new DeptIncome[validDeptIncomes.size()]);		

		
		return result;
	}

}
