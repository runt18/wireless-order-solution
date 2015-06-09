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
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByEachDay;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByStaff;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class CalcCancelStatisticsDao {
	
	public static class ExtraCond{
		private final DateType dateType;
		
		private final String orderTbl;
		//private final String orderTblAlias = "O";
		private final String orderFoodTbl;
		//private final String orderFoodTblAlias = "OF";
		private final String tasteGroupTbl;
		
		private Department.DeptId deptId;
		private int staffId;
		private int reasonId;
		private Region.RegionId regionId;
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
		
		public ExtraCond setDeptId(Department.DeptId deptId){
			this.deptId = deptId;
			return this;
		}
		
		public ExtraCond setStaffId(int staffId){
			this.staffId = staffId;
			return this;
		}
		
		public ExtraCond setReasonId(int reasonId){
			this.reasonId = reasonId;
			return this;
		}
		
		public ExtraCond setRegionId(Region.RegionId regionId){
			this.regionId = regionId;
			return this;
		}
		
		public ExtraCond setHourRange(HourRange hourRange){
			this.hourRange = hourRange;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(deptId != null){
				extraCond.append(" AND OF.dept_id = " + deptId.getVal());
			}
			if(staffId > 0){
				extraCond.append(" AND OF.staff_id = " + staffId);
			}
			if(reasonId > 0){
				extraCond.append(" AND OF.cancel_reason_id = " + reasonId);
			}
			if(regionId != null){
				extraCond.append(" AND O.region_id = " + regionId.getId());
			}
			if(hourRange != null){
				extraCond.append(" AND TIME(O.order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Calculate the cancel income by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<CancelIncomeByEachDay> calcCancelIncomeByEachDay(Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelIncomeByEachDay(dbCon, staff, dutyRange, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the cancel income by each day according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<CancelIncomeByEachDay> calcCancelIncomeByEachDay(DBCon dbCon, Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		
		List<CancelIncomeByEachDay> result = new ArrayList<CancelIncomeByEachDay>();
		
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
					  " ROUND(SUM(TMP.cancel_amount), 2) AS cancel_amount, " +
					  " ROUND(SUM(TMP.cancel_price), 2) AS cancel_price " +
				      " FROM (" +
					  makeSql4CancelFood(staff, range, extraCond) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new CancelIncomeByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()),
														 dbCon.rs.getFloat("cancel_amount"),
														 dbCon.rs.getFloat("cancel_price")));
				}
				dbCon.rs.close();

			}else{
				result.add(new CancelIncomeByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()), 0, 0)); 
			}
			
			dateBegin = c.getTime();
		}
		
		return result;
	}
	
	/**
	 * Calculate the cancel income by department according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CancelIncomeByDept> calcCancelIncomeByDept(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelIncomeByDept(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the cancel income by department according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CancelIncomeByDept> calcCancelIncomeByDept(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " D.dept_id, MAX(D.name) AS dept_name, D.type, " +
			  " ROUND(SUM(TMP.cancel_amount), 2) AS cancel_amount, " +
			  " ROUND(SUM(TMP.cancel_price), 2) AS cancel_price " +
			  " FROM (" +
			  makeSql4CancelFood(staff, range, extraCond) +
			  " ) AS TMP " +
			  " JOIN " + Params.dbName + ".department D ON TMP.dept_id = D.dept_id AND D.restaurant_id = " + staff.getRestaurantId() + 
			  " GROUP BY TMP.dept_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<CancelIncomeByDept> result = new ArrayList<CancelIncomeByDept>();
		while(dbCon.rs.next()){
			Department dept = new Department(dbCon.rs.getInt("dept_id"));
			dept.setType(dbCon.rs.getInt("type"));
			if(dept.isIdle()){
				dept.setName(dept.getName().isEmpty() ? "已删除部门" : dept.getName() + "(已删除)");
			}else{
				dept.setName(dbCon.rs.getString("dept_name"));
			}
			result.add(new CancelIncomeByDept(dept, 
											  dbCon.rs.getFloat("cancel_amount"), 
											  dbCon.rs.getFloat("cancel_price")));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Calculate the cancel income by staff according to duty range and extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link CancelIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CancelIncomeByStaff> calcCancelIncomeByStaff(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelIncomeByStaff(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the cancel income by staff according to duty range and extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link CancelIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CancelIncomeByStaff> calcCancelIncomeByStaff(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " TMP.staff_id, MAX(TMP.waiter) AS staff_name, " +
			  " ROUND(SUM(TMP.cancel_amount), 2) AS cancel_amount, " +
			  " ROUND(SUM(TMP.cancel_price), 2) AS cancel_price " +
			  " FROM (" +
			  makeSql4CancelFood(staff, range, extraCond) +
			  " ) AS TMP " +
			  " GROUP BY TMP.staff_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<CancelIncomeByStaff> result = new ArrayList<CancelIncomeByStaff>();
		while(dbCon.rs.next()){
			result.add(new CancelIncomeByStaff(dbCon.rs.getString("staff_name"), 
											   dbCon.rs.getFloat("cancel_amount"), 
											   dbCon.rs.getFloat("cancel_price")));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Calculate the cancel income by reason according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByReason}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CancelIncomeByReason> calcCancelIncomeByReason(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelIncomeByReason(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the cancel income by reason according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByReason}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CancelIncomeByReason> calcCancelIncomeByReason(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " TMP.cancel_reason_id, " +
			  " MAX(TMP.cancel_reason) AS cancel_reason, " +
			  " ROUND(SUM(TMP.cancel_amount), 2) AS cancel_amount, " +
			  " ROUND(SUM(TMP.cancel_price), 2) AS cancel_price " +
			  " FROM (" +
			  makeSql4CancelFood(staff, range, extraCond) +
			  " ) AS TMP " +
			  " GROUP BY TMP.cancel_reason_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<CancelIncomeByReason> result = new ArrayList<CancelIncomeByReason>();
		while(dbCon.rs.next()){
			result.add(new CancelIncomeByReason(new CancelReason(dbCon.rs.getInt("cancel_reason_id"), dbCon.rs.getString("cancel_reason"), staff.getRestaurantId()), 
											    dbCon.rs.getFloat("cancel_amount"), 
											    dbCon.rs.getFloat("cancel_price")));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	private static String makeSql4CancelFood(Staff staff, DutyRange range, ExtraCond extraCond){
		String sql;
		sql = " SELECT " +
			  " OF.dept_id, OF.staff_id, OF.waiter, OF.cancel_reason_id, IFNULL(OF.cancel_reason, '无原因') AS cancel_reason, " +
			  " ABS(OF.order_count) AS cancel_amount, " +
			  " ABS((OF.unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * OF.order_count * OF.discount) AS cancel_price " +
			  " FROM " + Params.dbName + "." + extraCond.orderFoodTbl + " OF " + 
			  " JOIN " + Params.dbName + "." + extraCond.orderTbl + " O " +
			  " ON 1 = 1 " +
			  " AND OF.order_id = O.id " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND O.cancel_price <> 0 " +
			  " JOIN " + Params.dbName + "." + extraCond.tasteGroupTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
			  " WHERE 1 = 1 " +
			  " AND OF.order_count < 0 " +
			  " AND OF.operation = " + OrderFood.Operation.CANCEL.getVal() +
			  extraCond;
		
		return sql;
	}
}
