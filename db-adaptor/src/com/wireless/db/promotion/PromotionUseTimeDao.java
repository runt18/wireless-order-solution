package com.wireless.db.promotion;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.promotion.PromotionUseTime;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class PromotionUseTimeDao{
	public static class ExtraCond{
		private int promotionId;
		
		public ExtraCond setPromotionId(int promotionId){
			this.promotionId = promotionId;
			return this;
		}
		
		public ExtraCond setPromotionId(Promotion promotion){
			this.promotionId = promotion.getId();
			return this;
		}
		
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			
			if(this.promotionId != 0){
				extraCond.append(" AND promotion_id = " + this.promotionId);
			}
			
			return extraCond.toString();
		}
	}
	
	public static List<PromotionUseTime> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static List<PromotionUseTime> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".promotion_use_time P " +
			  " WHERE 1 = 1 " + 
			  " AND restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) + 
			  (extraCond != null ? extraCond.toString() : "");
		
		final List<PromotionUseTime> result = new ArrayList<>();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			PromotionUseTime useTime = new PromotionUseTime();
			useTime.setPromotionId(dbCon.rs.getInt("promotion_id"));
//			useTime.setStart(dbCon.rs.getString("start"));
			useTime.setStart(dbCon.rs.getTimestamp("start").getTime());
			useTime.setEnd(dbCon.rs.getTimestamp("end").getTime());
			useTime.setWeek(PromotionUseTime.Week.valueOf(dbCon.rs.getInt("week")));
			result.add(useTime);
		}
		
		return result;
	}
	
	
	
	public static int insert(Staff staff, PromotionUseTime.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}

	public static int insert(DBCon dbCon, Staff staff, PromotionUseTime.InsertBuilder builder) throws SQLException { 
		PromotionUseTime promotionUseTime = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".promotion_use_time " +
			  " (restaurant_id, promotion_id, week, start, end) " +
			  " VALUES ( " +
			  staff.getRestaurantId() + "," +
			  promotionUseTime.getPromotionId() + "," +
			  promotionUseTime.getWeek().getVal() + "," +
			  "'" + DateUtil.format(promotionUseTime.getStart(), DateUtil.Pattern.HOUR) + "'," +
			  "'" + DateUtil.format(promotionUseTime.getEnd(), DateUtil.Pattern.HOUR) + "'" + 
			  " ) ";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		
		int promotionUseTimeId;
		if(dbCon.rs.next()){
			promotionUseTimeId = dbCon.rs.getInt(1);
		}else{
			throw new  SQLException("Failed to generated the promotionUseTime id");
		}
		return promotionUseTimeId;
	}
	
	public static void delById(Staff staff, int promotionId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			delById(dbCon, staff, promotionId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	public static void delById(DBCon dbCon, Staff staff, int promotionId) throws SQLException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".promotion_use_time WHERE promotion_id = " + promotionId;
		dbCon.stmt.executeUpdate(sql);
	}

}
