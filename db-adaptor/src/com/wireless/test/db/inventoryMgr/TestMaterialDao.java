package com.wireless.test.db.inventoryMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestMaterialDao {
	
	private static Staff staff;
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException, BusinessException{
		TestInit.init();
		try {
			staff = StaffDao.getByRestaurant(26).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void insert() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Material m = new Material(26, "26 insert material", 1, staff.getName(), Material.Status.NORMAL.getValue());
			MaterialDao.insert(dbCon, m);
		}catch(Exception e){
			e.printStackTrace();
			org.junit.Assert.fail();
		}finally{
			dbCon.disconnect();
		}
	}
}
