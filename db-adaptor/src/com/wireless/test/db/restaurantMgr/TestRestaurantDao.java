package com.wireless.test.db.restaurantMgr;

import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.member.MemberCondDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.member.represent.RepresentDao;
import com.wireless.db.printScheme.PrinterDao;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.serviceRate.ServicePlanDao;
import com.wireless.db.sms.SMStatDao;
import com.wireless.db.staffMgr.RoleDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.system.BusinessHourDao;
import com.wireless.db.system.SystemDao;
import com.wireless.db.weixin.restaurant.WxRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.pojo.member.MemberCond;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.restaurantMgr.Module;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.restaurantMgr.Restaurant.RecordAlive;
import com.wireless.pojo.serviceRate.ServicePlan;
import com.wireless.pojo.sms.SMStat;
import com.wireless.pojo.staffMgr.Role;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.BusinessHour;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.test.db.TestInit;

public class TestRestaurantDao {
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testChain() throws SQLException, BusinessException{
		Restaurant group = RestaurantDao.getById(40);
		Restaurant branch = RestaurantDao.getById(41);
		RestaurantDao.update(new Restaurant.UpdateBuilder(group.getId()).addBranch(branch));
		
		group = RestaurantDao.getById(group.getId());
		branch = RestaurantDao.getById(branch.getId());
		Assert.assertEquals("type to group restaurant", Restaurant.Type.GROUP, group.getType());
		Assert.assertEquals("branch to group restaurant", branch, group.getBranches().get(0));
		
		Assert.assertEquals("type to branch restaurant", Restaurant.Type.BRANCE, branch.getType());
		
		RestaurantDao.update(new Restaurant.UpdateBuilder(group.getId()).clearBranch());
		group = RestaurantDao.getById(group.getId());
		branch = RestaurantDao.getById(branch.getId());
		Assert.assertEquals("type to group restaurant after clearing branch", Restaurant.Type.RESTAURANT, group.getType());
		Assert.assertEquals("branch to group restaurant after clearing branch", false, group.hasBranches());
		
		Assert.assertEquals("type to branch restaurant after clearing branch", Restaurant.Type.RESTAURANT, branch.getType());
	}
	
	@Test 
	public void testCalcExpired() throws SQLException, BusinessException{
		System.out.println(RestaurantDao.calcExpired());
	}
	
	@Test
	public void testRestaurantDao() throws SQLException, ParseException, BusinessException, NoSuchAlgorithmException{
		int restaurantId = 0;
		try{
			Restaurant.InsertBuilder builder = new Restaurant.InsertBuilder("test_account", "测试餐厅", new SimpleDateFormat("yyyy-MM-dd").parse("2014-01-01").getTime(), "test@123")
															 .setAddress("测试地址")
															 .setRestaurantInfo("测试信息")
															 .setTele1("1333333333")
															 .setTele2("020-85473215")
															 .setDianpingId(1002);
			restaurantId = RestaurantDao.insert(builder);
			
			Restaurant expected = builder.build();
			expected.setId(restaurantId);
			
			Restaurant actual = RestaurantDao.getById(restaurantId);
			//Compare the basic info
			compareRestaurant(expected, actual);
			
			//Compare the role & staff
			Staff staff = compareRoleAndStaff(restaurantId);
			
			//Compare the '商品' material category
			//compareMaterialCate(restaurantId);
			
			//Compare the '大牌', '中牌', '例牌' and popular tastes
			compareTastes(staff, restaurantId);
			
			//Compare the '无折扣'
			compareDiscount(staff, restaurantId);
			
			//Compare the '免服务费'
			compareServicePlan(staff, restaurantId);
			
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
			
			//Compare the setting
			compareSetting(staff);
			
			//Compare the member type
			compareMemberType(staff);
			
			//Compare the printer
			comparePrinters(staff);
			
			//Compare the weixin restaurant
			compareWeixinRestaurant(staff);
			
			//Compare the SMS state
			compareSMStat(staff);
			
			//Compare the business hour
			compareBusinessHour(staff);
			
			//Compare the member condition
			compareMemberCond(staff);
			
			//Compare the represent
			compareRepresent(staff, restaurantId);
			
			//Update a restaurant
			Restaurant.UpdateBuilder updateBuilder = new Restaurant.UpdateBuilder(restaurantId)
																   .setAccount("test2")
														 		   .setPwd("test2@123")
														 		   .setRestaurantInfo("测试信息2")
														 		   .setAddress("测试地址2")
														 		   .setTele1("测试号码2")
														 		   .setTele2("测试号码2")
														 		   .setDianpingId(1003)
														 		   .setRecordAlive(RecordAlive.ONE_YEAR)
														 		   .setExpireDate(new SimpleDateFormat("yyyy-MM-dd").parse("2015-01-01").getTime())
														 		   .addModule(Module.Code.INVENTORY)
														 		   .addModule(Module.Code.MEMBER)
														 		   .resetRSA();
			
			RestaurantDao.update(updateBuilder);
			
			if(updateBuilder.isAccountChanged()){
				expected.setAccount(updateBuilder.build().getAccount());
			}
			if(updateBuilder.isTele2Changed()){
				expected.setTele2(updateBuilder.build().getTele2());
			}
			if(updateBuilder.isTele1Changed()){
				expected.setTele1(updateBuilder.build().getTele1());
			}
			if(updateBuilder.isDianpingIdChanged()){
				expected.setDianpingId(updateBuilder.build().getDianpingId());
			}
			if(updateBuilder.isRestaurantNameChanged()){
				expected.setName(updateBuilder.build().getName());
			}
			if(updateBuilder.isRestaurantInfoChanged()){
				expected.setInfo(updateBuilder.build().getInfo());
			}
			if(updateBuilder.isRecordAliveChanged()){
				expected.setRecordAlive(updateBuilder.build().getRecordAlive());
			}
			if(updateBuilder.isModuleChanged()){
				expected.setModule(updateBuilder.build().getModules());
			}
			if(updateBuilder.isExpireDateChanged()){
				expected.setExpireDate(updateBuilder.build().getExpireDate());
			}
			if(updateBuilder.isAddressChanged()){
				expected.setAddress(updateBuilder.build().getAddress());
			}
			if(updateBuilder.isAccountChanged()){
				expected.setAccount(updateBuilder.build().getAccount());
			}
			if(updateBuilder.isRSAChanged()){
				expected.setPublicKey(updateBuilder.build().getPublicKey());
				expected.setPrivateKey(updateBuilder.build().getPrivateKey());
			}
			
			if(updateBuilder.isBeeCloudChanged()){
				expected.setBeeCloudAppId(updateBuilder.build().getBeeCloudAppId());
				expected.setBeeCloudAppSecret(updateBuilder.build().getBeeCloudAppSecret());
			}
			
			actual = RestaurantDao.getById(restaurantId);
			compareRestaurant(expected, actual);
			
		}finally{
			RestaurantDao.deleteById(restaurantId);
		}
	}
	
	private void compareRepresent(Staff staff, int restaurantId) throws SQLException{
		Assert.assertTrue("failed to init represent", RepresentDao.getByCond(staff, new RepresentDao.ExtraCond()).size() == 1);
	}
	
	private void compareMemberCond(Staff staff) throws SQLException{
		for(MemberCond memberCond : MemberCondDao.getByCond(staff, null)){
			if(!memberCond.getName().equals("活跃会员") && !memberCond.getName().equals("沉睡会员")){
				Assert.assertTrue("business hour", false);
			}
		}
	}
	
	private void compareBusinessHour(Staff staff) throws SQLException{
		List<BusinessHour> actualHours = BusinessHourDao.getByCond(staff, null, null);
		for(BusinessHour hour : actualHours){
			if(!hour.getName().equals("早市") && 
			   !hour.getName().equals("午市") &&
			   !hour.getName().equals("晚市")){
				Assert.assertTrue("business hour", false);
			}
		}
		
	}
	
	private void compareSMStat(Staff staff) throws SQLException, BusinessException{
		SMStat actual = SMStatDao.get(staff);
		Assert.assertEquals("sms stat : restaurant", staff.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("sms stat : charge used", 0, actual.getChargeUsed());
		Assert.assertEquals("sms stat : consumption used", 0, actual.getConsumptionUsed());
		Assert.assertEquals("sms stat : verification used", 0, actual.getVerificationUsed());
		Assert.assertEquals("sms stat : total used", 0, actual.getTotalUsed());
		Assert.assertEquals("sms stat : remaining", 0, actual.getRemaining());
	}
	
	private void compareWeixinRestaurant(Staff staff) throws SQLException, BusinessException{
		WxRestaurant actual = WxRestaurantDao.get(staff);
		Assert.assertEquals("weixin restaurant : restaurant id", actual.getRestaurantId(), staff.getRestaurantId());
		Assert.assertEquals("weixin restaurant : info", actual.getWeixinInfo(), "");
		Assert.assertEquals("weixin restaurant : logo", actual.getWeixinLogo(), null);
		Assert.assertEquals("weixin restaurant : status", actual.getStatus(), WxRestaurant.Status.CREATED);
	}
	
	private void comparePrinters(Staff staff) throws SQLException{
		Assert.assertEquals("failed to insert 10 printers", 10, PrinterDao.getByCond(staff, null).size());
	}
	
	private void compareMemberType(Staff staff) throws SQLException{
		try{
			MemberTypeDao.getWxMemberType(staff);
		}catch(BusinessException e){
			Assert.assertTrue("failed to insert a weixin member type", false);
		}
	}
	
	private void compareSetting(Staff staff) throws SQLException, BusinessException{
		Assert.assertEquals("setting", SystemDao.getByCond(staff, null).get(0).getSetting().getRestaurantId(), staff.getRestaurantId());
	}
	
	private void compareCancelReason(Staff staff, int restaurantId) throws SQLException{
		List<CancelReason> cancelReasons = CancelReasonDao.getByCond(staff, null, null);
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
		List<Table> tables = TableDao.getByCond(staff, null, null);
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
				Assert.assertEquals("table associated restaurant id", t.getRestaurantId(), restaurantId);
				Assert.assertEquals("table associated region id", t.getRegion().getId(), Region.RegionId.REGION_1.getId());
			}else{
				Assert.assertTrue(false);
			}
		}
	}
	
	private void compareRegion(Staff staff, int restaurantId) throws SQLException{
		List<Region> regions = RegionDao.getByCond(staff, null, null);
		for(Region r : regions){
			Region.RegionId regionId = Region.RegionId.valueOf(r.getId());
			Assert.assertEquals("region associated restaurant id", r.getRestaurantId(), restaurantId);
			if(regionId == Region.RegionId.REGION_1 || regionId == Region.RegionId.REGION_2 || regionId == Region.RegionId.REGION_3){
				Assert.assertEquals("region status", r.getStatus(), Region.Status.BUSY);
			}else{
				Assert.assertEquals("region name", r.getName(), regionId.getName());
				Assert.assertEquals("region status", r.getStatus(), Region.Status.IDLE);
			}
		}
	}
	
	private void compareDept(Staff staff, int restaurantId) throws SQLException, BusinessException{
		for(Department dept : DepartmentDao.getByCond(staff, new DepartmentDao.ExtraCond().addType(Department.Type.NORMAL).addType(Department.Type.IDLE), null)){
			Kitchen feastKitchen = KitchenDao.getByCond(staff, new KitchenDao.ExtraCond().setDeptId(dept.getId()).setType(Kitchen.Type.FEAST), null).get(0);
			Assert.assertEquals("restaurant : associated feast kitchen", staff.getRestaurantId(), feastKitchen.getRestaurantId());
			Assert.assertEquals("name : associated feast kitchen", dept.getName() + "酒席费", feastKitchen.getName());
			Assert.assertEquals("display : associated feast kitchen", 0, feastKitchen.getDisplayId());
		}
		
		Department deptToTemp = DepartmentDao.getByType(staff, Department.Type.TEMP).get(0);
		Assert.assertEquals("department id", deptToTemp.getId(), Department.DeptId.DEPT_TMP.getVal());
		Assert.assertEquals("department name", deptToTemp.getName(), Department.DeptId.DEPT_TMP.getDesc());
		Assert.assertEquals("department associated restaurant id", deptToTemp.getRestaurantId(), restaurantId);
		Assert.assertEquals("department type", deptToTemp.getType(), Department.Type.TEMP);
		Assert.assertEquals("display id", deptToTemp.getDisplayId(), 0);

		Department deptToNull = DepartmentDao.getByType(staff, Department.Type.NULL).get(0);
		Assert.assertEquals("department id", deptToNull.getId(), Department.DeptId.DEPT_NULL.getVal());
		Assert.assertEquals("department name", deptToNull.getName(), Department.DeptId.DEPT_NULL.getDesc());
		Assert.assertEquals("department associated restaurant id", deptToNull.getRestaurantId(), restaurantId);
		Assert.assertEquals("department type", deptToNull.getType(), Department.Type.NULL);
		Assert.assertEquals("display id", deptToNull.getDisplayId(), 0);

		Department deptToWare = DepartmentDao.getByType(staff, Department.Type.WARE_HOUSE).get(0);
		Assert.assertEquals("department id", deptToWare.getId(), Department.DeptId.DEPT_WAREHOUSE.getVal());
		Assert.assertEquals("department name", deptToWare.getName(), Department.DeptId.DEPT_WAREHOUSE.getDesc());
		Assert.assertEquals("department associated restaurant id", deptToNull.getRestaurantId(), restaurantId);
		Assert.assertEquals("department type", deptToWare.getType(), Department.Type.WARE_HOUSE);
		Assert.assertEquals("display id", deptToWare.getDisplayId(), 0);
		
	}
	
	private void compareKitchens(Staff staff, int restaurantId) throws SQLException{
		Kitchen kitchenToNull = KitchenDao.getByType(staff, Kitchen.Type.NULL).get(0);
		Assert.assertEquals("deptId to kitchen", kitchenToNull.getDept().getId(), Department.DeptId.DEPT_NULL.getVal());
		Assert.assertEquals("kitchen name", kitchenToNull.getName(), "空厨房");
		Assert.assertEquals("kitchen type", kitchenToNull.getType(), Kitchen.Type.NULL);
		Assert.assertEquals("kitchen associated restaurant", kitchenToNull.getRestaurantId(), restaurantId);
		Assert.assertEquals("display id", kitchenToNull.getDisplayId(), 0);
		
		Kitchen kitchenToTemp = KitchenDao.getByType(staff, Kitchen.Type.TEMP).get(0);
		Assert.assertEquals("deptId to kitchen", kitchenToTemp.getDept().getId(), Department.DeptId.DEPT_TMP.getVal());
		Assert.assertEquals("kitchen name", kitchenToTemp.getName(), "临时厨房");
		Assert.assertEquals("kitchen type", kitchenToTemp.getType(), Kitchen.Type.TEMP);
		Assert.assertEquals("kitchen associated restaurant", kitchenToTemp.getRestaurantId(), restaurantId);
		Assert.assertEquals("display id", kitchenToTemp.getDisplayId(), 0);
	}
	
	private void compareServicePlan(Staff staff, int restaurantId) throws SQLException{
		ServicePlan actual = ServicePlanDao.getAll(staff).get(0);
		Assert.assertEquals("service plan name", "免服务费", actual.getName());
		Assert.assertEquals("service plan restaurant", restaurantId, actual.getRestaurantId());
		Assert.assertEquals("service plan type", ServicePlan.Type.RESERVED, actual.getType());
		Assert.assertEquals("service plan status", ServicePlan.Status.DEFAULT, actual.getStatus());

	}
	
	private void compareDiscount(Staff staff, int restaurantId) throws SQLException{
		Discount expected = Discount.EMPTY;
		
		Discount actual = DiscountDao.getAll(staff).get(0);

		Assert.assertEquals("discount name", actual.getName(), expected.getName());
		Assert.assertEquals("discount restaurant id", actual.getRestaurantId(), staff.getRestaurantId());
		Assert.assertEquals("discount status", actual.getStatus().getVal(), expected.getStatus().getVal());
		
		for(DiscountPlan dp : actual.getPlans()){
			Assert.assertEquals("discount rate", dp.getRate(), 1, 0.01);
		}
	}
	
	private void compareTastes(Staff staff, int restaurantId) throws SQLException, BusinessException{
//		TasteCategory specCategory = TasteCategoryDao.get(staff).get(0);
//		assertEquals("the restaurant id to spec category", specCategory.getRestaurantId(), restaurantId);
//		assertEquals("the name to spec category", specCategory.getName(), TasteCategory.SpecInsertBuilder.NAME);
//		assertEquals("the type to spec category", specCategory.getType(), TasteCategory.Type.RESERVED);
//		assertEquals("the status to spec category", specCategory.getStatus(), TasteCategory.Status.SPEC);
//		
//		TasteCategory tasteCategory = TasteCategoryDao.get(staff).get(1);
//		assertEquals("the restaurant id to spec category", tasteCategory.getRestaurantId(), restaurantId);
//		assertEquals("the name to spec category", tasteCategory.getName(), "口味");
//		assertEquals("the type to spec category", tasteCategory.getType(), TasteCategory.Type.NORMAL);
//		assertEquals("the status to spec category", tasteCategory.getStatus(), TasteCategory.Status.TASTE);
		
//		List<Taste> tastes = TasteDao.getTastes(staff, null, null);
//		for(Taste spec : tastes){
//			if(spec.isSpec()){
//				if(spec.getPreference().equals(Taste.RegularInsertBuilder.PREF) ||
//				   spec.getPreference().equals(Taste.MediumInsertBuilder.PREF)	||
//				   spec.getPreference().equals(Taste.LargeInsertBuilder.PREF)){
//					assertEquals(spec.getPreference() + "'s category", spec.getCategory().getId(), specCategory.getId());
//					assertEquals(spec.getPreference() + "'s calc type", spec.getCalc().getVal(), Taste.Calc.BY_RATE.getVal());
//					assertEquals(spec.getPreference() + "'s type", spec.getType().getVal(), Taste.Type.RESERVED.getVal());
//				}else{
//					assertTrue(false);
//				}
//			}
//		}
	}
	
	private Staff compareRoleAndStaff(int restaurantId) throws SQLException, BusinessException{
		
		Staff expectedStaff = StaffDao.getByRestaurant(restaurantId).get(0);
		Assert.assertEquals("admin staff restaurant id", expectedStaff.getRestaurantId(), restaurantId);
		Assert.assertEquals("admin staff type", expectedStaff.getType().getVal(), Staff.Type.RESERVED.getVal());
		Assert.assertEquals("admin staff role type", expectedStaff.getRole().getCategory().getVal(), Role.Category.ADMIN.getVal());
		Assert.assertEquals("admin staff name", expectedStaff.getName(), Staff.AdminBuilder.ADMIN);
		
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
		Assert.assertEquals("role name", expectedRole.getName(), cate.getDesc());
		Assert.assertEquals("role category", expectedRole.getCategory().getVal(), cate.getVal());
		Assert.assertEquals("role restaurant id", expectedRole.getRestaurantId(), restaurantId);
		Assert.assertEquals("role category", expectedRole.getType().getVal(), type.getVal());
	}
	
//	private void compareMaterialCate(int restaurantId) throws SQLException{
//		MaterialCate expectMaterialCate = new MaterialCate();
//		expectMaterialCate.setRestaurantId(restaurantId);
//		expectMaterialCate.setName(MaterialCate.Type.GOOD.getText());
//		expectMaterialCate.setType(MaterialCate.Type.GOOD);
//		
//		Map<Object, Object> params = new LinkedHashMap<Object, Object>();
//		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MC.restaurant_id = " + restaurantId);
//		MaterialCate actualMaterialCate = MaterialCateDao.getContent(params).get(0);
//		
//		Assert.assertEquals("material category name", expectMaterialCate.getName(), actualMaterialCate.getName());
//		Assert.assertEquals("material restaurant id", expectMaterialCate.getRestaurantId(), actualMaterialCate.getRestaurantId());
//		Assert.assertEquals("material category type", expectMaterialCate.getType().getValue(), actualMaterialCate.getType().getValue());
//	}
	
	private void compareRestaurant(Restaurant expected, Restaurant actual){
		Assert.assertEquals("restaurant id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurant account", expected.getAccount(), actual.getAccount());
		Assert.assertEquals("restaurant name", expected.getName(), actual.getName());
		Assert.assertEquals("restaurant info", expected.getInfo(), actual.getInfo());
		Assert.assertEquals("restaurant address", expected.getAddress(), actual.getAddress());
		Assert.assertEquals("restaurant 1st tele", expected.getTele1(), actual.getTele1());
		Assert.assertEquals("restaurant 2nd tele", expected.getTele2(), actual.getTele2());
		Assert.assertEquals("restaurant birth date", expected.getBirthDate(), actual.getBirthDate());
		Assert.assertEquals("restaurant expire date", expected.getExpireDate(), actual.getExpireDate());
		Assert.assertEquals("restaurant record alive", expected.getRecordAlive(), actual.getRecordAlive());
		Assert.assertEquals("resturant dianping id", expected.getDianpingId(), actual.getDianpingId());
		Assert.assertEquals("restaurant rsa public key", expected.getPublicKey(), actual.getPublicKey());
		Assert.assertEquals("restaurant rsa private key", expected.getPrivateKey(), actual.getPrivateKey());
		Assert.assertEquals("restaurant bee cloud app id", expected.getBeeCloudAppId(), actual.getBeeCloudAppId());
		Assert.assertEquals("restaurant bee cloud app secret", expected.getBeeCloudAppSecret(), actual.getBeeCloudAppSecret());
		Assert.assertEquals("init modules", expected.getModules(), actual.getModules());
	}
}
