package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.protocol.PricePlan;

public class QueryPricePlanDao {
	
	/**
	 * Get the price plan according the specific condition and order clause
	 * @param extraCond
	 * 			the extra condition to this query
	 * @param orderClause
	 * 			the order clause to this query
	 * @return	the array holding the price plan
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements.
	 */
	public static PricePlan[] exec(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the price plan according the specific condition and order clause
	 * @param dbCon
	 * 			the database connection to this query
	 * @param extraCond
	 * 			the extra condition to this query
	 * @param orderClause
	 * 			the order clause to this query
	 * @return	the array holding the price plan
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements.
	 */
	public static PricePlan[] exec(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " price_plan_id, restaurant_id, name, status " +
			  " FROM " + 
			  Params.dbName + ".price_plan" +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond : "") + " " +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<PricePlan> pricePlans = new ArrayList<PricePlan>();
		while(dbCon.rs.next()){
			pricePlans.add(new PricePlan(dbCon.rs.getInt("price_plan_id"),
										 dbCon.rs.getString("name"),
										 dbCon.rs.getInt("status"),
										 dbCon.rs.getInt("restaurant_id")));	
		}
		dbCon.rs.close();
		
		return pricePlans.toArray(new PricePlan[pricePlans.size()]);
	}
}
