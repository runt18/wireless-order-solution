package com.wireless.test.db.foodAssociation;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.foodAssociation.CalcFoodAssociationDao;
import com.wireless.exception.BusinessException;
import com.wireless.test.db.TestInit;

public class TestCalcFoodAssociation {
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
	}
	
	@Test
	public void testCalcFoodStatisticsDao() throws SQLException{
		System.out.println(CalcFoodAssociationDao.exec());
	}
}
