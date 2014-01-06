package com.wireless.test.db.weixin.member;

import java.beans.PropertyVetoException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.CalcWeixinSignature;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WeixinMemberError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestWeixinMemberDao {
	
	private static final String WEIXIN_RESTAURANT_SERIAL = "oACWTjsRKuGYTjEpEyG7fPTg06fc";
	private static final String WEIXIN_MEMBER_SERIAL = "oACWTjsRKuGYTjEpEyG7fPTg06fd";
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testWeixinMemberDao() throws SQLException, BusinessException, NoSuchAlgorithmException{
		final String account = "blt";
		final String timestamp = "2013-9-11 7:48:00";
		final String nonce = "jingyang";
		WeixinRestaurantDao.verify(account, CalcWeixinSignature.calc(RestaurantDao.getByAccount(account).getAccount(), timestamp, nonce), timestamp, nonce);
		WeixinRestaurantDao.bind(WEIXIN_RESTAURANT_SERIAL, account);
		
		Restaurant restaurant = RestaurantDao.getByAccount(account);
		try{
			WeixinMemberDao.interest(WEIXIN_RESTAURANT_SERIAL, WEIXIN_MEMBER_SERIAL);
			List<Member> memberList = MemberDao.getMember(StaffDao.getStaffs(restaurant.getId()).get(0), null, null);
			Staff staff = StaffDao.getStaffs(restaurant.getId()).get(0);
			
			if(!memberList.isEmpty()){
				//Test to bind the exist member
				WeixinMemberDao.bindExistMember(memberList.get(0).getId(), WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL);
				Assert.assertEquals("bind exist member", memberList.get(0).getId(), WeixinMemberDao.getBoundMemberIdByWeixin(WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL));
				
				String mobileToUpdate = Long.valueOf((Long.parseLong(memberList.get(0).getMobile()) + 1)).toString();
				//Test to change the bound mobile of exist member 
				WeixinMemberDao.updateMobile(mobileToUpdate, WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL);
				Assert.assertEquals("change the mobile to exist member", mobileToUpdate, MemberDao.getMemberById(staff, memberList.get(0).getId()).getMobile());
				
			}
			
			int memberId = 0;
			try{
				//Test to bind a new member
				Member.InsertBuilder builder = new Member.InsertBuilder(restaurant.getId(), "张菁洋", "18520590932", 
																		MemberTypeDao.getWeixinMemberType(staff).getId(), 
																		Member.Sex.FEMALE);
				memberId = WeixinMemberDao.bindNewMember(builder, WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL);
				
				Member memberJustInserted = MemberDao.getMemberById(staff, WeixinMemberDao.getBoundMemberIdByWeixin(WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL)); 
				
				Assert.assertEquals("id to new member just bound", memberId, memberJustInserted.getId());
				Assert.assertEquals("name to new member just bound", "张菁洋", memberJustInserted.getName());
				Assert.assertEquals("mobile to new member just bound", "18520590932", memberJustInserted.getMobile());
				Assert.assertEquals("type to new member just bound", MemberType.Type.WEIXIN, memberJustInserted.getMemberType().getType());
				Assert.assertEquals("sex to new member just bound", Member.Sex.FEMALE, memberJustInserted.getSex());
				
			}finally{
				if(memberId != 0){
					MemberDao.deleteById(staff, memberId);
				}
			}
			
		}finally{
			WeixinMemberDao.cancel(WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL);
			try{
				WeixinMemberDao.getBoundMemberIdByWeixin(WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL);
			}catch(BusinessException e){
				Assert.assertEquals("failed to cancel weixin member", WeixinMemberError.WEIXIN_MEMBER_NOT_BOUND, e.getErrCode());
			}
		}
	}
}
