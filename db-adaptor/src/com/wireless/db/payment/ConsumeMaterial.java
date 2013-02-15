package com.wireless.db.payment;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.orderMgr.QueryOrderFoodDao;
import com.wireless.dbObject.FoodMaterial;
import com.wireless.dbObject.MaterialDetail;
import com.wireless.dbReflect.FoodMaterialReflector;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Terminal;

/**
 * Perform to material consume according to a specific order
 * @author Ying.Zhang
 *
 */
public class ConsumeMaterial {
	
	/**
	 * Perform to material consume according to a specific order id.
	 * @param term
	 * 			the terminal to query
	 * @param orderID
	 * 			the order id to query
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if any logic exception occurred
	 */
	public static void execByOrderID(Terminal term, int orderID) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			execByOrderID(dbCon, term, orderID);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to material consume according to a specific order id.
	 * Note that the database should be connected before invoking this method
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to query
	 * @param orderID
	 * 			the order id to query
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if any logic exception occurred
	 */
	public static void execByOrderID(DBCon dbCon, Terminal term, int orderID) throws SQLException, BusinessException{

		//get the food details to this order
		OrderFood[] foods = QueryOrderFoodDao.getDetailToday(dbCon, " AND OF.order_id=" + orderID, "");
		for(OrderFood food : foods){
			if(food.isCombo()){
				for(Food childFood : food.getChildFoods()){
					OrderFood orderChildFood = new OrderFood(childFood);
					orderChildFood.setCount(childFood.getAmount() * food.getCount());
					inventoryHedging(dbCon, term, orderChildFood);
				}
			}else{
				inventoryHedging(dbCon, term, food);
			}			
		}	
	}
	
	private static void inventoryHedging(DBCon dbCon, Terminal term, OrderFood food) throws SQLException, BusinessException{
		//get each material consumption to every food
		FoodMaterial[] foodMaterials = FoodMaterialReflector.getFoodMaterial(dbCon, 
																			 " AND FOOD_MATE.food_id=" + food.getFoodId() +
																			 " AND FOOD_MATE.restaurant_id=" + term.restaurantID, 
																			 null);		
		try{
			
			String sql;
			
			dbCon.conn.setAutoCommit(false);
			
			//calculate the 库存对冲 and insert the record to material_detail
			for(FoodMaterial foodMaterial : foodMaterials){
				//calculate the 库存对冲
				float amount = (float)Math.round(food.getCount() * foodMaterial.consumption * 100) / 100;
				
				//insert the corresponding detail record to material_detail
				sql = "INSERT INTO " + Params.dbName + ".material_detail (" + 
					  "restaurant_id, food_id, material_id, price, date, staff, dept_id, amount, type) VALUES(" +
					  term.restaurantID + ", " +							//restaurant_id
					  food.getFoodId() + ", " +						//food_id
					  foodMaterial.material.materialID + ", " +		//material_id
					  "(SELECT price FROM " + Params.dbName + ".material_dept WHERE restaurant_id=" + 
					  term.restaurantID +
					  " AND material_id=" + foodMaterial.material.materialID + 	
					  " AND dept_id=0), " +	//price
					  "NOW(), " +			//date
					  "(SELECT owner_name FROM " + Params.dbName + 
					  ".terminal WHERE pin=" + "0x" + Long.toHexString(term.pin) + " AND model_id=" + term.modelID + "), " +	//staff
					  "(SELECT dept_id FROM " + Params.dbName + ".kitchen WHERE restaurant_id=" + 
					  term.restaurantID + " AND kitchen_alias=" + food.getKitchen().getAliasId() + "), " +				//dept_id
					  -amount + ", " + 				//amount
					  MaterialDetail.TYPE_CONSUME + //type
					  ")";
				dbCon.stmt.executeUpdate(sql);
				
				//update the stock of material_dept to this material
				sql = "UPDATE " + Params.dbName + ".material_dept SET " +
					  "stock = stock - " + amount +
					  " WHERE restaurant_id=" + term.restaurantID + 
					  " AND material_id=" + foodMaterial.material.materialID +
					  " AND dept_id=" + "(SELECT dept_id FROM " + Params.dbName + ".kitchen WHERE restaurant_id=" + 
					  term.restaurantID + " AND kitchen_alias=" + food.getKitchen().getAliasId() + ")";
				dbCon.stmt.executeUpdate(sql);
			}
			
			dbCon.conn.commit();

		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw new BusinessException(e.getMessage());
			
		}finally{
			dbCon.conn.setAutoCommit(true);
		}
	}
}
