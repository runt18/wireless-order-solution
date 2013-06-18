package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.db.stockMgr.StockReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockReport;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestStockReport {

	private static Terminal mTerminal;
	
	@BeforeClass
	public static void initDBParam() throws BusinessException, SQLException, PropertyVetoException{
		TestInit.init();
		try{
			mTerminal = VerifyPin.exec(217, Terminal.MODEL_STAFF);
		}catch(SQLException e){
			e.printStackTrace();
		}catch(BusinessException e){
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testStockReport() throws SQLException{
		long begin = DateUtil.parseDate("2013-04-01");
		long end = DateUtil.parseDate("2013-07-01");
		
		List<StockReport> stockReports = StockReportDao.getStockCollect(mTerminal, begin, end, null);
		for (StockReport stockReport : stockReports) {
			int materialId = stockReport.getMaterialId();
			String Prime = " AND S.approve_date < " + begin + " AND D.material_id = " + materialId  
								+ " ORDER BY S.approve_date DESC";
			StockAction stockActionPrime = StockActionDao.getStockAndDetail(mTerminal, Prime, null).get(0);
			
			String finals = " AND S.approve_date < " + end + " AND D.material_id = " + materialId 
							+ " ORDER BY S.approve_date DESC";
			StockAction stockActionFianl = StockActionDao.getStockAndDetail(mTerminal, finals, null).get(0);
			
			Assert.assertEquals("primeAmount", stockActionPrime.getStockDetails().get(0).getRemaining(), stockReport.getPrimeAmount());
			//对比期初数量加减出库,入库小计后是否与期末数量相等
			Assert.assertEquals("actualEndAmount", stockActionFianl.getStockDetails().get(0).getRemaining(), stockReport.getActualAmount());
			
		}
	}
	
	
	
	
	
}
