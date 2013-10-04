package com.wireless.test.db.client.member;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.client.member.MemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.test.db.TestInit;

public class TestMemberRecommendFoods {
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException, BusinessException{
		TestInit.init();
	}
	
	@Test
	public void testMemberRecommendFoods() throws SQLException{
		MemberDao.calcRecommendFoods();
	}
}
