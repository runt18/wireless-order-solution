package com.wireless.test.db.stockMgr;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.StockInDetailDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockInDetail;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestStockDetail {

	private static Terminal mTerminal;
	
	@BeforeClass
	public static void initDBParam() throws BusinessException, SQLException{
		TestInit.init();
		try{
			mTerminal = VerifyPin.exec(217, Terminal.MODEL_STAFF);
		}catch(SQLException e){
			e.printStackTrace();
		}catch(BusinessException e){
			e.printStackTrace();
		}
	}
	//比较
	private static void compare(StockInDetail expected, StockInDetail actual){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("stockInId", expected.getStockInId(), actual.getStockInId());
		Assert.assertEquals("materialId", expected.getMaterialId(), actual.getMaterialId());
		Assert.assertEquals("price", expected.getPrice(), actual.getPrice(), 0.0001f);
		Assert.assertEquals("amount", expected.getAmount(), actual.getAmount(), 0.0001f);
	}
	
	@Test
	public void testStockDetailDao() throws SQLException, BusinessException{
		int id = Insert();
		Update(id);
		Delete(id);
	}
	private int Insert() throws SQLException, BusinessException{
		StockInDetail expected = new StockInDetail(2, 20, 1.5f, 80f);
		int id = StockInDetailDao.InsertStockInDetail(expected);
		StockInDetail actual = StockInDetailDao.GetStockInDetailById(mTerminal, id);
		expected.setId(id);
		compare(expected, actual);
		return id;
	}
	private void Update(int id) throws SQLException, BusinessException{
		StockInDetail expected = StockInDetailDao.GetStockInDetailById(mTerminal, id) ;
		expected.setStockInId(116);
		
		StockInDetailDao.UpdateStockDetail(expected);
		
		StockInDetail actual = StockInDetailDao.GetStockInDetailById(mTerminal, expected.getId());
		
		compare(expected, actual);
	}
	private void Delete(int id) throws BusinessException, SQLException{
		StockInDetailDao.DeleteStockDetailById(mTerminal, id);
		try{
			StockInDetailDao.GetStockInDetailById(mTerminal, id);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test 
	public void UpdateStockDetail() throws SQLException, BusinessException{
		StockInDetail expected = StockInDetailDao.GetStockInDetailById(mTerminal, 1) ;
		expected.setStockInId(116);
		
		StockInDetailDao.UpdateStockDetail(expected);
		
		StockInDetail actual = StockInDetailDao.GetStockInDetailById(mTerminal, expected.getId());
		
		compare(expected, actual);
	}
	
	@Test
	public void InsertStockDetail() throws SQLException, BusinessException{
		StockInDetail expected = new StockInDetail(2, 20, 1.5f, 80f);
		int id = StockInDetailDao.InsertStockInDetail(expected);
		StockInDetail actual = StockInDetailDao.GetStockInDetailById(mTerminal, id);
		expected.setId(id);
		compare(expected, actual);
	}
	
	@Test
	public void DeleteStockDetail() throws BusinessException, SQLException{
		StockInDetailDao.DeleteStockDetailById(mTerminal, 5);
		try{
			StockInDetailDao.GetStockInDetailById(mTerminal, 5);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
