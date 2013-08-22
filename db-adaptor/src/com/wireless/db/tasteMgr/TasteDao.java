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
	 * @param term
	 * 			the terminal
	 * @param category
	 * 			the category
	 * @return the list holding the result to taste
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Taste> getTasteByCategory(DBCon dbCon, Staff term, Taste.Category category) throws SQLException{
		return getTastes(dbCon, term, " AND TASTE.category = " + category.getVal(), null);
	}
	
	/**
	 * Get the taste to the specified restaurant and taste id.
	 * @param dbCon
	 * 			the database connection
	 * @param term	
	 * 			the terminal
	 * @param tasteid
	 * 			the taste id
	 * @return the taste to specified restaurant and taste alias
	 * @throws BusinessException
	 * 			throws if the taste to query is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Taste getTasteById(DBCon dbCon, Staff term, int tasteId) throws SQLException, BusinessException{
		List<Taste> tastes = TasteDao.getTastes(dbCon, term, " AND taste_id = " + tasteId, null);
		if(tastes.isEmpty()){
			throw new BusinessException("The taste(taste_id = " + tasteId + ", restaurant_id = " + term.getRestaurantId() + ") is NOT found.");
		}else{
			return tastes.get(0);
		}
	}
	
	/**
	 * Get the taste to the specified restaurant and taste id.
	 * @param term	
	 * 			the terminal
	 * @param tasteid
	 * 			the taste id
	 * @return the taste to specified restaurant and taste alias
	 * @throws BusinessException
	 * 			throws if the taste to query is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Taste getTasteById(Staff term, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return TasteDao.getTasteById(dbCon, term, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the tastes to the specified restaurant defined in {@link Staff} and other extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the result to taste
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Taste> getTastes(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		String sql = " SELECT " +
				 	 " taste_id, restaurant_id, preference, " +
				 	 " category, calc, rate, price, type " +
				 	 " FROM " + 
				 	 Params.dbName + ".taste TASTE " +
				 	 " WHERE 1=1 " +
				 	 " AND TASTE.restaurant_id = " + term.getRestaurantId() + " " +
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
	 * @param term
	 * @param extraCond
	 * @param orderClause
	 * @return
	 * @throws SQLException
	 */
	public static List<Taste> getTastes(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return TasteDao.getTastes(dbCon, term, extraCond, orderClause);
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
						 + "( restaurant_id, preference, price, category, rate, calc )"
						 + "VALUES("
						 + tasteToInsert.getRestaurantId() + ","
						 + "'" + tasteToInsert.getPreference() + "',"
						 + tasteToInsert.getPrice() + ","
						 + tasteToInsert.getCategory().getVal() + ","
						 + tasteToInsert.getRate() + ","
						 + tasteToInsert.getCalc().getVal()
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
	public static void update(Staff term, Taste.UpdateBuilder builder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			TasteDao.update(dbCon, term, builder);
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
	public static void delete(DBCon dbCon, Staff term, int id) throws SQLException, BusinessException{
		String deleteSQL = "DELETE FROM " + Params.dbName + ".taste"
			+ " WHERE taste_id = " + id
			+ " AND restaurant_id = " + term.getRestaurantId();
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
	public static void delete(Staff term, int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			TasteDao.delete(dbCon, term, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
