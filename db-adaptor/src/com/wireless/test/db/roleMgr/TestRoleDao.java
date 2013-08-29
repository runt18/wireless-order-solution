package com.wireless.test.db.roleMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.PrivilegeDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Role.Category;
import com.wireless.pojo.staffMgr.Role.DefAdminInsertBuilder;
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
	
	public void compare(Role expected, Role actual){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("name", expected.getName(), actual.getName());
		Assert.assertEquals("type", expected.getType(), actual.getType());
		Assert.assertEquals("cate", expected.getCategory(), actual.getCategory());
		for (Privilege p : expected.getPrivileges()) {
			int index = actual.getPrivileges().indexOf(p);
			if(index >= 0){
				//权限信息对比
				Assert.assertEquals("privilegeId", p.getId(), actual.getPrivileges().get(index).getId());
				Assert.assertEquals("priCode", p.getCode(), actual.getPrivileges().get(index).getCode());
				Assert.assertEquals("cate", p.getCate(), actual.getPrivileges().get(index).getCate());
				if(p.getCode() == Code.DISCOUNT){
					for (Discount d : p.getDiscounts()) {
						int dcIndex = actual.getPrivileges().get(index).getDiscounts().indexOf(d);
						if(dcIndex >= 0){
							//折扣信息对比
							Assert.assertEquals("discountId", d.getId(), actual.getPrivileges().get(index).getDiscounts().get(dcIndex));
							Assert.assertEquals("discountName", d.getName(), actual.getPrivileges().get(index).getDiscounts().get(dcIndex));
							Assert.assertEquals("restaurant_id", d.getRestaurantId(), actual.getPrivileges().get(index).getDiscounts().get(dcIndex));
						}else{
							Assert.assertEquals("the discount", false);
						}
					}
				}
			}else{
				Assert.assertEquals("the privileges", false);
			}
		}
		
	}
	@Test
	public void TestRole() throws SQLException, BusinessException{
		//get all privilege
		List<Privilege> privileges = PrivilegeDao.getPrivileges(mStaff);
		//get all discount
		List<Discount> discounts = DiscountDao.getDiscount(mStaff, null, null);
		int roleId = 0, newRoleId = 0;
		
		try{
			//创建默认角色
			DefAdminInsertBuilder builder = new DefAdminInsertBuilder();
			builder.setRestaurantId(mStaff.getRestaurantId());
			
			//包括角色的插入和对应关系表的插入
			roleId = RoleDao.insertRole(mStaff, builder);
			
			//期望值
			Role expected = builder.build();
			expected.setId(roleId);
			//实际
			Role actual = RoleDao.getRoleById(mStaff, roleId);
			//对比
			compare(expected, actual);
			
			//创建新角色
			int index;
			InsertBuilder newBuilder = new InsertBuilder();
			newBuilder.setName("副部长");
			newBuilder.setRestaurantId(mStaff.getRestaurantId());
			newBuilder.setCategoty(Category.OTHER);
			newBuilder.setType(Type.NORMAL);
			
			
			index = privileges.indexOf(new Privilege(Privilege.Code.FRONT_BUSINESS)); 
			if(index >= 0){
				newBuilder.addPrivileges(privileges.get(index));
			}
			index = privileges.indexOf(new Privilege(Privilege.Code.BASIC));
			if(index >= 0){
				newBuilder.addPrivileges(privileges.get(index));
			}
			
			newRoleId = RoleDao.insertRole(mStaff, newBuilder);
			
			expected = newBuilder.build();
			expected.setId(newRoleId);
			
			actual = RoleDao.getRoleById(mStaff, newRoleId);
			
			compare(expected, actual);
			
			//修改角色信息
			actual.setName("经理");
			index = privileges.indexOf(Privilege.Code.DISCOUNT);
			if(index >= 0){
				for (Discount discount : discounts) {
					privileges.get(index).addDiscount(discount);
				}
			}
			
			RoleDao.updateRole(mStaff, actual);
			
			Role updateRole = RoleDao.getRoleById(mStaff, actual.getId());
			
			//compare
			compare(actual, updateRole);
		}finally{
			//删除角色
			RoleDao.deleteRole(roleId);
			RoleDao.deleteRole(newRoleId);
		}

		

		
		
		
		
		
		
	}
	
	
}
