package com.wireless.test.db.roleMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.StaffError;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.pojo.staffMgr.Privilege4Price;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Role.Category;
import com.wireless.pojo.staffMgr.Role.InsertBuilder;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestRoleDao {

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
			if(expected.getPrivileges().get(index).getCode() == Code.PRICE_PLAN){
				List<PricePlan> expectedPricePlans = ((Privilege4Price)expected.getPrivileges().get(index)).getPricePlans();
				List<PricePlan> actualPricePlans = ((Privilege4Price)actual.getPrivileges().get(index)).getPricePlans();
				Assert.assertEquals("amount of privilege price plan", expectedPricePlans.size(), actualPricePlans.size());
				for(PricePlan expectedPlan : expectedPricePlans){
					Assert.assertTrue("privilege price plan", actualPricePlans.indexOf(expectedPlan) >= 0);
				}
			}
		}	
	}
	
	@Test
	public void testRoleDao() throws SQLException, BusinessException{
		//get all discount
		List<Discount> discounts = DiscountDao.getAll(mStaff);
		List<PricePlan> pricePlans = PricePlanDao.getByCond(mStaff, null);
		int roleId = 0;
		
		try{
			
			//创建新角色
			InsertBuilder newBuilder = new InsertBuilder(mStaff.getRestaurantId(), "副部长")
										   .setCategoty(Category.OTHER)
										   .addPrivilege(Privilege.Code.ADD_FOOD)
										   .addPrivilege(Privilege.Code.BASIC)
										   .addDiscount(discounts.get(0))
										   .addDiscount(discounts.get(1))
										   .addPricePlan(pricePlans.get(0));
			
			roleId = RoleDao.insert(mStaff, newBuilder);
			
			Role expected = newBuilder.build();
			expected.setId(roleId);
			
			Role actual = RoleDao.getyById(mStaff, roleId);
			//compare
			compare(expected, actual);
			
			//修改角色信息
			Role.UpdateBuilder updateBuilder = new Role.UpdateBuilder(roleId)
													   .setName("经理")
													   .addPrivilege(Privilege.Code.BASIC)
													   .addPrivilege(Privilege.Code.CHECK_ORDER)
													   .addDiscount(discounts.get(1))
													   .addPricePlan(pricePlans.get(0));
			
			RoleDao.update(mStaff, updateBuilder);
			actual = RoleDao.getyById(mStaff, roleId);
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
					RoleDao.getyById(mStaff, roleId);
					Assert.assertTrue("failed to delete role", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete role", StaffError.ROLE_NOT_EXIST, e.getErrCode());
				}
			}			
		}
		
	}

	
	
	
}
