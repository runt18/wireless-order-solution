package com.wireless.test.db.sms;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.sms.SMStatDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.RestaurantError;
import com.wireless.pojo.sms.SMSDetail;
import com.wireless.pojo.sms.SMStat;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestSMStatDao {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, SQLException, BusinessException{
		TestInit.init();
		mStaff = StaffDao.getAdminByRestaurant(40);
	}
	
	@Test
	public void testSMStat() throws SQLException, BusinessException{
		try{
			SMStatDao.insert(mStaff, new SMStat.InsertBuilder(mStaff.getRestaurantId()));
			SMStat expected = new SMStat(mStaff.getRestaurantId());
			compare(expected, SMStatDao.get(mStaff));

			//Add the SMS
			SMStatDao.update(mStaff, new SMStat.UpdateBuilder(mStaff.getRestaurantId(), SMSDetail.Operation.ADD).setAmount(100));
			expected.add(100);
			compare(expected, SMStatDao.get(mStaff));
			//Test the detail to add.
			SMSDetail expectedDetail = new SMSDetail(0);
			expectedDetail.setRestaurantId(mStaff.getRestaurantId());
			expectedDetail.setModified(System.currentTimeMillis());
			expectedDetail.setDelta(100);
			expectedDetail.setOperation(SMSDetail.Operation.ADD);
			expectedDetail.setRemaining(expected.getRemaining());
			expectedDetail.setStaff(mStaff.getName());
			compare(expectedDetail, SMStatDao.getDetails(mStaff, null, " ORDER BY id DESC LIMIT 1").get(0));
			
			//Deduct the SMS
			SMStatDao.update(mStaff, new SMStat.UpdateBuilder(mStaff.getRestaurantId(), SMSDetail.Operation.DEDUCT).setAmount(50));
			expected.deduct(50);
			compare(expected, SMStatDao.get(mStaff));
			//Test the detail to deduct.
			expectedDetail.setRestaurantId(mStaff.getRestaurantId());
			expectedDetail.setModified(System.currentTimeMillis());
			expectedDetail.setDelta(50);
			expectedDetail.setOperation(SMSDetail.Operation.DEDUCT);
			expectedDetail.setRemaining(expected.getRemaining());
			expectedDetail.setStaff(mStaff.getName());
			compare(expectedDetail, SMStatDao.getDetails(mStaff, null, " ORDER BY id DESC LIMIT 1").get(0));
			
			//Use the SMS for verification.
			SMStatDao.update(mStaff, new SMStat.UpdateBuilder(mStaff.getRestaurantId(), SMSDetail.Operation.USE_VERIFY).setAmount(1));
			expected.use4Verify(1);
			compare(expected, SMStatDao.get(mStaff));
			//Test the detail to verify.
			expectedDetail.setRestaurantId(mStaff.getRestaurantId());
			expectedDetail.setModified(System.currentTimeMillis());
			expectedDetail.setDelta(1);
			expectedDetail.setOperation(SMSDetail.Operation.USE_VERIFY);
			expectedDetail.setRemaining(expected.getRemaining());
			expectedDetail.setStaff(mStaff.getName());
			compare(expectedDetail, SMStatDao.getDetails(mStaff, null, " ORDER BY id DESC LIMIT 1").get(0));
			
			//Use the SMS for consumption.
			SMStatDao.update(mStaff, new SMStat.UpdateBuilder(mStaff.getRestaurantId(), SMSDetail.Operation.USE_CONSUME).setAmount(1));
			expected.use4Consume(1);
			compare(expected, SMStatDao.get(mStaff));
			//Test the detail to consume.
			expectedDetail.setRestaurantId(mStaff.getRestaurantId());
			expectedDetail.setModified(System.currentTimeMillis());
			expectedDetail.setDelta(1);
			expectedDetail.setOperation(SMSDetail.Operation.USE_CONSUME);
			expectedDetail.setRemaining(expected.getRemaining());
			expectedDetail.setStaff(mStaff.getName());
			compare(expectedDetail, SMStatDao.getDetails(mStaff, null, " ORDER BY id DESC LIMIT 1").get(0));
			
			//Use the SMS for charge.
			SMStatDao.update(mStaff, new SMStat.UpdateBuilder(mStaff.getRestaurantId(), SMSDetail.Operation.USE_CHARGE).setAmount(1));
			expected.use4Charge(1);
			compare(expected, SMStatDao.get(mStaff));
			//Test the detail to verify.
			expectedDetail.setRestaurantId(mStaff.getRestaurantId());
			expectedDetail.setModified(System.currentTimeMillis());
			expectedDetail.setDelta(1);
			expectedDetail.setOperation(SMSDetail.Operation.USE_CHARGE);
			expectedDetail.setRemaining(expected.getRemaining());
			expectedDetail.setStaff(mStaff.getName());
			compare(expectedDetail, SMStatDao.getDetails(mStaff, null, " ORDER BY id DESC LIMIT 1").get(0));
			
		}finally{
			SMStatDao.delete(mStaff);
			try{
				SMStatDao.get(mStaff);
				Assert.assertTrue("failed to delete the sms stat", false);
			}catch(BusinessException e){
				Assert.assertEquals("failed to delete the sms stat", RestaurantError.SMS_STAT_NOT_EXIST, e.getErrCode());
				Assert.assertTrue("failed to delete the sms details", SMStatDao.getDetails(mStaff, null, null).isEmpty());
			}
		}
	}
	
	private void compare(SMStat expected, SMStat actual){
		Assert.assertEquals("restaurant - insert sms stat", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("charge used - insert sms stat", expected.getChargeUsed(), actual.getChargeUsed());
		Assert.assertEquals("consumption used - insert sms stat", expected.getConsumptionUsed(), actual.getConsumptionUsed());
		Assert.assertEquals("verification used - insert sms stat", expected.getVerificationUsed(), actual.getVerificationUsed());
		Assert.assertEquals("total used - insert sms stat", expected.getTotalUsed(), actual.getTotalUsed());
		Assert.assertEquals("remaining - insert sms stat", expected.getRemaining(), actual.getRemaining());
	}
	
	private void compare(SMSDetail expected, SMSDetail actual){
		Assert.assertEquals("restaurant - sms detail (" + expected.getOperation() + ")", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("delta - sms detail(" + expected.getOperation() + ")", expected.getDelta(), actual.getDelta());
		Assert.assertEquals("operation - sms detail(" + expected.getOperation() + ")", expected.getOperation(), actual.getOperation());
		Assert.assertEquals("remaining - sms detail(" + expected.getOperation() + ")", expected.getRemaining(), actual.getRemaining());
		Assert.assertEquals("staff - sms detail(" + expected.getOperation() + ")", expected.getStaff(), actual.getStaff());
		Assert.assertTrue("modified - sms(" + expected.getOperation() + ")", Math.abs(expected.getModified() - actual.getModified()) < 1000);
	}
}
