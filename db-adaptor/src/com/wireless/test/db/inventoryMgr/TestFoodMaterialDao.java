package com.wireless.test.db.inventoryMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	public static void beforeClass() throws PropertyVetoException{
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
			FoodMaterial fm = new FoodMaterial(term.restaurantID, 27942, 6, 1);
			FoodMaterialDao.insert(fm);
			System.out.println("绑定菜品和库存资料成功!");
		}catch(Exception e){
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void deleteAll(){
		try{
			FoodMaterialDao.deleteAll(27942, true);
			System.out.println("删除菜品原料配置成功;");
		}catch(Exception e){
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void update(){
		try{
			List<FoodMaterial> list = new ArrayList<FoodMaterial>();
			FoodMaterial a1 = new FoodMaterial(26, 27929, 5, 2);
			
			list.add(a1);
			FoodMaterialDao.update(27929, list);
			System.out.println("更新菜品原料配置成功;");
		}catch(Exception e){
			e.printStackTrace();
			Assert.fail();
		}
	}
	
}
