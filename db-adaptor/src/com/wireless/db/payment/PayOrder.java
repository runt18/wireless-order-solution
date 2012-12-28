package com.wireless.db.payment;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.db.QuerySetting;
import com.wireless.db.QueryTable;
import com.wireless.db.Util;
import com.wireless.db.VerifyPin;
import com.wireless.db.menuMgr.QueryPricePlanDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.dbObject.Setting;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Discount;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Member;
import com.wireless.protocol.Order;
import com.wireless.protocol.PricePlan;
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
			orderToPay.setId(QueryOrderDao.getOrderIdByUnPaidTable(dbCon, QueryTable.exec(dbCon, term, orderToPay.getDestTbl().getAliasId()))[0]);
			orderToPay.restaurantID = term.restaurantID;
			
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
			orderToPay.setId(QueryOrderDao.getOrderIdByUnPaidTable(dbCon, QueryTable.exec(dbCon, term, orderToPay.getDestTbl().getAliasId()))[0]);
			orderToPay.restaurantID = term.restaurantID;
			
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
			orderToPay.restaurantID = term.restaurantID;
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
		Order orderInfo = queryOrderByID(dbCon, term, orderToPay);			
			
		/**
		 * Get the member info if the pay type for member
		 */
		Member member = null;
		if(orderInfo.payType == Order.PAY_MEMBER){
			if(orderInfo.memberID != null){
				member = QueryMember.exec(dbCon, orderInfo.restaurantID, orderInfo.memberID);
			}else{
				member = new Member("", "", "", new Float(0));
			}
		}
			
		
		String sql;
		//Calculate the sequence id to this order in case of not been paid again.
		if(!isPaidAgain){
			sql = "SELECT CASE WHEN MAX(seq_id) IS NULL THEN 1 ELSE MAX(seq_id) + 1 END FROM " + 
				  Params.dbName + ".order WHERE restaurant_id=" + orderInfo.restaurantID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				orderInfo.seqID = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
		}

		//Calculate the cancel price to this order.
		float cancelPrice = 0;
		sql = " SELECT " + 
			  " ABS(ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2)) AS cancel_price " +
			  " FROM " +
			  Params.dbName + ".order_food OF" + 
			  " JOIN " + 
			  Params.dbName + ".taste_group TG" +
			  " ON " + " OF.taste_group_id = TG.taste_group_id " +
			  " WHERE " +
			  " OF.order_count < 0 " + " AND " + " OF.order_id = " + orderInfo.getId();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			cancelPrice = dbCon.rs.getFloat("cancel_price");
		}
		
		//Calculate the repaid price to this order.
		float repaidPrice = 0;
		sql = " SELECT " + 
			  " ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2) AS repaid_price " +
			  " FROM " +
			  Params.dbName + ".order_food OF" + 
			  " JOIN " + 
			  Params.dbName + ".taste_group TG" +
			  " ON " + " OF.taste_group_id = TG.taste_group_id " +
			  " WHERE " +
			  " OF.is_paid = 1 " + " AND " + " OF.order_id = " + orderInfo.getId();
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			repaidPrice = dbCon.rs.getFloat("repaid_price");
		}
		
		//Get the setting.
		Setting setting = QuerySetting.exec(dbCon, orderToPay.restaurantID);
		
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
			if(orderInfo.payType == Order.PAY_MEMBER && orderInfo.payManner == Order.MANNER_MEMBER && orderInfo.memberID != null){
				sql = "UPDATE " + Params.dbName + ".member SET balance=balance - " + orderInfo.getActualPrice() +
					  " + " + orderInfo.getActualPrice() + " * exchange_rate" + 
					  " WHERE restaurant_id=" + orderInfo.restaurantID +
					  " AND alias_id=" + member.alias_id;
				
				dbCon.stmt.executeUpdate(sql);
			}		

			//Get the total price .
			float totalPrice = orderInfo.calcTotalPrice();
			
			//Calculate the actual price.
			float actualPrice;
			//Comparing the minimum cost against total price.
			//Set the actual price to minimum cost if total price does NOT reach minimum cost.
			if(totalPrice < orderInfo.getMinimumCost()){
				actualPrice = orderInfo.getMinimumCost();			
			}else{
				actualPrice = Util.calcByTail(setting, totalPrice);
			}
			
			//Check to see whether has erase quota and the order exceed the erase quota.
			if(setting.hasEraseQuota() && orderToPay.getErasePrice() > setting.getEraseQuota()){
				throw new BusinessException("The order(id=" + orderToPay.getId() + ") exceeds the erase quota.", ErrorCode.EXCEED_GIFT_QUOTA);
			}else{
				actualPrice = actualPrice - orderInfo.getErasePrice();
				actualPrice = actualPrice > 0 ? actualPrice : 0;
			}			
			
			orderInfo.setActualPrice(actualPrice);
			
			//Update the order.
			sql = " UPDATE " + Params.dbName + ".order SET " +
				  " waiter = (SELECT owner_name FROM " + Params.dbName + ".terminal WHERE pin=" + "0x" + Long.toHexString(term.pin) + " AND (model_id=" + term.modelID + " OR model_id=" + Terminal.MODEL_ADMIN + "))" + " , " +
				  " terminal_model = " + term.modelID + ", " +
				  " terminal_pin = " + term.pin + ", " +
				  " gift_price = " + orderInfo.calcGiftPrice() + ", " +
				  " discount_price = " + orderInfo.calcDiscountPrice() + ", " +
				  " cancel_price = " + cancelPrice + ", " +
				  " repaid_price =  " + repaidPrice + ", " +
				  " erase_price = " + orderInfo.getErasePrice() + ", " +
				  " total_price = " + totalPrice + ", " + 
				  " total_price_2 = " + actualPrice + ", " +
				  " type = " + orderInfo.payManner + ", " + 
				  " discount_id = " + orderInfo.getDiscount().discountID + ", " +
				  " price_plan_id = " + orderInfo.getPricePlan().getId() + ", " +
				  " service_rate = " + orderInfo.getServiceRate() + ", " +
				  " status = " + (isPaidAgain ? Order.STATUS_REPAID : Order.STATUS_PAID) + ", " + 
				  (isPaidAgain ? "" : (" seq_id = " + orderInfo.seqID + ", ")) +
			   	  (isPaidAgain ? "" : " order_date = NOW(), ") + 
				  (orderInfo.comment == null ? "" : " comment= " + "'" + orderInfo.comment + "'" + ", ") +
				  (member != null ? " member_id= '" + member.alias_id + "', member='" + member.name + "'" : " member_id=NULL, member=NULL ") + 
				  " WHERE " +
				  " id = " + orderInfo.getId();
				
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
				if(orderInfo.isJoined() || orderInfo.isTakeout()){
					sql = " DELETE FROM " + Params.dbName + ".table WHERE " +
						  " restaurant_id = " + orderInfo.restaurantID + " AND " +
						  " table_alias = " + orderInfo.getDestTbl().getAliasId();
					dbCon.stmt.executeUpdate(sql);
					
				}else{

					sql = " UPDATE " + Params.dbName + ".table SET " +
						  " status = " + Table.TABLE_IDLE + ", " +
						  " custom_num = NULL, " +
						  " category = NULL " +
						  " WHERE " +
	  			  		  " restaurant_id = " + orderInfo.restaurantID + " AND " +
						  " table_alias = " + orderInfo.getDestTbl().getAliasId();
					dbCon.stmt.executeUpdate(sql);				
				}				
			}
			
			/**
			 * Update each food's discount & unit price to "order_food" table
			 */
			for(int i = 0; i < orderInfo.foods.length; i++){
				sql = " UPDATE " + Params.dbName + ".order_food " +
					  " SET " +
					  " discount = " + orderInfo.foods[i].getDiscount() + ", " +
					  " unit_price = " + orderInfo.foods[i].getPrice() +
					  " WHERE order_id = " + orderInfo.getId() + 
					  " AND food_alias = " + orderInfo.foods[i].getAliasId();
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
			orderToPay.setId(QueryOrderDao.getOrderIdByUnPaidTable(dbCon, QueryTable.exec(dbCon, term, orderToPay.getDestTbl().getAliasId()))[0]);
			orderToPay.restaurantID = term.restaurantID;
			
			return queryOrderByID(dbCon, term, orderToPay);
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
			orderToPay.setId(QueryOrderDao.getOrderIdByUnPaidTable(dbCon, QueryTable.exec(dbCon, term, orderToPay.getDestTbl().getAliasId()))[0]);
			orderToPay.restaurantID = term.restaurantID;
			
			return queryOrderByID(dbCon, term, orderToPay);
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
	public static Order queryOrderByID(long pin, short model, Terminal term, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryOrderByID(dbCon, term, orderToPay);
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
	public static Order queryOrderByID(DBCon dbCon, Terminal term, Order orderToPay) throws BusinessException, SQLException{

		//Get the detail to this order.
		Order orderInfo = QueryOrderDao.execByID(dbCon, orderToPay.getId(), QueryOrderDao.QUERY_TODAY);
		
		//Get the discount to this order.
		Discount[] discount = QueryMenu.queryDiscounts(dbCon, 
													   " AND DIST.restaurant_id=" + term.restaurantID +
													   " AND DIST.discount_id=" + orderToPay.getDiscount().discountID,
													   null);
		if(discount.length > 0){
			orderInfo.setDiscount(discount[0]);
		}

		//Get the details if the requested plan is set.
		//Otherwise use the price plan which is in used instead.
		if(orderToPay.hasPricePlan()){
			PricePlan[] pricePlans = QueryPricePlanDao.exec(dbCon, " AND price_plan_id = " + orderToPay.getPricePlan().getId(), null);
			if(pricePlans.length > 0){
				orderToPay.setPricePlan(pricePlans[0]);
			}
		}else{
			PricePlan[] pricePlans = QueryPricePlanDao.exec(dbCon, " AND status = " + PricePlan.IN_USE + " AND restaurant_id = " + term.restaurantID, null);
			if(pricePlans.length > 0){
				orderToPay.setPricePlan(pricePlans[0]);
			}
		}
		
		//Check to see whether the requested price plan is same as before.
		if(!orderToPay.getPricePlan().equals(orderInfo.getPricePlan())){
			
			//Get the price belongs to requested plan to each order food(except the temporary food) if different from before.
			orderInfo.setPricePlan(orderToPay.getPricePlan());
			
			for(int i = 0; i < orderInfo.foods.length; i++){
				if(!orderInfo.foods[i].isTemporary){
					String sql;
					sql = " SELECT " +
						  " unit_price " + " FROM " + Params.dbName + ".food_price_plan" +
						  " WHERE " + 
						  " price_plan_id = " + orderInfo.getPricePlan().getId() +
						  " AND " +
						  " food_id = " + orderInfo.foods[i].foodID;
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					if(dbCon.rs.next()){
						orderInfo.foods[i].setPrice(dbCon.rs.getFloat("unit_price"));
					}
					dbCon.rs.close();
				}
			}
		}
		
		orderInfo.setErasePrice(orderToPay.getErasePrice());
		orderInfo.restaurantID = orderToPay.restaurantID;
		orderInfo.payType = orderToPay.payType;
		orderInfo.memberID = orderToPay.memberID; 
		orderInfo.setCashIncome(orderToPay.getCashIncome());
		orderInfo.payManner = orderToPay.payManner;
		orderInfo.comment = orderToPay.comment;
		orderInfo.setServiceRate(orderToPay.getServiceRate());
			
		return orderInfo;
	}
}
