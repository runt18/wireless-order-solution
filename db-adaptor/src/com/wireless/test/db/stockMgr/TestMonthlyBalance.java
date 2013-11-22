package com.wireless.test.db.stockMgr;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MonthlyBalance;
import com.wireless.pojo.stockMgr.MonthlyBalance.InsertBuilder;
import com.wireless.pojo.stockMgr.MonthlyBalanceDetail;
import com.wireless.test.db.TestInit;

public class TestMonthlyBalance {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDBParam() throws BusinessException, PropertyVetoException{
		TestInit.init();
		try{
			mStaff = StaffDao.getStaffs(26).get(0);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	private void compare(MonthlyBalance expected, MonthlyBalance actual){
		assertEquals("monthly_Balance_id", expected.getId(), actual.getId());
		assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		assertEquals("staffName", expected.getStaffName(), actual.getStaffName());
		assertEquals("month", expected.getMonth(), actual.getMonth());
		for (MonthlyBalanceDetail detail : expected.getDetails()) {
			int index = actual.getDetails().indexOf(detail);
			if(index >= 0){
				assertEquals("detailId", detail.getId(), actual.getDetails().get(index).getId());
				assertEquals("monthlyBalanceId", detail.getMonthlyBalanceId(), actual.getDetails().get(index).getMonthlyBalanceId());
				assertEquals("deptId", detail.getDeptId(), actual.getDetails().get(index).getDeptId());
				assertEquals("opening_balance", detail.getOpeningBalance(), actual.getDetails().get(index).getOpeningBalance(), 0.001);
				assertEquals("ending_balance", detail.getEndingBalance(), actual.getDetails().get(index).getEndingBalance(), 0.01);
			}else{
				assertEquals("monthly_balance_detail", false);
			}
		}
	}
	
	@Test
	public void testMonthlyBalanceDao() throws SQLException, BusinessException{
		
		int monthlyBalanceId = 0;
		
		try{
			long current = SystemDao.getCurrentMonth(mStaff);
			//新建月结记录
			MonthlyBalance.InsertBuilder build = new InsertBuilder(mStaff.getRestaurantId(), mStaff.getName());
			
			monthlyBalanceId = MonthlyBalanceDao.insert(build, mStaff);
			
			MonthlyBalance expected = build.build();
			
			MonthlyBalance actual = MonthlyBalanceDao.getMonthlyBalanceById(monthlyBalanceId);
			
			expected.setId(monthlyBalanceId);
			
			expected.setMonth(current);
			
			for (int i = 0; i < actual.getDetails().size(); i++) {
				expected.getDetails().get(i).setId(actual.getDetails().get(i).getId());
			}
			
			//比较
			compare(expected, actual);
			
		}finally{
			if(monthlyBalanceId != 0){
				MonthlyBalanceDao.delete(monthlyBalanceId);
				try{
					MonthlyBalanceDao.getMonthlyBalanceById(monthlyBalanceId);
					assertEquals("failed to get monthlyBalance", false);
				}catch(BusinessException ignored){}
			}
		}
	}
	
	
}
