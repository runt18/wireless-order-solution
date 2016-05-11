package com.wireless.test.db.stockMgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.StockTakeDetailDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.StockTakeDetail;
import com.wireless.pojo.stockMgr.StockTakeDetail.InsertStockTakeDetail;
import com.wireless.test.db.TestInit;

public class TestStockTakeDetail {

	private static Staff mStaff;
	@BeforeClass
	public static void initDBParam() throws PropertyVetoException, SQLException, BusinessException{
		TestInit.init();
		try{
			mStaff = StaffDao.getAdminByRestaurant(37);
		}catch(SQLException e){
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
//		Map<Object, Object> params = new HashMap<Object, Object>();
//		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mStaff.getRestaurantId());
		List<Material> materials = MaterialDao.getByCond(mStaff, null);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		
		InsertStockTakeDetail build = new InsertStockTakeDetail()
									.setStockTakeId(1)
									.setMaterial(materials.get(0))
									.setExpectAmount(30)
									.setActualAmount(28);
		StockTakeDetail expected = build.build();
		final int id = StockTakeDetailDao.insertstockTakeDetail(mStaff, expected);
		
		expected.setId(id);
		StockTakeDetail actual = StockTakeDetailDao.getstockTakeDetailById(mStaff, id);
		
		compare(expected, actual);
	}
	
	@Test
	public void testStockTakeDetail() throws SQLException, BusinessException{
//		Map<Object, Object> params = new HashMap<Object, Object>();
//		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mStaff.getRestaurantId());
		List<Material> materials = MaterialDao.getByCond(mStaff, null);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		
		InsertStockTakeDetail build = new InsertStockTakeDetail()
									.setStockTakeId(1)
									.setMaterial(materials.get(0))
									.setExpectAmount(30)
									.setActualAmount(28);
		StockTakeDetail expected = build.build();
		final int id = StockTakeDetailDao.insertstockTakeDetail(mStaff, expected);
		expected.setId(id);
		StockTakeDetail actual = StockTakeDetailDao.getstockTakeDetailById(mStaff, id);
		
		compare(expected, actual);
		
		//update
		expected = actual;		
		expected.setActualAmount(29);
		
		StockTakeDetailDao.updateStockTakeDetail(mStaff, expected);
		
		actual = StockTakeDetailDao.getstockTakeDetailById(mStaff, id);
		
		compare(expected, actual);
		
		//delete
		StockTakeDetailDao.deleteStockTakeDetailById(id);
		try{
			StockTakeDetailDao.getstockTakeDetailById(mStaff, id);
			assertTrue("delete stock in record(id = " + id + ") failed", false);
		}catch(Exception e){}
		
		
	}
}
