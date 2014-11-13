package com.wireless.db.weixin.restaurant;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.oss.OssImageDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.weixin.CalcWeixinSignature;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxRestaurantError;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WeixinRestaurant;
import com.wireless.util.StringHtml;

public class WeixinRestaurantDao {
	
	/**
	 * Insert a new record.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void insert(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".weixin_restaurant" +
		      " (restaurant_id, status) " +
			  " VALUES (" + staff.getRestaurantId() + "," + WeixinRestaurant.Status.CREATED.getVal() + ")";
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Insert a new record.
	 * @param staff
	 * 			the staff to perform this action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void insert(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			insert(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the weixin restaurant according to builder {@link WeixinRestaurant#UpdateBuilder}
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link WeixinRestaurant#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin restaurant does NOT exist
	 */
	public static void update(Staff staff, WeixinRestaurant.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the weixin restaurant according to builder {@link WeixinRestaurant#UpdateBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link WeixinRestaurant#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin restaurant does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, WeixinRestaurant.UpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		
		WeixinRestaurant wr = builder.build();
		sql = " UPDATE " + Params.dbName + ".weixin_restaurant SET " +
			  " restaurant_id = " + staff.getRestaurantId() + 
			  (builder.isWeixinLogoChanged() ? " ,weixin_logo = '" + wr.getWeixinLogo().getId() + "'" : "") +
			  (builder.isWeixinInfoChanged() ? " ,weixin_info = '" + new StringHtml(wr.getWeixinInfo(), StringHtml.ConvertTo.TO_NORMAL) + "'" : "") +
			  (builder.isWeixinAppIdChanged() ? " ,app_id = '" + wr.getWeixinAppId() + "'" : "") +
			  (builder.isWeixinSecretChanged() ? " ,app_secret = '" + wr.getWeixinAppSecret() + "'" : "") +
			  " WHERE restaurant_id = " + staff.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(WxRestaurantError.WEIXIN_RESTAURANT_NOT_EXIST);
		}
		
		//Associated with the logo oss image.
		if(builder.isWeixinLogoChanged()){
			try{
				OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder(wr.getWeixinLogo().getId()).setSingleAssociated(OssImage.Type.WX_LOGO, staff.getRestaurantId()));
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		//Associated with the info oss image.
		if(builder.isWeixinInfoChanged()){
			OssImageDao.update(dbCon, staff, new OssImage.UpdateBuilder4Html(OssImage.Type.WX_INFO, staff.getRestaurantId()).setHtml(wr.getWeixinInfo()));
		}
	}

	/**
	 * Get the weixin restaurant to specific restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the weixin restaurant 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin restaurant does NOT exist
	 */
	public static WeixinRestaurant get(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return get(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the weixin restaurant to specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the weixin restaurant 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin restaurant does NOT exist
	 */
	public static WeixinRestaurant get(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		List<WeixinRestaurant> result = getByCond(dbCon, staff, null, null);
		if(result.isEmpty()){
			throw new BusinessException(WxRestaurantError.WEIXIN_RESTAURANT_NOT_EXIST);
		}else{
			WeixinRestaurant wr = result.get(0);
			if(wr.hasWeixinLogo()){
				wr.setWeixinLogo(OssImageDao.getById(dbCon, staff, wr.getWeixinLogo().getId()));
			}
			return wr;
		}
	}
	
	private static List<WeixinRestaurant> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".weixin_restaurant" +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		
		List<WeixinRestaurant> result = new ArrayList<WeixinRestaurant>();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			WeixinRestaurant wr = new WeixinRestaurant(dbCon.rs.getInt("restaurant_id"));
			if(dbCon.rs.getTimestamp("bind_date") != null){
				wr.setBindDate(dbCon.rs.getTimestamp("bind_date").getTime());
			}
			wr.setStatus(WeixinRestaurant.Status.valueOf(dbCon.rs.getInt("status")));
			wr.setWeixinAppId(dbCon.rs.getString("app_id"));
			wr.setWeixinAppSecret(dbCon.rs.getString("app_secret"));
			String info = dbCon.rs.getString("weixin_info");
			if(info != null && !info.isEmpty()){
				wr.setWeixinInfo(new StringHtml(info, StringHtml.ConvertTo.TO_HTML).toString());
			}
			wr.setWeixinSerial(dbCon.rs.getString("weixin_serial"));
			if(dbCon.rs.getInt("weixin_logo") != 0){
				wr.setWeixinLogo(new OssImage(dbCon.rs.getInt("weixin_logo")));
			}
			result.add(wr);
		}
		dbCon.rs.close();
		
		return result;
	}
	
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
		// 以餐厅的account作为TOKEN
		Restaurant restaurant = RestaurantDao.getByAccount(dbCon, account);
		
		if(signature.equals(CalcWeixinSignature.calc(restaurant.getAccount(), timestamp, nonce))) {
			// 请求验证成功，更新openId和餐厅的验证记录
			String sql;
			
			sql = " UPDATE " + Params.dbName + ".weixin_restaurant SET" +
				  " restaurant_id = " + restaurant.getId() +
				  " ,status = " + WeixinRestaurant.Status.VERIFIED.getVal() +
				  " WHERE restaurant_id = " + restaurant.getId();
			
			if(dbCon.stmt.executeUpdate(sql) == 0){
				throw new BusinessException(WxRestaurantError.WEIXIN_RESTAURANT_NOT_EXIST);
			}
			
		}else{
			throw new BusinessException(WxRestaurantError.WEIXIN_RESTAURANT_VERIFY_FAIL);
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
			
			sql  = " UPDATE " + Params.dbName + ".weixin_restaurant SET " +
				   " restaurant_id = " + restaurant.getId() +
				   " ,weixin_serial_crc = CRC32('" + weixinRestaurantSerial + "')" +
				   " ,weixin_serial = '" + weixinRestaurantSerial + "'" +
				   " ,bind_date = NOW() " +
				   " ,status = " + WeixinRestaurant.Status.BOUND.getVal() +
				   " WHERE restaurant_id = " + restaurant.getId();
			
			if(dbCon.stmt.executeUpdate(sql) == 0){
				throw new BusinessException(WxRestaurantError.WEIXIN_RESTAURANT_NOT_EXIST);
			}
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
			throw new BusinessException(WxRestaurantError.WEIXIN_RESTAURANT_NOT_BOUND);
		}
		return restaurantId;
	}
	
//	/**
//	 * 
//	 * @param dbCon
//	 * @param rid
//	 * @return
//	 * @throws SQLException
//	 * @throws BusinessException
//	 */
//	public static String getInfo(DBCon dbCon, int rid) throws SQLException, BusinessException{
//		String info = "";
//		String querySQL = "SELECT weixin_info FROM weixin_misc WHERE restaurant_id = " + rid;
//		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
//		if(dbCon.rs != null && dbCon.rs.next()){
//			info = dbCon.rs.getString(1);
//			info = info == null ? "" : info;
//			info = info.replaceAll("&amp;", "&")
//					.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"")
//					.replaceAll("\r&#10;", "　\n").replaceAll("&#10;", "　\n").replaceAll("&#032;", " ")
//					.replaceAll("&#039;", "'").replaceAll("&#033;", "!");
//		}
//		return info;
//	}
//	public static String getInfo(int rid) throws SQLException, BusinessException{
//		DBCon dbCon = null;
//		try{
//			dbCon = new DBCon();
//			dbCon.connect();
//			return getInfo(dbCon, rid);
//		}finally{
//			if(dbCon != null) dbCon.disconnect();
//		}
//	}
//	
//	/**
//	 * 
//	 * @param dbCon
//	 * @param serial
//	 * @return
//	 * @throws SQLException
//	 * @throws BusinessException
//	 */
//	public static String getInfoByRestaurantSerial(DBCon dbCon, String serial) throws SQLException, BusinessException{
//		int rid = getRestaurantIdByWeixin(dbCon, serial);
//		return getInfo(dbCon, rid);
//	}
//	public static String getInfoByRestaurantSerial(String serial) throws SQLException, BusinessException{
//		DBCon dbCon = null;
//		try{
//			dbCon = new DBCon();
//			dbCon.connect();
//			return getInfoByRestaurantSerial(dbCon, serial);
//		}finally{
//			if(dbCon != null) dbCon.disconnect();
//		}
//	}
//	
//	/**
//	 * 
//	 * @param dbCon
//	 * @param rid
//	 * @param info
//	 * @throws SQLException
//	 * @throws BusinessException
//	 */
//	public static void updateInfo(DBCon dbCon, int rid, String info) throws SQLException, BusinessException{
//		info = info.replaceAll("&", "&amp;")
//			.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;")
//			.replaceAll("\n\r", "&#10;").replaceAll("\r\n", "&#10;").replaceAll("\n", "&#10;")
//			.replaceAll(" ", "&#032;").replaceAll("'", "&#039;").replaceAll("!", "&#033;");
//		String updateSQL = "UPDATE weixin_misc SET weixin_info = '" + info + "' WHERE restaurant_id = " + rid;
//		if(dbCon.stmt.executeUpdate(updateSQL) == 0){
//			throw new BusinessException(WeixinRestaurantError.WEIXIN_UPDATE_INFO_FAIL);
//		}
//	}
//	public static void updateInfo(int rid, String info) throws SQLException, BusinessException{
//		DBCon dbCon = null;
//		try{
//			dbCon = new DBCon();
//			dbCon.connect();
//			updateInfo(dbCon, rid, info);
//		}finally{
//			if(dbCon != null) dbCon.disconnect();
//		}
//	}
//	
//	/**
//	 * 
//	 * @param dbCon
//	 * @param serial
//	 * @param info
//	 * @throws SQLException
//	 * @throws BusinessException
//	 */
//	public static void updateInfoByRestaurantSerial(DBCon dbCon, String serial, String info) throws SQLException, BusinessException{
//		int rid = getRestaurantIdByWeixin(dbCon, serial);
//		updateInfo(dbCon, rid, info);
//	}
//	public static void updateInfoByRestaurantSerial(String serial, String info) throws SQLException, BusinessException{
//		DBCon dbCon = null;
//		try{
//			dbCon = new DBCon();
//			dbCon.connect();
//			updateInfoByRestaurantSerial(dbCon, serial, info);
//		}finally{
//			if(dbCon != null) dbCon.disconnect();
//		}
//	}
//	
//	/**
//	 * 
//	 * @param dbCon
//	 * @param rid
//	 * @param imgKey
//	 * @throws SQLException
//	 */
//	public static void addImageMaterial(DBCon dbCon, int rid, String imgKey) throws SQLException{
//		String insertSQL = "INSERT INTO weixin_image (restaurant_id,image,last_modified)"
//				+ " VALUES(" + rid + ",'" + imgKey + "', NOW())";
//		dbCon.stmt.executeUpdate(insertSQL);
//	}
//	public static void addImageMaterial(int rid, String imgKey) throws SQLException{
//		DBCon dbCon = null;
//		try{
//			dbCon = new DBCon();
//			dbCon.connect();
//			addImageMaterial(dbCon, rid, imgKey);
//		}finally{
//			if(dbCon != null) dbCon.disconnect();
//		}
//	}
//	
//	/**
//	 * 
//	 * @param dbCon
//	 * @param restaurantId
//	 * @return
//	 * @throws SQLException
//	 * @throws BusinessException 
//	 */
//	public static String getLogo(DBCon dbCon, int restaurantId) throws SQLException, BusinessException{
//		String sql = " SELECT weixin_logo FROM weixin_misc WHERE restaurant_id = " + restaurantId;
//		String logo = null;
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		if(dbCon.rs.next()){
//			logo = dbCon.rs.getString(1);
//		}else{
//			throw new BusinessException("");
//		}
//		dbCon.rs.close();
//		return logo;
//	}
//	
//	public static String getLogo(int rid) throws SQLException, BusinessException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getLogo(dbCon, rid);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
//	
//	public static String getLogoByRestaurantSerial(DBCon dbCon, String serial) throws SQLException, BusinessException{
//		return getLogo(dbCon, getRestaurantIdByWeixin(dbCon, serial));
//	}
//	public static String getLogoByRestaurantSerial(String serial) throws SQLException, BusinessException{
//		DBCon dbCon = null;
//		try{
//			dbCon = new DBCon();
//			dbCon.connect();
//			return getLogoByRestaurantSerial(dbCon, serial);
//		}finally{
//			if(dbCon != null) dbCon.disconnect();
//		}
//	}
//	
//	/**
//	 * 
//	 * @param dbCon
//	 * @param rid
//	 * @param imgKey
//	 * @throws SQLException
//	 */
//	public static void updateLogo(DBCon dbCon, int rid, String imgKey) throws SQLException{
//		String updateSQL = "UPDATE weixin_misc SET weixin_logo = '" + imgKey + "' WHERE restaurant_id = " + rid;
//		dbCon.stmt.executeUpdate(updateSQL);
//	}
//	public static void updateLogo(int rid, String imgKey) throws SQLException{
//		DBCon dbCon = null;
//		try{
//			dbCon = new DBCon();
//			dbCon.connect();
//			updateLogo(dbCon, rid, imgKey);
//		}finally{
//			if(dbCon != null) dbCon.disconnect();
//		}
//	}
//	
	
}
