package com.wireless.test.db.promotion;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.CouponTypeDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PromotionError;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestPromotionDao {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getByRestaurant(63).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test 
	public void testCreatePromotion() throws SQLException, BusinessException, ParseException{
		int promotionId = 0;
		int couponTypeId = 0;
		try{
			List<Member> members = MemberDao.getByCond(mStaff, null, null);
			Member m1 = members.get(0);
			Member m2 = members.get(1);
			
			//--------Test to create a promotion-----------
			CouponType.InsertBuilder typeInsertBuilder = new CouponType.InsertBuilder("测试优惠券类型", 30).setComment("测试备注").setImage("2912w3slka.jpg");//.setExpired(System.currentTimeMillis() / 1000 * 1000);
			Promotion.CreateBuilder promotionCreateBuilder = new Promotion.CreateBuilder("测试优惠活动", new DateRange("2015-1-1", "2015-2-1"), "hello world<br>", typeInsertBuilder)
																		  .addMember(m1.getId()).addMember(m2.getId());
			promotionId = PromotionDao.create(mStaff, promotionCreateBuilder);
			
			Promotion expected = promotionCreateBuilder.build();
			expected.setId(promotionId);
			expected.setCouponType(typeInsertBuilder.build());
			
			Promotion actual = PromotionDao.getById(mStaff, promotionId);
			couponTypeId = actual.getCouponType().getId();
			
			//Compare the promotion.
			compare(expected, actual);
			//Compare the coupon related to this promotion.
			compare(promotionId, Coupon.Status.CREATED, couponTypeId, m1, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m1).setPromotion(promotionId), null).get(0));
			compare(promotionId, Coupon.Status.CREATED, couponTypeId, m2, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m2).setPromotion(promotionId), null).get(0));
			
			//--------Test to cancel publish a promotion whose status is NOT 'CREATE'-----------
			try{
				PromotionDao.cancelPublish(mStaff, promotionId);
			}catch(BusinessException e){
				Assert.assertEquals("failed to cancel publish the promotion", PromotionError.PROMOTION_PUBLISH_NOT_ALLOW, e.getErrCode());
			}
			
			//--------Test to publish a promotion-----------
			PromotionDao.publish(mStaff, promotionId);
			expected.setStatus(Promotion.Status.PUBLISH);
			//Compare the promotion.
			compare(expected, PromotionDao.getById(mStaff, promotionId));
			//Compare the coupon related to this promotion.
			compare(promotionId, Coupon.Status.PUBLISHED, couponTypeId, m1, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m1).setPromotion(promotionId), null).get(0));
			compare(promotionId, Coupon.Status.PUBLISHED, couponTypeId, m2, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m2).setPromotion(promotionId), null).get(0));

			//--------Test to cancel publish the promotion-----------
			PromotionDao.cancelPublish(mStaff, promotionId);
			expected.setStatus(Promotion.Status.CREATED);
			//Compare the promotion.
			compare(expected, actual);
			//Compare the coupon related to this promotion.
			compare(promotionId, Coupon.Status.CREATED, couponTypeId, m1, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m1).setPromotion(promotionId), null).get(0));
			compare(promotionId, Coupon.Status.CREATED, couponTypeId, m2, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m2).setPromotion(promotionId), null).get(0));

		}finally{
			if(promotionId != 0){
				PromotionDao.delete(mStaff, promotionId);
				try{
					PromotionDao.getById(mStaff, promotionId);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the promotion", PromotionError.PROMOTION_NOT_EXIST, e.getErrCode());
					Assert.assertTrue("failed to delete the associated coupon", CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setPromotion(promotionId), null).isEmpty());
					try{
						CouponTypeDao.getById(mStaff, couponTypeId);
					}catch(BusinessException e2){
						Assert.assertEquals("failed to delete the promotion", PromotionError.COUPON_TYPE_NOT_EXIST, e2.getErrCode());
					}
				}
			}
		}
	}
	
	private void compare(Promotion expected, Promotion actual){
		//The content to promotion
		Assert.assertEquals("promotion id", expected.getId(), actual.getId());
		Assert.assertEquals("promotion title", expected.getTitle(), actual.getTitle());
		Assert.assertEquals("promotion restaurant id", mStaff.getRestaurantId(), actual.getRestaurantId());
		Assert.assertTrue("promotion create date", Math.abs(expected.getCreateDate() - actual.getCreateDate()) < 24 * 3600 * 1000);
		Assert.assertEquals("promotion body", expected.getBody(), actual.getBody());
		Assert.assertEquals("promotion date range", expected.getDateRange(), actual.getDateRange());
		Assert.assertEquals("promotion status", expected.getStatus(), actual.getStatus());
		Assert.assertEquals("promotion type", expected.getType(), actual.getType());
		
		//The content to associated coupon type
		//Assert.assertEquals("id : insert coupon type", expected.getCouponType().getId(), actual.getCouponType().getId());
		Assert.assertEquals("name : insert coupon type", expected.getCouponType().getName(), actual.getCouponType().getName());
		Assert.assertEquals("price : insert coupon type", expected.getCouponType().getPrice(), actual.getCouponType().getPrice(), 0.01);
		Assert.assertEquals("restaurant : insert coupon type", mStaff.getRestaurantId(), actual.getCouponType().getRestaurantId());
		Assert.assertEquals("expired : insert coupon type", expected.getCouponType().getExpired(), actual.getCouponType().getExpired());
		Assert.assertEquals("comment : insert coupon type", expected.getCouponType().getComment(), actual.getCouponType().getComment());
		Assert.assertEquals("image : insert image", expected.getCouponType().getImage(), actual.getCouponType().getImage());
	}
	
	private void compare(int expectedPromotion, Coupon.Status expectedStatus, int expectedType, Member expectedMember, Coupon actual){
		Assert.assertEquals("coupon promotion id", expectedPromotion, actual.getPromotion().getId());
		Assert.assertEquals("coupon type", expectedType, actual.getCouponType().getId());
		Assert.assertEquals("coupon restaurant id", mStaff.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("coupon member id", expectedMember.getId(), actual.getMember().getId());
		Assert.assertEquals("coupon status", expectedStatus, actual.getStatus());
		Assert.assertTrue("coupon birth date", System.currentTimeMillis() - actual.getBirthDate() < 100000);
	}
	
//	@Test
//	public void tesCouponDao() throws SQLException, BusinessException{
//		int couponTypeId = 0;
//		try{
//			//Insert a new coupon type.
//			CouponType.InsertBuilder insertBuilder = new CouponType.InsertBuilder("测试优惠券类型", 30).setComment("测试备注").setImage("2912w3slka.jpg");//.setExpired(System.currentTimeMillis() / 1000 * 1000);
//			couponTypeId = CouponTypeDao.insert(mStaff, insertBuilder);
//			
//			CouponType expectedCouponType = insertBuilder.build();
//			CouponType actualCouponType = CouponTypeDao.getById(mStaff, couponTypeId);
//			
//			Assert.assertEquals("id : insert coupon type", couponTypeId, actualCouponType.getId());
//			Assert.assertEquals("name : insert coupon type", expectedCouponType.getName(), actualCouponType.getName());
//			Assert.assertEquals("price : insert coupon type", expectedCouponType.getPrice(), actualCouponType.getPrice(), 0.01);
//			Assert.assertEquals("restaurant : insert coupon type", mStaff.getRestaurantId(), actualCouponType.getRestaurantId());
//			Assert.assertEquals("expired : insert coupon type", expectedCouponType.getExpired(), actualCouponType.getExpired());
//			Assert.assertEquals("comment : insert coupon type", expectedCouponType.getComment(), actualCouponType.getComment());
//			Assert.assertEquals("image : insert image", expectedCouponType.getImage(), actualCouponType.getImage());
//			
//			//Insert a new coupon and assign to member1 & member2.
//			List<Member> members = MemberDao.getByCond(mStaff, null, null);
//			Member m1 = members.get(0);
//			Member m2 = members.get(1);
//			CouponDao.create(mStaff, new Coupon.CreateBuilder(couponTypeId)
//			  									  .addMemberId(m1.getId())
//			  									  .addMemberId(m2.getId()));
//			
//			Assert.assertEquals("coupon amount by type after inserting two ones", 2, CouponDao.getByType(mStaff, couponTypeId).size());
//			
//			Assert.assertEquals("coupon amount by member after inserting one to member 1", 1, CouponDao.getByMember(mStaff, m1.getId()).size());
//			Coupon actualCoupon1 = CouponDao.getByMember(mStaff, m1.getId()).get(0);
//			Assert.assertEquals("coupon type : insert a coupon to member 1", actualCouponType.getId(), actualCoupon1.getCouponType().getId());
//			Assert.assertEquals("coupon price : insert a coupon to member 1", actualCouponType.getPrice(), actualCoupon1.getPrice(), 0.01);
//			Assert.assertEquals("coupon name : insert a coupon to member 1", actualCouponType.getName(), actualCoupon1.getName());
//			Assert.assertEquals("restaurant : insert a coupon to member 1", mStaff.getRestaurantId(), actualCoupon1.getRestaurantId());
//			Assert.assertEquals("member id : insert a coupon to member 1", m1.getId(), actualCoupon1.getMember().getId());
//			Assert.assertEquals("member name : insert a coupon to member 1", m1.getName(), actualCoupon1.getMember().getName());
//			Assert.assertEquals("member mobile : insert a coupon to member 1", m1.getMobile(), actualCoupon1.getMember().getMobile());
//			Assert.assertEquals("member card : insert a coupon to member 1", m1.getMemberCard(), actualCoupon1.getMember().getMemberCard());
//			Assert.assertEquals("status : insert a coupon to member 1", Coupon.Status.CREATED, actualCoupon1.getStatus());
//			Assert.assertTrue("birth date : insert a coupon to member 1", System.currentTimeMillis() - actualCoupon1.getBirthDate() < 5000);
//			Assert.assertEquals("create staff : insert a coupon to member 1", mStaff.getName(), actualCoupon1.getCreateStaff());
//			
//			Assert.assertEquals("coupon amount by member after inserting one to member 2", 1, CouponDao.getByMember(mStaff, m2.getId()).size());
//			Coupon actualCoupon2 = CouponDao.getByMember(mStaff, m2.getId()).get(0);
//			Assert.assertEquals("coupon type : insert a coupon to member 2", couponTypeId, actualCoupon2.getCouponType().getId());
//			Assert.assertEquals("coupon price : insert a coupon to member 2", actualCouponType.getPrice(), actualCoupon2.getPrice(), 0.01);
//			Assert.assertEquals("coupon name : insert a coupon to member 2", actualCouponType.getName(), actualCoupon2.getName());
//			Assert.assertEquals("restaurant : insert a coupon to member 2", mStaff.getRestaurantId(), actualCoupon2.getRestaurantId());
//			Assert.assertEquals("member id : insert a coupon to member 2", m2.getId(), actualCoupon2.getMember().getId());
//			Assert.assertEquals("member name : insert a coupon to member 2", m2.getName(), actualCoupon2.getMember().getName());
//			Assert.assertEquals("member mobile : insert a coupon to member 2", m2.getMobile(), actualCoupon2.getMember().getMobile());
//			Assert.assertEquals("member card : insert a coupon to member 2", m2.getMemberCard(), actualCoupon2.getMember().getMemberCard());
//			Assert.assertEquals("status : insert a coupon to member 2", Coupon.Status.CREATED, actualCoupon2.getStatus());
//			Assert.assertTrue("birth date : insert a coupon to member 2", System.currentTimeMillis() - actualCoupon2.getBirthDate() < 5000);
//			Assert.assertEquals("create staff : insert a coupon to member 2", mStaff.getName(), actualCoupon2.getCreateStaff());
//
//			//Insert another coupon to member 1.
//			CouponDao.create(mStaff, new Coupon.CreateBuilder(couponTypeId)
//												  .addMemberId(m1.getId()));
//			
//			Assert.assertEquals("coupon amount by type after inserting one to member 1", 3, CouponDao.getByType(mStaff, couponTypeId).size());
//
//			Assert.assertEquals("coupon amount by member after inserting one to member 1", 2, CouponDao.getByMember(mStaff, m1.getId()).size());
//			actualCoupon1 = CouponDao.getByMember(mStaff, m1.getId()).get(1);
//			Assert.assertEquals("coupon type : insert another coupon to member 1", couponTypeId, actualCoupon1.getCouponType().getId());
//			Assert.assertEquals("coupon price : insert another coupon to member 1", actualCouponType.getPrice(), actualCoupon1.getPrice(), 0.01);
//			Assert.assertEquals("coupon name : insert another coupon to member 1", actualCouponType.getName(), actualCoupon1.getName());
//			Assert.assertEquals("restaurant : insert another coupon to member 1", mStaff.getRestaurantId(), actualCoupon1.getRestaurantId());
//			Assert.assertEquals("member id : insert another coupon to member 1", m1.getId(), actualCoupon1.getMember().getId());
//			Assert.assertEquals("member name : insert another coupon to member 1", m1.getName(), actualCoupon1.getMember().getName());
//			Assert.assertEquals("member mobile : insert another coupon to member 1", m1.getMobile(), actualCoupon1.getMember().getMobile());
//			Assert.assertEquals("member card : insert another coupon to member 1", m1.getMemberCard(), actualCoupon1.getMember().getMemberCard());
//			Assert.assertEquals("status : insert another coupon to member 1", Coupon.Status.CREATED, actualCoupon1.getStatus());
//			Assert.assertTrue("birth date : insert another coupon to member 1", System.currentTimeMillis() - actualCoupon1.getBirthDate() < 5000);
//			Assert.assertEquals("create staff : insert another coupon to member 1", mStaff.getName(), actualCoupon1.getCreateStaff());
//
//			//Update the coupon type.
//			CouponType.UpdateBuilder updateBuilder = new CouponType.UpdateBuilder(couponTypeId, "修改优惠券类型").setExpired(System.currentTimeMillis() / 1000 * 1000)
//																   .setComment("修改备注").setImage("1235slkj.jpg");
//			CouponTypeDao.update(mStaff, updateBuilder);
//			expectedCouponType = updateBuilder.build();
//			actualCouponType = CouponTypeDao.getById(mStaff, couponTypeId);
//			Assert.assertEquals("id : update coupon type", couponTypeId, actualCouponType.getId());
//			Assert.assertEquals("name : update coupon type", expectedCouponType.getName(), actualCouponType.getName());
//			Assert.assertEquals("restaurant : update coupon type", mStaff.getRestaurantId(), actualCouponType.getRestaurantId());
//			Assert.assertEquals("expired : update coupon type", expectedCouponType.getExpired(), actualCouponType.getExpired());
//			Assert.assertEquals("comment : update coupon type", expectedCouponType.getComment(), actualCouponType.getComment());
//			Assert.assertEquals("image : insert image", expectedCouponType.getImage(), actualCouponType.getImage());
//			
//		}finally{
//			if(couponTypeId != 0){
//				CouponTypeDao.delete(mStaff, couponTypeId);
//				try{
//					CouponTypeDao.getById(mStaff, couponTypeId);
//				}catch(BusinessException e){
//					Assert.assertEquals("failed to delete coupon type", e.getErrCode(), PromotionError.COUPON_TYPE_NOT_EXIST);
//				}
//				Assert.assertEquals("failed to delete the associated coupon", 0, CouponDao.getByType(mStaff, couponTypeId).size());
//			}
//		}
//	}
	
}
