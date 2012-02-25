package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.protocol.StaffTerminal;
import com.wireless.protocol.Terminal;

public class QueryStaffTerminal {
	
	/**
	 * Query all the staff information to a specific restaurant.
	 * 
	 * @param restaurantID
	 *            The restaurant id
	 * @param extraCond
	 *            the extra condition to the SQL statement
	 * @param orderClause
	 *            the order clause to the SQL statement
	 * @return an array holding all the staff to this restaurant
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static StaffTerminal[] exec(int restaurantID, String extraCond, String orderClause) throws SQLException{
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
	 * 
	 * @param Terminal
	 *            The terminal to query
	 * @return an array holding all the staff to this restaurant
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static StaffTerminal[] exec(Terminal term) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term.restaurant_id, null, null);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query all the staff information to a specific restaurant.
	 * 
	 * @param Terminal
	 *            the terminal to query
	 * @param extraCond
	 *            the extra condition to the SQL statement
	 * @param orderClause
	 *            the order clause to the SQL statement
	 * @return an array holding all the staff to this restaurant
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static StaffTerminal[] exec(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term.restaurant_id, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query all the staff information to a specific restaurant. Note that the
	 * database should be connected before invoking this method.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param restaurantID
	 *            indicates which staff of restaurant to query
	 * @param extraCond
	 *            the extra condition to the SQL statement
	 * @param orderClause
	 *            the order clause to the SQL statement
	 * @return an array holding all the staff to this restaurant
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static StaffTerminal[] exec(DBCon dbCon, int restaurantID, String extraCond, String orderClause) throws SQLException{
		dbCon.connect();
			
		ArrayList<StaffTerminal> staffs = new ArrayList<StaffTerminal>();
			
		String sql = "SELECT a.staff_id, a.staff_alias, a.name, a.pwd, a.terminal_id, b.pin, b.gift_quota, b.gift_amount FROM " + 
					 Params.dbName + ".staff a, terminal b WHERE a.restaurant_id=" +
					 restaurantID + " AND a.restaurant_id=b.restaurant_id AND a.terminal_id=b.terminal_id " +
					 (extraCond != null ? extraCond : " ") +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			StaffTerminal staff = new StaffTerminal();
			staff.id = dbCon.rs.getLong("staff_id");
			staff.aliasID = dbCon.rs.getInt("staff_alias");
			staff.name = dbCon.rs.getString("name");
			staff.pwd = dbCon.rs.getString("pwd");
			staff.pin = dbCon.rs.getLong("pin");
			staff.terminalID = dbCon.rs.getLong("terminal_id");
			float quota = dbCon.rs.getFloat("gift_quota");
			if(quota >= 0){
				staff.setGiftQuota(quota);
			}
			staff.setGiftAmount(dbCon.rs.getFloat("gift_amount"));
			staffs.add(staff);
		}
		return staffs.toArray(new StaffTerminal[staffs.size()]);

	}
	
}
