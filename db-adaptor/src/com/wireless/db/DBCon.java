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
	public Statement stmt2 = null;
	public Statement stmt3 = null;
	public Statement stmt4 = null;
	public Statement stmt5 = null;
	
	public ResultSet rs = null;
	public ResultSet rs2 = null;
	public ResultSet rs3 = null;
	public ResultSet rs4 = null;
	public ResultSet rs5 = null;
	
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
			if(rs2 != null){
				rs2.close();
				rs2 = null;
			}
			if(rs3 != null){
				rs3.close();
				rs3 = null;
			}
			if(rs4 != null){
				rs4.close();
				rs4 = null;
			}
			if(rs5 != null){
				rs5.close();
				rs5 = null;
			}
			if(stmt != null){
				stmt.close();
				stmt = null;
			}
			if(stmt2 != null){
				stmt2.close();
				stmt2 = null;
			}
			if(stmt3 != null){
				stmt3.close();
				stmt3 = null;
			}
			if(stmt4 != null){
				stmt4.close();
				stmt4 = null;
			}
			if(stmt5 != null){
				stmt5.close();
				stmt5 = null;
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
