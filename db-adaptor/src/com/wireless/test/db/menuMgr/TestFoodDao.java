package com.wireless.test.db.menuMgr;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.aliyun.openservices.oss.OSSClient;
import com.aliyun.openservices.oss.OSSException;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.menuMgr.FoodDao.ExtraCond4Combo;
import com.wireless.db.menuMgr.FoodUnitDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FoodError;
import com.wireless.exception.OssImageError;
import com.wireless.exception.PricePlanError;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;
import com.wireless.test.db.oss.TestOssImage;

public class TestFoodDao {
	
	private static Staff mStaff;

	private static OSSClient ossClient;
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException, BusinessException{
		TestInit.init();
		ossClient = new OSSClient("http://" + OssImage.Params.instance().getOssParam().OSS_INNER_POINT, 
				OssImage.Params.instance().getOssParam().ACCESS_OSS_ID, 
				OssImage.Params.instance().getOssParam().ACCESS_OSS_KEY);
		try {
			mStaff = StaffDao.getAdminByRestaurant(40);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCombo() throws SQLException, BusinessException{
		int foodId = 0;
		try {
			Food.InsertBuilder insertBuilder = new Food.InsertBuilder("测试套菜", 15.4f, KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL).get(0))
													   .setAliasId(100).setDesc("测试描述").setHot(true).setCommission(2.0f).setGift(true).setWeigh(false);
			foodId = FoodDao.insert(mStaff, insertBuilder); 
			
			List<Food> foods = FoodDao.getPureByCond(mStaff, null, null);
			Food childFood1 = foods.get(0);
			Food childFood2 = foods.get(2);
			
			Food.ComboBuilder comboBuilder = new Food.ComboBuilder(foodId)
													 .addChild(childFood1.getFoodId(), 1)
													 .addChild(childFood2.getFoodId(), 2);
			FoodDao.buildCombo(mStaff, comboBuilder);
			
			Food expected = comboBuilder.build();
			Food actual = FoodDao.getById(mStaff, foodId);
			
			Assert.assertEquals("parent id : insert combo", expected.getFoodId(), actual.getFoodId());
			Assert.assertEquals("children : insert combo", expected.getChildFoods(), actual.getChildFoods());
			
		}finally{
			if(foodId != 0){
				FoodDao.deleteById(mStaff, foodId);
				try{
					FoodDao.getById(mStaff, foodId);
					Assert.assertTrue("failed to delete the food", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the food", FoodError.FOOD_NOT_EXIST, e.getErrCode());
				}
				
				Assert.assertEquals("failed to delete the child foods", 0, FoodDao.getComboByCond(mStaff, new ExtraCond4Combo(foodId)).size());
			}
		}
	}
	
	@Test
	public void testFoodDao() throws SQLException, BusinessException, IOException{

		int foodId = 0;
		int planId = 0;
		
		try {
			String fileName = System.getProperty("user.dir") + "/src/" + TestOssImage.class.getPackage().getName().replaceAll("\\.", "/") + "/test.jpg";
			
			//---------- Test to insert a new food --------------
			int ossImageId = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.FOOD_IMAGE)
			 													    .setImgResource(OssImage.ImageType.JPG, new FileInputStream(new File(fileName))));
			
			//---------- Test to insert a price plan --------------
			PricePlan.InsertBuilder insertPlanBuilder = new PricePlan.InsertBuilder("测试价格方案");
			planId = PricePlanDao.insert(mStaff, insertPlanBuilder);
			
			PricePlan expectedPlan = insertPlanBuilder.build();
			expectedPlan.setId(planId);
			
			PricePlan actualPlan = PricePlanDao.getById(mStaff, planId);
			
			compare(expectedPlan, actualPlan);
			
			Food.InsertBuilder insertBuilder = new Food.InsertBuilder("测试菜品", 15.4f, KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL).get(0))
													   .setImage(ossImageId)
													   .addPrice(planId, 4)
													   .setAliasId(65500).setDesc("测试描述")
													   .setHot(true).setCommission(2.0f).setGift(true).setWeigh(false).setLimit(true, 10).setSplit(true)
													   .addUnit(2.5f, "半只")
													   .setPrintKitchen(KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL).get(0));
			foodId = FoodDao.insert(mStaff, insertBuilder); 
			
			Food expected = insertBuilder.build();
			expected.setImage(new OssImage(ossImageId));
			
			Food actual = FoodDao.getById(mStaff, foodId);
			
			compare(foodId, expected, actual, "insert food");

			//---------- Test to update a price plan --------------
			PricePlan.UpdateBuilder updatePlanBuilder = new PricePlan.UpdateBuilder(planId).setName("修改价格方案");
			PricePlanDao.update(mStaff, updatePlanBuilder);
			
			expectedPlan = updatePlanBuilder.build();
			expectedPlan.setType(actualPlan.getType());
			actualPlan = PricePlanDao.getById(mStaff, planId);
			
			compare(expectedPlan, actualPlan);
			
			//---------- Test to update the food --------------
			int oriImageId = ossImageId;
			ossImageId = OssImageDao.insert(mStaff, new OssImage.InsertBuilder(OssImage.Type.FOOD_IMAGE)
			    												.setImgResource(OssImage.ImageType.JPG, new FileInputStream(new File(fileName))));
			
			Food.UpdateBuilder updateBuilder = new Food.UpdateBuilder(foodId).setAliasId(1001).setName("测试修改菜品")
													   .setKitchen(KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL).get(1))
													   .setImage(ossImageId)
													   .setPrice(34.2f).setDesc("测试修改描述")
													   .addPrice(planId, 5)
													   .setHot(false).setCommission(3).setSellOut(true).setRecommend(true)
													   .setGift(false).setWeigh(true).setLimit(true, 5).setSplit(false).setLimit(false, 0)
													   .addUnit(3f, "份")
													   .setPrintKitchen(KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL).get(2))
													   ;
			FoodDao.update(mStaff, updateBuilder);
			
			expected = updateBuilder.build();
			actual = FoodDao.getById(mStaff, foodId);

			compare(foodId, expected, actual, "update food");

			//---------- Test the original oss image after update --------------
			OssImage oriImage = OssImageDao.getById(mStaff, oriImageId);
			Assert.assertEquals("original oss image status", OssImage.Status.SINGLE, oriImage.getStatus());
			Assert.assertEquals("original oss image associated id", 0, oriImage.getAssociatedId());
			OssImageDao.delete(mStaff, new OssImageDao.ExtraCond().setId(oriImageId));

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
			
			//---------- Test to delete the oss image --------------
//			oriImage = OssImageDao.getById(mStaff, ossImageId);
//			FoodDao.update(mStaff, new Food.UpdateBuilder(foodId).setImage(null));
//			try{
//				OssImageDao.getById(mStaff, ossImageId);
//				Assert.assertTrue("failed to delete the image", false);
//			}catch(BusinessException e){
//				Assert.assertEquals("failed to delete oss image", OssImageError.OSS_IMAGE_NOT_EXIST, e.getErrCode());
//			}
//			try{
//				ossClient.getObject(OssImage.Params.instance().getBucket(), oriImage.getObjectKey());
//				Assert.assertTrue("failed to delete the image from aliyun oss storage", false);
//			}catch(OSSException ignored){
//			}
			
		} finally{
			if(foodId != 0){
				Food original = FoodDao.getById(mStaff, foodId);
				FoodDao.deleteById(mStaff, foodId);
				try{
					FoodDao.getById(mStaff, foodId);
					Assert.assertTrue("failed to delete the food", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the food", FoodError.FOOD_NOT_EXIST, e.getErrCode());
				}
				if(original.hasImage()){
					try{
						OssImageDao.getById(mStaff, original.getImage().getId());
						Assert.assertTrue("failed to delete the image", false);
					}catch(BusinessException e2){
						Assert.assertEquals("failed to delete oss image", OssImageError.OSS_IMAGE_NOT_EXIST, e2.getErrCode());
					}
					try{
						ossClient.getObject(OssImage.Params.instance().getBucket(), original.getImage().getObjectKey());
						Assert.assertTrue("failed to delete the image from aliyun oss storage", false);
					}catch(OSSException ignored){
					}
				}
				if(original.hasFoodUnit()){
					Assert.assertTrue("failed to delete the associated food unit", FoodUnitDao.getByCond(mStaff, new FoodUnitDao.ExtraCond().addFood(original)).isEmpty());
				}
			}
			if(planId != 0){
				PricePlanDao.deleteById(mStaff, planId);
				try{
					PricePlanDao.getById(mStaff, planId);
					Assert.assertTrue("failed to delete price plan", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete price plan", PricePlanError.PRICE_PLAN_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compare(int foodId, Food expected, Food actual, final String tag){
		//------- the content to food --------
		Assert.assertEquals("id : " + tag, foodId, actual.getFoodId());
		Assert.assertEquals("restaurant : " + tag, mStaff.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("name : " + tag, expected.getName(), actual.getName());
		Assert.assertEquals("price : " + tag, expected.getPrice(), actual.getPrice(), 0.01);
		Assert.assertEquals("kitchen : " + tag, expected.getKitchen(), actual.getKitchen());
		Assert.assertEquals("print kitchen : " + tag, expected.getPrintKitchenId(), actual.getPrintKitchenId());
		Assert.assertEquals("alias : " + tag, expected.getAliasId(), actual.getAliasId());
		Assert.assertEquals("desc : " + tag, expected.getDesc(), actual.getDesc());
		Assert.assertEquals("current price : " + tag, expected.isCurPrice(), actual.isCurPrice());
		Assert.assertEquals("recommend : " + tag, expected.isRecommend(), actual.isRecommend());
		Assert.assertEquals("sell out : " + tag, expected.isSellOut(), actual.isSellOut());
		Assert.assertEquals("special : " + tag, expected.isSpecial(), actual.isSpecial());
		Assert.assertEquals("weight : " + tag, expected.isWeight(), actual.isWeight());
		Assert.assertEquals("hot : " + tag, expected.isHot(), actual.isHot());
		Assert.assertEquals("gift : " + tag, expected.isGift(), actual.isGift());
		Assert.assertEquals("limit : " + tag, expected.isLimit(), actual.isLimit());
		Assert.assertEquals("limit amount : " + tag, expected.getLimitAmount(), actual.getLimitAmount());
		Assert.assertEquals("commission : " + tag, expected.getCommission(), actual.getCommission(), 0.01);
		Assert.assertEquals("split : " + tag, expected.isSplit(), actual.isSplit());

		//------- the content to associated food units --------
		for(int i = 0; i < expected.getFoodUnits().size(); i++){
			//Assert.assertEquals("food unit id", expected.getFoodUnits().get(0).getId(), actual.getFoodUnits().get(0).getId());
			Assert.assertEquals("associated food id to unit", foodId, actual.getFoodUnits().get(0).getFoodId());
			Assert.assertEquals("food unit price", expected.getFoodUnits().get(0).getPrice(), actual.getFoodUnits().get(0).getPrice(), 0.01);
			Assert.assertEquals("food unit", expected.getFoodUnits().get(0).getUnit(), actual.getFoodUnits().get(0).getUnit());
		}
		//------- the content to associated image --------
		Assert.assertEquals("oss image type to food : " + tag, OssImage.Type.FOOD_IMAGE, actual.getImage().getType());
		Assert.assertEquals("oss image associated id to food : " + tag, actual.getFoodId(), actual.getImage().getAssociatedId());
		Assert.assertEquals("oss image status to food : " + tag, OssImage.Status.MARRIED, actual.getImage().getStatus());
		Assert.assertEquals("oss image id to food : " + tag, expected.getImage().getId(), actual.getImage().getId());
		Assert.assertTrue("failed to put image to oss storage", ossClient.getObject(OssImage.Params.instance().getBucket(), actual.getImage().getObjectKey()) != null);
		//------- the content to thumb nail image --------
		Assert.assertEquals("oss thumbnail type to food" + tag, OssImage.Type.THUMB_NAIL, actual.getImage().getThumbnail().getType());
		Assert.assertEquals("oss thumbnail id to food" + tag, actual.getImage().getId(), actual.getImage().getThumbnail().getAssociatedId());
		//------- the content to thumb nail image --------
		Assert.assertEquals("price plan" + tag, expected.getPricePlan(), actual.getPricePlan());
	}
	
	private void compare(PricePlan expected, PricePlan actual){
		Assert.assertEquals("price plan id", expected.getId(), actual.getId());
		Assert.assertEquals("price plan restaurant id", mStaff.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("price plan name", expected.getName(), actual.getName());
		Assert.assertEquals("price plan type", expected.getType(), actual.getType());
	}
}
