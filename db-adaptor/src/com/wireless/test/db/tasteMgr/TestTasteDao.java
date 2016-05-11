package com.wireless.test.db.tasteMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.orderMgr.TasteGroupDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.tasteMgr.TasteCategoryDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.pojo.tasteMgr.TasteCategory;
import com.wireless.test.db.TestInit;

public class TestTasteDao {
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getAdminByRestaurant(63);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testTasteDao() throws SQLException, BusinessException{
		int categoryId = 0;
		int tasteId = 0;
		try{
			categoryId = TasteCategoryDao.insert(mStaff, new TasteCategory.InsertBuilder(mStaff.getRestaurantId(), "测试类型"));
			TasteCategory category = TasteCategoryDao.getById(mStaff, categoryId);
			
			//Insert a new taste
			Taste.InsertBuilder builder = new Taste.InsertBuilder(mStaff.getRestaurantId(), "测试", category)
												   .setPrice(3);
			tasteId = TasteDao.insert(mStaff, builder);
			
			Taste expected = builder.build();
			expected.setTasteId(tasteId);
			Taste actual = TasteDao.getById(mStaff, tasteId);
			
			Assert.assertEquals("the id to taste", expected.getTasteId(), actual.getTasteId());
			Assert.assertEquals("the prefence to taste", expected.getPreference(), actual.getPreference());
			Assert.assertEquals("the price to taste", expected.getPrice(), actual.getPrice(), 0.01);
			Assert.assertEquals("the calc type to taste", expected.getCalc(), actual.getCalc());
			Assert.assertEquals("the rate to taste", expected.getRate(), actual.getRate(), 0.01);
			Assert.assertEquals("the type to taste", expected.getType(), actual.getType());
			Assert.assertEquals("the restaurant to taste", expected.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("the category to taste", expected.getCategory(), actual.getCategory());

			//Update the taste
			Taste.UpdateBuilder updateBuilder = new Taste.UpdateBuilder(tasteId)
														 .setPrefence("修改测试口味")
														 .setPrice(3);
			TasteDao.update(mStaff, updateBuilder);
			
			expected = updateBuilder.build();
			expected.setRestaurantId(mStaff.getRestaurantId());
			expected.setCategory(category);
			
			actual = TasteDao.getById(mStaff, tasteId);
			
			Assert.assertEquals("the id to taste", expected.getTasteId(), actual.getTasteId());
			Assert.assertEquals("the prefence to taste", expected.getPreference(), actual.getPreference());
			Assert.assertEquals("the price to taste", expected.getPrice(), actual.getPrice(), 0.01);
			Assert.assertEquals("the calc type to taste", expected.getCalc(), actual.getCalc());
			Assert.assertEquals("the type to taste", expected.getType(), actual.getType());
			Assert.assertEquals("the restaurant to taste", expected.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("the category to taste", expected.getCategory(), actual.getCategory());
			
		}finally{
			if(tasteId != 0){
				TasteDao.delete(mStaff, tasteId);
				TasteCategoryDao.delete(mStaff, categoryId);
				try {
					TasteDao.getById(mStaff, tasteId);
					Assert.assertTrue("failed to delete taste", false);
				} catch (BusinessException ignored) {}
			}
		}
	}
	
	@Test
	public void testTasteCleanup() throws SQLException{
		System.out.println(TasteGroupDao.cleanup());
	}
}
