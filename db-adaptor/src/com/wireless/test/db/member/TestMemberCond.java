package com.wireless.test.db.member;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.member.MemberCondDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberCond;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestMemberCond {
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
	
	@Test
	public void testMemberCondition() throws BusinessException, SQLException{
		int id = 0;
		try{
			MemberCond.InsertBuilder insertBuilder = new MemberCond.InsertBuilder("测试会员查询条件")
																   .setRange(System.currentTimeMillis(), System.currentTimeMillis() + 1000)
																   .setBalance(100, 200)
																   .setConsumeAmount(5, 8)
																   .setConsumeMoney(20, 100)
																   .setLastConsumption(10, 20)
																   .setSex(Member.Sex.FEMALE)
																   .addAge(Member.Age.AGE_80)
																   .addAge(Member.Age.AGE_70)
																   .setCharge(5,  10)
																   .setRaw(false)
																   .setMinFansAmount(10)
																   .setMaxFansAmount(20)
																   .setMinCommissionAmount(10)
																   .setMaxCommissionAmount(20);
			id = MemberCondDao.insert(mStaff, insertBuilder);
			
			MemberCond expected = insertBuilder.build();
			expected.setId(id);
			MemberCond actual = MemberCondDao.getById(mStaff, id);
			compare(expected, actual);
			
			MemberCond.UpdateBuilder updateBuilder = new MemberCond.UpdateBuilder(id)
																   .setName("修改会员查询条件")
																   .setRange(System.currentTimeMillis(), System.currentTimeMillis() + 3000)
   																   .setBalance(50, 300)
																   .setConsumeAmount(6, 10)
																   .setConsumeMoney(200, 500)
																   .setLastConsumption(10, 30)
																   .setSex(Member.Sex.MALE)
																   .clearAge()
																   .setCharge(10, 20)
																   .setRaw(true)
																   .setFansRange(20, 30);
			MemberCondDao.update(mStaff, updateBuilder);
			if(updateBuilder.isNameChanged()){
				expected.setName(updateBuilder.build().getName());
			}
			if(updateBuilder.isMemberTypeChanged()){
				expected.setMemberType(updateBuilder.build().getMemberType());
			}
			if(updateBuilder.isBalanceChanged()){
				expected.setMinBalance(updateBuilder.build().getMinBalance());
				expected.setMaxBalance(updateBuilder.build().getMaxBalance());
			}
			if(updateBuilder.isConsumeAmountChanged()){
				expected.setMinConsumeAmount(updateBuilder.build().getMinConsumeAmount());
				expected.setMaxConsumeAmount(updateBuilder.build().getMaxConsumeAmount());
			}
			if(updateBuilder.isConsumeMoneyChanged()){
				expected.setMinConsumeMoney(updateBuilder.build().getMinConsumeMoney());
				expected.setMaxConsumeMoney(updateBuilder.build().getMaxConsumeMoney());
			}
			if(updateBuilder.isRangeTypeChanged()){
				expected.setRangeType(updateBuilder.build().getRangeType());
			}
			if(updateBuilder.isRangeChanged()){
				expected.setRange(updateBuilder.build().getRange());
			}
			if(updateBuilder.isLastConsumptionChanged()){
				expected.setMinLastConsumption(updateBuilder.build().getMinLastConsumption());
				expected.setMaxLastConsumption(updateBuilder.build().getMaxLastConsumption());
			}
			if(updateBuilder.isSexChanged()){
				expected.setSex(updateBuilder.build().getSex());
			}
			if(updateBuilder.isAgeChanged()){
				expected.setAges(updateBuilder.build().getAges());
			}
			if(updateBuilder.isChargeChanged()){
				expected.setMinCharge(updateBuilder.build().getMinCharge());
				expected.setMaxCharge(updateBuilder.build().getMaxCharge());
			}
			if(updateBuilder.isRawChanged()){
				expected.setRaw(updateBuilder.build().isRaw());
			}
			if(updateBuilder.isFansChanged()){
				expected.setMinFansAmount(updateBuilder.build().getMinFansAmount());
				expected.setMaxFansAmount(updateBuilder.build().getMaxFansAmount());
			}
			actual = MemberCondDao.getById(mStaff, id);
			compare(expected, actual);
		}finally{
			if(id > 0){
				MemberCondDao.deleteById(mStaff, id);
				try{
					MemberCondDao.getById(mStaff, id);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the member condition", MemberError.MEMBER_CONDITION_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	private void compare(MemberCond expected, MemberCond actual){
		Assert.assertEquals("member condtion id", expected.getId(), actual.getId());
		Assert.assertEquals("member condition range type", expected.getRangeType(), actual.getRangeType());
		Assert.assertEquals("member condition range", expected.getRange(), actual.getRange());
		Assert.assertEquals("member condtion restaurant id", mStaff.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("member condtion name", expected.getName(), actual.getName());
		Assert.assertEquals("member condition min balance", expected.getMinBalance(), actual.getMinBalance(), 0.01);
		Assert.assertEquals("member condition max balance", expected.getMaxBalance(), actual.getMaxBalance(), 0.01);
		Assert.assertEquals("member condition min consume money", expected.getMinConsumeMoney(), actual.getMinConsumeMoney(), 0.01);
		Assert.assertEquals("member condition max consume money", expected.getMaxConsumeMoney(), actual.getMaxConsumeMoney(), 0.01);
		Assert.assertEquals("member condition max consume amount", expected.getMaxConsumeAmount(), actual.getMaxConsumeAmount());
		Assert.assertEquals("member condition min consume amount", expected.getMinConsumeAmount(), actual.getMinConsumeAmount());
		Assert.assertEquals("member condition min last consumption", expected.getMinLastConsumption(), actual.getMinLastConsumption());
		Assert.assertEquals("member condition max last consumption", expected.getMaxLastConsumption(), actual.getMaxLastConsumption());
		Assert.assertEquals("member condition min charge", expected.getMinCharge(), actual.getMinCharge(), 0.01);
		Assert.assertEquals("member condition max charge", expected.getMaxCharge(), actual.getMaxCharge(), 0.01);
		Assert.assertEquals("member condition sex", expected.getSex(), actual.getSex());
		Assert.assertEquals("member condition ages", expected.getAges(), actual.getAges());
		Assert.assertEquals("member condition raw", expected.isRaw(), actual.isRaw());
		Assert.assertEquals("member condition minFansAmount", expected.getMinFansAmount(), actual.getMinFansAmount());
		Assert.assertEquals("member condition maxFansAmount", expected.getMaxFansAmount(), actual.getMaxFansAmount());
		Assert.assertEquals("member condition minCommissionAmount", expected.getMinCommissionAmount(), actual.getMinCommissionAmount(), 0.01);
		Assert.assertEquals("member condition maxCommissionAmount", expected.getMaxCommissionAmount(), actual.getMaxCommissionAmount(), 0.01);
	}
}
