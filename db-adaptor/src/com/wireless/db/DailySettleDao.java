package com.wireless.db;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.TasteGroup;
import com.wireless.protocol.Terminal;

public class DailySettleDao {
	
	public enum SettleType{
		MANUAL, 
		AUTO_MATION
	}
	
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
		public int maxTasteGroupID;			//taste_group和taste_group_history表的最大id
		public int maxNormalTasteGroupID;	//normal_taste_group和normal_taste_group_history表的最大id
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
	 * Perform the daily settlement to the restaurant whose order record exceeds 1 day.
	 * Note that the database should be connected before invoking this method.
	 * @return the result to daily settlement
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static Result exec(DBCon dbCon) throws SQLException, BusinessException{
		
		String sql;
		
		ArrayList<Terminal> terms = new ArrayList<Terminal>();
		
		//Filter the restaurant whose order record exceed 1 day.
		sql = " SELECT restaurant_id " +
			  " FROM " + Params.dbName + ".order " +
			  " WHERE " +
			  " restaurant_id > " + Restaurant.RESERVED_7 +
			  " AND " +
			  " is_paid = 1 " +
			  " GROUP BY restaurant_id " +
			  " HAVING TO_DAYS(NOW()) - TO_DAYS(MIN(order_date)) > 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Terminal term = new Terminal();
			term.restaurantID = dbCon.rs.getInt("restaurant_id");
			term.owner = "system";
			terms.add(term);
		}
		dbCon.rs.close();		
		
		Result result = new Result();
		for(Terminal term : terms){			
			Result eachResult = exec(dbCon, term, SettleType.AUTO_MATION);
			
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
			return exec(dbCon, VerifyPin.exec(dbCon, pin, model), SettleType.MANUAL);
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
		return exec(dbCon, VerifyPin.exec(dbCon, pin, model), SettleType.MANUAL);
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
			return exec(dbCon, term, SettleType.MANUAL);
			
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
	 * @param type
	 * 			  indicates whether the daily settle is manual or automation
	 * @return the result to daily settle
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 * @throws BusinessException
	 */
	public static Result exec(DBCon dbCon, Terminal term, SettleType type) throws SQLException, BusinessException{
		Result result = new Result();
		
		String sql;
		String onDuty = null;
		
		/**
		 * Get the date to last daily settlement.
		 * Make the 00:00 of today as on duty if no daily settle record exist before. 
		 */
		sql = " SELECT MAX(off_duty) FROM " + Params.dbName + ".daily_settle_history " +
			  " WHERE " +
			  " restaurant_id = " + term.restaurantID;
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
		
		String paidOrderCond = null;
		
		//Get the amount and id to paid orders
		sql = " SELECT id FROM " + Params.dbName + ".order " +
			  " WHERE " +
			  " is_paid = 1 " +
			 (term.restaurantID < 0 ? "" : "AND restaurant_id=" + term.restaurantID);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			if(result.totalOrder == 0){
				paidOrderCond += dbCon.rs.getInt("id");
			}else{
				paidOrderCond += "," + dbCon.rs.getInt("id");
			}
			result.totalOrder++;
		}
		dbCon.rs.close();		
		
		if(paidOrderCond != null){
			//get the amount to order detail 
			sql = " SELECT COUNT(*) FROM " + Params.dbName + ".order_food WHERE order_id IN (" + paidOrderCond + ")";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				result.totalOrderDetail = dbCon.rs.getInt(1);				
			}
			dbCon.rs.close();
		}
		
		//get the amount to shift record
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".shift " +
			  " WHERE 1=1 " +
			  (term.restaurantID < 0 ? "AND restaurant_id <> " + Restaurant.ADMIN : "AND restaurant_id=" + term.restaurantID);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.totalShift = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//Calculate the max order id from both today and history.
		sql = " SELECT MAX(id) + 1 FROM (" + 
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_history) AS all_order";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxOrderID = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//Calculate the max order food id from both today and history.
		sql = " SELECT MAX(id) + 1 FROM (" +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_food " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".order_food_history) AS all_order_food";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxOrderFoodID = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//Calculate the max shift id from both today and history.
		sql = " SELECT MAX(id) + 1 FROM (" +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".shift " +
			  " UNION " +
			  " SELECT MAX(id) AS id FROM " + Params.dbName + ".shift_history) AS all_shift";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxShiftID = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//Calculate the max taste group id from both today and history.
		sql = " SELECT MAX(taste_group_id) + 1 " +
			  " FROM " +
			  " (SELECT MAX(taste_group_id) AS taste_group_id FROM " + Params.dbName + ".taste_group" +
			  " UNION " +
			  " SELECT MAX(taste_group_id) AS taste_group_id FROM " + Params.dbName + ".taste_group_history) AS all_taste_group";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxTasteGroupID = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//Calculate the max normal taste group id from both today and history
		sql = " SELECT MAX(normal_taste_group_id) + 1 " +
			  " FROM " +
			  " (SELECT MAX(normal_taste_group_id) AS normal_taste_group_id FROM " + Params.dbName + ".normal_taste_group" +
			  " UNION " +
			  " SELECT MAX(normal_taste_group_id) AS normal_taste_group_id FROM " + Params.dbName + ".normal_taste_group_history) AS all_normal_taste_group";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			result.maxNormalTasteGroupID = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		int orderIdToAdmin = 0;
		//Get the order id attached to admin.
		sql = "SELECT id FROM " + Params.dbName + ".order WHERE restaurant_id=" + Restaurant.ADMIN;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			orderIdToAdmin = dbCon.rs.getInt("id");
		}
		dbCon.rs.close();
		
		int ofIdToAdmin = 0;
		int tgIdToAdmin = 0;
		//Get the order food id and taste group id attached to admin.
		sql = "SELECT id, taste_group_id FROM " + Params.dbName + ".order_food WHERE order_id=" + orderIdToAdmin;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			ofIdToAdmin = dbCon.rs.getInt("id");
			tgIdToAdmin = dbCon.rs.getInt("taste_group_id");
		}
		dbCon.rs.close();
		
		
		final String orderItem = "`id`, `seq_id`, `restaurant_id`, `birth_date`, `order_date`, " +
				"`cancel_price`, `discount_price`, `gift_price`, `repaid_price`, `erase_price`, `total_price`, `total_price_2`, `custom_num`," + 
				"`waiter`, `type`, `category`, `member_id`, `member`,`terminal_pin`, `terminal_model`, " +
				"`region_id`, `region_name`, `table_alias`, `table_name`, `table2_alias`, `table2_name`, `service_rate`, `comment`";

		final String orderFoodItem = "`id`,`restaurant_id`, `order_id`, `food_id`, `food_alias`, `order_date`, `order_count`," + 
					"`unit_price`,`name`, `food_status`, `taste_group_id`, `cancel_reason_id`, `cancel_reason`," +
					"`discount`, `dept_id`, `kitchen_id`, `kitchen_alias`," +
					"`comment`,`waiter`,`is_temporary`,`is_paid`";
		
		final String tasteGroupItem = "`taste_group_id`, " +
									  "`normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`, " +
									  "`tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price`";
		
		final String normalTasteGroupItem = "`normal_taste_group_id`, `taste_id`";
		
		final String shiftItem = "`id`, `restaurant_id`, `name`, `on_duty`, `off_duty`";
		
		try{
			dbCon.conn.setAutoCommit(false);
			
			if(result.totalOrder > 0){
			
				//Move the paid order from "order" to "order_history".
				sql = " INSERT INTO " + Params.dbName + ".order_history (" + orderItem + ") " + 
					  " SELECT " + orderItem + " FROM " + Params.dbName + ".order WHERE id IN " + "(" + paidOrderCond + ")";
				dbCon.stmt.executeUpdate(sql);
			
				//Move the paid order details from "order_food" to "order_food_history".
				sql = " INSERT INTO " + Params.dbName + ".order_food_history (" + orderFoodItem + ") " +
					  " SELECT " + orderFoodItem + " FROM " + Params.dbName + ".order_food " +
					  " WHERE " +
					  " order_id IN ( " + paidOrderCond + " ) ";
				dbCon.stmt.executeUpdate(sql);
			
				//Move the paid order taste group from 'taste_group' to 'taste_group_history' except the empty taste group.
				sql = " INSERT INTO " + Params.dbName + ".taste_group_history (" + tasteGroupItem + " ) " +
					  " SELECT " + tasteGroupItem + " FROM " + Params.dbName + ".taste_group" +
					  " WHERE " +
					  " taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID +
					  " AND taste_group_id IN (" +
					  " SELECT taste_group_id FROM " + Params.dbName + ".order_food WHERE order_id IN (" + paidOrderCond + " ) " +
					  " ) ";
				dbCon.stmt.executeUpdate(sql);
				
				//Move the paid order normal taste group from 'normal_taste_group' to 'normal_taste_group_history' except the empty normal taste group.
				sql = " INSERT INTO " + Params.dbName + ".normal_taste_group_history (" + normalTasteGroupItem + ")" +
				      " SELECT " + normalTasteGroupItem + " FROM " + Params.dbName + ".normal_taste_group" +
					  " WHERE " +
				      " normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID +
				      " AND " +
				      " normal_taste_group_id IN(" +
				      " SELECT normal_taste_group_id " +
				      " FROM " + Params.dbName + ".order_food OF " + " JOIN " + Params.dbName + ".taste_group TG" +
				      " ON OF.taste_group_id = TG.taste_group_id " +
				      " WHERE " +
				      " OF.order_id IN (" + paidOrderCond + ")" +
				      " ) ";
				dbCon.stmt.executeUpdate(sql);
			}
				  
			//Move the shift record from 'shift' to 'shift_history'.
			sql = " INSERT INTO " + Params.dbName + ".shift_history (" + shiftItem + ") " +
				  " SELECT " + shiftItem + " FROM " + Params.dbName + ".shift " +
				  " WHERE " + (term.restaurantID < 0 ? "" : "restaurant_id=" + term.restaurantID);
			dbCon.stmt.executeUpdate(sql);
			
			//Create the daily settle record
			sql = " INSERT INTO " + Params.dbName + ".daily_settle_history (`restaurant_id`, `name`, `on_duty`, `off_duty`) VALUES (" +
				  term.restaurantID + ", " +
				  "'" + (term.owner == null ? "" : term.owner) + "', " +
				  onDuty + ", " +
				  " NOW() " +
				  " ) ";
			/**
			 * Insert the daily settle record in case of manual.
			 */
			if(type == SettleType.MANUAL){
				dbCon.stmt.executeUpdate(sql);				
			}else{
				/**
				 * Insert the record if the amount of rest order is greater than zero in case of automation.
				 */
				if(result.totalOrder > 0){
					dbCon.stmt.executeUpdate(sql);
				}
			}
			
			//Delete the paid order normal taste group except the empty normal taste group
			sql = " DELETE FROM " + Params.dbName + ".normal_taste_group " +
				  " WHERE " +
				  " normal_taste_group_id IN (" +
				  " SELECT normal_taste_group_id " +
				  " FROM " + Params.dbName + ".order_food OF " + " JOIN " + Params.dbName + ".taste_group TG" +
				  " ON OF.taste_group_id = TG.taste_group_id " +
				  " WHERE " +
				  " OF.order_id IN (" + paidOrderCond + ")" +
				  " ) " + 
				  " AND " +
				  " normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID;
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the paid order taste group except the empty taste group
			sql = " DELETE FROM " + Params.dbName + ".taste_group" +
				  " WHERE " +
				  " taste_group_id IN (" +
				  " SELECT taste_group_id FROM " + Params.dbName + ".order_food" +
				  " WHERE " + 
				  " order_id IN (" + paidOrderCond + ")" +
				  " ) " + 
				  " AND " +
				  " taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID;
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the paid order food from 'order_food' table.
			sql = "DELETE FROM " + Params.dbName + ".order_food WHERE order_id IN (" + paidOrderCond + ")";
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the paid order from "order" table.
			sql = "DELETE FROM " + Params.dbName + ".order WHERE id IN ( " + paidOrderCond + ")";
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the shift record from "shift".
			sql = "DELETE FROM " + Params.dbName + ".shift WHERE " + (term.restaurantID < 0 ? "" : "restaurant_id=" + term.restaurantID);
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the taste group record attached to admin.
			sql = " DELETE FROM " + Params.dbName + ".taste_group WHERE taste_group_id = " + tgIdToAdmin;
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the order_food record attached to admin.
			sql = " DELETE FROM " + Params.dbName + ".order_food WHERE id = " + ofIdToAdmin;
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the order record with max id.
			sql = " DELETE FROM " + Params.dbName + ".order WHERE restaurant_id=" + Restaurant.ADMIN;
			dbCon.stmt.executeUpdate(sql);
			
			//delete the shift record to root
			sql = " DELETE FROM " + Params.dbName + ".shift WHERE restaurant_id=" + Restaurant.ADMIN;
			dbCon.stmt.executeUpdate(sql);
			
			//Insert a order record with the max order id to root.
			sql = "INSERT INTO " + Params.dbName + ".order (`id`, `restaurant_id`, `order_date`) VALUES (" + 
				  result.maxOrderID + ", " +
				  Restaurant.ADMIN + ", " +
				  0 +
				  ")";
			dbCon.stmt.executeUpdate(sql);
			
			//Insert a order_food record with the max order food id, max taste group id to root.
			sql = "INSERT INTO " + Params.dbName + ".order_food (`id`, `order_id`, `taste_group_id`, `order_date`) VALUES (" +
				  result.maxOrderFoodID + ", " +
				  result.maxOrderID + ", " +
				  result.maxTasteGroupID + ", " +
				  0 +
				  ")";
			dbCon.stmt.executeUpdate(sql);
			
			//Insert a record with the max taste group id and max normal taste group id.
			sql = " INSERT INTO " + Params.dbName + ".taste_group" +
				  " (`taste_group_id`, `normal_taste_group_id`) " +
				  " VALUES " +
				  " ( " +
				  result.maxTasteGroupID + ", " +
				  result.maxNormalTasteGroupID +
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
			
			//Insert a shift record with the max shift id to root.
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
		 	  "SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id=" + term.restaurantID + " UNION " +
			  "SELECT off_duty FROM " + Params.dbName + ".shift_history WHERE restaurant_id=" + term.restaurantID + ") AS all_off_duty";
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
		sql = " SELECT id FROM " + Params.dbName + ".order WHERE " +
			  " restaurant_id = " + term.restaurantID + " AND " +
			  " is_paid = 1 " + " AND " +
			  " order_date BETWEEN " +
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
