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
	@SuppressWarnings("deprecation")
	private void compare(StockIn expected, StockIn actual){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("oriStockId", expected.getOriStockId(), actual.getOriStockId());
		Assert.assertEquals("deptIn", expected.getDeptIn(), actual.getDeptIn());
		Assert.assertEquals("deptOut", expected.getDeptOut(), actual.getDeptOut());
		Assert.assertEquals("price", expected.getPrice(), actual.getPrice());
		Assert.assertEquals("operator", expected.getOperator(), actual.getOperator());
		Assert.assertEquals("amount", expected.getAmount(), actual.getAmount());
		
	}
	
	@Test
	public void testInsert() throws SQLException, BusinessException{
		InsertBuilder builder = new StockIn.InsertBuilder(37, "abc123").setDeptIn(2).setDeptOut(3)
				.setOperatorId(219).setOperator("小李").setOperateDate(20130528).setComment("good")
				.setType(1).setSubType(1);
		
		int stockInId = StockInDao.insertStockIn(builder);
		
		StockIn actual = StockInDao.getStockInById(mTerminal, stockInId);
		
		compare(builder.build(), actual);
	}
	
	
	
}
