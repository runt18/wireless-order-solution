package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.system.Setting;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestCurrentMonth {

	private static Terminal mTerminal;
	
	@BeforeClass
	public static void initDBparam() throws BusinessException, SQLException, PropertyVetoException {
		TestInit.init();
		try{
			mTerminal = VerifyPin.exec(217, Terminal.MODEL_STAFF);
		}catch(SQLException e){
			e.printStackTrace();
		}catch(BusinessException e){
			e.printStackTrace();
		}
	}
	
	//期望值与真实值比较
	private static void compare(Setting expected, Setting actual){
		Assert.assertEquals("settingId", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantID(), actual.getRestaurantID());
		Assert.assertEquals("currentMonth", expected.getCurrentMonth() , actual.getCurrentMonth());
	}
	
	@Test
	public void testSetting() throws BusinessException, SQLException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.restaurant_id = " + mTerminal.restaurantID);
		
		Setting expectedSetting = SystemDao.getSystemSetting(params).get(0).getSetting();
		
		Calendar c = Calendar.getInstance();
		c.setTime(new Date(expectedSetting.getCurrentMonth()));
		//给日期增加一个工作月
		c.add(Calendar.MONTH, +1);
		
		expectedSetting.setCurrentMonth(c.getTime().getTime());
		
		SystemDao.updateCurrentMonth(expectedSetting);
		
		Setting actualSetting = SystemDao.getSystemSettingById(expectedSetting.getId()).getSetting();
		
		compare(expectedSetting, actualSetting);
		
		
	} 
	
	
	
	
	
	
	
}
