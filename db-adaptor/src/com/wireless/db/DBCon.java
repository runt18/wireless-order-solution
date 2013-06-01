package com.wireless.db;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

public class DBCon {
	//open the database
	public Connection conn;
	public Statement stmt;
	
	public ResultSet rs;
	public ResultSet rs2;
	public ResultSet rs3;
	public ResultSet rs4;
	public ResultSet rs5;
	
	private static final ComboPooledDataSource DB_POOL = new ComboPooledDataSource();
	
	public static void init(String dbHost, String dbPort, String dbName, String user, String pwd) throws PropertyVetoException{
		DB_POOL.setDriverClass("com.mysql.jdbc.Driver");
		DB_POOL.setJdbcUrl("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&characterEncoding=utf8");
		DB_POOL.setUser(user);
		DB_POOL.setPassword(pwd);
	}
	
	public DBCon() throws SQLException{
		
		conn = DB_POOL.getConnection();
		stmt = conn.createStatement();
		
		//set names to UTF-8
		stmt.execute("SET NAMES utf8");
		//use wireless order db
		stmt.execute("USE wireless_order_db");
	}
	
	public void connect() throws SQLException{
//		try{
//			Class.forName("com.mysql.jdbc.Driver");
//		}catch(ClassNotFoundException e){
//			throw new SQLException(e);
//		}
//		
//		conn = DriverManager.getConnection(Params.dbUrl, Params.dbUser, Params.dbPwd);   
//		stmt = conn.createStatement();   
//		//set names to UTF-8
//		stmt.execute("SET NAMES utf8");
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
			if(conn != null){
				conn.close();
				conn = null;
			}
		}catch(SQLException e){
			System.err.println(e.toString());
		}
	}
	
	public static void destroy() throws SQLException{
		DataSources.destroy(DB_POOL);
	}
	
	public static ComboPooledDataSource getPoolSource(){
		return DB_POOL;
	}
	
}
