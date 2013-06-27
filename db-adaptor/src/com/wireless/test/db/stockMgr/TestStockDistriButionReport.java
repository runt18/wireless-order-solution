package com.wireless.test.db.stockMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.stockMgr.MaterialDeptDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.stockMgr.MaterialDept;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestStockDistriButionReport {
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
	//期望与真实
	@Test
	public void testStockDistriBution() throws SQLException, BusinessException{
		List<MaterialDept> materialDepts = MaterialDeptDao.getMaterialDepts(mTerminal, " AND dept_id = " + 2, null);
		System.out.println("size" + materialDepts.size());
	}
	
	
	
}
