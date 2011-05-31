package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Member;
import com.wireless.protocol.Order;
import com.wireless.protocol.Util;

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
			/**
			 * Calculate the total price of this order as below.
			 * total = food_price_1 + food_price_2 + ...
			 * food_price_n = (unit * discount + taste) * count
			 */
			float totalPrice = 0;
			for(int i = 0; i < orderInfo.foods.length; i++){
				float discount = (float)Math.round(orderInfo.foods[i].discount) / 100;					
				float foodPrice = Util.price2Float(orderInfo.foods[i].price, Util.INT_MASK_2).floatValue();
				float tastePrice = Util.price2Float(orderInfo.foods[i].taste.price, Util.INT_MASK_2).floatValue();
				totalPrice += (foodPrice * discount + tastePrice) * orderInfo.foods[i].count2Float().floatValue();
			}
			totalPrice = (float)Math.round(totalPrice * 100) / 100;
			orderInfo.setTotalPrice(totalPrice);
			
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
			 * Calculate the member balance if the pay type is for member.
			 * The formula is as below.
			 * balance = balance - actualPrice + actualPrice * exchange_rate
			 */
			if(orderInfo.pay_type == Order.PAY_MEMBER){
				String actualMoney = Util.price2Float(orderInfo.actualPrice, Util.INT_MASK_3).toString();
				sql = "UPDATE " + Params.dbName + ".member SET balance=balance - " + actualMoney +
					  " + " + actualMoney + " * exchange_rate" + 
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
			 * - pay order date
			 * - comment if exist
			 * - member id if pay type is for member
			 * - member name if pay type is for member
			 */
			sql = "UPDATE `" + Params.dbName + "`.`order` SET terminal_pin=" + pin +
				  ", total_price=" + totalPrice + 
				  ", total_price_2=" + Util.price2Float(orderInfo.actualPrice, Util.INT_MASK_3) +
				  ", type=" + orderInfo.pay_manner + 
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
				
//				sql = "UPDATE " + WirelessSocketServer.database + 
//					  ".food SET order_count=order_count+1 WHERE alias_id=" + orderToPay.foods[i].alias_id +
//					  " AND restaurant_id=" + _restaurantID;
			}
			dbCon.stmt.executeBatch();
			
			return orderInfo;
			
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
		
		Order orderInfo = QueryOrder.exec(dbCon, pin, model, orderToPay.table_id);
		
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
			 * The special food does NOT discount
			 */
			if(orderInfo.foods[i].isSpecial()){
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
		
		orderInfo.restaurant_id = orderToPay.restaurant_id;
		orderInfo.pay_type = orderToPay.pay_type;
		orderInfo.discount_type = orderToPay.discount_type;
		orderInfo.member_id = orderToPay.member_id; 
		orderInfo.actualPrice = orderToPay.actualPrice;
		orderInfo.pay_manner = orderToPay.pay_manner;
		orderInfo.comment = orderToPay.comment;
			
		return orderInfo;
	}
}
