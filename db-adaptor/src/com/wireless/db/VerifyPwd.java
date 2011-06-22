package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Terminal;

public class VerifyPwd {
	
	public static int PASSWORD_1 = 1;
	public static int PASSWORD_2 = 2;
	
	/**
	 * Verify the password according to specific type.
	 * @param pin
	 * 		The pin to terminal.
	 * @param model
	 * 		The model to terminal.
	 * @param type
	 * 		The type of password
	 * @param pwd
	 * 		The password to verify which is in the form of MD5
	 * @return 
	 * 		True if match the password.
	 *  	Otherwise return false.
	 * @throws BusinessException
	 * 		Throws if the either of cases below.<br>
	 * 		- The terminal is NOT attached to any restaurant.
	 * 		- The terminal has been expired.
	 * @throws SQLException
	 * 		Throws if fail to execute any SQL statement.
	 */
	public static boolean exec(int pin, short model, int type, String pwd) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, pin, model, type, pwd);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Verify the password according to specific type.
	 * Note that this method should be invoked before database connected.
	 * @param dbCon
	 * 		The database connection.
	 * @param pin
	 * 		The pin to terminal.
	 * @param model
	 * 		The model to terminal.
	 * @param type
	 * 		The type of password
	 * @param pwd
	 * 		The password to verify which is in the form of MD5
	 * @return 
	 * 		True if match the password.
	 *  	Otherwise return false.
	 * @throws BusinessException
	 * 		Throws if the either of cases below.<br>
	 * 		- The terminal is NOT attached to any restaurant.
	 * 		- The terminal has been expired.
	 * @throws SQLException
	 * 		Throws if fail to execute any SQL statement.
	 */
	public static boolean exec(DBCon dbCon, int pin, short model, int type, String pwd) throws BusinessException, SQLException{
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		String pwdType;
		if(type == PASSWORD_1){
			pwdType = "pwd";
		}else if(type == PASSWORD_2){
			pwdType = "pwd2";
		}else{
			pwdType = "pwd2";
		}
		String sql = "SELECT id FROM " + Params.dbName + ".restaurant WHERE " +
					 pwdType + "='" + pwd + "'" + 
					 " AND id=" + term.restaurant_id;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		return dbCon.rs.next();
	}
}
