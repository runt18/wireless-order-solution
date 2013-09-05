package com.wireless.test.db.restaurantMgr;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.inventoryMgr.MaterialCateDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestRestaurantDao {
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testQueryByID() throws BusinessException, SQLException{
		
		Staff staff = StaffDao.getStaffs(37).get(0);
		
		Restaurant oriRestaurant = RestaurantDao.getById(staff.getRestaurantId());
		
		Restaurant restToUpdate = new Restaurant();
		restToUpdate.setId(oriRestaurant.getId());
		restToUpdate.setAccount(oriRestaurant.getAccount());
		restToUpdate.setName("测试名称");
		restToUpdate.setAddress("测试地址");
		restToUpdate.setInfo("测试信息");
		restToUpdate.setTele1("测试电话1");
		restToUpdate.setTele2("测试电话2");
		
		RestaurantDao.update(staff, restToUpdate);
		
		Restaurant restAfterUpdate = RestaurantDao.getById(staff.getRestaurantId());
		
		assertEquals("restaurant id", restToUpdate.getId(), restAfterUpdate.getId());
		assertEquals("restaurant account", restToUpdate.getAccount(), restAfterUpdate.getAccount());
		assertEquals("restaurant name", restToUpdate.getName(), restAfterUpdate.getName());
		assertEquals("restaurant info", restToUpdate.getInfo(), restAfterUpdate.getInfo());
		assertEquals("restaurant address", restToUpdate.getAddress(), restAfterUpdate.getAddress());
		assertEquals("restaurant 1st tele", restToUpdate.getTele1(), restAfterUpdate.getTele1());
		assertEquals("restaurant 2nd tele", restToUpdate.getTele2(), restAfterUpdate.getTele2());

		//restore the original restaurant info
		RestaurantDao.update(staff, oriRestaurant);
	}
	
	@Test
	public void testRestaurantDao() throws SQLException, ParseException, BusinessException{
		int restaurantId = 0;
		try{
			Restaurant.InsertBuilder builder = new Restaurant.InsertBuilder("test_account", "测试餐厅", new SimpleDateFormat("yyyy-MM-dd").parse("2014-01-01").getTime(), "test@123")
															 .setAddress("测试地址")
															 .setRestaurantInfo("测试信息")
															 .setTele1("1333333333")
															 .setTele2("020-85473215");
			restaurantId = RestaurantDao.insert(builder);
			
			Restaurant actual = builder.build();
			actual.setId(restaurantId);
			
			Restaurant expected = RestaurantDao.getById(restaurantId);
			//Compare the basic info
			compareRestaurant(expected, actual);
			
			//Compare the '商品' material category
			compareMaterialCate(restaurantId);
			
			//Compare the 默认的活动价格方案
			comparePricePlan(restaurantId);
			
			//Compare the role & staff
			compareRoleAndStaff(restaurantId);
			
		}finally{
			RestaurantDao.deleteById(restaurantId);
		}
	}
	
	private void compareRoleAndStaff(int restaurantId) throws SQLException, BusinessException{
		
		Staff expectedStaff = StaffDao.getStaffs(restaurantId).get(0);
		assertEquals("admin staff restaurant id", expectedStaff.getRestaurantId(), restaurantId);
		assertEquals("admin staff type", expectedStaff.getType().getVal(), Staff.Type.RESERVED.getVal());
		assertEquals("admin staff role type", expectedStaff.getRole().getCategory().getVal(), Role.Category.ADMIN.getVal());
		assertEquals("admin staff name", expectedStaff.getName(), Staff.DefAdminBuilder.ADMIN);
		
		compareRole(restaurantId, expectedStaff, Role.Category.ADMIN, Role.Type.RESERVED);
		
		compareRole(restaurantId, expectedStaff, Role.Category.BOSS, Role.Type.RESERVED);

		compareRole(restaurantId, expectedStaff, Role.Category.FINANCE, Role.Type.NORMAL);

		compareRole(restaurantId, expectedStaff, Role.Category.MANAGER, Role.Type.NORMAL);

		compareRole(restaurantId, expectedStaff, Role.Category.CASHIER, Role.Type.NORMAL);

		compareRole(restaurantId, expectedStaff, Role.Category.WAITER, Role.Type.NORMAL);

	}
	
	private void compareRole(int restaurantId, Staff staff, Role.Category cate, Role.Type type) throws SQLException{
		Role expectedRole = RoleDao.getRoles(staff, "AND cate = " + cate.getVal(), null).get(0);
		assertEquals("role name", expectedRole.getName(), cate.getDesc());
		assertEquals("role category", expectedRole.getCategory().getVal(), cate.getVal());
		assertEquals("role restaurant id", expectedRole.getRestaurantId(), restaurantId);
		assertEquals("role category", expectedRole.getType().getVal(), type.getVal());
	}
	
	private void comparePricePlan(int restaurantId) throws SQLException, BusinessException{
		PricePlan expectedPricePlan = new PricePlan();
		expectedPricePlan.setRestaurantId(restaurantId);
		expectedPricePlan.setStatus(PricePlan.Status.ACTIVITY);
		
		PricePlan actualPricePlan = PricePlanDao.getPricePlans(StaffDao.getStaffs(restaurantId).get(0), null, null).get(0);
		
		assertEquals("price plan restaurant id", expectedPricePlan.getRestaurantId(), actualPricePlan.getRestaurantId());
		assertEquals("price plan status", expectedPricePlan.getStatus().getVal(), actualPricePlan.getStatus().getVal());
	}
	
	private void compareMaterialCate(int restaurantId) throws SQLException{
		MaterialCate expectMaterialCate = new MaterialCate();
		expectMaterialCate.setRestaurantId(restaurantId);
		expectMaterialCate.setName(MaterialCate.Type.GOOD.getText());
		expectMaterialCate.setType(MaterialCate.Type.GOOD);
		
		Map<Object, Object> params = new LinkedHashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.restaurant_id = " + restaurantId);
		MaterialCate actualMaterialCate = MaterialCateDao.getContent(params).get(0);
		
		assertEquals("material category name", expectMaterialCate.getName(), actualMaterialCate.getName());
		assertEquals("material restaurant id", expectMaterialCate.getRestaurantId(), actualMaterialCate.getRestaurantId());
		assertEquals("material category type", expectMaterialCate.getType().getValue(), actualMaterialCate.getType().getValue());
	}
	
	private void compareRestaurant(Restaurant expected, Restaurant actual){
		assertEquals("restaurant id", expected.getId(), actual.getId());
		assertEquals("restaurant account", expected.getAccount(), actual.getAccount());
		assertEquals("restaurant name", expected.getName(), actual.getName());
		assertEquals("restaurant info", expected.getInfo(), actual.getInfo());
		assertEquals("restaurant address", expected.getAddress(), actual.getAddress());
		assertEquals("restaurant 1st tele", expected.getTele1(), actual.getTele1());
		assertEquals("restaurant 2nd tele", expected.getTele2(), actual.getTele2());
		assertEquals("restaurant birth date", expected.getBirthDate(), actual.getBirthDate());
		assertEquals("restaurant expire date", expected.getExpireDate(), actual.getExpireDate());
		assertEquals("restaurant record alive", expected.getRecordAlive(), actual.getRecordAlive());
	}
}
