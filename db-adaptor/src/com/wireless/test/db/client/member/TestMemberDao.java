package com.wireless.test.db.client.member;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.Member.Sex;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestMemberDao {
	
	private static Terminal mTerminal;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
		try {
			mTerminal = VerifyPin.exec(229, Terminal.MODEL_STAFF);
		} catch (BusinessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void compareMember(Member expected, Member actual){
		Assert.assertEquals("member id", expected.getId(), actual.getId());
		Assert.assertEquals("member card", expected.getMemberCard(), actual.getMemberCard());
		Assert.assertEquals("member_name", expected.getName(), actual.getName());
		Assert.assertEquals("member mobile", expected.getMobile(), actual.getMobile());
		Assert.assertEquals("member type", expected.getMemberType(), actual.getMemberType());
		Assert.assertEquals("associated restaurant id", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("member base balance", expected.getBaseBalance(), actual.getBaseBalance());
		Assert.assertEquals("member extra balance", expected.getExtraBalance(), actual.getExtraBalance());
		Assert.assertEquals("member point", expected.getPoint(), actual.getPoint());
		Assert.assertEquals("member tele", expected.getTele(), actual.getTele());
		Assert.assertEquals("member sex", expected.getSex(), actual.getSex());
		//Assert.assertEquals("member create date", expected.getCreateDate(), actual.getCreateDate());
		Assert.assertEquals("member id card", expected.getIdCard(), actual.getIdCard());
		Assert.assertEquals("member birthday", expected.getBirthday(), actual.getBirthday());
		Assert.assertEquals("member company", expected.getCompany(), actual.getCompany());
		Assert.assertEquals("member taste pref", expected.getTastePref(), actual.getTastePref());
		Assert.assertEquals("member taboo", expected.getTaboo(), actual.getTaboo());
		Assert.assertEquals("member contact address", expected.getContactAddress(), actual.getContactAddress());
		Assert.assertEquals("member comment", expected.getComment(), actual.getComment());
	}
	
	private void compareMemberOperation(MemberOperation expected, MemberOperation actual){
		Assert.assertEquals("mo - id", expected.getId(), actual.getId());
		Assert.assertEquals("mo - associated restaurant id", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("mo - staff id", expected.getStaffID(), actual.getStaffID());
		Assert.assertEquals("mo - member name", expected.getMemberName(), actual.getMemberName());
		Assert.assertEquals("mo - member mobile", expected.getMemberMobile(), actual.getMemberMobile());
		Assert.assertEquals("mo - member id", expected.getMemberId(), actual.getMemberId());
		Assert.assertEquals("mo - member card", expected.getMemberCard(), actual.getMemberCard());
		Assert.assertEquals("mo - operation seq", expected.getOperateSeq(), actual.getOperateSeq());
		//Assert.assertEquals("mo - operation date", expected.getOperateDate(), actual.getOperateDate());
		Assert.assertEquals("mo - operation type", expected.getOperationType(), expected.getOperationType());
		Assert.assertEquals("mo - consume money", expected.getPayMoney(), actual.getPayMoney());
		Assert.assertEquals("mo - charge type", expected.getChargeType(), actual.getChargeType());
		Assert.assertEquals("mo - charge balance", expected.getChargeMoney(), actual.getChargeMoney());
		Assert.assertEquals("mo - delta base balance", expected.getDeltaBaseMoney(), actual.getDeltaBaseMoney());
		Assert.assertEquals("mo - delta extra balance", expected.getDeltaExtraMoney(), actual.getDeltaExtraMoney());
		Assert.assertEquals("mo - delta point", expected.getDeltaPoint(), actual.getDeltaPoint());
		Assert.assertEquals("mo - remaining base balance", expected.getRemainingBaseMoney(), actual.getRemainingBaseMoney());
		Assert.assertEquals("mo - remaining extra balance", expected.getRemainingExtraMoney(), actual.getRemainingExtraMoney());
		Assert.assertEquals("mo - remaining point", expected.getRemainingPoint(), actual.getRemainingPoint());
	}
	
	@Test
	public void testMemberBasicOperation() throws BusinessException, SQLException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.restaurant_id = " + mTerminal.restaurantID);
		List<MemberType> list = MemberTypeDao.getMemberType(params);
		
		MemberType memberType = null;
		if(list.isEmpty()){
			throw new BusinessException("You don't add any member type!!!");
		}else{
			memberType = list.get(0);
		}
		
		//Insert a new member
		Member.InsertBuilder builder = new Member.InsertBuilder(mTerminal.restaurantID, "张三", "13694260535", memberType.getTypeID())
												 .setBirthday(DateUtil.parseDate("1981-03-15"))
												 .setCompany("Digie Co.,Ltd")
												 .setContactAddr("广州市东圃镇晨晖商务大厦")
												 .setIdCard("440711198103154818")
												 .setMemberCard("100010000")
												 .setSex(Sex.FEMALE)
												 .setTaboo("嫉妒咸鱼")
												 .setTastePref("喜欢甜品")
												 .setTele("020-87453214");
		
		int memberId = MemberDao.insert(mTerminal, builder);
		
		try{
			Member expect = builder.build();
			expect.setId(memberId);
			expect.setMemberType(memberType);
			expect.setPoint(memberType.getInitialPoint());
			//Set the initial point to expected member
			expect.setPoint(memberType.getInitialPoint());
			
			Member actual = MemberDao.getMemberById(memberId);
			
			//Compare the member just inserted
			compareMember(expect, actual);
			
			//Update the member just inserted
			Member.UpdateBuilder updateBuilder = new Member.UpdateBuilder(memberId, "李四", "18520590932", memberType.getTypeID())
														   .setBirthday(DateUtil.parseDate("1987-06-29"))
														   .setCompany("DingDing Tech")
														   .setContactAddr("广州市萝岗区科学城")
														   .setIdCard("4101234789965412")
														   .setMemberCard("1000100001")
														   .setSex(Sex.MALE)
														   .setTaboo("咩都要")
														   .setTastePref("垃圾桶")
														   .setTele("0750-3399559");
			MemberDao.update(mTerminal, updateBuilder);
			expect = updateBuilder.build();
			expect.setId(memberId);
			expect.setRestaurantId(mTerminal.restaurantID);
			expect.setMemberType(memberType);
			//Set the initial point to expected member
			expect.setPoint(memberType.getInitialPoint());
			
			actual = MemberDao.getMemberById(memberId);
			
			//Compare the member after update
			compareMember(expect, actual);
			
			//Perform to test charge
			testCharge(expect);
			
			//Perform to test consumption
			testConsume(expect);
			
		}finally{
			//Delete the member 
			MemberDao.deleteById(mTerminal, memberId);
			//Check to see whether the member is deleted
			try{
				MemberDao.getMemberById(memberId);
				Assert.assertTrue("failed to delete member", false);
			}catch(BusinessException ignored){}
			
			//Check to see whether the associated member operations are deleted
			Assert.assertTrue("failed to delete today member operation", MemberOperationDao.getTodayByMemberId(memberId).isEmpty());
			Assert.assertTrue("failed to delete history member operation", MemberOperationDao.getHistoryByMemberId(memberId).isEmpty());
		}

	}
	
	private void testCharge(Member expect) throws BusinessException, SQLException{
		MemberOperation mo = MemberDao.charge(mTerminal, expect.getId(), 100, ChargeType.CASH);
		expect.charge(100, ChargeType.CASH);
		
		compareMember(expect, MemberDao.getMemberById(expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getTodayById(mo.getId()));
	}
	
	private void testConsume(Member expect) throws BusinessException, SQLException{
		MemberDao.charge(mTerminal, expect.getId(), 100, ChargeType.CASH);
		expect.charge(100, ChargeType.CASH);
		
		//使用会员卡余额消费
		MemberOperation mo = MemberDao.consume(mTerminal, expect.getId(), 50, Order.PayType.MEMBER, 10);
		expect.consume(50, Order.PayType.MEMBER);
		
		compareMember(expect, MemberDao.getMemberById(expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getTodayById(mo.getId()));
		
		//使用现金消费
		mo = MemberDao.consume(mTerminal, expect.getId(), 50, Order.PayType.CASH, 10);
		expect.consume(50, Order.PayType.CASH);
		
		compareMember(expect, MemberDao.getMemberById(expect.getId()));
		compareMemberOperation(mo, MemberOperationDao.getTodayById(mo.getId()));
			
	}
}
