package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.text.ParseException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestStockReport {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDBParam() throws BusinessException, SQLException, PropertyVetoException{
		TestInit.init();
		try{
			mStaff = StaffDao.getAdminByRestaurant(37);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testStockReport() throws SQLException, ParseException, BusinessException{
		//TODO
		StockReportDao.getByCond(mStaff, null);
//		String begin = "2013-04-01";
//		String end = "2013-07-01";
//		
//		List<StockReport> stockReports = null;
//		stockReports = StockReportDao.getStockCollectByTime(mStaff, begin, end, null, null);
//		
//		/*		CateType cateType = CateType.MATERIAL;
//		if(cateType == null){
//			stockReports = StockReportDao.getStockCollectByTime(mTerminal, begin, end, null);
//		}else{
//			stockReports = StockReportDao.getStockCollectByTypes(mTerminal, begin, end, cateType, null);
//		}*/
//		
//		for (StockReport stockReport : stockReports) {
//			int materialId = stockReport.getMaterial().getId();
//			StockAction stockActionPrime = null;
//			String Prime = " AND S.ori_stock_date < '" + begin + "' AND D.material_id = " + materialId  
//								+ " ORDER BY S.ori_stock_date DESC";
//			if(StockActionDao.getStockAndDetail(mStaff, Prime, null).size() > 0){
//				stockActionPrime = StockActionDao.getStockAndDetail(mStaff, Prime, null).get(0);
//				assertEquals("primeAmount", stockActionPrime.getStockDetails().get(0).getRemaining(), stockReport.getPrimeAmount(), 0.01);
//			}
//		
//			String finals = " AND S.ori_stock_date < '" + end + "' AND D.material_id = " + materialId 
//							+ " ORDER BY S.ori_stock_date DESC";
//			StockAction stockActionFianl = StockActionDao.getStockAndDetail(mStaff, finals, null).get(0);
//			
//			
//			//对比期初数量加减出库,入库小计后是否与期末数量相等
//			assertEquals("actualEndAmount", stockActionFianl.getStockDetails().get(0).getRemaining(), stockReport.getFinalAmount(), 0.01);
//			
//		}
	}
	
	
	
	
	
}
