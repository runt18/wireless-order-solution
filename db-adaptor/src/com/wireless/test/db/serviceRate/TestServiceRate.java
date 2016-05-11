package com.wireless.test.db.serviceRate;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.serviceRate.ServicePlanDao;
import com.wireless.db.serviceRate.ServicePlanDao.ShowType;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ServiceRateError;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.serviceRate.ServicePlan;
import com.wireless.pojo.serviceRate.ServiceRate;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestServiceRate {
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDBParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try{
			mStaff = StaffDao.getAdminByRestaurant(37);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testServiceRate() throws SQLException, BusinessException{
		int planId = 0;
		try{
			ServicePlan.InsertBuilder builder = new ServicePlan.InsertBuilder("测试服务费方案").setRate(0.1f);
			planId = ServicePlanDao.insert(mStaff, builder);
			
			ServicePlan expected = builder.build();
			expected.setPlanId(planId);
			for(Region region : RegionDao.getByStatus(mStaff, Region.Status.BUSY)){
				ServiceRate expectedRate = new ServiceRate(0);
				expectedRate.setRegion(region);
				expectedRate.setRate(0.1f);
				expected.addRate(expectedRate);
			}
			
			ServicePlan actual = ServicePlanDao.getById(mStaff, planId, ShowType.BY_REGION);
			
			compare(expected, actual);
			
			//Test to update the service plan
			ServicePlan.UpdateBuilder updateBuilder = new ServicePlan.UpdateBuilder(planId)
																     .setName("修改测试方案")
																     .setStatus(ServicePlan.Status.DEFAULT)
																     .addRate(expected.getRates().get(0).getRegion(), 0.2f)
																     .addRate(expected.getRates().get(1).getRegion(), 0.3f);
			ServicePlanDao.update(mStaff, updateBuilder);
			expected.setName("修改测试方案");
			expected.setStatus(ServicePlan.Status.DEFAULT);
			expected.getRates().get(0).setRate(0.2f);
			expected.getRates().get(1).setRate(0.3f);
																	
			actual = ServicePlanDao.getById(mStaff, planId, ShowType.BY_REGION);
			
			compare(expected, actual);
			
		}finally{
			if(planId != 0){
				ServicePlanDao.delete(mStaff, planId);
				try{
					ServicePlanDao.getById(mStaff, planId, ShowType.BY_PLAN);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the service plan", ServiceRateError.SERVICE_RATE_PLAN_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compare(ServicePlan expected, ServicePlan actual){
		Assert.assertEquals("service plan id", expected.getPlanId(), actual.getPlanId());
		Assert.assertEquals("service plan name", expected.getName(), actual.getName());
		Assert.assertEquals("service plan status", expected.getStatus(), actual.getStatus());
		Assert.assertEquals("service plan type", expected.getType(), actual.getType());
		for(ServiceRate expectedRate : expected.getRates()){
			boolean isExist = false;
			for(ServiceRate actualRate : actual.getRates()){
				if(actualRate.getRegion().equals(expectedRate.getRegion())){
					Assert.assertEquals(expectedRate.getRegion().toString(), expectedRate.getRate(), actualRate.getRate(), 0.01);
					isExist = true;
					break;
				}
			}
			Assert.assertTrue(expectedRate.getRegion() + "does NOT exist in acutal", isExist);
		}
	}
}
