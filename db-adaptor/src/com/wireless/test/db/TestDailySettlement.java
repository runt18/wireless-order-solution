package com.wireless.test.db;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.DailySettleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;

public class TestDailySettlement {
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testDailySettlement() throws SQLException, BusinessException{
		System.out.println(DailySettleDao.manual(StaffDao.getAdminByRestaurant(40)));
		System.out.println(DailySettleDao.auto());
	}
}
