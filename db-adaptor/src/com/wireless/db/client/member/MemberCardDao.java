package com.wireless.db.client.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
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
	 * @throws Exception
	 */
	public static int insertMemberCard(DBCon dbCon, MemberCard mc) throws SQLException, BusinessException, Exception{
		int count = 0;
		// 添加会员卡资料并绑定会员
		String insertSQL = "INSERT INTO " + Params.dbName + ".member_card (restaurant_id, member_card_alias, status, last_staff_id, last_mod_date, comment) "
				+ " VALUES("
				+ mc.getRestaurantID() + ","
				+ mc.getAliasID() + ","
				+ mc.getStatus() + ","
				+ mc.getLastStaffID() + ","
				+ "NOW(),"
				+ "'" + mc.getComment() + "'"
				+ " )";
		count = dbCon.stmt.executeUpdate(insertSQL);
		if(count == 0){
			throw new BusinessException("操作失败, 会员卡资料新建失败, 未知错误.", 9971);
		}
		return count;
	}
	
	/**
	 * 
	 * @param mc
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws Exception
	 */
	public static int insertMemberCard(MemberCard mc) throws SQLException, BusinessException, Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.disconnect();
			count = MemberCardDao.insertMemberCard(dbCon, mc);
		}catch(Exception e){
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
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws Exception
	 */
	public static int updateMemberCard(DBCon dbCon, Map<Object, Object> params) throws SQLException, BusinessException, Exception{
		int count = 0;
		Object mcObject = params.get(MemberCard.class);
		if(mcObject == null){
			throw new BusinessException("操作失败, 没有指定会员卡信息.");
		}
		MemberCard mc = (MemberCard) mcObject;
		String updateSQL = "UPDATE " + Params.dbName + ".member_card A SET " 
				+ " A.last_mod_date = NOW(), A.status = " + mc.getStatus() 
				+ " ,A.comment = '" + mc.getComment() + "', "
				+ " A.last_staff_id = " + mc.getLastStaffID()
				+ " WHERE 1=1 ";
		updateSQL = SQLUtil.bindSQLParams(updateSQL, params);
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws BusinessException
	 * @throws Exception
	 */
	public static int updateMemberCard(Map<Object, Object> params) throws SQLException, BusinessException, Exception{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberCardDao.updateMemberCard(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 获取会员卡信息
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public static List<MemberCard> getMemberCard(DBCon dbCon, Map<Object, Object> params) throws SQLException, Exception{
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
	 * 获取会员卡信息
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws Exception
	 */
	public static List<MemberCard> getMemberCard(Map<Object, Object> params) throws SQLException, Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberCardDao.getMemberCard(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
}
