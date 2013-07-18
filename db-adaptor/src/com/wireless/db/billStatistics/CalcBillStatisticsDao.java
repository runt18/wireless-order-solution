package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.billStatistics.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.CancelIncomeByDept.IncomeByEachReason;
import com.wireless.pojo.billStatistics.CancelIncomeByDeptAndReason;
import com.wireless.pojo.billStatistics.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.CancelIncomeByReason.IncomeByEachDept;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByCancel;
import com.wireless.pojo.billStatistics.IncomeByCharge;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByDiscount;
import com.wireless.pojo.billStatistics.IncomeByErase;
import com.wireless.pojo.billStatistics.IncomeByFood;
import com.wireless.pojo.billStatistics.IncomeByGift;
import com.wireless.pojo.billStatistics.IncomeByKitchen;
import com.wireless.pojo.billStatistics.IncomeByPay;
import com.wireless.pojo.billStatistics.IncomeByRepaid;
import com.wireless.pojo.billStatistics.IncomeByService;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.protocol.Terminal;

public class CalcBillStatisticsDao {

	private final static String TBL_ORDER_TODAY = "order";
	private final static String TBL_ORDER_FOOD_TODAY = "order_food";
	private final static String TBL_ORDER_GROUP_TODAY = "order_group";
	private final static String TBL_TASTE_GROUP_TODAY = "taste_group";
	private final static String TBL_MEMBER_OPERATION = "member_operation";
	private final static String TBL_ORDER_HISTORY = "order_history";
	private final static String TBL_ORDER_FOOD_HISTORY = "order_food_history";
	private final static String TBL_ORDER_GROUP_HISTORY = "order_group_history";
	private final static String TBL_TASTE_GROUP_HISTORY = "taste_group_history";
	private final static String TBL_MEMBER_OPERATION_HISTORY = "member_operation_history";
	
	public final static int QUERY_TODAY = 0;
	public final static int QUERY_HISTORY = 1;
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByPay calcIncomeByPayType(Terminal term, DutyRange range, int queryType) throws SQLException{	
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByPayType(dbCon, term, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByPay calcIncomeByPayType(DBCon dbCon, Terminal term, DutyRange range, int queryType) throws SQLException{		
		
		String orderTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		IncomeByPay incomeByPay = new IncomeByPay();
		
		//Get amount of paid order to each pay type during this period.
		String sql;
		sql = " SELECT " +
			  " pay_type, COUNT(*) AS amount, ROUND(SUM(total_price), 2) AS total, ROUND(SUM(actual_price), 2) AS actual " +
			  " FROM " +
			  Params.dbName + "." + orderTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + term.restaurantID + 
			  " AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND (status = " + Order.Status.PAID.getVal() + " OR " + " status = " + Order.Status.REPAID.getVal() + ")"  +
			  " GROUP BY " +
			  " pay_type ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			int payType = dbCon.rs.getInt("pay_type");
			int amount = dbCon.rs.getInt("amount");
			float total = dbCon.rs.getFloat("total");
			float actual = dbCon.rs.getFloat("actual");
			if(payType == Order.PayType.CASH.getVal()){
				incomeByPay.setCashAmount(amount);
				incomeByPay.setCashIncome(total);
				incomeByPay.setCashActual(actual);
				
			}else if(payType == Order.PayType.CREDIT_CARD.getVal()){
				incomeByPay.setCreditCardAmount(amount);
				incomeByPay.setCreditCardIncome(total);
				incomeByPay.setCreditCardActual(actual);
				
			}else if(payType == Order.PayType.MEMBER.getVal()){
				incomeByPay.setMemeberCardAmount(amount);
				incomeByPay.setMemberCardIncome(total);
				incomeByPay.setMemberCardActual(actual);
				
			}else if(payType == Order.PayType.HANG.getVal()){
				incomeByPay.setHangAmount(amount);
				incomeByPay.setHangIncome(total);
				incomeByPay.setHangActual(actual);
				
			}else if(payType == Order.PayType.SIGN.getVal()){
				incomeByPay.setSignAmount(amount);
				incomeByPay.setSignIncome(total);
				incomeByPay.setSignActual(actual);
			}			
		}
		dbCon.rs.close();
		
		incomeByPay.setOrderAmount(incomeByPay.getCashAmount() + 
								   incomeByPay.getCreditCardAmount() + 
								   incomeByPay.getMemeberCardAmount() + 
								   incomeByPay.getHangAmount() +
								   incomeByPay.getSignAmount());
		
		incomeByPay.setTotalIncome(incomeByPay.getTotalIncome() + 
								   incomeByPay.getCreditCardIncome() + 
								   incomeByPay.getMemberCardIncome() + 
								   incomeByPay.getSignIncome() + 
								   incomeByPay.getHangIncome());
		
		incomeByPay.setTotalActual(incomeByPay.getCashActual() + 
								   incomeByPay.getCreditCardActual() + 
								   incomeByPay.getMemberCardActual() + 
								   incomeByPay.getSignActual() + 
								   incomeByPay.getHangActual());
		
		return incomeByPay;
	}
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByErase calcErasePrice(Terminal term, DutyRange range, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcErasePrice(dbCon, term, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByErase calcErasePrice(DBCon dbCon, Terminal term, DutyRange range, int queryType) throws SQLException{
		
		String orderTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		//Get the total & amount to erase price
		sql = " SELECT " +
			  " COUNT(*) AS amount, ROUND(SUM(erase_price), 2) AS total_erase " +
			  " FROM " +
			  Params.dbName + "." + orderTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + term.restaurantID +
			  " AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND erase_price > 0 ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByErase eraseIncome = new IncomeByErase();
		if(dbCon.rs.next()){
			eraseIncome.setEraseAmount(dbCon.rs.getInt("amount"));
			eraseIncome.setErasePrice(dbCon.rs.getFloat("total_erase"));
		}
		dbCon.rs.close();
		
		return eraseIncome;
	}
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByDiscount calcDiscountPrice(Terminal term, DutyRange range, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcDiscountPrice(dbCon, term, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByDiscount calcDiscountPrice(DBCon dbCon, Terminal term, DutyRange range, int queryType) throws SQLException{
		
		String orderTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		sql = " SELECT " +
			  " COUNT(*) AS amount, ROUND(SUM(discount_price), 2) AS total_discount " +
			  " FROM " +
			  Params.dbName + "." + orderTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + term.restaurantID +
			  " AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND discount_price > 0 ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByDiscount discountIncome = new IncomeByDiscount();
		if(dbCon.rs.next()){
			discountIncome.setDiscountAmount(dbCon.rs.getInt("amount"));
			discountIncome.setTotalDiscount(dbCon.rs.getFloat("total_discount"));
		}
		dbCon.rs.close();
		
		return discountIncome;
	}
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByGift calcGiftPrice(Terminal term, DutyRange range, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcGiftPrice(dbCon, term, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByGift calcGiftPrice(DBCon dbCon, Terminal term, DutyRange range, int queryType) throws SQLException{
		String orderTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(gift_price), 2) AS total_gift " +
		      " FROM " +
		      Params.dbName + "." + orderTbl +
		      " WHERE 1 = 1 " +
		      " AND restaurant_id = " + term.restaurantID +
		      " AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND gift_price > 0 ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByGift giftIncome = new IncomeByGift();
		if(dbCon.rs.next()){
			giftIncome.setGiftAmount(dbCon.rs.getInt("amount"));
			giftIncome.setTotalGift(dbCon.rs.getFloat("total_gift"));
		}
		dbCon.rs.close();
		
		return giftIncome;
	}
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByCancel calcCancelPrice(Terminal term, DutyRange range, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelPrice(dbCon, term, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByCancel calcCancelPrice(DBCon dbCon, Terminal term, DutyRange range, int queryType) throws SQLException{
		
		String orderTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(cancel_price), 2) AS total_cancel " +
		      " FROM " +
		      Params.dbName + "." + orderTbl +
		      " WHERE 1 = 1 " +
		      " AND restaurant_id = " + term.restaurantID +
		      " AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND cancel_price > 0 ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByCancel cancelIncome = new IncomeByCancel();
		if(dbCon.rs.next()){
			cancelIncome.setCancelAmount(dbCon.rs.getInt("amount"));
			cancelIncome.setTotalCancel(dbCon.rs.getFloat("total_cancel"));
		}
		dbCon.rs.close();
		
		return cancelIncome;
	}

	/**
	 * 
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByRepaid calcRepaidPrice(Terminal term, DutyRange range, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcRepaidPrice(dbCon, term, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByRepaid calcRepaidPrice(DBCon dbCon, Terminal term, DutyRange range, int queryType) throws SQLException{
		String orderTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(repaid_price), 2) AS total_repaid " +
		      " FROM " +
		      Params.dbName + "." + orderTbl +
		      " WHERE 1 = 1 " +
		      " AND restaurant_id = " + term.restaurantID +
		      " AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND status = " + Order.Status.REPAID.getVal();
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByRepaid repaidIncome = new IncomeByRepaid();
		if(dbCon.rs.next()){
			repaidIncome.setRepaidAmount(dbCon.rs.getInt("amount"));
			repaidIncome.setTotalRepaid(dbCon.rs.getFloat("total_repaid"));
		}
		dbCon.rs.close();
		
		return repaidIncome;
	}
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByService calcServicePrice(Terminal term, DutyRange range, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcServicePrice(dbCon, term, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByService calcServicePrice(DBCon dbCon, Terminal term, DutyRange range, int queryType) throws SQLException{
		String orderTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		sql = " SELECT " +
			  " COUNT(*) AS amount, ROUND(SUM(total_price * service_rate), 2) AS total_service " +
			  " FROM " +
			  Params.dbName + "." + orderTbl +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + term.restaurantID +
			  " AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND service_rate > 0 ";
				
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByService serviceIncome = new IncomeByService();
		if(dbCon.rs.next()){
			serviceIncome.setServiceAmount(dbCon.rs.getInt("amount"));
			serviceIncome.setTotalService(dbCon.rs.getFloat("total_service"));
		}
		dbCon.rs.close();
		
		return serviceIncome;
	}
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<IncomeByDept> calcIncomeByDept(Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByDept(dbCon, term, range, extraCond, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<IncomeByDept> calcIncomeByDept(DBCon dbCon, Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		String orderTbl = null;
		String orderFoodTbl = null;
		String orderGrpTbl = null;
		String tasteGrpTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = TBL_ORDER_HISTORY;
			orderFoodTbl = TBL_ORDER_FOOD_HISTORY;
			orderGrpTbl = TBL_ORDER_GROUP_HISTORY;
			tasteGrpTbl = TBL_TASTE_GROUP_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = TBL_ORDER_TODAY;
			orderFoodTbl = TBL_ORDER_FOOD_TODAY;
			orderGrpTbl = TBL_ORDER_GROUP_TODAY;
			tasteGrpTbl = TBL_TASTE_GROUP_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		//Get the gift, discount & total to each department during this period.
		sql = " SELECT " +
			  " dept_id, restaurant_id, dept_type, dept_name, " +
			  " ROUND(SUM(dept_gift), 2) AS dept_gift, " +
			  " ROUND(SUM(dept_discount), 2) AS dept_discount, " +
			  " ROUND(SUM(dept_income), 2) AS dept_income " +
			  " FROM (" +
				  " SELECT " +
				  " MAX(DEPT.dept_id) AS dept_id, MAX(DEPT.restaurant_id) AS restaurant_id, MAX(DEPT.type) AS dept_type, " +
				  " MAX(DEPT.name) AS dept_name, " +
				  " CASE WHEN ((OF.food_status & " + Food.GIFT + ") <> 0) THEN (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) ELSE 0 END AS dept_gift," +
				  " (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * (1 - discount) * SUM(OF.order_count) AS dept_discount, " +
				  " CASE WHEN ((OF.food_status & " + Food.GIFT + ") = 0 AND (OF.food_status & " + Food.WEIGHT + ") = 0) THEN (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) " +
				  	   " WHEN ((OF.food_status & " + Food.GIFT + ") = 0 AND (OF.food_status & " + Food.WEIGHT + ") <> 0) THEN (OF.unit_price * SUM(OF.order_count) + (IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0))) * discount " +
				  	   " ELSE 0 " +
				  	   " END AS dept_income " +
				  " FROM " +
				  Params.dbName + "." + orderFoodTbl + " OF " + 
				  " JOIN " + "(" + " SELECT id, order_date FROM " + Params.dbName + "." + orderTbl + 
				  			 	   " WHERE 1 = 1 " +
				  			 	   " AND " + " restaurant_id = " + term.restaurantID + 
				  			 	   " AND " + " status <> " + Order.Status.UNPAID.getVal() +
				  			 	   " AND " + " category <> " + Order.Category.MERGER_TBL.getVal() +
				  			 	   " UNION " +
				  			 	   " SELECT OG.sub_order_id AS id, O.order_date " +
				  			 	   " FROM " + Params.dbName + "." + orderGrpTbl + " OG " +
				  			 	   " JOIN " + Params.dbName + "." + orderTbl + " O " + " ON OG.order_id = O.id " +
				  			 	   " WHERE 1 = 1 " +
				  			 	   " AND " + " O.restaurant_id = " + term.restaurantID +
				  			 	   " AND " + " O.status <> " + Order.Status.UNPAID.getVal() + 
				  			 	   " AND " + " O.category = " + Order.Category.MERGER_TBL.getVal() +
				  			 ") AS O " + " ON OF.order_id = O.id " +
				  " JOIN " + Params.dbName + "." + tasteGrpTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
				  " JOIN " + Params.dbName + ".department DEPT " + " ON OF.dept_id = DEPT.dept_id AND OF.restaurant_id = DEPT.restaurant_id " +
				  " WHERE 1 = 1 " +
				  (extraCond == null ? "" : extraCond) +
				  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
				  " GROUP BY " + " OF.order_id, OF.food_alias, OF.taste_group_id " +
				  " HAVING SUM(order_count) > 0 " +
				  " ) AS TMP " +
			  " GROUP BY dept_id " +
			  " ORDER BY dept_id ASC ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<IncomeByDept> deptIncomes = new ArrayList<IncomeByDept>();
		while(dbCon.rs.next()){
			deptIncomes.add(new IncomeByDept(new Department(dbCon.rs.getString("dept_name"),
														    dbCon.rs.getShort("dept_id"),
														    dbCon.rs.getInt("restaurant_id"),
														    Department.Type.valueOf(dbCon.rs.getShort("dept_type"))),
										     dbCon.rs.getFloat("dept_gift"),
										     dbCon.rs.getFloat("dept_discount"),
										     dbCon.rs.getFloat("dept_income")));
		}
		dbCon.rs.close();
	
		return deptIncomes;
	}
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<IncomeByKitchen> calcIncomeByKitchen(Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByKitchen(dbCon, term, range, extraCond, queryType);
		}finally{
			dbCon.disconnect();
		}
	}

	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<IncomeByKitchen> calcIncomeByKitchen(DBCon dbCon, Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		String orderTbl = null;
		String orderFoodTbl = null;
		String orderGrpTbl = null;
		String tasteGrpTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = TBL_ORDER_HISTORY;
			orderFoodTbl = TBL_ORDER_FOOD_HISTORY;
			orderGrpTbl = TBL_ORDER_GROUP_HISTORY;
			tasteGrpTbl = TBL_TASTE_GROUP_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = TBL_ORDER_TODAY;
			orderFoodTbl = TBL_ORDER_FOOD_TODAY;
			orderGrpTbl = TBL_ORDER_GROUP_TODAY;
			tasteGrpTbl = TBL_TASTE_GROUP_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		//Get the gift, discount & total to each kitchen during this period.
		sql = " SELECT " +
			  " kitchen_id, kitchen_alias, kitchen_name, kitchen_type, " +
			  " dept_id, dept_type, dept_name, restaurant_id, " +
			  " ROUND(SUM(kitchen_gift), 2) AS kitchen_gift, ROUND(SUM(kitchen_discount), 2) AS kitchen_discount, ROUND(SUM(kitchen_income), 2) AS kitchen_income " +
			  " FROM ( " + 
				  " SELECT " +
				  " MAX(KITCHEN.kitchen_id) AS kitchen_id, MAX(KITCHEN.kitchen_alias) AS kitchen_alias, " +
				  " MAX(KITCHEN.name) AS kitchen_name, MAX(KITCHEN.type) AS kitchen_type, " +
				  " MAX(DEPT.dept_id) AS dept_id, MAX(DEPT.type) AS dept_type, MAX(DEPT.name) AS dept_name, " +
				  " MAX(OF.restaurant_id) AS restaurant_id, " +
				  " CASE WHEN ((OF.food_status & " + Food.GIFT + ") <> 0) THEN (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) ELSE 0 END AS kitchen_gift," +
				  " (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * (1 - discount) * SUM(OF.order_count) AS kitchen_discount, " +
				  " CASE WHEN ((OF.food_status & " + Food.GIFT + ") = 0 AND (OF.food_status & " + Food.WEIGHT + ") = 0) THEN (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) " +
			  	   	   " WHEN ((OF.food_status & " + Food.GIFT + ") = 0 AND (OF.food_status & " + Food.WEIGHT + ") <> 0) THEN (OF.unit_price * SUM(OF.order_count) + (IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0))) * discount " +
			  	   	   " ELSE 0 " +
			  	   	   " END AS kitchen_income " +
			  	  " FROM " +
				  Params.dbName + "." + orderFoodTbl + " OF " + 
				  " JOIN " + "(" + " SELECT id, order_date FROM " + Params.dbName + "." + orderTbl + 
				  			 	   " WHERE 1 = 1 " +
				  			 	   " AND " + " restaurant_id = " + term.restaurantID + 
				  			 	   " AND " + " status <> " + Order.Status.UNPAID.getVal() +
				  			 	   " AND " + " category <> " + Order.Category.MERGER_TBL.getVal() +
				  			 	   " UNION " +
				  			 	   " SELECT OG.sub_order_id AS id, O.order_date " +
				  			 	   " FROM " + Params.dbName + "." + orderGrpTbl + " OG " +
				  			 	   " JOIN " + Params.dbName + "." + orderTbl + " O " + " ON OG.order_id = O.id " +
				  			 	   " WHERE 1 = 1 " +
				  			 	   " AND " + " O.restaurant_id = " + term.restaurantID +
				  			 	   " AND " + " O.status <> " + Order.Status.UNPAID.getVal() + 
				  			 	   " AND " + " O.category = " + Order.Category.MERGER_TBL.getVal() +
				  			 ") AS O " + " ON OF.order_id = O.id " +
				  " JOIN " + Params.dbName + "." + tasteGrpTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
				  " JOIN " + Params.dbName + ".kitchen KITCHEN " + " ON OF.kitchen_id = KITCHEN.kitchen_id " + 
				  " JOIN " + Params.dbName + ".department DEPT " + " ON KITCHEN.dept_id = DEPT.dept_id AND KITCHEN.restaurant_id = DEPT.restaurant_id " +
				  " WHERE 1 = 1 " +
				  (extraCond == null ? "" : extraCond) +
				  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
				  " GROUP BY OF.order_id, OF.food_alias, OF.taste_group_id ) AS TMP " +
			  " GROUP BY kitchen_id " +
			  " ORDER BY kitchen_id ASC ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<IncomeByKitchen> kitchenIncomes = new ArrayList<IncomeByKitchen>();
		while(dbCon.rs.next()){
			Kitchen k = new Kitchen.Builder(dbCon.rs.getShort("kitchen_alias"), dbCon.rs.getString("kitchen_name"), dbCon.rs.getInt("restaurant_id"))
							.setKitchenId(dbCon.rs.getLong("kitchen_id"))
							.setAllowTemp(false)
							.setType(dbCon.rs.getShort("kitchen_type"))
							.setDept(new Department(dbCon.rs.getString("dept_name"),
											  	   dbCon.rs.getShort("dept_id"),
											  	   dbCon.rs.getInt("restaurant_id"),
											  	   Department.Type.valueOf(dbCon.rs.getShort("dept_type")))).build();
			
			kitchenIncomes.add(new IncomeByKitchen(k, 
												   dbCon.rs.getFloat("kitchen_gift"),
												   dbCon.rs.getFloat("kitchen_discount"),
												   dbCon.rs.getFloat("kitchen_income")));
		}
		
		dbCon.rs.close();
		
		return kitchenIncomes;
	}
	
	public static List<IncomeByFood> calcIncomeByFood(Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByFood(dbCon, term, range, extraCond, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<IncomeByFood> calcIncomeByFood(DBCon dbCon, Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		String orderTbl = null;
		String orderFoodTbl = null;
		String orderGrpTbl = null;
		String tasteGrpTbl = null;
		if(queryType == QUERY_HISTORY){
			orderTbl = TBL_ORDER_HISTORY;
			orderFoodTbl = TBL_ORDER_FOOD_HISTORY;
			orderGrpTbl = TBL_ORDER_GROUP_HISTORY;
			tasteGrpTbl = TBL_TASTE_GROUP_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderTbl = TBL_ORDER_TODAY;
			orderFoodTbl = TBL_ORDER_FOOD_TODAY;
			orderGrpTbl = TBL_ORDER_GROUP_TODAY;
			tasteGrpTbl = TBL_TASTE_GROUP_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		//Get the gift, discount & total to each food during this period.
		sql = " SELECT " +
			  " OF.food_id, MAX(OF.food_alias) AS food_alias, MAX(OF.name) AS food_name, " +
			  " MAX(OF.food_status) AS food_status, MAX(OF.restaurant_id) AS restaurant_id, " +
			  " MAX(OF.kitchen_id) AS kitchen_id, MAX(OF.kitchen_alias) AS kitchen_alias, " +
			  " MAX(OF.dept_id) AS dept_id, " +
			  " SUM(OF.order_count) AS sale_amount, " +
			  " ROUND(SUM(CASE WHEN ((OF.food_status & " + Food.GIFT + ") <> 0) THEN ((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * OF.order_count) ELSE 0 END), 2) AS food_gift," +
			  " ROUND(SUM((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * (1 - discount) * OF.order_count), 2) AS food_discount, " +
			  " ROUND(SUM(CASE WHEN ((OF.food_status & " + Food.GIFT + ") = 0) THEN ((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * OF.order_count) ELSE 0 END), 2) AS food_income " +
			  " FROM " +
			  Params.dbName + "." + orderFoodTbl + " OF " + 
			  " JOIN " + "(" + " SELECT id, order_date FROM " + Params.dbName + "." + orderTbl + 
			  			 	   " WHERE 1 = 1 " +
			  			 	   " AND " + " restaurant_id = " + term.restaurantID + 
			  			 	   " AND " + " status <> " + Order.Status.UNPAID.getVal() +
			  			 	   " AND " + " category <> " + Order.Category.MERGER_TBL.getVal() +
			  			 	   " UNION " +
			  			 	   " SELECT OG.sub_order_id AS id, O.order_date " +
			  			 	   " FROM " + Params.dbName + "." + orderGrpTbl + " OG " +
			  			 	   " JOIN " + Params.dbName + "." + orderTbl + " O " + " ON OG.order_id = O.id " +
			  			 	   " WHERE 1 = 1 " +
			  			 	   " AND " + " O.restaurant_id = " + term.restaurantID +
			  			 	   " AND " + " O.status <> " + Order.Status.UNPAID.getVal() + 
			  			 	   " AND " + " O.category = " + Order.Category.MERGER_TBL.getVal() +
			  			 ") AS O " + " ON OF.order_id = O.id " +
			  " JOIN " + Params.dbName + "." + tasteGrpTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " GROUP BY " + " OF.food_id " +
			  " HAVING sale_amount > 0 " +
			  " ORDER BY " + " OF.food_id ASC ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<IncomeByFood> foodIncomes = new ArrayList<IncomeByFood>();
		while(dbCon.rs.next()){

			Department dept = new Department();
			dept.setId(dbCon.rs.getShort("dept_id"));
			dept.setRestaurantId(dbCon.rs.getInt("restaurant_id"));

			
			Kitchen kitchen = new Kitchen();
			kitchen.setId(dbCon.rs.getInt("kitchen_id"));
			kitchen.setAliasId(dbCon.rs.getShort("kitchen_alias"));
			kitchen.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			kitchen.setDept(dept);

			Food food = new Food();
			food.setFoodId(dbCon.rs.getLong("food_id"));
			food.setAliasId(dbCon.rs.getInt("food_alias"));
			food.setName(dbCon.rs.getString("food_name"));
			food.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.setStatus(dbCon.rs.getShort("food_status"));
			food.setKitchen(kitchen);
			
			foodIncomes.add(new IncomeByFood(food,
											 dbCon.rs.getFloat("food_gift"),
											 dbCon.rs.getFloat("food_discount"),
											 dbCon.rs.getFloat("food_income"),
											 dbCon.rs.getFloat("sale_amount")));
		}
		
		dbCon.rs.close();
		
		return foodIncomes;
	}

	/**
	 * 
	 * @param term
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelIncomeByDept> calcCancelIncomeByDept(Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelIncomeByDept(dbCon, term, range, extraCond, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelIncomeByDept> calcCancelIncomeByDept(DBCon dbCon, Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		HashMap<Department, CancelIncomeByDept> result = new HashMap<Department, CancelIncomeByDept>();
		List<CancelIncomeByDeptAndReason> list = getCancelIncomeByDeptAndReason(dbCon, term, range, extraCond, queryType);
		for(CancelIncomeByDeptAndReason income : list){
			CancelIncomeByDept incomeByDept = result.get(income.getDept());
			if(incomeByDept != null){
				incomeByDept.getIncomeByEachReason().add(new IncomeByEachReason(income.getReason(),
																				income.getCancelAmount(),
																				income.getCancelPrice()));
			}else{
				List<IncomeByEachReason> incomeByEachReason = new ArrayList<IncomeByEachReason>();
				incomeByEachReason.add(new IncomeByEachReason(income.getReason(),
															  income.getCancelAmount(),
															  income.getCancelPrice()));
				result.put(income.getDept(), new CancelIncomeByDept(income.getDept(), incomeByEachReason));
			}
			
		}
		return result.values().size() > 0 ? new ArrayList<CancelIncomeByDept>(result.values()) : null;
	}
	
	/**
	 * 
	 * @param term
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelIncomeByReason> calcCancelIncomeByReason(Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelIncomeByReason(dbCon, term, range, extraCond, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelIncomeByReason> calcCancelIncomeByReason(DBCon dbCon, Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		HashMap<CancelReason, CancelIncomeByReason> result = new HashMap<CancelReason, CancelIncomeByReason>();
		List<CancelIncomeByDeptAndReason> list = getCancelIncomeByDeptAndReason(dbCon, term, range, extraCond, queryType);
		for(CancelIncomeByDeptAndReason income : list){
			CancelIncomeByReason incomeByReason = result.get(income.getReason());
			if(incomeByReason != null){
				incomeByReason.getIncomeByEachDept().add(new IncomeByEachDept(income.getDept(),
																			  income.getCancelAmount(),
																			  income.getCancelPrice()));
			}else{
				List<IncomeByEachDept> incomeByEachDept = new ArrayList<IncomeByEachDept>();
				incomeByEachDept.add(new IncomeByEachDept(income.getDept(),
														  income.getCancelAmount(),
														  income.getCancelPrice()));
				result.put(income.getReason(), new CancelIncomeByReason(income.getReason(), incomeByEachDept));
			}
		}
		return result.values().size() > 0 ? new ArrayList<CancelIncomeByReason>(result.values()) : null;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	 static List<CancelIncomeByDeptAndReason> getCancelIncomeByDeptAndReason(DBCon dbCon, Terminal term, DutyRange range, String extraCond, int queryType) throws SQLException{
		String orderFoodTbl = null;
		String tasteGrpTbl = null;
		if(queryType == QUERY_HISTORY){
			orderFoodTbl = TBL_ORDER_FOOD_HISTORY;
			tasteGrpTbl = TBL_TASTE_GROUP_HISTORY;
			
		}else if(queryType == QUERY_TODAY){
			orderFoodTbl = TBL_ORDER_FOOD_TODAY;
			tasteGrpTbl = TBL_TASTE_GROUP_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		sql = " SELECT " +
			  " MAX(DEPT.dept_id) AS dept_id, MAX(DEPT.name) AS dept_name, MAX(DEPT.restaurant_id) AS restaurant_id, " +
			  " OF.cancel_reason_id, " +
			  " CASE WHEN OF.cancel_reason_id = 1 THEN '无原因' ELSE MAX(OF.cancel_reason) END AS cancel_reason, " +
			  " ABS(SUM(OF.order_count)) AS cancel_amount, " +
			  " ABS(ROUND(SUM((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * OF.order_count * OF.discount), 2)) AS cancel_price " +
			  " FROM " + Params.dbName + "." + orderFoodTbl + " OF " +
			  " JOIN " + Params.dbName + "." + tasteGrpTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
			  " JOIN " + Params.dbName + ".department DEPT " + " ON OF.dept_id = DEPT.dept_id AND OF.restaurant_id = DEPT.restaurant_id " +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  " AND OF.restaurant_id = " + term.restaurantID +
			  " AND OF.order_count < 0 " +
			  " AND OF.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" + 
			  " GROUP BY OF.dept_id, OF.cancel_reason_id " +
			  " ORDER BY dept_id ";
		
		List<CancelIncomeByDeptAndReason> cancelByDept = new ArrayList<CancelIncomeByDeptAndReason>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			cancelByDept.add(new CancelIncomeByDeptAndReason(new Department(dbCon.rs.getString("dept_name"), 
					 											   		    dbCon.rs.getShort("dept_id"),
					 											   		    dbCon.rs.getInt("restaurant_id"),
					 											   		    Department.Type.NORMAL),
					 										 new CancelReason(dbCon.rs.getInt("cancel_reason_id"),
					 												 		  dbCon.rs.getString("cancel_reason"),
					 												 		  dbCon.rs.getInt("restaurant_id")),
					 										 dbCon.rs.getFloat("cancel_amount"),
					 										 dbCon.rs.getFloat("cancel_price")));
		}
		dbCon.rs.close();
		
		return cancelByDept;
	 }
	
	 /**
	  * Calculate the charge income.
	  * @param term
	  * @param range
	  * @param queryType
	  * @return the income by charge refer to {@link IncomeByCharge}
	  * @throws SQLException
	  * 			if failed to execute any SQL statement
	  */
	 public static IncomeByCharge calcIncomeByCharge(Terminal term, DutyRange range, int queryType) throws SQLException{
		 DBCon dbCon = new DBCon();
		 try{
			 dbCon.connect();
			 return calcIncomeByCharge(dbCon, term, range, queryType);
		 }finally{
			 dbCon.disconnect();
		 }
	 }
	 
	 /**
	  * Calculate the charge income.
	  * @param dbCon
	  * @param term
	  * @param range
	  * @param queryType
	  * @return the income by charge refer to {@link IncomeByCharge}
	  * @throws SQLException
	  * 			if failed to execute any SQL statement
	  */
	 public static IncomeByCharge calcIncomeByCharge(DBCon dbCon, Terminal term, DutyRange range, int queryType) throws SQLException{
		 String moTbl;
		 if(queryType == QUERY_TODAY){
			 moTbl = TBL_MEMBER_OPERATION;
		 }else{
			 moTbl = TBL_MEMBER_OPERATION_HISTORY;
		 }
		 
		 String sql;
		 
		 // Calculate the total charge money by cash 
		 sql = " SELECT " +
		 	   " SUM(IF(charge_type = " + ChargeType.CASH.getValue() + ", charge_money, 0)) AS total_charge_by_cash, " +
		 	   " SUM(IF(charge_type = " + ChargeType.CREDIT_CARD.getValue() + ", charge_money, 0)) AS total_charge_by_card " +
			   " FROM " + Params.dbName + "." + moTbl +
			   " WHERE " +
			   " operate_type = " + OperationType.CHARGE.getValue() +
			   " AND " +
			   " operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'";
		 
		 dbCon.rs = dbCon.stmt.executeQuery(sql);
		 
		 IncomeByCharge incomeByCharge = new IncomeByCharge();
		 
		 if(dbCon.rs.next()){
			 incomeByCharge.setCash(dbCon.rs.getFloat("total_charge_by_cash"));
			 incomeByCharge.setCreditCard(dbCon.rs.getFloat("total_charge_by_card"));
		 }
		 
		 dbCon.rs.close();
		 
		 return incomeByCharge;
		 
	 }
	 
}
