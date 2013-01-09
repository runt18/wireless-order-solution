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
import com.wireless.protocol.OrderFood;
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
	 *             Throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The order to be paid repeat.<br>
	 *             - The order to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order execByID(DBCon dbCon, Terminal term, Order orderToPay, boolean isPaidAgain) throws BusinessException, SQLException{
		int customNum = orderToPay.getCustomNum();
		Order orderCalculated = calcByID(dbCon, term, orderToPay);
		orderCalculated.setCustomNum(customNum);
		if(!isPaidAgain && orderCalculated.isPaid()){
			throw new BusinessException("The order(id = " + orderCalculated.getId() + ") can be paid repeatly.", ErrorCode.ORDER_BE_REPEAT_PAID);
		}
		return doPayInternal(dbCon, term, orderCalculated, isPaidAgain);
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
					
					//Update the unit price and discount to every food of each child order.
					for(OrderFood food : childOrder.foods){
						sql = " UPDATE " + Params.dbName + ".order_food " +
							  " SET " +
							  " discount = " + food.getDiscount() + ", " +
							  " unit_price = " + food.getPrice() +
							  " WHERE order_id = " + childOrder.getId() + 
							  " AND food_alias = " + food.getAliasId();
						dbCon.stmt.executeUpdate(sql);
					}
				}
				
			}else if(orderCalculated.isMergedChild()){
				//Delete the child order from 'sub_order'
				sql = " DELETE FROM " + Params.dbName + ".sub_order WHERE order_id = " + orderCalculated.getId();
				dbCon.stmt.executeUpdate(sql);
				
				//Get its parent order id
				sql = " SELECT order_id FROM " + Params.dbName + ".order_group WHERE sub_order_id = " + orderCalculated.getId();
				int parentOrderId = 0;
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					parentOrderId = dbCon.rs.getInt("order_id");
				}
				
				//Delete the relationship from 'order_group'
				sql = " DELETE FROM " + Params.dbName + ".order_group WHERE sub_order_id = " + orderCalculated.getId();
				dbCon.stmt.executeUpdate(sql);
				
				//Delete its parent in case of empty.
				sql = " DELETE FROM " + Params.dbName + ".order " +
					  " WHERE id = " + parentOrderId + 
					  " AND NOT EXISTS (" + " SELECT * FROM " + Params.dbName + ".order_group WHERE order_id = " + parentOrderId + ")";
				dbCon.stmt.executeUpdate(sql);
				
				//Set the status to normal.
				orderCalculated.setCategory(Order.CATE_NORMAL);
			}
			
			//Update the order.
			sql = " UPDATE " + Params.dbName + ".order SET " +
				  " waiter = (SELECT owner_name FROM " + Params.dbName + ".terminal WHERE pin=" + "0x" + Long.toHexString(term.pin) + " AND (model_id=" + term.modelID + " OR model_id=" + Terminal.MODEL_ADMIN + "))" + " , " +
				  " terminal_model = " + term.modelID + ", " +
				  " terminal_pin = " + term.pin + ", " +
				  " category = " + orderCalculated.getCategory() + ", " +
				  " gift_price = " + orderCalculated.getGiftPrice() + ", " +
				  " discount_price = " + orderCalculated.getDiscountPrice() + ", " +
				  " cancel_price = " + orderCalculated.getCancelPrice() + ", " +
				  " repaid_price =  " + orderCalculated.getRepaidPrice() + ", " +
				  " erase_price = " + orderCalculated.getErasePrice() + ", " +
				  " total_price = " + orderCalculated.getTotalPrice() + ", " + 
				  " total_price_2 = " + orderCalculated.getActualPrice() + ", " +
				  " custom_num = " + orderCalculated.getCustomNum() + ", " +
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
				
				sql = " UPDATE " + Params.dbName + ".table SET " +
					  " status = " + Table.TABLE_IDLE + ", " +
					  " custom_num = NULL, " +
					  " category = NULL " +
					  " WHERE " +
  			  		  " restaurant_id = " + orderCalculated.restaurantID + " AND " +
					  " table_alias = " + orderCalculated.getDestTbl().getAliasId();
				dbCon.stmt.executeUpdate(sql);				
						
			}
			
			/**
			 * Update each food's discount & unit price to "order_food" table
			 */
			for(OrderFood food : orderCalculated.foods){
				sql = " UPDATE " + Params.dbName + ".order_food " +
					  " SET " +
					  " discount = " + food.getDiscount() + ", " +
					  " unit_price = " + food.getPrice() +
					  " WHERE order_id = " + orderCalculated.getId() + 
					  " AND food_alias = " + food.getAliasId();
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
	
	/**
	 * Calculate the details to order according to the table and other condition referring to {@link ReqPayOrderParser} 
	 * regardless of merged status.
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
	 * and other condition referring to {@link ReqPayOrderParser} regardless of merged status
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
		
		orderToPay.setId(QueryOrderDao.getOrderIdByUnPaidTable(dbCon, QueryTable.exec(dbCon, term, orderToPay.getDestTbl().getAliasId()))[0]);			
		
		return calcByID(dbCon, term, orderToPay);		

	}
	
	/**
	 * Calculate the details to order according to the specific table
	 * and other condition referring to {@link ReqPayOrderParser}.
	 * If the table is merged, perform to get its parent order,
	 * otherwise get the order of its own.
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
	public static Order calcByTableDync(Terminal term, Order orderToPay) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcByTableDync(dbCon, term, orderToPay);
		}finally{
			dbCon.disconnect();
		}

	}
	
	/**
	 * Calculate the details to order according to the specific table
	 * and other condition referring to {@link ReqPayOrderParser}.
	 * If the table is merged, perform to get its parent order,
	 * otherwise get the order of its own.
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
	public static Order calcByTableDync(DBCon dbCon, Terminal term, Order orderToPay) throws BusinessException, SQLException{
		
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
		
		//Get the setting.
		Setting setting = QuerySetting.exec(dbCon, term.restaurantID);
		
		//Check to see whether has erase quota and the order exceed the erase quota.
		if(setting.hasEraseQuota() && orderToPay.getErasePrice() > setting.getEraseQuota()){
			throw new BusinessException("The order(id=" + orderToPay.getId() + ") exceeds the erase quota.", ErrorCode.EXCEED_GIFT_QUOTA);
		}
		
		//Get all the details of order to be calculated.
		Order orderToCalc = QueryOrderDao.execByID(orderToPay.getId(), QueryOrderDao.QUERY_TODAY);
		
		PricePlan oriPricePlan = orderToCalc.getPricePlan();
		
		//Set the order calculate parameters.
		setOrderCalcParams(orderToCalc, orderToPay);
		
		//Get the discount to this order.
		Discount[] discount = QueryMenu.queryDiscounts(dbCon, 
													   " AND DIST.restaurant_id = " + term.restaurantID +
													   " AND DIST.discount_id = " + orderToCalc.getDiscount().discountID,
													   null);
		if(discount.length > 0){
			orderToCalc.setDiscount(discount[0]);
		}else{
			//Make use the default discount if the discount does NOT exist.
			discount = QueryMenu.queryDiscounts(dbCon, 
					   							" AND DIST.restaurant_id = " + term.restaurantID +
					   							" AND (DIST.status = " + Discount.DEFAULT + " OR " + " DIST.status = " + Discount.DEFAULT_RESERVED + ")",
					   							null);
			if(discount.length > 0){
				orderToCalc.setDiscount(discount[0]);
			}
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
		
		if(orderToCalc.isMerged() && orderToCalc.hasChildOrder()){
			//Add up the custom number to each child order
			orderToCalc.setCustomNum(0);
			
			Order[] childOrders = orderToCalc.getChildOrder();
			for(int i = 0; i < childOrders.length; i++){
				//Set the calculate parameters to each child order.
				setOrderCalcParams(childOrders[i], orderToPay);
				//Calculate each child order.
				childOrders[i] = calcByID(dbCon, term, childOrders[i]);
				//Accumulate the custom number.
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
			
			//Minus the erase price.
			orderToCalc.setActualPrice(orderToCalc.getActualPrice() - orderToCalc.getErasePrice());
			
		}else{
			
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
			
			//Minus the erase price.
			actualPrice = actualPrice - orderToCalc.getErasePrice();
			actualPrice = actualPrice > 0 ? actualPrice : 0;
			
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
		if(!orderToCalc.isMergedChild()){
			orderToCalc.setErasePrice(calcParams.getErasePrice());
		}
		if(!(orderToCalc.isMerged() || orderToCalc.isMergedChild())){
			orderToCalc.setCustomNum(calcParams.getCustomNum());
		}
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
