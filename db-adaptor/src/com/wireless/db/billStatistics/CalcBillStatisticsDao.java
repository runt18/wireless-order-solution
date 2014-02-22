package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.CancelIncomeByDept.IncomeByEachReason;
import com.wireless.pojo.billStatistics.CancelIncomeByDeptAndReason;
import com.wireless.pojo.billStatistics.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.CancelIncomeByReason.IncomeByEachDept;
import com.wireless.pojo.billStatistics.CommissionStatistics;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.IncomeByCancel;
import com.wireless.pojo.billStatistics.IncomeByCharge;
import com.wireless.pojo.billStatistics.IncomeByCoupon;
import com.wireless.pojo.billStatistics.IncomeByDept;
import com.wireless.pojo.billStatistics.IncomeByDiscount;
import com.wireless.pojo.billStatistics.IncomeByEachDay;
import com.wireless.pojo.billStatistics.IncomeByErase;
import com.wireless.pojo.billStatistics.IncomeByFood;
import com.wireless.pojo.billStatistics.IncomeByGift;
import com.wireless.pojo.billStatistics.IncomeByKitchen;
import com.wireless.pojo.billStatistics.IncomeByPay;
import com.wireless.pojo.billStatistics.IncomeByRepaid;
import com.wireless.pojo.billStatistics.IncomeByService;
import com.wireless.pojo.billStatistics.RepaidStatistics;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DateType;

public class CalcBillStatisticsDao {

	private final static String TBL_ORDER_TODAY = "order";
	private final static String TBL_ORDER_FOOD_TODAY = "order_food";
	private final static String TBL_TASTE_GROUP_TODAY = "taste_group";
	private final static String TBL_MEMBER_OPERATION = "member_operation";
	private final static String TBL_ORDER_HISTORY = "order_history";
	private final static String TBL_ORDER_FOOD_HISTORY = "order_food_history";
	private final static String TBL_TASTE_GROUP_HISTORY = "taste_group_history";
	private final static String TBL_MEMBER_OPERATION_HISTORY = "member_operation_history";
	
	/**
	 * 
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByPay calcIncomeByPayType(Staff staff, DutyRange range, DateType queryType) throws SQLException{	
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByPayType(dbCon, staff, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByPay calcIncomeByPayType(DBCon dbCon, Staff staff, DutyRange range, DateType queryType) throws SQLException{		
		
		String orderTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
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
			  " AND restaurant_id = " + staff.getRestaurantId() + 
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
		
		return incomeByPay;
	}
	
	/**
	 * 
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByErase calcErasePrice(Staff staff, DutyRange range, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcErasePrice(dbCon, staff, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByErase calcErasePrice(DBCon dbCon, Staff staff, DutyRange range, DateType queryType) throws SQLException{
		
		String orderTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
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
			  " AND restaurant_id = " + staff.getRestaurantId() +
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
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByDiscount calcDiscountPrice(Staff staff, DutyRange range, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcDiscountPrice(dbCon, staff, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByDiscount calcDiscountPrice(DBCon dbCon, Staff staff, DutyRange range, DateType queryType) throws SQLException{
		
		String orderTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
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
			  " AND restaurant_id = " + staff.getRestaurantId() +
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
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByGift calcGiftPrice(Staff staff, DutyRange range, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcGiftPrice(dbCon, staff, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByGift calcGiftPrice(DBCon dbCon, Staff staff, DutyRange range, DateType queryType) throws SQLException{
		String orderTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
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
		      " AND restaurant_id = " + staff.getRestaurantId() +
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
	 * Calculate the cancel price to specific duty range.
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range 
	 * @param queryType
	 * 			the query type
	 * @return the income by cancel pricee
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByCancel calcCancelPrice(Staff staff, DutyRange range, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelPrice(dbCon, staff, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Calculate the cancel price to specific duty range.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range 
	 * @param queryType
	 * 			the query type
	 * @return the income by cancel price
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByCancel calcCancelPrice(DBCon dbCon, Staff staff, DutyRange range, DateType queryType) throws SQLException{
		
		String orderTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
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
		      " AND restaurant_id = " + staff.getRestaurantId() +
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
	 * Calculate the coupon price to specific duty range.
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param queryType 
	 * 			the query type
	 * @return the income by coupon price
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByCoupon calcCouponPrice(Staff staff, DutyRange range, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCouponPrice(dbCon, staff, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the coupon price to specific duty range.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param queryType 
	 * 			the query type
	 * @return the income by coupon price
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static IncomeByCoupon calcCouponPrice(DBCon dbCon, Staff staff, DutyRange range, DateType queryType) throws SQLException{
		String orderTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		sql = " SELECT " +
		      " COUNT(*) AS amount, ROUND(SUM(coupon_price), 2) AS total_coupon " +
		      " FROM " +
		      Params.dbName + "." + orderTbl +
		      " WHERE 1 = 1 " +
		      " AND restaurant_id = " + staff.getRestaurantId() +
		      " AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND coupon_price > 0 ";
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		IncomeByCoupon couponIncome = new IncomeByCoupon();
		if(dbCon.rs.next()){
			couponIncome.setCouponAmount(dbCon.rs.getInt("amount"));
			couponIncome.setTotalCoupon(dbCon.rs.getFloat("total_coupon"));
		}
		dbCon.rs.close();
		
		return couponIncome;
	}
	
	/**
	 * 
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByRepaid calcRepaidPrice(Staff staff, DutyRange range, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcRepaidPrice(dbCon, staff, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByRepaid calcRepaidPrice(DBCon dbCon, Staff staff, DutyRange range, DateType queryType) throws SQLException{
		String orderTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
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
		      " AND restaurant_id = " + staff.getRestaurantId() +
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
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByService calcServicePrice(Staff staff, DutyRange range, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcServicePrice(dbCon, staff, range, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static IncomeByService calcServicePrice(DBCon dbCon, Staff staff, DutyRange range, DateType queryType) throws SQLException{
		String orderTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
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
			  " AND restaurant_id = " + staff.getRestaurantId() +
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
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<IncomeByDept> calcIncomeByDept(Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByDept(dbCon, staff, range, extraCond, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<IncomeByDept> calcIncomeByDept(DBCon dbCon, Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		String orderTbl = null;
		String orderFoodTbl = null;
		String tasteGrpTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			orderFoodTbl = TBL_ORDER_FOOD_HISTORY;
			tasteGrpTbl = TBL_TASTE_GROUP_HISTORY;
			
		}else if(queryType.isToday()){
			orderTbl = TBL_ORDER_TODAY;
			orderFoodTbl = TBL_ORDER_FOOD_TODAY;
			tasteGrpTbl = TBL_TASTE_GROUP_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		//Get the gift, discount & total to each department during this period.
		sql = " SELECT " +
			  " dept_id, restaurant_id, dept_type, dept_name, dept_display_id, " +
			  " ROUND(SUM(dept_gift), 2) AS dept_gift, " +
			  " ROUND(SUM(dept_discount), 2) AS dept_discount, " +
			  " ROUND(SUM(dept_income), 2) AS dept_income " +
			  " FROM (" +
				  " SELECT " +
				  " MAX(D.dept_id) AS dept_id, MAX(D.restaurant_id) AS restaurant_id, MAX(D.type) AS dept_type, " +
				  " MAX(D.name) AS dept_name, MAX(D.display_id) AS dept_display_id, " +
				  " CASE WHEN ((OF.food_status & " + Food.GIFT + ") <> 0) THEN (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) ELSE 0 END AS dept_gift," +
				  " (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * (1 - discount) * SUM(OF.order_count) AS dept_discount, " +
				  " CASE WHEN ((OF.food_status & " + Food.GIFT + ") = 0 AND (OF.food_status & " + Food.WEIGHT + ") = 0) THEN (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) " +
				  	   " WHEN ((OF.food_status & " + Food.GIFT + ") = 0 AND (OF.food_status & " + Food.WEIGHT + ") <> 0) THEN (OF.unit_price * SUM(OF.order_count) + (IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0))) * discount " +
				  	   " ELSE 0 " +
				  	   " END AS dept_income " +
				  " FROM " + Params.dbName + "." + orderFoodTbl + " OF " + 
				  " JOIN " + Params.dbName + "." + orderTbl + " O ON 1 = 1 " + 
				  " AND OF.order_id = O.id " + 
				  " AND O.restaurant_id = " + staff.getRestaurantId() + 
				  " AND O.status <> " + Order.Status.UNPAID.getVal() +
				  " JOIN " + Params.dbName + "." + tasteGrpTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
				  " JOIN " + Params.dbName + ".department D " + " ON OF.dept_id = D.dept_id AND OF.restaurant_id = D.restaurant_id AND D.type = " + Department.Type.NORMAL.getVal() +
				  " WHERE 1 = 1 " +
				  (extraCond == null ? "" : extraCond) +
				  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
				  " GROUP BY " + " OF.order_id, OF.food_id, OF.taste_group_id " +
				  " HAVING SUM(order_count) > 0 " +
				  " ) AS TMP " +
			  " GROUP BY dept_id " +
			  " ORDER BY dept_display_id ASC ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<IncomeByDept> deptIncomes = new ArrayList<IncomeByDept>();
		while(dbCon.rs.next()){
			deptIncomes.add(new IncomeByDept(new Department(dbCon.rs.getString("dept_name"),
														    dbCon.rs.getShort("dept_id"),
														    dbCon.rs.getInt("restaurant_id"),
														    Department.Type.valueOf(dbCon.rs.getShort("dept_type")),
														    dbCon.rs.getInt("dept_display_id")),
										     dbCon.rs.getFloat("dept_gift"),
										     dbCon.rs.getFloat("dept_discount"),
										     dbCon.rs.getFloat("dept_income")));
		}
		dbCon.rs.close();
	
		return deptIncomes;
	}
	
	/**
	 * 
	 * @param staff
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<IncomeByKitchen> calcIncomeByKitchen(Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByKitchen(dbCon, staff, range, extraCond, queryType);
		}finally{
			dbCon.disconnect();
		}
	}

	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<IncomeByKitchen> calcIncomeByKitchen(DBCon dbCon, Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		String orderTbl = null;
		String orderFoodTbl = null;
		String tasteGrpTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			orderFoodTbl = TBL_ORDER_FOOD_HISTORY;
			tasteGrpTbl = TBL_TASTE_GROUP_HISTORY;
			
		}else if(queryType.isToday()){
			orderTbl = TBL_ORDER_TODAY;
			orderFoodTbl = TBL_ORDER_FOOD_TODAY;
			tasteGrpTbl = TBL_TASTE_GROUP_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		//Get the gift, discount & total to each kitchen during this period.
		sql = " SELECT " +
			  " kitchen_id, kitchen_display_id, kitchen_name, kitchen_type, " +
			  " dept_id, dept_type, dept_name, dept_display_id, restaurant_id, " +
			  " ROUND(SUM(kitchen_gift), 2) AS kitchen_gift, ROUND(SUM(kitchen_discount), 2) AS kitchen_discount, ROUND(SUM(kitchen_income), 2) AS kitchen_income " +
			  " FROM ( " + 
				  " SELECT " +
				  " MAX(K.kitchen_id) AS kitchen_id, MAX(K.display_id) AS kitchen_display_id, " +
				  " MAX(K.name) AS kitchen_name, MAX(K.type) AS kitchen_type, " +
				  " MAX(D.dept_id) AS dept_id, MAX(D.type) AS dept_type, MAX(D.name) AS dept_name, MAX(D.display_id) AS dept_display_id, " +
				  " MAX(OF.restaurant_id) AS restaurant_id, " +
				  " CASE WHEN ((OF.food_status & " + Food.GIFT + ") <> 0) THEN (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) ELSE 0 END AS kitchen_gift," +
				  " (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * (1 - discount) * SUM(OF.order_count) AS kitchen_discount, " +
				  " CASE WHEN ((OF.food_status & " + Food.GIFT + ") = 0 AND (OF.food_status & " + Food.WEIGHT + ") = 0) THEN (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) " +
			  	   	   " WHEN ((OF.food_status & " + Food.GIFT + ") = 0 AND (OF.food_status & " + Food.WEIGHT + ") <> 0) THEN (OF.unit_price * SUM(OF.order_count) + (IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0))) * discount " +
			  	   	   " ELSE 0 " +
			  	   	   " END AS kitchen_income " +
			  	  " FROM " +
				  Params.dbName + "." + orderFoodTbl + " OF " + 
				  " JOIN " + Params.dbName + "." + orderTbl + " O ON 1 = 1 " + 
				  " AND OF.order_id = O.id " + 
				  " AND O.restaurant_id = " + staff.getRestaurantId() + 
				  " AND O.status <> " + Order.Status.UNPAID.getVal() +
				  " JOIN " + Params.dbName + "." + tasteGrpTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
				  " JOIN " + Params.dbName + ".kitchen K " + " ON OF.kitchen_id = K.kitchen_id AND K.type = " + Kitchen.Type.NORMAL.getVal() + 
				  " JOIN " + Params.dbName + ".department D " + " ON K.dept_id = D.dept_id AND K.restaurant_id = D.restaurant_id " +
				  " WHERE 1 = 1 " +
				  (extraCond == null ? "" : extraCond) +
				  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
				  " GROUP BY OF.order_id, OF.food_id, OF.taste_group_id ) AS TMP " +
			  " GROUP BY kitchen_id " +
			  " ORDER BY kitchen_display_id ASC ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<IncomeByKitchen> kitchenIncomes = new ArrayList<IncomeByKitchen>();
		while(dbCon.rs.next()){
			Kitchen k = new Kitchen(dbCon.rs.getInt("kitchen_id"));
			k.setName(dbCon.rs.getString("kitchen_name"));
			k.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			k.setAllowTemp(false);
			k.setType(dbCon.rs.getShort("kitchen_type"));
			k.setDept(new Department(dbCon.rs.getString("dept_name"),
									 dbCon.rs.getShort("dept_id"),
									 dbCon.rs.getInt("restaurant_id"),
									 Department.Type.valueOf(dbCon.rs.getShort("dept_type")),
									 dbCon.rs.getInt("dept_display_id")));
			
			kitchenIncomes.add(new IncomeByKitchen(k, 
												   dbCon.rs.getFloat("kitchen_gift"),
												   dbCon.rs.getFloat("kitchen_discount"),
												   dbCon.rs.getFloat("kitchen_income")));
		}
		
		dbCon.rs.close();
		
		return kitchenIncomes;
	}
	
	public static List<IncomeByFood> calcIncomeByFood(Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByFood(dbCon, staff, range, extraCond, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<IncomeByFood> calcIncomeByFood(DBCon dbCon, Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		String orderTbl = null;
		String orderFoodTbl = null;
		String tasteGrpTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			orderFoodTbl = TBL_ORDER_FOOD_HISTORY;
			tasteGrpTbl = TBL_TASTE_GROUP_HISTORY;
			
		}else if(queryType.isToday()){
			orderTbl = TBL_ORDER_TODAY;
			orderFoodTbl = TBL_ORDER_FOOD_TODAY;
			tasteGrpTbl = TBL_TASTE_GROUP_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		//Get the gift, discount & total to each food during this period.
		sql = " SELECT " +
			  " OF.food_id, MAX(OF.name) AS food_name, " +
			  " MAX(OF.food_status) AS food_status, MAX(OF.restaurant_id) AS restaurant_id, " +
			  " MAX(OF.kitchen_id) AS kitchen_id, " +
			  " MAX(OF.dept_id) AS dept_id, " +
			  " SUM(OF.order_count) AS sale_amount, " +
			  " ROUND(SUM(CASE WHEN ((OF.food_status & " + Food.GIFT + ") <> 0) THEN ((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * OF.order_count) ELSE 0 END), 2) AS food_gift," +
			  " ROUND(SUM((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * (1 - discount) * OF.order_count), 2) AS food_discount, " +
			  " ROUND(SUM(CASE WHEN ((OF.food_status & " + Food.GIFT + ") = 0) THEN ((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * OF.order_count) ELSE 0 END), 2) AS food_income " +
//			  " ROUND(CASE WHEN ((OF.food_status & " + Food.GIFT + ") = 0 AND (OF.food_status & " + Food.WEIGHT + ") = 0) THEN (OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * discount * SUM(OF.order_count) " +
//	  	   	  " WHEN ((OF.food_status & " + Food.GIFT + ") = 0 AND (OF.food_status & " + Food.WEIGHT + ") <> 0) THEN (OF.unit_price * SUM(OF.order_count) + (IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0))) * discount " +
//	  	   	  " ELSE 0 " +
//	  	   	  " END, 2) AS food_income " +
			  " FROM " +
			  Params.dbName + "." + orderFoodTbl + " OF " + 
			  " JOIN " + Params.dbName + "." + orderTbl + " O ON 1 = 1 " + 
			  " AND OF.order_id = O.id " + 
			  " AND O.restaurant_id = " + staff.getRestaurantId() + 
			  " AND O.status <> " + Order.Status.UNPAID.getVal() +
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

			Department dept = new Department(dbCon.rs.getShort("dept_id"));
			dept.setRestaurantId(dbCon.rs.getInt("restaurant_id"));

			
			Kitchen kitchen = new Kitchen(dbCon.rs.getInt("kitchen_id"));
			kitchen.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			kitchen.setDept(dept);

			Food food = new Food(dbCon.rs.getInt("food_id"));
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
	 * @param staff
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelIncomeByDept> calcCancelIncomeByDept(Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelIncomeByDept(dbCon, staff, range, extraCond, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelIncomeByDept> calcCancelIncomeByDept(DBCon dbCon, Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		HashMap<Department, CancelIncomeByDept> result = new HashMap<Department, CancelIncomeByDept>();
		List<CancelIncomeByDeptAndReason> list = calcCancelIncomeByDeptAndReason(dbCon, staff, range, extraCond, queryType);
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
		return result.values().size() > 0 ? new ArrayList<CancelIncomeByDept>(result.values()) : new ArrayList<CancelIncomeByDept>(0);
	}
	
	/**
	 * 
	 * @param staff
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelIncomeByReason> calcCancelIncomeByReason(Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelIncomeByReason(dbCon, staff, range, extraCond, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	public static List<CancelIncomeByReason> calcCancelIncomeByReason(DBCon dbCon, Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		HashMap<CancelReason, CancelIncomeByReason> result = new HashMap<CancelReason, CancelIncomeByReason>();
		List<CancelIncomeByDeptAndReason> list = calcCancelIncomeByDeptAndReason(dbCon, staff, range, extraCond, queryType);
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
		return result.values().size() > 0 ? new ArrayList<CancelIncomeByReason>(result.values()) : new ArrayList<CancelIncomeByReason>(0);
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return
	 * @throws SQLException
	 */
	 private static List<CancelIncomeByDeptAndReason> calcCancelIncomeByDeptAndReason(DBCon dbCon, Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		String orderFoodTbl = null;
		String tasteGrpTbl = null;
		if(queryType.isHistory()){
			orderFoodTbl = TBL_ORDER_FOOD_HISTORY;
			tasteGrpTbl = TBL_TASTE_GROUP_HISTORY;
			
		}else if(queryType.isToday()){
			orderFoodTbl = TBL_ORDER_FOOD_TODAY;
			tasteGrpTbl = TBL_TASTE_GROUP_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		
		sql = " SELECT " +
			  " MAX(D.dept_id) AS dept_id, MAX(D.display_id) AS dept_display_id, MAX(D.name) AS dept_name, " +
			  " MAX(D.restaurant_id) AS restaurant_id, MAX(D.type) AS dept_type, " +
			  " OF.cancel_reason_id, " +
			  " CASE WHEN OF.cancel_reason_id = 1 THEN '无原因' ELSE MAX(OF.cancel_reason) END AS cancel_reason, " +
			  " ABS(SUM(OF.order_count)) AS cancel_amount, " +
			  " ABS(ROUND(SUM((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * OF.order_count * OF.discount), 2)) AS cancel_price " +
			  " FROM " + Params.dbName + "." + orderFoodTbl + " OF " +
			  " JOIN " + Params.dbName + ".order_history O ON 1 = 1" +
			  " AND OF.order_id = O.id " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND O.cancel_price <> 0 " +
			  " JOIN " + Params.dbName + "." + tasteGrpTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
			  " JOIN " + Params.dbName + ".department D " + " ON OF.dept_id = D.dept_id AND OF.restaurant_id = D.restaurant_id AND D.type = " + Department.Type.NORMAL.getVal() +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  " AND OF.order_count < 0 " +
			  " GROUP BY OF.dept_id, OF.cancel_reason_id " +
			  " ORDER BY dept_display_id ";
		
		List<CancelIncomeByDeptAndReason> cancelByDept = new ArrayList<CancelIncomeByDeptAndReason>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			cancelByDept.add(new CancelIncomeByDeptAndReason(new Department(dbCon.rs.getString("dept_name"), 
					 											   		    dbCon.rs.getShort("dept_id"),
					 											   		    dbCon.rs.getInt("restaurant_id"),
					 											   		    Department.Type.valueOf(dbCon.rs.getInt("dept_type")),
					 											   		    dbCon.rs.getInt("dept_display_id")),
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
	  * @param staff
	  * @param range
	  * @param queryType
	  * @return the income by charge refer to {@link IncomeByCharge}
	  * @throws SQLException
	  * 			if failed to execute any SQL statement
	  */
	 public static IncomeByCharge calcIncomeByCharge(Staff staff, DutyRange range, DateType queryType) throws SQLException{
		 DBCon dbCon = new DBCon();
		 try{
			 dbCon.connect();
			 return calcIncomeByCharge(dbCon, staff, range, queryType);
		 }finally{
			 dbCon.disconnect();
		 }
	 }
	 
	 /**
	  * Calculate the charge income.
	  * @param dbCon
	  * @param staff
	  * @param range
	  * @param queryType
	  * @return the income by charge refer to {@link IncomeByCharge}
	  * @throws SQLException
	  * 			if failed to execute any SQL statement
	  */
	 public static IncomeByCharge calcIncomeByCharge(DBCon dbCon, Staff staff, DutyRange range, DateType queryType) throws SQLException{
		 String moTbl;
		 if(queryType.isToday()){
			 moTbl = TBL_MEMBER_OPERATION;
		 }else{
			 moTbl = TBL_MEMBER_OPERATION_HISTORY;
		 }
		 
		 String sql;
		 
		 // Calculate the charge money. 
		 sql = " SELECT " +
			   " COUNT(*) AS charge_amount, " +
			   " SUM(delta_base_money + delta_extra_money) AS total_account_charge, " +
		 	   " SUM(IF(charge_type = " + ChargeType.CASH.getValue() + ", charge_money, 0)) AS total_actual_charge_by_cash, " +
		 	   " SUM(IF(charge_type = " + ChargeType.CREDIT_CARD.getValue() + ", charge_money, 0)) AS total_actual_charge_by_card " +
			   " FROM " + Params.dbName + "." + moTbl +
			   " WHERE 1 = 1 " +
			   " AND restaurant_id = " + staff.getRestaurantId() +
			   " AND operate_type = " + OperationType.CHARGE.getValue() +
			   " AND operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'";
		 
		 dbCon.rs = dbCon.stmt.executeQuery(sql);
		 
		 IncomeByCharge incomeByCharge = new IncomeByCharge();
		 
		 if(dbCon.rs.next()){
			 incomeByCharge.setChargeAmount(dbCon.rs.getInt("charge_amount"));
			 incomeByCharge.setActualCashCharge(dbCon.rs.getFloat("total_actual_charge_by_cash"));
			 incomeByCharge.setActualCreditCardCharge(dbCon.rs.getFloat("total_actual_charge_by_card"));
			 incomeByCharge.setTotalAccountCharge(dbCon.rs.getFloat("total_account_charge"));
		 }
		 
		 dbCon.rs.close();
		 
		 // Calculate the refund. 
		 sql = " SELECT " +
			   " COUNT(*) AS refund_amount, " +
			   " SUM(delta_base_money + delta_extra_money) AS total_account_refund, " +
		 	   " SUM(charge_money) AS total_actual_refund " +
			   " FROM " + Params.dbName + "." + moTbl +
			   " WHERE 1 = 1 " +
			   " AND restaurant_id = " + staff.getRestaurantId() +
			   " AND operate_type = " + OperationType.REFUND.getValue() +
			   " AND operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'";
		 
		 dbCon.rs = dbCon.stmt.executeQuery(sql);
		 
		 if(dbCon.rs.next()){
			 incomeByCharge.setRefundAmount(dbCon.rs.getInt("refund_amount"));
			 incomeByCharge.setTotalActualRefund(Math.abs(dbCon.rs.getFloat("total_actual_refund")));
			 incomeByCharge.setTotalAccountRefund(Math.abs(dbCon.rs.getFloat("total_account_refund")));
		 }
		 
		 return incomeByCharge;
		 
	 }
	 
	/**
	 * Get repaid list of order. 
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return the repaid list
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	private static List<RepaidStatistics> calcRepaidStat(DBCon dbCon, Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException{
		String orderTbl = null;
		if(queryType.isHistory()){
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		String sql;
		sql = " SELECT " +
			  " order_date, waiter, staff_id, id,  repaid_price, total_price, actual_price, pay_type "  +
			  " FROM " + Params.dbName + "." + orderTbl +
			  " WHERE 1 = 1 " +
			  " AND status = " + Order.Status.REPAID.getVal() + 
			  " AND restaurant_id = " + staff.getRestaurantId() + 
			  " AND order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  (extraCond != null ? extraCond : "") +
			  " ORDER BY order_date ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<RepaidStatistics> result = new ArrayList<RepaidStatistics>();
		while(dbCon.rs.next()){
			RepaidStatistics each = new RepaidStatistics();
			Staff oStaff = new Staff();
			each.setId(dbCon.rs.getInt("id"));
			each.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			each.setActualPrice(dbCon.rs.getFloat("actual_price"));
			each.setTotalPrice(dbCon.rs.getFloat("total_price"));
			each.setRepaidPrice(dbCon.rs.getFloat("repaid_price"));
			each.setPaymentType(dbCon.rs.getInt("pay_type"));
			oStaff.setId(dbCon.rs.getInt("staff_id"));
			oStaff.setName(dbCon.rs.getString("waiter"));
			each.setStaff(oStaff);
			result.add(each);
		}
		dbCon.rs.close();
		return Collections.unmodifiableList(result);
	}
	/**
	 * Get repaid detail by staff_id.
	 * @param staff
	 * @param range
	 * @param staffId
	 * @param queryType
	 * @return	the detail of repaid
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<RepaidStatistics> calcRepaidStatByStaff(Staff staff, DutyRange range, int staffId, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcRepaidStat(dbCon, staff, range, " AND staff_id = " + staffId , queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get repaid list of all.
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return	the repaid list
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<RepaidStatistics> calcRepaidStat(Staff staff, DutyRange range, DateType queryType) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcRepaidStat(dbCon, staff, range, null , queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get commission list.
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param extraCond
	 * @param queryType
	 * @return	the commission
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	private static List<CommissionStatistics> calcCommissionStat(DBCon dbCon, Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException, BusinessException{
		String orderFoodTbl = null;
		String orderTbl = null;
		if(queryType.isHistory()){
			orderFoodTbl = TBL_ORDER_FOOD_HISTORY;
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
			orderFoodTbl = TBL_ORDER_FOOD_TODAY;
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		
		String sql;
		sql = " SELECT OFH.order_id, OFH.order_date, OFH.name, OFH.unit_price, OFH.order_count, OFH.waiter, " +
			  " ROUND((OFH.unit_price * OFH.order_count), 2) AS total_price, " + 
			  " ROUND((OFH.commission * OFH.order_count), 2) AS commission, "  +
			  " D.dept_id, D.restaurant_id, D.name AS dept_name " +
			  " FROM " + Params.dbName + "." + orderFoodTbl + " OFH " +
			  " JOIN " + Params.dbName + "." + orderTbl + " OH ON 1 = 1 " + 
			  " AND OFH.order_id = OH.id " +
			  " AND OH.restaurant_id = " + staff.getRestaurantId() +
		 	  " AND OH.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " JOIN " + Params.dbName + ".department D ON D.dept_id = OFH.dept_id AND D.restaurant_id = OFH.restaurant_id " +
		 	  " WHERE 1 = 1 " +
			  " AND (OFH.food_status & " + Food.COMMISSION + ") <> 0 " +
		 	  " AND OFH.commission <> 0 " +
		 	  (extraCond != null ? extraCond : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<CommissionStatistics> result = new ArrayList<CommissionStatistics>();
		while(dbCon.rs.next()){
			CommissionStatistics c = new CommissionStatistics();
			c.setOrderId(dbCon.rs.getInt("order_id"));
			c.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			c.setFoodName(dbCon.rs.getString("name"));
			c.setDept( new Department(dbCon.rs.getInt("restaurant_id"), dbCon.rs.getShort("dept_id"), dbCon.rs.getString("dept_name")));
			c.setUnitPrice(dbCon.rs.getFloat("unit_price"));
			c.setAmount(dbCon.rs.getFloat("order_count"));
			c.setTotalPrice(dbCon.rs.getFloat("total_price"));
			c.setCommission(dbCon.rs.getFloat("commission"));
			c.setWaiter(dbCon.rs.getString("waiter"));
			result.add(c);
		}
		dbCon.rs.close();
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Get the commission list by staff_id. 
	 * @param staff
	 * @param range
	 * @param staffId
	 * @param queryType
	 * @return	the commission list
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<CommissionStatistics> calcCommissionStatByStaff(Staff staff, DutyRange range, int staffId, DateType queryType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCommissionStat(dbCon, staff, range, " AND OFH.staff_id = " + staffId , queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the commission list by dept_id. 
	 * @param staff
	 * @param range
	 * @param deptId
	 * @param queryType
	 * @return the commission list
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<CommissionStatistics> calcCommissionStatByDeptId(Staff staff, DutyRange range, int deptId, DateType queryType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCommissionStat(dbCon, staff, range, " AND OFH.dept_id = " + deptId , queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the commission list by extra condition.
	 * @param staff
	 * @param range
	 * @param staffId
	 * 			the staff id
	 * @param deptId
	 * 			the department id
	 * @param queryType
	 * @return
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<CommissionStatistics> calcCommissionStatByDeptAndStaff(Staff staff, DutyRange range,int staffId, int deptId, DateType queryType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCommissionStat(dbCon, staff, range, " AND OFH.staff_id = " + staffId + " AND OFH.dept_id = " + deptId , queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get commission list off all.
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return	the commission list
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<CommissionStatistics> calcCommissionStatistics(Staff staff, DutyRange range, DateType queryType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCommissionStat(dbCon, staff, range, null , queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<CommissionStatistics> calcCommissionTotal(Staff staff, DutyRange range, DateType queryType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCommissionTotal(dbCon, staff, range, null, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get 
	 * @param staff
	 * @param range
	 * @param deptId
	 * @param queryType
	 * @return
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			
	 */
	public static List<CommissionStatistics> calcCommissionTotalByDept(Staff staff, DutyRange range, int deptId, DateType queryType) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCommissionTotal(dbCon, staff, range, " AND OFH.dept_id = " + deptId, queryType);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get commission total by extra condition.
	 * @param dbCon
	 * @param staff
	 * @param range
	 * @param extraCond
	 * 			the extra condition
	 * @param queryType
	 * @return
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	private static List<CommissionStatistics> calcCommissionTotal(DBCon dbCon, Staff staff, DutyRange range, String extraCond, DateType queryType) throws SQLException, BusinessException{
		String orderFoodTbl = null;
		String orderTbl = null;
		if(queryType.isHistory()){
			orderFoodTbl = TBL_ORDER_FOOD_HISTORY;
			orderTbl = TBL_ORDER_HISTORY;
			
		}else if(queryType.isToday()){
			orderFoodTbl = TBL_ORDER_FOOD_TODAY;
			orderTbl = TBL_ORDER_TODAY;
			
		}else{
			throw new IllegalArgumentException("The query type is invalid.");
		}
		String sql;
		sql = " SELECT " +
			  " ROUND(SUM(OFH.unit_price * OFH.order_count), 2) AS totalPrice, " +
			  " ROUND(SUM(OFH.commission * OFH.order_count), 2) AS commission, MAX(OFH.waiter) AS waiter, " +
			  " OFH.staff_id " +
			  " FROM " + Params.dbName + "." + orderFoodTbl + " OFH " +
			  " JOIN " + Params.dbName + "." + orderTbl + " OH ON 1 = 1 " +
			  " AND OH.id = OFH.order_id " +
			  " AND OH.restaurant_id = " + staff.getRestaurantId() + 
			  " AND OH.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " WHERE (OFH.food_status & " + Food.COMMISSION + ") <> 0 " +
			  " AND OFH.commission <> 0 " +
			  (extraCond != null ? extraCond : "") +
			  " GROUP BY OFH.staff_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<CommissionStatistics> result = new ArrayList<CommissionStatistics>();
		while(dbCon.rs.next()){
			CommissionStatistics c = new CommissionStatistics();
			c.setWaiter(dbCon.rs.getString("waiter"));
			c.setTotalPrice(dbCon.rs.getFloat("totalPrice"));
			c.setCommission(dbCon.rs.getFloat("commission"));
			result.add(c);
		}
		dbCon.rs.close();
		
		return Collections.unmodifiableList(result);
	}

	/**
	 * Get income to each day during on & off duty.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param onDuty
	 * 			the on duty
	 * @param offDuty
	 * 			the off duty
	 * @return the income by each during on & off duty
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the on or off duty string
	 */
	public static List<IncomeByEachDay> calcIncomeByEachDay(DBCon dbCon, Staff staff, String onDuty, String offDuty) throws SQLException, ParseException{
		
		List<IncomeByEachDay> result = new ArrayList<IncomeByEachDay>();
		
		Calendar c = Calendar.getInstance();
		Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(onDuty);
		Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(offDuty);
		c.setTime(dateBegin);
		while (dateBegin.compareTo(dateEnd) <= 0) {
			c.add(Calendar.DATE, 1);
			
			DutyRange range = DutyRangeDao.exec(dbCon, staff, 
												DateUtil.format(dateBegin, DateUtil.Pattern.DATE_TIME), 
												DateUtil.format(c.getTime(), DateUtil.Pattern.DATE_TIME));
			
			IncomeByEachDay income = new IncomeByEachDay(DateUtil.format(dateBegin, DateUtil.Pattern.DATE));
			if(range != null){
				
				//Calculate the general income
				income.setIncomeByPay(calcIncomeByPayType(dbCon, staff, range, DateType.HISTORY));
				
				//Calculate the total & amount to erase price
				income.setIncomeByErase(calcErasePrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the total & amount to discount price
				income.setIncomeByDiscount(calcDiscountPrice(dbCon, staff, range, DateType.HISTORY));
	
				//Get the total & amount to gift price
				income.setIncomeByGift(calcGiftPrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the total & amount to cancel price
				income.setIncomeByCancel(calcCancelPrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the total & amount to coupon price
				income.setIncomeByCoupon(calcCouponPrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the total & amount to repaid order
				income.setIncomeByRepaid(calcRepaidPrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the total & amount to order with service
				income.setIncomeByService(calcServicePrice(dbCon, staff, range, DateType.HISTORY));
				
				//Get the charge income by both cash and credit card
				income.setIncomeByCharge(calcIncomeByCharge(dbCon, staff, range, DateType.HISTORY));
				
			}
			result.add(income);
			
			dateBegin = c.getTime();
		}
		
		return Collections.unmodifiableList(result);
	}

	/**
	 * Get income to each day during on & off duty.
	 * @param staff
	 * 			the staff to perform this action
	 * @param onDuty
	 * 			the on duty
	 * @param offDuty
	 * 			the off duty
	 * @return the income by each during on & off duty
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the on or off duty string
	 */
	public static List<IncomeByEachDay> calcIncomeByEachDay(Staff staff, String onDuty, String offDuty) throws SQLException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByEachDay(dbCon, staff, onDuty, offDuty);
		}finally{
			dbCon.disconnect();
		}
	}
	 
}
