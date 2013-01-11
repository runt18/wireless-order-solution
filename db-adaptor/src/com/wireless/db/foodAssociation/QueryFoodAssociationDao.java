package com.wireless.db.foodAssociation;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.excep.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Terminal;

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
	public static Food[] exec(Terminal term, Food foodToAssociated) throws SQLException, BusinessException{ 
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
	public static Food[] exec(DBCon dbCon, Terminal term, Food foodToAssociated) throws SQLException, BusinessException{
		//Get the detail to food to associated.
		Food[] srcFoods = QueryMenu.queryPureFoods(dbCon, "AND FOOD.food_alias = " + foodToAssociated.getAliasId() + " AND FOOD.restaurant_id = " + term.restaurantID, null);
		if(srcFoods.length > 0){
			foodToAssociated = srcFoods[0];
		}else{
			throw new BusinessException("The food (alias_id = " + foodToAssociated.getAliasId() + ", restaurant_id = " + term.restaurantID + ") to be associated does NOT exist.", ErrorCode.MENU_EXPIRED);
		}
		
		/**
		 * Calculate point to each associated food, and sort it in descending order.
		 * The calculation rule is as below,
		 * point = associated_amount * weight
		 * where weight is equal or greater than zero.
		 */
		String sql;
		sql = " SELECT " +
			  " FA.associated_food_id, FA.associated_amount * FS.weight AS point " +
			  " FROM " +
			  Params.dbName + ".food_association FA " +
			  " JOIN " + Params.dbName + ".food_statistics FS" + 
			  " ON " + 
			  " FS.food_id = FA.associated_food_id " + 
			  " AND " +
			  " FS.weight > 0 " +
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
			Food[] foods = QueryMenu.queryPureFoods(dbCon, "AND FOOD.food_id = " + food.getFoodId(), null);
			if(foods.length > 0){
				result.add(foods[0]);
			}
		}
		
		return result.toArray(new Food[result.size()]);
		
	}
	
}
