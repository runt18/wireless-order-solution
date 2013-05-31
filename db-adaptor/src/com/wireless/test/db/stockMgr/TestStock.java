package com.wireless.test.db.stockMgr;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.StockInDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.StockIn;
import com.wireless.pojo.stockMgr.StockIn.InsertBuilder;
import com.wireless.pojo.stockMgr.StockIn.Status;
import com.wireless.pojo.stockMgr.StockIn.SubType;
import com.wireless.pojo.stockMgr.StockIn.Type;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

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
	private void compare(StockIn expected, StockIn actual){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("oriStockId", expected.getOriStockId(), actual.getOriStockId());
		Assert.assertEquals("oriStockIdDate", expected.getOriStockIdDate(), actual.getOriStockIdDate());
		Assert.assertEquals("birthDate", expected.getBirthDate(), actual.getBirthDate());
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
		compare(expected, actual);
		
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
		
		compare(stockIn, actual);
	}
	
	@Test
	public void testInsertStock() throws SQLException, BusinessException{
		
		long date = DateUtil.parseDate("2011-09-20");
		InsertBuilder builder = new StockIn.InsertBuilder(37, "abc001").setOriStockIdDate(date)
				.setOperatorId(219).setOperator("edison").setComment("good").setDeptIn((short) 1).setDeptOut((short) 5)
				.setType(Type.STOCK_IN).setSubType(SubType.GOODS_STOCKIN).setSupplierName("乜记").setSupplierId(3);
		int stockInId = StockInDao.insertStockIn(builder);
		StockIn expected = builder.build();
		expected.setId(stockInId);
		expected.getDeptIn().setName("西式风情");
		expected.getDeptOut().setName("部门5");
		StockIn actual = StockInDao.getStockInById(mTerminal, stockInId);
		//expected.setDeptIn(deptIn)
		compare(expected, actual);
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
		
		compare(stockIn, actual);
	}
	
	
}
