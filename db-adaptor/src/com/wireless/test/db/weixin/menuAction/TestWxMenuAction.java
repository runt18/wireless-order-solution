package com.wireless.test.db.weixin.menuAction;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.menuAction.WxMenuAction;
import com.wireless.db.weixin.menuAction.WxMenuActionDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxMenuError;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestWxMenuAction {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException{
		TestInit.init();
		mStaff = StaffDao.getAdminByRestaurant(40);
	}
	
	@Test
	public void testWxMenuAction() throws SQLException, BusinessException{
		int actionId = 0;
		try{
			WxMenuAction.InsertBuilder4Text insert4Text = new WxMenuAction.InsertBuilder4Text("test");
			actionId = WxMenuActionDao.insert(mStaff, insert4Text);
			
			WxMenuAction expected = insert4Text.build();
			expected.setId(actionId);
			WxMenuAction actual = WxMenuActionDao.getById(mStaff, actionId);
			
			compare(expected, actual);
			
			WxMenuAction.UpdateBuilder4Text update4Text = new WxMenuAction.UpdateBuilder4Text(actionId, "测试修改");
			WxMenuActionDao.update(mStaff, update4Text);
			
			expected = update4Text.build();
			actual = WxMenuActionDao.getById(mStaff, actionId);
			compare(expected, actual);
			
		}finally{
			if(actionId != 0){
				WxMenuActionDao.deleteById(mStaff, actionId);
				try{
					WxMenuActionDao.getById(mStaff, actionId);
					Assert.assertTrue("failed to delete the weixin menu action", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the weixin menu action", WxMenuError.WEIXIN_MENU_ACTION_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compare(WxMenuAction expected, WxMenuAction actual){
		Assert.assertEquals("wx menu action id", expected.getId(), actual.getId());
		Assert.assertEquals("wx menu action msg type", expected.getMsgType().getType(), actual.getMsgType().getType());
		Assert.assertEquals("wx menu action", expected.getAction(), actual.getAction());
	}
}
