package com.wireless.test.db.billStatistics;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.billStatistics.CalcCancelStatisticsDao;
import com.wireless.db.billStatistics.CalcCancelStatisticsDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByEachDay;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.cancel.CancelIncomeByStaff;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.test.db.TestInit;

public class TestCalcCancelStatisticsDao {
	private static Staff mStaff;
	private static DutyRange mDutyRange;
	private static ExtraCond mExtraCond;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException, ParseException{
		TestInit.init();
		 mStaff = StaffDao.getAdminByRestaurant(40);
		 mDutyRange = new DutyRange("2014-2-10 23:40:04", "2014-2-26 23:49:36"); 
		 mExtraCond = new ExtraCond(DateType.HISTORY);
//		 mExtraCond.setDeptId(Department.DeptId.DEPT_2);
//		 mExtraCond.setRegionId(Region.RegionId.REGION_1);
//		 mExtraCond.setHourRange(new HourRange("10:00:00", "12:00:00"));
	}
	
	@Test
	public void testCalcCancelStatistics() throws SQLException, ParseException, BusinessException{
		List<CancelIncomeByReason> incomesByReason = CalcCancelStatisticsDao.calcCancelIncomeByReason(mStaff, mExtraCond.setRange(mDutyRange));
		float totalIncomeByReason = 0;
		float totalAmountByReason = 0;
		for(CancelIncomeByReason each : incomesByReason){
			totalIncomeByReason += each.getCancelPrice();
			totalAmountByReason += each.getCancelAmount();
		}
		
		List<CancelIncomeByStaff> incomesByStaff = CalcCancelStatisticsDao.calcCancelIncomeByStaff(mStaff, mExtraCond.setRange(mDutyRange));
		float totalIncomeByStaff = 0;
		float totalAmountByStaff = 0;
		for(CancelIncomeByStaff each : incomesByStaff){
			totalIncomeByStaff += each.getCancelPrice();
			totalAmountByStaff += each.getCancelAmount();
		}
		
		Assert.assertEquals("cancel income by reason is different from the one by staff", totalIncomeByReason, totalIncomeByStaff, 0.01);
		Assert.assertEquals("cancel amount by reason is different from the one by staff", totalAmountByReason, totalAmountByStaff, 0.01);
		
		List<CancelIncomeByDept> incomesByDept = CalcCancelStatisticsDao.calcCancelIncomeByDept(mStaff, mExtraCond.setRange(mDutyRange));
		float totalIncomeByDept = 0;
		float totalAmountByDept = 0;
		for(CancelIncomeByDept each : incomesByDept){
			totalIncomeByDept += each.getCancelPrice();
			totalAmountByDept += each.getCancelAmount();
		}
		
		Assert.assertEquals("cancel income by department is different from the one by staff", totalIncomeByDept, totalIncomeByStaff, 0.01);
		Assert.assertEquals("cancel amount by department is different from the one by staff", totalAmountByDept, totalAmountByStaff, 0.01);
		
		List<CancelIncomeByEachDay> incomesByEachDay = CalcCancelStatisticsDao.calcCancelIncomeByEachDay(mStaff, mDutyRange, mExtraCond);
		@SuppressWarnings("unused")
		float totalIncomeByEachDay = 0;
		@SuppressWarnings("unused")
		float totalAmountByEachDay = 0;
		for(CancelIncomeByEachDay each : incomesByEachDay){
			totalIncomeByEachDay += each.getCancelPrice();
			totalAmountByEachDay += each.getCancelAmount();
		}
		
		//Assert.assertEquals("cancel amount by each day is different from the one by staff", totalAmountByEachDay, totalAmountByStaff, 0.01);
		//Assert.assertEquals("cancel income by each day is different from the one by staff", totalIncomeByEachDay, totalIncomeByStaff, 0.01);
	}
}
