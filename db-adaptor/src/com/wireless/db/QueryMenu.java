package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Terminal;

public class QueryMenu {

	public static FoodMenu exec(int pin, short model) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(pin, model);
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			ArrayList<Food> foods = new ArrayList<Food>();
	        //get all the food information to this restaurant
			String sql = "SELECT alias_id, name, unit_price, kitchen FROM " + Params.dbName + ".food WHERE restaurant_id=" + term.restaurant_id +
						 " AND enabled=1";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Food food = new Food(dbCon.rs.getShort("alias_id"),
									 dbCon.rs.getString("name"),
									 new Float(dbCon.rs.getFloat("unit_price")),
									 dbCon.rs.getShort("kitchen"));
				foods.add(food);
			}
		
			dbCon.rs.close();
			//get all the kitchen information to this restaurant,
			ArrayList<Kitchen> kitchens = new ArrayList<Kitchen>();
			sql = "SELECT alias_id, name, discount, discount_2, discount_3, member_discount_1, member_discount_2, member_discount_3 FROM " + 
				  Params.dbName + ".kitchen WHERE restaurant_id=" + 
				  term.restaurant_id;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				kitchens.add(new Kitchen(dbCon.rs.getString("name"),
										 dbCon.rs.getShort("alias_id"),
										 (byte)(dbCon.rs.getFloat("discount") * 100),
										 (byte)(dbCon.rs.getFloat("discount_2") * 100),
										 (byte)(dbCon.rs.getFloat("discount_3") * 100),
										 (byte)(dbCon.rs.getFloat("member_discount_1") * 100),
										 (byte)(dbCon.rs.getFloat("member_discount_2") * 100),
										 (byte)(dbCon.rs.getFloat("member_discount_3") * 100)));
			}
			
			//Get the taste preferences to this restaurant sort by alias id in ascend order.
			//The lower alias id, the more commonly this preference used.
			//Put the most commonly used taste preference in first position 
			sql = "SELECT alias_id, preference, price FROM " + Params.dbName + ".taste WHERE restaurant_id=" + term.restaurant_id + 
					" ORDER BY alias_id";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			ArrayList<Taste> tastes = new ArrayList<Taste>();
			while(dbCon.rs.next()){
				Taste taste = new Taste(dbCon.rs.getShort("alias_id"), 
										dbCon.rs.getString("preference"),
										new Float(dbCon.rs.getFloat("price")));
				tastes.add(taste);
			}
			
			return new FoodMenu(foods.toArray(new Food[foods.size()]), 
							    tastes.toArray(new Taste[tastes.size()]),
							    kitchens.toArray(new Kitchen[kitchens.size()]));
			
		}finally{
			dbCon.disconnect();
		}
	}
	
}
