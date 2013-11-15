package com.wireless.test.db.weixin.restaurant;

import java.beans.PropertyVetoException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.weixin.CalcWeixinSignature;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.test.db.TestInit;

public class TestWeixinRestaurantDao {
	
	private static final String WEIXIN_RESTAURANT_SERIAL = "oACWTjsRKuGYTjEpEyG7fPTg06fc";
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
	}
	
	@Test
	public void testWeixinRestaurant() throws SQLException, BusinessException, NoSuchAlgorithmException{
		final String account = "demo";
		final String timestamp = "2013-9-11 7:48:00";
		final String nonce = "jingyang";
		WeixinRestaurantDao.verify(account, CalcWeixinSignature.calc(RestaurantDao.getByAccount(account).getAccount(), timestamp, nonce), timestamp, nonce);
		Assert.assertTrue("verify restaurant", WeixinRestaurantDao.isVerified(account));
		
		WeixinRestaurantDao.bind(WEIXIN_RESTAURANT_SERIAL, account);
		Assert.assertTrue("weixin serial is bound to restaurant account", WeixinRestaurantDao.isBound(WEIXIN_RESTAURANT_SERIAL, account));
		Assert.assertEquals("restaurant bound to weixin serial", RestaurantDao.getByAccount(account).getId(), WeixinRestaurantDao.getRestaurantIdByWeixin(WEIXIN_RESTAURANT_SERIAL));
	}
}
