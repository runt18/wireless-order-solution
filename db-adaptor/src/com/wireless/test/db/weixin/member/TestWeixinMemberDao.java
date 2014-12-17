package com.wireless.test.db.weixin.member;

import java.beans.PropertyVetoException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.member.WxMember;
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

		String weixinCard = "";
		try{
			//--------Test to interest a weixin member---------------
			int memberId = WxMemberDao.interest(mStaff, WEIXIN_MEMBER_SERIAL);
			
			Member actual = MemberDao.getById(mStaff, memberId);
			weixinCard = actual.getWeixin().getCard()+"";
			
			Assert.assertEquals("member id to weixin", memberId, actual.getId());
			Assert.assertEquals("member name to weixin", "微信会员", actual.getName());
			MemberType expectedType = MemberTypeDao.getWxMemberType(mStaff);
			Assert.assertEquals("member type id to weixin", expectedType.getId(), actual.getMemberType().getId());
			Assert.assertEquals("member type to weixin", expectedType.getType(), actual.getMemberType().getType());
			Assert.assertEquals("member type to weixin", expectedType.getName(), actual.getMemberType().getName());
			Assert.assertEquals("serial to weixin member", WEIXIN_MEMBER_SERIAL, actual.getWeixin().getSerial());
			Assert.assertEquals("member id to getByWxSerial", memberId, MemberDao.getByWxSerial(mStaff, WEIXIN_MEMBER_SERIAL).getId());
			Assert.assertEquals("member id to getByWxCard", memberId, MemberDao.getByWxCard(mStaff, weixinCard).getId());
			
			//----------Test to bind a weixin member----------------
			WxMemberDao.bind(mStaff, new WxMember.BindBuilder(WEIXIN_MEMBER_SERIAL, "18520590931"));
			Member expected = MemberDao.getByMobile(mStaff, "18520590931");
			actual = MemberDao.getByWxSerial(mStaff, WEIXIN_MEMBER_SERIAL);
			Assert.assertEquals("member id after bound", expected.getId(), actual.getId());
			Assert.assertEquals("card to weixin member", weixinCard, actual.getWeixin().getCard());
			
		}finally{
			if(weixinCard != null){
				
				MemberDao.deleteByCond(mStaff, new MemberDao.ExtraCond().setWeixinCard(weixinCard));
				
				try{
					MemberDao.getByWxCard(mStaff, weixinCard);
					Assert.assertTrue("failed to delete the member associated with weixin", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the member associated with weixin", MemberError.MEMBER_NOT_EXIST, e.getErrCode());
				}
				
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
