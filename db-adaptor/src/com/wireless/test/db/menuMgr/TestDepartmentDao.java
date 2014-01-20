package com.wireless.test.db.menuMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestDepartmentDao {
	private static Staff mStaff;
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getStaffs(37).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDepartmentDao() throws SQLException, BusinessException{
		int deptId = 0;
		try{
			Department.AddBuilder addBuilder = new Department.AddBuilder("测试部门");
			deptId = DepartmentDao.add(mStaff, addBuilder);
			
			Department actual = DepartmentDao.getById(mStaff, deptId);
			Department expected = addBuilder.build();
			
			Assert.assertEquals("id : insert department", deptId, actual.getId());
			Assert.assertEquals("restaurant : insert department", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("name : insert department", expected.getName(), actual.getName());
			Assert.assertEquals("type : insert department", Department.Type.NORMAL, actual.getType());

			Department.UpdateBuilder updateBuilder = new Department.UpdateBuilder(Department.DeptId.valueOf(deptId), "测试部门2");
			DepartmentDao.update(mStaff, updateBuilder);
			actual = DepartmentDao.getById(mStaff, deptId);
			expected = updateBuilder.build();
			
			Assert.assertEquals("id : insert department", deptId, actual.getId());
			Assert.assertEquals("restaurant : insert department", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("name : insert department", expected.getName(), actual.getName());
			Assert.assertEquals("type : insert department", Department.Type.NORMAL, actual.getType());
			
		}finally{
			if(deptId != 0){
				DepartmentDao.remove(mStaff, deptId);
				Department deptToRemove = DepartmentDao.getById(mStaff, deptId);
				Assert.assertEquals("type : failed to remove department", Department.Type.IDLE, deptToRemove.getType());
			}
		}
	}
}
