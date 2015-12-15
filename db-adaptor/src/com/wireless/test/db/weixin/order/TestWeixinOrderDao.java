package com.wireless.test.db.weixin.order;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.member.MemberDao;
import com.wireless.db.member.TakeoutAddressDao;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.order.WxOrderDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxOrderError;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.member.TakeoutAddress;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.order.WxOrder;
import com.wireless.test.db.TestInit;

public class TestWeixinOrderDao {
	
	private static Staff mStaff;
	private static final String WEIXIN_MEMBER_SERIAL = "oM02TjtmLtadFjiGtlUuxTFjJhno";

	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException{
		TestInit.init();
		mStaff = StaffDao.getAdminByRestaurant(40);
	}
	
	@Test
	public void testWeixinOrderDao() throws SQLException, BusinessException{
		int wxOrderId = 0;
		try{
			List<Food> foods = FoodDao.getByCond(mStaff, null, null);
			
			OrderFood of1 = new OrderFood(foods.get(0));
			of1.setCount(1);
			
			OrderFood of2 = new OrderFood(foods.get(1));
			of2.setCount(2);
			
			WxOrder.InsertBuilder4Inside insertBuilder = (WxOrder.InsertBuilder4Inside)new WxOrder.InsertBuilder4Inside(WEIXIN_MEMBER_SERIAL).add(of1).add(of2).setComment("测试备注");
			wxOrderId = WxOrderDao.insert(mStaff, insertBuilder);
			
			WxOrder expected = insertBuilder.build();
			WxOrder actual = WxOrderDao.getById(mStaff, wxOrderId);

			Assert.assertEquals("member to wx order", MemberDao.getByWxSerial(mStaff, WEIXIN_MEMBER_SERIAL), actual.getMember());
			Assert.assertEquals("restaurant to wx order", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("status to wx order", expected.getStatus(), actual.getStatus());
			Assert.assertEquals("type to wx order", expected.getType(), actual.getType());
			Assert.assertEquals("comment to wx order", expected.getComment(), actual.getComment());
			for(OrderFood of : expected.getFoods()){
				int index = actual.getFoods().indexOf(of);
				Assert.assertTrue(of.getName() + " does NOT contain in wx order", index >= 0);
				Assert.assertEquals(of.getName() + " 's count NOT equals in wx order", of.getCount(), actual.getFoods().get(index).getCount(), 0.01);
			}
			
		}finally{
			if(wxOrderId != 0){
				WxOrderDao.deleteById(mStaff, wxOrderId);
				try{
					WxOrderDao.getById(mStaff, wxOrderId);
					Assert.assertTrue("failed to delete the wx order", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the wx order", WxOrderError.WX_ORDER_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	@Test
	public void testWxTakeoutOrderDao() throws SQLException, BusinessException{
		int wxOrderId = 0;
		int addressId = 0;
		try{
			List<Food> foods = FoodDao.getByCond(mStaff, null, null);
			
			OrderFood of1 = new OrderFood(foods.get(0));
			of1.setCount(1);
			
			OrderFood of2 = new OrderFood(foods.get(1));
			of2.setCount(2);
			
			addressId = TakeoutAddressDao.insert(mStaff, new TakeoutAddress.InsertBuilder(MemberDao.getByWxSerial(mStaff, WEIXIN_MEMBER_SERIAL), "测试外卖地址", "18520590932", "Vincent"));
					
			WxOrder.InsertBuilder4Takeout insertBuilder = new WxOrder.InsertBuilder4Takeout(WEIXIN_MEMBER_SERIAL, TakeoutAddressDao.getById(mStaff, addressId)).add(of1).add(of2);
			wxOrderId = WxOrderDao.insert(mStaff, insertBuilder);
			
			WxOrder expected = insertBuilder.build();
			WxOrder actual = WxOrderDao.getById(mStaff, wxOrderId);
			actual.setTakoutAddress(TakeoutAddressDao.getById(mStaff, addressId));

			Assert.assertEquals("restaurant to wx order", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertEquals("status to wx order", expected.getStatus(), actual.getStatus());
			Assert.assertEquals("测试外卖地址", actual.getTakeoutAddress().getAddress());
			Assert.assertTrue("last used time to take-out address", Math.abs(System.currentTimeMillis() - actual.getTakeoutAddress().getLastUsed()) < 5000);
			Assert.assertEquals("type to wx order", expected.getType(), actual.getType());
			for(OrderFood of : expected.getFoods()){
				int index = actual.getFoods().indexOf(of);
				Assert.assertTrue(of.getName() + " does NOT contain in wx order", index >= 0);
				Assert.assertEquals(of.getName() + " 's count NOT equals in wx order", of.getCount(), actual.getFoods().get(index).getCount(), 0.01);
			}
			
		}finally{
			if(wxOrderId != 0){
				WxOrderDao.deleteById(mStaff, wxOrderId);
				try{
					WxOrderDao.getById(mStaff, wxOrderId);
					Assert.assertTrue("failed to delete the wx order", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the wx order", WxOrderError.WX_ORDER_NOT_EXIST, e.getErrCode());
				}
			}
			if(addressId != 0){
				TakeoutAddressDao.deleteById(mStaff, addressId);
			}
		}
	}
	
	@Test
	public void testCleanup() throws SQLException, BusinessException{
		System.out.println(WxOrderDao.cleanup());
	}
}
