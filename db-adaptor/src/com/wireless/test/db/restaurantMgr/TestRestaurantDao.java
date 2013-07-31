package com.wireless.test.db.restaurantMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestRestaurantDao {
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testQueryByID() throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(217, Terminal.MODEL_STAFF);
		
		Restaurant oriRestaurant = RestaurantDao.getById(term.restaurantID);
		
		Restaurant restToUpdate = new Restaurant();
		restToUpdate.setId(oriRestaurant.getId());
		restToUpdate.setAccount(oriRestaurant.getAccount());
		restToUpdate.setName("测试名称");
		restToUpdate.setAddress("测试地址");
		restToUpdate.setInfo("测试信息");
		restToUpdate.setTele1("测试电话1");
		restToUpdate.setTele2("测试电话2");
		
		RestaurantDao.update(term, restToUpdate);
		
		Restaurant restAfterUpdate = RestaurantDao.getById(term.restaurantID);
		
		assertEquals("restaurant id", restToUpdate.getId(), restAfterUpdate.getId());
		assertEquals("restaurant id", restToUpdate.getId(), restAfterUpdate.getId());
		assertEquals("restaurant account", restToUpdate.getAccount(), restAfterUpdate.getAccount());
		assertEquals("restaurant name", restToUpdate.getName(), restAfterUpdate.getName());
		assertEquals("restaurant info", restToUpdate.getInfo(), restAfterUpdate.getInfo());
		assertEquals("restaurant address", restToUpdate.getAddress(), restAfterUpdate.getAddress());
		assertEquals("restaurant 1st tele", restToUpdate.getTele1(), restAfterUpdate.getTele1());
		assertEquals("restaurant 2nd tele", restToUpdate.getTele2(), restAfterUpdate.getTele2());

		//restore the original restaurant info
		RestaurantDao.update(term, oriRestaurant);
	}
	
}
