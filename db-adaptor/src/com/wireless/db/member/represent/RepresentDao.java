package com.wireless.db.member.represent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.oss.OssImageDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.member.represent.Represent;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;

public class RepresentDao {

	public static class ExtraCond{
		private int id;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND id = " + id);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the represent according to specific builder {@link Represent#InsertBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder to represent {@link Represent#InsertBuilder}.
	 * @return the id to represent just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, Represent.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the represent according to specific builder {@link Represent#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder to represent {@link Represent#InsertBuilder}.
	 * @return the id to represent just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, Represent.InsertBuilder builder) throws SQLException{
		Represent represent = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".represent" +
			  " (`restaurant_id`, `finish_date`, `title`, `slogon`, `oss_image_id`, `recommend_point`, `recommend_money`, `subscribe_point`, `subscribe_money`, `commission_rate`) VALUES ( " +
			  staff.getRestaurantId() + "," +
			  (represent.getFinishDate() != 0 ? "'" + DateUtil.format(represent.getFinishDate()) + "'" : " NULL ") + "," +
			  "'" + represent.getTitle() + "'," +
			  "'" + represent.getSlogon() + "'," +
			  (builder.hasImage() ? represent.getImage().getId() : " NULL ") + "," +
			  represent.getRecommendPoint() + "," +
			  represent.getRecommentMoney() + "," +
			  represent.getSubscribePoint() + "," +
			  represent.getSubscribeMoney() + "," +
			  represent.getComissionRate() +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int id = 0;
		if(dbCon.rs.next()){
			id = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The represent id is NOT generated successfully.");
		}
		dbCon.rs.close();
		
		return id;
	}

	/**
	 * Update the represent to builder {@link Represent#UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link Represent#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the represent to udpate does NOT exist
	 */
	public static void update(Staff staff, Represent.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the represent to builder {@link Represent#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder {@link Represent#UpdateBuilder}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the represent to udpate does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, Represent.UpdateBuilder builder) throws SQLException, BusinessException{
		Represent represent = builder.build();
		
		String sql;
		if(builder.isImageChanged()){
			//Delete the original oss image.
			sql = " SELECT oss_image_id FROM " + Params.dbName + ".represent WHERE id = " + represent.getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				if(dbCon.rs.getInt("oss_image_id") != 0){
					try{
						OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setId(dbCon.rs.getInt("oss_image_id")));
					}catch(SQLException | BusinessException ignored){
						ignored.printStackTrace();
					}
				}
			}
			dbCon.rs.close();
		}
		
		sql = " UPDATE " + Params.dbName + ".represent SET " +
			  " id = id " +
			  (builder.isImageChanged() ? " ,oss_image_id = " + represent.getImage().getId() : "") +
			  (builder.isFinishDateChanged() ? " ,finish_date = '" + DateUtil.format(represent.getFinishDate()) + "'" : "") +
			  (builder.isRecommendMoneyChanged() ? " ,recommend_money = " + represent.getRecommentMoney() : "") +
			  (builder.isRecommendPointChanged() ? " ,recommend_point = " + represent.getRecommendPoint() : "") +
			  (builder.isSlogonChanged() ? " ,slogon = '" + represent.getSlogon() + "'" : "") +
			  (builder.isSubcribePointChanged() ? " ,subscribe_point = " + represent.getSubscribePoint() : "") +
			  (builder.isSubscribeMoneyChanged() ? " ,subscribe_money = " + represent.getSubscribeMoney() : "") +
			  (builder.isTitleChanged() ? " ,title = '" + represent.getTitle() + "'" : "") +
			  (builder.isCommissionRateChanged() ? " ,commission_rate = " + represent.getComissionRate() : "") +
			  " WHERE id = " + represent.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("找到相应的【我要代言】");
		}
		

	}
	
	/**
	 * Get the represent to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to represent
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Represent> getByCond(Staff staff, ExtraCond extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the represent to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the result to represent
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Represent> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".represent " +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) + 
			  (extraCond != null ? extraCond.toString() : "");
		
		final List<Represent> result = new ArrayList<>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Represent represent = new Represent(dbCon.rs.getInt("id"));
			if(dbCon.rs.getTimestamp("finish_date") != null){
				represent.setFinishDate(dbCon.rs.getTimestamp("finish_date").getTime());
			}
			represent.setTitle(dbCon.rs.getString("title"));
			represent.setSlogon(dbCon.rs.getString("slogon"));
			represent.setRecommendPoint(dbCon.rs.getInt("recommend_point"));
			represent.setRecommentMoney(dbCon.rs.getFloat("recommend_money"));
			represent.setSubscribePoint(dbCon.rs.getInt("subscribe_point"));
			represent.setSubscribeMoney(dbCon.rs.getFloat("subscribe_money"));
			represent.setCommissionRate(dbCon.rs.getFloat("commission_rate"));
			if(dbCon.rs.getInt("oss_image_id") != 0){
				represent.setImage(new OssImage(dbCon.rs.getInt("oss_image_id")));
			}
			result.add(represent);
		}
		dbCon.rs.close();
		
		//Get the oss image to each represent.
		for(Represent represent : result){
			if(represent.hasImage()){
				try {
					represent.setImage(OssImageDao.getById(dbCon, staff, represent.getImage().getId()));
				} catch (BusinessException e) {
					e.printStackTrace();
					represent.setImage(null);
				}
			}
		}
		return result;
	}
	
	/**
	 * Delete the represent to extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to represent deleted
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
	 * Delete the represent to extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to represent deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(Represent represent : getByCond(dbCon, staff, extraCond)){
			if(represent.hasImage()){
				try {
					OssImageDao.delete(dbCon, staff, new OssImageDao.ExtraCond().setId(represent.getImage().getId()));
				} catch (BusinessException | SQLException ignored) {
					ignored.printStackTrace();
				}
			}
			String sql = " DELETE FROM " + Params.dbName + ".represent WHERE id = " + represent.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
	
}
