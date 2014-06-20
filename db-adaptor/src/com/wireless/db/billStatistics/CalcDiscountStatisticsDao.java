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
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByDept;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByEachDay;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByStaff;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DateType;

public class CalcDiscountStatisticsDao {

	public static class ExtraCond{
		private final DateType dateType;
		
		private final String orderTbl;
		//private final String orderTblAlias = "O";
		private final String orderFoodTbl;
		//private final String orderFoodTblAlias = "OF";
		
		private int staffId;
		private HourRange hourRange;
		private Department.DeptId deptId;
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			if(this.dateType.isToday()){
				orderTbl = "order";
				orderFoodTbl = "order_food";
			}else{
				orderTbl = "order_history";
				orderFoodTbl = "order_food_history";
			}
		}
		
		
		public ExtraCond setStaffId(int staffId){
			this.staffId = staffId;
			return this;
		}
		
		public ExtraCond setDeptId(Department.DeptId deptId){
			this.deptId = deptId;
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
				extraCond.append(" AND O.staff_id = " + staffId);
			}
			if(deptId != null){
				extraCond.append(" AND OF.dept_id = " + deptId.getVal());
			}
			if(hourRange != null){
				extraCond.append(" AND TIME(O.order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Get discount detail list by condition.
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return	the discount list
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<Order> getDiscountStatisticsDetail(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDiscountStatisticsDetail(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<Order> getDiscountStatisticsDetail(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException, BusinessException{
		String sql = makeSql4Discount(staff, range, extraCond);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<Order> result = new ArrayList<Order>();
		while(dbCon.rs.next()){
			Order o = new Order();
			o.setId(dbCon.rs.getInt("id"));
			o.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			o.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
			o.setActualPrice(dbCon.rs.getFloat("actual_price"));
			o.setWaiter(dbCon.rs.getString("waiter"));
			o.setComment(dbCon.rs.getString("comment"));
			Table t = new Table(dbCon.rs.getInt("table_id"));
			t.setTableAlias(dbCon.rs.getInt("table_alias"));
			t.setTableName(dbCon.rs.getString("table_name"));
			o.setDestTbl(t);
			result.add(o);
		}
		dbCon.rs.close();
		
		return result;
		
	}
	/**
	 * Calculate the discount income by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link DiscountIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<DiscountIncomeByEachDay> calcDiscountIncomeByEachDay(Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcDiscountIncomeByEachDay(dbCon, staff, dutyRange, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the discount income by each day according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link DiscountIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<DiscountIncomeByEachDay> calcDiscountIncomeByEachDay(DBCon dbCon, Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		
		List<DiscountIncomeByEachDay> result = new ArrayList<DiscountIncomeByEachDay>();
		
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
					  " COUNT(*) AS discount_amount, " +
					  " ROUND(SUM(TMP.discount_price), 2) AS discount_price " +
				      " FROM (" +
				      makeSql4Discount(staff, range, extraCond) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new DiscountIncomeByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()),
														 dbCon.rs.getFloat("discount_amount"),
														 dbCon.rs.getFloat("discount_price")));
				}
				dbCon.rs.close();

			}else{
				result.add(new DiscountIncomeByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()), 0, 0)); 
			}
			
			dateBegin = c.getTime();
		}
		
		return result;
	}
	
	/**
	 * Calculate the discount income by staff according to duty range and extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link DiscountIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<DiscountIncomeByStaff> calcDiscountIncomeByStaff(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcDiscountIncomeByStaff(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the discount income by staff according to duty range and extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link DiscountIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<DiscountIncomeByStaff> calcDiscountIncomeByStaff(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " TMP.staff_id, MAX(TMP.waiter) AS staff_name, " +
			  " COUNT(*) AS discount_amount, " +
			  " ROUND(SUM(TMP.discount_price), 2) AS discount_price " +
			  " FROM (" +
			  makeSql4Discount(staff, range, extraCond) +
			  " ) AS TMP " +
			  " GROUP BY TMP.staff_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<DiscountIncomeByStaff> result = new ArrayList<DiscountIncomeByStaff>();
		while(dbCon.rs.next()){
			result.add(new DiscountIncomeByStaff(dbCon.rs.getString("staff_name"), 
											   dbCon.rs.getFloat("discount_amount"), 
											   dbCon.rs.getFloat("discount_price")));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Calculate the discount income by department according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link DiscountIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<DiscountIncomeByDept> calcDiscountIncomeByDept(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcDiscountIncomeByDept(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the discount income by department according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link DiscountIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<DiscountIncomeByDept> calcDiscountIncomeByDept(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " D.dept_id, MAX(D.name) AS dept_name, D.type, " +
			  " COUNT(*) AS discount_amount, " +
			  " ROUND(SUM(TMP.discount_price), 2) AS discount_price " +
			  " FROM (" +
			  makeSql4Discount(staff, range, extraCond) +
			  " ) AS TMP " +
			  " JOIN " + Params.dbName + ".department D ON TMP.dept_id = D.dept_id AND D.restaurant_id = " + staff.getRestaurantId() + 
			  " GROUP BY TMP.dept_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<DiscountIncomeByDept> result = new ArrayList<DiscountIncomeByDept>();
		while(dbCon.rs.next()){
			Department dept = new Department(dbCon.rs.getInt("dept_id"));
			dept.setType(dbCon.rs.getInt("type"));
			if(dept.isIdle()){
				dept.setName(dept.getName().isEmpty() ? "已删除部门" : dept.getName() + "(已删除)");
			}else{
				dept.setName(dbCon.rs.getString("dept_name"));
			}
			result.add(new DiscountIncomeByDept(dept, 
											  dbCon.rs.getFloat("discount_amount"), 
											  dbCon.rs.getFloat("discount_price")));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	private static String makeSql4Discount(Staff staff, DutyRange range, ExtraCond extraCond){
		String sql;
		sql = " SELECT " +
			  " O.id, O.order_date, O.waiter, O.staff_id, O.discount_price,O.actual_price, O.comment , O.table_alias, O.table_name, O.table_id, D.name, D.dept_id, D.type" +
			  " FROM " + Params.dbName + "." + extraCond.orderTbl + " O " +
			  " JOIN " + Params.dbName + "." + extraCond.orderFoodTbl + " OF " +
			  " ON OF.order_id = O.id " +
			  " JOIN " + Params.dbName + ".department D ON D.dept_id = OF.dept_id AND D.restaurant_id = OF.restaurant_id " +
			  " WHERE 1 = 1 " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND O.discount_price <> 0 " +
			  extraCond +
			  " GROUP BY O.id ";
		
		return sql;
	}

}
