package com.wireless.test.db.inventoryMgr;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

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
		try{
			FoodMaterial fm = new FoodMaterial(term.restaurantID, 27937, 1, 0);
			FoodMaterialDao.insert(fm);
			System.out.println("绑定菜品和库存资料成功!");
		}catch(Exception e){
			e.printStackTrace();
			Assert.fail();
		}
	}
}
