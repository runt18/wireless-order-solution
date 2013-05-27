package com.wireless.test.db;

import com.wireless.db.Params;

public final class TestInit {
	
	public static void init(){
		Params.setDbHost("127.0.0.1");
		Params.setDbPort(3306);
		Params.setDatabase("wireless_order_db");
		Params.setDbUser("root");
		Params.setDbPwd("root");
	}
}
