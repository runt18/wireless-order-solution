package com.wireless.test.db.tasteRef;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.test.db.TestInit;

public class TestTasteRefDao {
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException {
		TestInit.init();
	}
	
	
	@Test 
	public void testTasteRefDao() throws SQLException{
		System.out.println(TasteRefDao.exec());
	}
}
