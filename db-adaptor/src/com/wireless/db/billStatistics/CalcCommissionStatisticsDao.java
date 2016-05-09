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
import com.wireless.db.Params;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.billStatistics.commission.CommissionIncomeByEachDay;
import com.wireless.pojo.billStatistics.commission.CommissionIncomeByStaff;
import com.wireless.pojo.billStatistics.commission.CommissionStatistics;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.SortedList;

public class CalcCommissionStatisticsDao {
	public static class ExtraCond implements Cloneable{
		private final DateType dateType;
		
		private final String orderTbl;
		//private final String orderTblAlias = "O";
		private final String orderFoodTbl;
		//private final String orderFoodTblAlias = "OF";
		
		private int staffId;
		private Department.DeptId deptId;
		private HourRange hourRange;
		private DutyRange dutyRange;
		
		private boolean isChain;			//是否连锁
		private boolean calcByDuty;			//是否按日结区间计算
		
		private Staff staff;
		
		public ExtraCond setChain(boolean onOff){
			this.isChain = onOff;
			return this;
		}
		
		public ExtraCond setStaff(Staff staff){
			this.staff = staff;
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
		
		public ExtraCond setDeptId(Department.DeptId deptId){
			this.deptId = deptId;
			return this;
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
				extraCond.append(" AND OF.staff_id = " + staffId);
			}
			if(dutyRange != null){
				DutyRange range;
				if(calcByDuty){
					try {
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
	 * Get commission list by condition.
	 * @param staff
	 * @param range
	 * @param queryType
	 * @return	the commission list
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<CommissionStatistics> getCommissionStatisticsDetail(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getCommissionStatisticsDetail(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<CommissionStatistics> getCommissionStatisticsDetail(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		List<CommissionStatistics> result = new ArrayList<CommissionStatistics>();
		
		if(extraCond.isChain){
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			
			//Append the commission to the group
			result.addAll(getCommissionStatisticsDetail(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false)));
			
			//Append the commission to the branch
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				
				result.addAll(getCommissionStatisticsDetail(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false)));
			}
			
		}else{
			String sql = makeSql4Commission(staff, extraCond);
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
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
		}
		
	
		
		return result;
		
	}
	
	/**
	 * Calculate the commission income by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CommissionIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<CommissionIncomeByEachDay> calcCommissionIncomeByEachDay(Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCommissionIncomeByEachDay(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the commission income by each day according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CommissionIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<CommissionIncomeByEachDay> calcCommissionIncomeByEachDay(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		
		if(extraCond.isChain){
			final Map<DutyRange, CommissionIncomeByEachDay> chainResult = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			for(CommissionIncomeByEachDay groupIncome : calcCommissionIncomeByEachDay(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				chainResult.put(groupIncome.getRange(), groupIncome);
			}
			
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				for(CommissionIncomeByEachDay branchIncome : calcCommissionIncomeByEachDay(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					if(chainResult.containsKey(branchIncome.getRange())){
						CommissionIncomeByEachDay commissionIncome = chainResult.get(branchIncome.getRange());
						final float commissionAmount = branchIncome.getmCommissionAmount() + commissionIncome.getmCommissionAmount();
						final float commissionPrice = branchIncome.getmCommissionPrice() + commissionIncome.getmCommissionPrice();
						chainResult.put(branchIncome.getRange(), new CommissionIncomeByEachDay(branchIncome.getRange(), commissionAmount, commissionPrice));
					}else{
						chainResult.put(branchIncome.getRange(), branchIncome);
					}
				}
			}
			
			return SortedList.newInstance(chainResult.values());
		}else{
			List<CommissionIncomeByEachDay> result = new ArrayList<CommissionIncomeByEachDay>();
			
			Calendar c = Calendar.getInstance();
			Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOnDutyFormat());
			Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOffDutyFormat());
			c.setTime(dateBegin);
			while (dateBegin.compareTo(dateEnd) <= 0) {
				c.add(Calendar.DATE, 1);
				
				
				final DutyRange range = new DutyRange(dateBegin.getTime(), c.getTimeInMillis());
				String sql;
				sql = " SELECT " +
					  " ROUND(SUM(TMP.order_count), 2) AS commission_amount, " +
					  " ROUND(SUM(TMP.commission), 2) AS commission_price " +
				      " FROM (" +
				      makeSql4Commission(staff, ((ExtraCond)extraCond.clone()).setDutyRange(range).setCalcByDuty(true)) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new CommissionIncomeByEachDay(range,
														 dbCon.rs.getFloat("commission_amount"),
														 dbCon.rs.getFloat("commission_price")));
				}else{
					result.add(new CommissionIncomeByEachDay(range, 0, 0)); 
				}
				dbCon.rs.close();

				dateBegin = c.getTime();
			}
				
			
			return result;
			
		}
		
		
		
	}
	
	/**
	 * Calculate the commission income by staff according to duty range and extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link CommissionIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<CommissionIncomeByStaff> calcCommissionIncomeByStaff(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCommissionIncomeByStaff(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the commission income by staff according to duty range and extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link CommissionIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<CommissionIncomeByStaff> calcCommissionIncomeByStaff(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		List<CommissionIncomeByStaff> result = new ArrayList<CommissionIncomeByStaff>();
		
		if(extraCond.isChain){
			
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			
			//Append the commission to the group
			result.addAll(calcCommissionIncomeByStaff(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false)));
			
			//Append the commission to the branch
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.addAll(calcCommissionIncomeByStaff(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false)));
			}
			
		}else{
			String sql;
			sql = " SELECT " +
				  " TMP.staff_id, MAX(TMP.waiter) AS staff_name, " +
				  " ROUND(SUM(TMP.order_count), 2) AS commission_amount, " +
				  " ROUND(SUM(TMP.commission), 2) AS commission_price " +
				  " FROM (" +
				  makeSql4Commission(staff, extraCond) +
				  " ) AS TMP " +
				  " GROUP BY TMP.staff_id ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			
			while(dbCon.rs.next()){
				result.add(new CommissionIncomeByStaff(dbCon.rs.getString("staff_name"), 
												   dbCon.rs.getFloat("commission_amount"), 
												   dbCon.rs.getFloat("commission_price")));
			}
			dbCon.rs.close();
			
		}
		
		
		return result;
	}
	
	private static String makeSql4Commission(Staff staff, ExtraCond extraCond){
		String sql;
		sql = " SELECT " +
			  " OF.order_id, OF.order_date, OF.name, OF.unit_price, OF.order_count, OF.waiter, OF.staff_id, " +
			  " ROUND((OF.unit_price * OF.order_count), 2) AS total_price, " +
			  " ROUND((OF.commission * OF.order_count), 2) AS commission, " +
			  " D.dept_id, D.restaurant_id, D.name AS dept_name " +
			  " FROM " + Params.dbName + "." + extraCond.orderFoodTbl + " OF " + 
			  " JOIN " + Params.dbName + "." + extraCond.orderTbl + " O " +
			  " ON OF.order_id = O.id " +
			  " JOIN " + Params.dbName + ".department D ON D.dept_id = OF.dept_id AND D.restaurant_id = OF.restaurant_id " +
			  " WHERE 1 = 1 " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " AND (OF.food_status & " + Food.COMMISSION + ") <> 0 " +
			  //" AND OF.commission <> 0 " +
			  (extraCond != null ? extraCond.setStaff(staff) : "");
		
		return sql;
	}

	
}
