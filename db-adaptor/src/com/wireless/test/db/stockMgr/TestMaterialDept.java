package com.wireless.test.db.stockMgr;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DeptError;
import com.wireless.exception.MaterialError;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.test.db.TestInit;

public class TestMaterialDept {

	private static Staff mStaff;
	
	@BeforeClass
	public static void initDBParam() throws BusinessException, SQLException, PropertyVetoException{
			
		TestInit.init();
		try{
			mStaff = StaffDao.getByRestaurant(37).get(0);
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void compare(MaterialDept expected, MaterialDept actual){
		assertEquals("materialId", expected.getMaterialId(), actual.getMaterialId());
		assertEquals("deptId", expected.getDeptId(), actual.getDeptId());
		assertEquals("restaurantId", expected.getRestaurantId(), actual.getRestaurantId());
		assertEquals("stock", expected.getStock(), actual.getStock(), 0.01);
	}
	
	@Test
	public void testInsertMaterialDept() throws SQLException, BusinessException{
		Department dept;
		List<Department> depts = DepartmentDao.getDepartments4Inventory(mStaff);
		if(depts.isEmpty()){
			throw new BusinessException(DeptError.DEPT_NOT_EXIST);
		}else{
			dept = depts.get(1);
		}
		
//		Map<Object, Object> params = new HashMap<Object, Object>();
//		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + mStaff.getRestaurantId());
		List<Material> materials = MaterialDao.getByCond(mStaff, null);
		if(materials.isEmpty()){
			throw new BusinessException(MaterialError.SELECT_NOT_ADD);
		}
		MaterialDept materialDept = new MaterialDept();
		//mDept.setMaterialId(3);
		materialDept.setMaterialId(materials.get(0).getId());
		materialDept.setDeptId(dept.getId());
		materialDept.setRestaurantId(mStaff.getRestaurantId());
		materialDept.setStock(6);
		
		MaterialDeptDao.insertMaterialDept(mStaff, materialDept); 	
		
		materialDept.setMaterialId(materials.get(1).getId());
		materialDept.setDeptId(dept.getId());
		materialDept.setRestaurantId(mStaff.getRestaurantId());
		materialDept.setStock(9);
		
		MaterialDeptDao.insertMaterialDept(mStaff, materialDept); 	
		List<MaterialDept> MaterialDepts = MaterialDeptDao.getMaterialDepts(mStaff, " AND material_id = " + materials.get(1).getId() + " AND dept_id = " + dept.getId(), null);
		
		compare(materialDept, MaterialDepts.get(0));	
		
		//修改库存量
		materialDept.addStock(89);
		MaterialDeptDao.updateMaterialDept(mStaff, materialDept);
		
		MaterialDept actual = MaterialDeptDao.getMaterialDepts(mStaff, " AND material_id = " +materialDept.getMaterialId() + " AND dept_id = " + materialDept.getDeptId(), null).get(0);
		compare(materialDept, actual);
		
	
	}
	
}
