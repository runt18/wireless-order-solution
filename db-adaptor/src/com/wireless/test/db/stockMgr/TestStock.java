package com.wireless.test.db.stockMgr;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.stockMgr.StockInDao;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.stockMgr.StockIn;
import com.wireless.pojo.stockMgr.StockIn.InsertBuilder;
import com.wireless.pojo.stockMgr.StockIn.Status;
import com.wireless.pojo.stockMgr.StockIn.SubType;
import com.wireless.pojo.stockMgr.StockIn.Type;
import com.wireless.pojo.stockMgr.StockInDetail;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestStock {

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
	
	//期望值与真实值的比较
	private void compare(StockIn expected, StockIn actual, boolean isIncludeDetail){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("oriStockId", expected.getOriStockId(), actual.getOriStockId());
		Assert.assertEquals("oriStockIdDate", expected.getOriStockIdDate(), actual.getOriStockIdDate());
		Assert.assertEquals("birthDate", expected.getBirthDate(), actual.getBirthDate());
		Assert.assertEquals("supplier id", expected.getSupplier().getSupplierId(), actual.getSupplier().getSupplierId());
		Assert.assertEquals("supplier name", expected.getSupplier().getName(), actual.getSupplier().getName());
		Assert.assertEquals("deptIn", expected.getDeptIn().getId(), actual.getDeptIn().getId());
		Assert.assertEquals("deptInName", expected.getDeptIn().getName(), actual.getDeptIn().getName());
		Assert.assertEquals("deptOut", expected.getDeptOut().getId(), actual.getDeptOut().getId());
		Assert.assertEquals("deptOutName", expected.getDeptOut().getName(), actual.getDeptOut().getName());
		Assert.assertEquals("operatorId", expected.getOperatorId(), actual.getOperatorId());
		Assert.assertEquals("operator", expected.getOperator(), actual.getOperator());
		Assert.assertEquals("amount", expected.getAmount(), actual.getAmount(),0.0001F);
		Assert.assertEquals("price", expected.getPrice(), actual.getPrice(),0.0001F);
		Assert.assertEquals("type", expected.getType(), actual.getType());
		Assert.assertEquals("subType", expected.getSubType(), actual.getSubType());
		Assert.assertEquals("supplierId", expected.getSupplier().getSupplierId(), actual.getSupplier().getSupplierId());
		Assert.assertEquals("supplierName",expected.getSupplier().getName(), actual.getSupplier().getName());
		if(isIncludeDetail){
			for(StockInDetail expectedDetail : expected.getStockDetails()){
				int index = actual.getStockDetails().indexOf(expectedDetail);
				if(index >= 0){
					Assert.assertEquals("associated stock in id to detail", expectedDetail.getStockInId(), actual.getStockDetails().get(index).getStockInId());
					Assert.assertEquals("associated material id to detail", expectedDetail.getMaterialId(), actual.getStockDetails().get(index).getMaterialId());
					Assert.assertEquals("associated material name to detail", expectedDetail.getName(), actual.getStockDetails().get(index).getName());
					Assert.assertEquals("price to detail", expectedDetail.getPrice(), actual.getStockDetails().get(index).getPrice(), 0.001);
					Assert.assertEquals("amount to detail", expectedDetail.getAmount(), actual.getStockDetails().get(index).getAmount(), 0.001);
				}else{
					Assert.assertTrue("stock in detail", false);
				}
			}
		}
	}
	
	@Test
	public void testStockInDao() throws SQLException, BusinessException{
		 int stockInId = testInsert();
		 testUpdate(stockInId);
		 testDelete(stockInId);
	}
	
	private int testInsert() throws SQLException, BusinessException{
		
		long date = DateUtil.parseDate("2011-09-20");
		InsertBuilder builder = new StockIn.InsertBuilder(37, "abc001").setOriStockIdDate(date)
				.setOperatorId(219).setOperator("小皇").setComment("good").setDeptIn((short) 1).setDeptOut((short) 5)
				.setType(Type.STOCK_IN).setSubType(SubType.GOODS_STOCKIN).setSupplierName("乜记").setSupplierId(3);
		int stockInId = StockInDao.insertStockIn(builder);
		StockIn expected = builder.build();
		expected.setId(stockInId);
		expected.getDeptIn().setName("西式风情");
		expected.getDeptOut().setName("部门5");
		StockIn actual = StockInDao.getStockInById(mTerminal, stockInId);
		//expected.setDeptIn(deptIn)
		compare(expected, actual, false);
		
		return stockInId;
	}
	private void testDelete(int stockInId) throws BusinessException, SQLException{
		StockInDao.deleteStockInById(mTerminal, stockInId);
		
		try{
			StockInDao.getStockInById(mTerminal, stockInId);
		}catch(Exception e){}
	}
	
	private void testUpdate(int stockInId) throws SQLException, BusinessException {
		StockIn stockIn = StockInDao.getStockInById(mTerminal, stockInId);
		long date = DateUtil.parseDate("2014-09-20");
		stockIn.setApprover("阿凯");
		stockIn.setApproverDate(date);
		stockIn.setStatus(Status.AUDIT);
		
		StockInDao.updateStockIn(mTerminal, stockIn);
		
		StockIn actual = StockInDao.getStockInById(mTerminal, stockIn.getId());
		
		compare(stockIn, actual, false);
	}
	
	@Test
	public void testInsertStock() throws SQLException, BusinessException{
		
		Supplier supplier = SupplierDao.getSuppliers(mTerminal, null, null).get(0);

		Department deptIn = DepartmentDao.getDepartments(mTerminal, null, null).get(0);
		Department deptOut = DepartmentDao.getDepartments(mTerminal, null, null).get(1);
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		
		InsertBuilder builder = new StockIn.InsertBuilder(37, "abc001")
										   .setOriStockIdDate(DateUtil.parseDate("2011-09-20"))
										   .setOperatorId(219).setOperator("ediss")
										   .setComment("good")
										   .setDeptIn(deptIn.getId())
										   .setDeptOut(deptOut.getId())
										   .setType(Type.STOCK_IN).setSubType(SubType.GOODS_STOCKIN)
										   .setSupplierId(supplier.getSupplierId())
										   .addDetail(new StockInDetail(0, materials.get(0).getId(), 1.5f, 30))
										   .addDetail(new StockInDetail(0, materials.get(1).getId(), 1.5f, 30));
		
		int stockInId = StockInDao.insertStockIn(builder);
		
		StockIn expected = builder.build();
		expected.setId(stockInId);
		expected.setDeptIn(deptIn);
		expected.setDeptOut(deptOut);
		expected.setSupplier(supplier);
		
		StockIn actual = StockInDao.getStockInById(mTerminal, stockInId);
		compare(expected, actual, true);
		
		//TODO test update
		
		//Delete the stock in record just created &
		//Test whether to delete successfully 
		StockInDao.deleteStockInById(mTerminal, stockInId);
		
		try{
			StockInDao.getStockInById(mTerminal, stockInId);
			Assert.assertTrue("delete stock in record(id = " + stockInId + ") failed", false);
		}catch(BusinessException e){
			
		}
	}
	
	@Test
	public void testDeleteStock() throws BusinessException, SQLException{
		StockInDao.deleteStockInById(mTerminal, 1);
		
		try{
			StockInDao.getStockInById(mTerminal, 1);
		}catch(Exception e){}
	}
	
	@Test
	public void testUpdate() throws SQLException, BusinessException {
		StockIn stockIn = StockInDao.getStockInById(mTerminal, 1);
		long date = DateUtil.parseDate("2015-09-20");
		stockIn.setApprover("阿凯");
		stockIn.setApproverDate(date);
		stockIn.setStatus(Status.AUDIT);
		
		StockInDao.updateStockIn(mTerminal, stockIn);
		
		StockIn actual = StockInDao.getStockInById(mTerminal, stockIn.getId());
		
		compare(stockIn, actual, false);
	}
	
	
}
