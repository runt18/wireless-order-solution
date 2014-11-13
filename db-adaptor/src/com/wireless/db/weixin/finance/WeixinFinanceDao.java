package com.wireless.db.weixin.finance;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxFinanceError;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Role;

public class WeixinFinanceDao {
	
	/**
	 * Bind the weixin serial with restaurant.
	 * @param weixinSerial
	 * 			the weixin serial
	 * @param account
	 * 			the restaurant account
	 * @param pwd
	 * 			the password to admin of restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin serial has exist before<br>
	 * 			throws if the either account or password is NOT correct 
	 */
	public static void bind(String weixinSerial, String account, String pwd) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			bind(dbCon, weixinSerial, account, pwd);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Bind the weixin serial with restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinSerial
	 * 			the weixin serial
	 * @param account
	 * 			the restaurant account
	 * @param pwd
	 * 			the password to admin of restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the either account or password is NOT correct 
	 */
	public static void bind(DBCon dbCon, String weixinSerial, String account, String pwd) throws SQLException, BusinessException{
		
		String sql;

		//Delete the previous bind relationship.
		sql = " DELETE FROM " + Params.dbName + ".weixin_finance " +
			  " WHERE weixin_serial_crc = CRC32('" + weixinSerial + "')" +
			  " AND weixin_serial = '" + weixinSerial + "'";
		dbCon.stmt.executeUpdate(sql);
		
		//Check to see whether the password is matched with the admin
		sql = " SELECT REST.id " +
			  " FROM " + Params.dbName + ".staff S " +
			  " JOIN " + Params.dbName + ".restaurant REST ON S.restaurant_id = REST.id AND REST.account = '" + account + "'" +
			  " JOIN " + Params.dbName + ".role ROLE ON ROLE.cate = " + Role.Category.ADMIN.getVal() + " AND " + " S.role_id = ROLE.role_id " +
			  " WHERE S.pwd = MD5('" + pwd + "')";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int restaurantId = 0;
		if(dbCon.rs.next()){
			restaurantId = dbCon.rs.getInt("id");
		}else{
			dbCon.rs.close();
			throw new BusinessException(WxFinanceError.ACCOUNT_PWD_NOT_MATCH);
		}
		dbCon.rs.close();
		
		//Insert the binding between weixin serial and restaurant
		sql = " INSERT INTO " + Params.dbName + ".weixin_finance" +
			  "(`weixin_serial`, `restaurant_id`, `weixin_serial_crc`, `bind_date`)" +
			  " VALUES(" +
			  "'" + weixinSerial + "'," +
			  restaurantId + "," +
			  "CRC32('" + weixinSerial + "')," +
			  " NOW() " +
			  ")";
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Bind the weixin serial with restaurant.
	 * @param weixinSerial
	 * 			the weixin serial
	 * @param restaurantId
	 * 			the restaurant id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant does NOT exist
	 */
	public static Restaurant bind(String weixinSerial, int restaurantId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return bind(dbCon, weixinSerial, restaurantId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Bind the weixin serial with restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinSerial
	 * 			the weixin serial
	 * @param restaurantId
	 * 			the restaurant id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the restaurant does NOT exist
	 */
	public static Restaurant bind(DBCon dbCon, String weixinSerial, int restaurantId) throws SQLException, BusinessException{
		
		Restaurant restaurant = RestaurantDao.getById(dbCon, restaurantId);
		
		String sql;
		
		//Delete the previous bind relationship.
		sql = " DELETE FROM " + Params.dbName + ".weixin_finance " +
			  " WHERE weixin_serial_crc = CRC32('" + weixinSerial + "')" +
			  " AND weixin_serial = '" + weixinSerial + "'";
		dbCon.stmt.executeUpdate(sql);
		
		//Insert the binding between weixin serial and restaurant
		sql = " INSERT INTO " + Params.dbName + ".weixin_finance" +
			  "(`weixin_serial`, `restaurant_id`, `weixin_serial_crc`, `bind_date`)" +
			  " VALUES(" +
			  "'" + weixinSerial + "'," +
			  restaurantId + "," +
			  "CRC32('" + weixinSerial + "')," +
			  " NOW() " +
			  ")";
		dbCon.stmt.executeUpdate(sql);
		
		return restaurant;
		
	}
	
	/**
	 * Cancel the relationship.
	 * @param weixinSerial
	 * 			the weixin serial
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void cancel(String weixinSerial) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			cancel(dbCon, weixinSerial);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	/**
	 * Cancel the relationship.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinSerial
	 * 			the weixin serial
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void cancel(DBCon dbCon, String weixinSerial) throws SQLException{
		String sql;
		
		//Delete the previous bind relationship.
		sql = " DELETE FROM " + Params.dbName + ".weixin_finance " +
			  " WHERE weixin_serial_crc = CRC32('" + weixinSerial + "')" +
			  " AND weixin_serial = '" + weixinSerial + "'";
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Get the restaurant id to the specific weixin serial.
	 * @param weixinSerial
	 * 			the weinxin serial
	 * @return the restaurant id to this weixin serial
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin serial does NOT exist
	 */
	public static int getRestaurantIdByWeixin(String weixinSerial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getRestaurantIdByWeixin(dbCon, weixinSerial);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the restaurant id to the specific weixin serial.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinSerial
	 * 			the weinxin serial
	 * @return the restaurant id to this weixin serial
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin serial does NOT exist
	 */
	public static int getRestaurantIdByWeixin(DBCon dbCon, String weixinSerial) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT restaurant_id FROM " + Params.dbName + ".weixin_finance " +
			  " WHERE weixin_serial_crc = CRC32('" + weixinSerial + "')" +
			  " AND weixin_serial = '" + weixinSerial + "'";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int restaurantId = 0;
		if(dbCon.rs.next()){
			restaurantId = dbCon.rs.getInt("restaurant_id");
		}else{
			throw new BusinessException(WxFinanceError.WEIXIN_SERIAL_NOT_BOUND);
		}
		dbCon.rs.close();
		
		return restaurantId;
	}
	
}
