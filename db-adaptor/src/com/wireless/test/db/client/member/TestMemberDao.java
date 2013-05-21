package com.wireless.test.db.client.member;

import java.sql.SQLException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestMemberDao {
	
	private static Terminal mTerminal;
	private static Member mMember;
	
	@BeforeClass
	public static void initDbParam(){
		TestInit.init();
		try {
			mTerminal = VerifyPin.exec(229, Terminal.MODEL_STAFF);
			//FIXME Not a correct way to get the member
			mMember = MemberDao.getMemberById(1);
		} catch (BusinessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void compareMember(Member expected, Member actual){
		Assert.assertEquals("member id", expected.getId(), actual.getId());
		Assert.assertEquals("member card id", expected.getMemberCardID(), actual.getMemberCardID());
		Assert.assertEquals("member type id", expected.getMemberTypeID(), actual.getMemberTypeID());
		Assert.assertEquals("associated restaurant id", expected.getRestaurantID(), actual.getRestaurantID());
		Assert.assertEquals("member base balance", expected.getBaseBalance(), actual.getBaseBalance());
		Assert.assertEquals("member extra balance", expected.getExtraBalance(), actual.getExtraBalance());
		Assert.assertEquals("member point", expected.getPoint(), actual.getPoint());
	}
	
	private void compareMemberOperation(MemberOperation expected, MemberOperation actual){
		Assert.assertEquals("mo - id", expected.getId(), actual.getId());
		Assert.assertEquals("mo - associated restaurant id", expected.getRestaurantID(), actual.getRestaurantID());
		Assert.assertEquals("mo - staff id", expected.getStaffID(), actual.getStaffID());
		Assert.assertEquals("mo - member id", expected.getMemberID(), actual.getMemberID());
		Assert.assertEquals("mo - member card id", expected.getMemberCardID(), actual.getMemberCardID());
		Assert.assertEquals("mo - member card alias", expected.getMemberCardAlias(), actual.getMemberCardAlias());
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
	public void testCharge()throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			MemberOperation mo = MemberDao.charge(dbCon, mTerminal, mMember.getId(), 100, ChargeType.CASH);
			mMember.charge(100, ChargeType.CASH);
			
			compareMember(mMember, MemberDao.getMemberById(dbCon, mMember.getId()));
			compareMemberOperation(mo, MemberOperationDao.getTodayById(dbCon, mo.getId()));
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	@Test
	public void testConsume() throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			MemberDao.charge(dbCon, mTerminal, mMember.getId(), 100, ChargeType.CASH);
			mMember.charge(100, ChargeType.CASH);
			
			MemberOperation mo = MemberDao.consume(dbCon, mTerminal, mMember.getId(), 50, Order.PayType.MEMBER, 10);
			mMember.consume(50, Order.PayType.MEMBER);
			
			compareMember(mMember, MemberDao.getMemberById(dbCon, mMember.getId()));
			compareMemberOperation(mo, MemberOperationDao.getTodayById(dbCon, mo.getId()));
			
		}finally{
			dbCon.disconnect();
		}
	}
}
