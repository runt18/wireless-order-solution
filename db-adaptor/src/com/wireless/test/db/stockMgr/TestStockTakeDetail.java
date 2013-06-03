package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

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
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("stockTakeId", expected.getStockTakeId(), actual.getStockTakeId());
		Assert.assertEquals("materialId", expected.getMaterial().getId(), actual.getMaterial().getId());
		Assert.assertEquals("name", expected.getMaterial().getName(), actual.getMaterial().getName());
		Assert.assertEquals("expectAmount", expected.getExpectAmount(), actual.getExpectAmount());
		Assert.assertEquals("actualAmount", expected.getActualAmount(), actual.getActualAmount());
		Assert.assertEquals("deltaAmount", expected.getDeltaAmount(), actual.getDeltaAmount());
	}
	
	@Test
	public void testInsert() throws SQLException, BusinessException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		
		InsertStockTakeDetail build = new InsertStockTakeDetail()
									.setStockTakeId(1)
									.setMaterial(materials.get(0))
									.setExpectAmount(30)
									.setActualAmount(28);
		final int id = StockTakeDetailDao.insertstockTakeDetail(mTerminal, build);
		StockTakeDetail expected = build.build();
		expected.setId(id);
		StockTakeDetail actual = StockTakeDetailDao.getstockTakeDetailById(mTerminal, id);
		
		compare(expected, actual);
	}
}
