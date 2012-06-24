package com.wireless.dbReflect;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Taste;

/**
 * The DB reflector is designed to the bridge between the OrderFood instance of
 * protocol and database.
 * 
 * @author Ying.Zhang
 * 
 */
public class OrderFoodReflector {

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

		sql = "SELECT A.order_id, A.food_alias, A.taste_alias, A.taste2_alias, A.taste3_alias, A.taste_tmp_alias, A.hang_status, A.is_temporary, "
				+ " MAX(A.kitchen_alias) AS kitchen_alias, MAX(A.kitchen_id) AS kitchen_id, " 
				+ " MAX(A.food_id) AS food_id, MAX(A.name) AS name, MAX(A.food_status) AS food_status, " 
				+ " MAX(A.unit_price) AS unit_price, MAX(A.waiter) AS waiter, MAX(A.order_date) AS order_date,  MAX(A.discount) AS discount, "
				+ " MAX(A.taste) AS taste, MAX(A.taste_price) AS taste_price, "
				+ " MAX(A.taste_id) AS taste_id, MAX(A.taste2_id) AS taste2_id, MAX(A.taste3_id) AS taste3_id, " 
				+ " MAX(A.dept_id) AS dept_id, MAX(A.id) AS id, SUM(A.order_count) AS order_sum, "
				+ " MAX(B.type) AS type, MAX(B.table_id) AS table_id, MAX(B.table_alias) AS table_alias, "
				+ " MAX(B.region_id) AS region_id, MAX(B.table_name) AS table_name, MAX(B.region_name) AS region_name, "
				+ " MAX(A.order_date) AS pay_datetime, date_format(MAX(A.order_date), '%Y-%m-%d') AS pay_date, "
				+ " MAX(A.taste_tmp) AS taste_tmp, MAX(A.taste_tmp_price) AS taste_tmp_price "
				+ " FROM " 
				+ Params.dbName
				+ ".order_food A, "
				+ Params.dbName
				+ ".order B "
				+ " WHERE A.order_id = B.id "
				+ (extraCond == null ? "" : extraCond)
				+ " GROUP BY A.order_id, A.food_alias, A.taste_alias, A.taste2_alias, A.taste3_alias, A.taste_tmp_alias, A.hang_status, A.is_temporary "
				+ " HAVING order_sum > 0 " 
				+ (orderClause == null ? "" : " " + orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood();
			food.foodID = dbCon.rs.getInt("food_id");
			food.name = dbCon.rs.getString("name");
			food.aliasID = dbCon.rs.getInt("food_alias");
			food.status = dbCon.rs.getShort("food_status");
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.setPrice(dbCon.rs.getFloat("unit_price"));
			food.orderDate = dbCon.rs.getTimestamp("pay_datetime").getTime();
			food.waiter = dbCon.rs.getString("waiter");
			food.kitchen.kitchenID = dbCon.rs.getLong("kitchen_id");
			food.kitchen.aliasID = dbCon.rs.getShort("kitchen_alias");
			food.kitchen.dept.deptID = dbCon.rs.getShort("dept_id");
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.tasteNormalPref = dbCon.rs.getString("taste");
			food.setTasteNormalPrice(dbCon.rs.getFloat("taste_price"));
			food.tastes[0].tasteID = dbCon.rs.getInt("taste_id");
			food.tastes[1].tasteID = dbCon.rs.getInt("taste2_id");
			food.tastes[2].tasteID = dbCon.rs.getInt("taste3_id");
			food.tastes[0].aliasID = dbCon.rs.getInt("taste_alias");
			food.tastes[1].aliasID = dbCon.rs.getInt("taste2_alias");
			food.tastes[2].aliasID = dbCon.rs.getInt("taste3_alias");
			food.hangStatus = dbCon.rs.getShort("hang_status");
			food.isTemporary = dbCon.rs.getBoolean("is_temporary");
			food.payManner = dbCon.rs.getShort("type");
			food.table.tableID = dbCon.rs.getInt("table_id");
			food.table.aliasID = dbCon.rs.getInt("table_alias");
			food.table.name = dbCon.rs.getString("table_name");
			food.table.regionID = dbCon.rs.getShort("region_id");
			String tmpTastePref = dbCon.rs.getString("taste_tmp");
			if(tmpTastePref != null){
				food.tmpTaste = new Taste();
				food.tmpTaste.preference = tmpTastePref;
				food.tmpTaste.aliasID = dbCon.rs.getInt("taste_tmp_alias");
				food.tmpTaste.setPrice(dbCon.rs.getFloat("taste_tmp_price"));
			}
			orderFoods.add(food);
		}
		dbCon.rs.close();
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

		sql = "SELECT A.order_id, A.food_alias, A.taste_alias, A.taste2_alias, A.taste3_alias, A.taste_tmp_alias, A.is_temporary, "
				+ " MAX(A.kitchen_alias) AS kitchen_alias, MAX(A.kitchen_id) AS kitchen_id, " 
				+ " MAX(A.food_id) AS food_id, MAX(A.name) AS name, MAX(A.food_status) AS food_status, " 
				+ " MAX(A.unit_price) AS unit_price, MAX(A.waiter) AS waiter, MAX(A.order_date) AS order_date,  MAX(A.discount) AS discount, "
				+ " MAX(A.taste) AS taste, MAX(A.taste_price) AS taste_price, "
				+ " MAX(A.taste_id) AS taste_id, MAX(A.taste2_id) AS taste2_id, MAX(A.taste3_id) AS taste3_id, " 
				+ " MAX(A.dept_id) AS dept_id, MAX(A.id) AS id, SUM(A.order_count) AS order_sum, "
				+ " MAX(A.taste_tmp) AS taste_tmp, MAX(A.taste_tmp_price) AS taste_tmp_price, "
				+ " MAX(B.type) AS type, MAX(B.table_id) AS table_id, MAX(B.table_alias) AS table_alias, "
				+ " MAX(B.region_id) AS region_id, MAX(B.table_name) AS table_name, MAX(B.region_name) AS region_name, "
				+ " MAX(B.order_date) AS pay_datetime, date_format(MAX(B.order_date), '%Y-%m-%d') AS pay_date "
				+ " FROM " 
				+ Params.dbName
				+ ".order_food_history A, "
				+ Params.dbName
				+ ".order_history B "
				+ " WHERE A.order_id = B.id "
				+ (extraCond == null ? "" : extraCond)
				+ " GROUP BY A.order_id, A.food_alias, A.taste_alias, A.taste2_alias, A.taste3_alias, A.taste_tmp_alias, A.is_temporary "
				+ " HAVING order_sum > 0 " 
				+ (orderClause == null ? "" : " " + orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<OrderFood> orderFoods = new ArrayList<OrderFood>();
		while (dbCon.rs.next()) {
			OrderFood food = new OrderFood();
			food.foodID = dbCon.rs.getInt("food_id");
			food.name = dbCon.rs.getString("name");
			food.aliasID = dbCon.rs.getInt("food_alias");
			food.status = dbCon.rs.getShort("food_status");
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.setPrice(dbCon.rs.getFloat("unit_price"));
			food.waiter = dbCon.rs.getString("waiter");
			food.orderDate = dbCon.rs.getTimestamp("pay_datetime").getTime();
			food.kitchen.kitchenID = dbCon.rs.getLong("kitchen_id");
			food.kitchen.aliasID = dbCon.rs.getShort("kitchen_alias");
			food.kitchen.dept.deptID = dbCon.rs.getShort("dept_id");
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.tasteNormalPref = dbCon.rs.getString("taste");
			food.setTasteNormalPrice(dbCon.rs.getFloat("taste_price"));
			food.tastes[0].tasteID = dbCon.rs.getInt("taste_id");
			food.tastes[1].tasteID = dbCon.rs.getInt("taste2_id");
			food.tastes[2].tasteID = dbCon.rs.getInt("taste3_id");
			food.tastes[0].aliasID = dbCon.rs.getInt("taste_alias");
			food.tastes[1].aliasID = dbCon.rs.getInt("taste2_alias");
			food.tastes[2].aliasID = dbCon.rs.getInt("taste3_alias");
			// food.hangStatus = dbCon.rs.getShort("hang_status");
			food.isTemporary = dbCon.rs.getBoolean("is_temporary");
			food.payManner = dbCon.rs.getShort("type");
			food.table.tableID = dbCon.rs.getInt("table_id");
			food.table.aliasID = dbCon.rs.getInt("table_alias");
			food.table.name = dbCon.rs.getString("table_name");
			food.table.regionID = dbCon.rs.getShort("region_id");
			String tmpTastePref = dbCon.rs.getString("taste_tmp");
			if(tmpTastePref != null){
				food.tmpTaste = new Taste();
				food.tmpTaste.preference = tmpTastePref;
				food.tmpTaste.aliasID = dbCon.rs.getInt("taste_tmp_alias");
				food.tmpTaste.setPrice(dbCon.rs.getFloat("taste_tmp_price"));
			}
			orderFoods.add(food);
		}
		dbCon.rs.close();
		return orderFoods.toArray(new OrderFood[orderFoods.size()]);
	}

}
