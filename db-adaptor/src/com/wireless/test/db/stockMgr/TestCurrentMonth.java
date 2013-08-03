package com.wireless.test.db.stockMgr;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.Setting;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestCurrentMonth {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDBparam() throws BusinessException, SQLException, PropertyVetoException {
		TestInit.init();
		try{
			mStaff = StaffDao.getStaffs(37).get(0);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	//期望值与真实值比较
	private static void compare(Setting expected, Setting actual){
		assertEquals("settingId", expected.getId(), actual.getId());
		assertEquals("restaurantId", expected.getRestaurantID(), actual.getRestaurantID());
		assertEquals("currentMonth", expected.getLongCurrentMonth() , actual.getLongCurrentMonth());
	}
	
	@Test
	public void testSetting() throws BusinessException, SQLException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		System.out.println(mStaff.getRestaurantId());
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND B.restaurant_id = " + mStaff.getRestaurantId());
		
		Setting expectedSetting = SystemDao.getSystemSetting(params).get(0).getSetting();
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(expectedSetting.getLongCurrentMonth()));
		//给日期增加一个工作月
		c.add(Calendar.MONTH, +1);
		
		expectedSetting.setCurrentMonth(c.getTime().getTime());
		
		SystemDao.updateCurrentMonth(mStaff);
		System.out.println(expectedSetting.getId());
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND B.restaurant_id = " + mStaff.getRestaurantId());
		
		Setting actualSetting = SystemDao.getSystemSetting(params).get(0).getSetting();
		
		
		compare(expectedSetting, actualSetting);		
		
	} 
	
	
	
	
	
	
	
}
