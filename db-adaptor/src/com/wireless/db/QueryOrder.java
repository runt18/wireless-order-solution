package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;

public class QueryOrder {

	/**
	 * Get the order detail information to the specific table alias id.
	 * 
	 * @param pin
	 *            the pin to the terminal
	 * @param model
	 *            the model id to the terminal
	 * @param tableID
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The table associated with this order is idle.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order exec(int pin, short model, int tableID) throws BusinessException, SQLException {
		
		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();

			return exec(dbCon, pin, model, tableID);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information according to the specific order id. Note
	 *
	 * @param pin
	 *            the pin to terminal
	 * @param model
	 *            the model to terminal
	 * @param orderID
	 *            the order id to query
	 * @return the order detail information
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order execByID(int pin, short model, int orderID) throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();

			return execByID(dbCon, pin, model, orderID);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information to the specific table alias id.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			  the database connection
	 * @param pin
	 *            the pin to the terminal
	 * @param model
	 *            the model id to the terminal
	 * @param tableID
	 *            the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException
	 *             throws if one of cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The table to query does NOT exist.<br>
	 *             - The table associated with this order is idle.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement.
	 */
	public static Order exec(DBCon dbCon, int pin, short model, int tableID) throws BusinessException, SQLException {		

		Table table = QueryTable.exec(dbCon, pin, model, tableID);
			
		return execByID(dbCon, pin, model, Util.getUnPaidOrderID(dbCon, table));

	}

	/**
	 * Get the order detail information according to the specific order id. Note
	 * that the database should be connected before invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param pin
	 *            the pin to terminal
	 * @param model
	 *            the model to terminal
	 * @param orderID
	 *            the order id to query
	 * @return the order detail information
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order execByID(DBCon dbCon, int pin, short model, int orderID) throws BusinessException, SQLException{

		/**
		 * Get the related info to this order.
		 */
		String sql = "SELECT custom_num, table_id, table_name, table2_id, table2_name, restaurant_id, type, discount_type, category FROM `" + Params.dbName
				+ "`.`order` WHERE id=" + orderID;

		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		Order orderInfo = new Order();

		if(dbCon.rs.next()) {
			orderInfo.restaurant_id = dbCon.rs.getInt("restaurant_id");
			orderInfo.table_id = dbCon.rs.getShort("table_id");
			orderInfo.table_name = dbCon.rs.getString("table_name");
			orderInfo.table2_id = dbCon.rs.getShort("table2_id");
			orderInfo.table2_name = dbCon.rs.getString("table2_name");
			orderInfo.custom_num = dbCon.rs.getShort("custom_num");
			orderInfo.category = dbCon.rs.getShort("category");
			orderInfo.pay_manner = dbCon.rs.getShort("type");
			orderInfo.discount_type = dbCon.rs.getShort("discount_type");
		}else{
			throw new BusinessException("The order(id=" + orderID + ") does NOT exist.", ErrorCode.ORDER_NOT_EXIST);
		}
		dbCon.rs.close();
		
		/**
		 * Get the total and actual price if the order has been paid
		 */
		sql = "SELECT total_price, total_price_2 FROM `" + Params.dbName +
			   "`.`order` WHERE id=" + orderID +
			   " AND total_price IS NOT NULL";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			orderInfo.setTotalPrice(dbCon.rs.getFloat("total_price"));
			orderInfo.setActualPrice(dbCon.rs.getFloat("total_price_2"));
		}
		dbCon.rs.close();
		
		/**
		 * Get the type to handle the tail of price
		 */
		sql = "SELECT price_tail FROM " + Params.dbName +
			  ".setting WHERE restaurant_id=" + orderInfo.restaurant_id;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			orderInfo.price_tail = dbCon.rs.getShort("price_tail");
		}
		dbCon.rs.close();
		
		/**
		 * Get the minimum cost
		 */
		sql = "SELECT minimum_cost FROM " + Params.dbName +	
			  ".table WHERE restaurant_id=" + orderInfo.restaurant_id +
			  " AND alias_id=" + orderInfo.table_id;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			//orderInfo.minimum_cost = new Float(dbCon.rs.getFloat("minimum_cost") * 100).intValue();
			orderInfo.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
		}
		dbCon.rs.close();
		
		// query the food's id and order count associate with the order id for "order_food" table
		sql = "SELECT name, food_id, food_status, SUM(order_count) AS order_sum, unit_price, " +
				"discount, taste, taste_price, taste_id, taste_id2, taste_id3, hang_status, kitchen FROM `"
				+ Params.dbName
				+ "`.`order_food` WHERE order_id="
				+ orderID
				+ " GROUP BY food_id, taste_id, taste_id2, taste_id3, hang_status HAVING order_sum > 0";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Food> foods = new ArrayList<Food>();
		while (dbCon.rs.next()) {
			Food food = new Food();
			food.name = dbCon.rs.getString("name");
			food.alias_id = dbCon.rs.getInt("food_id");
			food.status = dbCon.rs.getShort("food_status");
			food.setCount(dbCon.rs.getFloat("order_sum"));
			food.setPrice(dbCon.rs.getFloat("unit_price"));
			food.kitchen = dbCon.rs.getShort("kitchen");
			food.setDiscount(dbCon.rs.getFloat("discount"));
			food.tastePref = dbCon.rs.getString("taste");
			food.setTastePrice(dbCon.rs.getFloat("taste_price"));
			food.tastes[0].alias_id = dbCon.rs.getInt("taste_id");
			food.tastes[1].alias_id = dbCon.rs.getInt("taste_id2");
			food.tastes[2].alias_id = dbCon.rs.getInt("taste_id3");
			food.hangStatus = dbCon.rs.getShort("hang_status");
			foods.add(food);
		}
		orderInfo.id = orderID;
		orderInfo.foods = foods.toArray(new Food[foods.size()]);

		return orderInfo;
	}
	
}
