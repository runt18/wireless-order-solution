package com.wireless.test.db.weixin.restaurant;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aliyun.openservices.oss.OSSClient;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.CalcWeixinSignature;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.test.db.TestInit;
import com.wireless.test.db.oss.TestOssImage;

public class TestWeixinRestaurantDao {
	
	private static Staff mStaff;
	
	private static OSSClient ossClient;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		ossClient = new OSSClient("http://" + OssImage.Params.instance().getOssParam().OSS_INNER_POINT, 
				  OssImage.Params.instance().getOssParam().ACCESS_OSS_ID, 
				  OssImage.Params.instance().getOssParam().ACCESS_OSS_KEY);
		try {
			mStaff = StaffDao.getAdminByRestaurant(11);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testVerify() throws SQLException, BusinessException, NoSuchAlgorithmException{
		final String account = "demo";
		final String timestamp = "2013-9-11 7:48:00";
		final String nonce = "jingyang";
		WxRestaurantDao.verify(account, CalcWeixinSignature.calc(RestaurantDao.getByAccount(account).getAccount(), timestamp, nonce), timestamp, nonce);
		WxRestaurant actual = WxRestaurantDao.get(StaffDao.getAdminByRestaurant(RestaurantDao.getByAccount(account).getId()));
		Assert.assertEquals("verify status", WxRestaurant.Status.VERIFIED, actual.getStatus());
		Assert.assertTrue("verify restaurant", WxRestaurantDao.isVerified(account));
		
	}
	
	@Test 
	public void testUpdate() throws SQLException, BusinessException, FileNotFoundException, IOException{
		
		
		String fileName = System.getProperty("user.dir") + "/src/" + TestOssImage.class.getPackage().getName().replaceAll("\\.", "/") + "/test.jpg";
		
		int ossImageId = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.WX_LOGO).setImgResource(OssImage.ImageType.JPG, new FileInputStream(new File(fileName))));

		int ossInfoImg1 = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.WX_INFO).setImgResource(OssImage.ImageType.JPG, new FileInputStream(new File(fileName))));
		OssImage infoImg1 = OssImageDao.getById(mStaff, ossInfoImg1);
		int ossInfoImg2 = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.WX_INFO).setImgResource(OssImage.ImageType.JPG, new FileInputStream(new File(fileName))));
		OssImage infoImg2 = OssImageDao.getById(mStaff, ossInfoImg2);

		String htmlTxt = "<br>数量份金沙路<div align=\"center\" style=\"width:100%;\"><img src='$(pic_1)' style=\"max-width:95%;\"></div>谁加路费金沙路费<br><br><div align=\"center\" style=\"width:100%;\"><img src='$(pic_2)' style=\"max-width:95%;\"></div><br>";
		String info = htmlTxt.replace("$(pic_1)", infoImg1.getObjectUrl()).replace("$(pic_2)", infoImg2.getObjectUrl());
		
		WxRestaurant.UpdateBuilder builder = new WxRestaurant.UpdateBuilder()
																	.setWeixinLogo(ossImageId)
																	.setWeixinInfo(info)
																	.setWeixinAppId("asdfsdfsaf")
																	.setWeixinAppSecret("dadsftwe")
																	.setQrCodeUrl("http://www.qrcode")
																	.setQrCode("http://qrcode")
																	.setHeadImgUrl("http://www.headimg")
																	.setNickName("测试昵称")
																	.setRefreshToken("adfeiilmasd;iottt")
																	.setDefaultOrderType(WxRestaurant.PayType.CONFIRM_BY_STAFF);
		WxRestaurantDao.update(mStaff, builder);
		
		WxRestaurant expected = builder.build();
		expected.setRestaurantId(mStaff.getRestaurantId());
		WxRestaurant actual = WxRestaurantDao.get(mStaff);
		
		Assert.assertEquals("weixin logo type", OssImage.Type.WX_LOGO, actual.getWeixinLogo().getType());
		Assert.assertEquals("weixin logo associated id", mStaff.getRestaurantId(), actual.getWeixinLogo().getAssociatedId());
		Assert.assertTrue("failed to upload weixin logo to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), actual.getWeixinLogo().getObjectKey()) != null);
		
		Assert.assertEquals("weixin info", expected.getWeixinInfo(), actual.getWeixinInfo());
		//Check the oss image 1 to info
		infoImg1 = OssImageDao.getById(mStaff, ossInfoImg1);
		Assert.assertEquals("weixin info type to image1", OssImage.Type.WX_INFO, infoImg1.getType());
		Assert.assertEquals("weixin info associated id to image1", mStaff.getRestaurantId(), infoImg1.getAssociatedId());
		Assert.assertTrue("failed to upload weixin info image1 to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), infoImg1.getObjectKey()) != null);
		//Check the oss image 2 to info
		infoImg2 = OssImageDao.getById(mStaff, ossInfoImg2);
		Assert.assertEquals("weixin info type to image2", OssImage.Type.WX_INFO, infoImg2.getType());
		Assert.assertEquals("weixin info associated id to image2", mStaff.getRestaurantId(), infoImg2.getAssociatedId());
		Assert.assertTrue("failed to upload weixin info image2 to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), infoImg2.getObjectKey()) != null);
		
		Assert.assertEquals("weixin app id", expected.getWeixinAppId(), actual.getWeixinAppId());
		Assert.assertEquals("weixin secret", expected.getWeixinAppSecret(), actual.getWeixinAppSecret());
		Assert.assertEquals("weixin qr code url", expected.getQrCodeUrl(), actual.getQrCodeUrl());
		Assert.assertEquals("weixin qr code", expected.getQrCode(), actual.getQrCode());
		Assert.assertEquals("weixin head image url", expected.getHeadImgUrl(), actual.getHeadImgUrl());
		Assert.assertEquals("weixin nick name", expected.getNickName(), actual.getNickName());
		Assert.assertEquals("weixin refresh token", expected.getRefreshToken(), actual.getRefreshToken());
		Assert.assertEquals("weixin refresh token", expected.getDefaultOrderType(), actual.getDefaultOrderType());

	}
}
