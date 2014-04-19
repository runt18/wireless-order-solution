package com.wireless.db.weixin.member;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.WeixinMemberError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.staffMgr.Staff;

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
			  " AND weixin_serial = '" + weixinMemberSerial + "'" +
			  " AND restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
		
		sql = " INSERT INTO " + Params.dbName + ".weixin_member " +
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
	 * Bind the weixin serial to exist member.
	 * @param dbCon
	 * 			the database connection
	 * @param memberId
	 * 			the exist member to which the weixin serial bind
	 * @param weixinMemberSerial
	 * 			the serial of member to bind
	 * @param weixinRestaurantSerial
	 * 			the weixin restaurant serial
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin member has NOT been interested in restaurant
	 */
	public static void bindExistMember(int memberId, String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			bindExistMember(dbCon, memberId, weixinMemberSerial, weixinRestaurantSerial);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Bind the weixin serial to exist member.
	 * @param dbCon
	 * 			the database connection
	 * @param memberId
	 * 			the exist member id to which the weixin serial bind
	 * @param weixinMemberSerial
	 * 			the serial of member to bind
	 * @param weixinRestaurantSerial
	 * 			the weixin restaurant serial
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either cases below<br>
	 * 			<li>throws if the member to be bound does NOT exist
	 * 			<li>throws if the weixin member has NOT been interested in restaurant
	 */
	public static void bindExistMember(DBCon dbCon, int memberId, String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		
		int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, weixinRestaurantSerial);
		
		Member member = MemberDao.getById(dbCon, StaffDao.getStaffs(restaurantId).get(0), memberId);
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".weixin_member SET " +
			  " status = " + Status.BOUND.getVal() + "," +
			  " bind_date = NOW(), " +
			  " member_id = " + member.getId() +
			  " WHERE 1 = 1 " +
			  " AND weixin_serial_crc = CRC32('" + weixinMemberSerial + "')" +
			  " AND weixin_serial = '" + weixinMemberSerial + "'";
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(WeixinMemberError.WEIXIN_MEMBER_NOT_INTEREST);
		}
	}
	
	/**
	 * Bind the weixin to a new member. 
	 * @param builder
	 * 			the builder of new member to which the weixin bind
	 * @param weixinMemberSerial
	 * 			the weixin to bind
	 * @param weixinRestaurantSerial
	 * 			the weixin restaurant serial
	 * @return the id to member just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException	
	 * 			throws if failed to insert the new member<br>
	 * 			throws if the weixin serial has NOT been interested before
	 */
	public static int bindNewMember(Member.InsertBuilder builder, String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return bindNewMember(dbCon, builder, weixinMemberSerial, weixinRestaurantSerial);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Bind the weixin to a new member. 
	 * @param dbCon
	 * 			the database connectin
	 * @param builder
	 * 			the builder of new member to which the weixin bind
	 * @param weixinMemberSerial
	 * 			the weixin to bind
	 * @param weixinRestaurantSerial
	 * 			the weixin restaurant serial
	 * @return the id to member just inserted  
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException	
	 * 			throws if failed to insert the new member<br>
	 * 			throws if the weixin serial has NOT been interested before
	 */
	public static int bindNewMember(DBCon dbCon, Member.InsertBuilder builder, String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		
		int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, weixinRestaurantSerial);
		int memberId = MemberDao.insert(dbCon, StaffDao.getStaffs(dbCon, restaurantId).get(0), builder);
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".weixin_member SET " +
			  " status = " + Status.BOUND.getVal() + "," +
			  " bind_date = NOW(), " +
			  " member_id = " + memberId +
			  " WHERE 1 = 1 " +
			  " AND weixin_serial_crc = CRC32('" + weixinMemberSerial + "')" +
			  " AND weixin_serial = '" + weixinMemberSerial + "'";
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(WeixinMemberError.WEIXIN_MEMBER_NOT_INTEREST);
		}
		return memberId;
	}
	
	/**
	 * update the new mobile to weixin serial which has been bound
	 * @param mobile
	 * 			the mobile to update
	 * @param weixinMemberSerial
	 * 			the weixin serial to update mobile
	 * @param weixinRestaurantSerial
	 * 			the weixin restaurant serial
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin serial is NOT bound<br>
	 * 			throws if the weixin serial has NOT been interested before<br>
	 * 			throws if the mobile to update has been exist before
	 */
	public static void updateMobile(String mobile, String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			updateMobile(dbCon, mobile, weixinMemberSerial, weixinRestaurantSerial);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * update the new mobile to weixin serial which has been bound
	 * @param dbCon
	 * 			the database connection
	 * @param mobile
	 * 			the mobile to update
	 * @param weixinMemberSerial
	 * 			the weixin serial to update mobile
	 * @param weixinRestaurantSerial
	 * 			the weixin restaurant serial
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin serial is NOT bound<br>
	 * 			throws if the weixin serial has NOT been interested before<br>
	 * 			throws if the mobile to update has been exist before
	 */
	public static void updateMobile(DBCon dbCon, String mobile, String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		int memberId = getBoundMemberIdByWeixin(dbCon, weixinMemberSerial, weixinRestaurantSerial);
		int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, weixinRestaurantSerial);
		Staff staff = StaffDao.getStaffs(dbCon, restaurantId).get(0);
		Member member = MemberDao.getById(dbCon, staff, memberId);
		member.setMobile(mobile);
		MemberDao.checkValid(dbCon, member);
		String updateSQL = " UPDATE " + Params.dbName + ".member SET mobile = " + "'" + member.getMobile() + "' WHERE member_id = " + member.getId(); 
		dbCon.stmt.executeUpdate(updateSQL);
//		MemberDao.update(dbCon, staff, new Member.UpdateBuilder(memberId, restaurantId).setMobile(mobile));
	}
	
	/**
	 * Cancel the weixin member.
	 * @param weixinMemberSerial
	 * 			the serial of weixin member to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the corresponding weixin restaurant does NOT exist 
	 */
	public static void cancel(String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			cancel(dbCon, weixinMemberSerial, weixinRestaurantSerial);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel the weixin member.
	 * @param dbCon
	 * 			the database connection
	 * @param weixinMemberSerial
	 * 			the serial of weixin member to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the corresponding weixin restaurant does NOT exist 
	 */			
	public static void cancel(DBCon dbCon, String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, weixinRestaurantSerial);
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".weixin_member WHERE 1 = 1 " +
			  " AND weixin_serial_crc = CRC32('" + weixinMemberSerial + "')" +
			  " AND weixin_serial = '" + weixinMemberSerial + "'" +
			  " AND restaurant_id = " + restaurantId;
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Get the bound member id according to the weixin member serial.
	 * @param weixinMemberSerial
	 * 			the wexin serial to member
	 * @param weixinRestaurantSerial
	 * 			the weixin serial to restaurant
	 * @return the member id this weixin member serial bound to
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the bound member id is NOT found 
	 */
	public static int getBoundMemberIdByWeixin(String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getBoundMemberIdByWeixin(dbCon, weixinMemberSerial, weixinRestaurantSerial);
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
	 * @param weixinRestaurantSerial
	 * 			the weixin serial to restaurant
	 * @return the member id this weixin member serial bound to
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the bound member id is NOT found 
	 */
	public static int getBoundMemberIdByWeixin(DBCon dbCon, String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		
		int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, weixinRestaurantSerial);
		
		String sql;
		sql = " SELECT member_id FROM " + Params.dbName + ".weixin_member " + 
			  " WHERE 1 = 1 " +
			  " AND weixin_serial_crc = CRC32('" + weixinMemberSerial + "')" +
			  " AND weixin_serial = '" + weixinMemberSerial + "'" + 
			  " AND restaurant_id = " + restaurantId +
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
