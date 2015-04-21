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
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.CalcWeixinSignature;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WxRestaurantError;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.weixin.restaurant.WxRestaurant;
import com.wireless.util.StringHtml;

public class WxRestaurantDao {
	
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
			  " VALUES (" + staff.getRestaurantId() + "," + WxRestaurant.Status.CREATED.getVal() + ")";
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
	 * Update the weixin restaurant according to builder {@link WxRestaurant#UpdateBuilder}
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link WxRestaurant#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin restaurant does NOT exist
	 */
	public static void update(Staff staff, WxRestaurant.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the weixin restaurant according to builder {@link WxRestaurant#UpdateBuilder}
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link WxRestaurant#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin restaurant does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, WxRestaurant.UpdateBuilder builder) throws SQLException, BusinessException{
		
		if(getByCond(dbCon, staff, null, null).isEmpty()){
			insert(dbCon, staff);
		}

		String sql;
		WxRestaurant wr = builder.build();
		
		//Delete the weixin restaurant with the same weixin serial.
		if(builder.isWxSerialChanged()){
			List<Integer> restaurants = new ArrayList<>();
			sql = " SELECT restaurant_id FROM " + Params.dbName + ".weixin_restaurant " +
				  " WHERE weixin_serial_crc = CRC32('" + wr.getWeixinSerial() + "')" +
				  " AND weixin_serial = '" + wr.getWeixinSerial() + "'" +
				  " AND restaurant_id <> " + staff.getRestaurantId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				restaurants.add(dbCon.rs.getInt("restaurant_id"));
			}
			dbCon.rs.close();
			
			for(int restaurantId : restaurants){
				sql = " DELETE FROM " + Params.dbName + ".weixin_restaurant WHERE restaurant_id = " + restaurantId;
				dbCon.stmt.executeUpdate(sql);
				
				insert(dbCon, StaffDao.getAdminByRestaurant(restaurantId));
			}
		}
		
		
		sql = " UPDATE " + Params.dbName + ".weixin_restaurant SET " +
			  " restaurant_id = " + staff.getRestaurantId() + 
			  (builder.isWxSerialChanged() ? " ,weixin_serial_crc = CRC32('" + wr.getWeixinSerial() + "') ,weixin_serial = '" + wr.getWeixinSerial() + "'" : "") +
			  (builder.isWeixinLogoChanged() ? " ,weixin_logo = '" + wr.getWeixinLogo().getId() + "'" : "") +
			  (builder.isWeixinInfoChanged() ? " ,weixin_info = '" + new StringHtml(wr.getWeixinInfo(), StringHtml.ConvertTo.TO_NORMAL) + "'" : "") +
			  (builder.isWeixinAppIdChanged() ? " ,app_id = '" + wr.getWeixinAppId() + "'" : "") +
			  (builder.isWeixinSecretChanged() ? " ,app_secret = '" + wr.getWeixinAppSecret() + "'" : "") +
			  (builder.isQrCodeUrlChanged() ? " ,qrcode_url = '" + wr.getQrCodeUrl() + "'" : "") +
			  (builder.isQrCodeChanged() ? " ,qrcode = '" + wr.getQrCode() + "'" : "") +
			  (builder.isNickNameChanged() ? " ,nick_name = '" + wr.getNickName() + "'" : "") +
			  (builder.isHeadImgUrlChanged() ? " ,head_img_url = '" + wr.getHeadImgUrl() + "'" : "") +
			  (builder.isRefreshTokenChanged() ? " ,refresh_token = '" + wr.getRefreshToken() + "'" : "") +
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
	public static WxRestaurant get(Staff staff) throws SQLException, BusinessException{
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
	public static WxRestaurant get(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		List<WxRestaurant> result = getByCond(dbCon, staff, null, null);
		if(result.isEmpty()){
			throw new BusinessException(WxRestaurantError.WEIXIN_RESTAURANT_NOT_EXIST);
		}else{
			WxRestaurant wr = result.get(0);
			if(wr.hasWeixinLogo()){
				wr.setWeixinLogo(OssImageDao.getById(dbCon, staff, wr.getWeixinLogo().getId()));
			}
			return wr;
		}
	}
	
	private static List<WxRestaurant> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".weixin_restaurant" +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		
		List<WxRestaurant> result = new ArrayList<WxRestaurant>();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			WxRestaurant wr = new WxRestaurant(dbCon.rs.getInt("restaurant_id"));
			if(dbCon.rs.getTimestamp("bind_date") != null){
				wr.setBindDate(dbCon.rs.getTimestamp("bind_date").getTime());
			}
			wr.setStatus(WxRestaurant.Status.valueOf(dbCon.rs.getInt("status")));
			wr.setWeixinAppId(dbCon.rs.getString("app_id"));
			wr.setWeixinAppSecret(dbCon.rs.getString("app_secret"));
			wr.setQrCodeUrl(dbCon.rs.getString("qrcode_url"));
			wr.setQrCode(dbCon.rs.getString("qrcode"));
			wr.setHeadImgUrl(dbCon.rs.getString("head_img_url"));
			wr.setRefreshToken(dbCon.rs.getString("refresh_token"));
			wr.setNickName(dbCon.rs.getString("nick_name"));
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
		
		final String token;
		if(restaurant.getAccount().length() < 3){
			token = "xxx";
		}else{
			token = restaurant.getAccount();
		}
		
		if(signature.equals(CalcWeixinSignature.calc(token, timestamp, nonce))) {
			// 请求验证成功，更新openId和餐厅的验证记录
			String sql;
			
			sql = " UPDATE " + Params.dbName + ".weixin_restaurant SET" +
				  " restaurant_id = " + restaurant.getId() +
				  " ,status = " + WxRestaurant.Status.VERIFIED.getVal() +
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
			  " AND status = " + WxRestaurant.Status.BOUND.getVal() +
			  " AND restaurant_id = ( SELECT id FROM " + Params.dbName + ".restaurant WHERE account = '" + account + "' )";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		boolean isBound = false;;
		
		if(dbCon.rs.next()){
			isBound = true;
		}
		
		dbCon.rs.close();
		return isBound;

	}
	
//	/**
//	 * bind the weixin serial and restaurant account.
//	 * @param weixinRestaurantSerial
//	 * 			the weinxin serial to bind
//	 * @param account
//	 * 			the restaurant to bind
//	 * @throws SQLException
//	 * 			throws if failed to execute any SQL statement
//	 * @throws BusinessException
//	 * 			throws if the account does NOT exist
//	 */
//	public static void bind(String weixinRestaurantSerial, String account) throws SQLException, BusinessException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			bind(dbCon, weixinRestaurantSerial, account);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
//	
//	/**
//	 * bind the weixin serial and restaurant account.
//	 * @param dbCon
//	 * 			the database connection
//	 * @param weixinRestaurantSerial
//	 * 			the weinxin serial to bind
//	 * @param account
//	 * 			the restaurant to bind
//	 * @throws SQLException
//	 * 			throws if failed to execute any SQL statement
//	 * @throws BusinessException
//	 * 			throws if the account does NOT exist
//	 */
//	public static void bind(DBCon dbCon, String weixinRestaurantSerial, String account) throws SQLException, BusinessException{
//		if(!isBound(dbCon, weixinRestaurantSerial, account)){
//			Restaurant restaurant = RestaurantDao.getByAccount(dbCon, account);
//			String sql;
//			
//			sql  = " UPDATE " + Params.dbName + ".weixin_restaurant SET " +
//				   " restaurant_id = " + restaurant.getId() +
//				   " ,weixin_serial_crc = CRC32('" + weixinRestaurantSerial + "')" +
//				   " ,weixin_serial = '" + weixinRestaurantSerial + "'" +
//				   " ,bind_date = NOW() " +
//				   " ,status = " + WxRestaurant.Status.BOUND.getVal() +
//				   " WHERE restaurant_id = " + restaurant.getId();
//			
//			if(dbCon.stmt.executeUpdate(sql) == 0){
//				throw new BusinessException(WxRestaurantError.WEIXIN_RESTAURANT_NOT_EXIST);
//			}
//		}
//	}
	
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
	
}
