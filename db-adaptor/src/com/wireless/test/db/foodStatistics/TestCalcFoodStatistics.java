package com.wireless.test.db.foodStatistics;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.foodStatistics.CalcFoodStatisticsDao;
import com.wireless.exception.BusinessException;
import com.wireless.test.db.TestInit;

public class TestCalcFoodStatistics {

	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
	}
	
	@Test
	public void testCalcFoodStatisticsDao() throws SQLException{
		System.out.println(CalcFoodStatisticsDao.exec());
	}
	
}
