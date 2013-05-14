package com.wireless.test.db.tasteRef;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.test.db.TestInit;

public class TestTasteRefDao {
	
	@BeforeClass
	public static void initDbParam() {
		TestInit.init();
	}
	
	
	@Test 
	public void testSmartCalc() throws SQLException{
		TasteRefDao.exec();
	}
}
