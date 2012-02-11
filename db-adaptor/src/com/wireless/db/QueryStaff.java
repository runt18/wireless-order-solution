package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.protocol.Staff;

public class QueryStaff {
	
	/**
	 * Query all the staff information to a specific restaurant.
	 * @param restaurantID indicates which staff of restaurant to query
	 * @return an array holding all the staff to this restaurant
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Staff[] exec(int restaurantID) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, restaurantID, null, null);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query all the staff information to a specific restaurant.
	 * @param restaurantID indicates which staff of restaurant to query
	 * @param extraCond the extra condition to the SQL statement
	 * @param orderClause the order clause to the SQL statement
	 * @return an array holding all the staff to this restaurant
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Staff[] exec(int restaurantID, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, restaurantID, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query all the staff information to a specific restaurant.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon the database connection
	 * @param restaurantID indicates which staff of restaurant to query
	 * @param extraCond the extra condition to the SQL statement
	 * @param orderClause the order clause to the SQL statement
	 * @return an array holding all the staff to this restaurant
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Staff[] exec(DBCon dbCon, int restaurantID, String extraCond, String orderClause) throws SQLException{
		dbCon.connect();
			
		ArrayList<Staff> staffs = new ArrayList<Staff>();
			
		String sql = "SELECT a.staff_id, a.name, a.pwd, b.pin FROM " + Params.dbName + ".staff a, terminal b WHERE a.restaurant_id=" +
					 restaurantID + " AND a.restaurant_id=b.restaurant_id AND a.terminal_id=b.terminal_id " +
					 (extraCond != null ? extraCond : " ") +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			staffs.add(new Staff(dbCon.rs.getLong(1),
								 dbCon.rs.getString(2),
								 dbCon.rs.getString(3),
								 dbCon.rs.getInt(4)));
		}
		return staffs.toArray(new Staff[staffs.size()]);

	}
	
}
