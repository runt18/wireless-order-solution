package com.wireless.test.db.printScheme;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.printScheme.PrintLossDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PrintSchemeError;
import com.wireless.pojo.printScheme.PrintLoss;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestPrintLoss {
	
	private final static byte[] TEST_CONTENT = new byte[]{0x01, 0x02, 0x03};
	
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
	
	@Test
	public void testPrintLoss() throws SQLException, BusinessException{
		int printLossId = 0;
		try{
			printLossId = PrintLossDao.insert(mStaff, new PrintLoss.InsertBuilder(TEST_CONTENT));
			PrintLoss actual = PrintLossDao.getById(mStaff, printLossId);
			
			Assert.assertEquals("restaurant id", mStaff.getRestaurantId(), actual.getRestaurantId());
			Assert.assertArrayEquals("content", TEST_CONTENT, actual.getContent());
			
		}finally{
			if(printLossId != 0){
				PrintLossDao.deleteById(mStaff, printLossId);
				try{
					PrintLossDao.getById(mStaff, printLossId);
					Assert.assertTrue("failed to delete the print loss", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the print loss", PrintSchemeError.PRINT_LOSS_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	@Test
	public void testStat() throws SQLException{
		System.out.println(PrintLossDao.stat());
	}
	
	@Test
	public void testCleanup() throws SQLException{
		System.out.println(PrintLossDao.cleanup());
	}
}
