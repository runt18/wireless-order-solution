package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import com.wireless.dbObject.FoodMaterial;
import com.wireless.dbObject.Material;
import com.wireless.dbObject.MaterialDetail;
import com.wireless.dbReflect.OrderFoodReflector;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Member;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class PayOrder {
	/**
	 * Perform to pay an order along with the table id, payment and discount type.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param orderToPay the pay order information along with the table id, payment and discount type, 				     
	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The table associated with this order is idle.<br>
	 * 							 - The order to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Order exec(int pin, short model, Order orderToPay) throws BusinessException, SQLException{
		
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			/**
			 * Get the unpaid order id associated with this table
			 */		
			Table table = QueryTable.exec(dbCon, pin, model, orderToPay.table_id);
			orderToPay.id = Util.getUnPaidOrderID(dbCon, table);
			orderToPay.restaurantID = table.restaurantID;
			
			return execByID(dbCon, pin, model, orderToPay);			
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to pay an order along with the order id, payment and discount type.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param orderToPay the pay order information along with the order ID, payment and discount type 				     
	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The order to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Order execByID(int pin, short model, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			orderToPay.restaurantID = term.restaurant_id;
			return execByID(dbCon, pin, model, orderToPay);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Perform to pay an order along with the order id.
	 * Note that the database should be connected before invoking this method.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param orderToPay the pay order information along with the order ID, payment and discount, 				     
	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The order to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Order execByID(DBCon dbCon, int pin, short model, Order orderToPay) throws BusinessException, SQLException{
		
		/**
		 * Get the completed order information.
		 */			
		Order orderInfo = queryOrderByID(dbCon, pin, model, orderToPay); 
			
		float totalPrice = orderInfo.getTotalPrice().floatValue();
		float totalPrice2 = orderInfo.getActualPrice().floatValue();
			
		/**
		 * Get the member info if the pay type for member
		 */
		Member member = null;
		if(orderInfo.pay_type == Order.PAY_MEMBER){
			if(orderInfo.memberID != null){
				member = QueryMember.exec(dbCon, orderInfo.restaurantID, orderInfo.memberID);
			}else{
				member = new Member("", "", "", new Float(0));
			}
		}
			
		dbCon.stmt.clearBatch();
		String sql = null;
		/**
		 * Calculate the member balance if both pay type and manner is for member .
		 * The formula is as below.
		 * balance = balance - actualPrice + actualPrice * exchange_rate
		 */
		if(orderInfo.pay_type == Order.PAY_MEMBER && orderInfo.pay_manner == Order.MANNER_MEMBER && orderInfo.memberID != null){
			sql = "UPDATE " + Params.dbName + ".member SET balance=balance - " + totalPrice2 +
				  " + " + totalPrice2 + " * exchange_rate" + 
				  " WHERE restaurant_id=" + orderInfo.restaurantID +
				  " AND alias_id=" + member.alias_id;
			
			dbCon.stmt.addBatch(sql);
		}
					 
		/**
		 * Update the values below to "order" table
		 * - total price
		 * - actual price
		 * - gift price
		 * - waiter
		 * - discount_type
		 * - payment manner
		 * - terminal pin
		 * - service rate
		 * - pay order date
		 * - comment if exist
		 * - member id if pay type is for member
		 * - member name if pay type is for member
		 */
		sql = "UPDATE `" + Params.dbName + "`.`order` SET " +
			  "waiter=(SELECT owner_name FROM " + Params.dbName + ".terminal WHERE pin=" + "0x" + Integer.toHexString(pin) + " AND model_id=" + model + ")" +
			  ", terminal_pin=" + pin +
			  ", gift_price=" + orderInfo.getGiftPrice() +
			  ", total_price=" + totalPrice + 
			  ", total_price_2=" + totalPrice2 +
			  ", type=" + orderInfo.pay_manner + 
			  ", discount_type=" + orderInfo.discount_type +
			  ", service_rate=" + ((float)orderInfo.service_rate / 100) +
		   	  ", order_date=NOW()" + 
			  (orderInfo.comment != null ? ", comment='" + orderInfo.comment + "'" : "") +
			  (member != null ? ", member_id='" + member.alias_id + "', member='" + member.name + "'" : ", member_id=NULL, member=NULL") + 
			  " WHERE id=" + orderInfo.id;
			
		dbCon.stmt.addBatch(sql);
			

		/**
		 * Update each food's discount to "order_food" table
		 */
		for(int i = 0; i < orderInfo.foods.length; i++){
			sql = "UPDATE " + Params.dbName + ".order_food SET discount=" + orderInfo.foods[i].getDiscount() +
				  " WHERE order_id=" + orderInfo.id + 
				  " AND food_id=" + orderInfo.foods[i].alias_id;
			dbCon.stmt.addBatch(sql);				
		}
			
		/**
		 * Delete the table in the case of "并台" or "外卖",
		 * since the table to these order is temporary. 
		 * Otherwise update the table status to idle.
		 */
		if(orderInfo.category == Order.CATE_JOIN_TABLE || orderInfo.category == Order.CATE_TAKE_OUT){
			sql = "DELETE FROM " + Params.dbName + ".table WHERE alias_id=" +
				  orderInfo.table_id + " AND restaurant_id=" + orderInfo.restaurantID;
			dbCon.stmt.addBatch(sql);
		}else{
			if(orderInfo.category == Order.CATE_MERGER_TABLE){
				sql = "UPDATE " + Params.dbName + ".table SET " +
				  	  "status=" + Table.TABLE_IDLE + ", " +
				  	  "custom_num=NULL, " +
				  	  "category=NULL " +
				  	  "WHERE alias_id=" + orderInfo.table2_id + " AND restaurant_id=" + orderInfo.restaurantID;
				dbCon.stmt.addBatch(sql);
			}
			sql = "UPDATE " + Params.dbName + ".table SET " +
				  "status=" + Table.TABLE_IDLE + ", " +
				  "custom_num=NULL, " +
				  "category=NULL " +
				  "WHERE alias_id=" + orderInfo.table_id + " AND restaurant_id=" + orderInfo.restaurantID;
			dbCon.stmt.addBatch(sql);
		}
		dbCon.stmt.executeBatch();
			
		//get the food details to this order
		OrderFood[] foods = OrderFoodReflector.getDetailToday(dbCon, " AND C.order_id=" + orderInfo.id, "");
		for(int i = 0; i < foods.length; i++){
			//get each material consumption to every food
			sql = "SELECT consumption, material_id FROM " +
					Params.dbName + ".food_material WHERE " +
					"food_id=" +
					"(SELECT id FROM " + 
					Params.dbName +
					".food WHERE alias_id=" + foods[i].alias_id +
					" AND restaurant_id="+ orderInfo.restaurantID + ")"; 
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			ArrayList<FoodMaterial> foodMaterials = new ArrayList<FoodMaterial>();
			while(dbCon.rs.next()){				
				foodMaterials.add(new FoodMaterial(foods[i],
												   new Material(dbCon.rs.getLong("material_id")),
												   dbCon.rs.getFloat("consumption")));
			}			
			dbCon.rs.close();
			
			dbCon.stmt.clearBatch();
			//calculate the 库存对冲 and insert the record to material_detail
			Iterator<FoodMaterial> iter = foodMaterials.iterator();
			while(iter.hasNext()){
				FoodMaterial foodMaterial = iter.next();
				//calculate the 库存对冲
				float amount = (float)Math.round(foodMaterial.food.getCount().floatValue() * foodMaterial.consumption * 100) /100;
				
				//insert the corresponding detail record to material_detail
				sql = "INSERT INTO " + Params.dbName + ".material_detail (" + 
					  "restaurant_id, material_id, price, date, staff, dept_id, amount, type) VALUES(" +
					  orderInfo.restaurantID + ", " +						//restaurant_id
					  foodMaterial.material.getMaterialID() + ", " +		//material_id
					  "(SELECT price FROM " + Params.dbName + ".material_dept WHERE restaurant_id=" + 
					  orderInfo.restaurantID + 
					  " AND material_id=" + foodMaterial.material.getMaterialID() + 	
					  " AND dept_id=0), " +	//price
					  "NOW(), " +			//date
					  "(SELECT owner_name FROM " + Params.dbName + 
					  ".terminal WHERE pin=" + "0x" + Integer.toHexString(pin) + " AND model_id=" + model + "), " +	//staff
					  "(SELECT dept_id FROM " + Params.dbName + ".kitchen WHERE restaurant_id=" + 
					  orderInfo.restaurantID + " AND alias_id=" + foodMaterial.food.kitchen + "), " +				//dept_id
					  -amount + ", " + 				//amount
					  MaterialDetail.TYPE_CONSUME + //type
					  ")";
				dbCon.stmt.addBatch(sql);
				
				//update the stock of material_dept to this material
				sql = "UPDATE " + Params.dbName + ".material_dept SET " +
					  "stock = stock - " + amount +
					  " WHERE restaurant_id=" + orderInfo.restaurantID + 
					  " AND material_id=" + foodMaterial.material.getMaterialID() +
					  " AND dept_id=" + "(SELECT dept_id FROM " + Params.dbName + ".kitchen WHERE restaurant_id=" + 
					  orderInfo.restaurantID + " AND alias_id=" + foodMaterial.food.kitchen + ")";
				dbCon.stmt.addBatch(sql);
			}
			
			dbCon.stmt.executeBatch();
			
		}
		
		return orderInfo;

	}
	
	/**
	 * Get the order detail information and get the discount to each food 
	 * according to the table id, payment and discount type.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon the database connection
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param orderToPay the pay order information along with table ID, payment and discount type,
	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 *  						 - The table associated with this order is idle.<br>
	 * 							 - The order to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Order queryOrder(int pin, short model, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			/**
			 * Get the unpaid order id associated with this table
			 */
			Table table = QueryTable.exec(dbCon, pin, model, orderToPay.table_id);
			orderToPay.id = Util.getUnPaidOrderID(dbCon, table);
			orderToPay.restaurantID = table.restaurantID;
			
			return queryOrderByID(dbCon, pin, model, orderToPay);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information and get the discount to each food 
	 * according to the order id, payment and discount type.
	 * @param dbCon the database connection
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param orderToPay the pay order information along with order ID, payment and discount type,
	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The order to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Order queryOrderByID(int pin, short model, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryOrderByID(dbCon, pin, model, orderToPay);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information and get the discount to each food 
	 * according to the order id, payment and discount type.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon the database connection
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param orderToPay the pay order information along with order ID, payment and discount type,
	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The order to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Order queryOrderByID(DBCon dbCon, int pin, short model, Order orderToPay) throws BusinessException, SQLException{
		
		Order orderInfo = QueryOrder.execByID(dbCon, pin, model, orderToPay.id);
		
		/**
		 * If the pay order formation does NOT comprise the discount,
		 * get the discount according the pay type and manner.
		 */
		//if(!incDiscount){
			String discount = "discount";
			if(orderToPay.pay_type == Order.PAY_NORMAL && orderToPay.discount_type == Order.DISCOUNT_1){
				discount = "discount";
				
			}else if(orderToPay.pay_type == Order.PAY_NORMAL && orderToPay.discount_type == Order.DISCOUNT_2){
				discount = "discount_2";
				
			}else if(orderToPay.pay_type == Order.PAY_NORMAL && orderToPay.discount_type == Order.DISCOUNT_3){
				discount = "discount_3";
				
			}else if(orderToPay.pay_type == Order.PAY_MEMBER && orderToPay.memberID != null){
				//validate the member id
				String sql = "SELECT id FROM " + Params.dbName + 
							 ".member WHERE restaurant_id=" + orderToPay.restaurantID + 
							 " AND alias_id='" + orderToPay.memberID + "'";
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					if(orderToPay.discount_type == Order.DISCOUNT_1){
						discount = "member_discount_1";
					}else if(orderToPay.discount_type == Order.DISCOUNT_2){
						discount = "member_discount_2";
					}else if(orderToPay.discount_type == Order.DISCOUNT_3){
						discount = "member_discount_3";
					}
				}else{
					throw new BusinessException("The member id(" + orderToPay.memberID + ") is invalid.", ErrorCode.MEMBER_NOT_EXIST);
				}
				
			}else if(orderToPay.pay_type == Order.PAY_MEMBER && orderToPay.memberID == null){
				if(orderToPay.discount_type == Order.DISCOUNT_1){
					discount = "member_discount_1";
				}else if(orderToPay.discount_type == Order.DISCOUNT_2){
					discount = "member_discount_2";
				}else if(orderToPay.discount_type == Order.DISCOUNT_3){
					discount = "member_discount_3";
				}
			}
				
			for(int i = 0; i < orderInfo.foods.length; i++){
				/**
				 * Both the special food and gifted food does NOT discount
				 */
				if(orderInfo.foods[i].isSpecial() || orderInfo.foods[i].isGift()){
					orderInfo.foods[i].setDiscount(new Float(1.0));
				}else{
					/**
					 * Get the discount to each food according to the kitchen of this restaurant.
					 */
					String sql = "SELECT " + discount + " FROM " + Params.dbName + 
								 ".kitchen WHERE restaurant_id=" + orderInfo.restaurantID + 
								 " AND alias_id=" + orderInfo.foods[i].kitchen;
					
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					if(dbCon.rs.next()){
						orderInfo.foods[i].setDiscount(dbCon.rs.getFloat(discount));
					}
					dbCon.rs.close();				
				}
			}
		//}
		
		/**
		 * Calculate the total price of this order(exclude the gifted foods) as below.
		 * total = food_price_1 + food_price_2 + ...
		 * food_price_n = (unit * discount + taste) * count
		 */
		float totalPrice = 0;
		for(int i = 0; i < orderInfo.foods.length; i++){
			if(!orderInfo.foods[i].isGift()){
				float dist = orderInfo.foods[i].getDiscount();					
				float foodPrice = orderInfo.foods[i].getPrice().floatValue();
				float tastePrice = orderInfo.foods[i].getTastePrice();
				totalPrice += (foodPrice * dist + tastePrice) * orderInfo.foods[i].getCount().floatValue();
			}
		}
		
		/**
		 * Minus the gift price as 
		 * total = total - gift
		 */
		totalPrice = (float)Math.round((totalPrice - orderToPay.getGiftPrice()) * 100) / 100;
		orderInfo.setTotalPrice(totalPrice);

		/**
		 * Multiplied by service rate
		 * total = total * (1 + service_rate)
		 */
		totalPrice = totalPrice * (1 + ((float)orderToPay.service_rate / 100));		
		totalPrice = (float)Math.round(totalPrice * 100) / 100;
		
		/**
		 * Calculate the total price 2 as below.
		 * total_2 = total * 尾数处理方式
		 */
		float totalPrice2;
		/**
		 * Comparing the minimum cost against total price.
		 * Set the actual price to minimum cost if total price is less than minimum cost.
		 */
		if(totalPrice < orderInfo.getMinimumCost()){
			//直接使用最低消费
			totalPrice2 = orderInfo.getMinimumCost();			
		}else if(orderInfo.price_tail == Order.TAIL_DECIMAL_CUT){
			//小数抹零
			totalPrice2 = new Float(totalPrice).intValue();
		}else if(orderInfo.price_tail == Order.TAIL_DECIMAL_ROUND){
			//小数四舍五入
			totalPrice2 = Math.round(totalPrice);
		}else{
			totalPrice2 = totalPrice;
		}
		
		orderInfo.setActualPrice(totalPrice2);
		
		orderInfo.restaurantID = orderToPay.restaurantID;
		orderInfo.pay_type = orderToPay.pay_type;
		orderInfo.discount_type = orderToPay.discount_type;
		orderInfo.memberID = orderToPay.memberID; 
		orderInfo.setCashIncome(orderToPay.getCashIncome());
		orderInfo.setGiftPrice(orderToPay.getGiftPrice());
		orderInfo.pay_manner = orderToPay.pay_manner;
		orderInfo.comment = orderToPay.comment;
		orderInfo.service_rate = orderToPay.service_rate;
			
		return orderInfo;
	}
}
