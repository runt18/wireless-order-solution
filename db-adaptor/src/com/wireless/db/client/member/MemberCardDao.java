package com.wireless.db.client.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.client.MemberCard;
import com.wireless.util.SQLUtil;

public class MemberCardDao {
	/**
	 * 
	 * @param dbCon
	 * @param mc
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int insertMemberCard(DBCon dbCon, MemberCard mc) throws SQLException, BusinessException{
		int count = 0;
		// 添加会员卡资料
		String insertSQL = "INSERT INTO " + Params.dbName + ".member_card (restaurant_id, member_card_alias, status, last_staff_id, last_mod_date, comment) "
				+ " VALUES("
				+ mc.getRestaurantID() + ","
				+ mc.getAliasID() + ","
				+ mc.getStatus().getValue() + ","
				+ mc.getLastStaffID() + ","
				+ "NOW(),"
				+ "'" + mc.getComment() + "'"
				+ " )";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	
	/**
	 * 
	 * @param mc
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int insertMemberCard(MemberCard mc) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.disconnect();
			count = MemberCardDao.insertMemberCard(dbCon, mc);
			if(count == 0){
				throw new BusinessException(MemberError.CARD_INSERT_FAIL);
			}
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param mc
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int deleteMemberCard(DBCon dbCon, MemberCard mc) throws SQLException{
		int count = 0;
		String deleteSQL = "DELETE FROM member_card WHERE member_card_id = " + mc.getId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
	}
	
	/**
	 * 
	 * @param mc
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int deleteMemberCard(MemberCard mc) throws SQLException, BusinessException{
		int count = 0;
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			count = MemberCardDao.deleteMemberCard(dbCon, mc);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateMemberCard(DBCon dbCon, MemberCard mc) throws SQLException, BusinessException{
		int count = 0;
		String updateSQL = "UPDATE " + Params.dbName + ".member_card A SET " 
				+ " A.last_mod_date = NOW(), A.status = " + mc.getStatus() 
				+ " ,A.comment = '" + mc.getComment() + "', "
				+ " A.last_staff_id = " + mc.getLastStaffID()
				+ " WHERE member_card_id = " + mc.getId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 */
	public static int updateMemberCard(MemberCard mc) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberCardDao.updateMemberCard(dbCon, mc);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberCard> getMemberCard(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<MemberCard> list = new ArrayList<MemberCard>();
		MemberCard item = null;
		String querySQL = "SELECT A.member_card_id, A.restaurant_id, A.member_card_alias, A.status, A.comment, A.last_staff_id, A.last_mod_date "
					+ " FROM " + Params.dbName + ".member_card A"
					+ " WHERE 1=1 ";
		
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new MemberCard();
			item.setId(dbCon.rs.getInt("member_card_id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setAliasID(dbCon.rs.getString("member_card_alias"));
			item.setStatus(dbCon.rs.getInt("status"));
			item.setComment(dbCon.rs.getString("comment"));
			item.setLastStaffID(dbCon.rs.getInt("last_staff_id"));
			item.setLastModDate(dbCon.rs.getTimestamp("last_mod_date").getTime());
			
			list.add(item);
			item = null;
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberCard> getMemberCard(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberCardDao.getMemberCard(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
}
