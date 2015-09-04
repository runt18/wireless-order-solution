package com.wireless.test.db.promotion;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.OSSException;
import com.wireless.db.member.MemberDao;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.CouponTypeDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.OssImageError;
import com.wireless.exception.PromotionError;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;
import com.wireless.test.db.oss.TestOssImage;

public class TestPromotionDao {

	private static Staff mStaff;
	
	private static OSSClient ossClient;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		ossClient = new OSSClient("http://" + OssImage.Params.instance().getOssParam().OSS_INNER_POINT, 
								  OssImage.Params.instance().getOssParam().ACCESS_OSS_ID, 
								  OssImage.Params.instance().getOssParam().ACCESS_OSS_KEY);
		try {
			mStaff = StaffDao.getAdminByRestaurant(63);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test 
	public void testCreatePromotion() throws SQLException, BusinessException, ParseException, IOException{
		int promotionId = 0;
		int couponTypeId = 0;
		int promotionImg1 = 0;
		int promotionImg2 = 0;
		try{
			List<Member> members = MemberDao.getByCond(mStaff, null, null);
			
			//--------Test to create a promotion-----------
			String fileName = System.getProperty("user.dir") + "/src/" + TestOssImage.class.getPackage().getName().replaceAll("\\.", "/") + "/test.jpg";
			
			int ossImageId = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.WX_COUPON_TYPE).setImgResource(OssImage.ImageType.JPG, new FileInputStream(new File(fileName))));
			promotionImg1 = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.WX_PROMOTION).setImgResource(OssImage.ImageType.JPG, new FileInputStream(new File(fileName))));
			promotionImg2 = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.WX_PROMOTION).setImgResource(OssImage.ImageType.JPG, new FileInputStream(new File(fileName))));

			CouponType.InsertBuilder typeInsertBuilder = new CouponType.InsertBuilder("测试优惠券类型", 30, "2016-2-1")
																	   .setComment("测试备注")
																	   .setImage(ossImageId);
			
			String htmlTxt = "<br>数量份金沙路<div align=\"center\" style=\"width:100%;\"><img src='$(pic_1)' style=\"max-width:95%;\"></div>谁加路费金沙路费<br><br><div align=\"center\" style=\"width:100%;\"></div><br>";
			String body = htmlTxt.replace("$(pic_1)", OssImageDao.getById(mStaff, promotionImg1).getObjectUrl());

			Promotion.CreateBuilder promotionCreateBuilder = Promotion.CreateBuilder
																	  .newInstance("测试优惠活动", body, Promotion.Rule.FREE, typeInsertBuilder, "hello jingjing<br>")
																	  //.setRange("2016-1-1", "2016-2-1")
																	  //.addMember(m1.getId()).addMember(m2.getId())
																	  ;
			promotionId = PromotionDao.create(mStaff, promotionCreateBuilder);
			
			Promotion expected = promotionCreateBuilder.build();
			expected.setId(promotionId);
			expected.setCouponType(typeInsertBuilder.build());
			
			Promotion actual = PromotionDao.getById(mStaff, promotionId);
			couponTypeId = actual.getCouponType().getId();
			
			//Compare the promotion.
			compare(expected, actual);
			//Compare the oss image to promotion body
			OssImage ossPromotionImg1 = OssImageDao.getById(mStaff, promotionImg1);
			Assert.assertEquals("type to promotion image", OssImage.Type.WX_PROMOTION, ossPromotionImg1.getType());
			Assert.assertEquals("associated id to promotion image", actual.getId(), ossPromotionImg1.getAssociatedId());
			Assert.assertEquals("status to promotion image", OssImage.Status.MARRIED, ossPromotionImg1.getStatus());
			Assert.assertTrue("failed to upload promotion image to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), ossPromotionImg1.getObjectKey()) != null);
			//Compare the coupon related to this promotion.
			for(Member m : members){
				compare(promotionId, Coupon.Status.DRAWN, couponTypeId, m, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m).setPromotion(promotionId), null).get(0));
				//compare(promotionId, Coupon.Status.CREATED, couponTypeId, m2, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m2).setPromotion(promotionId), null).get(0));
			}
			
			//--------Test to update a promotion-----------
			int oriImageToCouponType = ossImageId;
			Member m1 = members.get(0);
			Member m2 = members.get(1);
			Member m3 = members.get(2);
			ossImageId = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.WX_COUPON_TYPE).setImgResource(OssImage.ImageType.JPG, new FileInputStream(new File(fileName))));

			CouponType.UpdateBuilder typeUpdateBuilder = new CouponType.UpdateBuilder(couponTypeId, "修改测试优惠券类型")
																	   .setComment("修改测试备注")
																	   .setImage(ossImageId)
																	   .setPrice(50);
			body = htmlTxt.replace("$(pic_1)", OssImageDao.getById(mStaff, promotionImg2).getObjectUrl());
			Promotion.UpdateBuilder promotionUpdateBuilder = new Promotion.UpdateBuilder(promotionId).setRange("2016-2-1", "2016-3-1")
																									 .setTitle("修改优惠活动")
																									 .setBody(body, "hello jingjing<br>")
																									 .addMember(m1.getId()).addMember(m2.getId()).addMember(m3.getId())
																									 .setCouponTypeBuilder(typeUpdateBuilder);
			expected = promotionUpdateBuilder.build();
			expected.setCouponType(typeUpdateBuilder.build());
			expected.setCreateDate(actual.getCreateDate());
			
			PromotionDao.update(mStaff, promotionUpdateBuilder);
			actual = PromotionDao.getById(mStaff, promotionId);

			//Compare the promotion.
			compare(expected, actual);
			//Compare the oss image to promotion body
			//Check to see the promotion image 1
			ossPromotionImg1 = OssImageDao.getById(mStaff, promotionImg1);
			Assert.assertEquals("associated id to promotion image", 0, ossPromotionImg1.getAssociatedId());
			Assert.assertEquals("status to promotion image", OssImage.Status.SINGLE, ossPromotionImg1.getStatus());
			Assert.assertTrue("original promotion image to oss storage does NOT exist", ossClient.getObject(OssImage.Params.instance().getBucket(), ossPromotionImg1.getObjectKey()) != null);
			//Check to see the promotion image 2
			OssImage ossPromotionImg2 = OssImageDao.getById(mStaff, promotionImg2);
			Assert.assertEquals("type to promotion image", OssImage.Type.WX_PROMOTION, ossPromotionImg2.getType());
			Assert.assertEquals("associated id to promotion image", actual.getId(), ossPromotionImg2.getAssociatedId());
			Assert.assertEquals("status to promotion image", OssImage.Status.MARRIED, ossPromotionImg2.getStatus());
			Assert.assertTrue("failed to upload promotion image 2 to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), ossPromotionImg2.getObjectKey()) != null);
			//Compare the coupon related to this promotion.
			compare(promotionId, Coupon.Status.DRAWN, couponTypeId, m1, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m1).setPromotion(promotionId), null).get(0));
			compare(promotionId, Coupon.Status.DRAWN, couponTypeId, m2, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m2).setPromotion(promotionId), null).get(0));
			compare(promotionId, Coupon.Status.DRAWN, couponTypeId, m3, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m3).setPromotion(promotionId), null).get(0));
			
			//---------- Test the original oss image after promotion update --------------
			OssImage oriImage = OssImageDao.getById(mStaff, oriImageToCouponType);
			Assert.assertEquals("original oss image status", OssImage.Status.SINGLE, oriImage.getStatus());
			Assert.assertEquals("original oss image associated id", 0, oriImage.getAssociatedId());
			OssImageDao.delete(mStaff, new OssImageDao.ExtraCond().setId(oriImageToCouponType));

			try{
				OssImageDao.getById(mStaff, oriImage.getId());
			}catch(BusinessException e2){
				Assert.assertEquals("failed to delete original oss image after update", OssImageError.OSS_IMAGE_NOT_EXIST, e2.getErrCode());
			}
			try{
				ossClient.getObject(OssImage.Params.instance().getBucket(), oriImage.getObjectKey());
				Assert.assertTrue("failed to delete the original image from aliyun oss storage after update", false);
			}catch(OSSException ignored){
			}

//			//--------Test to cancel publish a promotion whose status is NOT 'CREATE'-----------
//			try{
//				PromotionDao.cancelPublish(mStaff, promotionId);
//			}catch(BusinessException e){
//				Assert.assertEquals("failed to cancel publish the promotion", PromotionError.PROMOTION_PUBLISH_NOT_ALLOW, e.getErrCode());
//			}
//			
//			//Test to draw a coupon which has NOT been published.
//			try{
//				Coupon coupon = CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m1).setPromotion(promotionId), null).get(0);
//				CouponDao.draw(mStaff, coupon.getId());
//			}catch(BusinessException e){			
//				Assert.assertEquals("failed to draw coupon", PromotionError.COUPON_DRAW_NOT_ALLOW, e.getErrCode());
//			}
//			//--------Test to publish a promotion-----------
//			PromotionDao.publish(mStaff, promotionId);
//			//Compare the promotion.
//			compare(expected, PromotionDao.getById(mStaff, promotionId));
//			//Compare the coupon related to this promotion.
//			compare(promotionId, Coupon.Status.PUBLISHED, couponTypeId, m1, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m1).setPromotion(promotionId), null).get(0));
//			compare(promotionId, Coupon.Status.PUBLISHED, couponTypeId, m2, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m2).setPromotion(promotionId), null).get(0));
//
//			//--------Test to cancel publish the promotion-----------
//			PromotionDao.cancelPublish(mStaff, promotionId);
//			//Compare the promotion.
//			compare(expected, actual);
//			//Compare the coupon related to this promotion.
//			compare(promotionId, Coupon.Status.CREATED, couponTypeId, m1, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m1).setPromotion(promotionId), null).get(0));
//			compare(promotionId, Coupon.Status.CREATED, couponTypeId, m2, CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m2).setPromotion(promotionId), null).get(0));

		}finally{
			if(promotionId != 0){
				//OssImage promotionImage2 = OssImageDao.getById(mStaff, promotionImg2);
				Promotion original = PromotionDao.getById(mStaff, promotionId);
				PromotionDao.delete(mStaff, promotionId);
				try{
					PromotionDao.getById(mStaff, promotionId);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the promotion", PromotionError.PROMOTION_NOT_EXIST, e.getErrCode());
					//Check to see whether or not the promotion image1 still survive
//					try{
//						OssImage promotionImage1 = OssImageDao.getById(mStaff, promotionImg1);
//						Assert.assertTrue("promotion image 1 should survive in oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), promotionImage1.getObjectKey()) != null);
//					}catch(BusinessException e2){
//						Assert.assertTrue("promotion image 1 should survive", false);
//					}
//					//Check to see whether or not the promotion image2 is removed
//					try{
//						OssImageDao.getById(mStaff, promotionImg2);
//						Assert.assertTrue("promotion image 2 should be removed", false);
//					}catch(BusinessException e2){
//						Assert.assertTrue("promotion image 2 should be removed", e2.getErrCode() == OssImageError.OSS_IMAGE_NOT_EXIST);
//					}
//					try{
//						ossClient.getObject(OssImage.Params.instance().getBucket(), promotionImage2.getObjectKey());
//						Assert.assertTrue("failed to delete the promotion image 2 from aliyun oss storage", false);
//					}catch(OSSException e3){}
					//Check to see whether or not the associated oss image to promotion is deleted.
					try{
						OssImageDao.getById(mStaff, original.getImage().getId());
					}catch(BusinessException e2){
						Assert.assertEquals("failed to delete oss image", OssImageError.OSS_IMAGE_NOT_EXIST, e2.getErrCode());
					}
					//Check to see whether or not the promotion image is deleted from oss storage.
					try{
						Assert.assertTrue("failed to put image to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), original.getImage().getObjectKey()) != null);
						Assert.assertTrue("failed to delete the promotion image from aliyun oss storage", false);
					}catch(OSSException ignored){}
					
					//Check to see whether or not the associated coupon is deleted.
					Assert.assertTrue("failed to delete the associated coupon", CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setPromotion(promotionId), null).isEmpty());
					//Check to see whether or not the associated coupon type is deleted.
					try{
						CouponTypeDao.getById(mStaff, couponTypeId);
					}catch(BusinessException e2){
						Assert.assertEquals("failed to delete the promotion", PromotionError.COUPON_TYPE_NOT_EXIST, e2.getErrCode());
					}
					//Check to see whether or not the associated oss image to coupon type is deleted.
					try{
						OssImageDao.getById(mStaff, original.getCouponType().getImage().getId());
					}catch(BusinessException e2){
						Assert.assertEquals("failed to delete oss image", OssImageError.OSS_IMAGE_NOT_EXIST, e2.getErrCode());
					}
					//Check to see whether or not the associated oss image to coupon type is deleted from oss storage.
					try{
						ossClient.getObject(OssImage.Params.instance().getBucket(), original.getCouponType().getImage().getObjectKey());
						Assert.assertTrue("failed to delete the coupon type image from aliyun oss storage", false);
					}catch(OSSException ignored){
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
		Assert.assertEquals("promotion body", expected.getEntire(), actual.getEntire());
		Assert.assertEquals("promotion date range", expected.getDateRange(), actual.getDateRange());
		Assert.assertEquals("promotion status", expected.getStatus(), actual.getStatus());
		Assert.assertEquals("promotion rule", expected.getRule(), actual.getRule());
		Assert.assertEquals("promotion type", expected.getType(), actual.getType());
		Assert.assertEquals("promotion oriented", expected.getOriented(), actual.getOriented());
		
		//The content to associated promotion image
		Assert.assertEquals("oss image type to promotion", OssImage.Type.PROMOTION, actual.getImage().getType());
		Assert.assertEquals("oss image associated id to promotion", actual.getId(), actual.getImage().getAssociatedId());
		Assert.assertTrue("failed to put image to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), actual.getImage().getObjectKey()) != null);
		
		//The content to associated coupon type
		//Assert.assertEquals("id : insert coupon type", expected.getCouponType().getId(), actual.getCouponType().getId());
		Assert.assertEquals("name : insert coupon type", expected.getCouponType().getName(), actual.getCouponType().getName());
		Assert.assertEquals("price : insert coupon type", expected.getCouponType().getPrice(), actual.getCouponType().getPrice(), 0.01);
		Assert.assertEquals("restaurant : insert coupon type", mStaff.getRestaurantId(), actual.getCouponType().getRestaurantId());
		//Assert.assertEquals("expired : insert coupon type", expected.getCouponType().getExpired(), actual.getCouponType().getExpired());
		Assert.assertEquals("comment : insert coupon type", expected.getCouponType().getComment(), actual.getCouponType().getComment());
		
		//The content to associated coupon type image
		Assert.assertEquals("oss image type to coupon type", OssImage.Type.WX_COUPON_TYPE, actual.getCouponType().getImage().getType());
		Assert.assertEquals("oss image associated id to coupon type", actual.getCouponType().getId(), actual.getCouponType().getImage().getAssociatedId());
		Assert.assertEquals("oss image id to coupon type", expected.getCouponType().getImage().getId(), actual.getCouponType().getImage().getId());
		Assert.assertTrue("failed to put image to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), actual.getCouponType().getImage().getObjectKey()) != null);
	}
	
	private void compare(int expectedPromotion, Coupon.Status expectedStatus, int expectedType, Member expectedMember, Coupon actual){
		Assert.assertEquals("coupon promotion id", expectedPromotion, actual.getPromotion().getId());
		Assert.assertEquals("coupon type", expectedType, actual.getCouponType().getId());
		Assert.assertEquals("coupon restaurant id", mStaff.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("coupon member id", expectedMember.getId(), actual.getMember().getId());
		Assert.assertEquals("coupon status", expectedStatus, actual.getStatus());
		Assert.assertTrue("coupon birth date", System.currentTimeMillis() - actual.getBirthDate() < 100000);
	}
	
	
}
