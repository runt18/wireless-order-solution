package com.wireless.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VerifyPin {
	/**
	 * Check the terminal along with pin and model is exist or not.
	 * Return the associated restaurant id if exist,
	 * otherwise throw the exception to tell the caller. 
	 * @param model the model id 
	 * @pin the pin value
	 * @throws Exception throws if the terminal with pin and model is NOT attached with any restaurant
	 */
	public static int exec(String model, String pin) throws Exception{
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
			
			String sql = "SELECT restaurant_id FROM " +  Params.dbName + ".terminal WHERE pin=" + pin +
						 "AND model_id=" + model;
			rs = stmt.executeQuery(sql);
			/**
			 * pass to verify the token if existing in restaurant table,
			 * otherwise fail to verify the token 
			 */
			if(rs.next()){
				return rs.getInt("restaurant_id");
			}else{
				throw new Exception("请求的PIN(" + pin + ")无对应的餐厅");
			}
			
		}catch(ClassNotFoundException e){
			e.printStackTrace();
			throw new Exception("请求的PIN(" + pin + ")不成功");
			
		}catch(SQLException e){
			e.printStackTrace();
			throw new Exception("请求的PIN(" + pin + ")不成功");
			
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
