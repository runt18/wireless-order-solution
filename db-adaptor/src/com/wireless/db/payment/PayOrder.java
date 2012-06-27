package com.wireless.db.payment;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryOrder;
import com.wireless.db.QuerySetting;
import com.wireless.db.QueryTable;
import com.wireless.db.Util;
import com.wireless.db.VerifyPin;
import com.wireless.dbObject.Setting;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Member;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class PayOrder {
	
	/**
	 * Perform to pay an order along with the table id, payment and discount
	 * type.
	 * 
	 * @param terminal
	 *            the terminal to pay order
	 * @param model
	 *            the model to this terminal
	 * @param orderToPay
	 *            the pay order information along with the table id, payment and
	 *            discount type, refer to the class "ReqPayOrder" for more
	 *            details on what information it contains.
 	 * @param isPaidAgain
	 * 			  indicating whether the order has been paid or not
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException
	 *             throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The table associated with this order is IDLE.<br>
	 *             - The order to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order exec(Terminal term, Order orderToPay, boolean isPaidAgain) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			/**
			 * Get the unpaid order id associated with this table
			 */	
			orderToPay.id = Util.getUnPaidOrderID(dbCon, QueryTable.exec(dbCon, term, orderToPay.table.aliasID));
			orderToPay.restaurantID = term.restaurant_id;
			
			return execByID(dbCon, term, orderToPay, isPaidAgain);	
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to pay an order along with the table id, payment and discount
	 * type.
	 * 
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @param orderToPay
	 *            the pay order information along with the table id, payment and
	 *            discount type, refer to the class "ReqPayOrder" for more
	 *            details on what information it contains.
	 * @param isPaidAgain
	 * 			  indicating whether the order has been paid or not
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException
	 *             throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The table associated with this order is idle.<br>
	 *             - The order to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order exec(long pin, short model, Order orderToPay, boolean isPaidAgain) throws BusinessException, SQLException{
		
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			/**
			 * Get the unpaid order id associated with this table
			 */	
			orderToPay.id = Util.getUnPaidOrderID(dbCon, QueryTable.exec(dbCon, term, orderToPay.table.aliasID));
			orderToPay.restaurantID = term.restaurant_id;
			
			return execByID(dbCon, term, orderToPay, isPaidAgain);			
			
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
	 * @param isPaidAgain indicating whether the order has been paid or not
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The order to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Order execByID(long pin, short model, Order orderToPay, boolean isPaidAgain) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			orderToPay.restaurantID = term.restaurant_id;
			return execByID(dbCon, term, orderToPay, isPaidAgain);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Perform to pay an order along with the order id. Note that the database
	 * should be connected before invoking this method.
	 * 
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @param orderToPay
	 *            the pay order information along with the order ID, payment and
	 *            discount, refer to the class "ReqPayOrder" for more details on
	 *            what information it contains.
	 * @param isPaidAgain
	 *            indicating whether the order is paid or not
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException
	 *             throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The order to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order execByID(DBCon dbCon, Terminal term, Order orderToPay, boolean isPaidAgain) throws BusinessException, SQLException{
		
		/**
		 * Get the completed order information.
		 */			
		Order orderInfo = queryOrderByID(dbCon, orderToPay); 
			
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
			
		
		String sql;
		/**
		 * Calculate the sequence id to this order in case of not been paid again.
		 */
		if(!isPaidAgain){
			sql = "SELECT CASE WHEN MAX(seq_id) IS NULL THEN 1 ELSE MAX(seq_id) + 1 END FROM " + 
				  Params.dbName + ".order WHERE restaurant_id=" + orderInfo.restaurantID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				orderInfo.seqID = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
		}

		
		/**
		 * Put all the INSERT statements into a database transition so as to assure 
		 * the status to both table and order is consistent. 
		 */
		try{
			
			dbCon.conn.setAutoCommit(false);
			
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
				
				dbCon.stmt.executeUpdate(sql);
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
			 * - sequence id
			 * - pay order date(if NOT paid again)
			 * - comment if exist
			 * - member id if pay type is for member
			 * - member name if pay type is for member
			 */
			sql = "UPDATE `" + Params.dbName + "`.`order` SET " +
				  "waiter=(SELECT owner_name FROM " + Params.dbName + ".terminal WHERE pin=" + "0x" + Long.toHexString(term.pin) + " AND (model_id=" + term.modelID + " OR model_id=" + Terminal.MODEL_ADMIN + "))" +
				  ", terminal_model=" + term.modelID +
				  ", terminal_pin=" + term.pin +
				  ", gift_price=" + orderInfo.calcGiftPrice() +
				  ", total_price=" + totalPrice + 
				  ", total_price_2=" + totalPrice2 +
				  ", type=" + orderInfo.pay_manner + 
				  ", discount_type=" + orderInfo.discount_type +
				  ", service_rate=" + orderInfo.getServiceRate() +
				  (isPaidAgain ? "" : ", seq_id=" + orderInfo.seqID) +
			   	  (isPaidAgain ? "" : ", order_date=NOW()") + 
				  (orderInfo.comment != null ? ", comment='" + orderInfo.comment + "'" : "") +
				  (member != null ? ", member_id='" + member.alias_id + "', member='" + member.name + "'" : ", member_id=NULL, member=NULL") + 
				  " WHERE id=" + orderInfo.id;
				
			dbCon.stmt.executeUpdate(sql);			
				

			/**
			 * Update the table status if the order is NOT paid again.
			 */
			if(!isPaidAgain){
				/**
				 * Delete the table in the case of "并台" or "外卖",
				 * since the table to these order is temporary. 
				 * Otherwise update the table status to idle.
				 */
				if(orderInfo.category == Order.CATE_JOIN_TABLE || orderInfo.category == Order.CATE_TAKE_OUT){
					sql = "DELETE FROM " + Params.dbName + ".table WHERE " +
						  "restaurant_id=" + orderInfo.restaurantID + " AND " +
						  "table_alias=" + orderInfo.table.aliasID;
					dbCon.stmt.executeUpdate(sql);
					
				}else{
					if(orderInfo.category == Order.CATE_MERGER_TABLE){
						sql = "UPDATE " + Params.dbName + ".table SET " +
						  	  "status=" + Table.TABLE_IDLE + ", " +
						  	  "custom_num=NULL, " +
						  	  "category=NULL " +
						  	  "WHERE " +
						  	  "restaurant_id=" + orderInfo.restaurantID + " AND " +
						  	  "table_alias=" + orderInfo.table2.aliasID;
						dbCon.stmt.executeUpdate(sql);
					}
					sql = "UPDATE " + Params.dbName + ".table SET " +
						  "status=" + Table.TABLE_IDLE + ", " +
						  "custom_num=NULL, " +
						  "category=NULL " +
						  "WHERE " +
	  			  		  "restaurant_id=" + orderInfo.restaurantID + " AND " +
						  "table_alias=" + orderInfo.table.aliasID;
					dbCon.stmt.executeUpdate(sql);				
				}				
			}
			
			/**
			 * Update each food's discount to "order_food" table
			 */
			for(int i = 0; i < orderInfo.foods.length; i++){
				sql = "UPDATE " + Params.dbName + ".order_food SET discount=" + orderInfo.foods[i].getDiscount() +
					  " WHERE order_id=" + orderInfo.id + 
					  " AND food_alias=" + orderInfo.foods[i].aliasID;
				dbCon.stmt.executeUpdate(sql);				
			}			
			
			dbCon.conn.commit();		
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw new SQLException(e);
			
		}finally{
			dbCon.conn.setAutoCommit(true);
		}
		
		return orderInfo;

	}
	
	/**
	 * Get the order detail information and get the discount to each food
	 * according to the table id, payment and discount type. 
	 * @param term
	 * 			  the terminal to pay order
	 * @param orderToPay
	 *            the pay order information along with table ID, payment and
	 *            discount type, refer to the class "ReqPayOrder" for more
	 *            details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException
	 *             throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The table associated with this order is idle.<br>
	 *             - The order to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order queryOrder(Terminal term, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			/**
			 * Get the unpaid order id associated with this table
			 */
			orderToPay.id = Util.getUnPaidOrderID(dbCon, QueryTable.exec(dbCon, term, orderToPay.table.aliasID));
			orderToPay.restaurantID = term.restaurant_id;
			
			return queryOrderByID(dbCon, orderToPay);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information and get the discount to each food
	 * according to the table id, payment and discount type. 
	 * 
	 * @param pin
	 *            the pin to this terminal
	 * @param model
	 *            the model to this terminal
	 * @param orderToPay
	 *            the pay order information along with table ID, payment and
	 *            discount type, refer to the class "ReqPayOrder" for more
	 *            details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException
	 *             throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The table associated with this order is idle.<br>
	 *             - The order to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order queryOrder(long pin, short model, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			/**
			 * Get the unpaid order id associated with this table
			 */
			orderToPay.id = Util.getUnPaidOrderID(dbCon, QueryTable.exec(dbCon, term, orderToPay.table.aliasID));
			orderToPay.restaurantID = term.restaurant_id;
			
			return queryOrderByID(dbCon, orderToPay);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information and get the discount to each food 
	 * according to the order id, payment and discount type.
	 * @param conn the database connection
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
	public static Order queryOrderByID(long pin, short model, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryOrderByID(dbCon, orderToPay);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the order detail information and get the discount to each food 
	 * according to the order id, payment and discount type.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon the database connection
	 * @param orderToPay the pay order information along with order ID, payment and discount type,
	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException throws if one of the cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.<br>
	 * 							 - The order to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Order queryOrderByID(DBCon dbCon, Order orderToPay) throws BusinessException, SQLException{
		
		Order orderInfo = QueryOrder.execByID(dbCon, orderToPay.id, QueryOrder.QUERY_TODAY);
		
		/**
		 * If the pay order formation does NOT comprise the discount,
		 * get the discount according the pay type and manner.
		 */
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
							 " AND kitchen_alias=" + orderInfo.foods[i].kitchen.aliasID;
				
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					orderInfo.foods[i].setDiscount(dbCon.rs.getFloat(discount));
				}
				dbCon.rs.close();				
			}
		}
		
		orderInfo.setTotalPrice(orderInfo.calcPriceWithTaste());

		/**
		 * Multiplied by service rate
		 * total = total * (1 + service_rate)
		 */
		float totalPriceWithServRate = orderInfo.getTotalPrice() * (1 + orderToPay.getServiceRate());		
		totalPriceWithServRate = (float)Math.round(totalPriceWithServRate * 100) / 100;
		
		/**
		 * Calculate the total price 2 as below.
		 * total_2 = total * 尾数处理方式
		 */
		float totalPrice2;
		/**
		 * Comparing the minimum cost against total price.
		 * Set the actual price to minimum cost if total price is less than minimum cost.
		 */
		if(totalPriceWithServRate < orderInfo.getMinimumCost()){
			//直接使用最低消费
			totalPrice2 = orderInfo.getMinimumCost();			
		}else{
			Setting setting = QuerySetting.exec(dbCon, orderToPay.restaurantID);
			totalPrice2 = Util.calcByTail(setting.priceTail, totalPriceWithServRate);
		}
		
		orderInfo.setActualPrice(totalPrice2);
		
		orderInfo.restaurantID = orderToPay.restaurantID;
		orderInfo.pay_type = orderToPay.pay_type;
		orderInfo.discount_type = orderToPay.discount_type;
		orderInfo.memberID = orderToPay.memberID; 
		orderInfo.setCashIncome(orderToPay.getCashIncome());
		orderInfo.pay_manner = orderToPay.pay_manner;
		orderInfo.comment = orderToPay.comment;
		orderInfo.setServiceRate(orderToPay.getServiceRate());
			
		return orderInfo;
	}
}
