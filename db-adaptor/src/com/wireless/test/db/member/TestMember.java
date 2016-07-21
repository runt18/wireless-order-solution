package com.wireless.test.db.member;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.http.client.ClientProtocolException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberDao.ActiveExtraCond;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.Member.AdjustType;
import com.wireless.pojo.member.Member.Sex;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.test.db.TestInit;

public class TestMember {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getAdminByRestaurant(40);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void compareMember(Member expected, Member actual){
		assertEquals("member id", expected.getId(), actual.getId());
		assertEquals("member card", expected.getMemberCard(), actual.getMemberCard());
		assertEquals("member_name", expected.getName(), actual.getName());
		assertEquals("member mobile", expected.getMobile(), actual.getMobile());
		assertEquals("member type", expected.getMemberType(), actual.getMemberType());
		assertEquals("associated restaurant id", expected.getRestaurantId(), actual.getRestaurantId());
		assertEquals("member consumption amount", expected.getConsumptionAmount(),  actual.getConsumptionAmount());
		assertEquals("member used balance", expected.getUsedBalance(), actual.getUsedBalance(), 0.01);
		assertEquals("member base balance", expected.getBaseBalance(), actual.getBaseBalance(), 0.01);
		assertEquals("member extra balance", expected.getExtraBalance(), actual.getExtraBalance(), 0.01);
		assertEquals("member point", expected.getPoint(), actual.getPoint());
		assertEquals("member used point", expected.getUsedPoint(), actual.getUsedPoint());
		assertEquals("member total consumption", expected.getTotalConsumption(), actual.getTotalConsumption(), 0.01);
		assertEquals("member total point", expected.getTotalPoint(), actual.getTotalPoint(), 0.01);
		assertEquals("member total charge", expected.getTotalCharge(), actual.getTotalCharge(), 0.01);
		assertEquals("member tele", expected.getTele(), actual.getTele());
		assertEquals("member sex", expected.getSex(), actual.getSex());
		//assertTrue("member create date", System.currentTimeMillis() - actual.getCreateDate() < 5000);
		assertEquals("member id card", expected.getIdCard(), actual.getIdCard());
		assertEquals("member age", expected.getAge(), actual.getAge());
		assertEquals("member birthday", expected.getBirthday(), actual.getBirthday());
		assertEquals("member company", expected.getCompany(), actual.getCompany());
		assertEquals("member contact address", expected.getContactAddress(), actual.getContactAddress());
		assertEquals("member referrer name", expected.getReferrer(), actual.getReferrer());
		assertEquals("member referrer id", expected.getReferrerId(), actual.getReferrerId());
		
	}
	
	private void compareMemberOperation(MemberOperation expected, MemberOperation actual){
		assertEquals("mo - id", expected.getId(), actual.getId());
		assertEquals("mo - associated restaurant id", expected.getRestaurantId(), actual.getRestaurantId());
		assertEquals("mo - associated branch id", expected.getBranchId(), actual.getBranchId());
		assertEquals("mo - staff id", expected.getStaffId(), actual.getStaffId());
		assertEquals("mo - member name", expected.getMemberName(), actual.getMemberName());
		assertEquals("mo - member mobile", expected.getMemberMobile(), actual.getMemberMobile());
		assertEquals("mo - member id", expected.getMemberId(), actual.getMemberId());
		assertEquals("mo - member card", expected.getMemberCard(), actual.getMemberCard());
		assertEquals("mo - operation seq", expected.getOperateSeq(), actual.getOperateSeq());
		//assertEquals("mo - operation date", expected.getOperateDate(), actual.getOperateDate());
		assertEquals("mo - operation type", expected.getOperationType(), expected.getOperationType());
		assertEquals("mo - consume money", expected.getPayMoney(), actual.getPayMoney(), 0.01);
		assertEquals("mo - charge type", expected.getChargeType(), actual.getChargeType());
		assertEquals("mo - charge balance", expected.getChargeMoney(), actual.getChargeMoney(), 0.01);
		assertEquals("mo - delta base balance", expected.getDeltaBaseMoney(), actual.getDeltaBaseMoney(), 0.01);
		assertEquals("mo - delta extra balance", expected.getDeltaExtraMoney(), actual.getDeltaExtraMoney(), 0.01);
		assertEquals("mo - delta point", expected.getDeltaPoint(), actual.getDeltaPoint());
		assertEquals("mo - remaining base balance", expected.getRemainingBaseMoney(), actual.getRemainingBaseMoney(), 0.01);
		assertEquals("mo - remaining extra balance", expected.getRemainingExtraMoney(), actual.getRemainingExtraMoney(), 0.01);
		assertEquals("mo - remaining point", expected.getRemainingPoint(), actual.getRemainingPoint());
		assertEquals("mo - coupon id", expected.getCouponId(), actual.getCouponId());
		assertEquals("mo - coupon money", expected.getCouponMoney(), actual.getCouponMoney(), 0.01);
		assertEquals("mo - coupon name", expected.getCouponName(), actual.getCouponName());
	}
	
	@Test
	public void testMemberChain() throws BusinessException, SQLException, ClientProtocolException, IOException{
		Restaurant group = RestaurantDao.getById(40);
		Staff groupStaff = StaffDao.getAdminByRestaurant(group.getId());
		Restaurant branch = RestaurantDao.getById(41);
		Staff branchStaff = StaffDao.getAdminByRestaurant(branch.getId());
		
		RestaurantDao.update(new Restaurant.UpdateBuilder(group.getId()).addBranch(branch));
		groupStaff = StaffDao.getById(groupStaff.getId());
		branchStaff = StaffDao.getById(branchStaff.getId());
		
		MemberType memberType = MemberTypeDao.getWxMemberType(branchStaff);
		int memberId = 0;
		
		try{
			//Insert a new member
			Member.InsertBuilder builder = new Member.InsertBuilder("张三", memberType.getId())
															   .setMobile("13794260531")
													 		   .setSex(Sex.FEMALE)
													 		   .setBirthday(DateUtil.parseDate("1981-03-15"))
													 		   .setCompany("Digie Co.,Ltd")
													 		   .setContactAddr("广州市东圃镇晨晖商务大厦")
													 		   .setIdCard("440711198103154818")
													 		   .setMemberCard("100010003")
													 		   .setTele("020-87453214")
													 		   ;
			
			//Test to insert member by branch.
			memberId = MemberDao.insert(branchStaff, builder);
			
			Member actual = MemberDao.getById(branchStaff, memberId);
			assertEquals("id to member get by branch", memberId, actual.getId());
			assertEquals("group id to member get by branch", groupStaff.getRestaurantId(), actual.getRestaurantId());
			assertEquals("branch id to member get by branch", branchStaff.getRestaurantId(), actual.getBranchId());

			actual = MemberDao.getById(groupStaff, memberId);
			assertEquals("id to member get by group", memberId, actual.getId());
			assertEquals("group id to member get by group", groupStaff.getRestaurantId(), actual.getRestaurantId());
			assertEquals("branch id to member get by group", branchStaff.getRestaurantId(), actual.getBranchId());
			
			//Test to update member by branch
			MemberDao.update(branchStaff, new Member.UpdateBuilder(memberId).setName("王五"));
			actual = MemberDao.getById(branchStaff, memberId);
			assertEquals("id to member get by branch", memberId, actual.getId());
			assertEquals("name to member get by branch", "王五", actual.getName());

			actual = MemberDao.getById(groupStaff, memberId);
			assertEquals("id to member get by group", memberId, actual.getId());
			assertEquals("name to member get by group", "王五", actual.getName());
			
			//Test charge 4 chain
			testCharge4Chain(groupStaff, branchStaff, actual);
			testCharge4Chain(branchStaff, groupStaff, actual);
			
			//Test consume 4 chain
			testConsume4Chain(groupStaff, branchStaff, actual);
			testConsume4Chain(branchStaff, branchStaff, actual);
			
			//Test adjust point 4 chain
			testAdjustPoint4Chain(groupStaff, branchStaff, actual);
			testAdjustPoint4Chain(branchStaff, groupStaff, actual);
			
			//Test point consume 4 chain
			testPointConsume4Chain(groupStaff, branchStaff, actual);
			testPointConsume4Chain(branchStaff, groupStaff, actual);
			
			//Test refund 4 chain
			testRefund4Chain(groupStaff, branchStaff, actual);
			testRefund4Chain(branchStaff, groupStaff, actual);
			
		}finally{
			if(memberId != 0){
				//Delete the member 		
				MemberDao.deleteById(branchStaff, memberId);
				//Check to see whether the member is deleted
				try{
					MemberDao.getById(branchStaff, memberId);
					assertTrue("failed to delete member", false);
				}catch(BusinessException ignored){
					
				}
			}
			
			RestaurantDao.update(new Restaurant.UpdateBuilder(group.getId()).clearBranch());
		}
	}
	
	private void testCharge4Chain(Staff groupStaff, Staff branchStaff, Member expected) throws BusinessException, SQLException{
		//Test to charge by branch staff.
		MemberOperation mo = MemberDao.charge(branchStaff, expected.getId(), 100, 120, ChargeType.CASH);
		expected.charge(100, 120, ChargeType.CASH);
		
		Member actual = MemberDao.getById(branchStaff, expected.getId());
		compareMember(expected, actual);
		compareMemberOperation(mo, MemberOperationDao.getById(branchStaff, DateType.TODAY, mo.getId()));
		
		actual = MemberDao.getById(groupStaff, expected.getId());
		compareMember(expected, actual);
		compareMemberOperation(mo, MemberOperationDao.getById(groupStaff, DateType.TODAY, mo.getId()));
	}
	
	private void testConsume4Chain(Staff groupStaff, Staff branchStaff, Member expected) throws BusinessException, SQLException{
		MemberDao.charge(branchStaff, expected.getId(), 100, 120, ChargeType.CASH);
		expected.charge(100, 120, ChargeType.CASH);
		
		final int orderId = 10;
		
		//使用会员卡余额消费
		MemberOperation mo = MemberDao.consume(branchStaff, new Member.ConsumeBuilder(expected.getId(), orderId)
																	  .setPrice(PayType.MEMBER, 50));
		expected.consume(50, PayType.MEMBER);
		
		compareMember(expected, MemberDao.getById(branchStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(branchStaff, DateType.TODAY, mo.getId()));

		compareMember(expected, MemberDao.getById(groupStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(groupStaff, DateType.TODAY, mo.getId()));
		
		//使用现金消费
		mo = MemberDao.consume(branchStaff, new Member.ConsumeBuilder(expected.getId(), orderId)
													  .setPrice(PayType.CASH, 50));
		expected.consume(50, PayType.CASH);
		
		compareMember(expected, MemberDao.getById(branchStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(branchStaff, DateType.TODAY, mo.getId()));
		
		compareMember(expected, MemberDao.getById(groupStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(groupStaff, DateType.TODAY, mo.getId()));
		
		//使用会员卡余额反结账
		mo = MemberDao.reConsume(branchStaff, expected.getId(), 50, PayType.MEMBER, orderId);
		expected.reConsume(50, PayType.MEMBER);
		
		compareMember(expected, MemberDao.getById(branchStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(branchStaff, DateType.TODAY, mo.getId()));
		
		compareMember(expected, MemberDao.getById(groupStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(groupStaff, DateType.TODAY, mo.getId()));
		
		//使用现金反结账
		mo = MemberDao.reConsume(branchStaff, expected.getId(), 50, PayType.CASH, orderId);
		expected.reConsume(50, PayType.CASH);
		
		compareMember(expected, MemberDao.getById(branchStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(branchStaff, DateType.TODAY, mo.getId()));
		
		compareMember(expected, MemberDao.getById(groupStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(groupStaff, DateType.TODAY, mo.getId()));
	}
	
	private void testPointConsume4Chain(Staff groupStaff, Staff branchStaff, Member expected) throws SQLException, BusinessException{
//		MemberOperation mo = MemberDao.pointConsume(branchStaff, expected.getId(), 20);
//		expected.pointConsume(20);
//		
//		compareMember(expected, MemberDao.getById(branchStaff, expected.getId()));
//		compareMemberOperation(mo, MemberOperationDao.getById(branchStaff, DateType.TODAY, mo.getId()));
//		compareMember(expected, MemberDao.getById(groupStaff, expected.getId()));
//		compareMemberOperation(mo, MemberOperationDao.getById(groupStaff, DateType.TODAY, mo.getId()));
	}
	
	private void testAdjustPoint4Chain(Staff groupStaff, Staff branchStaff, Member expected) throws SQLException, BusinessException{
		MemberOperation mo = MemberDao.adjustPoint(branchStaff, expected.getId(), 20, AdjustType.INCREASE);
		expected.adjustPoint(20, AdjustType.INCREASE);
		
		compareMember(expected, MemberDao.getById(branchStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(branchStaff, DateType.TODAY, mo.getId()));
		compareMember(expected, MemberDao.getById(groupStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(groupStaff, DateType.TODAY, mo.getId()));
	}
	
	private void testRefund4Chain(Staff groupStaff, Staff branchStaff, Member expected) throws SQLException, BusinessException, ClientProtocolException, IOException{
		MemberOperation mo = MemberDao.refund(branchStaff, expected.getId(), 10, 10, null);
		expected.refund(10, 10);
		
		compareMember(expected, MemberDao.getById(branchStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(branchStaff, DateType.TODAY, mo.getId()));
		compareMember(expected, MemberDao.getById(groupStaff, expected.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(groupStaff, DateType.TODAY, mo.getId()));
	}
	
	@Test
	public void testMemberBasicOperation() throws BusinessException, SQLException, ClientProtocolException, IOException{
		
		MemberType memberType = MemberTypeDao.getWxMemberType(mStaff);
		int memberId = 0;
		
		try{
			
			Staff referrer = StaffDao.getByCond(mStaff, null).get(1);
			//Insert a new member
			Member.InsertBuilder builder = new Member.InsertBuilder("张三", memberType.getId())
															   .setMobile("13094260535")
													 		   .setSex(Sex.FEMALE)
													 		   .setBirthday(DateUtil.parseDate("1981-03-15"))
													 		   .setCompany("Digie Co.,Ltd")
													 		   .setContactAddr("广州市东圃镇晨晖商务大厦")
													 		   .setIdCard("440711198103154818")
													 		   .setMemberCard("800010000")
													 		   .setTele("020-87453214")
													 		   .setReferrer(referrer.getId())
													 		   .setAge(Member.Age.AGE_80);
			
			memberId = MemberDao.insert(mStaff, builder);
			
			Member expect = builder.build();
			expect.setId(memberId);
			expect.setRestaurantId(mStaff.getRestaurantId());
			expect.setMemberType(memberType);
			expect.setReferrer(referrer.getName());
			expect.setReferrerId(referrer.getId());
			expect.setPoint(memberType.getInitialPoint());
			//Set the initial point to expected member
			expect.setPoint(memberType.getInitialPoint());
			
			Member actual = MemberDao.getById(mStaff, memberId);
			
			//Compare the member just inserted
			compareMember(expect, actual);
			
			//Update the member just inserted
			referrer = StaffDao.getByCond(mStaff, null).get(2);
			Member.UpdateBuilder updateBuilder = new Member.UpdateBuilder(memberId)
														   .setName("李四")
														   .setMobile("18121590921")
														   .setMemberType(memberType.getId())
														   .setSex(Sex.MALE)
														   .setBirthday(DateUtil.parseDate("1987-06-29"))
														   .setCompany("DingDing Tech")
														   .setContactAddr("广州市萝岗区科学城")
														   .setIdCard("4101234789965412")
														   .setMemberCard("320010001")
														   .setTele("0750-3399559")
														   .setReferrer(referrer.getId())
														   .setAge(Member.Age.AGE_00);
			MemberDao.update(mStaff, updateBuilder);
			
			expect = updateBuilder.build();
			expect.setId(memberId);
			expect.setBranchId(mStaff.getRestaurantId());
			expect.setRestaurantId(mStaff.getRestaurantId());
			expect.setMemberType(memberType);
			expect.setReferrer(referrer.getName());
			expect.setReferrerId(referrer.getId());
			//Set the initial point to expected member
			expect.setPoint(memberType.getInitialPoint());
			
			actual = MemberDao.getById(mStaff, memberId);
			
			//Compare the member after update
			compareMember(expect, actual);
			
			//Perform to test charge
			testCharge(expect);
			
			//Perform to test consumption
			testConsume(expect);
			
			//Perform to test point adjust
			testAdjustPoint(expect);
			
			//Perform to test point consumption
			testPointConsume(expect);
			
			//Perform to test balance adjust
			testRefund(expect);
			
		}finally{
			if(memberId != 0){
				Member member = MemberDao.getById(mStaff, memberId);
				
				if(member.getBaseBalance() > 0){
					MemberDao.refund(mStaff, member.getId(), member.getBaseBalance(), member.getTotalBalance(), null);
				}
				
				//Delete the member 
				MemberDao.deleteById(mStaff, memberId);
				//Check to see whether the member is deleted
				try{
					MemberDao.getById(mStaff, memberId);
					assertTrue("failed to delete member", false);
				}catch(BusinessException ignored){}
				
				//Check to see whether the associated member operations are deleted
				//assertTrue("failed to delete today member operation", MemberOperationDao.getTodayByMemberId(mStaff, memberId).isEmpty());
				//assertTrue("failed to delete history member operation", MemberOperationDao.getHistoryByMemberId(mStaff, memberId).isEmpty());
				
				//Check to see whether the coupon associated with this member are deleted
				assertTrue("failed to delete the coupons associated with this member", CouponDao.getByCond(mStaff, new CouponDao.ExtraCond().setMember(memberId), null).isEmpty());
			}
		}

	}
	
	private void testCharge(Member expect) throws BusinessException, SQLException{
		MemberOperation mo = MemberDao.charge(mStaff, expect.getId(), 100, 120, ChargeType.CASH);
		expect.charge(100, 120, ChargeType.CASH);
		
		//CalcBillStatisticsDao.calcIncomeByCharge(mStaff, range, DateType.TODAY);
		
		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
	}
	
	private void testConsume(Member expect) throws BusinessException, SQLException{
		MemberDao.charge(mStaff, expect.getId(), 100, 120, ChargeType.CASH);
		expect.charge(100, 120, ChargeType.CASH);
		
		final int orderId = 10;
		
		//使用会员卡余额消费
		MemberOperation mo = MemberDao.consume(mStaff, new Member.ConsumeBuilder(expect.getId(), orderId)
															     .setPrice(PayType.MEMBER, 50));
		expect.consume(50, PayType.MEMBER);
		
		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
		
		//使用现金消费
		mo = MemberDao.consume(mStaff, new Member.ConsumeBuilder(expect.getId(), orderId)
			     								 .setPrice(PayType.CASH, 50));
		expect.consume(50, PayType.CASH);
		
		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
		
//		//使用会员卡余额反结账
//		expect.reConsume(50, PayType.MEMBER, mo);
//		mo = MemberDao.reConsume(mStaff, expect.getId(), 50, PayType.MEMBER, new Order(orderId));
//		
//		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
//		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
//		
//		//使用现金反结账
//		expect.reConsume(50, PayType.CASH, mo);
//		mo = MemberDao.reConsume(mStaff, expect.getId(), 50, PayType.CASH, new Order(orderId));
		
		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
	}
	
	private void testPointConsume(Member expect) throws SQLException, BusinessException{
//		MemberOperation mo = MemberDao.pointConsume(mStaff, expect.getId(), 20);
//		expect.pointConsume(20);
//		
//		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
//		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
	}
	
	private void testAdjustPoint(Member expect) throws SQLException, BusinessException{
		MemberOperation mo = MemberDao.adjustPoint(mStaff, expect.getId(), 20, AdjustType.INCREASE);
		expect.adjustPoint(20, AdjustType.INCREASE);
		
		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
	}
	
	private void testRefund(Member expect) throws SQLException, BusinessException, ClientProtocolException, IOException{
		MemberOperation mo = MemberDao.refund(mStaff, expect.getId(), 10, 10, null);
		expect.refund(10, 10);
		
		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
	}
	
	@Test
	public void testSearchActiveMember() throws SQLException, BusinessException{
		MemberDao.getByCond(mStaff, ActiveExtraCond.instance().setFuzzyName("张"), null);
	}
	
	@Test
	public void testCalcFavorFoods() throws SQLException{
		MemberDao.calcFavorFoods();
	}
	
	@Test
	public void testCalcRecommendFoods() throws SQLException{
		MemberDao.calcRecommendFoods();
	}
	
	@Test
	public void testMemberUpgrade() throws SQLException, BusinessException{
//		System.out.println(MemberDao.upgrade());
	}
}
