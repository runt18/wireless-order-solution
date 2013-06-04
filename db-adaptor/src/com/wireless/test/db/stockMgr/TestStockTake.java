package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
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
import com.wireless.db.stockMgr.StockTakeDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.stockMgr.StockTake;
import com.wireless.pojo.stockMgr.StockTake.InsertBuilder;
import com.wireless.pojo.stockMgr.StockTake.Status;
import com.wireless.pojo.stockMgr.StockTake.UpdateBuilder;
import com.wireless.pojo.stockMgr.StockTakeDetail;
import com.wireless.pojo.stockMgr.StockTakeDetail.InsertStockTakeDetail;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestStockTake {

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
	public void compare(StockTake expected, StockTake actual, boolean isIncludeStockTakeDetail){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("deptId", expected.getDept().getId(), actual.getDept().getId());
		Assert.assertEquals("deptName", expected.getDept().getName(), actual.getDept().getName());
		Assert.assertEquals("materialCateId", actual.getMaterialCateId());
		Assert.assertEquals("status", expected.getStatus(), actual.getStatus());
		Assert.assertEquals("parentId", expected.getParentId(), actual.getParentId());
		Assert.assertEquals("operator", actual.getOperator());
		Assert.assertEquals("operatorId", actual.getOperatorId());
		Assert.assertEquals("startDate", expected.getStartDate(), actual.getStartDate());
		Assert.assertEquals("finishDate", expected.getFinishDate(), actual.getFinishDate());
		Assert.assertEquals("comment", expected.getComment(), actual.getComment());
		
		if(isIncludeStockTakeDetail){
			for (StockTakeDetail stockTakeDetail : expected.getStockTakeDetails()) {
				int index = actual.getStockTakeDetails().indexOf(stockTakeDetail);
				if(index >= 0){
					Assert.assertEquals("id", stockTakeDetail.getId(), actual.getStockTakeDetails().get(index).getId());
					Assert.assertEquals("stockTakeId", stockTakeDetail.getStockTakeId(), actual.getStockTakeDetails().get(index).getStockTakeId());
					Assert.assertEquals("materialId", stockTakeDetail.getMaterial().getId(), actual.getStockTakeDetails().get(index).getMaterial().getId());
					Assert.assertEquals("materialName", stockTakeDetail.getMaterial().getName(), actual.getStockTakeDetails().get(index).getMaterial().getName());
					Assert.assertEquals("actualAmount", stockTakeDetail.getActualAmount(), actual.getStockTakeDetails().get(index).getActualAmount(), 0.0001f);
					Assert.assertEquals("expectAmount", stockTakeDetail.getExpectAmount(), actual.getStockTakeDetails().get(index).getExpectAmount(), 0.0001f);
					Assert.assertEquals("deltaAmount", stockTakeDetail.getDeltaAmount(), actual.getStockTakeDetails().get(index).getDeltaAmount(), 0.0001f);
				}
			}
		}
	}
	@Test
	public void testInset() throws SQLException, BusinessException{
		Department dept = null;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.size() == 0){
			System.out.println("还没添加任何部门");
		}else{
			dept = depts.get(1);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);

		
		InsertBuilder builder = new InsertBuilder(mTerminal.restaurantID)
									.setMaterialCateId(1)
									.setDept(dept)
									.setStatus(Status.CHECKING)
									.setParentId(2)
									.setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
									.setStartDate(DateUtil.parseDate("2013-08-19 14:30:29"))
									.setComment("盘点八月份的")
									.addStockTakeDetail(new InsertStockTakeDetail().setMaterial(materials.get(0)).setExpectAmount(10).setActualAmount(9).build())
									.addStockTakeDetail(new InsertStockTakeDetail().setMaterial(materials.get(0)).setExpectAmount(20).setActualAmount(21).build());
									
		
		final int id = StockTakeDao.insertStockTake(mTerminal, builder);
		
		StockTake expected = builder.build();
		expected.setId(id);
		StockTake actual = StockTakeDao.getStockTakeById(mTerminal, id);
		
		compare(expected, actual, true);
	}
	
	
	@Test
	public void testStockTake() throws SQLException, BusinessException{
		Department dept = null;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.size() == 0){
			System.out.println("还没添加任何部门");
		}else{
			dept = depts.get(1);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		
		InsertBuilder builder = new InsertBuilder(mTerminal.restaurantID)
									.setMaterialCateId(1)
									.setDept(dept)
									.setStatus(Status.CHECKING)
									.setParentId(2)
									.setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
									.setStartDate(DateUtil.parseDate("2013-08-19 14:30:29"))
									.setComment("盘点八月份的")
									.addStockTakeDetail(new InsertStockTakeDetail().setMaterial(materials.get(0)).setExpectAmount(10).setActualAmount(9).build())
									.addStockTakeDetail(new InsertStockTakeDetail().setMaterial(materials.get(0)).setExpectAmount(20).setActualAmount(21).build());
									
		
		final int id = StockTakeDao.insertStockTake(mTerminal, builder);
		
		StockTake expected = builder.build();
		expected.setId(id);
		StockTake actual = StockTakeDao.getStockTakeById(mTerminal, id);
		
		compare(expected, actual, true);
		
		expected = actual;
		expected.setApprover(mTerminal.owner);
		expected.setApproverId((int) mTerminal.pin);
		expected.setFinishDate(DateUtil.parseDate("2013-08-18 12:12:12"));
		expected.setStatus(Status.AUDIT);
		
		UpdateBuilder uBuilder = new UpdateBuilder(id)
									.setApproverId((int) mTerminal.pin).setApprover(mTerminal.owner)
									.setFinishDate(DateUtil.parseDate("2013-08-18 12:12:12"))
									.setStatus(Status.AUDIT);
		StockTakeDao.updateStockTake(mTerminal, uBuilder);
		
		actual = StockTakeDao.getStockTakeById(mTerminal, id);
		
		compare(expected, actual, true);
		
		StockTakeDao.deleteStockTake(mTerminal, id);
		
		try{
			StockTakeDao.getStockTakeById(mTerminal, id);
			Assert.assertTrue("delete stock in record(id = " + id + ") failed", false);
		}catch(Exception e){}
											
	}
	
	
	
}
