package com.wireless.db.weixin.restaurant;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
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
	 * Check if the weixin serial has has been bound with the restaurant account.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinRestaurantSerial
	 * 			the weixin serial to check
	 * @return true if the weixin serial has been bound, otherwise false
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the restaurant account does NOT exist 
	 */
	public static boolean isBound(String weixinRestaurantSerial, String account) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return isBound(dbCon, weixinRestaurantSerial, account);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Check if the weixin serial has has been bound with the restaurant account.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinRestaurantSerial
	 * 			the weixin serial to check
	 * @return true if the weixin serial has been bound, otherwise false
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the restaurant account does NOT exist 
	 */
	public static boolean isBound(DBCon dbCon, String weixinRestaurantSerial, String account) throws SQLException, BusinessException{
		//Check to see whether the weixin serial has been bound with the account.
		String sql;
		sql = " SELECT restaurant_id FROM " + Params.dbName + ".weixin_restaurant " +
			  " WHERE weixin_serial_crc = CRC32('" + weixinRestaurantSerial + "')" +
			  " AND weixin_serial = '" + weixinRestaurantSerial + "'" +
			  " AND status = " + WeixinRestaurant.Status.BOUND.getVal() +
			  " AND restaurant_id = ( SELECT id FROM " + Params.dbName + ".restaurant WHERE account = '" + account + "' )";
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
		if(!isBound(dbCon, weixinRestaurantSerial, account)){
			Restaurant restaurant = RestaurantDao.getByAccount(dbCon, account);
			String sql;
			
			sql = " DELETE FROM " + Params.dbName + ".weixin_restaurant " +
				  " WHERE 1 = 1 " +
				  " AND (weixin_serial_crc = CRC32('" + weixinRestaurantSerial + "')" +
				  " AND weixin_serial = '" + weixinRestaurantSerial + "')" +
				  " OR restaurant_id = " + restaurant.getId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " INSERT INTO " + Params.dbName + ".weixin_restaurant " +
				  " (`weixin_serial`, `weixin_serial_crc`, `restaurant_id`, `bind_date`, `status`) " +
				  " VALUES(" +
				  "'" + weixinRestaurantSerial + "'," +
				  "CRC32('" + weixinRestaurantSerial + "')," +
				  restaurant.getId() + "," +
				  "NOW()," +
				  WeixinRestaurant.Status.BOUND.getVal() +
				  ")";
			dbCon.stmt.executeUpdate(sql);
		}
	}
	
	/**
	 * Get the restaurant id according to its weixin serial.
	 * @param weixinRestaurantSerial
	 * 			the weixin serial to restaurant
	 * @return the restaurant id this weixin restaurant serial belongs to
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
	 * @return the restaurant id this weixin restaurant serial belongs to
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
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static String getInfo(DBCon dbCon, int rid) throws SQLException, BusinessException{
		String info = "";
		String querySQL = "SELECT weixin_info FROM weixin_misc WHERE restaurant_id = " + rid;
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			info = dbCon.rs.getString(1);
			info = info == null ? "" : info;
			info = info.replaceAll("&amp;", "&")
					.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"")
					.replaceAll("\r&#10;", "　\n").replaceAll("&#10;", "　\n").replaceAll("&#032;", " ")
					.replaceAll("&#039;", "'").replaceAll("&#033;", "!");
		}
		return info;
	}
	public static String getInfo(int rid) throws SQLException, BusinessException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			return getInfo(dbCon, rid);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param serial
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static String getInfoByRestaurantSerial(DBCon dbCon, String serial) throws SQLException, BusinessException{
		int rid = getRestaurantIdByWeixin(dbCon, serial);
		return getInfo(dbCon, rid);
	}
	public static String getInfoByRestaurantSerial(String serial) throws SQLException, BusinessException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			return getInfoByRestaurantSerial(dbCon, serial);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @param info
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void updateInfo(DBCon dbCon, int rid, String info) throws SQLException, BusinessException{
		info = info.replaceAll("&", "&amp;")
			.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;")
			.replaceAll("\n\r", "&#10;").replaceAll("\r\n", "&#10;").replaceAll("\n", "&#10;")
			.replaceAll(" ", "&#032;").replaceAll("'", "&#039;").replaceAll("!", "&#033;");
		String updateSQL = "UPDATE weixin_misc SET weixin_info = '" + info + "' WHERE restaurant_id = " + rid;
		if(dbCon.stmt.executeUpdate(updateSQL) == 0){
			throw new BusinessException(WeixinRestaurantError.WEIXIN_UPDATE_INFO_FAIL);
		}
	}
	public static void updateInfo(int rid, String info) throws SQLException, BusinessException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			updateInfo(dbCon, rid, info);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param serial
	 * @param info
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static void updateInfoByRestaurantSerial(DBCon dbCon, String serial, String info) throws SQLException, BusinessException{
		int rid = getRestaurantIdByWeixin(dbCon, serial);
		updateInfo(dbCon, rid, info);
	}
	public static void updateInfoByRestaurantSerial(String serial, String info) throws SQLException, BusinessException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			updateInfoByRestaurantSerial(dbCon, serial, info);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @param imgKey
	 * @throws SQLException
	 */
	public static void addImageMateril(DBCon dbCon, int rid, String imgKey) throws SQLException{
		String insertSQL = "INSERT INTO weixin_image (restaurant_id,image,last_modified)"
				+ " VALUES(" + rid + ",'" + imgKey + "', NOW())";
		dbCon.stmt.executeUpdate(insertSQL);
	}
	public static void addImageMateril(int rid, String imgKey) throws SQLException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			addImageMateril(dbCon, rid, imgKey);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @return
	 * @throws SQLException
	 */
	public static String getLogo(DBCon dbCon, int rid) throws SQLException{
		String querySQL = "SELECT weixin_logo FROM weixin_misc WHERE restaurant_id = " + rid;
		String logo = null;
		ResultSet res = dbCon.stmt.executeQuery(querySQL);
		if(res != null && res.next()){
			logo = res.getString(1);
		}
		return logo;
	}
	public static String getLogo(int rid) throws SQLException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			return getLogo(dbCon, rid);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	public static String getLogoByRestaurantSerial(DBCon dbCon, String serial) throws SQLException, BusinessException{
		return getLogo(dbCon, getRestaurantIdByWeixin(dbCon, serial));
	}
	public static String getLogoByRestaurantSerial(String serial) throws SQLException, BusinessException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			return getLogoByRestaurantSerial(dbCon, serial);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param rid
	 * @param imgKey
	 * @throws SQLException
	 */
	public static void updateLogo(DBCon dbCon, int rid, String imgKey) throws SQLException{
		String updateSQL = "UPDATE weixin_misc SET weixin_logo = '" + imgKey + "' WHERE restaurant_id = " + rid;
		dbCon.stmt.executeUpdate(updateSQL);
	}
	public static void updateLogo(int rid, String imgKey) throws SQLException{
		DBCon dbCon = null;
		try{
			dbCon = new DBCon();
			dbCon.connect();
			updateLogo(dbCon, rid, imgKey);
		}finally{
			if(dbCon != null) dbCon.disconnect();
		}
	}
	
	
}
