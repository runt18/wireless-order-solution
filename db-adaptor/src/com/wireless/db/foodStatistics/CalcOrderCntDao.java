package com.wireless.db.foodStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
		
		List<Map.Entry<Integer, Integer>> foodOrderCnts = new ArrayList<Map.Entry<Integer, Integer>>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			
			final int foodId = dbCon.rs.getInt("food_id");
			final int orderCnt = dbCon.rs.getInt("order_cnt");
			
			foodOrderCnts.add(new Entry<Integer, Integer>(){

				@Override
				public Integer getKey() {
					return foodId;
				}

				@Override
				public Integer getValue() {
					return orderCnt;
				}

				@Override
				public Integer setValue(Integer value) {
					return null;
				}
				
			});
		}
		dbCon.rs.close();
		
		for(Map.Entry<Integer, Integer> entry : foodOrderCnts){
			sql = " UPDATE " + Params.dbName + ".food_statistics " +
				  " SET order_cnt = " + entry.getValue() + 
				  " WHERE " +
				  " food_id = " + entry.getKey();
			dbCon.stmt.executeUpdate(sql);
		}

	}
}
