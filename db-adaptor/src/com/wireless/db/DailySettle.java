package com.wireless.db;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Terminal;

public class DailySettle {
	/**
	 * The result to daily settle is as below.
	 * 1 - the amount to the order 
	 * 2 - the amount to the order detail
	 * 3 - the maximum order id 
	 */
	public static class Result{
		public int totalOrder;				//当日已结帐的账单数
		public int totalOrderDetail;		//当日已结帐的账单明细数
		public int totalShift;				//当日交班的记录数
		public int maxOrderID;				//order和order_history表的最大id
		public int maxOrderFoodID;			//order_food和order_food_history表的最大id
		public int maxShiftID;				//shift和shift_history表的最大id
		//public int[] restOrderID;			//日结操作前还没有进行交班操作的账单号
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
	 * Perform the daily settlement to all the restaurant.
	 * Note that the database should be connected before invoking this method.
	 * @return the result to daily settlement
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Result exec(DBCon dbCon) throws SQLException, BusinessException{
		
		String sql;
		
		ArrayList<Terminal> terms = new ArrayList<Terminal>();
		
		sql = "SELECT id AS restaurant_id FROM " + Params.dbName + ".restaurant WHERE id > " + Restaurant.RESERVED_7;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Terminal term = new Terminal();
			term.restaurant_id = dbCon.rs.getInt("restaurant_id");
			term.owner = "system";
			terms.add(term);
		}
		dbCon.rs.close();		
		
		Result result = new Result();
		for(Terminal term : terms){			
			Result eachResult = exec(dbCon, term, false);
			
			result.totalOrder += eachResult.totalOrder;
			result.totalOrderDetail += eachResult.totalOrderDetail;
			result.totalShift += eachResult.totalShift;
			result.maxOrderFoodID = eachResult.maxOrderFoodID;
			result.maxOrderID = eachResult.maxOrderID;
			result.maxShiftID = eachResult.maxShiftID;
		}
		
		return result;
	}
	
	/**
	 * Perform the daily settlement according to both pin and model.	
	 * 
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @return the result to daily settlement
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The member to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result exec(long pin, short model) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, VerifyPin.exec(dbCon, pin, model), true);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform the daily settlement according to both pin and model.	
	 * Note that the database should be connected before invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @return the result to daily settlement
	 * @throws BusinessException
	 *             throws if one the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The member to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Result exec(DBCon dbCon, long pin, short model) throws BusinessException, SQLException{
		return exec(dbCon, VerifyPin.exec(dbCon, pin, model), true);
	}
	
	/**
	 * Perform to daily settle according to a terminal.
	 * @param term
	 * 			  the terminal with both user name and restaurant id
	 * @return the result to daily settle
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException
	 */
	public static Result exec(Terminal term) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, true);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to daily settle according to a terminal.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 *            the database connection
	 * @param term
	 * 			  the terminal with both user name and restaurant id
	 * @param isManual
	 * 			  indicates whether the daily settle is manual or automation
	 * @return the result to daily settle
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException
	 */
	public static Result exec(DBCon dbCon, Terminal term, boolean isManual) throws SQLException, BusinessException{
		Result result = new Result();
		
		String sql;
		String onDuty = null;
		
		/**
		 * Get the date to last daily settlement.
		 * Make the 00:00 of today as on duty if no daily settle record exist before. 
		 */
		sql = " SELECT MAX(off_duty) FROM " + Params.dbName + ".daily_settle_history " +
			  " WHERE " +
			  " restaurant_id = " + term.restaurant_id;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			Timestamp offDuty = dbCon.rs.getTimestamp(1);
			if(offDuty != null){
				onDuty = "'" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDuty) + "'";
			}else{
				onDuty = "date_format(NOW(), '%Y-%m-%d')";
			}
		}else{
			onDuty = "date_format(NOW(), '%Y-%m-%d')";
		}
		
		//get the amount to order
		sql = "SELECT count(*) FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " +
			 (term.restaurant_id < 0 ? "" : "AND restaurant_id=" + term.restaurant_id);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.totalOrder = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//get the amount to order detail 
		sql = "SELECT count(*) FROM " + Params.dbName + ".order_food WHERE order_id IN (" +
		  	  "SELECT id FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " +
		  	  (term.restaurant_id < 0 ? "" : "AND restaurant_id=" + term.restaurant_id) + ")";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.totalOrderDetail = dbCon.rs.getInt(1);				
		}
		dbCon.rs.close();
		
		//get the amount to shift record
		sql = "SELECT count(*) FROM " + Params.dbName + ".shift " +
			  "WHERE 1=1 " +
			  (term.restaurant_id < 0 ? "AND restaurant_id <> " + Restaurant.ADMIN : "AND restaurant_id=" + term.restaurant_id);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.totalShift = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//calculate the max order id from both order and order_history
		sql = " SELECT MAX(id) + 1 FROM (" + 
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_history) AS all_order";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxOrderID = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//calculate the max order_food id from both order_food and order_food_history
		sql = " SELECT MAX(id) + 1 FROM (" +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_food " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_food_history) AS all_order_food";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxOrderFoodID = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//calculate the max shift id from both shift and shift_history
		sql = " SELECT MAX(id) + 1 FROM (" +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".shift " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".shift_history) AS all_shift";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxShiftID = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		final String orderItem = "`id`, `restaurant_id`,`order_date`, `total_price`, `total_price_2`, `custom_num`," + 
				"`waiter`, `type`, `discount_type`,`category`, `member_id`, `member`,`terminal_pin`, `terminal_model`, " +
				"`region_id`, `region_name`, `table_alias`, `table_name`, `table2_alias`, `table2_name`, `service_rate`, `comment`, `is_paid`";

		final String orderFoodItem = "`id`,`restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_date`, `order_count`," + 
					"`unit_price`,`name`, `food_status`, `taste`,`taste_price`," +
					"`taste_alias`, `taste2_alias`, `taste3_alias`, `taste_id`,`taste2_id`,`taste3_id`, " +
					"`discount`, `dept_id`, `kitchen_id`, `kitchen_alias`," +
					"`comment`,`waiter`,`is_temporary`,`is_paid`";
		
		final String shiftItem = "`id`, `restaurant_id`, `name`, `on_duty`, `off_duty`";
		
		try{
			dbCon.conn.setAutoCommit(false);
			
			//move the paid order from "order" to "order_history"
			sql = "INSERT INTO " + Params.dbName + ".order_history (" + orderItem + ") " + 
				  "SELECT " + orderItem + " FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " + 
				  (term.restaurant_id < 0 ? "" : "AND restaurant_id=" + term.restaurant_id);
			dbCon.stmt.executeUpdate(sql);
			
			//move the paid order details from "order_food" to "order_food_history" 
			sql = "INSERT INTO " + Params.dbName + ".order_food_history (" + orderFoodItem + ") " +
				  "SELECT " + orderFoodItem + " FROM " + Params.dbName + ".order_food WHERE order_id IN (" +
				  "SELECT id FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " +
				  (term.restaurant_id < 0 ? "" : "AND restaurant_id=" + term.restaurant_id) +
				  ")";
			dbCon.stmt.executeUpdate(sql);
			
			//move the shift record from "shift" to "shift_history"
			sql = "INSERT INTO " + Params.dbName + ".shift_history (" + shiftItem + ") " +
				  "SELECT " + shiftItem + " FROM " + Params.dbName + ".shift " +
				  "WHERE 1=1 " +
				  (term.restaurant_id < 0 ? "" : "AND restaurant_id=" + term.restaurant_id);
			dbCon.stmt.executeUpdate(sql);
			
			sql = "INSERT INTO " + Params.dbName + ".daily_settle_history (`restaurant_id`, `name`, `on_duty`, `off_duty`) VALUES (" +
				  term.restaurant_id + ", " +
				  "'" + (term.owner == null ? "" : term.owner) + "', " +
				  onDuty + ", " +
				  "NOW()" +
				  ")";
			/**
			 * Insert the daily settle record in case of manual.
			 */
			if(isManual){
				dbCon.stmt.executeUpdate(sql);				
			}else{
				/**
				 * Insert the record if the amount of rest order is greater than zero in case of automation.
				 */
				if(result.totalOrder > 0){
					dbCon.stmt.executeUpdate(sql);
				}
			}
			
			//delete the order details from "order_food"
			sql = "DELETE FROM " + Params.dbName + ".order_food WHERE order_id IN (" +
				  "SELECT id FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " +
				  (term.restaurant_id < 0 ? "" : "AND restaurant_id=" + term.restaurant_id) +
				  ")";
			dbCon.stmt.executeUpdate(sql);
			
			//delete the order from "order"
			sql = "DELETE FROM " + Params.dbName + ".order WHERE total_price IS NOT NULL " +
				  (term.restaurant_id < 0 ? "" : "AND restaurant_id=" + term.restaurant_id);
			dbCon.stmt.executeUpdate(sql);
			
			//delete the shift record from "shift"
			sql = "DELETE FROM " + Params.dbName + ".shift " +
				  "WHERE 1=1 " +
				  (term.restaurant_id < 0 ? "" : "AND restaurant_id=" + term.restaurant_id);
			dbCon.stmt.executeUpdate(sql);
			
			//delete the order_food record to root
			sql = "DELETE FROM " + Params.dbName + ".order_food WHERE order_id IN (SELECT id FROM " + 
				  Params.dbName + ".order WHERE restaurant_id=" + Restaurant.ADMIN + ")";
			dbCon.stmt.executeUpdate(sql);
			
			//delete the order record to root
			sql = "DELETE FROM " + Params.dbName + ".order WHERE restaurant_id=" + Restaurant.ADMIN;
			dbCon.stmt.executeUpdate(sql);
			
			//delete the shift record to root
			sql = "DELETE FROM " + Params.dbName + ".shift WHERE restaurant_id=" + Restaurant.ADMIN;
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
			
			//insert a shift record with the max shift id to root
			sql = "INSERT INTO " + Params.dbName + ".shift (`id`, `restaurant_id`) VALUES (" +
				  result.maxShiftID + ", " +
				  Restaurant.ADMIN +
				  ")";
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
	
	/**
	 * Check to see whether the paid order is exist before daily settlement to a specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal attached with the restaurant id
	 * @return
	 * 			an integer array holding the paid order id, 
	 * 			return int[0] if no paid order exist. 
	 * @throws SQLException
	 */
	public static int[] check(DBCon dbCon, Terminal term) throws SQLException{
		String sql;
		int[] restOrderID = new int[0];
		
		/**
		 * Get the last off duty date from both shift and shift_history,
		 */
		String lastOffDuty;
		sql = "SELECT MAX(off_duty) FROM (" +
		 	  "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurant_id + " UNION " +
			  "SELECT off_duty FROM " + Params.dbName + ".shift_history WHERE restaurant_id=" + term.restaurant_id + ") AS all_off_duty";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			Timestamp offDuty = dbCon.rs.getTimestamp(1);
			if(offDuty == null){
				lastOffDuty = "2011-07-30 00:00:00";
			}else{
				lastOffDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDuty);
			}
		}else{
			lastOffDuty = "2011-07-30 00:00:00";
		}
		dbCon.rs.close();

		
		/**
		 * In the case perform the daily settle to a specific restaurant,
		 * get the paid orders which has NOT been shifted between the last off duty and now,
		 */
		sql = "SELECT id FROM " + Params.dbName + ".order WHERE " +
			  "restaurant_id=" + term.restaurant_id + " AND " +
			  "total_price IS NOT NULL" + " AND " +
			  "order_date BETWEEN " +
			  "'" + lastOffDuty + "'" + " AND " + "NOW()";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Integer> orderIDs = new ArrayList<Integer>();
		while(dbCon.rs.next()){
			orderIDs.add(dbCon.rs.getInt("id"));
		}
		dbCon.rs.close();
		
		restOrderID = new int[orderIDs.size()];
		for(int i = 0; i < restOrderID.length; i++){
			restOrderID[i] = orderIDs.get(i).intValue();
		}
		
		return restOrderID;
	}
	
}
