package com.wireless.test.db.weixin.member;

import java.beans.PropertyVetoException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.client.WeixinMember;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestWeixinMemberDao {
	
	private static final String WEIXIN_MEMBER_SERIAL = "oACWTjsRKuGYTjEpEyG7fPTg06fd";
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException{
		TestInit.init();
		mStaff = StaffDao.getAdminByRestaurant(40);
	}
	
	@Test
	public void testWeixinMemberDao() throws SQLException, BusinessException, NoSuchAlgorithmException{

		int weixinCard = 0;
		try{
			//--------Test to interest a weixin member---------------
			int memberId = WeixinMemberDao.interest(mStaff, WEIXIN_MEMBER_SERIAL);
			
			Member actual = MemberDao.getById(mStaff, memberId);
			weixinCard = actual.getWeixin().getCard();
			
			Assert.assertEquals("member id to weixin", memberId, actual.getId());
			Assert.assertEquals("member name to weixin", "微信会员", actual.getName());
			MemberType expectedType = MemberTypeDao.getWeixinMemberType(mStaff);
			Assert.assertEquals("member type id to weixin", expectedType.getId(), actual.getMemberType().getId());
			Assert.assertEquals("member type to weixin", expectedType.getType(), actual.getMemberType().getType());
			Assert.assertEquals("member type to weixin", expectedType.getName(), actual.getMemberType().getName());
			Assert.assertEquals("serial to weixin member", WEIXIN_MEMBER_SERIAL, actual.getWeixin().getSerial());
			Assert.assertEquals("card to weixin member", MemberDao.getByCond(mStaff, new MemberDao.ExtraCond().setWeixinCard(actual.getWeixin().getCard()), null).get(0).getId(), actual.getId());
			
			//----------Test to bind a weixin member----------------
			WeixinMemberDao.bind(mStaff, new WeixinMember.BindBuilder(WEIXIN_MEMBER_SERIAL, "18520590932"));
			Member expected = MemberDao.getByMobile(mStaff, "18520590932");
			actual = MemberDao.getById(mStaff, MemberDao.getByCond(mStaff, new MemberDao.ExtraCond().setWeixinCard(weixinCard), null).get(0).getId());
			Assert.assertEquals("member id after bound", expected.getId(), actual.getId());
			Assert.assertEquals("serial to weixin member", WEIXIN_MEMBER_SERIAL, actual.getWeixin().getSerial());
			
		}finally{
			if(weixinCard != 0){
				
				MemberDao.deleteByCond(mStaff, new MemberDao.ExtraCond().setWeixinCard(weixinCard));
				
				Assert.assertTrue("failed to delete member associated with weixin", MemberDao.getByCond(mStaff, new MemberDao.ExtraCond().setWeixinCard(weixinCard), null).isEmpty());
				
//				try{
//					WeixinMemberDao.getBySerial(mStaff, WEIXIN_MEMBER_SERIAL);
//					Assert.assertTrue("failed to delete the weixin member", false);
//				}catch(BusinessException e){
//					Assert.assertEquals("failed to delete the weixin member", WeixinMemberError.WEIXIN_INFO_NOT_EXIST, e.getErrCode());
//				}
			}
		}
	}
}
