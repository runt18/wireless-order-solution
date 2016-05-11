package com.wireless.test.db.staffMgr;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StaffError;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.staffMgr.Staff.InsertBuilder;
import com.wireless.pojo.staffMgr.Staff.UpdateBuilder;
import com.wireless.test.db.TestInit;

public class TestStaffDao {

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
	
	public void compare(Staff expected, Staff actual){
		assertEquals("id", expected.getId(), actual.getId());
		assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		assertEquals("roleId", expected.getRole(), actual.getRole());
		assertEquals("name", expected.getName(), actual.getName());
		//Assert.assertEquals("tele", expected.getMobile(), actual.getMobile());
		assertEquals("pwd", expected.getPwd(), actual.getPwd());
		assertEquals("type", expected.getType(), actual.getType());
	}
	
	@Test
	public void testStaffDao() throws SQLException, BusinessException{
		
		int staffId = 0;
		try{
			//添加新员工
			InsertBuilder builder = new InsertBuilder("鸣人", "123", RoleDao.getByCategory(mStaff, Role.Category.FINANCE).get(0)).setMobile("13533464033");
			
			staffId = StaffDao.insert(mStaff, builder);
			
			Staff expected = builder.build();
			expected.setId(staffId);
			expected.setRestaurantId(mStaff.getRestaurantId());
			expected.setPwd("202cb962ac59075b964b07152d234b70");
			
			Staff actual = StaffDao.getById(staffId);
			//compare
			compare(expected, actual);
			
			//修改信息
			UpdateBuilder updateBuilder = new UpdateBuilder(staffId);
			
			updateBuilder.setStaffPwd("321");
			updateBuilder.setStaffName("佐助");
			updateBuilder.setMobile("13433464033");
			updateBuilder.setRoleId(RoleDao.getByCategory(mStaff, Role.Category.BOSS).get(0).getId());
			
			expected = updateBuilder.build();
			expected.setRestaurantId(mStaff.getRestaurantId());
			expected.setRole(RoleDao.getByCategory(mStaff, Role.Category.BOSS).get(0));
			expected.setPwd("caf1a3dfb505ffed0d024130f58c5cfa");
			
			StaffDao.update(mStaff, updateBuilder);
			
			actual = StaffDao.getById(staffId);
			
			compare(expected, actual);
			
			StaffDao.getByCond(mStaff, new StaffDao.ExtraCond().addPrivilegeCode(Privilege.Code.ADD_FOOD).addPrivilegeCode(Privilege.Code.ADD_FOOD));
			
		}finally{
			if(staffId != 0){
				StaffDao.deleteById(mStaff, staffId);
				try{
					StaffDao.getById(staffId);
				}catch(BusinessException e){
					assertEquals("failed to delete the staff", StaffError.STAFF_NOT_EXIST, e.getErrCode());
				}
			}
		}

		
	}
	
	
	
	
}
