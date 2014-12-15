package com.wireless.test.db.printScheme;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.printScheme.PrinterConnectionDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.PrintSchemeError;
import com.wireless.pojo.printScheme.PrinterConnection;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestPrinterConnection {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			mStaff = StaffDao.getAdminByRestaurant(37);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPrinterConnection() throws SQLException, BusinessException{
		int connectionId = 0;
		try{
			
			PrinterConnection.InsertBuilder builder = new PrinterConnection.InsertBuilder("192.168.1.100", "192.168.1.101");
			connectionId = PrinterConnectionDao.insert(mStaff, builder);
			
			PrinterConnection expected = builder.build();
			PrinterConnection actual = PrinterConnectionDao.getById(mStaff, connectionId);
			
			Assert.assertEquals("source to printer connection", expected.getSource(), actual.getSource());
			Assert.assertEquals("dest to printer connection", expected.getDest(), actual.getDest());
			Assert.assertTrue("last connected to printer connection", Math.abs(actual.getLastConnected() - System.currentTimeMillis()) < 1000);
			
		}finally{
			if(connectionId != 0){
				PrinterConnectionDao.deleteById(mStaff, connectionId);
				try{
					PrinterConnectionDao.getById(mStaff, connectionId);
					Assert.assertTrue("fail to delete the printer connection", false);
				}catch(BusinessException e){
					Assert.assertEquals("fail to delete the printer connection", PrintSchemeError.PRINT_SERVER_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
}
