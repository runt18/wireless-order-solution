package com.wireless.test.db;

import java.beans.PropertyVetoException;

import com.wireless.db.DBCon;

public final class TestInit {
	
	public static void init() throws PropertyVetoException{
		DBCon.init("127.0.0.1", "3306", "wireless_order_db", "root", "root");
	}
}
