package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.db.stockMgr.StockDetailReportDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockDetailReport;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestStockDetailReport {
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
	//期望值与真实值的比较
	private void compare(StockDetailReport stockDetailReport, StockAction stockAction){
		assertEquals("oriStockId", stockDetailReport.getOriStockId(), stockAction.getOriStockId());
		assertEquals("stockActionAmount", stockDetailReport.getStockActionAmount(), stockAction.getStockDetails().get(0).getAmount(), 0.01);
		assertEquals("remaining", stockDetailReport.getRemaining(), stockAction.getStockDetails().get(0).getRemaining(), 0.01);
	}
	
	
	@Test
	public void testStockDetial() throws SQLException{
		String begin = "2013-04-01";
		String end = "2013-07-01";
		
		Map<Object, Object> param = new HashMap<Object, Object>();
		param.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(param);
		int materialId = materials.get(2).getId();
		
		
		
		List<StockDetailReport> stockDetailReports;
		//按时间查询
		stockDetailReports = StockDetailReportDao.getStockDetailReportByDate(mTerminal, begin, end, materialId, null);
		//按部门查询
		//Department dept = DepartmentDao.getDepartments(mTerminal, null, null).get(2);
		//stockDetailReports = StockDetailReportDao.getStockDetailReportByDept(materialId, dept.getId());
		
		for (StockDetailReport stockDetailReport : stockDetailReports) {
			List<StockAction> stockActions = StockActionDao.getStockAndDetail(mTerminal, " AND S.ori_stock_date ='" + DateUtil.format(stockDetailReport.getDate()) + "' AND D.material_id =" + materialId , null);
			for (StockAction stockAction : stockActions) {
				if(stockDetailReport.getRemaining() == stockAction.getStockDetails().get(0).getRemaining()){
					compare(stockDetailReport, stockAction);
					break;
				}
			}
		}
	}
	
	
	
	
	
}
