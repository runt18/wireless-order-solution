package com.wireless.test.db.regionMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestTableDao {
	
	private static Terminal mTerminal;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException {
		TestInit.init();
		try {
			mTerminal = VerifyPin.exec(217, Terminal.MODEL_STAFF);
		} catch (BusinessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void compare(Table expected, Table actual){
		assertEquals("table id", expected.getTableId(), actual.getTableId());
		assertEquals("table_alias", expected.getAliasId(), actual.getAliasId());
		assertEquals("table category", expected.getCategory(), actual.getCategory());
		assertEquals("table custom number", expected.getCustomNum(), actual.getCustomNum());
		assertEquals("table status", expected.getStatus(), actual.getStatus());
		assertEquals("table minimum cost", expected.getMinimumCost(), actual.getMinimumCost(), 0.01);
		assertEquals("associated restaurant id", expected.getRestaurantId(), actual.getRestaurantId(), 0.01);
		assertEquals("table service rate", expected.getServiceRate(), actual.getServiceRate(), 0.01);
		assertEquals("table name", expected.getName(), actual.getName());
		assertEquals("associated region id", expected.getRegion().getRegionId(), actual.getRegion().getRegionId());
	}
	
	@Test
	public void testUpdate() throws BusinessException, SQLException{
		
		Table oriTbl = TableDao.getTables(mTerminal, null, null).get(0);
		
		Table.UpdateBuilder builder = new Table.UpdateBuilder(oriTbl.getTableId()).setMiniCost(20)
																				  .setServiceRate(0.1f)
																				  .setTableName("测试餐台")
																				  .setRegionId(Region.REGION_1);
		Table expected = builder.build();
		TableDao.updateById(mTerminal, expected);
		
		expected.setTableAlias(oriTbl.getAliasId());
		expected.setCategory(oriTbl.getCategory());
		expected.setCustomNum(oriTbl.getCustomNum());
		expected.setStatus(oriTbl.getStatus());
		expected.setRestaurantId(oriTbl.getRestaurantId());
		
		Table actual = TableDao.getTableById(mTerminal, expected.getTableId());
		
		compare(expected, actual);
		
		//Restore the original table
		TableDao.updateById(mTerminal, oriTbl);
	}
	
	@Test
	public void testInsert() throws BusinessException, SQLException{
		
		//Create and insert a new table
		Table.InsertBuilder builder = new Table.InsertBuilder(12345, mTerminal.restaurantID, Region.REGION_1).setMiniCost(200)
								  																			 .setServiceRate(0.1f)
								  																			 .setTableName("测试餐台");
		
		Table expected = TableDao.insert(mTerminal, builder);
		
		Table actual = TableDao.getTableById(mTerminal, expected.getTableId());
		
		compare(expected, actual);
		
		//Delete the table just created.
		TableDao.deleteById(mTerminal, expected.getTableId());
		
		try{
			TableDao.getTableById(mTerminal, expected.getTableId());
			assertTrue("fail to delete table", true);
		}catch(BusinessException e){
			
		}
		
		try{
			TableDao.getTableById(mTerminal, expected.getTableId());
			assertTrue("fail to delete table", true);
		}catch(BusinessException e){
			
		}
	}
}
