package com.wireless.order;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Verify {
	
	/**
	 * Verify the token to check if it is valid. 
	 * @param token the token to be verified
	 * @throws VerifyFault throws if fail to verify the token
	 */
	public static void exec(String token) throws VerifyFault{
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
			
			String sql = "SELECT id FROM " + Params.dbName + ".restaurant WHERE token='" + token + "'";
			rs = stmt.executeQuery(sql);
			/**
			 * pass to verify the token if existing in restaurant table,
			 * otherwise fail to verify the token 
			 */
			if(!rs.next()){
				throw new VerifyFault();
			}
		}catch(ClassNotFoundException e){
			throw new VerifyFault();
			
		}catch(SQLException e){
			throw new VerifyFault();
			
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
}

