package com.wireless.db.weixin.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.db.weixin.restaurant.WeixinRestaurantDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.exception.WeixinMemberError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.WeixinMember;
import com.wireless.pojo.staffMgr.Staff;

public class WeixinMemberDao {
	
	public static class ExtraCond{
		private int card;
		private String weixinSerial;
		private int memberId;
		private WeixinMember.Status status;
		
		public ExtraCond setCard(int card){
			this.card = card;
			return this;
		}
		
		public ExtraCond setSerial(String serial){
			this.weixinSerial = serial;
			return this;
		}
		
		public ExtraCond setStatus(WeixinMember.Status status){
			this.status = status;
			return this;
		}
		
		public ExtraCond setMember(int memberId){
			this.memberId = memberId;
			return this;
		}
		
		public ExtraCond setMember(Member member){
			this.memberId = member.getId();
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(card != 0){
				extraCond.append(" AND weixin_card = " + card);
			}
			if(status != null){
				extraCond.append(" AND status = " + status.getVal());
			}
			if(weixinSerial != null){
				extraCond.append(" AND weixin_serial = '" + weixinSerial + "' AND weixin_serial_crc = CRC32('" + weixinSerial + "')");
			}
			if(memberId != 0){
				extraCond.append(" AND member_id = " + memberId);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the weixin member to specific weixin serial.
	 * @param staff
	 * 			the staff to perform this action
	 * @param serial
	 * 			the weixin serial
	 * @return the member id associated with this weixin serial 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>failed to insert the member related to weixin serial
	 */
	public static int interest(Staff staff, String serial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int memberId = interest(dbCon, staff, serial);
			dbCon.conn.commit();
			return memberId;
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the weixin member to specific weixin serial.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param serial
	 * 			the weixin serial
	 * @return the member id associated with this weixin serial 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>failed to insert the member related to weixin serial
	 */
	public static int interest(DBCon dbCon, Staff staff, String weixinSerial) throws SQLException, BusinessException{
		
		String sql;

		final int weixinCard;
		List<WeixinMember> weixinMembers = getByCond(dbCon, staff, new ExtraCond().setSerial(weixinSerial));

		if(weixinMembers.isEmpty()){
			//Insert the weixin member.
			sql = " INSERT INTO " + Params.dbName + ".weixin_member " +
				  " (`weixin_serial`, `weixin_serial_crc`, `restaurant_id`, `status`, `interest_date`) " +
				  " VALUES(" +
				  "'" + weixinSerial + "'," +
				  " CRC32('" + weixinSerial + "')," +
				  staff.getRestaurantId() + "," +
				  WeixinMember.Status.INTERESTED.getVal() + "," +
				  " NOW() " +
				  ")";
			
			dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			dbCon.rs = dbCon.stmt.getGeneratedKeys();
			if(dbCon.rs.next()){
				weixinCard = dbCon.rs.getInt(1);
			}else{
				throw new SQLException("The card of weixin member is not generated successfully.");
			}
			
		}else{
			weixinCard = weixinMembers.get(0).getCard();
		}
		
		List<Member> associatedMembers = MemberDao.getByCond(dbCon, staff, new MemberDao.ExtraCond().setWeixinSerial(weixinSerial), null);
		final int memberId;
		if(associatedMembers.isEmpty()){
			memberId = MemberDao.insert(dbCon, staff, new Member.InsertBuilder("微信会员", "", MemberTypeDao.getWeixinMemberType(dbCon, staff).getId()));
		}else{
			memberId = associatedMembers.get(0).getId();
		}

		//Clear the original member relationship. 
		sql = " UPDATE " + Params.dbName + ".weixin_member SET " +
			  " member_id = NULL " +
			  " WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		//Associated the member id with the weixin serial.
		sql = " UPDATE " + Params.dbName + ".weixin_member SET " + 
			  " member_id = " + memberId +
			  " ,status = " + WeixinMember.Status.BOUND.getVal() + 
			  " ,bind_date = NOW() " +
			  " WHERE weixin_card = " + weixinCard;
		dbCon.stmt.executeUpdate(sql);

		return memberId;
	}
	
	/**
	 * Bind the weixin according to specific builder {@link WeixinMember#BindBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the bind builder {@link WeixinMember#BindBuilder}
	 * @return the id to member binded
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the weixin member to bind does NOT exist
	 * 			<li>the member to this weixin does NOT exist
	 */
	public static int bind(Staff staff, WeixinMember.BindBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int memberId = bind(dbCon, staff, builder);
			dbCon.conn.commit();
			return memberId;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Bind the weixin according to specific builder {@link WeixinMember#BindBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the bind builder {@link WeixinMember#BindBuilder}
	 * @return the id to member binded
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the weixin member to bind does NOT exist
	 * 			<li>the member to this weixin does NOT exist
	 */
	public static int bind(DBCon dbCon, Staff staff, WeixinMember.BindBuilder builder) throws SQLException, BusinessException{
		
		final int memberId;
		final WeixinMember weixinMember = getBySerial(dbCon, staff, builder.getSerial());
		
		List<Member> membersToMobile = MemberDao.getByCond(dbCon, staff, new MemberDao.ExtraCond().setMobile(builder.getMobile()), null);
		if(membersToMobile.isEmpty()){
			//Check to see whether the member associated with this weixin serial exist.
			List<Member> membersToSerial = MemberDao.getByCond(dbCon, staff, new MemberDao.ExtraCond().setWeixinSerial(weixinMember.getSerial()), null);
			if(membersToSerial.isEmpty()){
				throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
			}
			//Update the mobile to the member associated with the weixin serial.
			memberId = membersToSerial.get(0).getId();
			MemberDao.update(dbCon, staff, new Member.UpdateBuilder(memberId).setMobile(builder.getMobile()));
			
		}else{
			//Delete the member associated with this weixin while interested.
			MemberDao.deleteByCond(dbCon, staff, new MemberDao.ExtraCond().setWeixinSerial(weixinMember.getSerial()));
			
			memberId = membersToMobile.get(0).getId();
			String sql;
			//Associated the original member owns this mobile with this weixin serial.  
			sql = " UPDATE " + Params.dbName + ".weixin_member SET " + 
				  " member_id = " + memberId +
				  " ,status = " + WeixinMember.Status.BOUND.getVal() + 
				  " ,bind_date = NOW() " +
				  " WHERE weixin_card = " + weixinMember.getCard();
			
			if(dbCon.stmt.executeUpdate(sql) == 0){
				throw new BusinessException(WeixinMemberError.WEIXIN_INFO_NOT_EXIST);
			}
		}

		return memberId;
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
//	public static void cancel(Staff staff, String weixinSerial) throws SQLException, BusinessException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			cancel(dbCon, staff, weixinSerial);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
	
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
//	public static void cancel(DBCon dbCon, Staff staff, String weixinSerial) throws SQLException, BusinessException{
//		String sql;
//		sql = " UPDATE " + Params.dbName + ".weixin_member" +
//			  " SET status = " + WeixinMember.Status.CANCELED.getVal() +
//			  " WHERE 1 = 1 " +
//			  " AND weixin_serial_crc = CRC32('" + weixinSerial + "')" +
//			  " AND weixin_serial = '" + weixinSerial + "'" +
//			  " AND restaurant_id = " + staff.getRestaurantId();
//		
//		if(dbCon.stmt.executeUpdate(sql) == 0){
//			throw new BusinessException(WeixinMemberError.WEIXIN_INFO_NOT_EXIST);
//		}
//	}
	
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
//	private static int getBoundMemberIdByWeixin(String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return getBoundMemberIdByWeixin(dbCon, weixinMemberSerial, weixinRestaurantSerial);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
	
	
	/**
	 * Get the weixin member to specific weixin serial.
	 * @param staff
	 * 			the staff to perform this action
	 * @param card
	 * 			the weixin card
	 * @return the weixin member to this card
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin member to this card does NOT exist
	 */
	public static WeixinMember getBySerial(Staff staff, String serial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getBySerial(dbCon, staff, serial);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the weixin member to specific weixin serial.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param card
	 * 			the weixin card
	 * @return the weixin member to this card
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin member to this card does NOT exist
	 */
	public static WeixinMember getBySerial(DBCon dbCon, Staff staff, String serial) throws SQLException, BusinessException{
		List<WeixinMember> result = getByCond(dbCon, staff, new ExtraCond().setSerial(serial));
		if(result.isEmpty()){
			throw new BusinessException(WeixinMemberError.WEIXIN_INFO_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the weixin member to specific card number.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param card
	 * 			the weixin card
	 * @return the weixin member to this card
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the weixin member to this card does NOT exist
	 */
	public static WeixinMember getByCard(DBCon dbCon, Staff staff, int card) throws SQLException, BusinessException{
		List<WeixinMember> result = getByCond(dbCon, staff, new ExtraCond().setCard(card));
		if(result.isEmpty()){
			throw new BusinessException(WeixinMemberError.WEIXIN_INFO_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the weixin member according to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to weixin members
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<WeixinMember> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".weixin_member " + 
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		
		List<WeixinMember> result = new ArrayList<WeixinMember>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			WeixinMember weixinMember = new WeixinMember(dbCon.rs.getInt("weixin_card"));
			weixinMember.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			weixinMember.setBindDate(dbCon.rs.getTimestamp("bind_date").getTime());
			weixinMember.setInterestedDate(dbCon.rs.getTimestamp("interest_date").getTime());
			weixinMember.setStatus(WeixinMember.Status.valueOf(dbCon.rs.getInt("status")));
			weixinMember.setWeixinMemberSerial(dbCon.rs.getString("weixin_serial"));
			result.add(weixinMember);
		}
		dbCon.rs.close();
		
		return result;
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
//	private static int getBoundMemberIdByWeixin(DBCon dbCon, String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
//		
//		int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, weixinRestaurantSerial);
//		
//		String sql;
//		sql = " SELECT member_id FROM " + Params.dbName + ".weixin_member " + 
//			  " WHERE 1 = 1 " +
//			  " AND weixin_serial_crc = CRC32('" + weixinMemberSerial + "')" +
//			  " AND weixin_serial = '" + weixinMemberSerial + "'" + 
//			  " AND restaurant_id = " + restaurantId +
//		      " AND status = " + WeixinMember.Status.BOUND.getVal();
//		
//		int memberId = 0;
//		try{
//			dbCon.rs = dbCon.stmt.executeQuery(sql);
//			
//			if(dbCon.rs.next()){
//				memberId = dbCon.rs.getInt("member_id");
//			}else{
//				throw new BusinessException(WeixinMemberError.WEIXIN_MEMBER_NOT_BOUND);
//			}
//			
//		}finally{
//			dbCon.rs.close();
//		}
//		
//		return memberId;
//	}

	private static int getCardByWeixin(DBCon dbCon, String weixinMemberSerial, String weixinRestaurantSerial) throws SQLException, BusinessException{
		
		int restaurantId = WeixinRestaurantDao.getRestaurantIdByWeixin(dbCon, weixinRestaurantSerial);
		
		String sql;
		sql = " SELECT weixin_card FROM " + Params.dbName + ".weixin_member " +
		      " WHERE 1 = 1 " +
			  " AND weixin_serial_crc = CRC32('" + weixinMemberSerial + "')" +
			  " AND weixin_serial = '" + weixinMemberSerial + "'" + 
			  " AND restaurant_id = " + restaurantId;
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int weixinCard = 0;
		if(dbCon.rs.next()){
			weixinCard = dbCon.rs.getInt("weixin_card");
		}else{
			throw new BusinessException(WeixinMemberError.WEIXIN_MEMBER_NOT_INTEREST);
		}
		dbCon.rs.close();
		
		return weixinCard;
	}
	
	/**
	 * Delete the weixin member to specific extra condition{@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to weixin member deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int amount = deleteByCond(dbCon, staff, extraCond);
			dbCon.conn.commit();
			return amount;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the weixin member to specific extra condition{@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to weixin member deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(WeixinMember wxMember : getByCond(dbCon, staff, extraCond)){
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".weixin_member WHERE weixin_card = " + wxMember.getCard();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
	
}
