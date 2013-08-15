 package com.wireless.db.restaurantMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.exception.RestaurantError;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;

public class RestaurantDao {
	
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
	public static Restaurant getById(int restaurantId) throws SQLException, BusinessException{

		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, restaurantId);
			
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
	public static Restaurant getById(DBCon dbCon, int restaurantId) throws SQLException, BusinessException{
		List<Restaurant> result = getByCond(dbCon, " AND id = " + restaurantId, null);
		if(result.isEmpty()){
			throw new BusinessException(RestaurantError.RESTAURANT_NOT_FOUND);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the restaurant according to account
	 * @param account
	 * 			the account to restaurant
	 * @return the restaurant associated with the account
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant to query does NOT exist
	 */
	public static Restaurant getByAccount(String account) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByAccount(dbCon, account);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the restaurant according to account
	 * @param dbCon
	 * 			the database connection
	 * @param account
	 * 			the account to restaurant
	 * @return the restaurant associated with the account
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant associated with this account does NOT exist
	 */
	public static Restaurant getByAccount(DBCon dbCon, String account) throws SQLException, BusinessException{
		List<Restaurant> result = getByCond(dbCon, " AND account = '" + account + "'", null);
		if(result.isEmpty()){
			throw new BusinessException(ProtocolError.ACCOUNT_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	private static List<Restaurant> getByCond(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		
		List<Restaurant> result = new ArrayList<Restaurant>();
		
		String sql = " SELECT * FROM " + Params.dbName + ".restaurant " +
				 	 " WHERE 1 = 1 " +
				 	 (extraCond != null ? extraCond : "") + " " +
				 	 (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			Restaurant restaurant = new Restaurant();
			restaurant.setAccount(dbCon.rs.getString("account"));
			restaurant.setId(dbCon.rs.getInt("id"));
			restaurant.setRecordAlive(dbCon.rs.getInt("record_alive"));
			restaurant.setInfo(dbCon.rs.getString("restaurant_info"));
			restaurant.setName(dbCon.rs.getString("restaurant_name"));
			restaurant.setTele1(dbCon.rs.getString("tele1"));
			restaurant.setTele2(dbCon.rs.getString("tele2"));
			restaurant.setAddress(dbCon.rs.getString("address"));
			result.add(restaurant);
		}
		dbCon.rs.close();
		
		return result;
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
	public static void update(Staff term, Restaurant restaurant) throws SQLException, BusinessException {
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
	private static void update(DBCon dbCon, Staff term, Restaurant restaurant) throws SQLException, BusinessException{
		String sql = " UPDATE " + Params.dbName + ".restaurant SET " +
					 " restaurant_info = '" + restaurant.getInfo() + "'," +
					 " restaurant_name = '" + restaurant.getName() + "'," +
					 " address = '" + restaurant.getAddress() + "'," +
					 " restaurant.tele1 = '" + restaurant.getTele1() + "'," +
					 " restaurant.tele2 = '" + restaurant.getTele2() + "' " +
					 " WHERE " +
					 " id = " + term.getRestaurantId();
		
		if(dbCon.stmt.executeUpdate(sql) != 1){
			throw new BusinessException(RestaurantError.UPDATE_RESTAURANT_FAIL);
		}
	}
}
