package com.wireless.test.db.billStatistics;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.billStatistics.CalcRepaidStatisticsDao;
import com.wireless.db.billStatistics.CalcRepaidStatisticsDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.repaid.RepaidIncomeByEachDay;
import com.wireless.pojo.billStatistics.repaid.RepaidIncomeByStaff;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.test.db.TestInit;

public class TestCalcRepaidStatisticsDao {

	private static Staff mStaff;
	private static DutyRange mDutyRange;
	private static ExtraCond mExtraCond;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException, ParseException{
		TestInit.init();
		 mStaff = StaffDao.getAdminByRestaurant(40);
		 mDutyRange = new DutyRange("2014-6-10 00:40:04", "2014-6-26 23:59:36"); 
		 mExtraCond = new ExtraCond(DateType.HISTORY).setDutyRange(mDutyRange);
//		 mExtraCond.setHourRange(new HourRange("06:00:00", "12:00:00"));
	}
	
	@Test
	public void testCalcRepaidStatistics() throws SQLException, ParseException, BusinessException{
		float totalIncomeByReason = 0;
		float totalAmountByReason = 0;
		
		List<RepaidIncomeByEachDay> incomesByEachDay = CalcRepaidStatisticsDao.calcRepaidIncomeByEachDay(mStaff, mDutyRange, mExtraCond);
		@SuppressWarnings("unused")
		float totalIncomeByEachDay = 0;
		@SuppressWarnings("unused")
		float totalAmountByEachDay = 0;
		for(RepaidIncomeByEachDay each : incomesByEachDay){
			totalIncomeByEachDay += each.getRepaidPrice();
			totalAmountByEachDay += each.getRepaidAmount();
		}
		
		mExtraCond.setStaffId(mStaff.getId());
		List<RepaidIncomeByStaff> incomesByStaff = CalcRepaidStatisticsDao.calcRepaidIncomeByStaff(mStaff, mExtraCond);
		float totalIncomeByStaff = 0;
		float totalAmountByStaff = 0;
		for(RepaidIncomeByStaff each : incomesByStaff){
			totalIncomeByStaff += each.getmRepaidPrice();
			totalAmountByStaff += each.getmRepaidAmount();
		}
		
		Assert.assertEquals("repaid income by reason is different from the one by staff", totalIncomeByReason, totalIncomeByStaff, 0.01);
		Assert.assertEquals("repaid amount by reason is different from the one by staff", totalAmountByReason, totalAmountByStaff, 0.01);
		

	}
}
