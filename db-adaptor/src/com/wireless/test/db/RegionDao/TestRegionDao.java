package com.wireless.test.db.RegionDao;

import java.sql.SQLException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.DBCon;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.system.Region;
import com.wireless.protocol.Terminal;
import com.wireless.test.db.TestInit;

public class TestRegionDao {

	private static Terminal mTerminal;

	@BeforeClass
	public static void initDbParam() {
		TestInit.init();
		try {
			mTerminal = VerifyPin.exec(229, Terminal.MODEL_STAFF);
		} catch (BusinessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testExec() throws BusinessException, SQLException {
		DBCon dbCon = new DBCon();
		try {
			dbCon.connect();
			List<Region> list = RegionDao.exec(dbCon, mTerminal, null, null);

			for (int i = 0; i < list.size(); i++) {
				System.out.println("区域ID：" + list.get(i).getRegionID()
						+ "\t 区域名称：" + list.get(i).getRegionName() + "\t 餐台ID："
						+ list.get(i).getRestaurantID());
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			dbCon.disconnect();
		}
	}

}
