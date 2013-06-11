package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
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
			return exec(dbCon, term.restaurantID, null, null);
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
			return exec(dbCon, term.restaurantID, extraCond, orderClause);
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
			
		List<StaffTerminal> staffs = new ArrayList<StaffTerminal>();
			
		/**
		 * Get the normal account
		 */
//		String sql = "SELECT a.staff_id, a.staff_alias, a.name, a.pwd, a.terminal_id, b.pin, b.gift_quota, b.gift_amount FROM " + 
//					 Params.dbName + ".staff a, terminal b WHERE a.restaurant_id=" +
//					 restaurantID + " AND a.restaurant_id=b.restaurant_id AND a.terminal_id=b.terminal_id " +
//					 (extraCond != null ? extraCond : " ") +
//					 (orderClause != null ? orderClause : "");
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		while(dbCon.rs.next()){
//			StaffTerminal staff = new StaffTerminal();
//			staff.id = dbCon.rs.getLong("staff_id");
//			staff.aliasID = dbCon.rs.getInt("staff_alias");
//			staff.name = dbCon.rs.getString("name");
//			staff.pwd = dbCon.rs.getString("pwd");
//			staff.pin = dbCon.rs.getLong("pin");
//			staff.terminalID = dbCon.rs.getLong("terminal_id");
//			float quota = dbCon.rs.getFloat("gift_quota");
//			if(quota >= 0){
//				staff.setGiftQuota(quota);
//			}
//			staff.setGiftAmount(dbCon.rs.getFloat("gift_amount"));
//			staffs.add(staff);
//		}
//		dbCon.rs.close();
		
		/**
		 * Get the admin account
		 */
//		sql = " SELECT A.terminal_id, A.pin, A.gift_quota, A.gift_amount, B.account AS name, B.pwd AS pwd FROM " + 
//			  Params.dbName + ".terminal A, " +
//			  Params.dbName + ".restaurant B " +
//			  " WHERE " +
//			  " A.restaurant_id = B.id " + 
//			  " AND " +
//			  " A.model_id = " + Terminal.MODEL_ADMIN +
//			  " AND " + 
//			  " B.id = " + restaurantID;
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		while(dbCon.rs.next()){
//			StaffTerminal staff = new StaffTerminal();
//			staff.name = dbCon.rs.getString("name");
//			staff.pwd = dbCon.rs.getString("pwd");
//			staff.pin = dbCon.rs.getLong("pin");
//			staff.terminalID = dbCon.rs.getLong("terminal_id");
//			float quota = dbCon.rs.getFloat("gift_quota");
//			if(quota >= 0){
//				staff.setGiftQuota(quota);
//			}
//			staff.setGiftAmount(dbCon.rs.getFloat("gift_amount"));
//			staffs.add(0, staff);
//		}
		
		String sql = "select terminal_id, pin, gift_quota, gift_amount, staff_id, staff_alias, name, pwd, type  from " +
				" (" +
				" select a.terminal_id, a.pin, a.gift_quota, a.gift_amount, b.staff_id, b.staff_alias, b.name, b.pwd, 0 as type " +
				" from terminal a, staff b " +
				" where a.restaurant_id=b.restaurant_id and a.terminal_id=b.terminal_id and  b.restaurant_id = " + restaurantID +
				" union all " +
				" select a.terminal_id, a.pin, a.gift_quota, a.gift_amount, 0 as staff_id, 0 as staff_alias, b.account as name, b.pwd as pwd, 1 as type " +
				" from terminal a, restaurant b " +
				" where  a.restaurant_id = b.id and a.model_id = " + Terminal.MODEL_ADMIN + " and b.id = " + restaurantID + 
				" ) t " +
				" where 1 = 1 " +
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
			staff.terminalId = dbCon.rs.getInt("terminal_id");
			staff.type = dbCon.rs.getInt("type");
			float quota = dbCon.rs.getFloat("gift_quota");
			if(quota >= 0){
				staff.setGiftQuota(quota);
			}
			staff.setGiftAmount(dbCon.rs.getFloat("gift_amount"));
			staffs.add(staff);
		}
		dbCon.rs.close();
		
		return staffs.toArray(new StaffTerminal[staffs.size()]);

	}
	
}
