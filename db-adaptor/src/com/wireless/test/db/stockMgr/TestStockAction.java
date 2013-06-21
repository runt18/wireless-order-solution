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
import com.wireless.exception.DeptError;
import com.wireless.exception.MaterialError;
import com.wireless.exception.StockError;
import com.wireless.exception.SupplierError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.pojo.stockMgr.StockAction;
import com.wireless.pojo.stockMgr.StockAction.AuditBuilder;
import com.wireless.pojo.stockMgr.StockAction.CateType;
import com.wireless.pojo.stockMgr.StockAction.InsertBuilder;
import com.wireless.pojo.stockMgr.StockAction.Status;
import com.wireless.pojo.stockMgr.StockAction.SubType;
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
		if(actual.getDeptIn().getId() != 0){
			Assert.assertEquals("deptIn", expected.getDeptIn().getId(), actual.getDeptIn().getId());
			Assert.assertEquals("deptInName", expected.getDeptIn().getName(), actual.getDeptIn().getName());
		}
		if(actual.getDeptOut().getId() !=0){
			Assert.assertEquals("deptOut", expected.getDeptOut().getId(), actual.getDeptOut().getId());
			Assert.assertEquals("deptOutName", expected.getDeptOut().getName(), actual.getDeptOut().getName());
		}
		if(actual.getSupplier().getSupplierId() !=0){
			Assert.assertEquals("supplierId", expected.getSupplier().getSupplierId(), actual.getSupplier().getSupplierId());
			Assert.assertEquals("supplierName",expected.getSupplier().getName(), actual.getSupplier().getName());
		}
		Assert.assertEquals("operatorId", expected.getOperatorId(), actual.getOperatorId());
		Assert.assertEquals("operator", expected.getOperator(), actual.getOperator());
		Assert.assertEquals("approverId", expected.getApproverId(), actual.getApproverId());
		Assert.assertEquals("approver", expected.getApprover(), actual.getApprover());
		//Assert.assertEquals("amount", expected.getTotalAmount(), actual.getTotalAmount(),0.0001F);
		//Assert.assertEquals("price", expected.getTotalPrice(), actual.getTotalPrice(),0.0001F);
		Assert.assertEquals("status", expected.getStatus(), actual.getStatus());
		Assert.assertEquals("type", expected.getType(), actual.getType());
		Assert.assertEquals("subType", expected.getSubType(), actual.getSubType());

		if(isIncludeDetail){
			for(StockActionDetail expectedDetail : expected.getStockDetails()){
				int index = actual.getStockDetails().indexOf(expectedDetail);
				if(index >= 0){
					Assert.assertEquals("associated stock_action id to detail", expectedDetail.getStockActionId(), actual.getStockDetails().get(index).getStockActionId());
					Assert.assertEquals("associated material id to detail", expectedDetail.getMaterialId(), actual.getStockDetails().get(index).getMaterialId());
					Assert.assertEquals("price to detail", expectedDetail.getPrice(), actual.getStockDetails().get(index).getPrice(), 0.001);
					Assert.assertEquals("amount to detail", expectedDetail.getAmount(), actual.getStockDetails().get(index).getAmount(), 0.001);
				}else{
					Assert.assertTrue("stock action detail", false);
				}
			}
		}
		
	}

	private void testInsert(InsertBuilder builder) throws SQLException, BusinessException{

		//添加一张入库存单
	
		final int stockActionId = StockActionDao.insertStockAction(mTerminal, builder);
		
		StockAction expected = builder.build();
		//根据不同类型获取供应商或部门
		if(builder.getSubType() == SubType.STOCK_IN ){
			Department deptIn = DepartmentDao.getDepartmentById(mTerminal, builder.getDeptIn().getId());
			Supplier supplier = SupplierDao.getSupplierById(mTerminal, builder.getSupplier().getSupplierId());
			expected.setDeptIn(deptIn);
			expected.setSupplier(supplier);
		}else if(builder.getSubType() == SubType.STOCK_OUT){
			Department deptOut = DepartmentDao.getDepartmentById(mTerminal, builder.getDeptOut().getId());
			Supplier supplier = SupplierDao.getSupplierById(mTerminal, builder.getSupplier().getSupplierId());
			expected.setDeptOut(deptOut);
			expected.setSupplier(supplier);
		}else if(builder.getSubType() == SubType.STOCK_IN_TRANSFER || builder.getSubType() == SubType.STOCK_OUT_TRANSFER){
			Department deptIn = DepartmentDao.getDepartmentById(mTerminal, builder.getDeptIn().getId());
			Department deptOut = DepartmentDao.getDepartmentById(mTerminal, builder.getDeptOut().getId());
			expected.setDeptIn(deptIn);
			expected.setDeptOut(deptOut);
		}else{
			Department deptIn = DepartmentDao.getDepartmentById(mTerminal, builder.getDeptIn().getId());
			expected.setDeptIn(deptIn);
		}
		expected.setId(stockActionId);

		
		StockAction actual = StockActionDao.getStockAndDetailById(mTerminal, stockActionId);
		//compare(expected, actual, true);
		
		
		InsertBuilder updatebuilder = StockAction.InsertBuilder.newStockIn(mTerminal.restaurantID, DateUtil.parseDate("2013-09-29 12:12:12"))
				   .setOriStockId("aaa12000")
				   .setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
				   .setComment("good hting")
				   .setDeptIn(actual.getDeptIn().getId())
				   .setCateType(CateType.GOOD)
				   .setSupplierId(actual.getSupplier().getSupplierId())
				   .addDetail(new StockActionDetail(2, 30f, 90))
					.addDetail(new StockActionDetail(4, 30f, 30));
		StockActionDao.updateStockAction(mTerminal, actual.getId(), updatebuilder);
		//expected = updatebuilder.build();
	
		
		
		//在审核时先获取之前的数据以作对比
		Map<Object, Object> param = new HashMap<Object, Object>();
		param.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> beforeMaterials = MaterialDao.getContent(param);
		
		List<MaterialDept> beforeMaterialDepts = MaterialDeptDao.getMaterialDepts(mTerminal, " AND restaurant_id = " + mTerminal.restaurantID, null);
		
/*		for (StockActionDetail stockActionDetail : actual.getStockDetails()) {
			Map<Object, Object> param = new HashMap<Object, Object>();
			param.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID + " AND M.material_id = " + stockActionDetail.getMaterialId());
			Material beforeMaterial = MaterialDao.getContent(param).get(0);
			beforeMaterials.add(beforeMaterial);
		}*/
		//审核库存
		expected = actual;
		AuditBuilder uBuilder = StockAction.AuditBuilder.newStockActionAudit(expected.getId())
								.setApprover("兰戈")
								.setApproverId(12);
		//做对比数据之用
		expected.setApprover("兰戈");
		expected.setApproverId(12);
		expected.setStatus(Status.AUDIT);
		expected.setOriStockId("aaa12000");
		expected.setComment("good hting");
		expected.setOriStockIdDate(DateUtil.parseDate("2013-09-29 12:12:12"));
		
		//审核
		StockActionDao.auditStockAction(mTerminal, uBuilder);
		
		actual = StockActionDao.getStockAndDetailById(mTerminal, uBuilder.getId());
		//对比审核后期望与真实值
		compare(expected, actual, false);	

		//审核完成,与部门库存,商品原料库存对接
		for (StockActionDetail actualStockActionDetail : actual.getStockDetails()) {
			
			//获取变化数量
			float deltaStock = actualStockActionDetail.getAmount();
			int index;
			if(actual.getSubType() == SubType.STOCK_IN){
				MaterialDept afterMaterialDept = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + actualStockActionDetail.getMaterialId() + " AND dept_id = " + actual.getDeptIn().getId(), null).get(0);
				index = beforeMaterialDepts.indexOf(afterMaterialDept);
				if(index >= 0){
					float deltaMaterialDeptStock = Math.abs(afterMaterialDept.getStock() - beforeMaterialDepts.get(index).getStock());
					Assert.assertEquals("deltaMaterialDeptStock", deltaStock, deltaMaterialDeptStock, 0.0001);
				}else{
					float deltaMaterialDeptStock = afterMaterialDept.getStock();
					Assert.assertEquals("deltaMaterialDeptStock", deltaStock, deltaMaterialDeptStock, 0.0001);
				}
				//对比原料表的变化
				Material afterMaterial = MaterialDao.getById(actualStockActionDetail.getMaterialId());
				index = beforeMaterials.indexOf(afterMaterial);
				if(index >= 0){
					float deltaMaterialStock = afterMaterial.getStock() - beforeMaterials.get(index).getStock();
					Assert.assertEquals("deltaMaterialStock", deltaStock, deltaMaterialStock, 0.0001);
				}else{
					throw new BusinessException(MaterialError.SELECT_FAIL);
				}
			}else if(actual.getSubType() == SubType.SPILL || actual.getSubType() == SubType.DAMAGE || actual.getSubType() == SubType.USE_UP){
				MaterialDept afterMaterialDept = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + actualStockActionDetail.getMaterialId() + " AND dept_id = " + actual.getDeptIn().getId(), null).get(0);
				index = beforeMaterialDepts.indexOf(afterMaterialDept);
				if(index >= 0){
					float deltaMaterialDeptStock = Math.abs(afterMaterialDept.getStock() - beforeMaterialDepts.get(index).getStock());
					Assert.assertEquals("deltaMaterialDeptStock", deltaStock, deltaMaterialDeptStock, 0.0001);
				}else{
					throw new BusinessException(StockError.MATERIAL_DEPT_ADD);
				}
				//对比材料表变化
				Material afterMaterial = MaterialDao.getById(actualStockActionDetail.getMaterialId());
				index = beforeMaterials.indexOf(afterMaterial);
				if(index >= 0){
					float deltaMaterialStock = Math.abs(afterMaterial.getStock() - beforeMaterials.get(index).getStock());
					Assert.assertEquals("deltaMaterialStock", deltaStock, deltaMaterialStock, 0.0001);
				}else{
					throw new BusinessException(MaterialError.SELECT_FAIL);
				}
				
			}else if(actual.getSubType() == SubType.STOCK_OUT){
				MaterialDept afterMaterialDept = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + actualStockActionDetail.getMaterialId() + " AND dept_id = " + actual.getDeptOut().getId(), null).get(0);
				index = beforeMaterialDepts.indexOf(afterMaterialDept);
				if(index >= 0){
					float deltaMaterialDeptStock = beforeMaterialDepts.get(index).getStock() - afterMaterialDept.getStock();
					Assert.assertEquals("deltaMaterialDeptStock", deltaStock, deltaMaterialDeptStock, 0.0001);
				}else{
					throw new BusinessException(StockError.MATERIAL_DEPT_ADD);
				}
				//对比原料表的变化
				Map<Object, Object> afterParam = new HashMap<Object, Object>();
				afterParam.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID + " AND M.material_id = " + actualStockActionDetail.getMaterialId());
				Material afterMaterial = MaterialDao.getContent(afterParam).get(0);
				index = beforeMaterials.indexOf(afterMaterial);
				if(index >= 0){
					float deltaMaterialStock = Math.abs(afterMaterial.getStock() - beforeMaterials.get(index).getStock());
					Assert.assertEquals("deltaMaterialStock", deltaStock, deltaMaterialStock, 0.0001);
				}else{
					throw new BusinessException(MaterialError.SELECT_FAIL);
				}
			}else if(actual.getSubType() == SubType.STOCK_IN_TRANSFER || actual.getSubType() == SubType.STOCK_OUT_TRANSFER){
				MaterialDept afterMaterialDeptIn = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + actualStockActionDetail.getMaterialId() + " AND dept_id = " + actual.getDeptIn().getId(), null).get(0);
				MaterialDept afterMaterialDeptOut = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + actualStockActionDetail.getMaterialId() + " AND dept_id = " + actual.getDeptOut().getId(), null).get(0);
				int indexOut = beforeMaterialDepts.indexOf(afterMaterialDeptOut);
				if(indexOut >= 0){
					float deltaMaterialDeptStock = Math.abs(afterMaterialDeptOut.getStock() - beforeMaterialDepts.get(indexOut).getStock());
					Assert.assertEquals("deltaMaterialDeptStock", deltaStock, deltaMaterialDeptStock, 0.0001);
				}else{
					throw new BusinessException(StockError.MATERIAL_DEPT_ADD); 
				}
				
				index = beforeMaterialDepts.indexOf(afterMaterialDeptIn);
				if(index >= 0){
					float deltaMaterialDeptStock = Math.abs(afterMaterialDeptIn.getStock() - beforeMaterialDepts.get(index).getStock());
					Assert.assertEquals("deltaMaterialDeptStock", deltaStock, deltaMaterialDeptStock, 0.0001);
				}else{
					float deltaMaterialDeptStock = afterMaterialDeptIn.getStock();
					Assert.assertEquals("deltaMaterialDeptStock", deltaStock, deltaMaterialDeptStock, 0.0001);
				}
			}		
		}
		
/*		StockActionDao.deleteStockActionById(mTerminal, StockActionId);
		
		try{
			StockActionDao.getStockActionById(mTerminal, StockActionId);
			Assert.assertTrue("delete stock in record(id = " + StockActionId + ") failed", false);
		}catch(BusinessException e){
			
		}*/
	}
	
	//采购
	@Test
	public void testStockIn() throws BusinessException, SQLException{
		Supplier supplier;
		List<Supplier> suppliers = SupplierDao.getSuppliers(mTerminal, null, null);
		if(suppliers.isEmpty()){
			throw new BusinessException(SupplierError.SUPPLIER_NOT_ADD);
		}else{
			supplier = suppliers.get(0);
		}
		Department deptIn;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.isEmpty()){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}else{
			deptIn = depts.get(2);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException(MaterialError.SELECT_NOT_ADD);
		}
			
		InsertBuilder builder = StockAction.InsertBuilder.newStockIn(mTerminal.restaurantID, DateUtil.parseDate("2013-09-29"))
				   .setOriStockId("asd12000")
				   .setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
				   .setComment("good")
				   .setDeptIn(deptIn.getId())
				   .setCateType(CateType.GOOD)
				   .setSupplierId(supplier.getSupplierId())
				   .addDetail(new StockActionDetail(materials.get(0).getId(), 1.5f, 30))
				   .addDetail(new StockActionDetail(materials.get(2).getId(), 1.5f, 30));
		
		testInsert(builder);
		

				   

	}
	//入库调拨
	@Test
	public void testStockInTransfer() throws BusinessException, SQLException{

		Department deptIn;
		Department deptOut;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.isEmpty()){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}else{
			deptIn = depts.get(3);
			deptOut = depts.get(2);
		}
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException(MaterialError.SELECT_NOT_ADD);
		}
			
		InsertBuilder builder = StockAction.InsertBuilder.newStockInTransfer(mTerminal.restaurantID)
				   .setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
				   .setOriStockId("bbb111").setOriStockIdDate(DateUtil.parseDate("2013-09-26 12:12:12"))
				   .setComment("good")
				   .setDeptIn(deptIn.getId())
				   .setDeptOut(deptOut.getId())
				   .setCateType(CateType.GOOD)
				   .addDetail(new StockActionDetail(materials.get(0).getId(), 1.5f, 10))
				   .addDetail(new StockActionDetail(materials.get(2).getId(), 1.5f, 10));
		testInsert(builder);

	}
	//报溢
	@Test
	public void testSpill() throws BusinessException, SQLException{
		Department deptIn;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.isEmpty()){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}else{
			deptIn = depts.get(2);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException(MaterialError.SELECT_NOT_ADD);
		}
			
		InsertBuilder builder = StockAction.InsertBuilder.newSpill(mTerminal.restaurantID)
				   .setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
				   .setOriStockId("ccb111").setOriStockIdDate(DateUtil.parseDate("2013-09-26 12:12:12"))
				   .setComment("good")
				   .setDeptIn(deptIn.getId())
				   .setCateType(CateType.GOOD)
				   .addDetail(new StockActionDetail(materials.get(0).getId(), 1.5f, 13))
				   .addDetail(new StockActionDetail(materials.get(2).getId(), 1.5f, 13));
		
		testInsert(builder);

	}
	//退货
	@Test
	public void testStockOut() throws BusinessException, SQLException{
		Supplier supplier;
		List<Supplier> suppliers = SupplierDao.getSuppliers(mTerminal, null, null);
		if(suppliers.isEmpty()){
			throw new BusinessException(SupplierError.SUPPLIER_NOT_ADD);
		}else{
			supplier = suppliers.get(0);
		}
		Department deptOut;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.isEmpty()){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}else{
			deptOut = depts.get(2);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException(MaterialError.SELECT_NOT_ADD);
		}
			
		InsertBuilder builder = StockAction.InsertBuilder.newStockOut(mTerminal.restaurantID, DateUtil.parseDate("2013-09-28 12:12:12"))
				   .setOriStockId("asd12000")
				   .setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
				   .setComment("good...")
				   .setDeptOut(deptOut.getId())
				   .setCateType(CateType.GOOD)
				   .setSupplierId(supplier.getSupplierId())
				   .addDetail(new StockActionDetail(materials.get(0).getId(), 1.5f, 12))
				   .addDetail(new StockActionDetail(materials.get(2).getId(), 1.5f, 12));
		
		testInsert(builder);

	}
	//出库调拨
	@Test
	public void testStockOutTransfer() throws BusinessException, SQLException{

		Department deptIn;
		Department deptOut;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.isEmpty()){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}else{
			deptIn = depts.get(2);
			deptOut = depts.get(3);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException(MaterialError.SELECT_NOT_ADD);
		}
			
		InsertBuilder builder = StockAction.InsertBuilder.newStockOutTransfer(mTerminal.restaurantID)
				   .setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
				   .setOriStockId("bddb111").setOriStockIdDate(DateUtil.parseDate("2013-09-26 12:12:12"))
				   .setComment("good")
				   .setDeptIn(deptIn.getId())
				   .setDeptOut(deptOut.getId())
				   .setCateType(CateType.GOOD)
				   .addDetail(new StockActionDetail(materials.get(0).getId(), 1.5f, 5))
				   .addDetail(new StockActionDetail(materials.get(2).getId(), 1.5f, 5));
		
		testInsert(builder);

	}
	//报损
	@Test
	public void testDamage() throws BusinessException, SQLException{
		Department deptIn;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.isEmpty()){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}else{
			deptIn = depts.get(2);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException(MaterialError.SELECT_NOT_ADD);
		}
			
		InsertBuilder builder = StockAction.InsertBuilder.newDamage(mTerminal.restaurantID)
				   .setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
				   .setOriStockId("bbb111").setOriStockIdDate(DateUtil.parseDate("2013-09-26 12:12:12"))
				   .setComment("good")
				   .setDeptIn(deptIn.getId())
				   .setCateType(CateType.GOOD)
				   .addDetail(new StockActionDetail(materials.get(0).getId(), 1.5f, 10))
				   .addDetail(new StockActionDetail(materials.get(2).getId(), 1.5f, 8));
		
		testInsert(builder);

	}

	//消耗
	@Test
	public void testUseUp() throws BusinessException, SQLException{
		StockActionDao.getStockAndDetail(mTerminal, null, null);
		Department deptIn;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.isEmpty()){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}else{
			deptIn = depts.get(2);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException(MaterialError.SELECT_NOT_ADD);
		}
			
		InsertBuilder builder = StockAction.InsertBuilder.newDamage(mTerminal.restaurantID)
				   .setOperatorId((int) mTerminal.pin).setOperator(mTerminal.owner)
				   .setOriStockId("bbb111").setOriStockIdDate(DateUtil.parseDate("2013-09-26 12:12:12"))
				   .setComment("use_up")
				   .setDeptIn(deptIn.getId())
				   .setCateType(CateType.MATERIAL)
				   .addDetail(new StockActionDetail(materials.get(0).getId(), 1.5f, 10))
				   .addDetail(new StockActionDetail(materials.get(2).getId(), 1.5f, 8));
		
		testInsert(builder);
		
		
		

	}
	

	
	
}
