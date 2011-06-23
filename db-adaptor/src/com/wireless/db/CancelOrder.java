package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;

public class CancelOrder {
	/**
	 * Cancel the unpaid order according to the specific table if the table is busy.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param tableID the table alias id to query
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 						     - The table associated with this order is idle.
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static void exec(int pin, short model, short tableID) throws BusinessException, SQLException{		
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			Table table = QueryTable.exec(dbCon, pin, model, tableID);
			
			int orderID = Util.getUnPaidOrderID(dbCon, table);
			
			boolean isDelTable = false;
			String sql = "SELECT category FROM " + Params.dbName + ".order WHERE id=" + orderID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				short category = dbCon.rs.getShort("category");
				if(category == Order.CATE_JOIN_TABLE || category == Order.CATE_TAKE_OUT){
					isDelTable = true;
				}
			}
			/**
			 * Delete the table if the order category is "并台" or "外卖",
			 * since the table to these two category is temporary.
			 */
			dbCon.stmt.clearBatch();
			if(isDelTable){
				sql = "DELETE FROM " + Params.dbName + ".table WHERE alias_id=" +
					  table.alias_id + 
					  " AND restaurant_id=" + table.restaurant_id;
				dbCon.stmt.addBatch(sql);
			}
			//delete the records related to the order id and food id in "order_food" table
			sql = "DELETE FROM `" + Params.dbName + "`.`order_food` WHERE order_id=" + orderID;
			dbCon.stmt.addBatch(sql);
			//delete the corresponding order record in "order" table
			sql = "DELETE FROM `" + Params.dbName + "`.`order` WHERE id=" + orderID;
			dbCon.stmt.addBatch(sql);
			dbCon.stmt.executeBatch();
			
		}finally{
			dbCon.disconnect();
		}
	}
}
