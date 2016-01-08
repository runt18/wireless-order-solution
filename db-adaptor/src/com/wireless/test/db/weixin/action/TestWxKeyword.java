package com.wireless.test.db.weixin.action;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.action.WxKeywordDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.action.WxKeyword;
import com.wireless.test.db.TestInit;

public class TestWxKeyword {
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException{
		TestInit.init();
		mStaff = StaffDao.getAdminByRestaurant(40);
	}
	
	@Test
	public void testWxKeyword() throws SQLException, BusinessException{
		int keywordId = 0;
		try{
			WxKeyword.InsertBuilder insertBuilder = new WxKeyword.InsertBuilder("测试关键字", WxKeyword.Type.NORMAL);
			keywordId = WxKeywordDao.insert(mStaff, insertBuilder);
			
			WxKeyword expected = insertBuilder.build();
			expected.setId(keywordId);
			
			WxKeyword actual = WxKeywordDao.getByCond(mStaff, new WxKeywordDao.ExtraCond().setId(keywordId)).get(0);
			compare(expected, actual);
			
			WxKeyword.UpdateBuilder updateBuilder = new WxKeyword.UpdateBuilder(keywordId).setKeyword("修改关键字").setAction(100);
			WxKeywordDao.update(mStaff, updateBuilder);
			
			expected = updateBuilder.build();
			expected.setId(keywordId);
			
			actual = WxKeywordDao.getByCond(mStaff, new WxKeywordDao.ExtraCond().setId(keywordId)).get(0);
			expected.setType(actual.getType());
			compare(expected, actual);
			
		}finally{
			if(keywordId != 0){
				WxKeywordDao.deleteById(mStaff, keywordId);
				Assert.assertTrue("failed to delete wx keyword", WxKeywordDao.getByCond(mStaff, new WxKeywordDao.ExtraCond().setId(keywordId)).isEmpty());
			}
		}
	}
	
	private void compare(WxKeyword expected, WxKeyword actual){
		Assert.assertEquals(expected.getId(), actual.getId());
		Assert.assertEquals(mStaff.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals(expected.getKeyword(), actual.getKeyword());
		Assert.assertEquals(expected.getType(), actual.getType());
		Assert.assertEquals(expected.getActionId(), actual.getActionId());
	}
}
