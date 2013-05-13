package com.wireless.test.db.inventoryMgr;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.inventoryMgr.FoodMaterialDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.FoodMaterial;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestFoodMaterialDao {
	
	private static Terminal term;
	
	@BeforeClass
	public static void beforeClass(){
		TestInit.init();
		try {
			term = VerifyPin.exec(9720860, Terminal.MODEL_STAFF);
		} catch (BusinessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void insert(){
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			FoodMaterial fm = new FoodMaterial(term.restaurantID, 27935, 1, 0);
			FoodMaterialDao.insert(dbCon, fm);
		}catch(Exception e){
			e.printStackTrace();
			org.junit.Assert.fail();
		}finally{
			dbCon.disconnect();
		}
	}
}
