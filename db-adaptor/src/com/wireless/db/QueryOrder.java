package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;

public class QueryOrder {
	
	/**
	 * Get the order detail information to the specific table alias id.
	 * @param pin the pin to the terminal
	 * @param model the model id to the terminal
	 * @param tableID the table alias id to query
	 * @return Order the order detail information
	 * @throws BusinessException throws if one of cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The table to query does NOT exist.<br>
	 * 							 - The table to query is idle.
	 * @throws SQLException throws if fail to execute any SQL statement.
	 */
	public static Order exec(int pin, short model, short tableID) throws BusinessException, SQLException{
		
		Table table = QueryTable.exec(pin, model, tableID);
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			//query the order id associated with the this table
			String sql = "SELECT id FROM `" + Params.dbName + 
						"`.`order` WHERE table_id = " + tableID +
						" AND restaurant_id = " + table.restaurant_id +
						" AND total_price IS NULL";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				int orderID = dbCon.rs.getInt("id");
				int nCustom = 0;
				//query the custom number from "order" table according to the order id
				sql = "SELECT custom_num FROM `" + Params.dbName + "`.`order` WHERE id=" + orderID;
				
				dbCon.rs.close();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					nCustom = dbCon.rs.getByte(1);
				}
				//query the food's id and order count associate with the order id for "order_food" table
				sql = "SELECT name, food_id, SUM(order_count) AS order_sum, unit_price, discount, taste, taste_price, taste_id FROM `" + 
						Params.dbName + 
						"`.`order_food` WHERE order_id=" + orderID +
						" GROUP BY food_id, taste_id HAVING order_sum > 0";
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				ArrayList<Food> foods = new ArrayList<Food>();
				while(dbCon.rs.next()){
					Food food = new Food();
					food.name = dbCon.rs.getString("name");
					food.alias_id = dbCon.rs.getInt("food_id");
					food.setCount(new Float(dbCon.rs.getFloat("order_sum")));
					food.setPrice(new Float(dbCon.rs.getFloat("unit_price")));
					food.discount = (byte)(dbCon.rs.getFloat("discount") * 100);
					food.taste.preference = dbCon.rs.getString("taste");
					food.taste.setPrice(dbCon.rs.getFloat("taste_price"));
					food.taste.alias_id = dbCon.rs.getShort("taste_id");
					foods.add(food);			
				}
				
				Order orderInfo = new Order();
				orderInfo.id = orderID;
				orderInfo.tableID = tableID;
				orderInfo.customNum = nCustom;
				orderInfo.foods = foods.toArray(new Food[foods.size()]);
				
				return orderInfo;
				
			}else{
				throw new BusinessException("The table(alias_id=" + tableID + ") to query is idle.", ErrorCode.TABLE_IDLE);
			}
		}finally{
			dbCon.disconnect();
		}
		
	}
}
