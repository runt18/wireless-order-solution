package com.wireless.wirelessorder.impl;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;   
import java.sql.SQLException;   
import java.sql.Statement; 
import java.security.*; 
import com.wireless.wirelessorder.*;

public class Login {
	/**
	 * Perform the login service to verify the user account and password,
	 * and return the token if passing the verification. 
	 * @param user the user account
	 * @param pwd the password to this user account
	 * @return a token string if passing the verification, otherwise an empty string
	 * @throws LoginFaultException the meaning to error type is as below.<br>
	 * 1 - User account and password is not matched
	 * 2 - The server doesn't support md5 algorithm
	 * 3 - The server doesn't find the mysql package
	 * 4 - Error due to database error
	 */
	public static String perform(String user, String pwd) throws LoginFaultException{
		//open the database
		Connection dbCon = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {   
			Class.forName("com.mysql.jdbc.Driver");   
		
			dbCon = DriverManager.getConnection(Params.dbUrl, Params.dbUser, Params.dbPwd);   
			stmt = dbCon.createStatement();   		
			//set names to UTF-8
			stmt.execute("SET NAMES utf8");
			//encode the password using md5
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(pwd.getBytes());

			String sql = "SELECT id FROM " + Params.dbName + ".restaurant WHERE account='" + user +
						 "' AND pwd='" + toHexString(algorithm.digest()) + "'";
			rs = stmt.executeQuery(sql);
			/**
			 * Check to see if passing the user verification.
			 * In the case of passing, generate a token whose value is a random double encoded by md5.
			 * Then update this token to restaurant record and sent the token as response.
			 */
			if(rs.next()){
				algorithm.reset();
				algorithm.update(new Double(Math.random() * Math.random()).toString().getBytes());
				String token = toHexString(algorithm.digest());
				sql = "UPDATE `" + Params.dbName + "`.`restaurant` SET token='" + token +
						"' WHERE id=" + rs.getInt("id");
				stmt.execute(sql);
				return token;
				
			}else{
				throwLoginExcep(1);
				return "";
			}
			
		}catch(NoSuchAlgorithmException e){
			throwLoginExcep(2);
			return "";
			
		}catch(ClassNotFoundException e) { 
			throwLoginExcep(3);
			return "";
			
		}catch(SQLException e){
			throwLoginExcep(4);
			return "";
			
		}finally{
			try{
				if(rs != null){
					rs.close();
					rs = null;
				}
				if(stmt != null){
					stmt.close();
					stmt = null;
				}
				if(dbCon != null){
					dbCon.close();
					dbCon = null;
				}
			}catch(SQLException e){
				System.err.println(e.toString());
			}
		}
	}
	
	/**
	 * Convert the md5 byte to hex string.
	 * @param md5Msg the md5 byte value
	 * @return the hex string to this md5 byte value
	 */
	private static String toHexString(byte[] md5Msg){
		StringBuffer hexString = new StringBuffer();
		for (int i=0; i < md5Msg.length; i++) {
			if(md5Msg[i] >= 0x00 && md5Msg[i] < 0x10){
				hexString.append("0").append(Integer.toHexString(0xFF & md5Msg[i]));
			}else{
				hexString.append(Integer.toHexString(0xFF & md5Msg[i]));					
			}
		}
		return hexString.toString();
	}
	
	private static void throwLoginExcep(int errType) throws LoginFaultException{
		LoginFault fault = new LoginFault();
		fault.setErrType(errType);
		LoginFaultException excep = new LoginFaultException();
		excep.setFaultMessage(fault);
		throw excep;
	}
}
