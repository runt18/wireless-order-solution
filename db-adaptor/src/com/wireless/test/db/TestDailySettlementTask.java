package com.wireless.test.db;

import java.beans.PropertyVetoException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.foodStatistics.CalcFoodStatisticsDao;

public class TestDailySettlementTask {
	@BeforeClass
	public static void initDbParam() throws PropertyVetoException{
		TestInit.init();
	}
	
	@Test
	public void testDailySettlementTask() throws SQLException{
		final String sep = System.getProperty("line.separator");
		StringBuilder taskInfo = new StringBuilder();
		long beginTime = 0;
		long elapsedTime = 0;
		
//		//Perform to smart taste calculation.
//		beginTime = System.currentTimeMillis();
//		TasteRefDao.exec();
//		elapsedTime = System.currentTimeMillis() - beginTime;
//		
//		taskInfo.append("info : The calculation to smart taste reference takes " + elapsedTime / 1000 + " sec.").append(sep);
//		
//		//Perform to food association.
//		beginTime = System.currentTimeMillis();
//		CalcFoodAssociationDao.exec();
//		elapsedTime = System.currentTimeMillis() - beginTime;
//		
//		taskInfo.append("info : The calculation to food association takes " + elapsedTime / 1000 + " sec.").append(sep);
		
		//Perform to calculate food statistics.
		beginTime = System.currentTimeMillis();
		CalcFoodStatisticsDao.exec();
		elapsedTime = System.currentTimeMillis() - beginTime;
		
		taskInfo.append("info : The calculation to food's statistics takes " + elapsedTime / 1000 + " sec.").append(sep);
		
		//Perform to calculate member favor foods.
		beginTime = System.currentTimeMillis();
		MemberDao.calcFavorFoods();
		elapsedTime = System.currentTimeMillis() - beginTime;
		
		taskInfo.append("info : The calculation to member favor foods takes " + elapsedTime / 1000 + " sec.").append(sep);
		
		//Perform to calculate member recommended foods.
		beginTime = System.currentTimeMillis();
		MemberDao.calcRecommendFoods();
		elapsedTime = System.currentTimeMillis() - beginTime;
		
		taskInfo.append("info : The calculation to member recommended foods takes " + elapsedTime / 1000 + " sec.").append(sep);

		System.out.println(taskInfo);
	}
}
