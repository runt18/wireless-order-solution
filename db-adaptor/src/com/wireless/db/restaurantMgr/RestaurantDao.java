package com.wireless.db.restaurantMgr;

import java.sql.SQLException;

import com.wireless.db.DBCon;
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
	 */
	public static Restaurant queryByID(Terminal term) throws SQLException{

		DBCon dbCon = new DBCon();
		try{
			return query(dbCon, term, null, null);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query a restaurant according to specified id defined in terminal {@link Terminal} and other condition.
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
	 */
	private static Restaurant query(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		String sql = " SELECT * FROM restaurant " +
					 " WHERE 1 = 1 " +
					 " AND restaurant.id = " + term.restaurantID +
					 (extraCond != null ? extraCond : " ") +
					 (orderClause != null ? orderClause : " ");
		dbCon.connect();
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
		}
		dbCon.rs.close();
		return restaurant;
			
	}
	
	/**
	 * 根据餐厅ID修改餐厅信息
	 * @param restaurant
	 * @return
	 */
	public static boolean update(int restaurantID,Restaurant restaurant){
		boolean success = false;
		try{
			DBCon dbCon = new DBCon();
			String sql = "UPDATE restaurant SET restaurant.restaurant_info = '"+restaurant.getRestaurantInfo()+"',restaurant.restaurant_name='"+restaurant.getRestaurantName()+"',address='"+restaurant.getAddress()+"',restaurant.tele1='"+restaurant.getTele1()+"',restaurant.tele2='"+restaurant.getTele2()+"' WHERE restaurant.id = "+restaurantID+"";
			dbCon.connect();
			int rs = dbCon.stmt.executeUpdate(sql);
			if(rs > 0){
				success = true;
			}
			else{
				success = false; 
			}
			dbCon.disconnect();
		}
		catch(Exception e){
			success = false;
			e.printStackTrace();
		}
		return success;
	}
}
