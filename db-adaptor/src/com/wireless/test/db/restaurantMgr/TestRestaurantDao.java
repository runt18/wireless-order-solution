package com.wireless.test.db.restaurantMgr;

import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestRestaurantDao {
	@Test
	public void testQueryByID(){
		try{
			TestInit.init();
			DBCon dbCon = new DBCon();
			dbCon.connect();
			Terminal term = VerifyPin.exec(217, Terminal.MODEL_STAFF);
			Restaurant restaurant = RestaurantDao.queryByID(term);
			System.out.println(restaurant.toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	@Test
	public void testUpdate(){
		//Terminal terminal,Restaurant restaurant
		try{
			DBCon dbCon = new DBCon();
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(217, Terminal.MODEL_STAFF);
			dbCon.disconnect();
			Restaurant restaurant = RestaurantDao.queryByID(terminal);
			restaurant.setRestaurantName("好旺角休闲餐厅");
			RestaurantDao.update(terminal, restaurant);
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
