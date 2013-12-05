package com.wireless.db.foodStatistics;

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
	public static void exec() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			exec(dbCon);
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
	public static void exec(DBCon dbCon) throws SQLException{
		String sql;
		
		sql = " SELECT " +
			  " food_id, SUM(order_count) AS order_cnt " +
			  " FROM " +
			  Params.dbName + ".order_food_history " +
			  " WHERE food_id IS NOT NULL " +
			  " GROUP BY food_id " +
			  " HAVING order_cnt > 0 ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		dbCon.stmt.clearBatch();
		while(dbCon.rs.next()){
			
			final int foodId = dbCon.rs.getInt("food_id");
			final int orderCnt = dbCon.rs.getInt("order_cnt");
			
			sql = " UPDATE " + Params.dbName + ".food_statistics " +
				  " SET order_cnt = " + orderCnt + 
				  " WHERE " +
				  " food_id = " + foodId;
			dbCon.stmt.addBatch(sql);
			
		}
		dbCon.rs.close();
		
		dbCon.stmt.executeBatch();
		
//		sql = " UPDATE " + 
//		      Params.dbName + ".food_statistics FS, " +
//			  "( SELECT food_id, SUM(order_count) AS order_cnt FROM " +
//		      Params.dbName + ".order_food_history WHERE food_id IS NOT NULL " +
//			  " GROUP BY food_id HAVING order_cnt > 0 ) A " +
//		      " SET FS.order_cnt = A.order_cnt " +
//			  " WHERE FS.food_id = A.food_id ";
//		
//		dbCon.stmt.executeUpdate(sql);
		
	}
}
