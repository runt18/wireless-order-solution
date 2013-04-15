package com.wireless.db.frontBusiness;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.PTable;

public class CancelTable {
	
	/**
	 * Cancel a specific table.
	 * @param pin
	 * 			the pin to terminal
	 * @param model
	 * 			the model to terminal
	 * @param tableID
	 * 			the table id to cancel
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			- The terminal is NOT attached with any restaurant.<br>
	 * 			- The terminal is expired.<br> 
	 * 			- The table id to be canceled has NOT exist before.<br>
	 * @throws SQLException
	 * 			Throws if fail to execute any SQL statement.
	 */
	public static void exec(long pin, short model, short tableID) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			exec(dbCon, pin, model, tableID);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel a specific table.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param pin
	 * 			the pin to terminal
	 * @param model
	 * 			the model to terminal
	 * @param tableID
	 * 			the table id to cancel
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			- The terminal is NOT attached with any restaurant.<br>
	 * 			- The terminal is expired.<br> 
	 * 			- The table id to be canceled has NOT exist before.<br>
	 * @throws SQLException
	 * 			Throws if fail to execute any SQL statement.
	 */
	public static void exec(DBCon dbCon, long pin, short model, int tableID) throws BusinessException, SQLException{
		
		PTable table = QueryTable.exec(dbCon, pin, model, tableID);
		
		String sql = "DELETE FROM " + Params.dbName + ".table WHERE " +
					 "restaurant_id=" + table.getRestaurantId() + " AND " +
					 "table_alias=" + table.getAliasId();
		dbCon.stmt.execute(sql);
	}
}
