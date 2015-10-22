package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.erase.EraseIncomeByEachDay;
import com.wireless.pojo.billStatistics.erase.EraseIncomeByStaff;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class CalcEraseStatisticsDao {
	public static class ExtraCond{
		
		private final DBTbl dbTbl;
		
		private int staffId;
		private HourRange hourRange;
		private Department.DeptId deptId;
		
		public ExtraCond(DateType dateType){
			this.dbTbl = new DBTbl(dateType);
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
	 * Get erase detail list by condition.
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return	the erase list
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<Order> getEraseStatisticsDetail(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getEraseStatisticsDetail(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<Order> getEraseStatisticsDetail(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException, BusinessException{
		String sql = makeSql4Erase(staff, range, extraCond);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<Order> result = new ArrayList<Order>();
		while(dbCon.rs.next()){
			Order o = new Order();
			o.setId(dbCon.rs.getInt("id"));
			o.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			o.setErasePrice(dbCon.rs.getInt("erase_price"));
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
	 * Calculate the erase income by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link EraseIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<EraseIncomeByEachDay> calcEraseIncomeByEachDay(Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcEraseIncomeByEachDay(dbCon, staff, dutyRange, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the erase income by each day according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link EraseIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<EraseIncomeByEachDay> calcEraseIncomeByEachDay(DBCon dbCon, Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		
		List<EraseIncomeByEachDay> result = new ArrayList<EraseIncomeByEachDay>();
		
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
					  " COUNT(*) AS erase_amount, " +
					  " ROUND(SUM(TMP.erase_price), 2) AS erase_price " +
				      " FROM (" +
				      makeSql4Erase(staff, range, extraCond) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new EraseIncomeByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()),
														 dbCon.rs.getFloat("erase_amount"),
														 dbCon.rs.getFloat("erase_price")));
				}
				dbCon.rs.close();

			}else{
				result.add(new EraseIncomeByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()), 0, 0)); 
			}
			
			dateBegin = c.getTime();
		}
		
		return result;
	}
	
	/**
	 * Calculate the erase income by staff according to duty range and extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link EraseIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<EraseIncomeByStaff> calcEraseIncomeByStaff(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcEraseIncomeByStaff(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the erase income by staff according to duty range and extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link EraseIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<EraseIncomeByStaff> calcEraseIncomeByStaff(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " TMP.staff_id, MAX(IFNULL(TMP.waiter, '其他')) AS staff_name, " +
			  " COUNT(*) AS erase_amount, " +
			  " ROUND(SUM(TMP.erase_price), 2) AS erase_price " +
			  " FROM (" +
			  makeSql4Erase(staff, range, extraCond) +
			  " ) AS TMP " +
			  " GROUP BY TMP.staff_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<EraseIncomeByStaff> result = new ArrayList<EraseIncomeByStaff>();
		while(dbCon.rs.next()){
			result.add(new EraseIncomeByStaff(dbCon.rs.getString("staff_name"), 
											   dbCon.rs.getFloat("erase_amount"), 
											   dbCon.rs.getFloat("erase_price")));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	private static String makeSql4Erase(Staff staff, DutyRange range, ExtraCond extraCond){
		String sql;
		sql = " SELECT " +
			  " O.id, O.order_date, O.waiter, O.staff_id, O.erase_price, O.actual_price, O.comment, O.table_alias, O.table_name, O.table_id, D.name, D.dept_id, D.type " +
			  " FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
			  " JOIN " + Params.dbName + "." + extraCond.dbTbl.orderFoodTbl + " OF " +
			  " ON OF.order_id = O.id " +
			  " JOIN " + Params.dbName + ".department D ON D.dept_id = OF.dept_id AND D.restaurant_id = OF.restaurant_id " +
			  " WHERE 1 = 1 " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND O.erase_price <> 0 " +
			  extraCond +
			  " GROUP BY O.id ";
		
		return sql;
	}
}
