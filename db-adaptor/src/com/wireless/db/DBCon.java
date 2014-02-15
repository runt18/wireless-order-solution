package com.wireless.db;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBCon {
	//open the database
	public Connection conn;
	public Statement stmt;
	
	public ResultSet rs;
	
//	private static final ComboPooledDataSource DB_POOL = new ComboPooledDataSource();
	
	public static void init(String dbHost, String dbPort, String dbName, String user, String pwd) throws PropertyVetoException{
		Params.dbHost = dbHost;
		Params.dbPort = Integer.parseInt(dbPort);
		Params.dbUser = user;
		Params.dbName = dbName;
		Params.dbPwd = pwd;
		Params.dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&characterEncoding=utf8";
//		DB_POOL.setDriverClass("com.mysql.jdbc.Driver");
//		DB_POOL.setJdbcUrl(Params.dbUrl);
//		DB_POOL.setUser(user);
//		DB_POOL.setPassword(pwd);
//		//DB_POOL.setConnectionCustomizerClassName("com.wireless.db.VerboseConnectionCustomizer");
//		//DB_POOL.setDebugUnreturnedConnectionStackTraces(true);
//		//DB_POOL.setUnreturnedConnectionTimeout(180);
//		DB_POOL.setIdleConnectionTestPeriod(3600);
//		DB_POOL.setTestConnectionOnCheckin(true);
//		DB_POOL.setPreferredTestQuery("SELECT COUNT(*) FROM " + dbName + ".restaurant");
	}
	
	public DBCon() throws SQLException{
		//conn = DB_POOL.getConnection();
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e){
			throw new SQLException(e);
		}
		
		conn = DriverManager.getConnection(Params.dbUrl, Params.dbUser, Params.dbPwd);   
	}
	
	public void connect() throws SQLException{
		stmt = conn.createStatement();
		//set names to UTF-8
		stmt.execute("SET NAMES utf8");
		//use wireless order db
		stmt.execute("USE wireless_order_db");
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
	
	public static void destroy() throws SQLException{
//		DataSources.destroy(DB_POOL);
	}

}
