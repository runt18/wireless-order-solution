package com.wireless.db.restaurantMgr;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.RestaurantError;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.protocol.Terminal;

public class RestaurantDao {
	
	/**
	 * Query a restaurant according to specified id defined in terminal {@link Terminal}
	 * @param term
	 * 			the terminal
	 * @return the query restaurant result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 				if the restaurant to query does NOT exist
	 */
	public static Restaurant queryByID(Terminal term) throws SQLException, BusinessException{

		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryById(dbCon, term.restaurantID);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query a restaurant according to specified id.
	 * @param restaurantId
	 * 			the id to restaurant to query
	 * @return the query restaurant result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 * @throws BusinessException
	 * 				if the restaurant to query does NOT exist
	 */
	public static Restaurant queryById(int restaurantId) throws SQLException, BusinessException{

		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryById(dbCon, restaurantId);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query a restaurant according to specified id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the query restaurant result 
	 * @throws SQLException
	 * 				if failed to execute any SQL statements
	 * @throws BusinessException
	 * 				if the restaurant to query does NOT exist
	 */
	public static Restaurant queryById(DBCon dbCon, int restaurantId) throws SQLException, BusinessException{
		String sql = " SELECT * FROM " + Params.dbName + ".restaurant " +
					 " WHERE 1 = 1 " +
					 " AND id = " + restaurantId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		Restaurant restaurant = null;
		if(dbCon.rs.next()){
			restaurant = new Restaurant();
			restaurant.setAccount(dbCon.rs.getString("account"));
			restaurant.setId(dbCon.rs.getInt("id"));
			restaurant.setPwd(dbCon.rs.getString("pwd"));
			restaurant.setPwd2(dbCon.rs.getString("pwd2"));
			restaurant.setPwd3(dbCon.rs.getString("pwd3"));
			restaurant.setPwd4(dbCon.rs.getString("pwd4"));
			restaurant.setPwd5(dbCon.rs.getString("pwd5"));
			restaurant.setRecordAlive(dbCon.rs.getInt("record_alive"));
			restaurant.setRestaurantInfo(dbCon.rs.getString("restaurant_info"));
			restaurant.setRestaurantName(dbCon.rs.getString("restaurant_name"));
			restaurant.setTele1(dbCon.rs.getString("tele1"));
			restaurant.setTele2(dbCon.rs.getString("tele2"));
			restaurant.setAddress(dbCon.rs.getString("address"));
			
		}else{
			throw new BusinessException(RestaurantError.RESTAURANT_NOT_FOUND);
		}
		
		dbCon.rs.close();
		
		return restaurant;
			
	}
	
	/**
	 * Update a specified restaurant. 
	 * @param term
	 * 			the terminal
	 * @param restaurant
	 * 			the restaurant to update
	 * @return the count to modified restaurant record
	 * @throws BusinessException
	 * 			if the restaurant to update does NOT exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 */
	public static void update(Terminal term, Restaurant restaurant) throws SQLException, BusinessException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, term, restaurant);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update a specified restaurant. 
	 * @param dbCon
	 * 			The database connection
	 * @param term
	 * 			the terminal
	 * @param restaurant
	 * 			the restaurant to update
	 * @return the count to modified restaurant record
	 * @throws BusinessException
	 * 			if the restaurant to update does NOT exist
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 */
	private static void update(DBCon dbCon, Terminal term, Restaurant restaurant) throws SQLException, BusinessException{
		String sql = " UPDATE " + Params.dbName + ".restaurant SET " +
					 " restaurant_info = '" + restaurant.getRestaurantInfo() + "'," +
					 " restaurant_name = '" + restaurant.getRestaurantName() + "'," +
					 " address = '" + restaurant.getAddress() + "'," +
					 " restaurant.tele1 = '" + restaurant.getTele1() + "'," +
					 " restaurant.tele2 = '" + restaurant.getTele2() + "' " +
					 " WHERE " +
					 " id = " + term.restaurantID;
		
		if(dbCon.stmt.executeUpdate(sql) != 1){
			throw new BusinessException(RestaurantError.UPDATE_RESTAURANT_FAIL);
		}
	}
}
