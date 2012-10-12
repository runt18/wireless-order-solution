package com.wireless.db.distMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.distMgr.DiscountPojo;
import com.wireless.protocol.Discount;
import com.wireless.protocol.DiscountPlan;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Terminal;

public class QueryDiscountDao {
	
	/**
	 * Get the discount and corresponding plan detail, along with the kitchen details.
	 * @param term
	 * 			The terminal to query.
	 * @param extraCond
	 * 			The extra condition.
	 * @param orderClause
	 * 			The order clause.
	 * @return The array holding the discount info matching the condition. 
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static DiscountPojo[] exec(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the discount and corresponding plan detail, along with the kitchen details.
	 * Note that the database should be connected before connected.
	 * @param dbCon
	 * 			The database connection.
	 * @param term
	 * 			The terminal to query.
	 * @param extraCond
	 * 			The extra condition.
	 * @param orderClause
	 * 			The order clause.
	 * @return The array holding the discount info matching the condition. 
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static DiscountPojo[] exec(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " DIST.discount_id, DIST.restaurant_id, DIST.name AS dist_name, DIST.level, " +
			  " DIST_PLAN.dist_plan_id, DIST_PLAN.kitchen_id, DIST_PLAN.kitchen_alias, DIST_PLAN.rate, " +
			  " KITCHEN.name AS kitchen_name" +
			  " FROM " + 
			  Params.dbName + ".discount_plan DIST_PLAN " +
			  " JOIN " +
			  Params.dbName + ".discount DIST " +
			  " ON DIST_PLAN.discount_id = DIST.discount_id " +
			  " JOIN " +
			  Params.dbName + ".kitchen KITCHEN " +
			  " ON DIST_PLAN.kitchen_id = KITCHEN.kitchen_id " +
			  " WHERE 1=1 " +
			  " AND DIST.restaurant_id = " + term.restaurantID +
			  (extraCond == null ? "" : extraCond) + " " +
			  (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		LinkedHashMap<Discount, DiscountPojo> discounts = new LinkedHashMap<Discount, DiscountPojo>();
		
		while(dbCon.rs.next()){
			Discount tmp = new Discount(dbCon.rs.getInt("discount_id"));
			DiscountPojo distPojo = discounts.get(tmp);
			if(distPojo != null){
				Kitchen kitchen = new Kitchen();
				kitchen.restaurantID = dbCon.rs.getInt("restaurant_id");
				kitchen.kitchenID = dbCon.rs.getInt("kitchen_id");
				kitchen.aliasID = dbCon.rs.getShort("kitchen_alias");
				kitchen.name = dbCon.rs.getString("kitchen_name");
				distPojo.addPlan(new DiscountPlan(kitchen, dbCon.rs.getFloat("rate")));
				discounts.put(tmp, distPojo);
			}else{
				distPojo = new DiscountPojo();
				distPojo.setId(dbCon.rs.getInt("discount_id"));
				distPojo.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
				distPojo.setName(dbCon.rs.getString("dist_name"));
				distPojo.setLevel(dbCon.rs.getShort("level"));
				Kitchen kitchen = new Kitchen();
				kitchen.restaurantID = dbCon.rs.getInt("restaurant_id");
				kitchen.kitchenID = dbCon.rs.getInt("kitchen_id");
				kitchen.aliasID = dbCon.rs.getShort("kitchen_alias");
				kitchen.name = dbCon.rs.getString("kitchen_name");
				distPojo.addPlan(new DiscountPlan(kitchen, dbCon.rs.getFloat("rate")));
				discounts.put(distPojo.toProtocol(), distPojo);
			}
		}
		
		return discounts.values().toArray(new DiscountPojo[discounts.size()]);		

	}
	
	/**
	 * Get the pure discount info(such as id, name and so on).
	 * @param term
	 * 			The terminal to query.
	 * @param extraCond
	 * 			The extra condition.
	 * @param orderClause
	 * 			The order clause.
	 * @return The array holding the pure discount info.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static DiscountPojo[] execPureDiscount(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execPureDiscount(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the pure discount info(such as id, name and so on).
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			The database connection.
	 * @param term
	 * 			The terminal to query.
	 * @param extraCond
	 * 			The extra condition.
	 * @param orderClause
	 * 			The order clause.
	 * @return The array holding the pure discount info.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static DiscountPojo[] execPureDiscount(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " +
			  " DIST.discount_id, DIST.restaurant_id, DIST.name AS dist_name, DIST.level " +
			  " FROM " + 
			  Params.dbName + ".discount DIST " +
			  " WHERE 1=1 " +
			  " AND DIST.restaurant_id = " + term.restaurantID +
			  (extraCond == null ? "" : extraCond) + " " +
			  (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<DiscountPojo> pureDiscounts = new ArrayList<DiscountPojo>();
		while(dbCon.rs.next()){
			DiscountPojo distPojo = new DiscountPojo();
			distPojo.setId(dbCon.rs.getInt("discount_id"));
			distPojo.setName(dbCon.rs.getString("dist_name"));
			distPojo.setLevel(dbCon.rs.getInt("level"));
			distPojo.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
		}
		
		return pureDiscounts.toArray(new DiscountPojo[pureDiscounts.size()]);
		
	}
	
	
}
