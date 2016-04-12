package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockActionDetailDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.test.db.TestInit;

public class TestStockDetailAction {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDBParam() throws BusinessException, SQLException, PropertyVetoException{
		TestInit.init();
		
		try{
			mStaff = StaffDao.getByRestaurant(37).get(0);
		}catch(SQLException e){
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
//		Map<Object, Object> params = new HashMap<Object, Object>();
//		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mStaff.getRestaurantId());
		List<Material> materials = MaterialDao.getByCond(mStaff, null);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		
		StockActionDetail expected = new StockActionDetail(materials.get(0).getId(), 1.5f, 100f);
		expected.setStockActionId(8);
		
		final int id = StockActionDetailDao.insert(mStaff, expected);
		expected.setId(id);
		StockActionDetail actual = StockActionDetailDao.getByCond(mStaff, new StockActionDetailDao.ExtraCond().setId(id), null).get(0);
		
		compare(expected, actual);
		
		expected = StockActionDetailDao.getByCond(mStaff, new StockActionDetailDao.ExtraCond().setId(id), null).get(0);
		expected.setPrice(80);
		
		StockActionDetailDao.update(expected);
		
		actual = StockActionDetailDao.getByCond(mStaff, new StockActionDetailDao.ExtraCond().setId(id), null).get(0);
		
		compare(expected, actual);
		
		StockActionDetailDao.deleteById(mStaff, id);
		try{
			StockActionDetailDao.getByCond(mStaff, new StockActionDetailDao.ExtraCond().setId(id), null).get(0);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	

	
	@Test
	public void insertStockDetail() throws SQLException, BusinessException{
//		Map<Object, Object> params = new HashMap<Object, Object>();
//		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mStaff.getRestaurantId());
		List<Material> materials = MaterialDao.getByCond(mStaff, null);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		
		StockActionDetail expected = new StockActionDetail(materials.get(0).getId(), 1.5f, 100f);
		expected.setStockActionId(8);
		
		int id = StockActionDetailDao.insert(mStaff, expected);
		expected.setId(id);
		StockActionDetail actual = StockActionDetailDao.getByCond(mStaff, new StockActionDetailDao.ExtraCond().setId(id), null).get(0);
		
		compare(expected, actual);
	}
	@Test 
	public void updateStockDetail() throws SQLException, BusinessException{
		StockActionDetail expected = StockActionDetailDao.getByCond(mStaff, new StockActionDetailDao.ExtraCond().setId(1), null).get(0);
		expected.setStockActionId(116);
		
		StockActionDetailDao.update(expected);
		
		StockActionDetail actual = StockActionDetailDao.getByCond(mStaff, new StockActionDetailDao.ExtraCond().setId(expected.getId()), null).get(0);
		
		compare(expected, actual);
	}
	
	
}
