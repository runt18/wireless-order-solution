package com.wireless.db;

public class Params {
	
	static String dbName = null;
	static String dbHost = null;
	static int dbPort = 0;
	static String dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&characterEncoding=utf8";
	static String dbUser = null;
	static String dbPwd = null;
	static String socketHost = null;
	static int socketPort = 0;
	
	static{
		//FIX ME!!!
		//The parameters should be read from the configuration file
		dbName = "wireless_order_db";
		dbHost = "127.0.0.1";
		dbPort = 3306;
		dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?useUnicode=true&characterEncoding=utf8";
		dbUser = "root";
		dbPwd = "Sierra@123";
		socketHost = "127.0.0.1";
		socketPort = 55555;
	}
}
