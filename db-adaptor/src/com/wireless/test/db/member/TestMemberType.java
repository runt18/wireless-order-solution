package com.wireless.test.db.member;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.member.MemberType.Attribute;
import com.wireless.pojo.menuMgr.PricePlan;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.SortedList;
import com.wireless.test.db.TestInit;

public class TestMemberType {
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getByRestaurant(37).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void compare(MemberType expected, MemberType actual){
		assertEquals("member type id", expected.getId(), actual.getId());
		assertEquals("member type name", expected.getName(), actual.getName());
		assertEquals("type to member type", expected.getType(), actual.getType());
		assertEquals("member type attribute", expected.getAttribute().getVal(), actual.getAttribute().getVal());
		assertEquals("member type charge rate", expected.getChargeRate(), actual.getChargeRate(), 0.01);
		assertEquals("member type exchange rate", expected.getExchangeRate(), actual.getExchangeRate(), 0.01);
		assertEquals("member type initial point", expected.getInitialPoint(), actual.getInitialPoint());
		assertEquals("member type desc", expected.getDesc(), actual.getDesc());
		assertEquals("member type default discount", expected.getDefaultDiscount(), actual.getDefaultDiscount());
		assertEquals("member type associated discounts", SortedList.newInstance(expected.getDiscounts()), SortedList.newInstance(actual.getDiscounts()));
		assertEquals("member type default price", expected.getDefaultPrice(), actual.getDefaultPrice());
		assertEquals("member type associated prices", SortedList.newInstance(expected.getPrices()), SortedList.newInstance(actual.getPrices()));
	}
	
	@Test
	public void testMemberType4Chain() throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		dbCon.connect();
		
		Restaurant group = RestaurantDao.getById(41);
		Staff groupStaff = StaffDao.getAdminByRestaurant(group.getId());
		final List<Discount> groupDiscounts = DiscountDao.getByCond(groupStaff, null, DiscountDao.ShowType.BY_KITCHEN);
		final List<PricePlan> groupPrices = PricePlanDao.getByCond(groupStaff, null);
		Restaurant branch = RestaurantDao.getById(40);
		Staff branchStaff = StaffDao.getAdminByRestaurant(branch.getId());
		final List<Discount> branchDiscounts = DiscountDao.getByCond(branchStaff, null, DiscountDao.ShowType.BY_KITCHEN);
		final List<PricePlan> branchPrices = PricePlanDao.getByCond(branchStaff, null);
		
		RestaurantDao.update(new Restaurant.UpdateBuilder(group.getId()).addBranch(branch));
		groupStaff = StaffDao.getById(groupStaff.getId());
		branchStaff = StaffDao.getById(branchStaff.getId());
		
		int groupMemberTypeId = 0;
		try{

			//Test to insert a new member type
			MemberType.Discount4Chain chainDiscount = new MemberType.Discount4Chain(branch, branchDiscounts.get(0)).addDiscount(branchDiscounts.get(1));
			MemberType.Price4Chain chainPrice = new MemberType.Price4Chain(branch, branchPrices.get(0)).addPrice(branchPrices.get(1));
							
			MemberType.InsertBuilder builder = new MemberType.InsertBuilder("测试会员类型", groupDiscounts.get(0))
															 .setAttribute(Attribute.POINT)
															 .setInitialPoint(100)
															 .setExchangeRate(1.1f)
															 .setDesc("测试描述")
															 .setDefaultPrice(groupPrices.get(0))
															 .addChainDiscount(chainDiscount)
															 .addChainPrice(chainPrice)
															 ;
			//---------------------Insert-----------------------------
			groupMemberTypeId = MemberTypeDao.insert(groupStaff, builder);

			//Test to get member type by group staff.
			MemberType groupMemberType = MemberTypeDao.getById(groupStaff, groupMemberTypeId);
			assertEquals("default chain discount", groupDiscounts.get(0), groupMemberType.getDefaultDiscount());
			assertEquals("size to chain discounts owned by group", builder.build().getDiscounts().size(), groupMemberType.getDiscounts().size());
			assertEquals("default chain price", groupPrices.get(0), groupMemberType.getDefaultPrice());
			assertEquals("size to chain prices owned by group", builder.build().getPrices().size(), groupMemberType.getPrices().size());			
			//Test to get member type by branch staff.
			MemberType branchMemberType = MemberTypeDao.getById(branchStaff, groupMemberTypeId);
			assertEquals("default chain discount", chainDiscount.getDefaultDiscount(), branchMemberType.getDefaultDiscount());
			assertEquals("size to chain discounts owned by branch", chainDiscount.getDiscounts().size(), branchMemberType.getDiscounts().size());
			assertEquals("default chain price", chainPrice.getDefaultPrice(), branchMemberType.getDefaultPrice());
			assertEquals("size to chain discounts owned by branch", chainPrice.getPrices().size(), branchMemberType.getPrices().size());			
			
			//--------------Update-----------------------------------
			chainDiscount = new MemberType.Discount4Chain(branch.getId(), branchDiscounts.get(1)).addDiscount(branchDiscounts.get(0));
			chainPrice = new MemberType.Price4Chain(branch, branchPrices.get(1)).addPrice(branchPrices.get(0));
			MemberType.UpdateBuilder updateBuilder = new MemberType.UpdateBuilder(groupMemberTypeId).addChainDiscount(chainDiscount).addChainPrice(chainPrice);
			MemberTypeDao.update(groupStaff, updateBuilder);
			
			//Test to get member type by group staff.
			groupMemberType = MemberTypeDao.getById(groupStaff, groupMemberTypeId);
			assertEquals("default chain discount", groupDiscounts.get(0), groupMemberType.getDefaultDiscount());
			assertEquals("size to chain discounts owned by group", updateBuilder.build().getDiscounts().size(), groupMemberType.getDiscounts().size());
			assertEquals("default chain price", groupPrices.get(0), groupMemberType.getDefaultPrice());
			assertEquals("size to chain prices owned by group", updateBuilder.getChainPrices().size(), groupMemberType.getPrices().size());			
			//Test to get member type by branch staff.
			branchMemberType = MemberTypeDao.getById(branchStaff, groupMemberTypeId);
			assertEquals("default chain discount", branchDiscounts.get(1), branchMemberType.getDefaultDiscount());
			assertEquals("size to chain discount owned by branch", chainDiscount.getDiscounts().size(), branchMemberType.getPrices().size());	
			assertEquals("default chain price", branchPrices.get(1), branchMemberType.getDefaultPrice());
			assertEquals("size to chain prices owned by branch", chainPrice.getPrices().size(), branchMemberType.getPrices().size());	
			
		}finally{
			if(groupMemberTypeId != 0){
				MemberTypeDao.deleteById(groupStaff, groupMemberTypeId);
				try{
					MemberTypeDao.getById(mStaff, groupMemberTypeId);
					assertTrue("fail to delete group member type", false);
				}catch(BusinessException ingored){}
				
				assertTrue("fail to delete discounts associated with this group member type", DiscountDao.getByMemberType(groupStaff, new MemberType(groupMemberTypeId)).isEmpty());
				
				//Assure the cleanup the chain discount after member type removed. 
				String sql;
				sql = " SELECT COUNT(*) FROM " + Params.dbName + ".member_chain_discount WHERE group_member_type_id = " + groupMemberTypeId + " AND branch_id = " + branch.getId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					assertEquals("failed to delete chain discounts", 0, dbCon.rs.getInt(1));
				}
				dbCon.rs.close();
				
				//Assure the cleanup the chain prices after member type removed. 
				sql = " SELECT COUNT(*) FROM " + Params.dbName + ".member_chain_price WHERE group_member_type_id = " + groupMemberTypeId + " AND branch_id = " + branch.getId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					assertEquals("failed to delete chain prices", 0, dbCon.rs.getInt(1));
				}
				dbCon.rs.close();
			}
			
			RestaurantDao.update(new Restaurant.UpdateBuilder(group.getId()).clearBranch());
			
			dbCon.disconnect();
		}
	}
	
	@Test
	public void testMemberType() throws SQLException, BusinessException{
		int memberTypeId = 0;
		try{
			List<Discount> discounts = DiscountDao.getAll(mStaff);
			List<PricePlan> prices = PricePlanDao.getByCond(mStaff, null);
			
			//Test to insert a new member type
			MemberType.InsertBuilder builder = new MemberType.InsertBuilder("测试会员类型", discounts.get(0))
															 .setAttribute(Attribute.POINT)
															 .setInitialPoint(100)
															 .setExchangeRate(1.1f)
															 .setDesc("测试描述")
															 .addDiscount(discounts.get(1))
															 .addPrice(prices.get(0))
															 .setDefaultPrice(prices.get(0));
			
			MemberType expected = builder.build();
			memberTypeId = MemberTypeDao.insert(mStaff, builder);
			expected.setId(memberTypeId);
			
			MemberType actual = MemberTypeDao.getById(mStaff, memberTypeId);
			compare(expected, actual);
			
			//Test to update the member type just created
			MemberType.UpdateBuilder updateBuilder = new MemberType.UpdateBuilder(memberTypeId)
																   .setName("测试会员类型2")
																   .setAttribute(Attribute.CHARGE)
																   .setChargeRate(1.2f)
																   .setInitialPoint(101)
																   .setExchangeRate(0.4f)
																   .setDesc("修改描述")
																   .setDefaultDiscount(discounts.get(1))
																   .addDiscount(discounts.get(0))
																   .setDefaultPrice(prices.get(0));
			MemberTypeDao.update(mStaff, updateBuilder);
			
			expected = updateBuilder.build();
			actual = MemberTypeDao.getById(mStaff, memberTypeId);
			compare(expected, actual);
						
		}finally{
			if(memberTypeId != 0){
				MemberTypeDao.deleteById(mStaff, memberTypeId);
				try{
					MemberTypeDao.getById(mStaff, memberTypeId);
					assertTrue("fail to delete member type", false);
				}catch(BusinessException ingored){}
				
				assertTrue("fail to delete discounts associated with this member type", DiscountDao.getByMemberType(mStaff, new MemberType(memberTypeId)).isEmpty());
			}
		}
	}
}
