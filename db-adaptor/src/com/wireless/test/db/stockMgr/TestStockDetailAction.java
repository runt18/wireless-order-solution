package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.stockMgr.StockActionDetailDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestStockDetailAction {

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
		Assert.assertEquals("stockActionId", expected.getStockActionId(), actual.getStockActionId());
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
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		
		StockActionDetail expected = new StockActionDetail(materials.get(0).getId(), materials.get(0).getName(), 1.5f, 100f);
		expected.setStockActionId(8);
		
		int id = StockActionDetailDao.insertStockActionDetail(expected);
		expected.setId(id);
		StockActionDetail actual = StockActionDetailDao.getStockActionDetailById(mTerminal, id);
		
		compare(expected, actual);
		
		return id;
	}
	private void Update(int id) throws SQLException, BusinessException{
		StockActionDetail expected = StockActionDetailDao.getStockActionDetailById(mTerminal, id) ;
		expected.setPrice(80);
		
		StockActionDetailDao.updateStockDetail(expected);
		
		StockActionDetail actual = StockActionDetailDao.getStockActionDetailById(mTerminal, expected.getId());
		
		compare(expected, actual);
	}
	private void Delete(int id) throws BusinessException, SQLException{
		StockActionDetailDao.deleteStockDetailById(mTerminal, id);
		try{
			StockActionDetailDao.getStockActionDetailById(mTerminal, id);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test 
	public void UpdateStockDetail() throws SQLException, BusinessException{
		StockActionDetail expected = StockActionDetailDao.getStockActionDetailById(mTerminal, 1) ;
		expected.setStockActionId(116);
		
		StockActionDetailDao.updateStockDetail(expected);
		
		StockActionDetail actual = StockActionDetailDao.getStockActionDetailById(mTerminal, expected.getId());
		
		compare(expected, actual);
	}
	
	@Test
	public void InsertStockDetail() throws SQLException, BusinessException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		
		StockActionDetail expected = new StockActionDetail(materials.get(0).getId(), materials.get(0).getName(), 1.5f, 100f);
		expected.setStockActionId(8);
		
		int id = StockActionDetailDao.insertStockActionDetail(expected);
		expected.setId(id);
		StockActionDetail actual = StockActionDetailDao.getStockActionDetailById(mTerminal, id);
		
		compare(expected, actual);
	}
	
	@Test
	public void DeleteStockDetail() throws BusinessException, SQLException{
		StockActionDetailDao.deleteStockDetailById(mTerminal, 5);
		try{
			StockActionDetailDao.getStockActionDetailById(mTerminal, 5);
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
