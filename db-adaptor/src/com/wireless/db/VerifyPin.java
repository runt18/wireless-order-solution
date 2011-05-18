package com.wireless.db;

import java.sql.SQLException;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;


public class VerifyPin {
	/**
	 * Check the terminal along with pin and model is exist or not.
	 * Return the associated terminal info if exist,
	 * otherwise throw the exception to tell the caller. 
	 * @param model the model id 
	 * @param pin the pin value
	 * @return the terminal info if exist
	 * @throws BusinessException throws if the terminal is NOT attached with any restaurant
	 * @throws SQLException throws if fail to execute the SQL statement
	 */
	public static Terminal exec(int pin, short model) throws BusinessException, SQLException{
		

		
		//open the database
		DBCon dbCon = new DBCon();;
		try {   
			
			dbCon.connect();
			
			String sql = "SELECT restaurant_id, expire_date, owner_name, model_name FROM " +  
					     Params.dbName + ".terminal WHERE pin=" + pin +
						 " AND model_id=" + model;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			Terminal terminal = new Terminal();
			if(dbCon.rs.next()){
				terminal.restaurant_id = dbCon.rs.getInt("restaurant_id");
				terminal.expireDate = dbCon.rs.getDate("expire_date");
				terminal.owner = dbCon.rs.getString("owner_name");
				terminal.modelName = dbCon.rs.getString("model_name");
				terminal.modelID = model;
				terminal.pin = pin;
				return terminal;
			}
			
			/**
			 * Since the restaurant id 1 through 10 is reserved for system,
			 * throw a BusinessException with "TERMINAL_NOT_ATTACHED" 
			 * if the restaurant id is less than 10
			 */
			if(terminal.restaurant_id > 10){
				return terminal;
			}else{
				throw new BusinessException("The terminal is NOT attached with any restaurant.",
										    ErrorCode.TERMINAL_NOT_ATTACHED);
				
			}
			
		}finally{
			dbCon.disconnect();
		}
	}
}
