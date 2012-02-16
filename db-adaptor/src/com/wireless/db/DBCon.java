package com.wireless.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBCon {
	//open the database
	public Connection conn = null;
	public Statement stmt = null;
	public ResultSet rs = null;
	
	public void connect() throws SQLException{
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e){
			throw new SQLException(e);
		}
		
		conn = DriverManager.getConnection(Params.dbUrl, Params.dbUser, Params.dbPwd);   
		stmt = conn.createStatement();   
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
			if(conn != null){
				conn.close();
				conn = null;
			}
		}catch(SQLException e){
			System.err.println(e.toString());
		}
	}
	
}
