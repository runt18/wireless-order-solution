package com.wireless.test.db.inventoryMgr;

import java.beans.PropertyVetoException;

import org.junit.BeforeClass;

import com.wireless.test.db.TestInit;

public class TestMaterialCateDao {
	
	@BeforeClass
	public static void beforeClass() throws PropertyVetoException{
		TestInit.init();
	}
	
//	@Test
//	public void insert() throws SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			MaterialCate mc = new MaterialCate(26, "26 materialCate insert test");
//			MaterialCateDao.insert(mc);
//		}catch(Exception e){
//			e.printStackTrace();
//			org.junit.Assert.fail();
//		}finally{
//			dbCon.disconnect();
//		}
//	}
}
