package com.wireless.test.sms;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.http.client.ClientProtocolException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.test.db.TestInit;
import com.wireless.util.sms.SMS;

public class TestSMS {
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
	}
	
	@Test
	public void testSMS() throws ClientProtocolException, IOException, SQLException, BusinessException{
		System.out.println(SMS.send("18520590932", new SMS.Msg("你操作的验证码是0000", RestaurantDao.getByAccount("demo"))));
	}
}
