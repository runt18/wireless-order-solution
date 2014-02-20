package com.wireless.test.db.coupon;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.coupon.CouponDao;
import com.wireless.db.coupon.CouponTypeDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.coupon.Coupon;
import com.wireless.pojo.coupon.CouponType;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestCouponDao {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getStaffs(63).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void tesCouponDao() throws SQLException, BusinessException{
		int couponTypeId = 0;
		try{
			//Insert a new coupon type.
			CouponType.InsertBuilder insertBuilder = new CouponType.InsertBuilder("测试优惠券类型", 30).setComment("测试备注");//.setExpired(System.currentTimeMillis() / 1000 * 1000);
			couponTypeId = CouponTypeDao.insert(mStaff, insertBuilder);
			
			CouponType expectedCouponType = insertBuilder.build();
			CouponType actualCouponType = CouponTypeDao.getById(mStaff, couponTypeId);
			
			Assert.assertEquals("id : insert coupon type", couponTypeId, actualCouponType.getId());
			Assert.assertEquals("name : insert coupon type", expectedCouponType.getName(), actualCouponType.getName());
			Assert.assertEquals("price : insert coupon type", expectedCouponType.getPrice(), actualCouponType.getPrice(), 0.01);
			Assert.assertEquals("restaurant : insert coupon type", mStaff.getRestaurantId(), actualCouponType.getRestaurantId());
			Assert.assertEquals("expired : insert coupon type", expectedCouponType.getExpired(), actualCouponType.getExpired());
			Assert.assertEquals("comment : insert coupon type", expectedCouponType.getComment(), actualCouponType.getComment());
			
			//Insert a new coupon and assign to member1 & member2.
			List<Member> members = MemberDao.getMember(mStaff, null, null);
			Member m1 = members.get(0);
			Member m2 = members.get(1);
			CouponDao.insertAll(mStaff, new Coupon.InsertAllBuilder(couponTypeId)
			  									  .addMemberId(m1.getId())
			  									  .addMemberId(m2.getId()));
			
			Assert.assertEquals("coupon amount by type after inserting two ones", 2, CouponDao.getByType(mStaff, couponTypeId).size());
			
			Assert.assertEquals("coupon amount by member after inserting one to member 1", 1, CouponDao.getByMember(mStaff, m1.getId()).size());
			Coupon actualCoupon1 = CouponDao.getByMember(mStaff, m1.getId()).get(0);
			Assert.assertEquals("coupon type : insert a coupon to member 1", actualCouponType.getId(), actualCoupon1.getCouponType().getId());
			Assert.assertEquals("coupon price : insert a coupon to member 1", actualCouponType.getPrice(), actualCoupon1.getPrice(), 0.01);
			Assert.assertEquals("coupon name : insert a coupon to member 1", actualCouponType.getName(), actualCoupon1.getName());
			Assert.assertEquals("restaurant : insert a coupon to member 1", mStaff.getRestaurantId(), actualCoupon1.getRestaurantId());
			Assert.assertEquals("member id : insert a coupon to member 1", m1.getId(), actualCoupon1.getMember().getId());
			Assert.assertEquals("member name : insert a coupon to member 1", m1.getName(), actualCoupon1.getMember().getName());
			Assert.assertEquals("member mobile : insert a coupon to member 1", m1.getMobile(), actualCoupon1.getMember().getMobile());
			Assert.assertEquals("member card : insert a coupon to member 1", m1.getMemberCard(), actualCoupon1.getMember().getMemberCard());
			Assert.assertEquals("status : insert a coupon to member 1", Coupon.Status.CREATED, actualCoupon1.getStatus());
			Assert.assertTrue("birth date : insert a coupon to member 1", System.currentTimeMillis() - actualCoupon1.getBirthDate() < 5000);
			Assert.assertEquals("create staff : insert a coupon to member 1", mStaff.getName(), actualCoupon1.getCreateStaff());
			
			Assert.assertEquals("coupon amount by member after inserting one to member 2", 1, CouponDao.getByMember(mStaff, m2.getId()).size());
			Coupon actualCoupon2 = CouponDao.getByMember(mStaff, m2.getId()).get(0);
			Assert.assertEquals("coupon type : insert a coupon to member 2", couponTypeId, actualCoupon2.getCouponType().getId());
			Assert.assertEquals("coupon price : insert a coupon to member 2", actualCouponType.getPrice(), actualCoupon2.getPrice(), 0.01);
			Assert.assertEquals("coupon name : insert a coupon to member 2", actualCouponType.getName(), actualCoupon2.getName());
			Assert.assertEquals("restaurant : insert a coupon to member 2", mStaff.getRestaurantId(), actualCoupon2.getRestaurantId());
			Assert.assertEquals("member id : insert a coupon to member 2", m2.getId(), actualCoupon2.getMember().getId());
			Assert.assertEquals("member name : insert a coupon to member 2", m2.getName(), actualCoupon2.getMember().getName());
			Assert.assertEquals("member mobile : insert a coupon to member 2", m2.getMobile(), actualCoupon2.getMember().getMobile());
			Assert.assertEquals("member card : insert a coupon to member 2", m2.getMemberCard(), actualCoupon2.getMember().getMemberCard());
			Assert.assertEquals("status : insert a coupon to member 2", Coupon.Status.CREATED, actualCoupon2.getStatus());
			Assert.assertTrue("birth date : insert a coupon to member 2", System.currentTimeMillis() - actualCoupon2.getBirthDate() < 5000);
			Assert.assertEquals("create staff : insert a coupon to member 2", mStaff.getName(), actualCoupon2.getCreateStaff());

			//Insert another coupon to member 1.
			CouponDao.insertAll(mStaff, new Coupon.InsertAllBuilder(couponTypeId)
												  .addMemberId(m1.getId()));
			
			Assert.assertEquals("coupon amount by type after inserting one to member 1", 3, CouponDao.getByType(mStaff, couponTypeId).size());

			Assert.assertEquals("coupon amount by member after inserting one to member 1", 2, CouponDao.getByMember(mStaff, m1.getId()).size());
			actualCoupon1 = CouponDao.getByMember(mStaff, m1.getId()).get(1);
			Assert.assertEquals("coupon type : insert another coupon to member 1", couponTypeId, actualCoupon1.getCouponType().getId());
			Assert.assertEquals("coupon price : insert another coupon to member 1", actualCouponType.getPrice(), actualCoupon1.getPrice(), 0.01);
			Assert.assertEquals("coupon name : insert another coupon to member 1", actualCouponType.getName(), actualCoupon1.getName());
			Assert.assertEquals("restaurant : insert another coupon to member 1", mStaff.getRestaurantId(), actualCoupon1.getRestaurantId());
			Assert.assertEquals("member id : insert another coupon to member 1", m1.getId(), actualCoupon1.getMember().getId());
			Assert.assertEquals("member name : insert another coupon to member 1", m1.getName(), actualCoupon1.getMember().getName());
			Assert.assertEquals("member mobile : insert another coupon to member 1", m1.getMobile(), actualCoupon1.getMember().getMobile());
			Assert.assertEquals("member card : insert another coupon to member 1", m1.getMemberCard(), actualCoupon1.getMember().getMemberCard());
			Assert.assertEquals("status : insert another coupon to member 1", Coupon.Status.CREATED, actualCoupon1.getStatus());
			Assert.assertTrue("birth date : insert another coupon to member 1", System.currentTimeMillis() - actualCoupon1.getBirthDate() < 5000);
			Assert.assertEquals("create staff : insert another coupon to member 1", mStaff.getName(), actualCoupon1.getCreateStaff());

			//Update the coupon type.
			CouponType.UpdateBuilder updateBuilder = new CouponType.UpdateBuilder(couponTypeId, "修改优惠券类型").setExpired(System.currentTimeMillis() / 1000 * 1000)
																   .setComment("修改备注");
			CouponTypeDao.update(mStaff, updateBuilder);
			expectedCouponType = updateBuilder.build();
			actualCouponType = CouponTypeDao.getById(mStaff, couponTypeId);
			Assert.assertEquals("id : update coupon type", couponTypeId, actualCouponType.getId());
			Assert.assertEquals("name : update coupon type", expectedCouponType.getName(), actualCouponType.getName());
			Assert.assertEquals("restaurant : update coupon type", mStaff.getRestaurantId(), actualCouponType.getRestaurantId());
			Assert.assertEquals("expired : update coupon type", expectedCouponType.getExpired(), actualCouponType.getExpired());
			Assert.assertEquals("comment : update coupon type", expectedCouponType.getComment(), actualCouponType.getComment());
			
			
		}finally{
			if(couponTypeId != 0){
				CouponTypeDao.delete(mStaff, couponTypeId);
				try{
					CouponTypeDao.getById(mStaff, couponTypeId);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete coupon type", e.getErrCode(), MemberError.COUPON_TYPE_NOT_EXIST);
				}
				Assert.assertEquals("failed to delete the associated coupon", 0, CouponDao.getByType(mStaff, couponTypeId).size());
			}
		}
	}
	
}
