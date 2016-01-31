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
import com.wireless.pojo.billStatistics.gift.GiftIncomeByDept;
import com.wireless.pojo.billStatistics.gift.GiftIncomeByEachDay;
import com.wireless.pojo.billStatistics.gift.GiftIncomeByStaff;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;

public class CalcGiftStatisticsDao {

	public static class ExtraCond{
		private final DateType dateType;
		
		private final String orderTbl;
		//private final String orderTblAlias = "O";
		private final String orderFoodTbl;
		//private final String orderFoodTblAlias = "OF";
		private final String tasteGroupTbl;
		
		private Department.DeptId deptId;
		private int staffId;
		private Region.RegionId regionId;
		private HourRange hourRange;
		private String foodName;
		
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
		
		public ExtraCond setRegionId(Region.RegionId regionId){
			this.regionId = regionId;
			return this;
		}
		
		public ExtraCond setFoodName(String foodName){
			this.foodName = foodName;
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
			if(regionId != null){
				extraCond.append(" AND O.region_id = " + regionId.getId());
			}
			if(foodName != null){
				extraCond.append(" AND OF.name LIKE '%" + foodName + "%'");
			}
			if(hourRange != null){
				extraCond.append(" AND TIME(O.order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Calculate the gift income by staff according to duty range and extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link GiftIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<GiftIncomeByStaff> calcGiftIncomeByStaff(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcGiftIncomeByStaff(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the gift income by staff according to duty range and extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link GiftIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<GiftIncomeByStaff> calcGiftIncomeByStaff(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " TMP.staff_id, MAX(TMP.waiter) AS staff_name, " +
			  " ROUND(SUM(TMP.gift_amount), 2) AS gift_amount, " +
			  " ROUND(SUM(TMP.gift_price), 2) AS gift_price " +
			  " FROM (" +
			  makeSql4GiftFood(staff, range, extraCond) +
			  " ) AS TMP " +
			  " GROUP BY TMP.staff_id ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<GiftIncomeByStaff> result = new ArrayList<GiftIncomeByStaff>();
		while(dbCon.rs.next()){
			result.add(new GiftIncomeByStaff(dbCon.rs.getString("staff_name"), 
											   dbCon.rs.getFloat("gift_amount"), 
											   dbCon.rs.getFloat("gift_price")));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Calculate the gift income according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link GiftIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<GiftIncomeByDept> calcGiftIncomeByDept(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcGiftIncomeByDept(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the gift income by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link GiftIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<GiftIncomeByEachDay> calcGiftIncomeByEachDay(Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcGiftIncomeByEachDay(dbCon, staff, dutyRange, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the gift income by each day according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link GiftIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 */
	public static List<GiftIncomeByEachDay> calcGiftIncomeByEachDay(DBCon dbCon, Staff staff, DutyRange dutyRange, ExtraCond extraCond) throws SQLException, ParseException{
		List<GiftIncomeByEachDay> result = new ArrayList<GiftIncomeByEachDay>();
		
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
					  " ROUND(SUM(TMP.gift_amount), 2) AS gift_amount, " +
					  " ROUND(SUM(TMP.gift_price), 2) AS gift_price " +
				      " FROM (" +
					  makeSql4GiftFood(staff, range, extraCond) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new GiftIncomeByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()),
														 dbCon.rs.getFloat("gift_amount"),
														 dbCon.rs.getFloat("gift_price")));
				}
				dbCon.rs.close();

			}else{
				result.add(new GiftIncomeByEachDay(new DutyRange(dateBegin.getTime(), c.getTimeInMillis()), 0, 0)); 
			}
			
			dateBegin = c.getTime();
		}
		
		return result;
	}
	
	/**
	 * Calculate the gift income according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link GiftIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<GiftIncomeByDept> calcGiftIncomeByDept(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		List<GiftIncomeByDept> result = new ArrayList<GiftIncomeByDept>();
		
		String sql;
		sql = " SELECT " +
			  " D.dept_id, MAX(D.name) AS dept_name, D.type, " +
			  " ROUND(SUM(TMP.gift_amount), 2) AS gift_amount, " +
			  " ROUND(SUM(TMP.gift_price), 2) AS gift_price " +
		      " FROM (" +
			  makeSql4GiftFood(staff, range, extraCond) +
			  " ) AS TMP " +
			  " JOIN " + Params.dbName + ".department D ON D.dept_id = TMP.dept_id AND D.restaurant_id = " + staff.getRestaurantId() +
			  " GROUP BY TMP.dept_id ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			Department dept = new Department(dbCon.rs.getInt("dept_id"));
			dept.setType(dbCon.rs.getInt("type"));
			if(dept.isIdle()){
				dept.setName(dept.getName().isEmpty() ? "已删除部门" : dept.getName() + "(已删除)");
			}else{
				dept.setName(dbCon.rs.getString("dept_name"));
			}
			result.add(new GiftIncomeByDept(dept, dbCon.rs.getFloat("gift_amount"), dbCon.rs.getFloat("gift_price")));
		}
		dbCon.rs.close();
		
		return result;
	}
	
	
	
	private static String makeSql4GiftFood(Staff staff, DutyRange range, ExtraCond extraCond){
		String sql;
		sql = (" SELECT " +
			  " OF.dept_id, OF.staff_id, OF.waiter, " +
			  " ABS(OF.order_count) AS gift_amount, " +
			  " (($(unit_price) + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * OF.order_count) AS gift_price " +
			  " FROM " + Params.dbName + "." + extraCond.orderFoodTbl + " OF " + 
			  " JOIN " + Params.dbName + "." + extraCond.orderTbl + " O " +
			  " ON 1 = 1 " +
			  " AND OF.order_id = O.id " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " JOIN " + Params.dbName + "." + extraCond.tasteGroupTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
			  " WHERE 1 = 1 " +
			  " AND OF.is_gift = 1 " +
			  extraCond).replace("$(unit_price)", "IFNULL(OF.plan_price, IFNULL(OF.food_unit_price, OF.unit_price))");
		
		return sql;
	}
}
