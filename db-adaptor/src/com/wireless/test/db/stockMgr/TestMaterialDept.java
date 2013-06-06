package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

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

public class TestMaterialDept {

	private static Terminal mTerminal;
	
	@BeforeClass
	public static void initDBParam() throws BusinessException, SQLException, PropertyVetoException{
			
	
		try{
			TestInit.init();
			mTerminal = VerifyPin.exec(217, Terminal.MODEL_STAFF);
		}catch(SQLException e){
			e.printStackTrace();
		}catch(BusinessException e){
			throw new BusinessException("数据库连接失败!!");
		}
	}
	
	public void compare(MaterialDept expected, MaterialDept actual){
		Assert.assertEquals("materialId", expected.getMaterialId(), actual.getMaterialId());
		Assert.assertEquals("deptId", expected.getDeptId(), actual.getDeptId());
		Assert.assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		Assert.assertEquals("stock", expected.getStock(), actual.getStock());
	}
	
	@Test
	public void testInsertMaterialDept() throws SQLException, BusinessException{
		Department dept;
		List<Department> depts = DepartmentDao.getDepartments(mTerminal, null, null);
		if(depts.isEmpty()){
			throw new BusinessException("还没添加任何部门!");
		}else{
			dept = depts.get(1);
		}
		
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mTerminal.restaurantID);
		List<Material> materials = MaterialDao.getContent(params);
		if(materials.isEmpty()){
			throw new BusinessException("没有添加任何材料!");
		}
		MaterialDept mDept = new MaterialDept();
		//mDept.setMaterialId(3);
		mDept.setMaterialId(materials.get(0).getId());
		mDept.setDeptId(dept.getId());
		mDept.setRestaurantId(mTerminal.restaurantID);
		mDept.setStock(6);
		
		MaterialDeptDao.insertMaterialDept(mTerminal, mDept); 	
		
		mDept.setMaterialId(materials.get(1).getId());
		mDept.setDeptId(dept.getId());
		mDept.setRestaurantId(mTerminal.restaurantID);
		mDept.setStock(9);
		
		MaterialDeptDao.insertMaterialDept(mTerminal, mDept); 	
		List<MaterialDept> list1 = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + materials.get(1).getId() + " AND dept_id = " + dept.getId(), null);
		
		compare(mDept, list1.get(0));	
		
	}
	
	@Test
	public void testAndit() throws SQLException, BusinessException{
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
		//审核
		UpdateBuilder uBuilder = new StockAction.UpdateBuilder(stockInId)
									.setApprover("兰戈2")
									.setApproverId(12)
									.setApproverDate(DateUtil.parseDate("2013-06-03"))
									.setStatus(Status.AUDIT);
		
		StockActionDao.updateStockIn(mTerminal, uBuilder);
		
		StockAction stockAction = StockActionDao.getStockAndDetailById(mTerminal, stockInId);
		//审核完成,与部门库存,商品原料库存对接
		if(stockAction.getStatus() == Status.AUDIT){
			int deptId = stockAction.getDeptIn().getId();
			for (StockActionDetail sActionDetail : stockAction.getStockDetails()) {
				
				MaterialDept mDept = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + sActionDetail.getMaterialId() + " AND dept_id = " + deptId, null).get(0);
				
				Material material = MaterialDao.getById(mDept.getMaterialId());
				
				if(stockAction.getType() == Type.STOCK_IN){
					mDept.plusStock(sActionDetail.getAmount());
					material.plusStock(sActionDetail.getAmount());
				}else{
					mDept.cutStock(sActionDetail.getAmount());
					material.cutStock(sActionDetail.getAmount());
				}
				
				MaterialDeptDao.updateMaterialDept(mTerminal, mDept);
				
				material.setLastModStaff(mTerminal.owner);
				MaterialDao.update(material);	
	
				
				MaterialDept actual = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + mDept.getMaterialId() + " AND dept_id = " + mDept.getDeptId(), null).get(0);
				compare(mDept, actual);
			}	
			
		}
		
		
		
	
	}
	
}
