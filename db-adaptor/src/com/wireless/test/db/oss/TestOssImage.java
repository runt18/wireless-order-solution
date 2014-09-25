package com.wireless.test.db.oss;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.OSSException;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.OssImageError;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestOssImage {
	private static Staff mStaff;
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getByRestaurant(37).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testOssImageDao() throws SQLException, BusinessException, IOException{
		OSSClient ossClient = new OSSClient("http://" + OssImage.Params.instance().getOssParam().OSS_INNER_POINT, 
			    							OssImage.Params.instance().getOssParam().ACCESS_OSS_ID, 
			    							OssImage.Params.instance().getOssParam().ACCESS_OSS_KEY);
		int ossImageId = 0;
		try{
			String fileName = System.getProperty("user.dir") + "/src/" + TestOssImage.class.getPackage().getName().replaceAll("\\.", "/") + "/test.jpg";
			
			//---------- Test to insert a new oss image --------------
			OssImage.InsertBuilder builder = new OssImage.//InsertBuilder(OssImage.Type.WX_PROMOTION)
														  InsertBuilder(OssImage.Type.WX_PROMOTION, 1)
														 .setImgResource(OssImage.ImageType.JPG, new FileInputStream(new File(fileName)));
			ossImageId = OssImageDao.insert(mStaff, builder);
			
			OssImage actual = OssImageDao.getById(mStaff, ossImageId);
			OssImage expected = builder.build();
			Assert.assertEquals("oss image id", ossImageId, actual.getId());
			Assert.assertEquals("oss image type", expected.getType(), actual.getType());
			Assert.assertEquals("oss associated id", expected.getAssociatedId(), actual.getAssociatedId());
			Assert.assertEquals("oss image status", expected.getStatus(), actual.getStatus());
			Assert.assertTrue("failed to put the image to aliyunc oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), actual.getObjectKey()) != null);
			
			//---------- Test to update a oss image --------------
			OssImage.UpdateBuilder updateBuilder = new OssImage.UpdateBuilder(ossImageId)
															   .setAssociated(OssImage.Type.WX_FINANCE, "VZtrdLaO6WFcJQrvffO9XBPVpbKGRP")
															   .setImgResource("test.jpg", new FileInputStream(new File(fileName)));
			String originalImgKey = actual.getObjectKey();
			OssImageDao.update(mStaff, updateBuilder);
			expected = updateBuilder.build();
			actual = OssImageDao.getByCond(mStaff, new OssImageDao.ExtraCond().setAssociated(expected.getType(), expected.getAssociatedSerial())).get(0);
			Assert.assertEquals("oss image id", ossImageId, actual.getId());
			Assert.assertEquals("oss image type", expected.getType(), actual.getType());
			Assert.assertEquals("oss image", expected.getImage(), actual.getImage());
			Assert.assertEquals("oss image status", expected.getStatus(), actual.getStatus());
			try{
				ossClient.getObject(OssImage.Params.instance().getBucket(), originalImgKey);
				Assert.assertTrue("failed to remove the original image to aliyunc oss storage", false);
			}catch(OSSException ignored){}
			Assert.assertTrue("failed to put the image to aliyunc oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), actual.getObjectKey()) != null);
			
		}finally{
			if(ossImageId != 0){
				OssImage original = OssImageDao.getById(mStaff, ossImageId);
				OssImageDao.delete(mStaff, new OssImageDao.ExtraCond().setId(ossImageId));
				try{
					OssImageDao.getById(mStaff, ossImageId);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete oss image", OssImageError.OSS_IMAGE_NOT_EXIST, e.getErrCode());
				}
				try{
					ossClient.getObject(OssImage.Params.instance().getBucket(), original.getObjectKey());
					Assert.assertTrue("failed to delete the image from aliyun oss storage", true);
				}catch(OSSException ignored){
				}
			}
		}
	}
}
