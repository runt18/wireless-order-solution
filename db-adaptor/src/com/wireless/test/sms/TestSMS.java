package com.wireless.test.sms;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

import org.apache.http.client.ClientProtocolException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.sms.SMStatDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.sms.SMSDetail;
import com.wireless.pojo.sms.SMStat;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;
import com.wireless.util.sms.SMS;

public class TestSMS {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException, SQLException{
		TestInit.init();
		mStaff = StaffDao.getAdminByRestaurant(40);
	}
	
	@Test
	public void testSMS() throws ClientProtocolException, IOException, SQLException, BusinessException{
		SMStatDao.update(mStaff, new SMStat.UpdateBuilder(mStaff.getRestaurantId(), SMSDetail.Operation.ADD).setAmount(1));
		SMS.send(mStaff, "18520590932", new SMS.Msg4Verify(1000));
	}
}
