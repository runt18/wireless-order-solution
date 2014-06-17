package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.TasteGroup;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;

/**
 * The DB reflector is designed to the bridge between the OrderFood instance of
 * protocol and database.
 * 
 * @author Ying.Zhang
 * 
 */
public class OrderFoodDao {
	
	public static class ExtraCond4CancelFood extends ExtraCond{
		
		private int reasonId = -1;
		
		public ExtraCond4CancelFood(DateType dateType){
			super(dateType);
		}
		
		public ExtraCond4CancelFood setReasonId(int reasonId){
			this.reasonId = reasonId;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(reasonId > 0){
				extraCond.append(" AND " + orderFoodTblAlias + ".cancel_reason_id = " + reasonId);
			}
			extraCond.append(" AND " + orderTblAlias + ".cancel_price <> 0");
			extraCond.append(" AND " + orderFoodTblAlias + ".order_count < 0");
			extraCond.append(super.toString());
			
			return extraCond.toString();
		}
	}
	
	public static class ExtraCond{
		private final DateType dateType;
		final String orderFoodTblAlias = "OF";
		final String orderTblAlias = "O";
		final String orderFoodTbl;
		final String orderTbl;
		
		private int orderId;
		private Department.DeptId deptId;
		private DutyRange dutyRange;
		private HourRange hourRange;
		private Region.RegionId regionId;
		private int staffId;
		private String foodName;
		private boolean isGift;
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			if(dateType.isToday()){
				orderTbl = "order";
				orderFoodTbl = "order_food";
			}else{
				orderTbl = "order_history";
				orderFoodTbl = "order_food_history";
			}
		}
		
		public ExtraCond setOrderId(int orderId){
			this.orderId = orderId;
			return this;
		}
		
		public ExtraCond setDeptId(Department.DeptId deptId){
			this.deptId = deptId;
			return this;
		}
		
		public ExtraCond setDutyRange(DutyRange range){
			this.dutyRange = range;
			return this;
		}
		
		public ExtraCond setHourRange(HourRange range){
			this.hourRange = range;
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
		
		public ExtraCond setStaffId(int staffId){
			this.staffId = staffId;
			return this;
		}
		
		public ExtraCond setGift(boolean onOff){
			this.isGift = onOff;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(orderId > 0){
				extraCond.append(" AND " + orderTblAlias + ".id = " + orderId);
			}
			if(deptId != null){
				extraCond.append(" AND " + orderFoodTblAlias + ".dept_id = " + deptId.getVal());
			}
			if(dutyRange != null){
				extraCond.append(" AND " + orderTblAlias + ".order_date BETWEEN '" + dutyRange.getOnDutyFormat() + "' AND '" + dutyRange.getOffDutyFormat() + "'");
			}
			if(hourRange != null){
				extraCond.append(" AND TIME(" + orderTblAlias + ".order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
			}
			if(regionId != null){
				extraCond.append(" AND " + orderTblAlias + ".region_id = " + regionId.getId());
			}
			if(staffId > 0){
				extraCond.append(" AND " + orderFoodTblAlias + ".staff_id = " + staffId);
			}
			if(foodName != null){
				extraCond.append(" AND " + orderFoodTblAlias + ".name LIKE '%" + foodName + "%'");
			}
			if(isGift){
				extraCond.append(" AND " + orderFoodTblAlias + ".is_gift = 1");
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Get each single detail from order to today according to the specific table. 
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return the list of order holding each single detail
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	public static List<OrderFood> getSingleDetailByTable(Staff staff, Table tbl) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getSingleDetailByTable(dbCon, staff, tbl);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get each single detail from order to today according to the specific table. 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return the list of order holding each single detail
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	public static List<OrderFood> getSingleDetailByTable(DBCon dbCon, Staff staff, Table tbl) throws BusinessException, SQLException {
		int orderId = OrderDao.getOrderIdByUnPaidTable(dbCon, staff, tbl);
		return getSingleDetail(dbCon, staff, new ExtraCond(DateType.TODAY).setOrderId(orderId), null);
	}
	
	/**
	 * Get each single detail of order according to extra condition. 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return the list of order holding each single detail
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement.
	 * @throws BusinessException 
	 * 			   throws if any associated taste group does NOT exist
	 */
	public static List<OrderFood> getSingleDetail(Staff staff, ExtraCond extraCond, String orderClause) throws Exception {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return OrderFoodDao.getSingleDetail(dbCon, staff, extraCond, orderClause);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get each single detail of order according to extra condition. 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return the list of order holding each single detail
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement.
	 * @throws BusinessException 
	 * 			   throws if any associated taste group does NOT exist
	 */
	public static List<OrderFood> getSingleDetail(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException {
		return getSingleDetail(dbCon, staff, extraCond.toString(), orderClause, extraCond.dateType);
	}
	
	/**
	 * Get each single detail from order to history. 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @param dateType the date type {@link DateType}
	 * @return the list of order holding each single detail
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement.
	 * @throws BusinessException 
	 * 			   throws if any associated taste group does NOT exist
	 */
	private static List<OrderFood> getSingleDetail(DBCon dbCon, Staff staff, String extraCond, String orderClause, DateType dateType) throws SQLException, BusinessException {
		String sql;

		if(dateType.isHistory()){
		sql = "SELECT OF.order_id, OF.taste_group_id, OF.is_temporary, " +
				" OF.restaurant_id, OF.food_id, OF.name, OF.food_status, OF.is_paid, " +
				" OF.unit_price, OF.order_count, OF.waiter, OF.order_date, OF.discount, OF.order_date, " +
				" OF.cancel_reason_id, IF(OF.cancel_reason_id = 1, '无原因', OF.cancel_reason) cancel_reason, " +
				" OF.kitchen_id, (CASE WHEN K.kitchen_id IS NULL THEN '已删除厨房' ELSE K.name END) AS kitchen_name, " +
				" OF.dept_id, (CASE WHEN D.dept_id IS NULL THEN '已删除部门' ELSE D.name END) as dept_name " +
				" FROM " + Params.dbName + ".order_food_history OF " +
				" JOIN " + Params.dbName + ".order_history O ON O.id = OF.order_id " +
				" LEFT JOIN " + Params.dbName + ".kitchen K ON OF.kitchen_id = K.kitchen_id " +
				" LEFT JOIN " + Params.dbName + ".department D ON D.dept_id = K.dept_id AND D.restaurant_id = K.restaurant_id " +
				" WHERE 1 = 1 " +
				" AND O.restaurant_id = " + staff.getRestaurantId() +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : " " + orderClause);
		
		}else if(dateType.isToday()){
			sql = " SELECT OF.id, OF.order_id, OF.taste_group_id, OF.is_temporary, " +
					" OF.restaurant_id, OF.food_id, OF.name, OF.food_status, OF.is_paid, " +
					" OF.unit_price, OF.order_count, OF.waiter, OF.order_date, OF.discount, OF.order_date, " +
					" OF.cancel_reason_id, OF.cancel_reason, " +
					" OF.kitchen_id, (CASE WHEN K.kitchen_id IS NULL THEN '已删除厨房' ELSE K.name END) AS kitchen_name, " +
					" OF.dept_id, (CASE WHEN D.dept_id IS NULL THEN '已删除部门' ELSE D.name END) as dept_name " +
					" FROM " + Params.dbName + ".order_food OF " +
					" JOIN " + Params.dbName + ".order O ON OF.order_id = O.id " +
					" LEFT JOIN " + Params.dbName + ".kitchen K " + " ON OF.kitchen_id = K.kitchen_id " +
					" LEFT JOIN " + Params.dbName + ".department D " + " ON D.dept_id = K.dept_id AND D.restaurant_id = K.restaurant_id " +
					" WHERE 1 = 1 " +
					" AND O.restaurant_id = " + staff.getRestaurantId() +
					(extraCond == null ? "" : extraCond) +
					(orderClause == null ? "" : " " + orderClause);
		}else{
			throw new IllegalArgumentException("The date type is invalid.");
		}
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood of = new OrderFood();
			of.setOrderId(dbCon.rs.getInt("order_id"));
			of.asFood().setFoodId(dbCon.rs.getInt("food_id"));
			of.asFood().setName(dbCon.rs.getString("name"));
			of.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			of.asFood().setStatus(dbCon.rs.getShort("food_status"));
			of.setRepaid(dbCon.rs.getBoolean("is_paid"));
			
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			//Get the detail to taste group.
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				of.setTasteGroup(TasteGroupDao.getById(staff, tasteGroupId, dateType));
			}
			
			of.setCount(dbCon.rs.getFloat("order_count"));
			of.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			of.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			of.setWaiter(dbCon.rs.getString("waiter"));
			
			Kitchen kitchen = new Kitchen(dbCon.rs.getInt("kitchen_id"));
			kitchen.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			kitchen.setName(dbCon.rs.getString("kitchen_name"));
			
			Department dept = new Department(dbCon.rs.getShort("dept_id"));
			dept.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			dept.setName(dbCon.rs.getString("dept_name"));
			
			kitchen.setDept(dept);
			of.asFood().setKitchen(kitchen);
			
			of.setDiscount(dbCon.rs.getFloat("discount"));
			of.setTemp(dbCon.rs.getBoolean("is_temporary"));
			
			CancelReason cr = new CancelReason(dbCon.rs.getInt("cancel_reason_id"),
											   dbCon.rs.getString("cancel_reason"),
											   dbCon.rs.getInt("restaurant_id"));
			of.setCancelReason(cr);
			
			orderFoods.add(of);
		}
		dbCon.rs.close();
		
		return orderFoods;
	}

	/**
	 * Get the detail according to extra condition
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the order food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any taste group does NOT exist 
	 */
	public static List<OrderFood> getDetail(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		String sql;

		sql = " SELECT OF.order_id, OF.food_id, OF.taste_group_id, OF.is_temporary, OF.is_gift, " +
			  " MIN(OF.id) AS id, MAX(OF.restaurant_id) AS restaurant_id, MAX(OF.kitchen_id) AS kitchen_id, " + 
			  " MAX(OF.name) AS name, MAX(OF.food_status) AS food_status, " +
			  " MAX(OF.unit_price) AS unit_price, MAX(OF.commission) AS commission, MAX(OF.waiter) AS waiter, MAX(OF.order_date) AS order_date, MAX(OF.discount) AS discount, " +
			  " MAX(OF.dept_id) AS dept_id, MAX(OF.id) AS id, MAX(OF.order_date) AS pay_datetime, SUM(OF.order_count) AS order_sum " +
			  " FROM " + Params.dbName + "." + extraCond.orderFoodTbl + " " + extraCond.orderFoodTblAlias +
			  " JOIN " + Params.dbName + "." + extraCond.orderTbl + " " + extraCond.orderTblAlias +
			  " ON OF.order_id = O.id " +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  " GROUP BY OF.food_id, OF.taste_group_id, OF.is_temporary, OF.is_gift " + 
			  " HAVING order_sum > 0 " +
			  " ORDER BY id ASC ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood of = new OrderFood(dbCon.rs.getLong("id"));
			of.asFood().setFoodId(dbCon.rs.getInt("food_id"));
			of.asFood().setName(dbCon.rs.getString("name"));
			of.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			of.asFood().setStatus(dbCon.rs.getShort("food_status"));
			if(of.asFood().isCombo() && extraCond.dateType.isToday()){
				of.asFood().setChildFoods(FoodDao.getChildrenByParent(staff, of.asFood().getFoodId()));
			}
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				of.setTasteGroup(TasteGroupDao.getById(staff, tasteGroupId, extraCond.dateType));
			}
			of.setCount(dbCon.rs.getFloat("order_sum"));
			of.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			of.asFood().setCommission(dbCon.rs.getFloat("commission"));
			of.setOrderDate(dbCon.rs.getTimestamp("pay_datetime").getTime());
			of.setWaiter(dbCon.rs.getString("waiter"));
			of.getKitchen().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			of.getKitchen().setId(dbCon.rs.getInt("kitchen_id"));
			of.getKitchen().getDept().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			of.getKitchen().getDept().setId(dbCon.rs.getShort("dept_id"));
			of.setDiscount(dbCon.rs.getFloat("discount"));
			of.setTemp(dbCon.rs.getBoolean("is_temporary"));
			of.setGift(dbCon.rs.getBoolean("is_gift"));
			orderFoods.add(of);
		}
		dbCon.rs.close();
		
		return orderFoods;
	}
	
}
