package com.wireless.test.db.regionMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.RegionError;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Region.Status;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestRegionDao {

	private static Staff mStaff;

	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException {
		TestInit.init();
		try {
			mStaff = StaffDao.getAdminByRestaurant(37);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRegonDao() throws BusinessException, SQLException {
		int regionId = 0;
		try{
			regionId = RegionDao.add(mStaff, new Region.AddBuilder("测试区域"));
			
			Region expected = new Region(regionId);
			expected.setName("测试区域");
			expected.setStatus(Status.BUSY);
			
			compare(expected, RegionDao.getById(mStaff, regionId));
			
			RegionDao.update(mStaff, new Region.UpdateBuilder(regionId, "修改区域"));
			expected.setName("修改区域");
			
			compare(expected, RegionDao.getById(mStaff, regionId));
			
		}finally{
			RegionDao.remove(mStaff, regionId);
			try{
				RegionDao.getById(mStaff, regionId);
			}catch(BusinessException e){
				Assert.assertEquals("failed to remove region", e, RegionError.REGION_NOT_EXIST);
			}
		}
	}

	private void compare(Region expected, Region actual){
		Assert.assertEquals("region id", expected.getId(), actual.getId());
		Assert.assertEquals("region name", expected.getName(), actual.getName());
		Assert.assertEquals("region status", expected.getStatus(), actual.getStatus());
	}
}
