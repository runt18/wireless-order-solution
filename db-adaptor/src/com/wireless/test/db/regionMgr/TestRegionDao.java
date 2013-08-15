package com.wireless.test.db.regionMgr;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.test.db.TestInit;

public class TestRegionDao {

	@SuppressWarnings("unused")
	private static Staff mStaff;

	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException {
		TestInit.init();
		try {
			mStaff = StaffDao.getStaffs(37).get(0);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testExec() throws BusinessException, SQLException {
//		DBCon dbCon = new DBCon();
//		try {
//			dbCon.connect();
//			List<Region> list = RegionDao.get(dbCon, mTerminal, null, null);
//
//			for (int i = 0; i < list.size(); i++) {
//				System.out.println("区域ID：" + list.get(i).getRegionID()
//						+ "\t 区域名称：" + list.get(i).getRegionName() + "\t 餐台ID："
//						+ list.get(i).getRestaurantID());
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			dbCon.disconnect();
//		}
	}

}
