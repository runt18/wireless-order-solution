package com.wireless.test.db.member;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.member.MemberDao;
import com.wireless.db.member.TakeoutAddressDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.TakeoutAddress;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestTakeoutAddress {
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getAdminByRestaurant(40);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testTakeoutAddress() throws SQLException, BusinessException{
		int addressId = 0;
		Member member = MemberDao.getByCond(mStaff, null, null).get(0);
		try{
			TakeoutAddress.InsertBuilder builder = new TakeoutAddress.InsertBuilder(member, "测试外卖地址", "18520590932");
			addressId = TakeoutAddressDao.insert(mStaff, builder);
			
			TakeoutAddress expected = builder.build();
			TakeoutAddress actual = TakeoutAddressDao.getById(mStaff, addressId);
			
			Assert.assertEquals(expected.getMemberId(), actual.getMemberId());
			Assert.assertEquals(expected.getAddress(), actual.getAddress());
			Assert.assertEquals(expected.getTele(), actual.getTele());
		}finally{
			if(addressId != 0){
				TakeoutAddressDao.deleteById(mStaff, addressId);
				try{
					TakeoutAddressDao.getById(mStaff, addressId);
					Assert.assertTrue("failed to delete the take-out address", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the take-out address", MemberError.TAKE_OUT_ADDRESS_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
}
