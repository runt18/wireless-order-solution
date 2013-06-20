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
		Params.dbHost = dbHost;
		Params.dbPort = Integer.parseInt(dbPort);
		Params.dbUser = user;
		Params.dbName = dbName;
		Params.dbPwd = pwd;
		DB_POOL.setDriverClass("com.mysql.jdbc.Driver");
		DB_POOL.setJdbcUrl("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&characterEncoding=utf8");
		DB_POOL.setUser(user);
		DB_POOL.setPassword(pwd);
		//DB_POOL.setConnectionCustomizerClassName("com.wireless.db.VerboseConnectionCustomizer");
		//DB_POOL.setDebugUnreturnedConnectionStackTraces(true);
		//DB_POOL.setUnreturnedConnectionTimeout(180);
		DB_POOL.setIdleConnectionTestPeriod(3600);
		DB_POOL.setTestConnectionOnCheckin(true);
		DB_POOL.setPreferredTestQuery("SELECT COUNT(*) FROM " + dbName + ".restaurant");
	}
	
	public DBCon() throws SQLException{
		conn = DB_POOL.getConnection();
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
