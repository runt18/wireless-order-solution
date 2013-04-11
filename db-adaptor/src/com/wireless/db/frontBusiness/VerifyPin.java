package com.wireless.db.frontBusiness;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.protocol.PRestaurant;
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
	public static Terminal exec(long pin, short model) throws BusinessException, SQLException{		
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
	public static Terminal exec(DBCon dbCon, long pin, short model) throws BusinessException, SQLException{
		Terminal terminal = QueryTerminal.exec(dbCon, pin, model);
		
		if(terminal != null){
			/**
			* Since the restaurant id 1 through 10 is reserved for system,
			* throw a BusinessException with "TERMINAL_NOT_ATTACHED" 
			* if the restaurant id is less than 10
			*/
			if(terminal.restaurantID > PRestaurant.RESERVED_7){
				/**
				* Check if the terminal is expired or not.
				* Note that NULL means the terminal never expire
				*/
				if(terminal.expireDate != null && System.currentTimeMillis() > terminal.expireDate.getTime()){
					throw new BusinessException("The terminal(pin=0x" + Long.toHexString(pin) + ", model=" + model + ") is expired.", ProtocolError.TERMINAL_EXPIRED);
				}
			}
			return terminal;
			
		}else{
			throw new BusinessException("The terminal(pin=0x" + Long.toHexString(pin) + ", model=" + model + ") is NOT attached with any restaurant.",
					ProtocolError.TERMINAL_NOT_ATTACHED);
		}

	}
}
