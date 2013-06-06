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
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.stockMgr.MaterialDept;
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
		MaterialDept materialDept = new MaterialDept();
		//mDept.setMaterialId(3);
		materialDept.setMaterialId(materials.get(0).getId());
		materialDept.setDeptId(dept.getId());
		materialDept.setRestaurantId(mTerminal.restaurantID);
		materialDept.setStock(6);
		
		MaterialDeptDao.insertMaterialDept(mTerminal, materialDept); 	
		
		materialDept.setMaterialId(materials.get(1).getId());
		materialDept.setDeptId(dept.getId());
		materialDept.setRestaurantId(mTerminal.restaurantID);
		materialDept.setStock(9);
		
		MaterialDeptDao.insertMaterialDept(mTerminal, materialDept); 	
		List<MaterialDept> MaterialDepts = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " + materials.get(1).getId() + " AND dept_id = " + dept.getId(), null);
		
		compare(materialDept, MaterialDepts.get(0));	
		
		//修改库存量
		materialDept.plusStock(89);
		MaterialDeptDao.updateMaterialDept(mTerminal, materialDept);
		
		MaterialDept actual = MaterialDeptDao.getMaterialDepts(mTerminal, " AND material_id = " +materialDept.getMaterialId() + " AND dept_id = " + materialDept.getDeptId(), null).get(0);
		compare(materialDept, actual);
		
	
	}
	
}
