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
import com.wireless.pojo.tasteMgr.TasteCategory;

public class TasteDao {

	public static class ExtraCond{
		private int id;
		private int categoryId;
		private String preference;
		private float price;
		private String operator4Price;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setCategory(int categoryId){
			this.categoryId = categoryId;
			return this;
		}
		
		public ExtraCond setPreference(String pref){
			this.preference = pref;
			return this;
		}
		
		public ExtraCond setPrice(String operator, float price){
			this.operator4Price = operator;
			this.price = price;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND TASTE.taste_id = " + id);
			}
			if(categoryId != 0){
				extraCond.append(" AND TASTE.category_id = " + categoryId);
			}
			if(preference != null){
				extraCond.append(" AND TASTE.preference like '%" + preference + "%'");
			}
			if(operator4Price != null){
				extraCond.append(" AND TASTE.price " + operator4Price + " " + price);
			}
			return extraCond.toString();
		}
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
	public static Taste getById(DBCon dbCon, Staff staff, int tasteId) throws SQLException, BusinessException{
		List<Taste> tastes = TasteDao.getByCond(dbCon, staff, new ExtraCond().setId(tasteId), null);
		if(tastes.isEmpty()){
			throw new BusinessException(TasteError.TASTE_NOT_EXIST);
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
	public static Taste getById(Staff staff, int tasteId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return TasteDao.getById(dbCon, staff, tasteId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the tastes to the specified restaurant defined in {@link Staff} and other extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the result to taste
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the category to any taste does NOT exist 
	 */
	public static List<Taste> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		String sql = " SELECT " +
				 	 " TASTE.taste_id, TASTE.restaurant_id, TASTE.preference, " +
				 	 " TASTE.category_id, TASTE.calc, TASTE.rate, TASTE.price, TASTE.type, TCATE.name " +
				 	 " FROM " + 
				 	 Params.dbName + ".taste TASTE " +
				 	 " JOIN " + Params.dbName + ".taste_category TCATE ON TCATE.category_id = TASTE.category_id " +
				 	 " WHERE 1=1 " +
				 	 " AND TASTE.restaurant_id = " + staff.getRestaurantId() + " " +
				 	 (extraCond == null ? "" : extraCond) + " " +
				 	 (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Taste> tastes = new ArrayList<Taste>();
		while(dbCon.rs.next()){
			Taste taste = new Taste(dbCon.rs.getInt("taste_id"));
			TasteCategory tc = new TasteCategory(dbCon.rs.getInt("category_id"));
			tc.setName(dbCon.rs.getString("name"));
			taste.setCategory(tc);
			taste.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			taste.setPreference(dbCon.rs.getString("preference"));
			taste.setCalc(dbCon.rs.getShort("calc"));
			taste.setRate(dbCon.rs.getFloat("rate"));
			taste.setPrice(dbCon.rs.getFloat("price"));
			taste.setType(dbCon.rs.getShort("type"));
			
			tastes.add(taste);
		}
		dbCon.rs.close();
	
		for(Taste t : tastes){
			t.getCategory().copyFrom(TasteCategoryDao.getById(dbCon, staff, t.getCategory().getId()));
		}
		
		return tastes;
	}
	
	/**
	 * Get the tastes to the specified restaurant defined in {@link Staff} and other extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the result to taste
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the category to any taste does NOT exist 
	 */
	public static List<Taste> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return TasteDao.getByCond(dbCon, staff, extraCond, orderClause);
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
						 + "( restaurant_id, preference, price, category_id, rate, calc, type )"
						 + "VALUES("
						 + tasteToInsert.getRestaurantId() + ","
						 + "'" + tasteToInsert.getPreference() + "',"
						 + tasteToInsert.getPrice() + ","
						 + tasteToInsert.getCategory().getId() + ","
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
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".taste SET " +
			  " taste_id = " + tasteToUpdate.getTasteId() + 
			  (builder.isPrefChanged() ? " ,preference = '" + tasteToUpdate.getPreference() + "'" : "") +
			  (builder.isRateChanged() ? " ,rate = " + tasteToUpdate.getRate() : "") +
			  (builder.isPriceChanged() ?" ,price = " + tasteToUpdate.getPrice() : "") +
			  (builder.isCategoryChanged() ? " ,category_id = " + tasteToUpdate.getCategory().getId() : "") +
			  (builder.isCategoryChanged() ? " ,calc = " + tasteToUpdate.getCalc().getVal() : "") +
			  " WHERE restaurant_id = " + staff.getRestaurantId() +
			  " AND taste_id = " + tasteToUpdate.getTasteId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(TasteError.TASTE_NOT_EXIST);
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
			throw new BusinessException(TasteError.TASTE_NOT_EXIST);
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
