package com.wireless.db.foodStatistics;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;

public class CalcFoodStatisticsDao {
	
	/**
	 * Calculate the food statistics.
	 * @return The amount of statistics record would be written to database. 
	 * @throws SQLException
	 * 			Throws if failed to execute the SQL statement.
	 */
	public static int exec() throws SQLException{
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
	 * 			The database base connection
	 * @return The amount of statistics record would be written to database. 
	 * @throws SQLException
	 * 			Throws if failed to execute the SQL statement.
	 */
	public static int exec(DBCon dbCon) throws SQLException{
		
		int nRows = 0;
		
		try{
			//dbCon.conn.setAutoCommit(false);
			
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
			
			return nRows;
			
		}catch(SQLException e){
			//dbCon.conn.rollback();
			throw e;
			
		}finally{
			//dbCon.conn.setAutoCommit(true);
		}
	}
}
