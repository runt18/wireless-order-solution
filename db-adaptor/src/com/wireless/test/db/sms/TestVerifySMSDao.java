package com.wireless.test.db.sms;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.sms.VerifySMSDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.SMSError;
import com.wireless.pojo.sms.VerifySMS;
import com.wireless.test.db.TestInit;

public class TestVerifySMSDao {
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testVerifySMS() throws SQLException, BusinessException, InterruptedException{
		int smsId = 0;
		try{
			VerifySMS.InsertBuilder insertBuilder = new VerifySMS.InsertBuilder(VerifySMS.ExpiredPeriod.SECOND_1);
			smsId = VerifySMSDao.insert(insertBuilder);
			
			VerifySMS expected = insertBuilder.build();
			expected.setId(smsId);
			
			VerifySMS actual = VerifySMSDao.getById(smsId);
			compare(expected, actual);
			
			VerifySMSDao.verify(new VerifySMS.VerifyBuilder(actual.getId(), actual.getCode()));
			
			//Test if the sms does not exist.
			try{
				VerifySMSDao.verify(new VerifySMS.VerifyBuilder(0, actual.getCode()));
			}catch(BusinessException e){
				Assert.assertEquals("verification sms not exist", SMSError.VERIFICATION_SMS_NOT_EXIST, e.getErrCode());
			}
			
			//Test if the code is not matched.
			try{
				VerifySMSDao.verify(new VerifySMS.VerifyBuilder(actual.getId(), 0));
			}catch(BusinessException e){
				Assert.assertEquals("verification code not matched", SMSError.VERIFICATION_CODE_NOT_MATCH, e.getErrCode());
			}
			
			//Test if the sms is expired.
			Thread.sleep(1500);
			try{
				VerifySMSDao.verify(new VerifySMS.VerifyBuilder(actual.getId(), actual.getCode()));
			}catch(BusinessException e){
				Assert.assertEquals("verification sms is expired", SMSError.VERIFICATION_SMS_EXPIRED, e.getErrCode());
			}
			
		}finally{
			VerifySMSDao.delete(smsId);
			try{
				VerifySMSDao.getById(smsId);
			}catch(BusinessException e){
				Assert.assertEquals("failed to delete verification sms", SMSError.VERIFICATION_SMS_NOT_EXIST, e.getErrCode());
			}
		}
	}
	
	private void compare(VerifySMS expected, VerifySMS actual){
		Assert.assertEquals("verification sms id", expected.getId(), actual.getId());
		Assert.assertTrue("verification sms code", actual.getCode() >= 1000 && actual.getCode() <= 9999);
		Assert.assertTrue("verification sms created time", System.currentTimeMillis() - expected.getCreated() < 5000);
		Assert.assertEquals("verification sms expired time", actual.getExpired() - actual.getCreated(), VerifySMS.ExpiredPeriod.SECOND_1.getTime());
	}
}
