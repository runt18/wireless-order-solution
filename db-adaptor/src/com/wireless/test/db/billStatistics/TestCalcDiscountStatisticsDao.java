package com.wireless.test.db.billStatistics;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.billStatistics.CalcDiscountStatisticsDao;
import com.wireless.db.billStatistics.CalcDiscountStatisticsDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByEachDay;
import com.wireless.pojo.billStatistics.discount.DiscountIncomeByStaff;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.test.db.TestInit;

public class TestCalcDiscountStatisticsDao {

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
	public void testCalcDiscountStatistics() throws SQLException, ParseException, BusinessException{
		float totalIncome = 0;
		float totalAmount = 0;
		
		List<DiscountIncomeByEachDay> incomesByEachDay = CalcDiscountStatisticsDao.calcIncomeByEachDay(mStaff, mExtraCond);
		@SuppressWarnings("unused")
		float totalIncomeByEachDay = 0;
		@SuppressWarnings("unused")
		float totalAmountByEachDay = 0;
		for(DiscountIncomeByEachDay each : incomesByEachDay){
			totalIncomeByEachDay += each.getPrice();
			totalAmountByEachDay += each.getAmount();
		}
		
//		mExtraCond.setStaffId(mStaff.getId());
		List<DiscountIncomeByStaff> incomesByStaff = CalcDiscountStatisticsDao.calcIncomeByStaff(mStaff, mExtraCond);
		float totalIncomeByStaff = 0;
		float totalAmountByStaff = 0;
		for(DiscountIncomeByStaff each : incomesByStaff){
			totalIncomeByStaff += each.getmDiscountPrice();
			totalAmountByStaff += each.getmDiscountAmount();
		}
		
		Assert.assertEquals("discount income  is different from the one by staff", totalIncome, totalIncomeByStaff, 0.01);
		Assert.assertEquals("discount amount  is different from the one by staff", totalAmount, totalAmountByStaff, 0.01);
		

	}

}
