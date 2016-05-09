package com.wireless.test.db.billStatistics;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.billStatistics.CalcCommissionStatisticsDao;
import com.wireless.db.billStatistics.CalcCommissionStatisticsDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.commission.CommissionIncomeByEachDay;
import com.wireless.pojo.billStatistics.commission.CommissionIncomeByStaff;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.test.db.TestInit;

public class TestCalcCommissionStatisticsDao {

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
	public void testCalcCommissionStatistics() throws SQLException, ParseException, BusinessException{
		float totalIncome = 0;
		float totalAmount = 0;
		
		List<CommissionIncomeByEachDay> incomesByEachDay = CalcCommissionStatisticsDao.calcIncomeByEachDay(mStaff, mExtraCond);
		@SuppressWarnings("unused")
		float totalIncomeByEachDay = 0;
		@SuppressWarnings("unused")
		float totalAmountByEachDay = 0;
		for(CommissionIncomeByEachDay each : incomesByEachDay){
			totalIncomeByEachDay += each.getmCommissionPrice();
			totalAmountByEachDay += each.getmCommissionAmount();
		}
		
//		mExtraCond.setStaffId(mStaff.getId());
		List<CommissionIncomeByStaff> incomesByStaff = CalcCommissionStatisticsDao.calcIncomeByStaff(mStaff,  mExtraCond);
		float totalIncomeByStaff = 0;
		float totalAmountByStaff = 0;
		for(CommissionIncomeByStaff each : incomesByStaff){
			totalIncomeByStaff += each.getmCommissionPrice();
			totalAmountByStaff += each.getmCommissionAmount();
		}
		
		Assert.assertEquals("commission income  is different from the one by staff", totalIncome, totalIncomeByStaff, 0.01);
		Assert.assertEquals("commission amount  is different from the one by staff", totalAmount, totalAmountByStaff, 0.01);
		

	}

}
