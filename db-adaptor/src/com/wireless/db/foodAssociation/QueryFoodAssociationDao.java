package com.wireless.db.foodAssociation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;

public class QueryFoodAssociationDao {
	
	/**
	 * Calculate the rank to each associated food according to a specific food.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the associated terminal
	 * @param foodToAssociated
	 * 			the food to be associated
	 * @return the associated foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to be associated NOT exist
	 */
	public static Food[] exec(Staff term, Food foodToAssociated) throws SQLException, BusinessException{ 
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, foodToAssociated);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the rank to each associated food according to a specific food.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the associated terminal
	 * @param foodToAssociated
	 * 			the food to be associated
	 * @return the associated foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to be associated NOT exist
	 */
	public static Food[] exec(DBCon dbCon, Staff term, Food foodToAssociated) throws SQLException, BusinessException{
		//Get the detail to food to associated.
		foodToAssociated = FoodDao.getPureFoodByAlias(dbCon, term, foodToAssociated.getAliasId());
		
		/**
		 * Calculate point to each associated food, and sort it in descending order.
		 * The calculation rule is as below,
		 * point = similarity * weight
		 * where weight is equal or greater than zero.
		 */
		String sql;
		sql = " SELECT " +
			  " FA.associated_food_id, FA.similarity * F.weight AS point " +
			  " FROM " +
			  Params.dbName + ".food_association FA " +
			  " JOIN " + Params.dbName + ".food F" + 
			  " ON " + 
			  " F.food_id = FA.associated_food_id " + 
			  " AND " +
			  " F.weight > 0 " +
			  " WHERE " +
			  " FA.food_id = " + foodToAssociated.getFoodId() +
			  " ORDER BY point DESC ";
		
		List<Food> associatedFoods = new ArrayList<Food>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			associatedFoods.add(new Food(dbCon.rs.getInt("associated_food_id"), 0, 0));
		}
		
		//Get the details to each associated food.
		List<Food> result = new ArrayList<Food>(associatedFoods.size());
		for(Food food : associatedFoods){
			result.add(FoodDao.getPureFoodById(dbCon, term, food.getFoodId()));
		}
		
		return result.toArray(new Food[result.size()]);
		
	}
	
}
