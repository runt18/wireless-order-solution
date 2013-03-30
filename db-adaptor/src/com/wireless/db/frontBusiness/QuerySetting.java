package com.wireless.db.frontBusiness;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.dbObject.Setting;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Terminal;

public class QuerySetting {
	
	/**
	 * Get the restaurant setting that this terminal attached to.
	 * @param pin 
	 * 			the pin value to this terminal
	 * @param model
	 * 			the model value to this terminal
	 * @return the setting result
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either of cases below.<br>
	 * 			- The terminal is NOT attached to any restaurant.<br>
	 * 			- The terminal is expired.<br>
	 * 			- The restaurant this terminal attached to does NOT exist.
	 */
	public static Setting exec(long pin, short model) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return exec(dbCon, term.restaurantID);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	/**
	 * Get the setting according to a specific restaurant id.
	 * @param restaurantID 
	 * 			the restaurant id
	 * @return the setting to this restaurant
	 * @throws SQLException 
	 * 			throws if any error occurred while execute the SQL statement
	 */
	public static Setting exec(int restaurantID) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, restaurantID);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the setting according to a specific restaurant id.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon 
	 * 			the database connection 
	 * @param restaurantID 
	 * 			the restaurant id
	 * @return the setting to this restaurant
	 * @throws SQLException 
	 * 			throws if any error occurred while execute the SQL statement
	 */
	public static Setting exec(DBCon dbCon, int restaurantID) throws SQLException{
		Setting setting = new Setting();
		
		String sql = " SELECT " +
					 " price_tail, auto_reprint, receipt_style, erase_quota " +
					 " FROM " + Params.dbName +
		  			 ".setting " +
		  			 " WHERE " +
		  			 " restaurant_id = " + restaurantID;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			setting.setPriceTail(dbCon.rs.getInt("price_tail"));
			setting.setAutoReprint(dbCon.rs.getBoolean("auto_reprint"));
			setting.setReceiptStyle((int)dbCon.rs.getLong("receipt_style"));
			setting.setEraseQuota(dbCon.rs.getInt("erase_quota"));
		}
		dbCon.rs.close();
		
		return setting;
	}
	
}
