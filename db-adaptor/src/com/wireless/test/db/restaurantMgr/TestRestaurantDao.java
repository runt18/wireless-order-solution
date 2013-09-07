package com.wireless.test.db.restaurantMgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.inventoryMgr.MaterialCateDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
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
			
			//Compare the role & staff
			Staff staff = compareRoleAndStaff(restaurantId);
			
			//Compare the '商品' material category
			compareMaterialCate(restaurantId);
			
			//Compare the 默认的活动价格方案
			comparePricePlan(restaurantId);
			
			//Compare the '大牌', '中牌', '例牌' and popular tastes
			compareTastes(staff, restaurantId);
			
			//Compare the '无折扣'
			compareDiscount(staff, restaurantId);
			
			//Compare the kitchens ranged from 1 to 50
			compareKitchens(staff, restaurantId);
			
			//Compare the department ranged from 1 to 10
			compareDept(staff, restaurantId);
	
			//Compare the region ranged from 1 to 10
			compareRegion(staff, restaurantId);
			
			//Compare the table
			compareTable(staff, restaurantId);
			
			//Compare the popular cancel reason
			compareCancelReason(staff, restaurantId);
			
		}finally{
			RestaurantDao.deleteById(restaurantId);
		}
	}
	
	private void compareCancelReason(Staff staff, int restaurantId) throws SQLException{
		List<CancelReason> cancelReasons = CancelReasonDao.getReasons(staff, null, null);
		for(CancelReason.DefaultCR defCR : CancelReason.DefaultCR.values()){
			boolean isExist = false;
			for(CancelReason cr : cancelReasons){
				if(cr.getReason().equals(defCR.getReason())){
					isExist = true;
					break;
				}
			}
			assertTrue("lack of" + defCR.getReason(), isExist);
		}
	}
	
	private void compareTable(Staff staff, int restaurantId) throws SQLException{
		List<Table> tables = TableDao.getTables(staff, null, null);
		for(Table t : tables){
			if(t.getAliasId() == 1 || 
			   t.getAliasId() == 2 ||
			   t.getAliasId() == 3 ||
			   t.getAliasId() == 5 ||
			   t.getAliasId() == 6 ||
			   t.getAliasId() == 7 ||
			   t.getAliasId() == 8 ||
			   t.getAliasId() == 9 ||
			   t.getAliasId() == 10){
				assertEquals("table associated restaurant id", t.getRestaurantId(), restaurantId);
				assertEquals("table associated region id", t.getRegion().getRegionId(), Region.RegionId.REGION_1.getId());
			}else{
				assertTrue(false);
			}
		}
	}
	
	private void compareRegion(Staff staff, int restaurantId) throws SQLException{
		List<Region> regions = RegionDao.getRegions(staff, null, null);
		for(Region r : regions){
			Region.RegionId regionId = Region.RegionId.valueOf(r.getRegionId());
			assertEquals("region name", r.getName(), regionId.getName());
			assertEquals("region associated restaurant id", r.getRestaurantId(), restaurantId);
		}
	}
	
	private void compareDept(Staff staff, int restaurantId) throws SQLException{
		List<Department> depts = DepartmentDao.getDepartments(staff, null, null);
		for(Department d : depts){
			Department.DeptId deptId = Department.DeptId.valueOf(d.getId());
			assertEquals("department id", d.getId(), deptId.getVal());
			assertEquals("department name", d.getName(), deptId.getDesc());
			assertEquals("department associated restaurant id", d.getRestaurantId(), restaurantId);
			assertEquals("department type", d.getType().getVal(), deptId.getType().getVal());
		}
	}
	
	private void compareKitchens(Staff staff, int restaurantId) throws SQLException{
		List<Kitchen> kitchens = KitchenDao.getKitchens(staff, null, null);
		for(Kitchen k : kitchens){
			Kitchen.KitchenAlias kitchenAlias = Kitchen.KitchenAlias.valueOf(k.getAliasId());
			assertEquals("deptId to kitchen", k.getDept().getId(), Department.DeptId.DEPT_1.getVal());
			assertEquals("kitchen alias", k.getAliasId(), kitchenAlias.getAliasId());
			assertEquals("kitchen name", k.getName(), kitchenAlias.getDesc());
			assertEquals("kitchen type", k.getType().getVal(), kitchenAlias.getType().getVal());
			assertEquals("kitchen associated restaurant", k.getRestaurantId(), restaurantId);
		}
	}
	
	private void compareDiscount(Staff staff, int restaurantId) throws SQLException{
		Discount expected = new Discount.NotDiscountBuilder(restaurantId).build();
		
		Discount actual = DiscountDao.getDiscount(staff, null, null).get(0);

		assertEquals("discount name", actual.getName(), expected.getName());
		assertEquals("discount restaurant id", actual.getRestaurantId(), expected.getRestaurantId());
		assertEquals("discount status", actual.getStatus().getVal(), expected.getStatus().getVal());
		
		for(DiscountPlan dp : actual.getPlans()){
			assertEquals("discount rate", dp.getRate(), 1, 0.01);
		}
	}
	
	private void compareTastes(Staff staff, int restaurantId) throws SQLException{
		List<Taste> tastes = TasteDao.getTastes(staff, null, null);
		for(Taste spec : tastes){
			if(spec.getPreference().equals(Taste.RegularInsertBuilder.PREF) ||
			   spec.getPreference().equals(Taste.MediumInsertBuilder.PREF)	||
			   spec.getPreference().equals(Taste.LargeInsertBuilder.PREF)){
				assertEquals(spec.getPreference() + "'s category", spec.getCategory().getVal(), Taste.Category.SPEC.getVal());
				assertEquals(spec.getPreference() + "'s calc type", spec.getCalc().getVal(), Taste.Calc.BY_RATE.getVal());
				assertEquals(spec.getPreference() + "'s type", spec.getType().getVal(), Taste.Type.RESERVED.getVal());
			}else{
				assertTrue(false);
			}
		}
	}
	
	private Staff compareRoleAndStaff(int restaurantId) throws SQLException, BusinessException{
		
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

		return expectedStaff;
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
