package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Restaurant;

public class DailySettle {
	/**
	 * The result to daily settle is as below.
	 * 1 - the amount to the order 
	 * 2 - the amount to the order detail
	 * 3 - the maximum order id 
	 */
	public static class Result{
		public int totalOrder;
		public int totalOrderDetail;
		public int maxOrderID;
		public int maxOrderFoodID;
	}

	/**
	 * Perform the daily settlement to all the restaurant.
	 * 
	 * @return the result to daily settlement
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Result exec() throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform the daily settlement to a specific restaurant.
	 * 
	 * @param restaurantID
	 *            the restaurant id to daily settle
	 * @return the result to daily settlement
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Result exec(int restaurantID) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, restaurantID);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform the daily settlement to all the restaurant.
	 * Note that the database should be connected before invoking this method.
	 * @return the result to daily settlement
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Result exec(DBCon dbCon) throws SQLException, BusinessException{
		return exec(dbCon, -1);
	}
	
	/**
	 * Perform to daily settle according to the restaurant id.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 *            the database connection
	 * @param restaurantID
	 *            the restaurant id to perform daily settle, -1 means all
	 *            restaurants
	 * @return the result to daily settle
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException
	 */
	public static Result exec(DBCon dbCon, int restaurantID) throws SQLException, BusinessException{
		Result result = new Result();
		//get the amount to order
		String sql = "SELECT count(*) FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " +
					 (restaurantID < 0 ? "" : "AND restaurant_id=" + restaurantID);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.totalOrder = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//get the amount to order detail 
		sql = "SELECT count(*) FROM " + Params.dbName + ".order_food WHERE order_id IN (" +
		  	  "SELECT id FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " +
		  	  (restaurantID < 0 ? "" : "AND restaurant_id=" + restaurantID) + ")";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.totalOrderDetail = dbCon.rs.getInt(1);				
		}
		dbCon.rs.close();
		
		//get the max order id from both order and order_history
		sql = "SELECT MAX(`id`) + 1 FROM (" + "SELECT id FROM " + Params.dbName + 
			  ".order UNION SELECT id FROM " + Params.dbName + 
			  ".order_history) AS all_order";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxOrderID = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//get the max order_food id from both order_food and order_food_history
		sql = "SELECT MAX(`id`) + 1 FROM (SELECT id FROM " + Params.dbName +
			  ".order_food UNION SELECT id FROM " + Params.dbName +
			  ".order_food_history) AS all_order";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxOrderFoodID = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//delete the order_food record to root
		sql = "DELETE FROM " + Params.dbName + ".order_food WHERE order_id IN (SELECT id FROM " + 
			  Params.dbName + ".order WHERE restaurant_id=" + Restaurant.ADMIN + ")";
		dbCon.stmt.executeUpdate(sql);
		
		//delete the order record to root
		sql = "DELETE FROM " + Params.dbName + ".order WHERE restaurant_id=" + Restaurant.ADMIN;
		dbCon.stmt.executeUpdate(sql);
		
		//insert a order record with the max order id to root
		sql = "INSERT INTO " + Params.dbName + ".order (`id`, `restaurant_id`, `order_date`) VALUES (" + 
			  result.maxOrderID + ", " +
			  Restaurant.ADMIN + ", " +
			  0 +
			  ")";
		dbCon.stmt.executeUpdate(sql);
		
		//insert a order_food record with the max order_food id to root
		sql = "INSERT INTO " + Params.dbName + ".order_food (`id`, `order_id`, `order_date`) VALUES (" +
			  result.maxOrderFoodID + ", " +
			  result.maxOrderID + ", " +
			  0 +
			  ")";
		dbCon.stmt.executeUpdate(sql);
		
		final String orderItem = "`id`, `restaurant_id`,`order_date`, `total_price`, `total_price_2`, `custom_num`," + 
				"`waiter`, `type`, `discount_type`,`category`, `member_id`, `member`,`terminal_pin`, `terminal_model`, " +
				"`region_id`, `region_name`, `table_alias`, `table_name`, `table2_alias`, `table2_name`, `service_rate`, `comment`, `is_paid`";

		final String orderFoodItem = "`id`,`restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_date`, `order_count`," + 
					"`unit_price`,`name`, `food_status`, `taste`,`taste_price`," +
					"`taste_alias`, `taste2_alias`, `taste3_alias`, `taste_id`,`taste2_id`,`taste3_id`, " +
					"`discount`, `dept_id`, `kitchen_id`, `kitchen_alias`," +
					"`comment`,`waiter`,`is_temporary`,`is_paid`";
		
		try{
			dbCon.conn.setAutoCommit(false);
			
			//move the paid order from "order" to "order_history"
			sql = "INSERT INTO " + Params.dbName + ".order_history (" + orderItem + ") " + 
				  "SELECT " + orderItem + " FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " + 
				  (restaurantID < 0 ? "" : "AND restaurant_id=" + restaurantID);
			dbCon.stmt.executeUpdate(sql);
			
			//move the paid order details from "order_food" to "order_food_history" 
			sql = "INSERT INTO " + Params.dbName + ".order_food_history (" + orderFoodItem + ") " +
				  "SELECT " + orderFoodItem + " FROM " + Params.dbName + ".order_food WHERE order_id IN (" +
				  "SELECT id FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " +
				  (restaurantID < 0 ? "" : "AND restaurant_id=" + restaurantID) +
				  ")";
			dbCon.stmt.executeUpdate(sql);
			
			//delete the order details from "order_food"
			sql = "DELETE FROM " + Params.dbName + ".order_food WHERE order_id IN (" +
				  "SELECT id FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " +
				  (restaurantID < 0 ? "" : "AND restaurant_id=" + restaurantID) +
				  ")";
			dbCon.stmt.executeUpdate(sql);
			
			//delete the order from "order"
			sql = "DELETE FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " +
				  (restaurantID < 0 ? "" : "AND restaurant_id=" + restaurantID);
			dbCon.stmt.executeUpdate(sql);
			
			dbCon.conn.commit();
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw new BusinessException(e.getMessage());
			
		}finally{
			dbCon.conn.setAutoCommit(true);
		}
		
		return result;
	}
}
