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
import com.wireless.db.promotion.CouponOperationDao;
import com.wireless.db.promotion.CouponTypeDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.promotion.PromotionUseTimeDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.OssImageError;
import com.wireless.exception.PromotionError;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.CouponOperation;
import com.wireless.pojo.promotion.CouponType;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.promotion.PromotionTrigger;
import com.wireless.pojo.promotion.PromotionUseTime;
import com.wireless.pojo.restaurantMgr.Restaurant;
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
	public void test4Chain() throws SQLException, BusinessException{
		Restaurant group = RestaurantDao.getById(40);
		Staff groupStaff = StaffDao.getAdminByRestaurant(group.getId());
		Restaurant branch = RestaurantDao.getById(65);
		Staff branchStaff = StaffDao.getAdminByRestaurant(branch.getId());
		
		RestaurantDao.update(new Restaurant.UpdateBuilder(group.getId()).addBranch(branch));
		groupStaff = StaffDao.getById(groupStaff.getId());
		branchStaff = StaffDao.getById(branchStaff.getId());
		
		final List<Member> members = MemberDao.getByCond(branchStaff, null, null);
		Member branchMember1 = members.get(0);
		Member branchMember2 = members.get(1);
		Member branchMember3 = members.get(2);
		
		int promotionId = 0;
		try{
			Promotion.CreateBuilder promotionCreateBuilder = Promotion.CreateBuilder.newInstance("测试优惠活动", "测试优惠活动", new CouponType.InsertBuilder("测试优惠券类型", 30, "2020-2-1")
							   										  .setComment("测试备注"), "hello jingjing<br>")
					  												   ;
			promotionId = PromotionDao.create(groupStaff, promotionCreateBuilder);
			
			Promotion actualPromotion = PromotionDao.getById(branchStaff, promotionId);
			
			//Issue the coupon to m1, m2, m3 belongs to branch.
			Coupon.IssueBuilder issueBuilder = Coupon.IssueBuilder.newInstance4Fast().addPromotion(actualPromotion).addMember(branchMember1).addMember(branchMember2).addMember(branchMember3);
			CouponDao.issue(branchStaff, issueBuilder);
			//Compare the coupon related to this promotion.
			Coupon coupon1 = CouponDao.getByCond(branchStaff, new CouponDao.ExtraCond().setMember(branchMember1).setPromotion(promotionId), null).get(0);
			Coupon coupon2 = CouponDao.getByCond(branchStaff, new CouponDao.ExtraCond().setMember(branchMember2).setPromotion(promotionId), null).get(0);
			Coupon coupon3 = CouponDao.getByCond(branchStaff, new CouponDao.ExtraCond().setMember(branchMember3).setPromotion(promotionId), null).get(0);
			compare(branchStaff, issueBuilder, actualPromotion, branchMember1, coupon1);
			compare(groupStaff, issueBuilder, actualPromotion, branchMember1, coupon1);
			compare(branchStaff, issueBuilder, actualPromotion, branchMember2, coupon2);
			compare(groupStaff, issueBuilder, actualPromotion, branchMember2, coupon2);
			compare(branchStaff, issueBuilder, actualPromotion, branchMember3, coupon3);
			compare(groupStaff, issueBuilder, actualPromotion, branchMember3, coupon3);
			
			//Use the coupon to m1 belongs to branch
			Coupon.UseBuilder useBuilder = Coupon.UseBuilder.newInstance4Fast(branchMember1).addCoupon(coupon1);
			CouponDao.use(branchStaff, useBuilder);
			coupon1 = CouponDao.getById(branchStaff, coupon1.getId());
			compare(branchStaff, useBuilder, branchMember1, coupon1);
			compare(groupStaff, useBuilder, branchMember1, coupon1);
			
		}finally{
			if(promotionId != 0){
				PromotionDao.deleteById(groupStaff, promotionId);
			}
			RestaurantDao.update(new Restaurant.UpdateBuilder(group.getId()).clearBranch());
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

			CouponType.InsertBuilder typeInsertBuilder = new CouponType.InsertBuilder("测试优惠券类型", 30, "2020-2-1")
																	   .setComment("测试备注")
																	   .setImage(ossImageId);
			
			String htmlTxt = "<br>数量份金沙路<div align=\"center\" style=\"width:100%;\"><img src='$(pic_1)' style=\"max-width:95%;\"></div>谁加路费金沙路费<br><br><div align=\"center\" style=\"width:100%;\"></div><br>";
			String body = htmlTxt.replace("$(pic_1)", OssImageDao.getById(mStaff, promotionImg1).getObjectUrl());

			Promotion.CreateBuilder promotionCreateBuilder = Promotion.CreateBuilder
																	  .newInstance("测试优惠活动", body, typeInsertBuilder, "hello jingjing<br>")
																	  .setIssueTrigger(PromotionTrigger.InsertBuilder.newIssue4Free())
																	  .setUseTrigger(PromotionTrigger.InsertBuilder.newUse4SingleExceed(100))
																	  .addUseTime(PromotionUseTime.InsertBuilder.newInstance(PromotionUseTime.Week.Monday, "12:15:00", "16:45:00"))
																	  .addUseTime(PromotionUseTime.InsertBuilder.newInstance(PromotionUseTime.Week.Friday, "14:15:00", "22:45:00"))
																	  .addUseTime(PromotionUseTime.InsertBuilder.newInstance(PromotionUseTime.Week.Thursday, "12:15:00", "23:45:00"));
																	  ;
			promotionId = PromotionDao.create(mStaff, promotionCreateBuilder);
			
			Promotion expectedPromotion = promotionCreateBuilder.build();
			expectedPromotion.setId(promotionId);
			expectedPromotion.setCouponType(typeInsertBuilder.build());
			
			Promotion actualPromotion = PromotionDao.getById(mStaff, promotionId);
			couponTypeId = actualPromotion.getCouponType().getId();
			
			//Compare the promotion.
			compare(expectedPromotion, actualPromotion);
			//Compare the oss image to promotion body
			OssImage ossPromotionImg1 = OssImageDao.getById(mStaff, promotionImg1);
			Assert.assertEquals("type to promotion image", OssImage.Type.WX_PROMOTION, ossPromotionImg1.getType());
			Assert.assertEquals("associated id to promotion image", actualPromotion.getId(), ossPromotionImg1.getAssociatedId());
			Assert.assertEquals("status to promotion image", OssImage.Status.MARRIED, ossPromotionImg1.getStatus());
			Assert.assertTrue("failed to upload promotion image to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), ossPromotionImg1.getObjectKey()) != null);
			
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
			
			Promotion.UpdateBuilder promotionUpdateBuilder = new Promotion.UpdateBuilder(promotionId).setRange("2016-2-1", "2020-3-1")
																									 .setTitle("修改优惠活动")
																									 .setBody(body, "hello jingjing<br>")
																									 .setCouponTypeBuilder(typeUpdateBuilder)
																									 .setIssueTrigger(PromotionTrigger.InsertBuilder.newIssue4SingleExceed(100))
																									 .setUseTrigger(null)
																									 .addUseTime(PromotionUseTime.InsertBuilder.newInstance(PromotionUseTime.Week.Wednesday, "13:15:00", "21:45:00"))
																									 .addUseTime(PromotionUseTime.InsertBuilder.newInstance(PromotionUseTime.Week.Monday, "14:15:00", "22:45:00"))
																									 ;
			expectedPromotion = promotionUpdateBuilder.build();
			expectedPromotion.setCouponType(typeUpdateBuilder.build());
			expectedPromotion.setCreateDate(actualPromotion.getCreateDate());
			
			PromotionDao.update(mStaff, promotionUpdateBuilder);
			actualPromotion = PromotionDao.getById(mStaff, promotionId);

			//Compare the promotion.
			compare(expectedPromotion, actualPromotion);
			//Compare the oss image to promotion body
			//Check to see the promotion image 1
			ossPromotionImg1 = OssImageDao.getById(mStaff, promotionImg1);
			Assert.assertEquals("associated id to promotion image", 0, ossPromotionImg1.getAssociatedId());
			Assert.assertEquals("status to promotion image", OssImage.Status.SINGLE, ossPromotionImg1.getStatus());
			Assert.assertTrue("original promotion image to oss storage does NOT exist", ossClient.getObject(OssImage.Params.instance().getBucket(), ossPromotionImg1.getObjectKey()) != null);
			//Check to see the promotion image 2
			OssImage ossPromotionImg2 = OssImageDao.getById(mStaff, promotionImg2);
			Assert.assertEquals("type to promotion image", OssImage.Type.WX_PROMOTION, ossPromotionImg2.getType());
			Assert.assertEquals("associated id to promotion image", actualPromotion.getId(), ossPromotionImg2.getAssociatedId());
			Assert.assertEquals("status to promotion image", OssImage.Status.MARRIED, ossPromotionImg2.getStatus());
			Assert.assertTrue("failed to upload promotion image 2 to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), ossPromotionImg2.getObjectKey()) != null);
			
			//Issue the coupon to m1, m2, m3
			Coupon.IssueBuilder issueBuilder = Coupon.IssueBuilder.newInstance4Fast().addPromotion(actualPromotion).addMember(m1).addMember(m2).addMember(m3);
			CouponDao.issue(mStaff, issueBuilder);
			//Compare the coupon related to this promotion.
			Coupon coupon1 = CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m1).setPromotion(promotionId), null).get(0);
			Coupon coupon2 = CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m2).setPromotion(promotionId), null).get(0);
			Coupon coupon3 = CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(m3).setPromotion(promotionId), null).get(0);
			compare(mStaff, issueBuilder, actualPromotion, m1, coupon1);
			compare(mStaff, issueBuilder, actualPromotion, m2, coupon2);
			compare(mStaff, issueBuilder, actualPromotion, m3, coupon3);
			
			//Use the coupon to m1
			Coupon.UseBuilder useBuilder = Coupon.UseBuilder.newInstance4Fast(m1).addCoupon(coupon1);
			CouponDao.use(mStaff, useBuilder);
			coupon1 = CouponDao.getById(mStaff, coupon1.getId());
			compare(mStaff, useBuilder, m1, coupon1);
			
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

		}finally{
			if(promotionId != 0){
				//OssImage promotionImage2 = OssImageDao.getById(mStaff, promotionImg2);
				Promotion original = PromotionDao.getById(mStaff, promotionId);
				PromotionDao.deleteById(mStaff, promotionId);
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
//					try{
//						if(original.hasImage()){
//							OssImageDao.getById(mStaff, original.getImage().getId());
//						}
//					}catch(BusinessException e2){
//						Assert.assertEquals("failed to delete oss image", OssImageError.OSS_IMAGE_NOT_EXIST, e2.getErrCode());
//					}
					//Check to see whether or not the promotion image is deleted from oss storage.
//					try{
//						if(original.hasImage()){
//							Assert.assertTrue("failed to put image to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), original.getImage().getObjectKey()) != null);
//							Assert.assertTrue("failed to delete the promotion image from aliyun oss storage", false);
//						}
//					}catch(OSSException ignored){}

					Assert.assertTrue("failed to delete the promotion useTime", PromotionUseTimeDao.getByCond(mStaff, new PromotionUseTimeDao.ExtraCond().setPromotionId(promotionId)).isEmpty());
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
		Assert.assertEquals("promotion type", expected.getType(), actual.getType());
		
		//The content to associated promotion image
//		Assert.assertEquals("oss image type to promotion", OssImage.Type.PROMOTION, actual.getImage().getType());
//		Assert.assertEquals("oss image associated id to promotion", actual.getId(), actual.getImage().getAssociatedId());
//		Assert.assertTrue("failed to put image to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), actual.getImage().getObjectKey()) != null);
		
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
		
		//The issue triggers
		//Assert.assertEquals("promotion trigger id", expected.getTrigger().getId(), actual.getTrigger().getId());
		Assert.assertEquals("issue trigger type", expected.getIssueTrigger().getType(), actual.getIssueTrigger().getType());
		Assert.assertEquals("issue promotion id to trigger", expected.getId(), actual.getIssueTrigger().getPromotionId());
		Assert.assertEquals("issue trigger issue rule", expected.getIssueTrigger().getIssueRule(), actual.getIssueTrigger().getIssueRule());
		Assert.assertEquals("issue trigger use rule", expected.getIssueTrigger().getUseRule(), actual.getIssueTrigger().getUseRule());
		Assert.assertEquals("issue trigger extra", expected.getIssueTrigger().getExtra(), actual.getIssueTrigger().getExtra());
		
		//The use triggers
		//Assert.assertEquals("promotion trigger id", expected.getTrigger().getId(), actual.getTrigger().getId());
		Assert.assertEquals("use trigger type", expected.getUseTrigger().getType(), actual.getUseTrigger().getType());
		Assert.assertEquals("promotion id to use trigger", expected.getId(), actual.getUseTrigger().getPromotionId());
		Assert.assertEquals("use trigger issue rule", expected.getUseTrigger().getIssueRule(), actual.getUseTrigger().getIssueRule());
		Assert.assertEquals("use trigger use rule", expected.getUseTrigger().getUseRule(), actual.getUseTrigger().getUseRule());
		Assert.assertEquals("use trigger extra", expected.getUseTrigger().getExtra(), actual.getUseTrigger().getExtra());
		
		//the useTime
		//Assert.assertEquals("primmotionUseTime id", expected.getUseTime().)
		for(int i = 0; i < actual.getUseTime().size(); i++){
			Assert.assertEquals("promotion id to useTime", expected.getId(), actual.getUseTime().get(i).getPromotionId());
			Assert.assertEquals("promotion useTime week", expected.getUseTime().get(i).getWeek(), actual.getUseTime().get(i).getWeek());
			Assert.assertEquals("promotion useTime start", expected.getUseTime().get(i).getStart(), actual.getUseTime().get(i).getStart());
			Assert.assertEquals("promotion useTime end", expected.getUseTime().get(i).getEnd(), actual.getUseTime().get(i).getEnd());
		}
		
		
	}
	
	private void compare(Staff staff, Coupon.IssueBuilder issueBuilder, Promotion expectedPromotion, Member expectedMember, Coupon actual) throws SQLException{
		if(staff.isBranch()){
			Assert.assertEquals("coupon restaurant id", staff.getGroupId(), actual.getRestaurantId());
		}else{
			Assert.assertEquals("coupon restaurant id", staff.getRestaurantId(), actual.getRestaurantId());
		}
		Assert.assertEquals("coupon promotion id", expectedPromotion.getId(), actual.getPromotion().getId());
		Assert.assertEquals("coupon member id", expectedMember.getId(), actual.getMember().getId());
		Assert.assertEquals("coupon status", Coupon.Status.ISSUED, actual.getStatus());
		Assert.assertTrue("coupon birth date", System.currentTimeMillis() - actual.getBirthDate() < 100000);
		
		CouponOperation operation = CouponOperationDao.getByCond(staff, new CouponOperationDao.ExtraCond().setCoupon(actual.getId())
																										   .addOperation(CouponOperation.Operate.FAST_ISSUE)
																										   .setMemberFuzzy(expectedMember.getName())).get(0);
		if(staff.isBranch()){
			Assert.assertEquals("restaurant id to coupon operation", staff.getGroupId(), operation.getRestaurantId());
			Assert.assertEquals("branch id to coupon operation", staff.getRestaurantId(), operation.getBranchId());
		}else{
			Assert.assertEquals("restaurant id to coupon operation", staff.getRestaurantId(), operation.getRestaurantId());
		}
		Assert.assertTrue("coupon issue date", System.currentTimeMillis() - operation.getOperateDate() < 100000);
		Assert.assertEquals("coupon issue staff", staff.getName(), operation.getOperateStaff());
		Assert.assertEquals("coupon issue mode", issueBuilder.getOperation(), operation.getOperate());
		Assert.assertEquals("coupon issue associate id", issueBuilder.getAssociateId(), operation.getAssociateId());
		Assert.assertEquals("coupon issue comment", issueBuilder.getComment(), operation.getComment());
		Assert.assertEquals("coupon issue member id", expectedMember.getId(), operation.getMemberId());
		Assert.assertEquals("coupon issue member name", expectedMember.getName(), operation.getMemberName());
	}
	
	private void compare(Staff staff, Coupon.UseBuilder useBuilder, Member expectedMember, Coupon actual) throws SQLException, BusinessException{
		if(staff.isBranch()){
			Assert.assertEquals("coupon restaurant id", staff.getGroupId(), actual.getRestaurantId());
		}else{
			Assert.assertEquals("coupon restaurant id", staff.getRestaurantId(), actual.getRestaurantId());
		}
		Assert.assertEquals("coupon member id", expectedMember.getId(), actual.getMember().getId());
		Assert.assertEquals("coupon status", Coupon.Status.USED, actual.getStatus());
		
		CouponOperation operation = CouponOperationDao.getByCond(staff, new CouponOperationDao.ExtraCond().setCoupon(actual.getId())
				   .addOperation(CouponOperation.Operate.FAST_USE)).get(0);


		if(staff.isBranch()){
			Assert.assertEquals("restaurant id to coupon operation", staff.getGroupId(), operation.getRestaurantId());
			Assert.assertEquals("branch id to coupon operation", staff.getRestaurantId(), operation.getBranchId());
		}else{
			Assert.assertEquals("restaurant id to coupon operation", staff.getRestaurantId(), operation.getRestaurantId());
		}
		Assert.assertTrue("coupon use date", System.currentTimeMillis() - operation.getOperateDate() < 100000);
		Assert.assertEquals("coupon use staff", staff.getName(), operation.getOperateStaff());
		Assert.assertEquals("coupon use mode", useBuilder.getOperation(), operation.getOperate());
		Assert.assertEquals("coupon use associate id", useBuilder.getAssociateId(), operation.getAssociateId());
		Assert.assertEquals("coupon use comment", useBuilder.getComment(), operation.getComment());
		Assert.assertEquals("coupon issue member id", expectedMember.getId(), operation.getMemberId());
		Assert.assertEquals("coupon issue member name", expectedMember.getName(), operation.getMemberName());
	}
	
}
