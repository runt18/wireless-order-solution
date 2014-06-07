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
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.TasteGroup;
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
				extraCond.append(" AND " + orderFoodTbl + ".cancel_reason_id = " + reasonId);
			}
			extraCond.append(" AND " + orderTbl + ".cancel_price <> 0");
			extraCond.append(" AND " + orderFoodTbl + ".order_count < 0");
			extraCond.append(super.toString());
			
			return extraCond.toString();
		}
	}
	
	public static class ExtraCond{
		private final DateType dateType;
		final String orderFoodTbl;
		final String orderTbl;
		
		private int orderId;
		private Department.DeptId deptId;
		private DutyRange dutyRange;
		private HourRange hourRange;
		
		public ExtraCond(DateType dateType){
			this.dateType = dateType;
			if(this.dateType.isHistory()){
				orderTbl = "OH";
				orderFoodTbl = "OFH";
			}else{
				orderTbl = "O";
				orderFoodTbl = "OF";
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
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(orderId > 0){
				extraCond.append(" AND " + orderTbl + ".id = " + orderId);
			}
			if(deptId != null){
				extraCond.append(" AND " + orderFoodTbl + ".dept_id = " + deptId.getVal());
			}
			if(dutyRange != null){
				extraCond.append(" AND " + orderTbl + ".order_date BETWEEN '" + dutyRange.getOnDutyFormat() + "' AND '" + dutyRange.getOffDutyFormat() + "'");
			}
			if(hourRange != null){
				extraCond.append(" AND TIME(" + orderTbl + ".order_date) BETWEEN '" + hourRange.getOpeningFormat() + "' AND '" + hourRange.getEndingFormat() + "'");
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
		sql = "SELECT OFH.order_id, OFH.taste_group_id, OFH.is_temporary, " +
				" OFH.restaurant_id, OFH.food_id, OFH.name, OFH.food_status, OFH.is_paid, " +
				" OFH.unit_price, OFH.order_count, OFH.waiter, OFH.order_date, OFH.discount, OFH.order_date, " +
				" OFH.cancel_reason_id, IF(OFH.cancel_reason_id = 1, '无原因', OFH.cancel_reason) cancel_reason, " +
				" OFH.kitchen_id, (CASE WHEN K.kitchen_id IS NULL THEN '已删除厨房' ELSE K.name END) AS kitchen_name, " +
				" OFH.dept_id, (CASE WHEN D.dept_id IS NULL THEN '已删除部门' ELSE D.name END) as dept_name " +
				" FROM " + Params.dbName + ".order_food_history OFH " +
				" JOIN " + Params.dbName + ".order_history OH ON OH.id = OFH.order_id " +
				" LEFT JOIN " + Params.dbName + ".kitchen K ON OFH.kitchen_id = K.kitchen_id " +
				" LEFT JOIN " + Params.dbName + ".department D ON D.dept_id = K.dept_id AND D.restaurant_id = K.restaurant_id " +
				" WHERE 1 = 1 " +
				" AND OH.restaurant_id = " + staff.getRestaurantId() +
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
			OrderFood food = new OrderFood();
			food.setOrderId(dbCon.rs.getInt("order_id"));
			food.asFood().setFoodId(dbCon.rs.getInt("food_id"));
			food.asFood().setName(dbCon.rs.getString("name"));
			food.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.asFood().setStatus(dbCon.rs.getShort("food_status"));
			food.setRepaid(dbCon.rs.getBoolean("is_paid"));
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				food.makeTasteGroup(tasteGroupId, null, null);
			}
			food.setCount(dbCon.rs.getFloat("order_count"));
			food.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			food.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			food.setWaiter(dbCon.rs.getString("waiter"));
			
			Kitchen kitchen = new Kitchen(dbCon.rs.getInt("kitchen_id"));
			kitchen.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			kitchen.setName(dbCon.rs.getString("kitchen_name"));
			
			Department dept = new Department(dbCon.rs.getShort("dept_id"));
			dept.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			dept.setName(dbCon.rs.getString("dept_name"));
			
			kitchen.setDept(dept);
			food.asFood().setKitchen(kitchen);
			
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.setTemp(dbCon.rs.getBoolean("is_temporary"));
			
			CancelReason cr = new CancelReason(dbCon.rs.getInt("cancel_reason_id"),
											   dbCon.rs.getString("cancel_reason"),
											   dbCon.rs.getInt("restaurant_id"));
			food.setCancelReason(cr);
			
			food.asFood().setChildFoods(FoodDao.getChildrenByParent(staff, food.asFood().getFoodId()));
			orderFoods.add(food);
		}
		dbCon.rs.close();
		/**
		 * Get the taste group to order food which has taste
		 */
		for(OrderFood orderFood : orderFoods){
			if(orderFood.hasTasteGroup()){
				if(dateType.isToday()){
					orderFood.setTasteGroup(TasteGroupDao.getTodayById(dbCon, staff, orderFood.getTasteGroup().getGroupId()));
				}else{
					orderFood.setTasteGroup(TasteGroupDao.getHistoryById(dbCon, staff, orderFood.getTasteGroup().getGroupId()));
				}
			}
		}
		
		return orderFoods;
	}
	
	/**
	 * Create the foods from database table 'order_food' according an extra
	 * condition. Note that the database should be connected before invoking
	 * this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 * @throws BusinessException 
	 * 				throws if the any associated taste group is NOT found
	 */
	public static List<OrderFood> getDetailToday(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException, BusinessException {
		String sql;

		sql = " SELECT OF.order_id, OF.food_id, OF.taste_group_id, OF.is_temporary, " +
			  " MIN(OF.id) AS id, MAX(OF.restaurant_id) AS restaurant_id, MAX(OF.kitchen_id) AS kitchen_id, " + 
		      " MAX(OF.name) AS name, MAX(OF.food_status) AS food_status, " +
		      " MAX(OF.unit_price) AS unit_price, MAX(OF.commission) AS commission, MAX(OF.waiter) AS waiter, MAX(OF.order_date) AS order_date, MAX(OF.discount) AS discount, " +
			  " MAX(OF.dept_id) AS dept_id, MAX(OF.id) AS id, MAX(OF.order_date) AS pay_datetime, SUM(OF.order_count) AS order_sum " +
			  " FROM " +
			  Params.dbName +	".order_food OF " +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  " GROUP BY OF.food_id, OF.taste_group_id, OF.is_temporary " + 
			  " HAVING " +
			  " order_sum > 0 " +
			  (orderClause == null ? " ORDER BY id ASC " : " " + orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood(dbCon.rs.getLong("id"));
			food.asFood().setFoodId(dbCon.rs.getInt("food_id"));
			food.asFood().setName(dbCon.rs.getString("name"));
			food.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.asFood().setStatus(dbCon.rs.getShort("food_status"));
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				food.makeTasteGroup(tasteGroupId, null, null);
			}
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			food.asFood().setCommission(dbCon.rs.getFloat("commission"));
			food.setOrderDate(dbCon.rs.getTimestamp("pay_datetime").getTime());
			food.setWaiter(dbCon.rs.getString("waiter"));
			food.getKitchen().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.getKitchen().setId(dbCon.rs.getInt("kitchen_id"));
			food.getKitchen().getDept().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.getKitchen().getDept().setId(dbCon.rs.getShort("dept_id"));
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.setTemp(dbCon.rs.getBoolean("is_temporary"));
			food.asFood().setChildFoods(FoodDao.getChildrenByParent(staff, food.asFood().getFoodId()));
			orderFoods.add(food);
		}
		dbCon.rs.close();
		
		/**
		 * Get the taste group to order food which has taste
		 */
		for(OrderFood orderFood : orderFoods){
			if(orderFood.hasTasteGroup()){
				orderFood.setTasteGroup(TasteGroupDao.getTodayById(dbCon, staff, orderFood.getTasteGroup().getGroupId()));
			}
		}
		
		return orderFoods;
	}
	
	/**
	 * Create the foods from database table 'order_food' according an extra
	 * condition. Note that the database should be connected before invoking
	 * this method.
	 * 
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 * @throws BusinessException 
	 * 				throws if the any associated taste group is NOT found
	 */
	public static List<OrderFood> getDetailToday(Staff staff, String extraCond, String orderClause) throws Exception {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return OrderFoodDao.getDetailToday(dbCon, staff, extraCond, orderClause);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Create the foods from database table 'order_food_history' according an
	 * extra condition. Note that the database should be connected before
	 * invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 * @throws BusinessException 
	 * 			   throws if any associated taste group does NOT exist
	 */
	public static List<OrderFood> getDetailHistory(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException, BusinessException {
		String sql;

		sql = " SELECT OFH.order_id, OFH.food_id, OFH.taste_group_id, OFH.is_temporary, " +
			  " MIN(OFH.id) AS id, MAX(OFH.restaurant_id) AS restaurant_id, MAX(OFH.kitchen_id) AS kitchen_id, " +
			  " MAX(OFH.name) AS name, MAX(OFH.food_status) AS food_status, " +
			  " MAX(OFH.unit_price) AS unit_price, MAX(OFH.commission) AS commission, MAX(OFH.waiter) AS waiter, MAX(OFH.order_date) AS order_date, MAX(OFH.discount) AS discount, " +
			  " MAX(OFH.dept_id) AS dept_id, MAX(OFH.id) AS id, MAX(OFH.order_date) AS pay_datetime, SUM(OFH.order_count) AS order_sum " +
			  " FROM " +
			  Params.dbName + ".order_food_history OFH " +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  " GROUP BY OFH.food_id, OFH.taste_group_id, OFH.is_temporary " +
			  " HAVING order_sum > 0 " +
			  (orderClause == null ? " ORDER BY id ASC " : " " + orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood();
			food.asFood().setFoodId(dbCon.rs.getInt("food_id"));
			food.asFood().setName(dbCon.rs.getString("name"));
			food.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.asFood().setStatus(dbCon.rs.getShort("food_status"));
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				food.makeTasteGroup(tasteGroupId, null, null);
			}
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			food.asFood().setCommission(dbCon.rs.getFloat("commission"));
			food.setWaiter(dbCon.rs.getString("waiter"));
			food.setOrderDate(dbCon.rs.getTimestamp("pay_datetime").getTime());
			food.getKitchen().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.getKitchen().setId(dbCon.rs.getInt("kitchen_id"));
			food.getKitchen().getDept().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.getKitchen().getDept().setId(dbCon.rs.getShort("dept_id"));
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.setTemp(dbCon.rs.getBoolean("is_temporary"));
			orderFoods.add(food);
		}
		dbCon.rs.close();
		
		/**
		 * Get the taste group to order food which has taste
		 */
		for(OrderFood orderFood : orderFoods){
			if(orderFood.hasTasteGroup()){
				orderFood.setTasteGroup(TasteGroupDao.getHistoryById(dbCon, staff, orderFood.getTasteGroup().getGroupId()));
			}
		}
		
		return orderFoods;
	}
	
	/**
	 * Get the foods from database table 'order_food_history' according an extra condition.
	 * @param staff
	 * 			  the staff to perform this action
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 * @throws BusinessException 
	 * 			   throws if any associated taste group does NOT exist
	 */
	public static List<OrderFood> getDetailHistory(Staff staff, String extraCond, String orderClause) throws Exception {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return OrderFoodDao.getDetailHistory(dbCon, staff, extraCond, orderClause);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param extraCond
	 * @return
	 * @throws SQLException
	 */
	public static float getSalesMoney(DBCon dbCon, Staff term, String extraCond) throws SQLException{
		String sql = "SELECT SUM(unit_price * order_count) as money FROM " + Params.dbName + ".order_food_history " +
				 " WHERE restaurant_id = " + term.getRestaurantId() + 
				 (extraCond == null ? "" : extraCond);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			return dbCon.rs.getFloat("money");
		}else{
			return 0;
		}
		
				
	}
}
