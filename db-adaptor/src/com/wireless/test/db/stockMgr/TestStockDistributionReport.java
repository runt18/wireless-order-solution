package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.test.db.TestInit;

public class TestStockDistributionReport {
	private static Staff mStaff;
	
	@BeforeClass
	public static void initDBParam() throws BusinessException, SQLException, PropertyVetoException{
		TestInit.init();
		try{
			mStaff = StaffDao.getAdminByRestaurant(37);
			//mTerminal.restaurantID = 37;
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	@Test
	public void testStockDistriBution() throws SQLException, BusinessException{
		List<MaterialDept> materialDepts = MaterialDeptDao.getMaterialDepts(mStaff, null, null);
		System.out.println("size" + materialDepts.size());
	}
	
	
	
}
