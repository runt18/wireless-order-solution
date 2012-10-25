package com.wireless.foodStatistics;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;

public class CalcOrderCntDao {

	/**
	 * Calculate order count to each food from the bill history.
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
	 * Calculate order count to each food from the bill history.
	 * Note that the database should be connected before invoking this method. 
	 * @param dbCon
	 * 			The database base connection
	 * @return The amount of statistics record would be written to database. 
	 * @throws SQLException
	 * 			Throws if failed to execute the SQL statement.
	 */
	public static int exec(DBCon dbCon) throws SQLException{
		String sql;
		
		try{
			dbCon.conn.setAutoCommit(false);
			
			sql = " DELETE FROM " + Params.dbName + ".food_statistics";
			
			dbCon.stmt.executeUpdate(sql);
			
			sql = " INSERT INTO " + Params.dbName + ".food_statistics" +
				  " (`food_id`, `order_cnt`) " +
				  " SELECT " +
				  " food_id, SUM(order_count) AS order_cnt " +
				  " FROM " +
				  Params.dbName + ".order_food_history " +
				  " WHERE food_id IS NOT NULL " +
				  " GROUP BY food_id " +
				  " HAVING order_cnt > 0 ";
			
			int nRows = dbCon.stmt.executeUpdate(sql);
			
			dbCon.conn.commit();
			
			return nRows;
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.conn.setAutoCommit(true);
		}
	}
}
