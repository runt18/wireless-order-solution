package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.protocol.Terminal;

public class PricePlanDao {
	
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
	public static List<PricePlan> getPricePlans(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPricePlans(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the price plan to a specified restaurant defined in {@link Terminal} according the specific condition and order clause
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
	public static List<PricePlan> getPricePlans(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " price_plan_id, restaurant_id, name, status " +
			  " FROM " + 
			  Params.dbName + ".price_plan PP " +
			  " WHERE 1 = 1 " +
			  " AND PP.restaurant_id = " + term.restaurantID + " " +
			  (extraCond != null ? extraCond : "") + " " +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<PricePlan> pricePlans = new ArrayList<PricePlan>();
		while(dbCon.rs.next()){
			pricePlans.add(new PricePlan(dbCon.rs.getInt("price_plan_id"),
										 dbCon.rs.getString("name"),
										 PricePlan.Status.valueOf(dbCon.rs.getInt("status")),
										 dbCon.rs.getInt("restaurant_id")));	
		}
		dbCon.rs.close();
		
		return pricePlans;
	}
	
	/**
	 * Get the price plan to specified id
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param pricePlanId
	 * 			the price plan id to query
	 * @return the price plan to specified id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the price plan to this specified id is NOT found
	 */
	public static PricePlan getPricePlanById(DBCon dbCon, Terminal term, int pricePlanId) throws SQLException, BusinessException{
		List<PricePlan> result = getPricePlans(dbCon, term, "AND PP.price_plan_id = " + pricePlanId, null);
		if(result.isEmpty()){
			throw new BusinessException("The price plan(id = " + pricePlanId + ") is NOT found.");
		}else{
			return result.get(0);
		}
	}

	/**
	 * Get the active price plan to a specified restaurant defined in {@link Terminal}.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @return the active price plan to specified restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the active price plan to this specified restaurant is NOT found
	 */
	public static PricePlan getActivePricePlan(DBCon dbCon, Terminal term) throws SQLException, BusinessException{
		List<PricePlan> result = getPricePlans(dbCon, term, "AND PP.status = " + PricePlan.Status.ACTIVITY.getVal(), null);
		if(result.isEmpty()){
			throw new BusinessException("The active price plan to restuarnt(id = " + term.restaurantID + ") is NOT found.");
		}else{
			return result.get(0);
		}
	}
	
}
