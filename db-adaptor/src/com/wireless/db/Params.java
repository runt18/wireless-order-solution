package com.wireless.db;

public class Params {
	
	static String dbName = null;
	static String dbHost = null;
	static int dbPort = 0;
	static String dbUrl = null;
	static String dbUser = null;
	static String dbPwd = null;
	static String socketHost = null;
	static int socketPort = 0;
	
	private static void concateURL(){
		dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&characterEncoding=utf8";
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
	
	public static void setSocketHost(String host){
		socketHost = host;
	}
	
	public static void setSocketPort(int port){
		socketPort = port;
	}
	

	
//	static{
//		//FIX ME!!!
//		//The parameters should be read from the configuration file
//		dbName = "wireless_order_db";
//		dbHost = "192.168.123.130";
//		dbPort = 3306;
//		dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&characterEncoding=utf8";
//		dbUser = "yzhang";
//		dbPwd = "HelloZ315";
//		socketHost = "127.0.0.1";
//		socketPort = 55555;
//	}
}
