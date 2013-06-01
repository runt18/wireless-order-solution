package com.wireless.test.db.menuMgr;

import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestFoodBasicDao {
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
	public void insert() throws SQLException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			Food fb = new Food();
			fb.setRestaurantId(26);
			fb.setAliasId(44);
			fb.setName("44");
			fb.setPinyin("44");
			fb.setPrice(0.00f);
			fb.getKitchen().setAliasId((short) 0);
			fb.getKitchen().setId(160);
			fb.setStockStatus(Food.StockStatus.GOOD);
			
			FoodDao.insertFoodBaisc(dbCon, term, fb);
			System.out.println("菜品资料添加成功.");
			
			dbCon.conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbCon.conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			fail();
		} finally{
			dbCon.disconnect();
		}
	}
	
	@Test
	public void delete() throws SQLException{
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			
			Food fb = new Food();
			fb.setFoodId(27945);
			fb.setRestaurantId(26);
			
			FoodDao.deleteFood(fb);
			System.out.println("菜品资料删除成功.");
			
			dbCon.conn.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				dbCon.conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			fail();
		} finally{
			dbCon.disconnect();
		}
	}
	
}
