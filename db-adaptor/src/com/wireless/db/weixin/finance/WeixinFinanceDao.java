package com.wireless.db.weixin.finance;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WeixinFinanceError;
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
	public static void bindRestaurant(String weixinSerial, String account, String pwd) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			bindRestaurant(dbCon, weixinSerial, account, pwd);
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
	public static void bindRestaurant(DBCon dbCon, String weixinSerial, String account, String pwd) throws SQLException, BusinessException{
		
		String sql;

//		//Check to see whether the weixin serial has exist before
//		sql = " SELECT restaurant_id FROM " + Params.dbName + ".weixin_restaurant " +
//			  " WHERE weixin_serial_crc = CRC32('" + weixinSerial + "')" +
//			  " AND weixin_serial = '" + weixinSerial + "'";
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		if(dbCon.rs.next()){
//			dbCon.rs.close();
//			throw new BusinessException(WeixinFinanceError.WEIXIN_SERIAL_DUPLICATED);
//		}
//		dbCon.rs.close();

		//Delete the previous bind relationship.
		sql = " DELETE FROM " + Params.dbName + ".weixin_restaurant " +
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
			throw new BusinessException(WeixinFinanceError.ACCOUNT_PWD_NOT_MATCH);
		}
		dbCon.rs.close();
		
		//Insert the binding between weixin serial and restaurant
		sql = " INSERT INTO " + Params.dbName + ".weixin_restaurant" +
			  "(`weixin_serial`, `restaurant_id`, `weixin_serial_crc`)" +
			  " VALUES(" +
			  "'" + weixinSerial + "'," +
			  restaurantId + "," +
			  "CRC32('" + weixinSerial + "')" +
			  ")";
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
		sql = " SELECT restaurant_id FROM " + Params.dbName + ".weixin_restaurant " +
			  " WHERE weixin_serial_crc = CRC32('" + weixinSerial + "')" +
			  " AND weixin_serial = '" + weixinSerial + "'";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int restaurantId = 0;
		if(dbCon.rs.next()){
			restaurantId = dbCon.rs.getInt("restaurant_id");
		}else{
			throw new BusinessException(WeixinFinanceError.WEIXIN_SERIAL_NOT_EXIST);
		}
		dbCon.rs.close();
		
		return restaurantId;
	}
	
}
