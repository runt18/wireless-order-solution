package com.wireless.test.db.staffMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.staffMgr.Staff.DefAdminBuilder;
import com.wireless.pojo.staffMgr.Staff.StaffInsertBuilder;
import com.wireless.pojo.staffMgr.Staff.StaffUpdateBuilder;
import com.wireless.test.db.TestInit;

public class TestStaffDao {

	private static Staff mStaff;
	@BeforeClass
	public static void initDBParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try{
			mStaff = StaffDao.getStaffs(37).get(0);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void compare(Staff expected, Staff actual){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("roleId", expected.getRole(), actual.getRole());
		Assert.assertEquals("name", expected.getName(), actual.getName());
		//Assert.assertEquals("tele", expected.getMobile(), actual.getMobile());
		Assert.assertEquals("pwd", expected.getPwd(), actual.getPwd());
		Assert.assertEquals("type", expected.getType(), actual.getType());
	}
	
	@Test
	public void TestStaff() throws SQLException, BusinessException{
		//get all role
		List<Role> roles = RoleDao.getRoles(mStaff, null, null);
		
		int staffId = 0, adminId = 0;
		try{
			Role staffRole = null;
			for (Role role : roles) {
				if(role.getCategory() == Role.Category.ADMIN){
					staffRole = role;
				}
			}
			//添加默认管理员
			DefAdminBuilder admin = new DefAdminBuilder("123", mStaff.getRestaurantId(), staffRole);
			//admin.setRestaurantId(mStaff.getRestaurantId());

			adminId = StaffDao.insertStaff(admin);
			
			Staff expected = admin.build();
			expected.setId(adminId);
			expected.setPwd("202cb962ac59075b964b07152d234b70");
			
			Staff actual = StaffDao.getStaffById(adminId);
			//compare
			compare(expected, actual);
			
			//财务
			for (Role role : roles) {
				if(role.getCategory() == Role.Category.FINANCE){
					staffRole = role;
				}
			}
			//添加新员工
			StaffInsertBuilder builder = new StaffInsertBuilder("鸣人", "123", mStaff.getRestaurantId(), staffRole);

			builder.setMobile("13533464033");
			
			staffId = StaffDao.insertStaff(builder);
			
			expected = builder.build();
			expected.setId(staffId);
			expected.setPwd("202cb962ac59075b964b07152d234b70");
			
			actual = StaffDao.getStaffById(staffId);
			//compare
			compare(expected, actual);
			
			//修改信息
			StaffUpdateBuilder updateBuilder = new StaffUpdateBuilder(staffId);
			
			updateBuilder.setStaffPwd("321");
			updateBuilder.setStaffName("佐助");
			updateBuilder.setMobile("13433464033");
			updateBuilder.setRoleId(staffRole.getId());
			
			StaffDao.updateStaff(updateBuilder);
			
			Staff currentStaff = StaffDao.getStaffById(staffId);
			
			actual.setPwd("caf1a3dfb505ffed0d024130f58c5cfa");
			actual.setName("佐助");
			actual.setMobile("13433464033");
			
			compare(actual, currentStaff);
			
			
		}finally{
			//StaffDao.deleteStaff(adminId);
			//StaffDao.deleteStaff(staffId);
		}

		
	}
	
	
	
	
}
