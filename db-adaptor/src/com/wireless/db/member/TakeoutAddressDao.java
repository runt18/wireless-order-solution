package com.wireless.db.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.TakeoutAddress;
import com.wireless.pojo.staffMgr.Staff;

public class TakeoutAddressDao {

	public static class ExtraCond{
		private int id;
		private int memberId;
		
		public ExtraCond setId(int id){
			this.id = id;
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
			if(id != 0){
				extraCond.append(" AND id = " + id);
			}
			if(memberId != 0){
				extraCond.append(" AND member_id = " + memberId);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert a new take out address according to builder {@link TakeoutAddress#InsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder {@link TakeoutAddress#InsertBuilder}
	 * @return the id to take out address just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, TakeoutAddress.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new take out address according to builder {@link TakeoutAddress#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder {@link TakeoutAddress#InsertBuilder}
	 * @return the id to take out address just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, TakeoutAddress.InsertBuilder builder) throws SQLException{
		TakeoutAddress address = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".take_out_address" +
			  " (member_id, address, tele, name) VALUES( " +
			  address.getMemberId() + "," +
			  "'" + address.getAddress() + "'," +
			  "'" + address.getTele() + "'," +
			  "'" + address.getName() + "'" +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int id;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id to take out address is NOT generated successfully.");
		}
		dbCon.rs.close();
		return id;
	}
	
	/**
	 * Get the take out address to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the address id
	 * @return the take out address to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statment
	 * @throws BusinessException
	 * 			throws if the take out address to this id does NOT exist
	 */
	public static TakeoutAddress getById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the take out address to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the address id
	 * @return the take out address to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statment
	 * @throws BusinessException
	 * 			throws if the take out address to this id does NOT exist
	 */
	public static TakeoutAddress getById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		List<TakeoutAddress> result = getByCond(dbCon, staff, new ExtraCond().setId(id));
		if(result.isEmpty()){
			throw new BusinessException(MemberError.TAKE_OUT_ADDRESS_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the take-out address to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the take-out addresses to this extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<TakeoutAddress> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".take_out_address " +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond.toString() : "") +
			  " ORDER BY last_used DESC ";
		
		final List<TakeoutAddress> result = new ArrayList<>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			TakeoutAddress address = new TakeoutAddress(dbCon.rs.getInt("id"));
			address.setAddress(dbCon.rs.getString("address"));
			address.setTele(dbCon.rs.getString("tele"));
			address.setMemberId(dbCon.rs.getInt("member_id"));
			address.setName(dbCon.rs.getString("name"));
			if(dbCon.rs.getTimestamp("last_used") != null){
				address.setLastUsed(dbCon.rs.getTimestamp("last_used").getTime());
			}
			result.add(address);
		}
		dbCon.rs.close();
		return result;
	}
	
	/**
	 * Get the take-out address to extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the take-out addresses to this extra condition
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<TakeoutAddress> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the take-out address to specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the take-out address to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the take-out address to delete does NOT exist
	 */
	public static void deleteById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the take-out address to specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the take-out address to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the take-out address to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(MemberError.TAKE_OUT_ADDRESS_NOT_EXIST);
		}
	}
	
	/**
	 * Delete the take-out address to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount of take-out address to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(TakeoutAddress address : getByCond(dbCon, staff, extraCond)){
			String sql = " DELETE FROM " + Params.dbName + ".take_out_address WHERE id = " + address.getId();
			dbCon.stmt.executeUpdate(sql);
			amount++;
		}
		return amount;
	}
}
