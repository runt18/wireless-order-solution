package com.wireless.db.weixin.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.exception.WxMemberError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.WxMember;
import com.wireless.pojo.staffMgr.Staff;

public class WxMemberDao {
	
	public static class ExtraCond{
		private int card;
		private String weixinSerial;
		private int memberId;
		private WxMember.Status status;
		
		public ExtraCond setCard(int card){
			this.card = card;
			return this;
		}
		
		public ExtraCond setSerial(String serial){
			this.weixinSerial = serial;
			return this;
		}
		
		public ExtraCond setStatus(WxMember.Status status){
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
		List<WxMember> weixinMembers = getByCond(dbCon, staff, new ExtraCond().setSerial(weixinSerial));

		if(weixinMembers.isEmpty()){
			//Insert the weixin member.
			sql = " INSERT INTO " + Params.dbName + ".weixin_member " +
				  " (`weixin_serial`, `weixin_serial_crc`, `restaurant_id`, `status`, `interest_date`) " +
				  " VALUES(" +
				  "'" + weixinSerial + "'," +
				  " CRC32('" + weixinSerial + "')," +
				  staff.getRestaurantId() + "," +
				  WxMember.Status.INTERESTED.getVal() + "," +
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
			memberId = MemberDao.insert(dbCon, staff, Member.InsertBuilder.build4Weixin("微信会员", MemberTypeDao.getWxMemberType(dbCon, staff)));
		}else{
			memberId = associatedMembers.get(0).getId();
		}

		//Clear the original member relationship. 
		sql = " UPDATE " + Params.dbName + ".weixin_member SET " +
			  " member_id = 0 " +
			  " ,status = " + WxMember.Status.INTERESTED.getVal() +
			  " WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		//Associated the member id with the weixin serial.
		sql = " UPDATE " + Params.dbName + ".weixin_member SET " + 
			  " member_id = " + memberId +
			  " ,status = " + WxMember.Status.BOUND.getVal() + 
			  " ,bind_date = NOW() " +
			  " WHERE weixin_card = " + weixinCard;
		dbCon.stmt.executeUpdate(sql);

		return memberId;
	}
	
	/**
	 * Bind the weixin according to specific builder {@link WxMember#BindBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the bind builder {@link WxMember#BindBuilder}
	 * @return the id to member binded
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the weixin member to bind does NOT exist
	 * 			<li>the member to this weixin does NOT exist
	 */
	public static int bind(Staff staff, WxMember.BindBuilder builder) throws SQLException, BusinessException{
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
	 * Bind the weixin according to specific builder {@link WxMember#BindBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the bind builder {@link WxMember#BindBuilder}
	 * @return the id to member binded
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the weixin member to bind does NOT exist
	 * 			<li>the member to this weixin does NOT exist
	 */
	public static int bind(DBCon dbCon, Staff staff, WxMember.BindBuilder builder) throws SQLException, BusinessException{
		
		final int memberId;
		final WxMember weixinMember = getBySerial(dbCon, staff, builder.getSerial());
		
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
				  " ,status = " + WxMember.Status.BOUND.getVal() + 
				  " ,bind_date = NOW() " +
				  " WHERE weixin_card = " + weixinMember.getCard();
			
			if(dbCon.stmt.executeUpdate(sql) == 0){
				throw new BusinessException(WxMemberError.WEIXIN_INFO_NOT_EXIST);
			}
		}

		return memberId;
	}
	
	
	
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
	public static WxMember getBySerial(Staff staff, String serial) throws SQLException, BusinessException{
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
	public static WxMember getBySerial(DBCon dbCon, Staff staff, String serial) throws SQLException, BusinessException{
		List<WxMember> result = getByCond(dbCon, staff, new ExtraCond().setSerial(serial));
		if(result.isEmpty()){
			throw new BusinessException(WxMemberError.WEIXIN_INFO_NOT_EXIST);
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
	public static WxMember getByCard(DBCon dbCon, Staff staff, int card) throws SQLException, BusinessException{
		List<WxMember> result = getByCond(dbCon, staff, new ExtraCond().setCard(card));
		if(result.isEmpty()){
			throw new BusinessException(WxMemberError.WEIXIN_INFO_NOT_EXIST);
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
	public static List<WxMember> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".weixin_member " + 
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond.toString() : "");
		
		List<WxMember> result = new ArrayList<WxMember>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			WxMember weixinMember = new WxMember(dbCon.rs.getInt("weixin_card"));
			weixinMember.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			if(dbCon.rs.getTimestamp("bind_date") != null){
				weixinMember.setBindDate(dbCon.rs.getTimestamp("bind_date").getTime());
			}
			if(dbCon.rs.getTimestamp("interest_date") != null){
				weixinMember.setInterestedDate(dbCon.rs.getTimestamp("interest_date").getTime());
			}
			weixinMember.setStatus(WxMember.Status.valueOf(dbCon.rs.getInt("status")));
			weixinMember.setWeixinMemberSerial(dbCon.rs.getString("weixin_serial"));
			result.add(weixinMember);
		}
		dbCon.rs.close();
		
		return result;
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
		for(WxMember wxMember : getByCond(dbCon, staff, extraCond)){
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".weixin_member WHERE weixin_card = " + wxMember.getCard();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
	
}
