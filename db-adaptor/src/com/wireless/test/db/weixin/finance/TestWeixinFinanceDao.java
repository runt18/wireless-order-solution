package com.wireless.test.db.weixin.finance;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.weixin.finance.WeixinFinanceDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxFinanceError;
import com.wireless.test.db.TestInit;

public class TestWeixinFinanceDao {

	private static final String WEIXIN_SERIAL = "oACWTjsRKuGYTjEpEyG7fPTg06fc";
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
	}
	
	@Test
	public void testWeixinFinance() throws SQLException, BusinessException{
		//Check to see whether the weixin and restaurant binding is correct
		WeixinFinanceDao.bind(WEIXIN_SERIAL, "demo", "demo@123");
		WeixinFinanceDao.getRestaurantIdByWeixin(WEIXIN_SERIAL);
		Assert.assertEquals("binding restaurant id", WeixinFinanceDao.getRestaurantIdByWeixin(WEIXIN_SERIAL), RestaurantDao.getByAccount("demo").getId());
		
		//Check to see whether the business exception would be thrown in case of password NOT correct
		try{
			WeixinFinanceDao.bind(WEIXIN_SERIAL, "demo", "1demo@123");
		}catch(BusinessException e){
			Assert.assertEquals("binding not correct", WxFinanceError.ACCOUNT_PWD_NOT_MATCH, e.getErrCode());
		}
	}
}
