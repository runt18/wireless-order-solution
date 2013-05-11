package com.wireless.test.db.inventoryMgr;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.inventoryMgr.Material;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestMaterialDao {
	
	private static Terminal term;
	
	@BeforeClass
	public static void beforeClass(){
		TestInit.init();
		try {
			term = VerifyPin.exec(9720860, Terminal.MODEL_STAFF);
		} catch (BusinessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void insert(){
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Material m = new Material(26, "26 insert material", 1, term.owner, Material.Status.NORMAL.getValue());
			MaterialDao.insert(dbCon, m);
		}catch(Exception e){
			e.printStackTrace();
			org.junit.Assert.fail();
		}finally{
			dbCon.disconnect();
		}
	}
}
