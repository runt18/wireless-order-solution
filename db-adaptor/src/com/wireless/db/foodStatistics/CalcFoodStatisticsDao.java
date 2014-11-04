package com.wireless.db.foodStatistics;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;

public class CalcFoodStatisticsDao {
	
	public final static class Result{
		private final int amount;
		private final int elapsed;
		Result(int amount, int elapsed){
			this.amount = amount;
			this.elapsed = elapsed;
		}
		public int getAmount(){
			return this.amount;
		}
		public int getElapsed(){
			return this.elapsed;
		}
		@Override
		public String toString(){
			return "The calculation to " + amount + " food's statistics takes " + elapsed + " sec.";
		}
	}
	
	/**
	 * Calculate the food statistics.
	 * @return the result to food statistics calculation
	 * @throws SQLException
	 * 			throws if failed to execute the SQL statement
	 */
	public static Result exec() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the food statistics.
	 * Note that the database should be connected before invoking this method. 
	 * @param dbCon
	 * 			the database base connection
	 * @return the result to food statistics calculation
	 * @throws SQLException
	 * 			throws if failed to execute the SQL statement
	 */
	public static Result exec(DBCon dbCon) throws SQLException{
		
		int nRows = 0;
		
		try{
			//dbCon.conn.setAutoCommit(false);
			
			long beginTime = System.currentTimeMillis();
			
			String sql;
			
			sql = " SELECT COUNT(*) FROM " + Params.dbName + ".food";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				nRows = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
			
			//Calculate the order count to each food
			CalcOrderCntDao.exec(dbCon);
			
			//Calculate the weight to each food
			CalcFoodWeightDao.exec(dbCon);
			
			//dbCon.conn.commit();
			
			return new Result(nRows, (int)(System.currentTimeMillis() - beginTime) / 1000);
			
		}catch(SQLException e){
			//dbCon.conn.rollback();
			throw e;
			
		}finally{
			//dbCon.conn.setAutoCommit(true);
		}
	}
}
