package com.wireless.db.tasteMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TasteError;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.TasteCategory;

public class TasteCategoryDao {

	/**
	 * Swap the display id between two taste category.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the swap display builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the taste category to swap does NOT exist
	 */
	public static void swap(Staff staff, TasteCategory.SwapDisplayBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			swap(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Swap the display id between two taste category.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the swap display builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the taste category to swap does NOT exist
	 */
	public static void swap(DBCon dbCon, Staff staff, TasteCategory.SwapDisplayBuilder builder) throws SQLException, BusinessException{
		TasteCategory tcA = getById(dbCon, staff, builder.getIdA());
		TasteCategory tcB = getById(dbCon, staff, builder.getIdB());
		
		String sql;
		
		sql = " UPDATE " + Params.dbName + ".taste_category SET " +
			  " display_id = " + tcB.getDisplayId() +
			  " WHERE category_id = " + tcA.getId();
		dbCon.stmt.executeUpdate(sql);
		
		sql = " UPDATE " + Params.dbName + ".taste_category SET " +
			  " display_id = " + tcA.getDisplayId() +
			  " WHERE category_id = " + tcB.getId();
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Insert a new taste category.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder 
	 * @return the id to taste category just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, TasteCategory.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new taste category.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder 
	 * @return the id to taste category just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, TasteCategory.InsertBuilder builder) throws SQLException{
		TasteCategory tasteCategory = builder.build();
		String sql;
		
		//Calculate the display id.
		sql = " SELECT IFNULL(MAX(display_id), 0) + 1 FROM " + Params.dbName + ".taste_category WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int displayId = 0;
		if(dbCon.rs.next()){
			displayId = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		//Insert a new taste category.
		sql = " INSERT INTO " + Params.dbName + ".taste_category" +
			  "(`restaurant_id`, `name`, `type`, `status`, `display_id`) VALUES (" +
			  staff.getRestaurantId() + "," +
			  "'" + tasteCategory.getName() + "'," +
			  tasteCategory.getType().getVal() + "," +
			  tasteCategory.getStatus().getVal() + ", " +
			  displayId +
			  " ) ";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of taste category is not generated successfully.");
		}
	}
	
	/**
	 * Update a taste category according to specific builder.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the taste category to update does NOT exist
	 */
	public static void update(Staff staff, TasteCategory.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update a taste category according to specific builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the taste category to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, TasteCategory.UpdateBuilder builder) throws SQLException, BusinessException{
		TasteCategory tasteCategory = builder.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".taste_category SET " +
			  " name = '" + tasteCategory.getName() + "'" +
			  " WHERE category_id = " + tasteCategory.getId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(TasteError.TASTE_CATE_NOT_EXIST);
		}
	}

	/**
	 * Delete a taste category according to a specific category id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param categoryId
	 * 			the category id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the category to delete does NOT exist 
	 */
	public static void delete(Staff staff, int categoryId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			delete(dbCon, staff, categoryId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete a taste category according to a specific category id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param categoryId
	 * 			the category id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the category to delete does NOT exist 
	 * 			throws if the taste to this category has been exist
	 */
	public static void delete(DBCon dbCon, Staff staff, int categoryId) throws SQLException, BusinessException{
		String sql;
		
		//Check to see whether the taste to this category does exist
		if(TasteDao.getTasteAmountByCategory(dbCon, staff, categoryId) > 0){
			throw new BusinessException(TasteError.TASTE_NOT_CLEAN_UP);
		}
		
		sql = " DELETE FROM " + Params.dbName + ".taste_category WHERE category_id = " + categoryId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(TasteError.TASTE_CATE_NOT_EXIST);
		}
	}
	
	/**
	 * Get the taste category to a specific restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the taste category to specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<TasteCategory> get(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, null, null);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the taste category to a specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the taste category to specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<TasteCategory> get(DBCon dbCon, Staff staff) throws SQLException{
		return getByCond(dbCon, staff, null, null);
	}
	
	/**
	 * Get the taste category to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param categoryId
	 * 			the specific taste category id  
	 * @return the taste category 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the taste category to this id does NOT exist
	 */
	public static TasteCategory getById(DBCon dbCon, Staff staff, int categoryId) throws SQLException, BusinessException{
		List<TasteCategory> result = getByCond(dbCon, staff, "AND category_id = " + categoryId, null);
		if(result.isEmpty()){
			throw new BusinessException(TasteError.TASTE_CATE_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the taste category to a specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param categoryId
	 * 			the specific taste category id  
	 * @return the taste category 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the taste category to this id does NOT exist
	 */
	public static TasteCategory getById(Staff staff, int categoryId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, categoryId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	private static List<TasteCategory> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".taste_category " +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() + " " +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : " ORDER BY display_id ");
		
		List<TasteCategory> result = new ArrayList<TasteCategory>();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			TasteCategory tc = new TasteCategory(dbCon.rs.getInt("category_id"));
			tc.setName(dbCon.rs.getString("name"));
			tc.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			tc.setType(TasteCategory.Type.valueOf(dbCon.rs.getInt("type")));
			tc.setStatus(TasteCategory.Status.valueOf(dbCon.rs.getInt("status")));
			tc.setDisplayId(dbCon.rs.getInt("display_id"));
			result.add(tc);
		}
		dbCon.rs.close();
		
		return Collections.unmodifiableList(result);
	}
}
