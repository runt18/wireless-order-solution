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
import com.wireless.pojo.billStatistics.cancel.CancelDetail;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByEachDay;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByFood;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByStaff;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.SortedList;

public class CalcCancelStatisticsDao {
	
	public static class ExtraCond implements Cloneable{
		private final DBTbl dbTbl;
		
		private Department.DeptId deptId;
		private int staffId;
		private int reasonId;
		private Region.RegionId regionId;
		private DutyRange dutyRange;
		private HourRange hourRange;
		private String foodName;
		private int orderId;
		private boolean skipCancelPrice;
		
		private boolean isChain;		//是否连锁
		private boolean calcByDuty;	//是否按日结区间计算
		
		private Staff staff;
		
		public ExtraCond(DateType dateType){
			this.dbTbl = new DBTbl(dateType);
		}
		
		private ExtraCond setStaff(Staff staff){
			this.staff = staff;
			return this;
		}
		
		public ExtraCond setCalcByDuty(boolean onOff){
			this.calcByDuty = onOff;
			return this;
		}
		
		public ExtraCond setChain(boolean onOff){
			this.isChain = onOff;
			return this;
		}
		
		public ExtraCond setFoodName(String foodName){
			this.foodName = foodName;
			return this;
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
		
		public ExtraCond setRange(DutyRange range){
			this.dutyRange = range;
			return this;
		}
		
		public ExtraCond setHourRange(HourRange hourRange){
			this.hourRange = hourRange;
			return this;
		}
		
		public ExtraCond setOrder(int orderId){
			this.orderId = orderId;
			return this;
		}
		
		public ExtraCond setOrder(Order order){
			this.orderId = order.getId();
			return this;
		}
		
		ExtraCond setSkipCancelPrice(boolean skip){
			this.skipCancelPrice = skip;
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
			if(this.dutyRange != null){
				DutyRange range;
				if(this.calcByDuty){
					try {
						range = DutyRangeDao.exec(staff, dutyRange);
					} catch (SQLException e) {
						range = this.dutyRange;
						e.printStackTrace();
					}
				}else{
					range = this.dutyRange;
				}
				if(range != null){
					extraCond.append(" AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'");
				}else{
					extraCond.append(" AND 0 ");
				}
			}
			if(hourRange != null){
				extraCond.append(" AND TIME(O.order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			if(foodName != null){
				extraCond.append(" AND OF.name LIKE '%" + foodName + "%'");
			}
			if(orderId != 0){
				extraCond.append(" AND O.id = " + orderId);
			}
			if(!skipCancelPrice){
				extraCond.append(" AND O.cancel_price <> 0 ");
			}
			return extraCond.toString();
		}
	}
	
	
	/**
	 * Get cancel detail list to extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return	the cancel detail to this extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<CancelDetail> getDetail(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDetail(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get cancel detail list to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return	the cancel detail to this extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<CancelDetail> getDetail(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		final List<CancelDetail> result = new ArrayList<CancelDetail>();
		
		if(extraCond.isChain){
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			
			//Append the cancel detail to the group
			result.addAll(getDetail(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false)));
			
			//Append the cancel detail to the branch
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.addAll(getDetail(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false)));
			}
			
		}else{
			String sql = makeSql4CancelFood(staff, extraCond);
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				CancelDetail c = new CancelDetail();
				c.setCancelCount(dbCon.rs.getFloat("cancel_amount"));
				c.setCancelReason(dbCon.rs.getString("cancel_reason"));
				c.setDept(dbCon.rs.getString("dept_name"));
				c.setName(dbCon.rs.getString("name"));
				c.setOrderDateFormat(dbCon.rs.getTimestamp("order_date").getTime());
				c.setOrderId(dbCon.rs.getInt("order_id"));
				c.setUnitPrice(dbCon.rs.getFloat("unit_price"));
				c.setWaiter(dbCon.rs.getString("waiter"));
				c.setTotalAmount(dbCon.rs.getFloat("cancel_amount"));
				c.setTotalCancel(dbCon.rs.getFloat("cancel_price"));
				c.setRid(dbCon.rs.getInt("restaurant_id"));
				c.setRestaurantName(dbCon.rs.getString("restaurant_name"));
				result.add(c);
			}
			
			dbCon.rs.close();
		}
		return result;
	}
	
	/**
	 * Calculate the cancel income by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<CancelIncomeByEachDay> calcIncomeByEachDay(Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByEachDay(dbCon, staff, extraCond);
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
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<CancelIncomeByEachDay> calcIncomeByEachDay(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		
		if(extraCond.isChain){
			Map<DutyRange, CancelIncomeByEachDay> result = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the cancel income by each day to the group.
			for(CancelIncomeByEachDay groupIncome : calcIncomeByEachDay(dbCon, staff, ((ExtraCond)extraCond.clone()).setChain(false))){
				result.put(groupIncome.getDutyRange(), groupIncome);
			}
			
			//Append cancel income by each day to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				for(CancelIncomeByEachDay branchIncome : calcIncomeByEachDay(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					CancelIncomeByEachDay cancelIncome = result.get(branchIncome.getDutyRange());
					if(cancelIncome != null){
						final float cancelAmount = branchIncome.getCancelAmount() + cancelIncome.getCancelAmount();
						final float cancelPrice = branchIncome.getCancelPrice() + cancelIncome.getCancelPrice();
						result.put(cancelIncome.getDutyRange(), new CancelIncomeByEachDay(cancelIncome.getDutyRange(), cancelAmount, cancelPrice));
					}else{
						result.put(branchIncome.getDutyRange(), branchIncome);
					}
				}
			}
			
			return SortedList.newInstance(result.values());
		}else{
			
			List<CancelIncomeByEachDay> result = new ArrayList<CancelIncomeByEachDay>();
			Calendar c = Calendar.getInstance();
			Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOnDutyFormat());
			Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOffDutyFormat());
			c.setTime(dateBegin);
			while (dateBegin.compareTo(dateEnd) <= 0) {
				c.add(Calendar.DATE, 1);
				
				DutyRange rangeByEachDay = new DutyRange(dateBegin.getTime(), c.getTimeInMillis());
				String sql;
				sql = " SELECT " +
					  " ROUND(SUM(TMP.cancel_amount), 2) AS cancel_amount, " +
					  " ROUND(SUM(TMP.cancel_price), 2) AS cancel_price " +
				      " FROM (" +
					  makeSql4CancelFood(staff, ((ExtraCond)extraCond.clone()).setCalcByDuty(true).setRange(rangeByEachDay)) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new CancelIncomeByEachDay(rangeByEachDay,
														 dbCon.rs.getFloat("cancel_amount"),
														 dbCon.rs.getFloat("cancel_price")));
				}else{
					result.add(new CancelIncomeByEachDay(rangeByEachDay, 0, 0)); 
				}
				dbCon.rs.close();
				
				dateBegin = c.getTime();
			}
			return result;
		}
		
	}
	
	/**
	 * Calculate the cancel income by department according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<CancelIncomeByDept> calcIncomeByDept(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByDept(dbCon, staff, extraCond);
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
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<CancelIncomeByDept> calcIncomeByDept(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		
		
		if(extraCond.isChain){
			
			Map<String, CancelIncomeByDept> result = new HashMap<>();
			
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the cancel income by department to the group.
			for(CancelIncomeByDept groupIncome : calcIncomeByDept(dbCon, staff, ((ExtraCond)extraCond.clone()).setChain(false).setCalcByDuty(true))){
				result.put(groupIncome.getDepartment().getName(), groupIncome);
			}
			
			//Append cancel income by department to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				for(CancelIncomeByDept branchIncome : calcIncomeByDept(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false).setCalcByDuty(true))){
					CancelIncomeByDept income = result.get(branchIncome.getDepartment().getName());
					if(income != null){
						final float cancelPrice = income.getCancelPrice() + branchIncome.getCancelPrice();
						final float cancelAmount = income.getCancelAmount() + branchIncome.getCancelAmount();
						result.put(income.getDepartment().getName(), new CancelIncomeByDept(income.getDepartment(), cancelAmount, cancelPrice));
					}else{
						result.put(branchIncome.getDepartment().getName(), branchIncome);
					}
				}
			}
			
			return new ArrayList<>(result.values());
			
		}else{
			
			final List<CancelIncomeByDept> result = new ArrayList<CancelIncomeByDept>();
			String sql;
			sql = " SELECT " +
				  " D.dept_id, MAX(D.name) AS dept_name, D.type, " +
				  " ROUND(SUM(TMP.cancel_amount), 2) AS cancel_amount, " +
				  " ROUND(SUM(TMP.cancel_price), 2) AS cancel_price " +
				  " FROM (" +
				  makeSql4CancelFood(staff, extraCond) +
				  " ) AS TMP " +
				  " JOIN " + Params.dbName + ".department D ON TMP.dept_id = D.dept_id AND D.restaurant_id = " + staff.getRestaurantId() + 
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
				result.add(new CancelIncomeByDept(dept, 
												  dbCon.rs.getFloat("cancel_amount"), 
												  dbCon.rs.getFloat("cancel_price")));
			}
			dbCon.rs.close();
			
			return result;
		}
		
	}
	
	/**
	 * Calculate the cancel income by staff according to duty range and extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param dutyRange
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link CancelIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<CancelIncomeByStaff> calcIncomeByStaff(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByStaff(dbCon, staff, extraCond);
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
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link CancelIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<CancelIncomeByStaff> calcIncomeByStaff(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		
		List<CancelIncomeByStaff> result = new ArrayList<CancelIncomeByStaff>();
		
		if(extraCond.isChain){
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the cancel income by staff to the group.
			result.addAll(calcIncomeByStaff(dbCon, staff, ((ExtraCond)extraCond.clone()).setChain(false).setCalcByDuty(true)));
			
			//Append cancel income by staff to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.addAll(calcIncomeByStaff(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false).setCalcByDuty(true)));
			}
			
		}else{
			
			String sql;
			sql = " SELECT " +
				  " TMP.staff_id, MAX(TMP.waiter) AS staff_name, " +
				  " ROUND(SUM(TMP.cancel_amount), 2) AS cancel_amount, " +
				  " ROUND(SUM(TMP.cancel_price), 2) AS cancel_price " +
				  " FROM (" +
				  makeSql4CancelFood(staff, extraCond) +
				  " ) AS TMP " +
				  " GROUP BY TMP.staff_id ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				result.add(new CancelIncomeByStaff(dbCon.rs.getString("staff_name"), 
												   dbCon.rs.getFloat("cancel_amount"), 
												   dbCon.rs.getFloat("cancel_price")));
			}
			dbCon.rs.close();
		}
		

		
		return result;
	}
	
	/**
	 * Calculate the cancel income by reason according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByReason}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<CancelIncomeByReason> calcIncomeByReason(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByReason(dbCon, staff, extraCond);
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
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link CancelIncomeByReason}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<CancelIncomeByReason> calcIncomeByReason(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		
		if(extraCond.isChain){
			Map<String, CancelIncomeByReason> result = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the cancel income by reason to group.
			for(CancelIncomeByReason groupIncome : calcIncomeByReason(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				result.put(groupIncome.getCancelReason().getReason(), groupIncome);
			}

			//Append the cancel income by reason to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				for(CancelIncomeByReason branchIncome : calcIncomeByReason(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					CancelIncomeByReason cancelIncome = result.get(branchIncome.getCancelReason().getReason());
					if(cancelIncome != null){
						final float cancelPrice = cancelIncome.getCancelPrice() + branchIncome.getCancelPrice();
						final float cancelAmount = cancelIncome.getCancelAmount() + branchIncome.getCancelAmount();
						result.put(branchIncome.getCancelReason().getReason(), new CancelIncomeByReason(cancelIncome.getCancelReason(), cancelAmount, cancelPrice));
					}else{
						result.put(branchIncome.getCancelReason().getReason(), branchIncome);
					}
				}
			}
			
			return new ArrayList<>(result.values());
			
		}else{
			
			List<CancelIncomeByReason> result = new ArrayList<CancelIncomeByReason>();
			String sql;
			sql = " SELECT " +
				  " TMP.cancel_reason_id, " +
				  " MAX(TMP.cancel_reason) AS cancel_reason, " +
				  " ROUND(SUM(TMP.cancel_amount), 2) AS cancel_amount, " +
				  " ROUND(SUM(TMP.cancel_price), 2) AS cancel_price " +
				  " FROM (" +
				  makeSql4CancelFood(staff, extraCond) +
				  " ) AS TMP " +
				  " GROUP BY TMP.cancel_reason_id ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			
			while(dbCon.rs.next()){
				result.add(new CancelIncomeByReason(new CancelReason(dbCon.rs.getInt("cancel_reason_id"), dbCon.rs.getString("cancel_reason"), staff.getRestaurantId()), 
												    dbCon.rs.getFloat("cancel_amount"), 
												    dbCon.rs.getFloat("cancel_price")));
			}
			dbCon.rs.close();
			
			return result;
		}
		
	}
	
	/**
	 * Calculate the cancel income by food according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to cancel income by each food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CancelIncomeByFood> calcIncomeByFood(Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcCancelIncomeByFood(dbCon, staff, range, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the cancel income by food according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param range
	 * 			the duty range
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to cancel income by each food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<CancelIncomeByFood> calcCancelIncomeByFood(DBCon dbCon, Staff staff, DutyRange range, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " MAX(TMP.name) AS food_name, " +
			  " ROUND(SUM(TMP.cancel_amount), 2) AS cancel_amount, " +
			  " ROUND(SUM(TMP.cancel_price), 2) AS cancel_price " +
			  " FROM (" +
			  makeSql4CancelFood(staff, extraCond.setRange(range)) +
			  " ) AS TMP " +
			  " GROUP BY TMP.food_id " +
			  " ORDER BY cancel_amount DESC ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<CancelIncomeByFood> result = new ArrayList<CancelIncomeByFood>();
		while(dbCon.rs.next()){
			result.add(new CancelIncomeByFood(dbCon.rs.getString("food_name"),
											  dbCon.rs.getFloat("cancel_amount"), 
											  dbCon.rs.getFloat("cancel_price")));
		}
		dbCon.rs.close();
		
		return result;
	}

	/**
	 * Calculate the cancel price & income to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result stored in float[2], cancel_amoun = float[0], cancel_price = float[1] 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static float[] calcByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the cancel price & income to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result stored in float[2], cancel_amoun = float[0], cancel_price = float[1] 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static float[] calcByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		
		final float[] result = new float[2];
		
		if(extraCond.isChain){
			
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the cancel result to the group.
			final float[] result4Group = calcByCond(dbCon, staff, ((ExtraCond)extraCond.clone()).setChain(false).setCalcByDuty(true));
			result[0] += result4Group[0];
			result[1] += result4Group[1];
			
			//Append cancel result to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				final float[] result4Branch = calcByCond(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false).setCalcByDuty(true));
				result[0] += result4Branch[0];
				result[1] += result4Branch[1];
			}
			
		}else{
			
			String sql;
			sql = " SELECT " +
				  " ROUND(SUM(TMP.cancel_amount), 2) AS cancel_amount, " +
				  " ROUND(SUM(TMP.cancel_price), 2) AS cancel_price " +
				  " FROM (" +
				  makeSql4CancelFood(staff, extraCond.setSkipCancelPrice(true).setStaff(staff)) +
				  " ) AS TMP ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				result[0] = dbCon.rs.getFloat("cancel_amount");
				result[1] = dbCon.rs.getFloat("cancel_price");
			}
			dbCon.rs.close();
			
		}
		
		return result;
	}
	
	private static String makeSql4CancelFood(Staff staff, ExtraCond extraCond){
		String sql;
		sql = " SELECT " +
			  "  R.restaurant_name, OF.restaurant_id, IFNULL(D.name, '已删除部门') AS dept_name, OF.order_date, OF.order_id, OF.unit_price, OF.food_id, OF.name, OF.dept_id, OF.staff_id, OF.waiter, OF.cancel_reason_id, IF(OF.cancel_reason_id = 1, '无原因', OF.cancel_reason) AS cancel_reason, " +
			  " ABS(OF.order_count) AS cancel_amount, " +
			  " ABS(($(unit_price) + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * OF.order_count * OF.discount) AS cancel_price " +
			  " FROM " + Params.dbName + "." + extraCond.dbTbl.orderFoodTbl + " OF " + 
			  " JOIN " + Params.dbName + "." + extraCond.dbTbl.orderTbl + " O " +
			  " ON 1 = 1 " +
			  " AND OF.order_id = O.id " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " JOIN " + Params.dbName + "." + extraCond.dbTbl.tgTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
			  " LEFT JOIN " + Params.dbName + ".department D ON D.dept_id = OF.dept_id AND D.restaurant_id = OF.restaurant_id " +
			  " LEFT JOIN " + Params.dbName + ".restaurant R ON R.id = OF.restaurant_id " +
			  " WHERE 1 = 1 " +
			  " AND OF.order_count < 0 " +
			  " AND OF.operation = " + OrderFood.Operation.CANCEL.getVal() +
			  (extraCond != null ? extraCond.setStaff(staff).toString() : "");
		
		return sql.replace("$(unit_price)", "IFNULL(OF.plan_price, IFNULL(OF.food_unit_price, OF.unit_price))");
	}
}
