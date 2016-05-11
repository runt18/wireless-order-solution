package com.wireless.test.db.regionMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TableError;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.SortedList;
import com.wireless.test.db.TestInit;

public class TestTableDao {
	
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException {
		TestInit.init();
		try {
			mStaff = StaffDao.getAdminByRestaurant(37);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void compare(Table expected, Table actual){
		Assert.assertEquals("table id", expected.getId(), actual.getId());
		Assert.assertEquals("table_alias", expected.getAliasId(), actual.getAliasId());
		Assert.assertEquals("table category", expected.getCategory(), actual.getCategory());
		Assert.assertEquals("table custom number", expected.getCustomNum(), actual.getCustomNum());
		Assert.assertEquals("table status", expected.getStatus(), actual.getStatus());
		Assert.assertEquals("table minimum cost", expected.getMinimumCost(), actual.getMinimumCost(), 0.01);
		Assert.assertEquals("associated restaurant id", expected.getRestaurantId(), actual.getRestaurantId(), 0.01);
		Assert.assertEquals("table name", expected.getName(), actual.getName());
		Assert.assertEquals("associated region id", expected.getRegion().getId(), actual.getRegion().getId());
	}
	
	@Test
	public void testTableDao() throws BusinessException, SQLException{
		
		int tableId = 0;
		
		try{
			//----------Test to insert a new table---------------------
			Table.InsertBuilder builder = new Table.InsertBuilder(12345, Region.RegionId.REGION_1).setMiniCost(200).setTableName("测试餐台");
			tableId = TableDao.insert(mStaff, builder);
			
			Table expected = builder.build();
			expected.setId(tableId);
			expected.setRestaurantId(mStaff.getRestaurantId());
			
			Table actual = TableDao.getById(mStaff, expected.getId());
			
			compare(expected, actual);
			
			//----------Test to update a new table---------------------
			Table.UpdateBuilder updateBuilder = new Table.UpdateBuilder(tableId).setMiniCost(20).setTableName("修改测试餐台").setRegionId(Region.RegionId.REGION_2);
			TableDao.update(mStaff, updateBuilder);
			
			expected = updateBuilder.build();
			expected.setRestaurantId(mStaff.getRestaurantId());
			expected.setTableAlias(actual.getAliasId());

			actual = TableDao.getById(mStaff, expected.getId());
			
			compare(expected, actual);

		}finally{
			if(tableId != 0){
				TableDao.deleteById(mStaff, tableId);
				try{
					TableDao.getById(mStaff, tableId);
					Assert.assertTrue("failed to delete the table", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete the table", TableError.TABLE_NOT_EXIST, e.getErrCode());
				}
			}
		}
	}
	
	@Test
	public void testBatchInsertDao() throws BusinessException, SQLException{
		final int start = 10000;
		final int end = 10010;
		try{
			Table.BatchInsertBuilder insertBuilder = new Table.BatchInsertBuilder(start, end, Region.RegionId.REGION_1);
			TableDao.insert(mStaff, insertBuilder);
			
			List<Table> expected = SortedList.newInstance();
			for(Table.InsertBuilder singleBuilder :insertBuilder.build()){
				Table t = singleBuilder.build();
				t.setRestaurantId(mStaff.getRestaurantId());
				expected.add(t);
			}
			
			List<Table> actual = SortedList.newInstance();
			for(int i = start; i <= end; i++){
				actual.add(TableDao.getByAlias(mStaff, i));
			}
			
			Assert.assertEquals(expected, actual);
		}finally{
			for(int i = start; i <= end; i++){
				TableDao.deleteByAliasId(mStaff, i);
				try{
					TableDao.getByAlias(mStaff, i);
					Assert.assertTrue("failed to delete table[" + i + "]", false);
				}catch(BusinessException e){
					Assert.assertEquals("failed to delete table[" + i + "]", TableError.TABLE_NOT_EXIST, e.getErrCode());
				}
			}
			
		}
	}
}
