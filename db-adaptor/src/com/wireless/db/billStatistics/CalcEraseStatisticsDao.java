package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.DBTbl;
import com.wireless.db.Params;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.erase.EraseIncomeByEachDay;
import com.wireless.pojo.billStatistics.erase.EraseIncomeByStaff;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.SortedList;

public class CalcEraseStatisticsDao {
	public static class ExtraCond implements Cloneable{
		
		private final DBTbl dbTbl;
		
		private int staffId;
		private HourRange hourRange;
		private DutyRange dutyRange;
		private Department.DeptId deptId;
		
		private boolean isChain;     //是否连锁
		private boolean calcByDuty;  //是否按日结区间计算
		
		private Staff staff;
		
		public ExtraCond(DateType dateType){
			this.dbTbl = new DBTbl(dateType);
		}
		
		public ExtraCond setChain(boolean onOff){
			this.isChain = onOff;
			return this;
		}
		
		public ExtraCond setCalcByDuty(boolean onOff){
			this.calcByDuty = onOff;
			return this;
		}
		
		public ExtraCond setDutyRange(DutyRange range){
			this.dutyRange = range;
			return this;
		}
		
		public ExtraCond setStaff(Staff staff){
			this.staff = staff;
			return this;
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
	    public Object clone() {   
	        try {   
	            return super.clone();   
	        } catch (CloneNotSupportedException e) {   
	            return null;   
	        }   
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
			if(dutyRange != null){
				DutyRange range;
				if(calcByDuty){
					try{
						range = DutyRangeDao.exec(staff, dutyRange);
					}catch(SQLException e){
						e.printStackTrace();
						range = dutyRange;
					}
				}else{
					range = dutyRange;
				}
				
				if(range != null){
					extraCond.append(" AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'");
				}else{
					extraCond.append(" AND 0 ");
				}
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Get erase detail list by condition.
	 * @param staff
	 * @param queryType
	 * @return	the erase list
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<Order> getDetail(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDetail(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<Order> getDetail(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		final List<Order> result = new ArrayList<Order>();
		
		if(extraCond.isChain){
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			
			//Append the erase detail to the group
			result.addAll(getDetail(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false)));
			
			//Append the erase details to each branch
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.addAll(getDetail(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false)));
			}
			
		}else{
			String sql = makeSql4Erase(staff, extraCond);
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				Order o = new Order();
				o.setId(dbCon.rs.getInt("id"));
				o.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
				o.setErasePrice(dbCon.rs.getInt("erase_price"));
				o.setActualPrice(dbCon.rs.getFloat("actual_price"));
				o.setWaiter(dbCon.rs.getString("waiter"));
				o.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				o.setComment(dbCon.rs.getString("comment"));
				o.setRestaurantName(dbCon.rs.getString("restaurant_name"));
				Table t = new Table(dbCon.rs.getInt("table_id"));
				t.setTableAlias(dbCon.rs.getInt("table_alias"));
				t.setTableName(dbCon.rs.getString("table_name"));
				o.setDestTbl(t);
				result.add(o);
			}
			dbCon.rs.close();
		}
		
		return result;
		
	}
	/**
	 * Calculate the erase income by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link EraseIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<EraseIncomeByEachDay> calcIncomeByEachDay(Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByEachDay(dbCon, staff, extraCond);
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
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link EraseIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<EraseIncomeByEachDay> calcIncomeByEachDay(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		
		if(extraCond.isChain){
			final Map<DutyRange, EraseIncomeByEachDay> chainResult = new HashMap<>();
			//Append the erase income by each day to the group.
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()); 
			for(EraseIncomeByEachDay groupIncome : calcIncomeByEachDay(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				chainResult.put(groupIncome.getRange(), groupIncome);
			}
			
			//Append the erase income by each day to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				for(EraseIncomeByEachDay branchIncome : calcIncomeByEachDay(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					if(chainResult.containsKey(branchIncome.getRange())){
						EraseIncomeByEachDay eraseIncome = chainResult.get(branchIncome.getRange());
						final float eraseAmount = branchIncome.getEraseAmount() + eraseIncome.getEraseAmount();
						final float erasePrice = branchIncome.getErasePrice() + eraseIncome.getErasePrice();
						chainResult.put(eraseIncome.getRange(), new EraseIncomeByEachDay(eraseIncome.getRange(), eraseAmount, erasePrice));
					}else{
						chainResult.put(branchIncome.getRange(), branchIncome);
					}
				}
			}
			
			return SortedList.newInstance(chainResult.values());
			
		}else{
			List<EraseIncomeByEachDay> result = new ArrayList<EraseIncomeByEachDay>();
			
			Calendar c = Calendar.getInstance();
			Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOnDutyFormat());
			Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOffDutyFormat());
			c.setTime(dateBegin);
			while (dateBegin.compareTo(dateEnd) <= 0) {
				c.add(Calendar.DATE, 1);
				
				final DutyRange range = new DutyRange(dateBegin.getTime(), c.getTimeInMillis());
				String sql;
				sql = " SELECT " +
					  " COUNT(*) AS erase_amount, " +
					  " ROUND(SUM(TMP.erase_price), 2) AS erase_price " +
				      " FROM (" +
				      makeSql4Erase(staff, ((ExtraCond)extraCond.clone()).setDutyRange(range).setCalcByDuty(true)) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new EraseIncomeByEachDay(range,
														 dbCon.rs.getFloat("erase_amount"),
														 dbCon.rs.getFloat("erase_price")));
				}else{
					result.add(new EraseIncomeByEachDay(range, 0, 0)); 
				}
				dbCon.rs.close();

				dateBegin = c.getTime();
			}
			
			return result;
		}
		
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
	 * @throws BusinessException 
	 */
	public static List<EraseIncomeByStaff> calcIncomeByStaff(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByStaff(dbCon, staff, extraCond);
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
	 * @throws BusinessException 
	 */
	public static List<EraseIncomeByStaff> calcIncomeByStaff(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		List<EraseIncomeByStaff> result = new ArrayList<EraseIncomeByStaff>();
		if(extraCond.isChain){
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the erase income by staff to the group.
			result.addAll(calcIncomeByStaff(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false)));
			
			//Append erase income by staff to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.addAll(calcIncomeByStaff(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false)));
			}
		}else{
			String sql;
			sql = " SELECT " +
				  " TMP.staff_id, MAX(IFNULL(TMP.waiter, '其他')) AS staff_name, " +
				  " COUNT(*) AS erase_amount, " +
				  " ROUND(SUM(TMP.erase_price), 2) AS erase_price " +
				  " FROM (" +
				  makeSql4Erase(staff, extraCond) +
				  " ) AS TMP " +
				  " GROUP BY TMP.staff_id ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				result.add(new EraseIncomeByStaff(dbCon.rs.getString("staff_name"), 
												   dbCon.rs.getFloat("erase_amount"), 
												   dbCon.rs.getFloat("erase_price")));
			}
			dbCon.rs.close();
		}
		
		
		return result;
	}
	
	private static String makeSql4Erase(Staff staff, ExtraCond extraCond){
		String sql;
		sql = " SELECT " +
			  "  R.restaurant_name, O.id, O.restaurant_id, O.order_date, O.waiter, O.staff_id, O.erase_price, O.actual_price, O.comment, O.table_alias, O.table_name, O.table_id, D.name, D.dept_id, D.type " +
			  " FROM " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
			  " JOIN " + Params.dbName + "." + extraCond.dbTbl.orderFoodTbl + " OF " +
			  " ON OF.order_id = O.id " +
			  " JOIN " + Params.dbName + ".department D ON D.dept_id = OF.dept_id AND D.restaurant_id = OF.restaurant_id " +
			  " JOIN " + Params.dbName + ".restaurant R ON O.restaurant_id = R.id" +
			  " WHERE 1 = 1 " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " AND O.erase_price <> 0 " +
			  (extraCond != null ? extraCond.setStaff(staff) : "") +
			  " GROUP BY O.id ";
		
		return sql;
	}
}
