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
			
			ArrayList<Staff> staffs = new ArrayList<Staff>();
			
			String sql = "SELECT a.name, a.pwd, b.pin FROM " + Params.dbName + ".staff a, terminal b WHERE a.restaurant_id=" +
						 + restaurantID + " AND a.restaurant_id=b.restaurant_id AND a.terminal_id=b.terminal_id";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				staffs.add(new Staff(dbCon.rs.getString(1),
									 dbCon.rs.getString(2),
									 dbCon.rs.getInt(3)));
			}
			dbCon.rs.close();
			return staffs.toArray(new Staff[staffs.size()]);
		}finally{
			dbCon.disconnect();
		}
	}
}
