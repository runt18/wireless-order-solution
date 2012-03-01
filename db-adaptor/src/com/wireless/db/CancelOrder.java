package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class CancelOrder {
	/**
	 * Cancel the unpaid order according to the specific table if the table is busy.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param tableID the table alias id to query
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 						     - The table associated with this order is IDLE.<br>
	 * 							 - The order to this table does NOT exist.<br>
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static void exec(long pin, short model, int tableID) throws BusinessException, SQLException{		
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			exec(dbCon, VerifyPin.exec(dbCon, pin, model), tableID);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel the unpaid order according the specific terminal and table.
	 * @param term
	 * 			the terminal 
	 * @param tableID 
	 * 			the table alias id to query
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 						     - The table associated with this order is IDLE.<br>
	 * 							 - The order to this table does NOT exist.<br>
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static void exec(Terminal term, int tableID) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			exec(dbCon, term, tableID);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel the unpaid order according the specific terminal and table.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param tableID 
	 * 			the table alias id to query
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 						     - The table associated with this order is IDLE.<br>
	 * 							 - The order to this table does NOT exist.<br>
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static void exec(DBCon dbCon, Terminal term, int tableID) throws BusinessException, SQLException{ 
		
		Table table = QueryTable.exec(dbCon, term, tableID);
		
		int orderID = Util.getUnPaidOrderID(dbCon, table);
		
		String sql;
		
		/**
		 * Calculate the gift amount and remove it from the total gift amount if the gift quota exist.
		 */
		float giftAmount = 0;
		if(term.getGiftQuota() >= 0){
			sql = "SELECT unit_price, order_count, food_status FROM " + Params.dbName +
				  ".order_food WHERE order_id=" + orderID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				if((dbCon.rs.getShort("food_status") & Food.GIFT) != 0){
					giftAmount += dbCon.rs.getFloat("unit_price") * dbCon.rs.getFloat("order_count");
				}
			}
			dbCon.rs.close();
		}
		
		short category = Order.CATE_NORMAL;
		int table2AliasID = 0;
		boolean isDelTable = false;
		sql = "SELECT category, table2_alias FROM " + Params.dbName + ".order WHERE id=" + orderID;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			category = dbCon.rs.getShort("category");
			table2AliasID = dbCon.rs.getInt("table2_alias");
			if(category == Order.CATE_JOIN_TABLE || category == Order.CATE_TAKE_OUT){
				isDelTable = true;
			}
		}
		
		/**
		 * Put all the UPDATE, INSERT, DELETE statements into a database transition so as to assure 
		 * the status to both table and order is consistent. 
		 */
		try{
			dbCon.conn.setAutoCommit(false);
			
			/**
			 * Update the gift amount if the gift quota is set.
			 */
			if(term.getGiftQuota() >= 0){
				sql = "UPDATE " + Params.dbName + ".terminal SET" +
					  	  " gift_amount = gift_amount - " + giftAmount +
					  	  " WHERE pin=" + "0x" + Long.toHexString(term.pin) +
					  	  " AND restaurant_id=" + term.restaurant_id;
				dbCon.stmt.executeUpdate(sql);
			}
			
			/**
			 * Delete the table if the order category is "并台" or "外卖",
			 * since the table to these two category is temporary.
			 * Otherwise update the table status to idle
			 */
			if(isDelTable){
				sql = "DELETE FROM " + Params.dbName + ".table WHERE " +
					  "restaurant_id=" + table.restaurantID + " AND " +
					  "table_alias=" + table.aliasID;
				dbCon.stmt.executeUpdate(sql);
			}else{
				if(category == Order.CATE_MERGER_TABLE){
					sql = "UPDATE " + Params.dbName + ".table SET " +
					  	  "status=" + Table.TABLE_IDLE + ", " +
					  	  "custom_num=NULL, " +
					  	  "category=NULL " +
					  	  "WHERE " +
					  	  "restaurant_id=" + table.restaurantID + " AND " +
					  	  "table_alias=" + table2AliasID;
					dbCon.stmt.executeUpdate(sql);
				}
				sql = "UPDATE " + Params.dbName + ".table SET " +
					  "status=" + Table.TABLE_IDLE + ", " +
					  "custom_num=NULL, " +
					  "category=NULL " +
					  "WHERE restaurant_id=" + table.restaurantID + 
					  " AND table_alias=" + table.aliasID;
				dbCon.stmt.executeUpdate(sql);
			}
			//delete the records related to the order id and food id in "order_food" table
			sql = "DELETE FROM `" + Params.dbName + "`.`order_food` WHERE order_id=" + orderID;
			dbCon.stmt.executeUpdate(sql);
			//delete the corresponding order record in "order" table
			sql = "DELETE FROM `" + Params.dbName + "`.`order` WHERE id=" + orderID;
			dbCon.stmt.executeUpdate(sql);

			dbCon.conn.commit();
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw new SQLException(e);
			
		}finally{
			dbCon.conn.setAutoCommit(true);
		}
	}
}
