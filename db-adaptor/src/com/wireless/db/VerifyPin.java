package com.wireless.db;

import java.sql.SQLException;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Terminal;


public class VerifyPin {
	/**
	 * Check the terminal along with pin and model is exist or not.
	 * Return the associated terminal info if exist,
	 * otherwise throw the exception to tell the caller. 
	 * @param model the model id 
	 * @param pin the pin value
	 * @return the terminal info if exist
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attached with any restaurant.<br>
	 * 							 - The terminal is expired.
	 * @throws SQLException throws if fail to execute the SQL statement
	 */
	public static Terminal exec(int pin, short model) throws BusinessException, SQLException{		
		//open the database
		DBCon dbCon = new DBCon();;
		try {   
			
			dbCon.connect();
			
			return exec(dbCon, pin, model);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Check the terminal along with pin and model is exist or not.
	 * Return the associated terminal info if exist,
	 * otherwise throw the exception to tell the caller. 
	 * Note that the the database should be connect before invoking this method
	 * @param dbCon the database connection 
	 * @param pin the pin to this terminal 
	 * @param model the model to this terminal
	 * @return the terminal info if exist
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attached with any restaurant.<br>
	 * 							 - The terminal is expired.
	 * @throws SQLException throws if fail to execute the SQL statement
	 */
	public static Terminal exec(DBCon dbCon, int pin, short model) throws BusinessException, SQLException{
		String sql = "SELECT restaurant_id, expire_date, owner_name, model_name, gift_quota, gift_amount FROM " +  
	     			Params.dbName + ".terminal WHERE pin=" + "0x" + Integer.toHexString(pin) +
	     			" AND model_id=" + model;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Terminal terminal = new Terminal();
		if(dbCon.rs.next()){
			terminal.restaurant_id = dbCon.rs.getInt("restaurant_id");
			terminal.expireDate = dbCon.rs.getDate("expire_date");
			terminal.owner = dbCon.rs.getString("owner_name");
			terminal.modelName = dbCon.rs.getString("model_name");
			float quota = dbCon.rs.getFloat("gift_quota");
			if(quota > 0){
				terminal.setGiftQuota(quota);
			}
			terminal.setGiftAmount(dbCon.rs.getFloat("gift_amount"));
			terminal.modelID = model;
			terminal.pin = pin;
			return terminal;
		}
		dbCon.rs.close();
		
		/**
		* Since the restaurant id 1 through 10 is reserved for system,
		* throw a BusinessException with "TERMINAL_NOT_ATTACHED" 
		* if the restaurant id is less than 10
		*/
		if(terminal.restaurant_id > Restaurant.RESERVED_7){
			/**
			* Check if the terminal is expired or not.
			* Note that NULL means the terminal never expire
			*/
			if(terminal.expireDate != null){
				if(System.currentTimeMillis() > terminal.expireDate.getTime()) {
					throw new BusinessException("The terminal is expired.",	ErrorCode.TERMINAL_EXPIRED);
				}else{
					return terminal;
				}
			}else{
				return terminal;
			}
		}else{
			throw new BusinessException("The terminal is NOT attached with any restaurant.",
								    	ErrorCode.TERMINAL_NOT_ATTACHED);
		}
	}
}
