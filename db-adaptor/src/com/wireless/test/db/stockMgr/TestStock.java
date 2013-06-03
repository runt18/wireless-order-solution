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
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.CateType;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockAction.Status;
import com.wireless.pojo.stockMgr.StockAction.SubType;
import com.wireless.pojo.stockMgr.StockAction.Type;
import com.wireless.pojo.stockMgr.StockAction.UpdateBuilder;
import com.wireless.pojo.stockMgr.StockActionDetail;
import com.wireless.pojo.supplierMgr.Supplier;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;
import com.wireless.util.SQLUtil;

public class TestStock {

	private static Terminal mTerminal;
	
	@BeforeClass
	public static void initDBParam() throws BusinessException, SQLException, PropertyVetoException{
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
	private void compare(StockAction expected, StockAction actual, boolean isIncludeDetail){
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
		Assert.assertEquals("amount", expected.getTotalAmount(), actual.getTotalAmount(),0.0001F);
		Assert.assertEquals("price", expected.getTotalPrice(), actual.getTotalPrice(),0.0001F);
		Assert.assertEquals("type", expected.getType(), actual.getType());
		Assert.assertEquals("subType", expected.getSubType(), actual.getSubType());
		Assert.assertEquals("supplierId", expected.getSupplier().getSupplierId(), actual.getSupplier().getSupplierId());
		Assert.assertEquals("supplierName",expected.getSupplier().getName(), actual.getSupplier().getName());
		if(isIncludeDetail){
			for(StockActionDetail expectedDetail : expected.getStockDetails()){
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
	public void testInsertStock() throws SQLException, BusinessException{
		
		Supplier supplier = SupplierDao.getSuppliers(mTerminal, null, null).get(0);

		Department deptIn = DepartmentDao.getDepartments(mTerminal, null, null).get(1);
		Department deptOut = DepartmentDao.getDepartments(mTerminal, null, null).get(2);
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		
		InsertBuilder builder = new StockAction.InsertBuilder(37, "abc10000")
										   .setOriStockIdDate(DateUtil.parseDate("2011-09-20"))
										   .setOperatorId(219).setOperator("ediss")
										   .setComment("good")
										   .setDeptIn(deptIn.getId())
										   .setDeptOut(deptOut.getId())
										   .setType(Type.STOCK_IN).setSubType(SubType.STOCK_IN).setCateType(CateType.GOOD)
										   .setSupplierId(supplier.getSupplierId())
										   .addDetail(new StockActionDetail(materials.get(0).getId(), materials.get(0).getName(), 1.5f, 30))
										   .addDetail(new StockActionDetail(materials.get(1).getId(), materials.get(1).getName(), 1.5f, 30));
		
		final int stockInId = StockActionDao.insertStockIn(mTerminal, builder);
		
		StockAction expected = builder.build();
		expected.setId(stockInId);
		expected.setDeptIn(deptIn);
		expected.setDeptOut(deptOut);
		expected.setSupplier(supplier);
		
		StockAction actual = StockActionDao.getStockAndDetailById(mTerminal, stockInId);
		compare(expected, actual, true);
		//TODO test update
		
		expected = actual;
		UpdateBuilder uBuilder = new StockAction.UpdateBuilder(expected.getId())
									.setApprover("兰戈2")
									.setApproverId(12)
									.setApproverDate(DateUtil.parseDate("2013-06-03"))
									.setStatus(Status.AUDIT);
		
		expected.setApprover("兰戈2");
		expected.setApproverId(12);
		expected.setApproverDate(DateUtil.parseDate("2013-06-03"));
		expected.setStatus(Status.AUDIT);
		
		StockActionDao.updateStockIn(mTerminal, uBuilder);
		
		actual = StockActionDao.getStockAndDetailById(mTerminal, uBuilder.getId());
		
		compare(expected, actual, true);

		StockActionDao.deleteStockInById(mTerminal, stockInId);
		
		try{
			StockActionDao.getStockInById(mTerminal, stockInId);
			//Assert.assertTrue("delete stock in record(id = " + stockInId + ") failed", false);
		}catch(BusinessException e){
			
		}
	}
	

	@Test
	public void testInsert() throws SQLException, BusinessException{
		
		Supplier supplier = SupplierDao.getSuppliers(mTerminal, null, null).get(0);

		Department deptIn = DepartmentDao.getDepartments(mTerminal, null, null).get(1);
		Department deptOut = DepartmentDao.getDepartments(mTerminal, null, null).get(2);
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		
		InsertBuilder builder = new StockAction.InsertBuilder(37, "abc10000")
										   .setOriStockIdDate(DateUtil.parseDate("2011-09-20"))
										   .setOperatorId(219).setOperator("ediss")
										   .setComment("very good")
										   .setDeptIn(deptIn.getId())
										   .setDeptOut(deptOut.getId())
										   .setType(Type.STOCK_IN).setSubType(SubType.STOCK_IN).setCateType(CateType.GOOD)
										   .setSupplierId(supplier.getSupplierId())
										   .addDetail(new StockActionDetail(materials.get(0).getId(), materials.get(0).getName(), 1.5f, 30))
										   .addDetail(new StockActionDetail(materials.get(1).getId(), materials.get(1).getName(), 1.5f, 30));
		
		final int stockInId = StockActionDao.insertStockIn(mTerminal, builder);
		
		StockAction expected = builder.build();
		expected.setId(stockInId);
		expected.setDeptIn(deptIn);
		expected.setDeptOut(deptOut);
		expected.setSupplier(supplier);
		
		StockAction actual = StockActionDao.getStockAndDetailById(mTerminal, stockInId);
		compare(expected, actual, true);
	}
	
	@Test
	public void testDelete() throws BusinessException, SQLException{
		StockActionDao.deleteStockInById(mTerminal, 1);
		
		try{
			StockActionDao.getStockInById(mTerminal, 1);
		}catch(Exception e){}
	}
	
	

	
	
}
