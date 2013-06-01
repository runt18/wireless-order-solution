package com.wireless.test.db.inventoryMgr;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.inventoryMgr.MaterialCateDao;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.test.db.TestInit;

public class TestMaterialCateDao {
	
	@BeforeClass
	public static void beforeClass(){
		TestInit.init();
	}
	
	@Test
	public void insert() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			MaterialCate mc = new MaterialCate(26, "26 materialCate insert test");
			MaterialCateDao.insert(mc);
		}catch(Exception e){
			e.printStackTrace();
			org.junit.Assert.fail();
		}finally{
			dbCon.disconnect();
		}
	}
}
