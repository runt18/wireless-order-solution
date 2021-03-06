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
import com.wireless.pojo.billStatistics.gift.GiftDetail;
import com.wireless.pojo.billStatistics.gift.GiftIncomeByDept;
import com.wireless.pojo.billStatistics.gift.GiftIncomeByEachDay;
import com.wireless.pojo.billStatistics.gift.GiftIncomeByStaff;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.SortedList;

public class CalcGiftStatisticsDao {

	public static class ExtraCond implements Cloneable{
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
		private DutyRange dutyRange;
		
		private boolean isChain;				//是否连锁
		private boolean calcByDuty;				//是否按日结区间计算
		
		private Staff staff;
		
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
		
		public ExtraCond setDutyRange(DutyRange range){
			this.dutyRange = range;
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
			if(regionId != null){
				extraCond.append(" AND O.region_id = " + regionId.getId());
			}
			if(foodName != null){
				extraCond.append(" AND OF.name LIKE '%" + foodName + "%'");
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
	 * Get gift detail list by condition.
	 * @param staff
	 * @param queryType
	 * @return	the gift list
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throw if the query type is invalid
	 */
	public static List<GiftDetail> getDetail(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			return getDetail(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<GiftDetail> getDetail(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		final List<GiftDetail> result = new ArrayList<GiftDetail>();
		
		if(extraCond.isChain){
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			
			//Append the gift detail to the group
			result.addAll(getDetail(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false)));
			
			//Append the gift detail to the branch
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				result.addAll(getDetail(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false)));
			}
		}else{
			String sql = makeSql4GiftFood(staff, extraCond);
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				GiftDetail g = new GiftDetail();
				g.setOrderId(dbCon.rs.getInt("order_id"));
				g.setCount(dbCon.rs.getFloat("gift_amount"));
				g.setName(dbCon.rs.getString("name"));
				g.setOrderDateFormat(dbCon.rs.getTimestamp("order_date").getTime());
				g.setRestaurantName(dbCon.rs.getString("restaurant_name"));
				g.setRid(dbCon.rs.getInt("restaurant_id"));
				g.setWaiter(dbCon.rs.getString("waiter"));
				g.setTotalGift(dbCon.rs.getFloat("gift_price"));
				g.setTotalAmount(dbCon.rs.getInt("gift_amount"));
				result.add(g);
			}
			dbCon.rs.close();
		}
		
		return result;
		
	}
	
	/**
	 * Calculate the gift income by staff according to duty range and extra condition
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link GiftIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<GiftIncomeByStaff> calcIncomeByStaff(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByStaff(dbCon, staff, extraCond);
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
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result list {@link GiftIncomeByStaff}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<GiftIncomeByStaff> calcIncomeByStaff(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		
		List<GiftIncomeByStaff> result = new ArrayList<GiftIncomeByStaff>();
		
		if(extraCond.isChain){
			
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the gift income by staff to the group.
			result.addAll(calcIncomeByStaff(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false)));
			
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				//Append the gift income by staff to each branch.
				result.addAll(calcIncomeByStaff(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false)));
			}
			
		}else{
			String sql;
			sql = " SELECT " +
				  " TMP.staff_id, MAX(TMP.waiter) AS staff_name, " +
				  " ROUND(SUM(TMP.gift_amount), 2) AS gift_amount, " +
				  " ROUND(SUM(TMP.gift_price), 2) AS gift_price " +
				  " FROM (" +
				  makeSql4GiftFood(staff, extraCond) +
				  " ) AS TMP " +
				  " GROUP BY TMP.staff_id ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				result.add(new GiftIncomeByStaff(dbCon.rs.getString("staff_name"), 
												   dbCon.rs.getFloat("gift_amount"), 
												   dbCon.rs.getFloat("gift_price")));
			}
			dbCon.rs.close();
		}
		
		return result;
	}
	
	/**
	 * Calculate the gift income according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link GiftIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<GiftIncomeByDept> calcIncomeByDept(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByDept(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the gift income by each day according to specific range and extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link GiftIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<GiftIncomeByEachDay> calcIncomeByEachDay(Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcIncomeByEachDay(dbCon, staff, extraCond);
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
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link GiftIncomeByEachDay}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws ParseException
	 * 			throws if failed to parse the duty range
	 * @throws BusinessException 
	 */
	public static List<GiftIncomeByEachDay> calcIncomeByEachDay(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, ParseException, BusinessException{
		
		if(extraCond.isChain){
			
			final Map<DutyRange, GiftIncomeByEachDay> chainResult = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the gift income by each day to the group.
			for(GiftIncomeByEachDay groupIncome : calcIncomeByEachDay(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				chainResult.put(groupIncome.getRange(), groupIncome);
			}
			
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				//Append the gift income by each to each branch.
				for(GiftIncomeByEachDay branchIncome : calcIncomeByEachDay(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					if(chainResult.containsKey(branchIncome.getRange())){
						GiftIncomeByEachDay giftIncome = chainResult.get(branchIncome.getRange());
						final float giftAmount = branchIncome.getGiftAmount() + giftIncome.getGiftAmount();
						final float giftPrice = branchIncome.getGiftPrice() + giftIncome.getGiftPrice();
						chainResult.put(branchIncome.getRange(), new GiftIncomeByEachDay(branchIncome.getRange(), giftAmount, giftPrice));
					}else{
						chainResult.put(branchIncome.getRange(), branchIncome);
					}
				}
			}
			
			return SortedList.newInstance(chainResult.values());
			
		}else{
			final List<GiftIncomeByEachDay> result = new ArrayList<GiftIncomeByEachDay>();
			
			Calendar c = Calendar.getInstance();
			Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOnDutyFormat());
			Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(extraCond.dutyRange.getOffDutyFormat());
			c.setTime(dateBegin);
			while (dateBegin.compareTo(dateEnd) <= 0) {
				c.add(Calendar.DATE, 1);
				
				final DutyRange rangeByEachDay = new DutyRange(dateBegin.getTime(), c.getTimeInMillis());
				String sql;
				sql = " SELECT " +
					  " ROUND(SUM(TMP.gift_amount), 2) AS gift_amount, " +
					  " ROUND(SUM(TMP.gift_price), 2) AS gift_price " +
				      " FROM (" +
					  makeSql4GiftFood(staff, ((ExtraCond)extraCond.clone()).setDutyRange(rangeByEachDay).setCalcByDuty(true)) + 
					  " ) AS TMP ";
			    dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					result.add(new GiftIncomeByEachDay(rangeByEachDay,
													   dbCon.rs.getFloat("gift_amount"),
													   dbCon.rs.getFloat("gift_price")));
				}else{
					result.add(new GiftIncomeByEachDay(rangeByEachDay, 0, 0));
				}
				dbCon.rs.close();

				dateBegin = c.getTime();
			}
			
			return result;
			
		}
		
	}
	
	/**
	 * Calculate the gift income according to specific range and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result list {@link GiftIncomeByDept}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static List<GiftIncomeByDept> calcIncomeByDept(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		
		if(extraCond.isChain){
			
			final Map<String, GiftIncomeByDept> chainResult = new HashMap<>();
			final Staff groupStaff = StaffDao.getAdminByRestaurant(dbCon, staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId());
			//Append the gift income by department to the group.
			for(GiftIncomeByDept groupIncome : calcIncomeByDept(dbCon, groupStaff, ((ExtraCond)extraCond.clone()).setChain(false))){
				chainResult.put(groupIncome.getDepartment().getName(), groupIncome);
			}
			
			for(Restaurant branch : RestaurantDao.getById(dbCon, groupStaff.getRestaurantId()).getBranches()){
				//Append the gift income by department to each branch.
				for(GiftIncomeByDept branchIncome : calcIncomeByDept(dbCon, StaffDao.getAdminByRestaurant(dbCon, branch.getId()), ((ExtraCond)extraCond.clone()).setChain(false))){
					if(chainResult.containsKey(branchIncome.getDepartment().getName())){
						GiftIncomeByDept giftIncome = chainResult.get(branchIncome.getDepartment().getName());
						final float giftAmount = branchIncome.getGiftAmount() + giftIncome.getGiftAmount();
						final float giftPrice = branchIncome.getGiftPrice() + giftIncome.getGiftPrice();
						chainResult.put(branchIncome.getDepartment().getName(), new GiftIncomeByDept(branchIncome.getDepartment(), giftAmount, giftPrice));
					}else{
						chainResult.put(branchIncome.getDepartment().getName(), branchIncome);
					}
				}
			}
			
			return new ArrayList<>(chainResult.values());
			
		}else{
			List<GiftIncomeByDept> result = new ArrayList<GiftIncomeByDept>();
			
			String sql;
			sql = " SELECT " +
				  " D.dept_id, MAX(D.name) AS dept_name, D.type, " +
				  " ROUND(SUM(TMP.gift_amount), 2) AS gift_amount, " +
				  " ROUND(SUM(TMP.gift_price), 2) AS gift_price " +
			      " FROM (" +
				  makeSql4GiftFood(staff, extraCond) +
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
		

	}
	
	
	
	private static String makeSql4GiftFood(Staff staff, ExtraCond extraCond){
		String sql;
		sql = (" SELECT " +
			  " OF.dept_id, OF.staff_id, OF.order_date, OF.waiter, OF.restaurant_id, R.restaurant_name, OF.order_id, OF.name, " +
			  " ABS(OF.order_count) AS gift_amount, " +
			  " (($(unit_price) + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * OF.order_count) AS gift_price " +
			  " FROM " + Params.dbName + "." + extraCond.orderFoodTbl + " OF " + 
			  " JOIN " + Params.dbName + "." + extraCond.orderTbl + " O " +
			  " JOIN " + Params.dbName + ".restaurant R " + 
			  " ON 1 = 1 " +
			  " AND R.id = OF.restaurant_id " +
			  " AND OF.order_id = O.id " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  //" AND O.order_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
			  " JOIN " + Params.dbName + "." + extraCond.tasteGroupTbl + " TG " + " ON OF.taste_group_id = TG.taste_group_id " +
			  " WHERE 1 = 1 " +
			  " AND OF.is_gift = 1 " +
			  (extraCond != null ? extraCond.setStaff(staff) : "")
			  ).replace("$(unit_price)", "IFNULL(OF.plan_price, IFNULL(OF.food_unit_price, OF.unit_price))");
		
		return sql;
	}
}
