package com.wireless.test.db;

import java.beans.PropertyVetoException;

import com.wireless.db.DBCon;

public final class TestInit {
	
	public static void init() throws PropertyVetoException{
		DBCon.init("192.168.157.100", "3306", "wireless_order_db", "root", "HelloZ315");
	}
}
