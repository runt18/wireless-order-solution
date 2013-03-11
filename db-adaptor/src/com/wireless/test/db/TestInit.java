package com.wireless.test.db;

import com.wireless.db.Params;

public final class TestInit {
	
	public static void init(){
		Params.setDbUser("root");
		Params.setDbHost("192.168.157.100");
		Params.setDbPort(3306);
		Params.setDatabase("wireless_order_db");
		Params.setDbPwd("HelloZ315");
	}
}
