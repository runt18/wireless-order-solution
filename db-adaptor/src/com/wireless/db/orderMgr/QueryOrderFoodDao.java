package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.TasteGroup;

/**
 * The DB reflector is designed to the bridge between the OrderFood instance of
 * protocol and database.
 * 
 * @author Ying.Zhang
 * 
 */
public class QueryOrderFoodDao {

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

		sql = "SELECT OF.order_id, OF.food_alias, OF.taste_group_id, OF.hang_status, OF.is_temporary, " +
				" MAX(OF.restaurant_id) AS restaurant_id, MAX(OF.kitchen_alias) AS kitchen_alias, MAX(OF.kitchen_id) AS kitchen_id, " + 
				" MAX(OF.food_id) AS food_id, MAX(OF.name) AS name, MAX(OF.food_status) AS food_status, " +
				" MAX(OF.unit_price) AS unit_price, MAX(OF.waiter) AS waiter, MAX(OF.order_date) AS order_date, MAX(OF.discount) AS discount, " +
				" MAX(OF.dept_id) AS dept_id, MAX(OF.id) AS id, SUM(OF.order_count) AS order_sum, " +
				" MAX(O.type) AS type, MAX(O.table_id) AS table_id, MAX(O.table_alias) AS table_alias, " +
				" MAX(O.region_id) AS region_id, MAX(O.table_name) AS table_name, MAX(O.region_name) AS region_name, " +
				" MAX(OF.order_date) AS pay_datetime " +
				" FROM " +
				Params.dbName +	".order_food OF, " +
				Params.dbName + ".order O "	+
				" WHERE " +
				" OF.order_id = O.id "	+
				(extraCond == null ? "" : extraCond) +
				" GROUP BY OF.order_id, OF.food_alias, OF.taste_group_id, OF.hang_status, OF.is_temporary " + 
				" HAVING " +
				" order_sum > 0 " +
				(orderClause == null ? "" : " " + orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood();
			food.foodID = dbCon.rs.getInt("food_id");
			food.name = dbCon.rs.getString("name");
			food.setAliasId(dbCon.rs.getInt("food_alias"));
			food.restaurantID = dbCon.rs.getInt("restaurant_id");
			food.setStatus(dbCon.rs.getShort("food_status"));
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				food.makeTasteGroup(tasteGroupId, null, null);
			}
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.setPrice(dbCon.rs.getFloat("unit_price"));
			food.orderDate = dbCon.rs.getTimestamp("pay_datetime").getTime();
			food.waiter = dbCon.rs.getString("waiter");
			food.kitchen.restaurantID = dbCon.rs.getInt("restaurant_id");
			food.kitchen.kitchenID = dbCon.rs.getLong("kitchen_id");
			food.kitchen.aliasID = dbCon.rs.getShort("kitchen_alias");
			food.kitchen.dept.restaurantID = dbCon.rs.getInt("restaurant_id");
			food.kitchen.dept.deptID = dbCon.rs.getShort("dept_id");
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.hangStatus = dbCon.rs.getShort("hang_status");
			food.isTemporary = dbCon.rs.getBoolean("is_temporary");
			food.payManner = dbCon.rs.getShort("type");
			food.table.tableID = dbCon.rs.getInt("table_id");
			food.table.aliasID = dbCon.rs.getInt("table_alias");
			food.table.name = dbCon.rs.getString("table_name");
			food.table.regionID = dbCon.rs.getShort("region_id");
			food.childFoods = QueryMenu.queryComboByParent(food);
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
			  " MAX(OFH.dept_id) AS dept_id, MAX(OFH.id) AS id, SUM(OFH.order_count) AS order_sum, " +
			  " MAX(OH.type) AS type, MAX(OH.table_id) AS table_id, MAX(OH.table_alias) AS table_alias, " +
			  " MAX(OH.region_id) AS region_id, MAX(OH.table_name) AS table_name, MAX(OH.region_name) AS region_name, " +
			  " MAX(OH.order_date) AS pay_datetime "	+
			  " FROM " +
			  Params.dbName + ".order_food_history OFH, " +
			  Params.dbName	+ ".order_history OH " +
			  " WHERE " +
			  " OFH.order_id = OH.id "	+
			  (extraCond == null ? "" : extraCond) +
			  " GROUP BY OFH.order_id, OFH.food_alias, OFH.taste_group_id, OFH.is_temporary " +
			  " HAVING order_sum > 0 " +
			  (orderClause == null ? "" : " " + orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood();
			food.foodID = dbCon.rs.getInt("food_id");
			food.name = dbCon.rs.getString("name");
			food.setAliasId(dbCon.rs.getInt("food_alias"));
			food.restaurantID = dbCon.rs.getInt("restaurant_id");
			food.setStatus(dbCon.rs.getShort("food_status"));
			int tasteGroupId = dbCon.rs.getInt("taste_group_id");
			if(tasteGroupId != TasteGroup.EMPTY_TASTE_GROUP_ID){
				food.makeTasteGroup(tasteGroupId, null, null);
			}
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.setPrice(dbCon.rs.getFloat("unit_price"));
			food.waiter = dbCon.rs.getString("waiter");
			food.orderDate = dbCon.rs.getTimestamp("pay_datetime").getTime();
			food.kitchen.restaurantID = dbCon.rs.getInt("restaurant_id");
			food.kitchen.kitchenID = dbCon.rs.getLong("kitchen_id");
			food.kitchen.aliasID = dbCon.rs.getShort("kitchen_alias");
			food.kitchen.dept.restaurantID = dbCon.rs.getInt("restaurant_id");
			food.kitchen.dept.deptID = dbCon.rs.getShort("dept_id");
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.isTemporary = dbCon.rs.getBoolean("is_temporary");
			food.payManner = dbCon.rs.getShort("type");
			food.table.tableID = dbCon.rs.getInt("table_id");
			food.table.aliasID = dbCon.rs.getInt("table_alias");
			food.table.name = dbCon.rs.getString("table_name");
			food.table.regionID = dbCon.rs.getShort("region_id");
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

}
