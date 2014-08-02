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
		
		dbCon.stmt.execute("SET SQL_SAFE_UPDATES = 0");
		
		String sql;
		
		sql = " UPDATE " + Params.dbName + ".food F " +
			  " SET F.order_amount = 0 ";
		dbCon.stmt.executeUpdate(sql);
		
		sql = " SELECT " +
			  " OFH.food_id, SUM(order_count) AS order_cnt " +
			  " FROM " + Params.dbName + ".order_food_history OFH " +
			  " JOIN " + Params.dbName + ".food F ON OFH.food_id = F.food_id " +
			  " WHERE OFH.food_id IS NOT NULL " +
			  //" AND OFH.order_date BETWEEN DATE_SUB(CURDATE(), INTERVAL 90 DAY) AND CURDATE() " +
			  " GROUP BY OFH.food_id " +
			  " HAVING order_cnt > 0 ";
		
		sql = " UPDATE " + Params.dbName + ".food F " +
			  " JOIN ( " + sql + " ) AS TMP ON F.food_id = TMP.food_id " +
			  " SET F.order_amount = TMP.order_cnt ";
		
		dbCon.stmt.executeUpdate(sql);
		
	}
}
