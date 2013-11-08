package com.wireless.db.weixin.restaurant;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.weixin.CalcWeixinSignature;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WeixinRestaurantError;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.weixin.restaurant.WeixinRestaurant;

public class WeixinRestaurantDao {
	
	/**
	 * Verify the restaurant account to weixin server.
	 * @param account
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the account does NOT exist
	 * @throws NoSuchAlgorithmException
	 * 			throws if the algorithm is NOT correct
	 */
	public static void verify(String account, String signature, String timestamp, String nonce) throws SQLException, BusinessException, NoSuchAlgorithmException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			verify(dbCon, account, signature, timestamp, nonce);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Verify the restaurant account to weixin server.
	 * @param dbCon
	 * @param account
	 * @param signature
	 * @param timestamp
	 * @param nonce
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the account does NOT exist
	 * @throws NoSuchAlgorithmException
	 * 			throws if the algorithm is NOT correct
	 */
	public static void verify(DBCon dbCon, String account, String signature, String timestamp, String nonce) throws SQLException, BusinessException, NoSuchAlgorithmException{
		// FIXME 以餐厅的account作为TOKEN
		Restaurant restaurant = RestaurantDao.getByAccount(dbCon, account);
		
		if(signature.equals(CalcWeixinSignature.calc(restaurant.getAccount(), timestamp, nonce))) {
			// 请求验证成功，生成一条微信openId和餐厅的验证记录
			String sql;
			
			sql = " DELETE FROM " + Params.dbName + ".weixin_restaurant WHERE " +
				  " restaurant_id = ( SELECT id FROM " + Params.dbName + ".restaurant WHERE account = '" + account + "' )";
			dbCon.stmt.executeUpdate(sql);
			
			sql = " INSERT INTO " + Params.dbName + ".weixin_restaurant " +
				  " (`restaurant_id`, `status`) " +
				  " VALUES( " +
				  restaurant.getId() + "," +
				  WeixinRestaurant.Status.VERIFIED.getVal() +
				  ")";
			dbCon.stmt.executeUpdate(sql);
			
		}else{
			throw new BusinessException(WeixinRestaurantError.WEIXIN_RESTAURANT_VERIFY_FAIL);
		}
	}
	
	/**
	 * Check if the account has been verified.
	 * @param dbCon
	 * 			the database connection
	 * @param account
	 * 			the restaurant account to check
	 * @return true if the account has been verified, otherwise false
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static boolean isVerified(String account) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return isVerified(dbCon, account);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Check if the account has been verified.
	 * @param dbCon
	 * 			the database connection
	 * @param account
	 * 			the restaurant account to check
	 * @return true if the account has been verified, otherwise false
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static boolean isVerified(DBCon dbCon, String account) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".weixin_restaurant WHERE " +
			  " restaurant_id = ( SELECT id FROM " + Params.dbName + ".restaurant WHERE account = '" + account + "' )";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		boolean isVerified = false;
		if(dbCon.rs.next()){
			isVerified = true;
		}
		
		dbCon.rs.close();
		
		return isVerified;
	}
	
	/**
	 * Check if the weixin serial has has been bound.
	 * @param weixinRestaurantSerial
	 * 			the weixin serial to check
	 * @return true if the weixin serial has been bound, otherwise false
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static boolean isBound(String weixinRestaurantSerial) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return isBound(dbCon, weixinRestaurantSerial);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Check if the weixin serial has has been bound.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinRestaurantSerial
	 * 			the weixin serial to check
	 * @return true if the weixin serial has been bound, otherwise false
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static boolean isBound(DBCon dbCon, String weixinRestaurantSerial) throws SQLException{
		//Check to see whether the weixin serial has been bound.
		String sql;
		sql = " SELECT restaurant_id FROM " + Params.dbName + ".weixin_restaurant " +
			  " WHERE weixin_serial_crc = CRC32('" + weixinRestaurantSerial + "')" +
			  " AND weixin_serial = '" + weixinRestaurantSerial + "'" +
			  " AND status = " + WeixinRestaurant.Status.BOUND.getVal();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		boolean isBound = false;;
		
		if(dbCon.rs.next()){
			isBound = true;
		}
		
		dbCon.rs.close();
		return isBound;

	}
	
	/**
	 * bind the weixin serial and restaurant account.
	 * @param weixinRestaurantSerial
	 * 			the weinxin serial to bind
	 * @param account
	 * 			the restaurant to bind
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the account does NOT exist
	 */
	public static void bind(String weixinRestaurantSerial, String account) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			bind(dbCon, weixinRestaurantSerial, account);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * bind the weixin serial and restaurant account.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinRestaurantSerial
	 * 			the weinxin serial to bind
	 * @param account
	 * 			the restaurant to bind
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the account does NOT exist
	 */
	public static void bind(DBCon dbCon, String weixinRestaurantSerial, String account) throws SQLException, BusinessException{
		if(!isBound(dbCon, weixinRestaurantSerial)){
			Restaurant restaurant = RestaurantDao.getByAccount(dbCon, account);
			String sql;
			sql = " INSERT INTO " + Params.dbName + ".weixin_restaurant " +
				  " (`weixin_serial`, `weixin_serial_crc`, `restaurant_id`, `status`, `bind_date`) " +
				  " VALUES( " +
				  "'" + weixinRestaurantSerial + "'," +
				  "CRC32('" + weixinRestaurantSerial + "')," +
				  restaurant.getId() + "," +
				  WeixinRestaurant.Status.BOUND.getVal() + "," +
				  "NOW()" +
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
	}
	
	/**
	 * Get the restaurant id according to its weixin serial.
	 * @param weixinRestaurantSerial
	 * 			the weixin serial to restaurant
	 * @return the restaurant id to its weixin serial
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin serial isn't bound to any restaurant  
	 */
	public static int getRestaurantIdByWeixin(String weixinRestaurantSerial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getRestaurantIdByWeixin(dbCon, weixinRestaurantSerial);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get the restaurant id according to its weixin serial.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinRestaurantSerial
	 * 			the weixin serial to restaurant
	 * @return the restaurant id to its weixin serial
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin serial isn't bound to any restaurant  
	 */
	public static int getRestaurantIdByWeixin(DBCon dbCon, String weixinRestaurantSerial) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT restaurant_id FROM " + Params.dbName + ".weixin_restaurant " +
			  " WHERE weixin_serial_crc = CRC32('" + weixinRestaurantSerial + "')" +
			  " AND weixin_serial = '" + weixinRestaurantSerial + "'";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		int restaurantId = 0;
		if(dbCon.rs.next()){
			restaurantId = dbCon.rs.getInt("restaurant_id");
		}else{
			throw new BusinessException(WeixinRestaurantError.WEIXIN_RESTAURANT_NOT_BOUND);
		}
		return restaurantId;
	}
}
