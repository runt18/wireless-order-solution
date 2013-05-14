package com.wireless.test.db;

import com.wireless.db.Params;

public final class TestInit {
	
	public static void init(){
		Params.setDbHost("192.168.157.100");
		Params.setDbPort(3306);
		Params.setDatabase("wireless_order_db");
		Params.setDbUser("root");
		Params.setDbPwd("HelloZ315");
	}
}
