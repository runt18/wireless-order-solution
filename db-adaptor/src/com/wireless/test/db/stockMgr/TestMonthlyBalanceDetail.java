package com.wireless.test.db.stockMgr;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MonthlyBalanceDetail;
import com.wireless.test.db.TestInit;

public class TestMonthlyBalanceDetail {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDBParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try{
			mStaff = StaffDao.getStaffs(26).get(0);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	private void compare(MonthlyBalanceDetail expected, MonthlyBalanceDetail actual){
		assertEquals("detailId", expected.getId(), actual.getId());
		assertEquals("monthlyBalanceId", expected.getMonthlyBalanceId(), actual.getMonthlyBalanceId());
		assertEquals("deptId", expected.getDeptId(), actual.getDeptId());
		assertEquals("opening_balance", expected.getOpeningBalance(), actual.getOpeningBalance(), 0.001);
		assertEquals("ending_balance", expected.getEndingBalance(), actual.getEndingBalance(), 0.001);
	}
	
	@Test
	public void testMonthlyBalanceDetailDao(){
		
	}
	
}
