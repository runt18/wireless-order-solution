package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

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
	public static void exec(DBCon dbCon, long pin, short model, short tableID) throws BusinessException, SQLException{
		Terminal term = VerifyPin.exec(pin, model);
		String sql = "SELECT id FROM " + Params.dbName + 
		  			 ".table WHERE alias_id=" + tableID +
		  			 " AND restaurant_id=" + term.restaurant_id;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(!dbCon.rs.next()){
			throw new BusinessException("Table(alias_id=" + tableID + ") is NOT exist.", ErrorCode.TABLE_NOT_EXIST);
		}	
		dbCon.rs.close();
		
		sql = "DELETE FROM " + Params.dbName + ".table WHERE restaurant_id=" + 
			  term.restaurant_id + " AND alias_id=" + tableID;
		dbCon.stmt.execute(sql);
	}
}
