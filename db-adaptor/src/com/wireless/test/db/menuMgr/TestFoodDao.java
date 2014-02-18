package com.wireless.test.db.menuMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FoodError;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestFoodDao {
	private static Staff mStaff;
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getStaffs(26).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCombo() throws SQLException, BusinessException{
		int foodId = 0;
		try {
			Food.InsertBuilder insertBuilder = new Food.InsertBuilder("测试套菜", 15.4f, KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL).get(0))
													   .setAliasId(100).setDesc("测试描述").setHot(true).setCommission(2.0f).setGift(true).setWeigh(false);
			foodId = FoodDao.insert(mStaff, insertBuilder); 
			
			List<Food> foods = FoodDao.getPureFoods(mStaff);
			Food childFood1 = foods.get(0);
			Food childFood2 = foods.get(2);
			
			Food.ComboBuilder comboBuilder = new Food.ComboBuilder(foodId)
													 .addChild(childFood1.getFoodId(), 1)
													 .addChild(childFood2.getFoodId(), 2);
			FoodDao.buildCombo(mStaff, comboBuilder);
			
			Food expected = comboBuilder.build();
			Food actual = FoodDao.getById(mStaff, foodId);
			
			Assert.assertEquals("parent id : insert combo", expected.getFoodId(), actual.getFoodId());
			Assert.assertEquals("children : insert combo", expected.getChildFoods(), actual.getChildFoods());
			
		}finally{
			if(foodId != 0){
				FoodDao.delete(mStaff, foodId);
				try{
					FoodDao.getById(mStaff, foodId);
					Assert.assertTrue("failed to delete the food", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the food", FoodError.FOOD_NOT_EXIST, e.getErrCode());
				}
				
				Assert.assertEquals("failed to delete the child foods", 0, FoodDao.getChildrenByParent(mStaff, foodId).size());
			}
		}
	}
	
	@Test
	public void testFoodDao() throws SQLException, BusinessException{
		int foodId = 0;
		try {
			Food.InsertBuilder insertBuilder = new Food.InsertBuilder("测试菜品", 15.4f, KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL).get(0))
													   .setAliasId(100).setDesc("测试描述").setHot(true).setCommission(2.0f).setGift(true).setWeigh(false);
			foodId = FoodDao.insert(mStaff, insertBuilder); 
			
			Food expected = insertBuilder.build();
			Food actual = FoodDao.getById(mStaff, foodId);
			
			compare(foodId, expected, actual, "insert food");

			Food.UpdateBuilder updateBuilder = new Food.UpdateBuilder(foodId).setAliasId(100).setName("测试修改菜品")
													   .setKitchen(KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL).get(1))
													   .setImage("hello")
													   .setPrice(34.2f).setDesc("测试修改描述")
													   .setHot(false).setCommission(3).setSellOut(true).setRecommend(true)
													   .setGift(false).setWeigh(true);
			FoodDao.update(mStaff, updateBuilder);
			
			expected = updateBuilder.build();
			actual = FoodDao.getById(mStaff, foodId);

			compare(foodId, expected, actual, "update food");
			
		} finally{
			if(foodId != 0){
				FoodDao.delete(mStaff, foodId);
				try{
					FoodDao.getById(mStaff, foodId);
					Assert.assertTrue("failed to delete the food", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the food", FoodError.FOOD_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compare(int foodId, Food expected, Food actual, final String tag){
		Assert.assertEquals("id : " + tag, foodId, actual.getFoodId());
		Assert.assertEquals("restaurant : " + tag, mStaff.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("name : " + tag, expected.getName(), actual.getName());
		Assert.assertEquals("price : " + tag, expected.getPrice(), actual.getPrice(), 0.01);
		Assert.assertEquals("kitchen : " + tag, expected.getKitchen(), actual.getKitchen());
		Assert.assertEquals("alias : " + tag, expected.getAliasId(), actual.getAliasId());
		Assert.assertEquals("desc : " + tag, expected.getDesc(), actual.getDesc());
		Assert.assertEquals("image : " + tag, expected.getImage(), actual.getImage());
		Assert.assertEquals("current price : " + tag, expected.isCurPrice(), actual.isCurPrice());
		Assert.assertEquals("recommend : " + tag, expected.isRecommend(), actual.isRecommend());
		Assert.assertEquals("sell out : " + tag, expected.isSellOut(), actual.isSellOut());
		Assert.assertEquals("special : " + tag, expected.isSpecial(), actual.isSpecial());
		Assert.assertEquals("weight : " + tag, expected.isWeigh(), actual.isWeigh());
		Assert.assertEquals("hot : " + tag, expected.isHot(), actual.isHot());
		Assert.assertEquals("gift : " + tag, expected.isGift(), actual.isGift());
		Assert.assertEquals("commission : " + tag, expected.isCommission(), actual.isCommission());
		Assert.assertEquals("commission : " + tag, expected.getCommission(), actual.getCommission(), 0.01);
	}
}
