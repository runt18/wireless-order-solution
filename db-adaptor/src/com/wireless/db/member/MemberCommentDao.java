package com.wireless.db.member;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberComment;
import com.wireless.pojo.member.MemberComment.CommitBuilder;
import com.wireless.pojo.member.MemberComment.Type;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class MemberCommentDao {
	
	/**
	 * Get the public comments to a specific member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param member
	 * 			the public comments to this member 
	 * @return the public comments to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<MemberComment> getPublicCommentByMember(Staff staff, Member member) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPublicCommentByMember(dbCon, staff, member);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the public comments to a specific member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param member
	 * 			the public comments to this member 
	 * @return the public comments to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<MemberComment> getPublicCommentByMember(DBCon dbCon, Staff staff, Member member) throws SQLException{
		return getComments(dbCon, staff, "AND MC.member_id = " + member.getId() + " AND MC.type = " + Type.PUBLIC.getVal(), null);
	}
	
	/**
	 * Get the private comment to a specific member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param member
	 * 			the private comments to this member 
	 * @return the private comments to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static MemberComment getPrivateCommentByMember(Staff staff, Member member) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPrivateCommentByMember(dbCon, staff, member);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the private comment to a specific member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param member
	 * 			the private comments to this member 
	 * @return the private comments to this member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static MemberComment getPrivateCommentByMember(DBCon dbCon, Staff staff, Member member) throws SQLException{
		List<MemberComment> results = getComments(dbCon, staff, " AND MC.staff_id = " + staff.getId() + " AND MC.member_id = " + member.getId() + " AND MC.type = " + Type.PRIVATE.getVal(), null);
		if(results.isEmpty()){
			return MemberComment.newPrivateComment(staff, member, "");
		}else{
			return results.get(0);
		}
	}
	
	/**
	 * Get the comments according to a specific extra condition and order clause.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the comments result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static List<MemberComment> getComments(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{ 
		String sql;
		sql = " SELECT " +
			  " S.staff_id, S.name, MC.member_id, MC.type, MC.comment, MC.last_modified " +
			  " FROM " + Params.dbName + ".member_comment MC " +
			  " JOIN " + Params.dbName + ".staff S ON MC.staff_id = S.staff_id " +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond : "") + " " +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<MemberComment> comments = new ArrayList<MemberComment>();
		while(dbCon.rs.next()){
			if(MemberComment.Type.valueOf(dbCon.rs.getInt("type")) == MemberComment.Type.PUBLIC){
				MemberComment comment = MemberComment.newPublicComment(new Staff(dbCon.rs.getInt("staff_id"), dbCon.rs.getString("name")),
						  											   new Member(dbCon.rs.getInt("member_id")),
						  											   dbCon.rs.getString("comment"));
				comment.setLastModified(dbCon.rs.getTimestamp("last_modified").getTime());
				comments.add(comment);
				
			}else if(MemberComment.Type.valueOf(dbCon.rs.getInt("type")) == MemberComment.Type.PRIVATE){
				MemberComment comment = MemberComment.newPrivateComment(new Staff(dbCon.rs.getInt("staff_id")),
						   											    new Member(dbCon.rs.getInt("member_id")),
						   											    dbCon.rs.getString("comment"));
				comment.setLastModified(dbCon.rs.getTimestamp("last_modified").getTime());
				comments.add(comment);
			}
		}
		dbCon.rs.close();
		
		return Collections.unmodifiableList(comments);
	}
	
	/**
	 * Commit a comment.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the comment builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void commit(Staff staff, CommitBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			commit(dbCon, staff, builder);
			dbCon.conn.commit();
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Commit a comment.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the comment builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void commit(DBCon dbCon, Staff staff, CommitBuilder builder) throws SQLException{
		
		MemberComment comment = builder.build(); 
		
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".member_comment " +
			  " WHERE staff_id = " + comment.getStaff().getId() + 
			  " AND " + " member_id = " + comment.getMember().getId() +
			  " AND " + " type = " + comment.getType().getVal();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			sql = " UPDATE " + Params.dbName + ".member_comment SET " +
				  " comment = '" + comment.getComment() + "'" +
				  " ,last_modified = '" + DateUtil.format(comment.getLastModified(), DateUtil.Pattern.DATE_TIME) + "'" + 
				  " WHERE 1 = 1 " +
				  " AND staff_id = " + comment.getStaff().getId() +
				  " AND member_id = " + comment.getMember().getId() +
				  " AND type = " + comment.getType().getVal();
		}else{
			sql = " INSERT INTO " + Params.dbName + ".member_comment " +
				  " (`member_id`, `staff_id`, `type`, `comment`, `last_modified`) " +
				  " VALUES (" +
				  comment.getMember().getId() + "," +
				  comment.getStaff().getId() + "," +
				  comment.getType().getVal() + "," +
				  "'" + comment.getComment() + "'," +
				  "'" + DateUtil.format(comment.getLastModified(), DateUtil.Pattern.DATE_TIME) + "'" +
				  ")";
		}
		dbCon.rs.close();
		
		dbCon.stmt.executeUpdate(sql);
	}
	
}
