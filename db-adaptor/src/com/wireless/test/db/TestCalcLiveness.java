package com.wireless.test.db;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.restaurantMgr.RestaurantDao;

public class TestCalcLiveness {
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testCalcLiveness() throws SQLException{
		RestaurantDao.calcLiveness();
	}
}
