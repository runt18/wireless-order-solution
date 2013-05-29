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
import com.wireless.pojo.stockMgr.StockIn.SubType;
import com.wireless.pojo.stockMgr.StockIn.Type;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestStockMgr {

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
		Assert.assertEquals("deptIn", expected.getDeptIn(), actual.getDeptIn());
		Assert.assertEquals("deptOut", expected.getDeptOut(), actual.getDeptOut());
		Assert.assertEquals("operator", expected.getOperator(), actual.getOperator());
		Assert.assertEquals("amount", expected.getAmount(), actual.getAmount(),0.0001F);
		Assert.assertEquals("price", expected.getPrice(), actual.getPrice(),0.0001F);
		Assert.assertEquals("type", expected.getType(), actual.getType());
		Assert.assertEquals("subType", expected.getSubType(), actual.getSubType());
		
		
		
		
	}
	
	@Test
	public void testStockInDao() throws SQLException, BusinessException{
		 int stockInId = testInsert();
		 testDelete(stockInId);
	}
	
	private int testInsert() throws SQLException, BusinessException{
		InsertBuilder builder = new StockIn.InsertBuilder(37, "abc001")
				.setOperatorId(219).setOperator("小李").setComment("good").setDeptIn((short) 1).setDeptOut((short) 5)
				.setType(Type.STOCK_IN).setSubType(SubType.GOODS_STOCKIN).setSupplierName("乜记");
		int stockInId = StockInDao.insertStockIn(builder);
		
		StockIn actual = StockInDao.getStockInById(mTerminal, stockInId);
		
		compare(builder.build(), actual);
		
		return stockInId;
	}
	
	private void testDelete(int stockInId) throws BusinessException, SQLException{
		StockInDao.deleteStockInById(mTerminal, stockInId);
		
		try{
			StockInDao.getStockInById(mTerminal, stockInId);
		}catch(Exception e){}
	}
	
	
	
}
