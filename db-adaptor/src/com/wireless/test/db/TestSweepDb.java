package com.wireless.test.db;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.misc.DbArchiveDao;
import com.wireless.db.misc.SweepDB;
import com.wireless.exception.BusinessException;

public class TestSweepDb {
	
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testSweepDb() throws SQLException{
		System.out.println(SweepDB.exec());
	}
	
	@Test 
	public void testDbArchived() throws SQLException, BusinessException{
		System.out.println(DbArchiveDao.archive());
	}
}
