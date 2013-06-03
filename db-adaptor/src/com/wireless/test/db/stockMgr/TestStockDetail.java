package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.StockActionDetailDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestStockDetail {

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
	//比较
	private static void compare(StockActionDetail expected, StockActionDetail actual){
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
		StockActionDetail expected = new StockActionDetail(20, "薄荷", 1.5f, 100f);
		int id = StockActionDetailDao.insertStockInDetail(expected);
		StockActionDetail actual = StockActionDetailDao.getStockInDetailById(mTerminal, id);
		expected.setId(id);
		compare(expected, actual);
		return id;
	}
	private void Update(int id) throws SQLException, BusinessException{
		StockActionDetail expected = StockActionDetailDao.getStockInDetailById(mTerminal, id) ;
		expected.setPrice(80);
		
		StockActionDetailDao.updateStockDetail(expected);
		
		StockActionDetail actual = StockActionDetailDao.getStockInDetailById(mTerminal, expected.getId());
		
		compare(expected, actual);
	}
	private void Delete(int id) throws BusinessException, SQLException{
		StockActionDetailDao.deleteStockDetailById(mTerminal, id);
		try{
			StockActionDetailDao.getStockInDetailById(mTerminal, id);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test 
	public void UpdateStockDetail() throws SQLException, BusinessException{
		StockActionDetail expected = StockActionDetailDao.getStockInDetailById(mTerminal, 1) ;
		expected.setStockInId(116);
		
		StockActionDetailDao.updateStockDetail(expected);
		
		StockActionDetail actual = StockActionDetailDao.getStockInDetailById(mTerminal, expected.getId());
		
		compare(expected, actual);
	}
	
	@Test
	public void InsertStockDetail() throws SQLException, BusinessException{
		StockActionDetail expected = new StockActionDetail(20, "苛刻", 1.5f, 90f);
		int id = StockActionDetailDao.insertStockInDetail(expected);
		StockActionDetail actual = StockActionDetailDao.getStockInDetailById(mTerminal, id);
		expected.setId(id);
		compare(expected, actual);
	}
	
	@Test
	public void DeleteStockDetail() throws BusinessException, SQLException{
		StockActionDetailDao.deleteStockDetailById(mTerminal, 5);
		try{
			StockActionDetailDao.getStockInDetailById(mTerminal, 5);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
