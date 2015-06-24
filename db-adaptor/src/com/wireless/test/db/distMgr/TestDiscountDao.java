package com.wireless.test.db.distMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.distMgr.DiscountDao.ShowType;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DiscountError;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.distMgr.DiscountPlan;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestDiscountDao {
	
	private final static Comparator<DiscountPlan> PLAN_COMP = new Comparator<DiscountPlan>(){
		@Override
		public int compare(DiscountPlan dp0, DiscountPlan dp1) {
			if(dp0.getKitchen().getId() < dp1.getKitchen().getId()){
				return -1;
			}else if(dp0.getKitchen().getId() > dp1.getKitchen().getId()){
				return 1;
			}else{
				return 0;
			}
		}
	};
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getAdminByRestaurant(37);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDiscountDao() throws BusinessException, SQLException{
		int discountId = 0;
		try{
			final float rate = 0.88f;
			Discount.InsertBuilder insertBuilder = new Discount.InsertBuilder("测试折扣方案").setRate(rate);
			discountId = DiscountDao.insert(mStaff, insertBuilder);
			
			//Test to insert a new discount
			Discount expected = insertBuilder.build();
			expected.setId(discountId);
			
			List<Kitchen> kitchens = KitchenDao.getByType(mStaff, Kitchen.Type.NORMAL);
			for(Kitchen k : kitchens){
				expected.addPlan(new DiscountPlan(k, rate));
			}
			
			compareDiscount(expected, DiscountDao.getById(mStaff, discountId, ShowType.BY_PLAN), "insert discount", ShowType.BY_PLAN);
			compareDiscount(expected, DiscountDao.getById(mStaff, discountId, ShowType.BY_KITCHEN), "insert discount", ShowType.BY_KITCHEN);
			
			//Test to update some discount plans
			DiscountPlan dp1 = expected.getPlans().get(0);
			dp1.setRate(1f);
			DiscountPlan dp2 = expected.getPlans().get(1);
			dp2.setRate(0.75f);
			Discount.UpdatePlanBuilder planBuilder = new Discount.UpdatePlanBuilder(discountId)
																 .add(dp1.getKitchen(), dp1.getRate())
																 .add(dp2.getKitchen(), dp2.getRate());
			DiscountDao.updatePlan(mStaff, planBuilder);
			compareDiscount(expected, DiscountDao.getById(mStaff, discountId, ShowType.BY_PLAN), "update plan", ShowType.BY_PLAN);
			compareDiscount(expected, DiscountDao.getById(mStaff, discountId, ShowType.BY_KITCHEN), "update plan", ShowType.BY_KITCHEN);
			
			//Test to update the discount
			Discount.UpdateBuilder updateBuilder = new Discount.UpdateBuilder(discountId).setName("修改折扣方案").setDefault();
			DiscountDao.update(mStaff, updateBuilder);
			expected.setName(updateBuilder.build().getName());
			expected.setStatus(updateBuilder.build().getStatus());
			
			compareDiscount(expected, DiscountDao.getById(mStaff, discountId, ShowType.BY_PLAN), "update discount", ShowType.BY_PLAN);
			compareDiscount(expected, DiscountDao.getById(mStaff, discountId, ShowType.BY_KITCHEN), "update discount", ShowType.BY_KITCHEN);
			
		}finally{ 
			if(discountId != 0){
				DiscountDao.delete(mStaff, discountId);
				try{
					DiscountDao.getById(mStaff, discountId);
					Assert.assertTrue("failed to delete discount", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete discount", DiscountError.DISCOUNT_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compareDiscount(Discount expected, Discount actual, final String tag, ShowType showType){
		Assert.assertEquals("id : " + tag, expected.getId(), actual.getId());
		Assert.assertEquals("restaurant : " + tag, mStaff.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("name : " + tag, expected.getName(), actual.getName());
		Assert.assertEquals("status : " + tag, expected.getStatus(), actual.getStatus());
		Assert.assertEquals("type : " + tag, expected.getType(), actual.getType());
		
		List<DiscountPlan> expectedPlans = new ArrayList<DiscountPlan>(expected.getPlans());
		Iterator<DiscountPlan> iter = expectedPlans.iterator();
		if(showType == ShowType.BY_PLAN){
			while(iter.hasNext()){
				if(iter.next().getRate() == 1){
					iter.remove();
				}
			}
		}
		Collections.sort(expectedPlans, PLAN_COMP);
		
		List<DiscountPlan> actualPlans = new ArrayList<DiscountPlan>(actual.getPlans());
		Collections.sort(actualPlans, PLAN_COMP);

		for(int i = 0; i < expectedPlans.size(); i++){
			Assert.assertEquals("rate[" + i + "] : " + tag, expectedPlans.get(i).getRate(), actualPlans.get(i).getRate(), 0.01);
			Assert.assertEquals("kitchen[" + i + "]" + tag, expectedPlans.get(i).getKitchen(), actualPlans.get(i).getKitchen());
		}
	}
	
}
