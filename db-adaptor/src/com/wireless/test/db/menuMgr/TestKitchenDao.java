package com.wireless.test.db.menuMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestKitchenDao {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getStaffs(26).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testKitchenDao() throws SQLException, BusinessException{
		int kitchenId = 0;
		try{
			//Add a kitchen.
			Kitchen.AddBuilder addBuilder = new Kitchen.AddBuilder("测试厨房", Department.DeptId.DEPT_1).setAllowTmp(false);
			kitchenId = KitchenDao.add(mStaff, addBuilder);
			
			Kitchen expected = addBuilder.build();
			Kitchen actual = KitchenDao.getById(mStaff, kitchenId);
			
			Assert.assertEquals("id : add kitchen", kitchenId, actual.getId());
			Assert.assertEquals("name : add kitchen", expected.getName(), actual.getName());
			Assert.assertEquals("dept : add kitchen", expected.getDept().getId(), actual.getDept().getId());
			Assert.assertEquals("type : add kitchen", Kitchen.Type.NORMAL, actual.getType());
			Assert.assertEquals("restaurant : add kitchen", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("is allow tmp : add kitchen", expected.isAllowTemp(), actual.isAllowTemp());
			
			//Update a kitchen.
			Kitchen.UpdateBuilder updateBuilder = new Kitchen.UpdateBuilder(kitchenId).setName("测试厨房2").setDeptId(Department.DeptId.DEPT_2).setAllowTmp(true);
			KitchenDao.update(mStaff, updateBuilder);
			expected = updateBuilder.build();
			actual = KitchenDao.getById(mStaff, kitchenId);
			
			Assert.assertEquals("id : update kitchen", kitchenId, actual.getId());
			Assert.assertEquals("name : update kitchen", expected.getName(), actual.getName());
			Assert.assertEquals("dept : update kitchen", expected.getDept().getId(), actual.getDept().getId());
			Assert.assertEquals("type : update kitchen", Kitchen.Type.NORMAL, actual.getType());
			Assert.assertEquals("restaurant : update kitchen", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("is allow tmp : update kitchen", expected.isAllowTemp(), actual.isAllowTemp());
			
		}finally{
			if(kitchenId != 0){
				KitchenDao.remove(mStaff, kitchenId);
				Kitchen kitchenRemoved = KitchenDao.getById(mStaff, kitchenId);
				Assert.assertEquals("type : failed to remove kitchen", kitchenRemoved.getType(), Kitchen.Type.IDLE);
				Assert.assertEquals("dept : failed to remove kitchen", kitchenRemoved.getDept().getId(), Department.DeptId.DEPT_NULL.getVal());
				Assert.assertEquals("is allow temp : failed to remove kitchen", kitchenRemoved.isAllowTemp(), false);
			}
		}
	}
}
