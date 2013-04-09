package com.wireless.test.db.tasteRef;

import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestTasteRefDao {
	@Test
	public void testUpdate(){
		try{
			TestInit.init();
			DBCon dbCon = new DBCon();
			dbCon.connect();
			Terminal terminal = VerifyPin.exec(217, Terminal.MODEL_STAFF);
			dbCon.disconnect();
			String modTastes = "";
			System.out.println(TasteRefDao.update(terminal,modTastes));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
