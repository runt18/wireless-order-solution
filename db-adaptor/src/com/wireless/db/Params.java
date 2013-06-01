package com.wireless.db;

public class Params {
	
	public static String dbName = null;
	public static String dbHost = null;
	public static int dbPort = 0;
	public static String dbUrl = null;
	public static String dbUser = null;
	public static String dbPwd = null;
	
	private static void concateURL(){
		dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&characterEncoding=utf8";
	}
	
	public static String getJdbcUrl(){
		return dbUrl;
	}
	
	public static void setDatabase(String db){
		dbName = db;
		concateURL();
	}
	
	public static void setDbHost(String host){
		dbHost = host;
		concateURL();
	}
	
	public static void setDbPort(int port){
		dbPort = port;
		concateURL();
	}
	
	public static void setDbUser(String user){
		dbUser = user;
	}
	
	public static void setDbPwd(String pwd){
		dbPwd = pwd;
	}
}
