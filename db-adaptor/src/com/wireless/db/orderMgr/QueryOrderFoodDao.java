package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.protocol.OrderFood;

/**
 * The DB reflector is designed to the bridge between the OrderFood instance of
 * protocol and database.
 * 
 * @author Ying.Zhang
 * 
 */
public class QueryOrderFoodDao {
	
	/**
	 * Get each single detail from order to today according to the specific table. 
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of order holding each single detail
	 * @throws SQLException
	 *             Throws if fail to execute the SQL statement.
	 */
	public static OrderFood[] getSingleDetailTodayByTable(String extraCond, String orderClause, Table tbl) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getSingleDetailTodayByTable(dbCon, extraCond, orderClause, tbl);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get each single detail from order to today according to the specific table. 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of order holding each single detail
	 * @throws SQLException
	 *             Throws if fail to execute the SQL statement.
	 */
	public static OrderFood[] getSingleDetailTodayByTable(DBCon dbCon, String extraCond, String orderClause, Table tbl) throws BusinessException, SQLException {
		int orderId = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, tbl)[0];
		return getSingleDetailToday(dbCon, " AND OF.order_id = " + orderId + (extraCond != null ? extraCond : " "), orderClause);
	}
	
	/**
	 * Get each single detail from order to today. 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of order holding each single detail
	 * @throws SQLException
	 *             Throws if fail to execute the SQL statement.
	 */
	public static OrderFood[] getSingleDetailToday(DBCon dbCon, String extraCond, String orderClause) throws SQLException {
		String sql;

		sql = "SELECT OF.order_id, OF.food_alias, OF.taste_group_id, OF.is_temporary, " +
				" OF.restaurant_id, OF.food_id, OF.name, OF.food_status, OF.is_paid, " +
				" OF.unit_price, OF.order_count, OF.waiter, OF.order_date, OF.discount, OF.order_date, " +
				" OF.cancel_reason_id, OF.cancel_reason, " +
				" OF.kitchen_alias, OF.kitchen_id, (CASE WHEN K.kitchen_id IS NULL THEN '已删除厨房' ELSE K.name END) AS kitchen_name, " +
				" OF.dept_id, (CASE WHEN D.dept_id IS NULL THEN '已删除部门' ELSE D.name END) as dept_name " +
				" FROM " + Params.dbName + ".order_food OF " +
				" LEFT JOIN " + Params.dbName + ".kitchen K " + " ON OF.kitchen_id = K.kitchen_id " +
				" LEFT JOIN " + Params.dbName + ".department D " + " ON D.dept_id = K.dept_id AND D.restaurant_id = K.restaurant_id " +
				" WHERE 1 = 1 " +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : " " + orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood();
			food.setOrderId(dbCon.rs.getInt("order_id"));
			food.asFood().setFoodId(dbCon.rs.getLong("food_id"));
			food.asFood().setName(dbCon.rs.getString("name"));
			food.asFood().setAliasId(dbCon.rs.getInt("food_alias"));
			food.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.setRepaid(dbCon.rs.getBoolean("is_paid"));
			food.asFood().setStatus(dbCon.rs.getShort("food_status"));
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				food.makeTasteGroup(tasteGroupId, null, null);
			}
			food.setCount(dbCon.rs.getFloat("order_count"));
			food.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			food.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			food.setWaiter(dbCon.rs.getString("waiter"));
			
			Kitchen kitchen = new Kitchen();
			kitchen.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			kitchen.setId(dbCon.rs.getLong("kitchen_id"));
			kitchen.setAliasId(dbCon.rs.getShort("kitchen_alias"));
			kitchen.setName(dbCon.rs.getString("kitchen_name"));
			
			Department dept = new Department();
			dept.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			dept.setId(dbCon.rs.getShort("dept_id"));
			dept.setName(dbCon.rs.getString("dept_name"));
			
			kitchen.setDept(dept);
			food.asFood().setKitchen(kitchen);
			
			food.setDiscount(dbCon.rs.getFloat("discount"));
			
			food.setTemp(dbCon.rs.getBoolean("is_temporary"));
			
			CancelReason cr = new CancelReason(dbCon.rs.getInt("cancel_reason_id"),
											   dbCon.rs.getString("cancel_reason"),
											   dbCon.rs.getInt("restaurant_id"));
			food.setCancelReason(cr);
			
			food.asFood().setChildFoods(FoodDao.queryComboByParent(food.asFood()));
			orderFoods.add(food);
		}
		dbCon.rs.close();
		/**
		 * Get the taste group to order food which has taste
		 */
		for(OrderFood orderFood : orderFoods){
			if(orderFood.getTasteGroup() != null){
				TasteGroup[] tasteGroups = QueryTasteGroupDao.execByToday(dbCon, "AND TG.taste_group_id=" + orderFood.getTasteGroup().getGroupId(), null);
				if(tasteGroups.length > 0){
					orderFood.setTasteGroup(tasteGroups[0]);
				}
			}
		}
		
		return orderFoods.toArray(new OrderFood[orderFoods.size()]);
	}
	
	/**
	 * 
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws Exception
	 */
	public static OrderFood[] getSingleDetailToday(String extraCond, String orderClause) throws Exception {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return QueryOrderFoodDao.getSingleDetailToday(dbCon, extraCond, orderClause);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get each single detail from order to history. 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of order holding each single detail
	 * @throws SQLException
	 *             Throws if fail to execute the SQL statement.
	 */
	public static OrderFood[] getSingleDetailHistory(DBCon dbCon, String extraCond, String orderClause) throws SQLException {
		String sql;

		sql = "SELECT OFH.order_id, OFH.food_alias, OFH.taste_group_id, OFH.is_temporary, " +
				" OFH.restaurant_id, OFH.food_id, OFH.name, OFH.food_status, OFH.is_paid, " +
				" OFH.unit_price, OFH.order_count, OFH.waiter, OFH.order_date, OFH.discount, OFH.order_date, " +
				" OFH.cancel_reason_id, IF(OFH.cancel_reason_id = 1, '无原因', OFH.cancel_reason) cancel_reason, " +
				" OFH.kitchen_alias, OFH.kitchen_id, (CASE WHEN K.kitchen_id IS NULL THEN '已删除厨房' ELSE K.name END) AS kitchen_name, " +
				" OFH.dept_id, (CASE WHEN D.dept_id IS NULL THEN '已删除部门' ELSE D.name END) as dept_name " +
				" FROM " + Params.dbName + ".order_food_history OFH " +
				" LEFT JOIN " + Params.dbName + ".kitchen K " + " ON OFH.kitchen_id = K.kitchen_id " +
				" LEFT JOIN " + Params.dbName + ".department D " + " ON D.dept_id = K.dept_id AND D.restaurant_id = K.restaurant_id " +
				" WHERE 1 = 1 " +
				(extraCond == null ? "" : extraCond) +
				(orderClause == null ? "" : " " + orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood();
			food.setOrderId(dbCon.rs.getInt("order_id"));
			food.asFood().setFoodId(dbCon.rs.getLong("food_id"));
			food.asFood().setName(dbCon.rs.getString("name"));
			food.asFood().setAliasId(dbCon.rs.getInt("food_alias"));
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
			
			Kitchen kitchen = new Kitchen();
			kitchen.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			kitchen.setId(dbCon.rs.getLong("kitchen_id"));
			kitchen.setAliasId(dbCon.rs.getShort("kitchen_alias"));
			kitchen.setName(dbCon.rs.getString("kitchen_name"));
			
			Department dept = new Department();
			dept.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			dept.setId(dbCon.rs.getShort("dept_id"));
			dept.setName(dbCon.rs.getString("dept_name"));
			
			kitchen.setDept(dept);
			food.asFood().setKitchen(kitchen);
			
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.setTemp(dbCon.rs.getBoolean("is_temporary"));
			
			CancelReason cr = new CancelReason(dbCon.rs.getInt("cancel_reason_id"),
											   dbCon.rs.getString("cancel_reason"),
											   dbCon.rs.getInt("restaurant_id"));
			food.setCancelReason(cr);
			
			food.asFood().setChildFoods(FoodDao.queryComboByParent(food.asFood()));
			orderFoods.add(food);
		}
		dbCon.rs.close();
		/**
		 * Get the taste group to order food which has taste
		 */
		for(OrderFood orderFood : orderFoods){
			if(orderFood.getTasteGroup() != null){
				TasteGroup[] tasteGroups = QueryTasteGroupDao.execByHistory(dbCon, "AND TG.taste_group_id=" + orderFood.getTasteGroup().getGroupId(), null);
				if(tasteGroups.length > 0){
					orderFood.setTasteGroup(tasteGroups[0]);
				}
			}
		}
		
		return orderFoods.toArray(new OrderFood[orderFoods.size()]);
	}
	
	/**
	 * 
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws Exception
	 */
	public static OrderFood[] getSingleDetailHistory(String extraCond, String orderClause) throws Exception {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return QueryOrderFoodDao.getSingleDetailHistory(dbCon, extraCond, orderClause);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Create the foods from database table 'order_food' according an extra
	 * condition. Note that the database should be connected before invoking
	 * this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	public static OrderFood[] getDetailToday(DBCon dbCon, String extraCond,	String orderClause) throws SQLException {
		String sql;

		sql = "SELECT OF.order_id, OF.food_alias, OF.taste_group_id, OF.is_temporary, " +
				" MAX(OF.restaurant_id) AS restaurant_id, MAX(OF.kitchen_alias) AS kitchen_alias, MAX(OF.kitchen_id) AS kitchen_id, " + 
				" MAX(OF.food_id) AS food_id, MAX(OF.name) AS name, MAX(OF.food_status) AS food_status, " +
				" MAX(OF.unit_price) AS unit_price, MAX(OF.waiter) AS waiter, MAX(OF.order_date) AS order_date, MAX(OF.discount) AS discount, " +
				" MAX(OF.dept_id) AS dept_id, MAX(OF.id) AS id, MAX(OF.order_date) AS pay_datetime, SUM(OF.order_count) AS order_sum " +
				" FROM " +
				Params.dbName +	".order_food OF " +
				" WHERE 1 = 1 " +
				(extraCond == null ? "" : extraCond) +
				" GROUP BY OF.food_alias, OF.taste_group_id, OF.hang_status, OF.is_temporary " + 
				" HAVING " +
				" order_sum > 0 " +
				(orderClause == null ? "" : " " + orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood();
			food.asFood().setFoodId(dbCon.rs.getLong("food_id"));
			food.asFood().setName(dbCon.rs.getString("name"));
			food.asFood().setAliasId(dbCon.rs.getInt("food_alias"));
			food.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.asFood().setStatus(dbCon.rs.getShort("food_status"));
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				food.makeTasteGroup(tasteGroupId, null, null);
			}
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			food.setOrderDate(dbCon.rs.getTimestamp("pay_datetime").getTime());
			food.setWaiter(dbCon.rs.getString("waiter"));
			food.getKitchen().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.getKitchen().setId(dbCon.rs.getLong("kitchen_id"));
			food.getKitchen().setAliasId(dbCon.rs.getShort("kitchen_alias"));
			food.getKitchen().getDept().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.getKitchen().getDept().setId(dbCon.rs.getShort("dept_id"));
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.setTemp(dbCon.rs.getBoolean("is_temporary"));
			food.asFood().setChildFoods(FoodDao.queryComboByParent(food.asFood()));
			orderFoods.add(food);
		}
		dbCon.rs.close();
		
		/**
		 * Get the taste group to order food which has taste
		 */
		for(OrderFood orderFood : orderFoods){
			if(orderFood.getTasteGroup() != null){
				TasteGroup[] tasteGroups = QueryTasteGroupDao.execByToday(dbCon, "AND TG.taste_group_id=" + orderFood.getTasteGroup().getGroupId(), null);
				if(tasteGroups.length > 0){
					orderFood.setTasteGroup(tasteGroups[0]);
				}
			}
		}
		
		return orderFoods.toArray(new OrderFood[orderFoods.size()]);
	}
	
	/**
	 * 
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws Exception
	 */
	public static OrderFood[] getDetailToday(String extraCond, String orderClause) throws Exception {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return QueryOrderFoodDao.getDetailToday(dbCon, extraCond, orderClause);
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
	 * @param extraCond
	 *            the extra condition to search the foods
	 * @param orderClause
	 *            the order clause to search the foods
	 * @return an array of foods
	 * @throws SQLException
	 *             throws if fail to execute the SQL statement
	 */
	public static OrderFood[] getDetailHistory(DBCon dbCon, String extraCond, String orderClause) throws SQLException {
		String sql;

		sql = "SELECT OFH.order_id, OFH.food_alias, OFH.taste_group_id, OFH.is_temporary, " +
			  " MAX(OFH.restaurant_id) AS restaurant_id, MAX(OFH.kitchen_alias) AS kitchen_alias, MAX(OFH.kitchen_id) AS kitchen_id, " +
			  " MAX(OFH.food_id) AS food_id, MAX(OFH.name) AS name, MAX(OFH.food_status) AS food_status, " +
			  " MAX(OFH.unit_price) AS unit_price, MAX(OFH.waiter) AS waiter, MAX(OFH.order_date) AS order_date, MAX(OFH.discount) AS discount, " +
			  " MAX(OFH.dept_id) AS dept_id, MAX(OFH.id) AS id, MAX(OFH.order_date) AS pay_datetime, SUM(OFH.order_count) AS order_sum " +
			  " FROM " +
			  Params.dbName + ".order_food_history OFH " +
			  " WHERE 1 = 1 " +
			  (extraCond == null ? "" : extraCond) +
			  " GROUP BY OFH.food_alias, OFH.taste_group_id, OFH.is_temporary " +
			  " HAVING order_sum > 0 " +
			  (orderClause == null ? "" : " " + orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood();
			food.asFood().setFoodId(dbCon.rs.getLong("food_id"));
			food.asFood().setName(dbCon.rs.getString("name"));
			food.asFood().setAliasId(dbCon.rs.getInt("food_alias"));
			food.asFood().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.asFood().setStatus(dbCon.rs.getShort("food_status"));
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				food.makeTasteGroup(tasteGroupId, null, null);
			}
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
			food.setWaiter(dbCon.rs.getString("waiter"));
			food.setOrderDate(dbCon.rs.getTimestamp("pay_datetime").getTime());
			food.getKitchen().setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			food.getKitchen().setId(dbCon.rs.getLong("kitchen_id"));
			food.getKitchen().setAliasId(dbCon.rs.getShort("kitchen_alias"));
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
			if(orderFood.getTasteGroup() != null){
				TasteGroup[] tasteGroups = QueryTasteGroupDao.execByHistory(dbCon, "AND TG.taste_group_id=" + orderFood.getTasteGroup().getGroupId(), null);
				if(tasteGroups.length > 0){
					orderFood.setTasteGroup(tasteGroups[0]);
				}
			}
		}
		
		return orderFoods.toArray(new OrderFood[orderFoods.size()]);
	}
	
	/**
	 * 
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws Exception
	 */
	public static OrderFood[] getDetailHistory(String extraCond, String orderClause) throws Exception {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return QueryOrderFoodDao.getDetailHistory(dbCon, extraCond, orderClause);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
}
