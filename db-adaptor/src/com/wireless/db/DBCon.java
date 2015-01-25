package com.wireless.db;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.alibaba.druid.pool.DruidDataSource;

public class DBCon {
	//open the database
	public final Connection conn;
	public Statement stmt;
	
	public ResultSet rs;
	
	private static DruidDataSource dbPool;
	
	public static void init(String dbHost, String dbPort, String dbName, String user, String pwd, boolean usingPool) throws PropertyVetoException{
		Params.dbHost = dbHost;
		Params.dbPort = Integer.parseInt(dbPort);
		Params.dbUser = user;
		Params.dbName = dbName;
		Params.dbPwd = pwd;
		Params.dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&characterEncoding=utf8";
		if(usingPool){
			dbPool = new DruidDataSource();
			//dbPool.setDriverClassName("com.mysql.jdbc.Driver");
			dbPool.setUrl(Params.dbUrl);
			dbPool.setUsername(user);
			dbPool.setPassword(pwd);
			dbPool.setInitialSize(1);
	        dbPool.setMaxActive(30);
	        dbPool.setMinIdle(1);
	        //创建物理连接失败后不进行重试
	        dbPool.setConnectionErrorRetryAttempts(0);
	        //配置获取连接等待超时的时间, 获取失败后直接退出
	        dbPool.setMaxWait(0);
	        //配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
	        dbPool.setTimeBetweenEvictionRunsMillis(300 * 1000); // 30秒
	        //配置一个连接在池中最小生存的时间，单位是毫秒
	        dbPool.setMinEvictableIdleTimeMillis(600 * 1000); // 60秒
	        //申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效
	        dbPool.setTestWhileIdle(true);
	        //申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
	        dbPool.setTestOnBorrow(false);
	        //归还连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能
	        dbPool.setTestOnReturn(false);
	        dbPool.setValidationQuery("SELECT 1");
		}else{        
			dbPool = null;
		}
	}
	
	public DBCon() throws SQLException{
		if(dbPool != null){
			conn = dbPool.getConnection();
		}else{
			conn = DriverManager.getConnection(Params.dbUrl, Params.dbUser, Params.dbPwd);
		}
	}
	
	public static class PoolStat{
		public final int initPool;
		public final int maxActive;
		public final int minIdle;
		public final int poolCount;
		public final int activeCount;
		public final long logicConnect;
		public final long logicClose;
		public final long physicalConnect;
		public final long physicalClose;
		
		PoolStat(){
			this.initPool = 0;
			this.maxActive = 0;
			this.minIdle = 0;
			this.poolCount = 0;
			this.activeCount = 0;
			this.logicConnect = 0;
			this.logicClose = 0;
			this.physicalConnect = 0;
			this.physicalClose = 0;
		}
		
		PoolStat(int initPool, int maxActive, int minIdle, int poolCount, int activeCount, long logicConnect, long logicClose, long physicalConnect, long physicalClose){
			this.initPool = initPool;
			this.maxActive = maxActive;
			this.minIdle = minIdle;
			this.poolCount = poolCount;
			this.activeCount = activeCount;
			this.logicConnect = logicConnect;
			this.logicClose = logicClose;
			this.physicalConnect = physicalConnect;
			this.physicalClose = physicalClose;
		}
	}
	
	public static PoolStat getPoolStat(){
		if(dbPool != null){
			return new PoolStat(dbPool.getInitialSize(),
								dbPool.getMaxActive(),
								dbPool.getMinIdle(),
								dbPool.getPoolingCount(),
								dbPool.getActiveCount(),
								dbPool.getConnectCount(),
								dbPool.getCloseCount(),
								dbPool.getCreateCount(),
								dbPool.getDestroyCount());
		}else{
			return new PoolStat();
		}
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
			}
		}catch(SQLException e){
			System.err.println(e.toString());
		}
	}
	
	public static void destroy() throws SQLException{
		dbPool.close();
	}

}
