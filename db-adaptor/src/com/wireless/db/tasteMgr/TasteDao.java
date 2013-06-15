package com.wireless.db.tasteMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.TasteError;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.protocol.Terminal;

public class TasteDao {

	/**
	 * Get the tastes to the specified restaurant defined in {@link Terminal} and category {@link Taste.Category}.
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
	public static List<Taste> getTasteByCategory(DBCon dbCon, Terminal term, Taste.Category category) throws SQLException{
		return getTastes(dbCon, term, " AND TASTE.category = " + category.getVal(), null);
	}
	
	/**
	 * Get the taste to the specified restaurant and taste alias.
	 * @param dbCon
	 * 			the database connection
	 * @param term	
	 * 			the terminal
	 * @param tasteAlias
	 * 			the taste alias
	 * @return the taste to specified restaurant and taste alias
	 * @throws BusinessException
	 * 			throws if the taste to query is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Taste getTasteByAlias(DBCon dbCon, Terminal term, int tasteAlias) throws BusinessException, SQLException{
		List<Taste> tastes = getTastes(dbCon, term, " AND TASTE.taste_alias = " + tasteAlias, null);
		if(tastes.isEmpty()){
			throw new BusinessException("The taste(alias = " + tasteAlias + ", restaurant_id = " + term.restaurantID + ") is NOT found.");
		}else{
			return tastes.get(0);
		}
	}
	
	/**
	 * Get the tastes to the specified restaurant defined in {@link Terminal} and other extra condition.
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
	public static List<Taste> getTastes(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		String sql = " SELECT " +
				 	 " taste_id, taste_alias, restaurant_id, preference, " +
				 	 " category, calc, rate, price, type " +
				 	 " FROM " + 
				 	 Params.dbName + ".taste TASTE " +
				 	 " WHERE 1=1 " +
				 	 " AND TASTE.restaurant_id = " + term.restaurantID + " " +
				 	 (extraCond == null ? "" : extraCond) + " " +
				 	 (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Taste> tastes = new ArrayList<Taste>();
		while(dbCon.rs.next()){
			
			Taste taste = new Taste(dbCon.rs.getInt("taste_id"),
								    dbCon.rs.getInt("taste_alias"), 
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
	public static List<Taste> getTastes(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return TasteDao.getTastes(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static Taste getTasteById(DBCon dbCon, Terminal term, int id) throws SQLException{
		List<Taste> list = TasteDao.getTastes(dbCon, term, " AND taste_id = " + id, null);
		if(list != null && !list.isEmpty()){
			return list.get(0);
		}else{
			return null;
		}
	}
	/**
	 * 
	 * @param term
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static Taste getTasteById(Terminal term, int id) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return TasteDao.getTasteById(dbCon, term, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 检查某餐厅下某口味编号是否已经存在
	 * @param dbCon
	 * @param restuarntId
	 * @param alias
	 * @return
	 * @throws SQLException
	 */
	public static boolean hasAlias(DBCon dbCon, int restuarntId, int alias) throws SQLException{
		String querySQL = "SELECT COUNT(taste_id) FROM " + Params.dbName + ".taste "
						+ " WHERE restaurant_id = " + restuarntId + " AND taste_alias = " + alias;
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt(1) > 0){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param insert
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int insert(DBCon dbCon, Terminal term, Taste insert) throws BusinessException, SQLException{
		int count = 0;
		if(TasteDao.hasAlias(dbCon, term.restaurantID, insert.getAliasId())){
			throw new BusinessException(TasteError.HAS_ALIAS);
		}
		String insertSQL = "INSERT INTO " + Params.dbName + ".taste"
						 + "( restaurant_id, taste_alias, preference, price, category, rate, calc )"
						 + "VALUES("
						 + insert.getRestaurantId() + ","
						 + insert.getAliasId() + ","
						 + "'" + insert.getPreference() + "',"
						 + insert.getPrice() + ","
						 + insert.getCategory().getVal() + ","
						 + insert.getRate() + ","
						 + insert.getCalc().getVal()
						 + ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	/**
	 * 
	 * @param term
	 * @param insert
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void insert(Terminal term, Taste insert) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = TasteDao.insert(dbCon, term, insert);
			if(count == 0){
				throw new BusinessException(TasteError.INSERT_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param update
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int update(DBCon dbCon, Terminal term, Taste update) throws BusinessException, SQLException{
		int count = 0;
		Taste old = TasteDao.getTasteById(dbCon, term, update.getTasteId());
		// 如果新旧编号不一样.则再次检查新编号是否已存在
		if(old.getAliasId() != update.getAliasId()){
			if(TasteDao.hasAlias(dbCon, term.restaurantID, update.getAliasId())){
				throw new BusinessException(TasteError.HAS_ALIAS);
			}
		}
		String updateSQL = "UPDATE " + Params.dbName + ".taste SET "
						 + " preference = '" + update.getPreference() + "',"
						 + " rate = " + update.getRate() + ","
						 + " calc = " + update.getCalc().getVal() + ","
						 + " price = " + update.getPrice() + ","
						 + " category = " + update.getCategory().getVal() + ","
						 + " taste_alias = " + update.getAliasId() 
						 + " WHERE restaurant_id = " + term.restaurantID 
						 + " AND taste_id = " + update.getTasteId();
		count = dbCon.stmt.executeUpdate(updateSQL);
		return count;
	}
	/**
	 * 
	 * @param term
	 * @param update
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void update(Terminal term, Taste update) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = TasteDao.update(dbCon, term, update);
			if(count == 0){
				throw new BusinessException(TasteError.UPDATE_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param id
	 * @return
	 * @throws SQLException
	 */
	public static int delete(DBCon dbCon, Terminal term, int id) throws SQLException{
		int count = 0;
		String deleteSQL = "DELETE FROM " + Params.dbName + ".taste"
			+ " WHERE taste_id = " + id
			+ " AND restaurant_id = " + term.restaurantID;
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
	}
	/**
	 * 
	 * @param term
	 * @param id
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void delete(Terminal term, int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			int count = TasteDao.delete(dbCon, term, id);
			if(count == 0){
				throw new BusinessException(TasteError.DELETE_FAIL);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
}
