package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Member;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;

public class PayOrder {
	/**
	 * Perform to pay an order submitted by terminal.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param orderToPay the pay order information submitted by terminal,
	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The table associated with this order is idle.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Order exec(int pin, short model, Order orderToPay) throws BusinessException, SQLException{
		
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			/**
			 * Get the completed order information.
			 */			
			Order orderInfo = execQueryOrder(dbCon, pin, model, orderToPay); 
			
			float totalPrice = orderInfo.getTotalPrice().floatValue();
			float totalPrice2 = orderInfo.getActualPrice().floatValue();
			
			/**
			 * Get the member info if the pay type for member
			 */
			Member member = null;
			if(orderInfo.pay_type == Order.PAY_MEMBER && orderInfo.member_id != null){
				member = QueryMember.exec(dbCon, orderInfo.restaurant_id, orderInfo.member_id);
			}
			
			dbCon.stmt.clearBatch();
			String sql = null;
			/**
			 * Calculate the member balance if both pay type and manner is for member .
			 * The formula is as below.
			 * balance = balance - actualPrice + actualPrice * exchange_rate
			 */
			if(orderInfo.pay_type == Order.PAY_MEMBER && orderInfo.pay_manner == Order.MANNER_MEMBER){
				sql = "UPDATE " + Params.dbName + ".member SET balance=balance - " + totalPrice2 +
					  " + " + totalPrice2 + " * exchange_rate" + 
					  " WHERE restaurant_id=" + orderInfo.restaurant_id +
					  " AND alias_id=" + member.alias_id;
				
				dbCon.stmt.addBatch(sql);
			}
					 
			/**
			 * Update the values below to "order" table
			 * - total price
			 * - actual price
			 * - payment manner
			 * - terminal pin
			 * - service rate
			 * - pay order date
			 * - comment if exist
			 * - member id if pay type is for member
			 * - member name if pay type is for member
			 */
			sql = "UPDATE `" + Params.dbName + "`.`order` SET terminal_pin=" + pin +
				  ", total_price=" + totalPrice + 
				  ", total_price_2=" + totalPrice2 +
				  ", type=" + orderInfo.pay_manner + 
				  ", service_rate=" + ((float)orderInfo.service_rate / 100) +
			   	  ", order_date=NOW()" + 
				  (orderInfo.comment != null ? ", comment='" + orderInfo.comment + "'" : "") +
				  (member != null ? ", member_id=" + member.alias_id + ", member='" + member.name + "'" : "") + 
				  " WHERE id=" + orderInfo.id;
			
			dbCon.stmt.addBatch(sql);
			

			/**
			 * Update each food's discount to "order_food" table
			 */
			for(int i = 0; i < orderInfo.foods.length; i++){
				float discount = (float)orderInfo.foods[i].discount / 100;
				sql = "UPDATE " + Params.dbName + ".order_food SET discount=" + discount +
					  " WHERE order_id=" + orderInfo.id + 
					  " AND food_id=" + orderInfo.foods[i].alias_id;
				dbCon.stmt.addBatch(sql);				
			}
			
			/**
			 * Delete the table in the case of "å¹¶å�°" and "å¤–å�–",
			 * since the table to these order is temporary. 
			 */
			if(orderInfo.category == Order.CATE_JOIN_TABLE || orderInfo.category == Order.CATE_TAKE_OUT){
				sql = "DELETE FROM " + Params.dbName + ".table WHERE alias_id=" +
					  orderInfo.table_id + " AND restaurant_id=" + orderInfo.restaurant_id;
				dbCon.stmt.addBatch(sql);
			}
			dbCon.stmt.executeBatch();
			
			return orderInfo;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information and get the discount to each food according the payment and discount type.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param orderToPay the pay order information submitted by terminal,
	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The table associated with this order is idle.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Order queryOrder(int pin, short model, Order orderToPay) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			return execQueryOrder(dbCon, pin, model, orderToPay);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information and get the discount to each food according the payment and discount type.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon the database connection
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param orderToPay the pay order information submitted by terminal,
	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The table associated with this order is idle.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	private static Order execQueryOrder(DBCon dbCon, int pin, short model, Order orderToPay) throws BusinessException, SQLException{
		
		Table table = QueryTable.exec(dbCon, pin, model, orderToPay.table_id);
		
		Order orderInfo = QueryOrder.execByID(dbCon, pin, model, Util.getUnPaidOrderID(dbCon, table));
		
		String discount = "discount";
		if(orderToPay.pay_type == Order.PAY_NORMAL && orderToPay.discount_type == Order.DISCOUNT_1){
			discount = "discount";
			
		}else if(orderToPay.pay_type == Order.PAY_NORMAL && orderToPay.discount_type == Order.DISCOUNT_2){
			discount = "discount_2";
			
		}else if(orderToPay.pay_type == Order.PAY_NORMAL && orderToPay.discount_type == Order.DISCOUNT_3){
			discount = "discount_3";
			
		}else if(orderToPay.pay_type == Order.PAY_MEMBER){
			//validate the member id
			String sql = "SELECT id FROM " + Params.dbName + 
						 ".member WHERE restaurant_id=" + orderToPay.restaurant_id + 
						 " AND alias_id='" + orderToPay.member_id + "'";
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
				throw new BusinessException("The member id(" + orderToPay.member_id + ") is invalid.", ErrorCode.MEMBER_NOT_EXIST);
			}
		}
			
		for(int i = 0; i < orderInfo.foods.length; i++){
			/**
			 * Both the special food and gifted food does NOT discount
			 */
			if(orderInfo.foods[i].isSpecial() || orderInfo.foods[i].isGift()){
				orderInfo.foods[i].discount = 100;
			}else{
				//get the discount to each food according to the payment and discount type
				String sql = "SELECT " + discount + " FROM " + Params.dbName + 
							 ".kitchen WHERE restaurant_id=" + orderInfo.restaurant_id + 
							 " AND alias_id=" + orderInfo.foods[i].kitchen;
				
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					orderInfo.foods[i].discount = (byte)(dbCon.rs.getFloat(discount) * 100);
				}
				dbCon.rs.close();				
			}
		}
		
		/**
		 * Calculate the total price of this order as below.
		 * total = food_price_1 + food_price_2 + ...
		 * food_price_n = (unit * discount + taste) * count
		 */
		float totalPrice = 0;
		for(int i = 0; i < orderInfo.foods.length; i++){
			float dist = (float)Math.round(orderInfo.foods[i].discount) / 100;					
			float foodPrice = orderInfo.foods[i].getPrice().floatValue();
			float tastePrice = orderInfo.foods[i].getTastePrice();
			totalPrice += (foodPrice * dist + tastePrice) * orderInfo.foods[i].count2Float().floatValue();
		}
		totalPrice = (float)Math.round(totalPrice * 100) / 100;
		orderInfo.setTotalPrice(totalPrice);

		/**
		 * Multiplied by the service rate.
		 * total = total * (1 + service_rate)
		 */
		totalPrice = totalPrice * (1 + ((float)orderToPay.service_rate / 100));		
		
		/**
		 * Calculate the total price 2 as below.
		 * total_2 = total * 尾数处理方式
		 */
		float totalPrice2;
		/**
		 * Comparing the minimum cost against total price.
		 * Set the actual price to minimum cost if total price is less than minimum cost.
		 */
		if(totalPrice < table.getMinimumCost().floatValue()){
			//直接使用最低消费
			totalPrice2 = table.getMinimumCost().floatValue();
			
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
		
		orderInfo.restaurant_id = orderToPay.restaurant_id;
		orderInfo.pay_type = orderToPay.pay_type;
		orderInfo.discount_type = orderToPay.discount_type;
		orderInfo.member_id = orderToPay.member_id; 
		orderInfo.setCashIncome(orderToPay.getCashIncome());
		orderInfo.pay_manner = orderToPay.pay_manner;
		orderInfo.comment = orderToPay.comment;
		orderInfo.service_rate = orderToPay.service_rate;
			
		return orderInfo;
	}
}
