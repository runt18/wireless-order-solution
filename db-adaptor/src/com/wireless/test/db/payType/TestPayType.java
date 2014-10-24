package com.wireless.test.db.payType;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.orderMgr.PayTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PayTypeError;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestPayType {
	private static Staff mStaff;
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getAdminByRestaurant(37);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPayType() throws SQLException, BusinessException{
		int payTypeId = 0;
		try{
			//-------------Test to insert a pay type--------------
			PayType.InsertBuilder insertBuilder = new PayType.InsertBuilder("测试付款方式");
			payTypeId = PayTypeDao.insert(mStaff, insertBuilder);
			
			PayType expected = insertBuilder.build();
			expected.setId(payTypeId);
			PayType actual = PayTypeDao.getById(mStaff, payTypeId);
			
			compare(expected, actual);
			
			//-------------Test to update the pay type--------------
			PayType.UpdateBuilder updateBuilder = new PayType.UpdateBuilder(payTypeId).setName("修改付款方式");
			PayTypeDao.update(mStaff, updateBuilder);
			
			expected = updateBuilder.build();
			
			actual = PayTypeDao.getById(mStaff, payTypeId);
			
			compare(expected, actual);
			
		}finally{
			if(payTypeId != 0){
				PayTypeDao.deleteById(mStaff, payTypeId);
				try{
					PayTypeDao.getById(mStaff, payTypeId);
					Assert.assertTrue("failed to delete the pay type", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the pay type", PayTypeError.PAY_TYPE_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compare(PayType expected, PayType actual){
		Assert.assertEquals("pay type id", expected.getId(), actual.getId());
		Assert.assertEquals("pay type name", expected.getName(), actual.getName());
		Assert.assertEquals("pay type", PayType.Type.EXTRA, actual.getType());
	}
}
