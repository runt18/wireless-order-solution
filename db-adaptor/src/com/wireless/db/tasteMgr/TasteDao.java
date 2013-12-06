package com.wireless.db.tasteMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TasteError;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;

public class TasteDao {

	/**
	 * Get the tastes to the specified restaurant defined in {@link Staff} and category {@link Taste.Category}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param category
	 * 			the category
	 * @return the list holding the result to taste
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Taste> getTasteByCategory(DBCon dbCon, Staff staff, Taste.Category category) throws SQLException{
		return getTastes(dbCon, staff, " AND TASTE.category = " + category.getVal(), null);
	}
	
	/**
	 * Get the taste to the specified restaurant and taste id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff	
	 * 			the terminal
	 * @param tasteid
	 * 			the taste id
	 * @return the taste to specified restaurant and taste alias
	 * @throws BusinessException
	 * 			throws if the taste to query is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Taste getTasteById(DBCon dbCon, Staff staff, int tasteId) throws SQLException, BusinessException{
		List<Taste> tastes = TasteDao.getTastes(dbCon, staff, " AND taste_id = " + tasteId, null);
		if(tastes.isEmpty()){
			throw new BusinessException("The taste(taste_id = " + tasteId + ", restaurant_id = " + staff.getRestaurantId() + ") is NOT found.");
		}else{
			return tastes.get(0);
		}
	}
	
	/**
	 * Get the taste to the specified restaurant and taste id.
	 * @param staff	
	 * 			the terminal
	 * @param tasteid
	 * 			the taste id
	 * @return the taste to specified restaurant and taste alias
	 * @throws BusinessException
	 * 			throws if the taste to query is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Taste getTasteById(Staff staff, int tasteId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return TasteDao.getTasteById(dbCon, staff, tasteId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the tastes to the specified restaurant defined in {@link Staff} and other extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the result to taste
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Taste> getTastes(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql = " SELECT " +
				 	 " taste_id, restaurant_id, preference, " +
				 	 " category, calc, rate, price, type " +
				 	 " FROM " + 
				 	 Params.dbName + ".taste TASTE " +
				 	 " WHERE 1=1 " +
				 	 " AND TASTE.restaurant_id = " + staff.getRestaurantId() + " " +
				 	 (extraCond == null ? "" : extraCond) + " " +
				 	 (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Taste> tastes = new ArrayList<Taste>();
		while(dbCon.rs.next()){
			
			Taste taste = new Taste(dbCon.rs.getInt("taste_id"),
								    dbCon.rs.getInt("restaurant_id"));
			
			taste.setPreference(dbCon.rs.getString("preference"));
			taste.setCategory(dbCon.rs.getShort("category"));
			taste.setCalc(dbCon.rs.getShort("calc"));
			taste.setRate(dbCon.rs.getFloat("rate"));
			taste.setPrice(dbCon.rs.getFloat("price"));
			taste.setType(dbCon.rs.getShort("type"));
			
			tastes.add(taste);
		}
		dbCon.rs.close();
	
		return tastes;
	}
	
	/**
	 * 
	 * @param staff
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static List<Taste> getTastes(Staff staff, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return TasteDao.getTastes(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Insert a new taste according to a insert builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this operation
	 * @param builder
	 * 			the builder to taste inserted
	 * @return the id to taste just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, Taste.InsertBuilder builder) throws SQLException{
		
		Taste tasteToInsert = builder.build();
		
		String insertSQL = "INSERT INTO " + Params.dbName + ".taste"
						 + "( restaurant_id, preference, price, category, rate, calc, type )"
						 + "VALUES("
						 + tasteToInsert.getRestaurantId() + ","
						 + "'" + tasteToInsert.getPreference() + "',"
						 + tasteToInsert.getPrice() + ","
						 + tasteToInsert.getCategory().getVal() + ","
						 + tasteToInsert.getRate() + ","
						 + tasteToInsert.getCalc().getVal() + "," 
						 + tasteToInsert.getType().getVal() 
						 + ")";
		
		dbCon.stmt.executeUpdate(insertSQL, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of taste is not generated successfully.");
		}
	}
	
	/**
	 * Insert a new taste according to a insert builder.
	 * @param staff
	 * 			the staff to perform this operation
	 * @param builder
	 * 			the builder to taste inserted
	 * @return the id to taste just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, Taste.InsertBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return TasteDao.insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update a taste according to update builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this operation
	 * @param builder
	 * 			the update builder
	 * @throws BusinessException
	 * 			throws if the taste to update does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void update(DBCon dbCon, Staff staff, Taste.UpdateBuilder builder) throws BusinessException, SQLException{
		
		Taste tasteToUpdate = builder.build();
		
		String updateSQL = " UPDATE " + Params.dbName + ".taste SET "
						 + " preference = '" + tasteToUpdate.getPreference() + "',"
						 + " rate = " + tasteToUpdate.getRate() + ","
						 + " calc = " + tasteToUpdate.getCalc().getVal() + ","
						 + " price = " + tasteToUpdate.getPrice() + ","
						 + " category = " + tasteToUpdate.getCategory().getVal() 
						 + " WHERE restaurant_id = " + staff.getRestaurantId() 
						 + " AND taste_id = " + tasteToUpdate.getTasteId();
		
		if(dbCon.stmt.executeUpdate(updateSQL) == 0){
			throw new BusinessException(TasteError.UPDATE_FAIL);
		}
	}
	
	/**
	 * Update a taste according to update builder.
	 * @param staff
	 * 			the staff to perform this operation
	 * @param builder
	 * 			the update builder
	 * @throws BusinessException
	 * 			throws if the taste to update does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void update(Staff staff, Taste.UpdateBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			TasteDao.update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the taste to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this operation
	 * @param id
	 * 			the taste id to delete
	 * @throws BusinessException
	 * 			throws if the taste to delete does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void delete(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		String deleteSQL = "DELETE FROM " + Params.dbName + ".taste"
			+ " WHERE taste_id = " + id
			+ " AND restaurant_id = " + staff.getRestaurantId();
		if(dbCon.stmt.executeUpdate(deleteSQL) == 0){
			throw new BusinessException(TasteError.DELETE_FAIL);
		}
	}
	
	/**
	 * Delete the taste to a specific id.
	 * @param staff
	 * 			the staff to perform this operation
	 * @param id
	 * 			the taste id to delete
	 * @throws BusinessException
	 * 			throws if the taste to delete does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void delete(Staff staff, int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			TasteDao.delete(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
