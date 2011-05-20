package com.wireless.db;

public class Params {
	
	static String dbName = null;
	static String dbHost = null;
	static int dbPort = 0;
	static String dbUrl = null;
	static String dbUser = null;
	static String dbPwd = null;
	
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
}
