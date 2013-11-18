package com.wireless.test.db.stockMgr;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MonthlyBalance;
import com.wireless.pojo.stockMgr.MonthlyBalance.InsertBuilder;
import com.wireless.pojo.stockMgr.MonthlyBalanceDetail;
import com.wireless.pojo.util.DateUtil;
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
			if(index > 0){
				assertEquals("detailId", detail.getId(), actual.getDetails().get(index).getId());
				assertEquals("monthlyBalanceId", detail.getMonthlyBalanceId(), actual.getDetails().get(index).getMonthlyBalanceId());
				assertEquals("deptId", detail.getDeptId(), actual.getDetails().get(index).getDeptId());
				assertEquals("opening_balance", detail.getOpeningBalance(), actual.getDetails().get(index).getOpeningBalance(), 0.001);
				assertEquals("ending_balance", detail.getEndingBalance(), actual.getDetails().get(index).getEndingBalance(), 0.001);
			}else{
				assertEquals("monthly_balance_id", false);
			}
		}
	}
	
	@Test
	public void testMonthlyBalanceDao() throws SQLException, BusinessException{
		
		int monthlyBalanceId = 0;
		
		try{
			//新建月结记录
			
			List<Department> depts = DepartmentDao.getDepartments(mStaff, null, null);
			
			MonthlyBalance.InsertBuilder build = new InsertBuilder(mStaff.getRestaurantId(), mStaff.getName(), DateUtil.parseDate("2013-11-01"));
			
			MonthlyBalanceDetail.InsertBuilder detailBuild = new MonthlyBalanceDetail.InsertBuilder(depts.get(0).getId(), 900, 80);
			monthlyBalanceId = MonthlyBalanceDao.insert(build);
			
			MonthlyBalance expected = build.build();
			expected.setId(monthlyBalanceId);
			
			MonthlyBalance actual = MonthlyBalanceDao.getMonthlyBalanceById(monthlyBalanceId);
			
			//比较
			compare(expected, actual);
			
		}finally{
			if(monthlyBalanceId != 0){
				MonthlyBalanceDao.delete(monthlyBalanceId);
				try{
					MonthlyBalanceDao.getMonthlyBalanceById(monthlyBalanceId);
					assertEquals("failed to delete monthlyBalance", false);
				}catch(BusinessException ignored){}
			}
		}
	}
	
	
}
