package com.wireless.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBCon {
	//open the database
	Connection dbCon = null;
	Statement stmt = null;
	ResultSet rs = null;
	
	public void connect() throws SQLException{
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e){
			throw new SQLException(e);
		}
		
		dbCon = DriverManager.getConnection(Params.dbUrl, Params.dbUser, Params.dbPwd);   
		stmt = dbCon.createStatement();   
		//set names to UTF-8
		stmt.execute("SET NAMES utf8");
	}
	
	public void disconnect(){
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
