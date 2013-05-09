package com.wireless.db.tasteMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Taste;
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
	
}
