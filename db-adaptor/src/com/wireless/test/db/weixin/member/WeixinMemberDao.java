package com.wireless.test.db.weixin.member;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WeixinMemberError;

public class WeixinMemberDao {
	
	public static enum Status{
		INTERESTED(1, "已关注"),
		BOUND(2, "已绑定"),
		CANCELED(3, "取消关注");
		
		private final int val;
		private final String desc;
		
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return "Status(val=" + val + ",desc=" + desc + ")";
		}
	}
	
	/**
	 * Make the weixin member interested in weixin restaurant.
	 * @param weixinRestaurantSerial
	 * 			the weixin restaurant to be interested
	 * @param weixinMemberSerial
	 * 			the weixin member to interest
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin restaurant is invalid
	 */
	public static void interest(String weixinRestaurantSerial, String weixinMemberSerial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			interest(dbCon, weixinRestaurantSerial, weixinMemberSerial);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Make the weixin member interested in weixin restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinRestaurantSerial
	 * 			the weixin restaurant to be interested
	 * @param weixinMemberSerial
	 * 			the weixin member to interest
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin restaurant is invalid
	 */
	public static void interest(DBCon dbCon, String weixinRestaurantSerial, String weixinMemberSerial) throws SQLException, BusinessException{
		
		int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, weixinRestaurantSerial);
		
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".weixin_member " +
			  " WHERE 1 = 1 " +
			  " AND weixin_serial_crc = CRC32('" + weixinMemberSerial + "')" +
			  " AND weixin_serial = '" + weixinMemberSerial + "'";
		dbCon.stmt.executeUpdate(sql);
		
		sql = " INSERT INTO " + Params.dbName + ".winxin_member " +
			  " (`weixin_serial`, `weixin_serial_crc`, `restaurant_id`, `status`, `interest_date`) " +
			  " VALUES(" +
			  "'" + weixinMemberSerial + "'," +
			  "CRC32('" + weixinMemberSerial + "')," +
			  restaurantId + "," +
			  Status.INTERESTED.getVal() + "," +
			  "NOW()" +
			  ")";
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Get the bound member id according to the weixin member serial.
	 * @param weixinMemberSerial
	 * 			the wexin serial to member
	 * @return the bound member id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the bound member id is NOT found 
	 */
	public static int getBoundMemberIdByWeixin(String weixinMemberSerial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getBoundMemberIdByWeixin(dbCon, weixinMemberSerial);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the bound member id according to the weixin member serial.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinMemberSerial
	 * 			the wexin serial to member
	 * @return the bound member id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the bound member id is NOT found 
	 */
	public static int getBoundMemberIdByWeixin(DBCon dbCon, String weixinMemberSerial) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT member_id FROM " + Params.dbName + ".weixin_member " + 
			  " WHERE 1 = 1 " +
			  " AND weixin_serial_crc = CRC32('" + weixinMemberSerial + "')" +
			  " AND weixin_serial = '" + weixinMemberSerial + "'" + 
		      " AND status = " + Status.BOUND.getVal();
		
		int memberId = 0;
		try{
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			if(dbCon.rs.next()){
				memberId = dbCon.rs.getInt("member_id");
			}else{
				throw new BusinessException(WeixinMemberError.WEIXIN_MEMBER_NOT_BOUND);
			}
			
		}finally{
			dbCon.rs.close();
		}
		
		return memberId;
	}
	
}
