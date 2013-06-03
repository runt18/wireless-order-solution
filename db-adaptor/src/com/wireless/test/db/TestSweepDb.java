package com.wireless.test.db;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.misc.SweepDB;

public class TestSweepDb {
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testSweepDb() throws SQLException{
		System.out.println(SweepDB.exec());
	}
}
