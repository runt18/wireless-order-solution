package com.wireless.test.db.regionMgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestTableDao {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException {
		TestInit.init();
		try {
			mStaff = StaffDao.getStaffs(37).get(0);
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
		
		Table oriTbl = TableDao.getTables(mStaff, null, null).get(0);
		
		Table.UpdateBuilder builder = new Table.UpdateBuilder(oriTbl.getTableId()).setMiniCost(20)
																				  .setServiceRate(0.1f)
																				  .setTableName("测试餐台")
																				  .setRegionId(Region.RegionId.REGION_1.getId());
		Table expected = builder.build();
		TableDao.updateById(mStaff, expected);
		
		expected.setTableAlias(oriTbl.getAliasId());
		expected.setCategory(oriTbl.getCategory());
		expected.setCustomNum(oriTbl.getCustomNum());
		expected.setStatus(oriTbl.getStatus());
		expected.setRestaurantId(oriTbl.getRestaurantId());
		
		Table actual = TableDao.getTableById(mStaff, expected.getTableId());
		
		compare(expected, actual);
		
		//Restore the original table
		TableDao.updateById(mStaff, oriTbl);
	}
	
	@Test
	public void testInsert() throws BusinessException, SQLException{
		
		//Create and insert a new table
		Table.InsertBuilder builder = new Table.InsertBuilder(12345, mStaff.getRestaurantId(), Region.RegionId.REGION_1.getId()).setMiniCost(200)
								  																			 .setServiceRate(0.1f)
								  																			 .setTableName("测试餐台");
		
		Table expected = TableDao.insert(mStaff, builder);
		
		Table actual = TableDao.getTableById(mStaff, expected.getTableId());
		
		compare(expected, actual);
		
		//Delete the table just created.
		TableDao.deleteById(mStaff, expected.getTableId());
		
		try{
			TableDao.getTableById(mStaff, expected.getTableId());
			assertTrue("fail to delete table", true);
		}catch(BusinessException e){
			
		}
		
		try{
			TableDao.getTableById(mStaff, expected.getTableId());
			assertTrue("fail to delete table", true);
		}catch(BusinessException e){
			
		}
	}
}
