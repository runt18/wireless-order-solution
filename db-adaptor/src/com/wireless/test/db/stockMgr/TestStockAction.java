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
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.db.stockMgr.StockActionDao;
import com.wireless.db.supplierMgr.SupplierDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.stockMgr.MaterialDept;
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

public class TestStockAction {

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
	
	
	public void compareMaterial(Material expected, Material actual){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("stock", expected.getStock(), actual.getStock(), 0.0001);
		
	};
	
	public void compareMaterialDept(MaterialDept expected, MaterialDept actual){
		Assert.assertEquals("materialId", expected.getMaterialId(), actual.getMaterialId());
		Assert.assertEquals("deptId", expected.getDeptId(), actual.getDeptId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("stock", expected.getStock(), actual.getStock(),0.0001);
	}
	
	//期望值与真实值的比较
	private void compare(StockAction expected, StockAction actual, boolean isIncludeDetail){
		Assert.assertEquals("id", expected.getId(), actual.getId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("oriStockId", expected.getOriStockId(), actual.getOriStockId());
		Assert.assertEquals("oriStockIdDate", expected.getOriStockIdDate(), actual.getOriStockIdDate());
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
	public void testInsert() throws SQLException, BusinessException{
		Supplier supplier = null;
		List<Supplier> suppliers = SupplierDao.getSuppliers(mTerminal, null, null);
		if(suppliers.isEmpty()){
			throw new BusinessException("没有添加任何供应商!");
		}else{
			supplier = suppliers.get(0);
		}

		Department deptIn = null;
		Department deptOut = null;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.isEmpty()){
			throw new BusinessException("还没添加任何部门!");
		}else{
			deptIn = depts.get(1);
			deptOut = depts.get(2);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		
		InsertBuilder builder = new StockAction.InsertBuilder(mTerminal.restaurantID, "abc10000")
										   .setOriStockIdDate(DateUtil.parseDate("2014-09-20 11:33:34"))
										   .setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
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
		StockActionDao.deleteStockInById(mTerminal, 61);
		
		try{
			StockActionDao.getStockInById(mTerminal, 61);
		}catch(Exception e){}
	}

	@Test
	public void testStockDao() throws SQLException, BusinessException{
		Supplier supplier;
		List<Supplier> suppliers = SupplierDao.getSuppliers(mTerminal, null, null);
		if(suppliers.isEmpty()){
			throw new BusinessException("没有添加任何供应商!");
		}else{
			supplier = suppliers.get(0);
		}

		Department deptIn;
		Department deptOut;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.isEmpty()){
			throw new BusinessException("还没添加任何部门!");
		}else{
			deptIn = depts.get(1);
			deptOut = depts.get(2);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		//添加一张库存单
		InsertBuilder builder = new StockAction.InsertBuilder(mTerminal.restaurantID, "abc10000")
										   .setOriStockIdDate(DateUtil.parseDate("2011-09-20 11:33:34"))
										   .setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
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

		//审核库存
		expected = actual;
		UpdateBuilder uBuilder = new StockAction.UpdateBuilder(expected.getId())
									.setApprover("兰戈2")
									.setApproverId(12)
									.setApproverDate(DateUtil.parseDate("2013-06-03"))
									.setStatus(Status.AUDIT);
		//做对比数据之用
		expected.setApprover("兰戈2");
		expected.setApproverId(12);
		expected.setApproverDate(DateUtil.parseDate("2013-06-03"));
		expected.setStatus(Status.AUDIT);
		
		StockActionDao.updateStockIn(mTerminal, uBuilder);
		
		actual = StockActionDao.getStockAndDetailById(mTerminal, uBuilder.getId());
		//对比审核后期望与真实值
		compare(expected, actual, true);
		
		//审核完成,与部门库存,商品原料库存对接
		if(actual.getStatus() == Status.AUDIT){		
			int deptId;
			for (StockActionDetail sActionDetail : actual.getStockDetails()) {
				MaterialDept materialDept;
				List<MaterialDept> materialDepts;
				Material material;
				//判断是入库还是出库单
				if(actual.getType() == Type.STOCK_IN){
					deptId = actual.getDeptIn().getId();
					
					materialDepts = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + sActionDetail.getMaterialId() + " AND dept_id = " + deptId, null);
					//判断此部门下是否添加了这个原料
					if(materialDepts.isEmpty()){
						//如果没有就新增一条记录
						materialDept = new MaterialDept(sActionDetail.getMaterialId(), deptId, mTerminal.restaurantID, sActionDetail.getAmount());
						MaterialDeptDao.insertMaterialDept(mTerminal, materialDept);
						
						MaterialDept MaterialDeptactual = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + materialDept.getMaterialId() + " AND dept_id = " + materialDept.getDeptId(), null).get(0);
						//对比新增记录
						compareMaterialDept(materialDept, MaterialDeptactual);

					}else{
						materialDept = materialDepts.get(0);
						//入库单增加库存
						materialDept.plusStock(sActionDetail.getAmount());
					}
					material = MaterialDao.getById(materialDept.getMaterialId());

					material.plusStock(sActionDetail.getAmount());					
				}else{
					deptId = actual.getDeptIn().getId();
					
					materialDepts = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + sActionDetail.getMaterialId() + " AND dept_id = " + deptId, null);
					if(materialDepts.isEmpty()){
						throw new BusinessException("此部门下还没添加这个原料!");
					}else{
						materialDept = materialDepts.get(0);
					}
					material = MaterialDao.getById(materialDept.getMaterialId());
					//出库单减少库存
					materialDept.cutStock(sActionDetail.getAmount());
					material.cutStock(sActionDetail.getAmount());
				}
				
				MaterialDeptDao.updateMaterialDept(mTerminal, materialDept);
				//更新原料_部门表后对比			
				MaterialDept materialDeptActual = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + materialDept.getMaterialId() + " AND dept_id = " + materialDept.getDeptId(), null).get(0);
				compareMaterialDept(materialDept, materialDeptActual);
				
				material.setLastModStaff(mTerminal.owner);
				MaterialDao.update(material);	
				//更新原料表后对比
				Material MaterialActual = MaterialDao.getById(material.getId());
				compareMaterial(material, MaterialActual);

			}	
			
		}
	
		
/*		StockActionDao.deleteStockInById(mTerminal, stockInId);
		
		try{
			StockActionDao.getStockInById(mTerminal, stockInId);
			Assert.assertTrue("delete stock in record(id = " + stockInId + ") failed", false);
		}catch(BusinessException e){
			
		}*/
	}
	


	
	

	
	
}
