package com.wireless.test.db.client.member;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.client.MemberType.Attribute;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.SortedList;
import com.wireless.test.db.TestInit;

public class TestMemberTypeDao {
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
	}
	
	@Test
	public void testMemberType() throws SQLException, BusinessException{
		int memberTypeId = 0;
		try{
			List<Discount> discounts = DiscountDao.getAll(mStaff);
			
			//Test to insert a new member type
			MemberType.InsertBuilder builder = new MemberType.InsertBuilder(mStaff.getRestaurantId(), "测试会员类型", discounts.get(0))
															 .setAttribute(Attribute.POINT)
															 .setInitialPoint(100)
															 .setExchangeRate(1.1f)
															 .setDesc("测试描述")
															 .addDiscount(discounts.get(1));
			
			MemberType expected = builder.build();
			memberTypeId = MemberTypeDao.insert(mStaff, builder);
			expected.setId(memberTypeId);
			
			MemberType actual = MemberTypeDao.getMemberTypeById(mStaff, memberTypeId);
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
																   .addDiscount(discounts.get(0));
			MemberTypeDao.update(mStaff, updateBuilder);
			
			expected = updateBuilder.build();
			actual = MemberTypeDao.getMemberTypeById(mStaff, memberTypeId);
			compare(expected, actual);
						
		}finally{
			if(memberTypeId != 0){
				MemberTypeDao.deleteById(mStaff, memberTypeId);
				try{
					MemberTypeDao.getMemberTypeById(mStaff, memberTypeId);
					assertTrue("fail to delete member type", false);
				}catch(BusinessException ingored){}
				
				assertTrue("fail to delete discounts associated with this member type", DiscountDao.getByMemberType(mStaff, memberTypeId).isEmpty());
			}
		}
	}
}
