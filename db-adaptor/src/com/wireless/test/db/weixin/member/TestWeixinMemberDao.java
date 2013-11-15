package com.wireless.test.db.weixin.member;

import java.beans.PropertyVetoException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.CalcWeixinSignature;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.restaurantMgr.Restaurant;
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
			if(!memberList.isEmpty()){
				WeixinMemberDao.bindExistMember(memberList.get(0).getId(), WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL);
				Assert.assertEquals("bind exist member", memberList.get(0).getId(), WeixinMemberDao.getBoundMemberIdByWeixin(WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL));
			}else{
				//WeixinMemberDao.bindNewMember(builder, WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL);
			}
			
			
		}finally{
			WeixinMemberDao.cancel(WEIXIN_MEMBER_SERIAL, WEIXIN_RESTAURANT_SERIAL);
//			try{
//				WeixinMemberDao.getRestaurantIdByWeixin(WEIXIN_MEMBER_SERIAL);
//			}catch(BusinessException e){
//				Assert.assertEquals("failed to cancel weixin member", WeixinMemberError.WEIXIN_MEMBER_NOT_INTEREST, e.getErrCode());
//			}
		}
	}
}
