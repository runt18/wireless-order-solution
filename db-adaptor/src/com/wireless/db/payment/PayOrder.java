package com.wireless.db.payment;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.db.QuerySetting;
import com.wireless.db.QueryTable;
import com.wireless.db.Util;
import com.wireless.db.menuMgr.QueryPricePlanDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.dbObject.Setting;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.Discount;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.PricePlan;
import com.wireless.protocol.ReqPayOrderParser;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class PayOrder {
	
	/**
	 * Perform to pay an order along with the table id and other pay condition referring to {@link ReqPayOrderParser}
	 * @param term
	 * 			the terminal
	 * @param orderToPay
	 * 			the order to pay along with table and other pay condition referring to {@link ReqPayOrderParser}
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
	public static Order execByTable(Terminal term, Order orderToPay, boolean isPaidAgain) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			
			dbCon.connect();
			
			return execByTable(dbCon, term, orderToPay, isPaidAgain);	
			
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Perform to pay an order along with the table id and other pay condition referring to {@link ReqPayOrderParser}
	 * 
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param orderToPay
	 * 			the order to pay along with table and other pay condition referring to {@link ReqPayOrderParser}
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
	public static Order execByTable(DBCon dbCon, Terminal term, Order orderToPay, boolean isPaidAgain) throws BusinessException, SQLException{		
		
		return doPayInternal(dbCon, term, calcByTable(dbCon, term, orderToPay), isPaidAgain);

	}
	
	/**
	 * Perform to pay an order along with the id and other pay condition.
	 * 
	 * @param term
	 * 			the terminal
	 * @param orderToPay
	 * 			the order to pay along with id and other pay condition
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
	public static Order execByID(Terminal term, Order orderToPay, boolean isPaidAgain) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByID(dbCon, term, orderToPay, isPaidAgain);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to pay an order along with the id and other pay condition.
	 * 
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param orderToPay
	 * 			the order to pay along with id and other pay condition
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
		//
		return doPayInternal(dbCon, term, calcByID(dbCon, term, orderToPay), isPaidAgain);
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param orderCalculated
	 * @param isPaidAgain
	 * @return
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	private static Order doPayInternal(DBCon dbCon, Terminal term, Order orderCalculated, boolean isPaidAgain) throws SQLException{
		
		String sql;
		
		//Calculate the sequence id to this order in case of NOT re-paid.
		if(!isPaidAgain){
			sql = "SELECT CASE WHEN MAX(seq_id) IS NULL THEN 1 ELSE MAX(seq_id) + 1 END FROM " + 
				  Params.dbName + ".order WHERE restaurant_id=" + orderCalculated.restaurantID;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				orderCalculated.seqID = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
		}
		
		/**
		 * Put all the INSERT statements into a database transition so as to assure 
		 * the status to both table and order is consistent. 
		 */
		try{
			
			dbCon.conn.setAutoCommit(false);	
			
			if(orderCalculated.isMerged() && orderCalculated.hasChildOrder()){
				for(Order childOrder : orderCalculated.getChildOrder()){
					//Delete each child order from table 'order'
					sql = " DELETE FROM " + Params.dbName + ".order WHERE id = " + childOrder.getId();
					dbCon.stmt.executeUpdate(sql);
					
					//Update the status to the table associated with each child order.
					sql = " UPDATE " + Params.dbName + ".table SET " +
						  " status = " + Table.TABLE_IDLE + ", " +
						  " custom_num = NULL, " +
						  " category = NULL " +
						  " WHERE " +
		  			  	  " table_id = " + childOrder.getDestTbl().getTableId();
					dbCon.stmt.executeUpdate(sql);	
					
					//Update the status to each child order
					sql = " UPDATE " + Params.dbName + ".sub_order SET " +
					      " cancel_price = " + childOrder.getCancelPrice() + ", " +
						  " gift_price = " + childOrder.getGiftPrice() + ", " +
					      " discount_price = " + childOrder.getDiscountPrice() + ", " +
						  " erase_price = " + childOrder.getErasePrice() + ", " +
					      " total_price = " + childOrder.getTotalPrice() + ", " +
						  " actual_price = " + childOrder.getActualPrice() +
						  " WHERE order_id = " + childOrder.getId();
					dbCon.stmt.executeUpdate(sql);
				}
			}
			
			//Update the order.
			sql = " UPDATE " + Params.dbName + ".order SET " +
				  " waiter = (SELECT owner_name FROM " + Params.dbName + ".terminal WHERE pin=" + "0x" + Long.toHexString(term.pin) + " AND (model_id=" + term.modelID + " OR model_id=" + Terminal.MODEL_ADMIN + "))" + " , " +
				  " terminal_model = " + term.modelID + ", " +
				  " terminal_pin = " + term.pin + ", " +
				  " gift_price = " + orderCalculated.getGiftPrice() + ", " +
				  " discount_price = " + orderCalculated.getDiscountPrice() + ", " +
				  " cancel_price = " + orderCalculated.getCancelPrice() + ", " +
				  " repaid_price =  " + orderCalculated.getRepaidPrice() + ", " +
				  " erase_price = " + orderCalculated.getErasePrice() + ", " +
				  " total_price = " + orderCalculated.getTotalPrice() + ", " + 
				  " total_price_2 = " + orderCalculated.getActualPrice() + ", " +
				  " type = " + orderCalculated.payManner + ", " + 
				  " discount_id = " + orderCalculated.getDiscount().discountID + ", " +
				  " price_plan_id = " + (orderCalculated.hasPricePlan() ? orderCalculated.getPricePlan().getId() : "price_plan_id") + ", " +
				  " service_rate = " + orderCalculated.getServiceRate() + ", " +
				  " status = " + (isPaidAgain ? Order.STATUS_REPAID : Order.STATUS_PAID) + ", " + 
				  (isPaidAgain ? "" : (" seq_id = " + orderCalculated.seqID + ", ")) +
			   	  (isPaidAgain ? "" : " order_date = NOW(), ") + 
				  " comment = " + (orderCalculated.comment == null ? " NULL " : ("'" + orderCalculated.comment + "'")) + 
				  " WHERE " +
				  " id = " + orderCalculated.getId();
				
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
				if(orderCalculated.isJoined() || orderCalculated.isTakeout()){
					sql = " DELETE FROM " + Params.dbName + ".table WHERE " +
						  " restaurant_id = " + orderCalculated.restaurantID + " AND " +
						  " table_alias = " + orderCalculated.getDestTbl().getAliasId();
					dbCon.stmt.executeUpdate(sql);
					
				}else{

					sql = " UPDATE " + Params.dbName + ".table SET " +
						  " status = " + Table.TABLE_IDLE + ", " +
						  " custom_num = NULL, " +
						  " category = NULL " +
						  " WHERE " +
	  			  		  " restaurant_id = " + orderCalculated.restaurantID + " AND " +
						  " table_alias = " + orderCalculated.getDestTbl().getAliasId();
					dbCon.stmt.executeUpdate(sql);				
				}				
			}
			
			/**
			 * Update each food's discount & unit price to "order_food" table
			 */
			for(int i = 0; i < orderCalculated.foods.length; i++){
				sql = " UPDATE " + Params.dbName + ".order_food " +
					  " SET " +
					  " discount = " + orderCalculated.foods[i].getDiscount() + ", " +
					  " unit_price = " + orderCalculated.foods[i].getPrice() +
					  " WHERE order_id = " + orderCalculated.getId() + 
					  " AND food_alias = " + orderCalculated.foods[i].getAliasId();
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
		
		return orderCalculated;
	}
	
//	/**
//	 * Get the order detail information and get the discount to each food
//	 * according to the table id, payment and discount type. 
//	 * @param term
//	 * 			  the terminal to pay order
//	 * @param orderToPay
//	 *            the pay order information along with table ID, payment and
//	 *            discount type, refer to the class "ReqPayOrder" for more
//	 *            details on what information it contains.
//	 * @return Order completed pay order information to paid order
//	 * @throws BusinessException
//	 *             throws if one of the cases below.<br>
//	 *             - The terminal is NOT attached to any restaurant.<br>
//	 *             - The terminal is expired.<br>
//	 *             - The table associated with this order is idle.<br>
//	 *             - The order to query does NOT exist.
//	 * @throws SQLException
//	 *             throws if fail to execute any SQL statement
//	 */
//	public static Order queryOrder(Terminal term, Order orderToPay) throws BusinessException, SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			/**
//			 * Get the unpaid order id associated with this table
//			 */
//			orderToPay.setId(QueryOrderDao.getOrderIdByUnPaidTable(dbCon, QueryTable.exec(dbCon, term, orderToPay.getDestTbl().getAliasId()))[0]);
//			orderToPay.restaurantID = term.restaurantID;
//			
//			return queryOrderByID(dbCon, term, orderToPay);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
	
//	/**
//	 * Get the order detail information and get the discount to each food
//	 * according to the table id, payment and discount type. 
//	 * 
//	 * @param pin
//	 *            the pin to this terminal
//	 * @param model
//	 *            the model to this terminal
//	 * @param orderToPay
//	 *            the pay order information along with table ID, payment and
//	 *            discount type, refer to the class "ReqPayOrder" for more
//	 *            details on what information it contains.
//	 * @return Order completed pay order information to paid order
//	 * @throws BusinessException
//	 *             throws if one of the cases below.<br>
//	 *             - The terminal is NOT attached to any restaurant.<br>
//	 *             - The terminal is expired.<br>
//	 *             - The table associated with this order is idle.<br>
//	 *             - The order to query does NOT exist.
//	 * @throws SQLException
//	 *             throws if fail to execute any SQL statement
//	 */
//	public static Order queryOrder(long pin, short model, Order orderToPay) throws BusinessException, SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			
//			Terminal term = VerifyPin.exec(dbCon, pin, model);
//			/**
//			 * Get the unpaid order id associated with this table
//			 */
//			orderToPay.setId(QueryOrderDao.getOrderIdByUnPaidTable(dbCon, QueryTable.exec(dbCon, term, orderToPay.getDestTbl().getAliasId()))[0]);
//			orderToPay.restaurantID = term.restaurantID;
//			
//			return queryOrderByID(dbCon, term, orderToPay);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
//	
//	/**
//	 * Get the order detail information and get the discount to each food 
//	 * according to the order id, payment and discount type.
//	 * @param conn the database connection
//	 * @param pin the pin to this terminal
//	 * @param model the model to this terminal
//	 * @param orderToPay the pay order information along with order ID, payment and discount type,
//	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
//	 * @return Order completed pay order information to paid order
//	 * @throws BusinessException throws if one of the cases below.<br>
//	 * 							 - The terminal is NOT attached to any restaurant.<br>
//	 * 							 - The terminal is expired.<br>
//	 * 							 - The order to query does NOT exist.
//	 * @throws SQLException throws if fail to execute any SQL statement
//	 */
//	public static Order queryOrderByID(long pin, short model, Terminal term, Order orderToPay) throws BusinessException, SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return queryOrderByID(dbCon, term, orderToPay);
//		}finally{
//			dbCon.disconnect();
//		}
//	}
//	
//	/**
//	 * Get the order detail information and get the discount to each food 
//	 * according to the order id, payment and discount type.
//	 * Note that the database should be connected before invoking this method.
//	 * @param dbCon the database connection
//	 * @param orderToPay the pay order information along with order ID, payment and discount type,
//	 * 					 refer to the class "ReqPayOrder" for more details on what information it contains.
//	 * @return Order completed pay order information to paid order
//	 * @throws BusinessException throws if one of the cases below.<br>
//	 * 							 - The terminal is NOT attached to any restaurant.<br>
//	 * 							 - The terminal is expired.<br>
//	 * 							 - The order to query does NOT exist.
//	 * @throws SQLException throws if fail to execute any SQL statement
//	 */
//	public static Order queryOrderByID(DBCon dbCon, Terminal term, Order orderToPay) throws BusinessException, SQLException{
//
//		//Get the detail to this order.
//		Order orderInfo = QueryOrderDao.execByID(dbCon, orderToPay.getId(), QueryOrderDao.QUERY_TODAY);
//		
//		//Get the discount to this order.
//		Discount[] discount = QueryMenu.queryDiscounts(dbCon, 
//													   " AND DIST.restaurant_id=" + term.restaurantID +
//													   " AND DIST.discount_id=" + orderToPay.getDiscount().discountID,
//													   null);
//		if(discount.length > 0){
//			orderInfo.setDiscount(discount[0]);
//		}
//
//		//Get the details if the requested plan is set.
//		//Otherwise use the price plan which is in used instead.
//		if(orderToPay.hasPricePlan()){
//			PricePlan[] pricePlans = QueryPricePlanDao.exec(dbCon, " AND price_plan_id = " + orderToPay.getPricePlan().getId(), null);
//			if(pricePlans.length > 0){
//				orderToPay.setPricePlan(pricePlans[0]);
//			}
//		}else{
//			PricePlan[] pricePlans = QueryPricePlanDao.exec(dbCon, " AND status = " + PricePlan.IN_USE + " AND restaurant_id = " + term.restaurantID, null);
//			if(pricePlans.length > 0){
//				orderToPay.setPricePlan(pricePlans[0]);
//			}
//		}
//		
//		//Check to see whether the requested price plan is same as before.
//		if(!orderToPay.getPricePlan().equals(orderInfo.getPricePlan())){
//			
//			//Get the price belongs to requested plan to each order food(except the temporary food) if different from before.
//			orderInfo.setPricePlan(orderToPay.getPricePlan());
//			
//			for(int i = 0; i < orderInfo.foods.length; i++){
//				if(!orderInfo.foods[i].isTemporary){
//					String sql;
//					sql = " SELECT " +
//						  " unit_price " + " FROM " + Params.dbName + ".food_price_plan" +
//						  " WHERE " + 
//						  " price_plan_id = " + orderInfo.getPricePlan().getId() +
//						  " AND " +
//						  " food_id = " + orderInfo.foods[i].foodID;
//					dbCon.rs = dbCon.stmt.executeQuery(sql);
//					if(dbCon.rs.next()){
//						orderInfo.foods[i].setPrice(dbCon.rs.getFloat("unit_price"));
//					}
//					dbCon.rs.close();
//				}
//			}
//		}
//		
//		orderInfo.setErasePrice(orderToPay.getErasePrice());
//		orderInfo.restaurantID = orderToPay.restaurantID;
//		orderInfo.payType = orderToPay.payType;
//		orderInfo.memberID = orderToPay.memberID; 
//		orderInfo.setCashIncome(orderToPay.getCashIncome());
//		orderInfo.payManner = orderToPay.payManner;
//		orderInfo.comment = orderToPay.comment;
//		orderInfo.setServiceRate(orderToPay.getServiceRate());
//		
//		String sql;
//		
//		//Calculate the cancel price to this order.
//		sql = " SELECT " + 
//			  " ABS(ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2)) AS cancel_price " +
//			  " FROM " +
//			  Params.dbName + ".order_food OF" + 
//			  " JOIN " + 
//			  Params.dbName + ".taste_group TG" +
//			  " ON " + " OF.taste_group_id = TG.taste_group_id " +
//			  " WHERE " +
//			  " OF.order_count < 0 " + " AND " + " OF.order_id = " + orderInfo.getId();
//		
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		if(dbCon.rs.next()){
//			orderInfo.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
//		}
//		
//		//Calculate the repaid price to this order.
//		sql = " SELECT " + 
//			  " ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2) AS repaid_price " +
//			  " FROM " +
//			  Params.dbName + ".order_food OF" + 
//			  " JOIN " + 
//			  Params.dbName + ".taste_group TG" +
//			  " ON " + " OF.taste_group_id = TG.taste_group_id " +
//			  " WHERE " +
//			  " OF.is_paid = 1 " + " AND " + " OF.order_id = " + orderInfo.getId();
//			
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		if(dbCon.rs.next()){
//			orderInfo.setRepaidPrice(dbCon.rs.getFloat("repaid_price"));
//		}
//		
//		//Get the total price .
//		float totalPrice = orderInfo.calcTotalPrice();
//		
//		orderInfo.setTotalPrice(totalPrice);		
//		
//		//Get the setting.
//		Setting setting = QuerySetting.exec(dbCon, orderToPay.restaurantID);
//		
//		//Calculate the actual price.
//		float actualPrice = 0;
//		//Comparing the minimum cost against total price.
//		//Set the actual price to minimum cost if total price does NOT reach minimum cost.
////		if(totalPrice < orderInfo.getMinimumCost()){
////			actualPrice = orderInfo.getMinimumCost();			
////		}else{
////			actualPrice = Util.calcByTail(setting, totalPrice);
////		}
//		
//		//Check to see whether has erase quota and the order exceed the erase quota.
//		if(setting.hasEraseQuota() && orderToPay.getErasePrice() > setting.getEraseQuota()){
//			throw new BusinessException("The order(id=" + orderToPay.getId() + ") exceeds the erase quota.", ErrorCode.EXCEED_GIFT_QUOTA);
//		}else{
//			actualPrice = actualPrice - orderInfo.getErasePrice();
//			actualPrice = actualPrice > 0 ? actualPrice : 0;
//		}			
//		
//		orderInfo.setActualPrice(actualPrice);
//		
//		return orderInfo;
//	}
	
	/**
	 * Calculate the details to order according to the table and other condition referring to {@link ReqPayOrderParser}
	 * @param term
	 * 			the terminal 
	 * @param orderToPay
	 * 			the order with table and other condition referring to {@link ReqPayOrderParser}
	 * @return the order after calculated
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - The order associated with table to be paid does NOT exist.<br>
	 * 			2 - The erase price exceeds the quota.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static Order calcByTable(Terminal term, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcByTable(dbCon, term, orderToPay);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the details to order according to the specific table 
	 * and other condition referring to {@link ReqPayOrderParser}
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param orderToPay
	 * 			the order along with table and other condition referring to {@link ReqPayOrderParser}
	 * @return the order result after calculated
	 * @throws BusinessException
	 * 			Throws if one of cases below.<br>
	 * 			1 - The order associated with table to be paid does NOT exist.<br>
	 * 			2 - The erase price exceeds the quota.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static Order calcByTable(DBCon dbCon, Terminal term, Order orderToPay) throws BusinessException, SQLException{
		
		//Get the details of table to be calculated.
		Table tblToCalc = QueryTable.exec(dbCon, term, orderToPay.getDestTbl().getAliasId());
		
		//If the table is merged, get its parent order.
		//Otherwise get the order of its own.
		int[] unpaidId = QueryOrderDao.getOrderIdByUnPaidTable(dbCon, tblToCalc);
		if(unpaidId.length > 1){
			orderToPay.setId(unpaidId[1]);			
		}else{
			orderToPay.setId(unpaidId[0]);			
		}
		
		return calcByID(dbCon, term, orderToPay);		

	}
	
	/**
	 * Calculate the details to the order according to id and
	 * other condition.
	 * @param term
	 * 			the terminal
	 * @param orderToPay
	 * 			the order along with id and other condition
	 * @return the order result after calculated
	 * @throws BusinessException
	 * 			Throws if the order to this id does NOT exist.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static Order calcByID(Terminal term, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcByID(dbCon, term, orderToPay);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the details to the order according to id and
	 * other condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param orderToPay
	 * 			the order along with id and other condition
	 * @return the order result after calculated
	 * @throws BusinessException
	 * 			Throws if the order to this id does NOT exist.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static Order calcByID(DBCon dbCon, Terminal term, Order orderToPay) throws BusinessException, SQLException{
		
		String sql;
		
		//Get all the details of order to be calculated.
		Order orderToCalc = QueryOrderDao.execByID(orderToPay.getId(), QueryOrderDao.QUERY_TODAY);
		
		PricePlan oriPricePlan = orderToCalc.getPricePlan();
		
		//Set the order calculate parameters.
		setOrderCalcParams(orderToCalc, orderToPay);
		
		if(orderToCalc.isMerged() && orderToCalc.hasChildOrder()){
			Order[] childOrders = orderToCalc.getChildOrder();
			for(int i = 0; i < childOrders.length; i++){
				//Set the calculate parameters to each child order.
				setOrderCalcParams(childOrders[i], orderToPay);
				//Calculate each child order.
				childOrders[i] = calcByID(dbCon, term, childOrders[i]);
				//Accumulate the custom number
				orderToCalc.setCustomNum(orderToCalc.getCustomNum() + childOrders[i].getCustomNum());
				//Accumulate the discount price.
				orderToCalc.setDiscountPrice(orderToCalc.getDiscountPrice() + childOrders[i].getDiscountPrice());
				//Accumulate the gift price.
				orderToCalc.setGiftPrice(orderToCalc.getGiftPrice() + childOrders[i].getGiftPrice());
				//Accumulate the cancel price.
				orderToCalc.setCancelPrice(orderToCalc.getCancelPrice() + childOrders[i].getCancelPrice());
				//Accumulate the repaid price.
				orderToCalc.setRepaidPrice(orderToCalc.getRepaidPrice() + childOrders[i].getRepaidPrice());
				//Accumulate the total price.
				orderToCalc.setTotalPrice(orderToCalc.getTotalPrice() + childOrders[i].getTotalPrice());
				//Accumulate the actual price.
				orderToCalc.setActualPrice(orderToCalc.getActualPrice() + childOrders[i].getActualPrice());
			}
			
		}else{
			//Get the discount to this order.
			Discount[] discount = QueryMenu.queryDiscounts(dbCon, 
														   " AND DIST.restaurant_id = " + term.restaurantID +
														   " AND DIST.discount_id = " + orderToCalc.getDiscount().discountID,
														   null);
			if(discount.length > 0){
				orderToCalc.setDiscount(discount[0]);
			}

			//Get the details if the requested plan is set.
			//Otherwise use the price plan which is in used instead.
			if(orderToCalc.hasPricePlan()){
				PricePlan[] pricePlans = QueryPricePlanDao.exec(dbCon, " AND price_plan_id = " + orderToCalc.getPricePlan().getId(), null);
				if(pricePlans.length > 0){
					orderToCalc.setPricePlan(pricePlans[0]);
				}
			}else{
				PricePlan[] pricePlans = QueryPricePlanDao.exec(dbCon, " AND status = " + PricePlan.IN_USE + " AND restaurant_id = " + term.restaurantID, null);
				if(pricePlans.length > 0){
					orderToCalc.setPricePlan(pricePlans[0]);
				}
			}
			
			//Check to see whether the requested price plan is same as before.
			if(!orderToCalc.getPricePlan().equals(oriPricePlan)){
				
				//Get the price belongs to requested plan to each order food(except the temporary food) if different from before.				
				for(int i = 0; i < orderToCalc.foods.length; i++){
					if(!orderToCalc.foods[i].isTemporary){
						sql = " SELECT " +
							  " unit_price " + " FROM " + Params.dbName + ".food_price_plan" +
							  " WHERE " + 
							  " price_plan_id = " + orderToCalc.getPricePlan().getId() +
							  " AND " +
							  " food_id = " + orderToCalc.foods[i].foodID;
						dbCon.rs = dbCon.stmt.executeQuery(sql);
						if(dbCon.rs.next()){
							orderToCalc.foods[i].setPrice(dbCon.rs.getFloat("unit_price"));
						}
						dbCon.rs.close();
					}
				}
			}
			
			float cancelPrice = 0;
			//Calculate the cancel price to this order.
			sql = " SELECT " + 
				  " ABS(ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2)) AS cancel_price " +
				  " FROM " +
				  Params.dbName + ".order_food OF" + 
				  " JOIN " + 
				  Params.dbName + ".taste_group TG" +
				  " ON " + " OF.taste_group_id = TG.taste_group_id " +
				  " WHERE " +
				  " OF.order_count < 0 " + " AND " + " OF.order_id = " + orderToCalc.getId();
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				cancelPrice = dbCon.rs.getFloat("cancel_price");
			}
			
			float repaidPrice = 0;
			//Calculate the repaid price to this order.
			sql = " SELECT " + 
				  " ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2) AS repaid_price " +
				  " FROM " +
				  Params.dbName + ".order_food OF" + 
				  " JOIN " + 
				  Params.dbName + ".taste_group TG" +
				  " ON " + " OF.taste_group_id = TG.taste_group_id " +
				  " WHERE " +
				  " OF.is_paid = 1 " + " AND " + " OF.order_id = " + orderToCalc.getId();
				
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				repaidPrice = dbCon.rs.getFloat("repaid_price");
			}
			
			//Get the total price .
			float totalPrice = orderToCalc.calcTotalPrice();			
			
			//Get the setting.
			Setting setting = QuerySetting.exec(dbCon, orderToCalc.restaurantID);
			
			//Calculate the actual price.
			float actualPrice;
			
			//Comparing the minimum cost against total price.
			//Set the actual price to minimum cost if total price does NOT reach minimum cost.
			float miniCost = orderToCalc.getDestTbl().getMinimumCost();
			if(totalPrice < miniCost){
				actualPrice = miniCost;			
			}else{
				actualPrice = Util.calcByTail(setting, totalPrice);
			}
			
			//Check to see whether has erase quota and the order exceed the erase quota.
			if(setting.hasEraseQuota() && orderToCalc.getErasePrice() > setting.getEraseQuota()){
				throw new BusinessException("The order(id=" + orderToCalc.getId() + ") exceeds the erase quota.", ErrorCode.EXCEED_GIFT_QUOTA);
			}else{
				actualPrice = actualPrice - orderToCalc.getErasePrice();
				actualPrice = actualPrice > 0 ? actualPrice : 0;
			}			
			
			//Set the cancel price.
			orderToCalc.setCancelPrice(cancelPrice);
			//Set the repaid price.
			orderToCalc.setRepaidPrice(repaidPrice);
			//Set the gift price.
			orderToCalc.setGiftPrice(orderToCalc.calcGiftPrice());			
			//Set the discount price.
			orderToCalc.setDiscountPrice(orderToCalc.calcDiscountPrice());
			//Set the total price.
			orderToCalc.setTotalPrice(totalPrice);
			//Set the actual price
			orderToCalc.setActualPrice(actualPrice);
			
		}
		
		return orderToCalc;
	}
	
	private static void setOrderCalcParams(Order orderToCalc, Order calcParams){
		orderToCalc.setErasePrice(calcParams.getErasePrice());
		orderToCalc.setDiscount(calcParams.getDiscount());
		orderToCalc.setPricePlan(calcParams.getPricePlan());
		orderToCalc.payType = calcParams.payType;
		orderToCalc.memberID = calcParams.memberID; 
		orderToCalc.setCashIncome(calcParams.getCashIncome());
		orderToCalc.payManner = calcParams.payManner;
		orderToCalc.comment = calcParams.comment;
		orderToCalc.setServiceRate(calcParams.getServiceRate());
	}
	
}
