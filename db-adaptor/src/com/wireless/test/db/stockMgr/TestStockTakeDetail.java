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
import com.wireless.db.stockMgr.StockTakeDetailDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.stockMgr.StockTakeDetail;
import com.wireless.pojo.stockMgr.StockTakeDetail.InsertStockTakeDetail;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestStockTakeDetail {

	private static Terminal mTerminal;
	@BeforeClass
	public static void initDBParam() throws PropertyVetoException, SQLException, BusinessException{
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
	
	public void compare(StockTakeDetail expected, StockTakeDetail actual){
		assertEquals("id", expected.getId(), actual.getId());
		assertEquals("stockTakeId", expected.getStockTakeId(), actual.getStockTakeId());
		assertEquals("materialId", expected.getMaterial().getId(), actual.getMaterial().getId());
		assertEquals("name", expected.getMaterial().getName(), actual.getMaterial().getName());
		assertEquals("expectAmount", expected.getExpectAmount(), actual.getExpectAmount(), 0.01);
		assertEquals("actualAmount", expected.getActualAmount(), actual.getActualAmount(), 0.01);
		assertEquals("deltaAmount", expected.getDeltaAmount(), actual.getDeltaAmount(), 0.01);
	}
	@Test
	public void testInsert() throws SQLException, BusinessException {
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		
		InsertStockTakeDetail build = new InsertStockTakeDetail()
									.setStockTakeId(1)
									.setMaterial(materials.get(0))
									.setExpectAmount(30)
									.setActualAmount(28);
		StockTakeDetail expected = build.build();
		final int id = StockTakeDetailDao.insertstockTakeDetail(mTerminal, expected);
		
		expected.setId(id);
		StockTakeDetail actual = StockTakeDetailDao.getstockTakeDetailById(mTerminal, id);
		
		compare(expected, actual);
	}
	
	@Test
	public void testStockTakeDetail() throws SQLException, BusinessException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		
		InsertStockTakeDetail build = new InsertStockTakeDetail()
									.setStockTakeId(1)
									.setMaterial(materials.get(0))
									.setExpectAmount(30)
									.setActualAmount(28);
		StockTakeDetail expected = build.build();
		final int id = StockTakeDetailDao.insertstockTakeDetail(mTerminal, expected);
		expected.setId(id);
		StockTakeDetail actual = StockTakeDetailDao.getstockTakeDetailById(mTerminal, id);
		
		compare(expected, actual);
		
		//update
		expected = actual;		
		expected.setActualAmount(29);
		
		StockTakeDetailDao.updateStockTakeDetail(mTerminal, expected);
		
		actual = StockTakeDetailDao.getstockTakeDetailById(mTerminal, id);
		
		compare(expected, actual);
		
		//delete
		StockTakeDetailDao.deleteStockTakeDetailById(id);
		try{
			StockTakeDetailDao.getstockTakeDetailById(mTerminal, id);
			assertTrue("delete stock in record(id = " + id + ") failed", false);
		}catch(Exception e){}
		
		
	}
}
