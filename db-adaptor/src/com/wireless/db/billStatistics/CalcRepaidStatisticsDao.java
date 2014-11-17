package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.repaid.RepaidIncomeByEachDay;
import com.wireless.pojo.billStatistics.repaid.RepaidIncomeByStaff;
import com.wireless.pojo.billStatistics.repaid.RepaidStatistics;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DateType;

public class CalcRepaidStatisticsDao {
	public static class ExtraCond{
		private final DateType dateType;
		
		private final String orderTbl;
		//private final String orderTblAlias = "O";
		private final String orderFoodTbl;
		//private final String orderFoodTblAlias = "OF";
		private final String tasteGroupTbl;
		
		private int staffId;
		private HourRange hourRange;
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			if(this.dateType.isToday()){
				orderTbl = "order";
				orderFoodTbl = "order_food";
				tasteGroupTbl = "taste_group";
			}else{
				orderTbl = "order_history";
				orderFoodTbl = "order_food_history";
				tasteGroupTbl = "taste_group_history";
			}
		}
		
		
		public ExtraCond setStaffId(int staffId){
			this.staffId = staffId;
			return this;
		}
		
		public ExtraCond setHourRange(HourRange hourRange){
			this.hourRange = hourRange;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(staffId > 0){
				extraCond.append(" AND OF.staff_id = " + staffId);
			}
			if(hourRange != null){
				extraCond.append(" AND TIME(O.order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Get the repaid detail by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link RepaidIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<RepaidStatistics> getRepaidIncomeDetail(Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getRepaidIncomeDetail(dbCon, staff, dutyRange, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<RepaidStatistics> getRepaidIncomeDetail(DBCon dbCon, Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException{
		String sql = makeSql4RepaidFood(staff, dutyRange, extraCond);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<RepaidStatistics> result = new ArrayList<RepaidStatistics>();
		while(dbCon.rs.next()){
			RepaidStatistics each = new RepaidStatistics();
			each.setId(dbCon.rs.getInt("order_id"));
			each.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			each.setActualPrice(dbCon.rs.getFloat("actual_price"));
			each.setTotalPrice(dbCon.rs.getFloat("total_price"));
			each.setRepaidPrice(dbCon.rs.getFloat("repaid_price"));
			PayType payType = new PayType(dbCon.rs.getInt("pay_type_id"));
			payType.setName(dbCon.rs.getString("pay_type_name"));
			each.setPaymentType(payType);
			Staff operator = new Staff();
			operator.setId(dbCon.rs.getInt("staff_id"));
			operator.setName(dbCon.rs.getString("waiter"));
			each.setStaff(operator);
			result.add(each);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	
	/**
	 * Calculate the repaid income by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link RepaidIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<RepaidIncomeByEachDay> calcRepaidIncomeByEachDay(Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcRepaidIncomeByEachDay(dbCon, staff, dutyRange, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the repaid income by each day according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link RepaidIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<RepaidIncomeByEachDay> calcRepaidIncomeByEachDay(DBCon dbCon, Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		
		List<RepaidIncomeByEachDay> result = new ArrayList<RepaidIncomeByEachDay>();
		
		Calendar c = Calendar.getInstance();
		Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(dutyRange.getOnDutyFormat());
		Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(dutyRange.getOffDutyFormat());
		c.setTime(dateBegin);
		while (dateBegin.compareTo(dateEnd) <= 0) {
			c.add(Calendar.DATE, 1);
			
			DutyRange range = DutyRangeDao.exec(dbCon, staff, 
					DateUtil.format(dateBegin, DateUtil.Pattern.DATE_TIME), 
					DateUtil.format(c.getTime(), DateUtil.Pattern.DATE_TIME));
			
			if(range != null){
				String sql;
				sql = " SELECT " +
					  " ROUND(SUM(TMP.repaid_amount), 2) AS repaid_amount, " +
					  " ROUND(SUM(TMP.repaid_price), 2) AS repaid_price " +
				      " FROM (" +
					  makeSql4RepaidFood(staff, range, extraCond) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new RepaidIncomeByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()),
														 dbCon.rs.getFloat("repaid_amount"),
														 dbCon.rs.getFloat("repaid_price")));
				}
				dbCon.rs.close();

			}else{
				result.add(new RepaidIncomeByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()), 0, 0)); 
			}
			
			dateBegin = c.getTime();
		}
		
		return result;
	}
	
	/**
	 * Calculate the repaid income by staff according to duty range and extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link RepaidIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<RepaidIncomeByStaff> calcRepaidIncomeByStaff(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcRepaidIncomeByStaff(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the repaid income by staff according to duty range and extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link RepaidIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<RepaidIncomeByStaff> calcRepaidIncomeByStaff(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " TMP.staff_id, MAX(TMP.waiter) AS staff_name, " +
			  " ROUND(SUM(TMP.repaid_amount), 2) AS repaid_amount, " +
			  " ROUND(SUM(TMP.repaid_price), 2) AS repaid_price " +
			  " FROM (" +
			  makeSql4RepaidFood(staff, range, extraCond) +
			  " ) AS TMP " +
			  " GROUP BY TMP.staff_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<RepaidIncomeByStaff> result = new ArrayList<RepaidIncomeByStaff>();
		while(dbCon.rs.next()){
			result.add(new RepaidIncomeByStaff(dbCon.rs.getString("staff_name"), 
											   dbCon.rs.getFloat("repaid_amount"), 
											   dbCon.rs.getFloat("repaid_price")));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	private static String makeSql4RepaidFood(Staff staff, DutyRange range, ExtraCond extraCond){
		String sql;
		sql = " SELECT " +
			  " OF.dept_id, OF.staff_id, OF.waiter, OF.order_date, OF.order_id, O.repaid_price detail_repaid_price, O.total_price, O.actual_price, " + 
			  " O.pay_type_id, IFNULL(PT.name, '其他') AS pay_type_name, " +
			  " ABS(OF.order_count) AS repaid_amount, " +
			  " ABS((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * OF.order_count * OF.discount) AS repaid_price " +
			  " FROM " + Params.dbName + "." + extraCond.orderFoodTbl + " OF " + 
			  " JOIN " + Params.dbName + "." + extraCond.orderTbl + " O ON 1 = 1 " +
			  " AND OF.order_id = O.id " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND OF.is_paid = 1 " +
			  " JOIN " + Params.dbName + "." + extraCond.tasteGroupTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
			  " LEFT JOIN " + Params.dbName + ".pay_type PT ON PT.pay_type_id = O.pay_type_id " + 
			  " WHERE 1 = 1 " +
			  extraCond;
		
		return sql;
	}

}
