package com.wireless.test.db.billStatistics;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.billStatistics.CalcMemberStatisticsDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.test.db.TestInit;

public class TestCalcMemberStatisticsDao {
	private static Staff mStaff;
	private static DutyRange mDutyRange;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException, ParseException{
		TestInit.init();
		 mStaff = StaffDao.getAdminByRestaurant(40);
		 mDutyRange = new DutyRange("2015-7-20", "2015-7-26"); 
	}
	
	@Test 
	public void test() throws BusinessException, SQLException, ParseException{
		System.out.println(CalcMemberStatisticsDao.calcStatisticsByEachDay(mStaff, mDutyRange, new CalcMemberStatisticsDao.ExtraCond(DateType.HISTORY)));
	}
}