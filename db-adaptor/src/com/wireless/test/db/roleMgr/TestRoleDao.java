package com.wireless.test.db.roleMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StaffError;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Role.Category;
import com.wireless.pojo.staffMgr.Role.InsertBuilder;
import com.wireless.pojo.staffMgr.Role.Type;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestRoleDao {

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
	
	private void compare(Role expected, Role actual){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("name", expected.getName(), actual.getName());
		Assert.assertEquals("type", expected.getType(), actual.getType());
		Assert.assertEquals("cate", expected.getCategory(), actual.getCategory());
		comparePrivileges(expected, actual);
	}
	
	private void comparePrivileges(Role expected, Role actual){
		Assert.assertEquals("amount of privileges", expected.getPrivileges().size(), actual.getPrivileges().size());
		for(int index = 0; index < expected.getPrivileges().size(); index++){
			//权限信息对比
			Assert.assertEquals("privilege id # " + index, expected.getPrivileges().get(index).getId(), actual.getPrivileges().get(index).getId());
			Assert.assertEquals("privilege cate # " + index, expected.getPrivileges().get(index).getCate(), actual.getPrivileges().get(index).getCate());
			
			Assert.assertEquals("privilege code # " + index, expected.getPrivileges().get(index).getCode(), actual.getPrivileges().get(index).getCode());
			if(expected.getPrivileges().get(index).getCode() == Code.DISCOUNT){
				Assert.assertEquals("amount of privilege discount", expected.getPrivileges().get(index).getDiscounts().size(), actual.getPrivileges().get(index).getDiscounts().size());
				for(Discount expectedDiscount : expected.getPrivileges().get(index).getDiscounts()){
					Assert.assertTrue("privilege discount", actual.getPrivileges().get(index).getDiscounts().indexOf(expectedDiscount) >= 0);
				}
			}
		}	
	}
	
	@Test
	public void testRoleDao() throws SQLException, BusinessException{
		//get all discount
		List<Discount> discounts = DiscountDao.getAll(mStaff);
		int roleId = 0;
		
		try{
//			//创建默认角色
//			DefAdminBuilder builder = new DefAdminBuilder(mStaff.getRestaurantId());
//			
//			roleId = RoleDao.insertRole(mStaff, builder);
//			
//			//期望值
//			Role expected = builder.build();
//			expected.setId(roleId);
//			//实际
//			Role actual = RoleDao.getRoleById(mStaff, roleId);
//			//对比
//			compare(expected, actual);
			
			//创建新角色
			InsertBuilder newBuilder = new InsertBuilder(mStaff.getRestaurantId(), "副部长")
										   .setCategoty(Category.OTHER)
										   .setType(Type.NORMAL)
										   .addPrivilege(Privilege.Code.ADD_FOOD)
										   .addPrivilege(Privilege.Code.BASIC)
										   .addDiscount(discounts.get(0))
										   .addDiscount(discounts.get(1));
			
			roleId = RoleDao.insertRole(mStaff, newBuilder);
			
			Role expected = newBuilder.build();
			expected.setId(roleId);
			
			Role actual = RoleDao.getRoleById(mStaff, roleId);
			//compare
			compare(expected, actual);
			
			//修改角色信息
			Role.UpdateBuilder updateBuilder = new Role.UpdateBuilder(roleId)
													   .setName("经理")
													   .addPrivilege(Privilege.Code.BASIC)
													   .addPrivilege(Privilege.Code.CHECK_ORDER)
													   .addDiscount(discounts.get(1));
			
			RoleDao.updateRole(mStaff, updateBuilder);
			actual = RoleDao.getRoleById(mStaff, roleId);
			expected = updateBuilder.build();
			
			Assert.assertEquals("restaurantId", mStaff.getRestaurantId(), actual.getRestaurantId());
			if(updateBuilder.isNameChanged()){
				Assert.assertEquals("name", expected.getName(), actual.getName());
			}
			if(updateBuilder.isPrivilegeChanged()){
				comparePrivileges(expected, actual);
			}
			
		}finally{
			//删除角色
			if(roleId != 0){
				RoleDao.deleteRole(roleId);
				try{
					RoleDao.getRoleById(mStaff, roleId);
					Assert.assertTrue("failed to delete role", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete role", StaffError.ROLE_NOT_EXIST, e.getErrCode());
				}
			}			
		}
		
	}

	
	
	
}
