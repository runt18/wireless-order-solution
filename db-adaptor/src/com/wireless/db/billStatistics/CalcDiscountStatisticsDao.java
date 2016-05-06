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
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByDept;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByEachDay;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByStaff;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.SortedList;

public class CalcDiscountStatisticsDao implements Cloneable {

	public static class ExtraCond implements Cloneable{
		private final DateType dateType;
		
		private final String orderTbl;
		//private final String orderTblAlias = "O";
		private final String orderFoodTbl;
		//private final String orderFoodTblAlias = "OF";
		
		private int staffId;
		private DutyRange dutyRange;
		private HourRange hourRange;
		private Department.DeptId deptId;
		
		private boolean isChain;				//是否连锁
		private boolean calcByDuty;				//是否按日结区间计算
		private Staff staff;
		
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
		
		private ExtraCond setStaff(Staff staff){
			this.staff = staff;
			return this;
		}
		
		public ExtraCond setChain(boolean onOff){
			this.isChain = onOff;
			return this;
		}
		
		public ExtraCond setCalcByDuty(boolean onOff){
			this.calcByDuty = onOff;
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
		
		public ExtraCond setDutyRange(DutyRange dutyRange){
			this.dutyRange = dutyRange;
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
				extraCond.append(" AND O.discount_staff_id = " + staffId);
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
					try {
						range = DutyRangeDao.exec(staff, dutyRange);
					} catch (SQLException e) {
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
			//Append the discount details to the group.
			result.addAll(getDetail(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false)));

			//Append the discount details to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.addAll(getDetail(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false)));
			}
			
		}else{
			String sql = makeSql4Discount(staff, extraCond);
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Order o = new Order();
				o.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				o.setRestaurantName(dbCon.rs.getString("restaurant_name"));
				o.setId(dbCon.rs.getInt("id"));
				o.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
				o.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
				o.setActualPrice(dbCon.rs.getFloat("actual_price"));
				o.setDiscounter(dbCon.rs.getString("discount_staff"));
				o.setComment(dbCon.rs.getString("comment"));
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
	 * Calculate the discount income by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link DiscountIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<DiscountIncomeByEachDay> calcIncomeByEachDay(Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByEachDay(dbCon, staff, extraCond);
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
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link DiscountIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<DiscountIncomeByEachDay> calcIncomeByEachDay(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		
		if(extraCond.isChain){
			
			final Map<DutyRange, DiscountIncomeByEachDay> chainResult = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the discount income by each day to the group.
			for(DiscountIncomeByEachDay groupIncome : calcIncomeByEachDay(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				chainResult.put(groupIncome.getRange(), groupIncome);
			}
			
			//Append the discount income by each day to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				for(DiscountIncomeByEachDay branchIncome : calcIncomeByEachDay(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					if(chainResult.containsKey(branchIncome.getRange())){
						DiscountIncomeByEachDay discountIncome = chainResult.get(branchIncome.getRange());
						final float discountAmount = branchIncome.getAmount() + discountIncome.getAmount();
						final float discountPrice = branchIncome.getPrice() + discountIncome.getPrice();
						chainResult.put(branchIncome.getRange(), new DiscountIncomeByEachDay(branchIncome.getRange(), discountAmount, discountPrice));
					}else{
						chainResult.put(branchIncome.getRange(), branchIncome);
					}
				}
			}
			
			return SortedList.newInstance(chainResult.values());
			
		}else{
			List<DiscountIncomeByEachDay> result = new ArrayList<DiscountIncomeByEachDay>();
			
			Calendar c = Calendar.getInstance();
			Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOnDutyFormat());
			Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOffDutyFormat());
			c.setTime(dateBegin);
			while (dateBegin.compareTo(dateEnd) <= 0) {
				c.add(Calendar.DATE, 1);
				
				final DutyRange rangeByEachDay = new DutyRange(dateBegin.getTime(), c.getTimeInMillis());
				
				String sql;
				sql = " SELECT " +
					  " COUNT(*) AS discount_amount, " +
					  " ROUND(SUM(TMP.discount_price), 2) AS discount_price " +
				      " FROM (" +
				      makeSql4Discount(staff, ((ExtraCond)extraCond.clone()).setDutyRange(rangeByEachDay).setCalcByDuty(true)) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new DiscountIncomeByEachDay(rangeByEachDay,
														 dbCon.rs.getFloat("discount_amount"),
														 dbCon.rs.getFloat("discount_price")));
				}else{
					result.add(new DiscountIncomeByEachDay(rangeByEachDay, 0, 0)); 
				}
				dbCon.rs.close();

				dateBegin = c.getTime();
			}
			
			return result;
		}

	}
	
	/**
	 * Calculate the discount income by staff according to duty range and extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link DiscountIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<DiscountIncomeByStaff> calcIncomeByStaff(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByStaff(dbCon, staff, extraCond);
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
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link DiscountIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<DiscountIncomeByStaff> calcIncomeByStaff(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		List<DiscountIncomeByStaff> result = new ArrayList<DiscountIncomeByStaff>();

		if(extraCond.isChain){
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the discount income by staff to the group.
			result.addAll(calcIncomeByStaff(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false)));

			//Append the discount income by staff to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.addAll(calcIncomeByStaff(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false)));
			}
		}else{
			String sql;
			sql = " SELECT " +
				  " TMP.discount_staff_id, MAX(IFNULL(TMP.discount_staff, '其他')) AS staff_name, " +
				  " COUNT(*) AS discount_amount, " +
				  " ROUND(SUM(TMP.discount_price), 2) AS discount_price " +
				  " FROM (" +
				  makeSql4Discount(staff, extraCond) +
				  " ) AS TMP " +
				  " GROUP BY TMP.discount_staff_id ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				result.add(new DiscountIncomeByStaff(dbCon.rs.getString("staff_name"), 
												   dbCon.rs.getFloat("discount_amount"), 
												   dbCon.rs.getFloat("discount_price")));
			}
			dbCon.rs.close();
			
		}
		
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
	 * @throws BusinessException 
	 */
	public static List<DiscountIncomeByDept> calcDiscountIncomeByDept(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcDiscountIncomeByDept(dbCon, staff, extraCond);
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
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link DiscountIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<DiscountIncomeByDept> calcDiscountIncomeByDept(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		
		if(extraCond.isChain){
			final Map<String, DiscountIncomeByDept> chainResult = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the discount income by department to the group.
			for(DiscountIncomeByDept groupIncome : calcDiscountIncomeByDept(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				chainResult.put(groupIncome.getDepartment().getName(), groupIncome);
			}
			
			//Append the discount income by department to each branch.
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				for(DiscountIncomeByDept branchIncome : calcDiscountIncomeByDept(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					if(chainResult.containsKey(branchIncome.getDepartment().getName())){
						DiscountIncomeByDept discountIncome = chainResult.get(branchIncome.getDepartment().getName());
						final float discountAmount = branchIncome.getAmount() + discountIncome.getAmount();
						final float discountPrice = branchIncome.getPrice() + discountIncome.getPrice();
						chainResult.put(branchIncome.getDepartment().getName(), new DiscountIncomeByDept(branchIncome.getDepartment(), discountAmount, discountPrice));
					}else{
						chainResult.put(branchIncome.getDepartment().getName(), branchIncome);
					}
				}
			}
			
			return new ArrayList<>(chainResult.values());
			
		}else{
			String sql;
			sql = " SELECT " +
				  " D.dept_id, MAX(D.name) AS dept_name, D.type, " +
				  " COUNT(*) AS discount_amount, " +
				  " ROUND(SUM(TMP.discount_price), 2) AS discount_price " +
				  " FROM (" +
				  makeSql4Discount(staff, extraCond) +
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
		

	}
	
	private static String makeSql4Discount(Staff staff, ExtraCond extraCond){
		String sql;
		sql = " SELECT " +
			  " O.id, O.order_date, O.discount_staff, O.discount_staff_id, O.discount_price, O.actual_price, O.comment, O.table_alias, O.table_name, O.table_id, D.name, D.dept_id, D.type " +
			  " ,R.restaurant_name, R.id AS restaurant_id " +	
			  " FROM " + Params.dbName + "." + extraCond.orderTbl + " O " +
			  " JOIN " + Params.dbName + "." + extraCond.orderFoodTbl + " OF ON OF.order_id = O.id " +
			  " JOIN " + Params.dbName + ".department D ON D.dept_id = OF.dept_id AND D.restaurant_id = OF.restaurant_id " +
			  " JOIN " + Params.dbName + ".restaurant R ON R.id = O.restaurant_id " +
			  " WHERE 1 = 1 " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  //" AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " AND O.discount_price <> 0 " +
			  (extraCond != null ? extraCond.setStaff(staff) : "") +
			  " GROUP BY O.id ";
		
		return sql;
	}

}
