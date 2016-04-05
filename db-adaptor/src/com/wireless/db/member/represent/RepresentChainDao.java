package com.wireless.db.member.represent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.member.MemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.represent.RepresentChain;
import com.wireless.pojo.staffMgr.Staff;

public class RepresentChainDao {

	public static class ExtraCond{
		private int id;
		private int referrerId;
		private int subscriberId;
		private String recommendFuzzy;
		private String subscribeFuzzy;
		private Staff staff;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setReferrerId(int referrerId){
			this.referrerId = referrerId;
			return this;
		}
		
		public ExtraCond setSubscriberId(int subscriberId){
			this.subscriberId = subscriberId;
			return this;
		}
		
		public ExtraCond setRecommendFuzzy(String recommendFuzzy){
			this.recommendFuzzy = recommendFuzzy;
			return this;
		}
		
		public ExtraCond setSubscribeFuzzy(String subscribeFuzzy){
			this.subscribeFuzzy = subscribeFuzzy;
			return this;
		}
		
		ExtraCond setStaff(Staff staff){
			this.staff = staff;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			
			if(id != 0){
				extraCond.append(" AND id = " + id);
			}
			
			if(this.referrerId != 0){
				extraCond.append(" AND recommend_member_id = " + referrerId);
			}
			
			if(this.subscriberId != 0){
				extraCond.append(" AND subscribe_member_id = " + subscriberId);
			}
			
			if(this.recommendFuzzy != null){
				try {
					final StringBuilder recommends = new StringBuilder();
					for(Member recommend : MemberDao.getByCond(staff, new MemberDao.ExtraCond().setFuzzyName(recommendFuzzy), null)){
						if(recommends.length() != 0){
							recommends.append(",");
						}
						recommends.append(recommend.getId());
					}
					if(recommends.length() != 0){
						extraCond.append(" AND recommend_member_id IN ( " + recommends.toString() + " ) ");
					}
				} catch (SQLException | BusinessException ignored) {
					ignored.printStackTrace();
				}
			}
			
			if(this.subscribeFuzzy != null){
				try {
					final StringBuilder subscribes = new StringBuilder();
					for(Member subscribe : MemberDao.getByCond(staff, new MemberDao.ExtraCond().setFuzzyName(subscribeFuzzy), null)){
						if(subscribes.length() != 0){
							subscribes.append(",");
						}
						subscribes.append(subscribe.getId());
					}
					if(subscribes.length() != 0){
						extraCond.append(" AND recommend_member_id IN ( " + subscribes.toString() + " ) ");
					}
				} catch (SQLException | BusinessException ignored) {
					ignored.printStackTrace();
				}
			}
			return extraCond.toString();
		}
	}

	/**
	 * Get the represent chain to extra condition {@link ExtraCond}. 
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to represent chain
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<RepresentChain> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the represent chain to extra condition {@link ExtraCond}. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the result to represent chain
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<RepresentChain> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".represent_chain " +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  (extraCond != null ? extraCond.toString() : "");
		
		final List<RepresentChain> result = new ArrayList<>();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			RepresentChain chain = new RepresentChain(dbCon.rs.getInt("id"));
			chain.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			if(dbCon.rs.getTimestamp("subscribe_date") != null){
				chain.setSubscribeDate(dbCon.rs.getTimestamp("subscribe_date").getTime());
			}
			chain.setRecommendMemberId(dbCon.rs.getInt("recommend_member_id"));
			chain.setRecommendMember(dbCon.rs.getString("recommend_member"));
			chain.setRecommendPoint(dbCon.rs.getInt("recommend_point"));
			chain.setRecommendMoney(dbCon.rs.getFloat("recommend_money"));
			chain.setSubscribeMemberId(dbCon.rs.getInt("subscribe_member_id"));
			chain.setSubscribeMember(dbCon.rs.getString("subscribe_member"));
			chain.setSubscribePoint(dbCon.rs.getInt("subscribe_point"));
			chain.setSubscribeMoney(dbCon.rs.getFloat("subscribe_money"));
			result.add(chain);
		}
		dbCon.rs.close();
		
		return result;
	}

	/**
	 * Delete the represent chain to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to represent chain deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the represent chain to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to represent chain deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(RepresentChain chain : getByCond(dbCon, staff, extraCond)){
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".represent_chain WHERE id = " + chain.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0) {
				amount++;
			}
		}
		return amount;
	}
}
