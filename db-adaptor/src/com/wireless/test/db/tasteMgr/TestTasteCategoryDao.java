package com.wireless.test.db.tasteMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.tasteMgr.TasteCategoryDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TasteError;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.TasteCategory;
import com.wireless.test.db.TestInit;

public class TestTasteCategoryDao {
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
	public void testTasteCategoryDao() throws SQLException, BusinessException{
		int categoryId = 0;
		try{
			TasteCategory.InsertBuilder builder = new TasteCategory.InsertBuilder(mStaff.getRestaurantId(), "测试口味类型");
			categoryId = TasteCategoryDao.insert(mStaff, builder);
			
			TasteCategory expected = builder.build();
			expected.setId(categoryId);
			compare(expected, TasteCategoryDao.getById(mStaff, categoryId));
			
			TasteCategory.UpdateBuilder updateBuilder = new TasteCategory.UpdateBuilder(categoryId, "新口味类型");
			TasteCategoryDao.update(mStaff, updateBuilder);
			expected.setName("新口味类型");
			compare(expected, TasteCategoryDao.getById(mStaff, categoryId));
			
		}finally{
			if(categoryId != 0){
				TasteCategoryDao.delete(mStaff, categoryId);
				try{
					TasteCategoryDao.getById(mStaff, categoryId);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the taste category", TasteError.TASTE_CATE_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compare(TasteCategory expected, TasteCategory actual){
		Assert.assertEquals("id to taste cateogry", expected.getId(), actual.getId());
		Assert.assertEquals("restaurant id to taste category", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("type to taste category", expected.getType(), actual.getType());
		Assert.assertEquals("status to taste category", expected.getStatus(), actual.getStatus());
		Assert.assertEquals("name to taste category", expected.getName(), actual.getName());
	}
}
