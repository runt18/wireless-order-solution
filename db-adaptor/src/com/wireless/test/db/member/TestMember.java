package com.wireless.test.db.member;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.member.MemberCommentDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.member.MemberDao.ActiveExtraCond;
import com.wireless.db.member.MemberOperationDao;
import com.wireless.db.member.MemberTypeDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.Member.AdjustType;
import com.wireless.pojo.member.Member.Sex;
import com.wireless.pojo.member.MemberComment;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.member.MemberType;
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
		assertTrue("member create date", System.currentTimeMillis() - actual.getCreateDate() < 5000);
		assertEquals("member id card", expected.getIdCard(), actual.getIdCard());
		assertEquals("member birthday", expected.getBirthday(), actual.getBirthday());
		assertEquals("member company", expected.getCompany(), actual.getCompany());
		assertEquals("member contact address", expected.getContactAddress(), actual.getContactAddress());
		
		assertEquals("public comment - size", expected.getPublicComments().size(), actual.getPublicComments().size());
		for(int i = 0; i < expected.getPublicComments().size(); i++){
			assertEquals("public comment - staff id", expected.getPublicComments().get(i).getStaff().getId(), actual.getPublicComments().get(i).getStaff().getId());
			assertEquals("public comment - member id", expected.getPublicComments().get(i).getMember().getId(), actual.getPublicComments().get(i).getMember().getId());
			assertEquals("public comment - type", expected.getPublicComments().get(i).getType().getVal(), actual.getPublicComments().get(i).getType().getVal());
			assertEquals("public comment - comment", expected.getPublicComments().get(i).getComment(), actual.getPublicComments().get(i).getComment());
			assertEquals("public comment - last modified", Math.abs(expected.getPublicComments().get(i).getLastModified() - actual.getPublicComments().get(i).getLastModified()) / 5000, 0);
		}
		
		assertEquals("private comment", expected.hasPrivateComment(), actual.hasPrivateComment());
		if(expected.hasPrivateComment()){
			assertEquals("private comment - staff id", expected.getPrivateComment().getStaff().getId(), actual.getPrivateComment().getStaff().getId());
			assertEquals("private comment - member id", expected.getPrivateComment().getMember().getId(), actual.getPrivateComment().getMember().getId());
			assertEquals("private comment - type", expected.getPrivateComment().getType().getVal(), actual.getPrivateComment().getType().getVal());
			assertEquals("private comment - comment", expected.getPrivateComment().getComment(), actual.getPrivateComment().getComment());
			assertEquals("private comment - last modified", Math.abs(expected.getPrivateComment().getLastModified() - actual.getPrivateComment().getLastModified()) / 5000, 0);
		}
	}
	
	private void compareMemberOperation(MemberOperation expected, MemberOperation actual){
		assertEquals("mo - id", expected.getId(), actual.getId());
		assertEquals("mo - associated restaurant id", expected.getRestaurantId(), actual.getRestaurantId());
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
	public void testMemberBasicOperation() throws BusinessException, SQLException{
		
		MemberType memberType = MemberTypeDao.getWxMemberType(mStaff);
		int memberId = 0;
		
		try{
			
			//Insert a new member
			Member.InsertBuilder builder = Member.InsertBuilder.build4Mobile("张三", "13694260535", memberType.getId())
													 		   .setSex(Sex.FEMALE)
													 		   .setBirthday(DateUtil.parseDate("1981-03-15"))
													 		   .setCompany("Digie Co.,Ltd")
													 		   .setContactAddr("广州市东圃镇晨晖商务大厦")
													 		   .setIdCard("440711198103154818")
													 		   .setMemberCard("100010000")
													 		   .setPrivateComment("嫉妒咸鱼")
													 		   .setPublicComment("喜欢甜品")
													 		   .setTele("020-87453214");
			
			memberId = MemberDao.insert(mStaff, builder);
			
			//Commit a private comment to member just inserted
			MemberComment.CommitBuilder privateCommentBuilder = MemberComment.CommitBuilder.newPrivateBuilder(mStaff.getId(), memberId, "张老板好客，大方");
			
			MemberCommentDao.commit(mStaff, privateCommentBuilder);
			
			//Commit a public comment to member just inserted
			MemberComment.CommitBuilder publicCommentBuilder = MemberComment.CommitBuilder.newPublicBuilder(mStaff.getId(), memberId, "张老板是高富帅！！！");
			
			MemberCommentDao.commit(mStaff, publicCommentBuilder);
			
			Member expect = builder.build();
			expect.setId(memberId);
			expect.setRestaurantId(mStaff.getRestaurantId());
			expect.setMemberType(memberType);
			expect.setPoint(memberType.getInitialPoint());
			//Set the initial point to expected member
			expect.setPoint(memberType.getInitialPoint());
			//Set the private member comment
			MemberComment privateComment = privateCommentBuilder.build();
			privateComment.setLastModified(System.currentTimeMillis());
			expect.setPrivateComment(privateComment);
			//Set the last public member comment since the staff and member id NOT be set in insert builder
			expect.getPublicComments().get(expect.getPublicComments().size() - 1).setStaff(mStaff);
			expect.getPublicComments().get(expect.getPublicComments().size() - 1).setMember(new Member(memberId));
			expect.getPublicComments().get(expect.getPublicComments().size() - 1).setLastModified(System.currentTimeMillis());
			//Set the public member comment
			expect.addPublicComment(publicCommentBuilder.build());
			
			Member actual = MemberDao.getById(mStaff, memberId);
			
			//Compare the member just inserted
			compareMember(expect, actual);
			
			//Update the member just inserted
			Member.UpdateBuilder updateBuilder = new Member.UpdateBuilder(memberId)
														   .setName("李四")
														   .setMobile("18520590931")
														   .setMemberType(memberType.getId())
														   .setSex(Sex.MALE)
														   .setBirthday(DateUtil.parseDate("1987-06-29"))
														   .setCompany("DingDing Tech")
														   .setContactAddr("广州市萝岗区科学城")
														   .setIdCard("4101234789965412")
														   .setMemberCard("1000100001")
														   .setPrivateComment("咩都要")
														   .setPublicComment("垃圾桶")
														   .setTele("0750-3399559");
			MemberDao.update(mStaff, updateBuilder);
			
			//Commit a private comment to member just inserted
			privateCommentBuilder = MemberComment.CommitBuilder.newPrivateBuilder(mStaff.getId(), memberId, "老板小气。。。抠门");
			
			MemberCommentDao.commit(mStaff, privateCommentBuilder);
			
			//Commit a public comment to member just inserted
			publicCommentBuilder = MemberComment.CommitBuilder.newPublicBuilder(mStaff.getId(), memberId, "张老板是白富美！！！");
			
			MemberCommentDao.commit(mStaff, publicCommentBuilder);
			
			expect = updateBuilder.build();
			expect.setId(memberId);
			expect.setRestaurantId(mStaff.getRestaurantId());
			expect.setMemberType(memberType);
			//Set the initial point to expected member
			expect.setPoint(memberType.getInitialPoint());
			//Set the private member comment
			privateComment = privateCommentBuilder.build();
			privateComment.setLastModified(System.currentTimeMillis());
			expect.setPrivateComment(privateComment);
			//Set the last public member comment since the staff and member id NOT be set in insert builder
			expect.getPublicComments().get(expect.getPublicComments().size() - 1).setStaff(mStaff);
			expect.getPublicComments().get(expect.getPublicComments().size() - 1).setMember(new Member(memberId));
			expect.getPublicComments().get(expect.getPublicComments().size() - 1).setLastModified(System.currentTimeMillis());
			//Set the public member comment
			expect.addPublicComment(publicCommentBuilder.build());
			
			actual = MemberDao.getById(mStaff, memberId);
			
			//Compare the member after update
			compareMember(expect, actual);
			
			//Perform to test charge
			testCharge(expect);
			
			//Perform to test consumption
			testConsume(expect);
			
			//Perform to test point consumption
			testPointConsume(expect);
			
			//Perform to test point adjust
			testAdjustPoint(expect);
			
			//Perform to test balance adjust
			testAdjustBalance(expect);
			
		}finally{
			if(memberId != 0){
				//Delete the member 
				MemberDao.deleteById(mStaff, memberId);
				//Check to see whether the member is deleted
				try{
					MemberDao.getById(mStaff, memberId);
					assertTrue("failed to delete member", false);
				}catch(BusinessException ignored){}
				
				//Check to see whether the private member comment is deleted
				assertEquals("failed to delete member private comment", "", MemberCommentDao.getPrivateCommentByMember(mStaff, new Member(memberId)).getComment());
				
				//Check to see whether the private member comments are deleted
				assertTrue("failed to delete member public comments", MemberCommentDao.getPublicCommentByMember(mStaff, new Member(memberId)).isEmpty());
				
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
		MemberOperation mo = MemberDao.consume(mStaff, expect.getId(), 50, PayType.MEMBER, orderId);
		expect.consume(50, PayType.MEMBER);
		
		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
		
		//使用现金消费
		mo = MemberDao.consume(mStaff, expect.getId(), 50, PayType.CASH, orderId);
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
		MemberOperation mo = MemberDao.pointConsume(mStaff, expect.getId(), 20);
		expect.pointConsume(20);
		
		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
	}
	
	private void testAdjustPoint(Member expect) throws SQLException, BusinessException{
		MemberOperation mo = MemberDao.adjustPoint(mStaff, expect.getId(), 10, AdjustType.INCREASE);
		expect.adjustPoint(10, AdjustType.INCREASE);
		
		compareMember(expect, MemberDao.getById(mStaff, expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getById(mStaff, DateType.TODAY, mo.getId()));
	}
	
	private void testAdjustBalance(Member expect) throws SQLException, BusinessException{
		MemberOperation mo = MemberDao.adjustBalance(mStaff, expect.getId(), 10);
		expect.adjustBalance(10);
		
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
