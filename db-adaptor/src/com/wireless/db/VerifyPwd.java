package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Terminal;

public class VerifyPwd {
	
	public static int PASSWORD_1 = 1;	//����Ա����
	public static int PASSWORD_2 = 2;	//����Ȩ������
	public static int PASSWORD_3 = 3;	//�곤Ȩ������
	public static int PASSWORD_4 = 4;	//����ԱȨ������
	public static int PASSWORD_5 = 5;	//�˲�Ȩ������
	
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
	public static boolean exec(long pin, short model, int type, String pwd) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, pin, model, type, pwd);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Verify the password to check whether permit to do this action.
	 * The priority to password is below.
	 * pwd1 > pwd2 > pwd3 > pwd4 > pwd5
	 * e.g. While asking to verify 3rd password, then would pass if type 1st or 2nd password correctly. 
	 * Note that this method should be invoked before database connected.
	 * @param dbCon
	 * 		The database connection.
	 * @param pin
	 * 		The pin to terminal.
	 * @param model
	 * 		The model to terminal.
	 * @param type
	 * 		The type of password
	 * @param pwd2Verify
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
	public static boolean exec(DBCon dbCon, long pin, short model, int type, String pwd2Verify) throws BusinessException, SQLException{
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		
		String pwd = "", pwd2 = "", pwd3 = "", pwd4 = "", pwd5 = "";
		
		String sql = "SELECT pwd, pwd2, pwd3, pwd4, pwd5 FROM " + Params.dbName + ".restaurant WHERE id=" + term.restaurantID;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			pwd = dbCon.rs.getString("pwd");
			pwd2 = dbCon.rs.getString("pwd2");
			pwd3 = dbCon.rs.getString("pwd3");
			pwd4 = dbCon.rs.getString("pwd4");
			pwd5 = dbCon.rs.getString("pwd5");
		}
		
		if(type == PASSWORD_1){
			return pwd2Verify.equals(pwd);
			
		}else if(type == PASSWORD_2){
			if(pwd2Verify.equals(pwd)){
				return true;
			}else{
				return pwd2Verify.equals(pwd2);
			}

		}else if(type == PASSWORD_3){
			if(pwd2Verify.equals(pwd) || pwd2Verify.equals(pwd2)){
				return true;
			}else{
				return pwd2Verify.equals(pwd3);
			}
			
		}else if(type == PASSWORD_4){
			if(pwd2Verify.equals(pwd) || pwd2Verify.equals(pwd2) || pwd2Verify.equals(pwd3)){
				return true;
			}else{
				return pwd2Verify.equals(pwd4);
			}
			
		}else if(type == PASSWORD_5){
			if(pwd2Verify.equals(pwd) || pwd2Verify.equals(pwd2) || pwd2Verify.equals(pwd3) || pwd2Verify.equals(pwd4)){
				return true;
			}else{
				return pwd2Verify.equals(pwd5);
			}
			
		}else{
			return false;
		}

	}
}
