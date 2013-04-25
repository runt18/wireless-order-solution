package com.wireless.db.frontBusiness;

import java.sql.SQLException;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.db.menuMgr.QueryPricePlanDao;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.system.SystemDao;
//import com.wireless.dbObject.Setting;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.dishesOrder.Order.PayType;
import com.wireless.pojo.system.Setting;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.PDiscount;
import com.wireless.protocol.PricePlan;
import com.wireless.protocol.PTable;
import com.wireless.protocol.Terminal;

public class PayOrder {
	
	/**
	 * Perform to pay an order along with the id and other pay condition.
	 * 
	 * @param term
	 * 			the terminal
	 * @param orderToPay
	 * 			the order to pay along with id and other pay condition
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException
	 *             throws if one of the cases below.<br>
	 *             - The terminal is NOT attached to any restaurant.<br>
	 *             - The terminal is expired.<br>
	 *             - The order to query does NOT exist.
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order execByID(Terminal term, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByID(dbCon, term, orderToPay);
			
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
	public static Order execByID(DBCon dbCon, Terminal term, Order orderToPay) throws BusinessException, SQLException{
		
		return doPayment(dbCon, term, doPrepare(dbCon, term, orderToPay));
		
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param orderToPay
	 * @return
	 * @throws BusinessException
	 * 			Throws if one of cases below.
	 * 			<li>Failed to calculate the order referred to {@link PayOrder#calcByID}
	 * 			<li>The consume price exceeds total balance to this member account in case of normal consumption.
	 * 		    <li>The consume price exceeds total balance to this member account in case of repaid consumption.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statements.
	 */
	private static Order doPrepare(DBCon dbCon, Terminal term, Order orderToPay) throws BusinessException, SQLException{
		int customNum = orderToPay.getCustomNum();
		Order orderCalculated = calcByID(dbCon, term, orderToPay);
		orderCalculated.setCustomNum(customNum);
		
		if(orderCalculated.isSettledByMember()){
			
			Member member = MemberDao.getMemberById(orderCalculated.getMember().getId());
			
			if(orderCalculated.isUnpaid()){
				//Check to see whether be able to perform consumption.
				member.checkConsume(orderCalculated.getActualPrice(), PayType.valueOf(orderCalculated.getPaymentType()));
			}else{
				//Check to see whether be able to perform repaid consumption.
				member.checkRepaidConsume(orderCalculated.getActualPrice(), 
										  MemberOperationDao.getTodayByOrderId(dbCon, orderCalculated.getId()),
										  PayType.valueOf(orderCalculated.getPaymentType()));
			}
			
			orderCalculated.setMember(member.toProtocolObj());
		}
		
		//Calculate the sequence id to this order in case of unpaid.
		if(orderCalculated.isUnpaid()){
			String sql;
			sql = " SELECT CASE WHEN MAX(seq_id) IS NULL THEN 1 ELSE MAX(seq_id) + 1 END FROM " + 
				  Params.dbName + ".order WHERE restaurant_id=" + orderCalculated.getRestaurantId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				orderCalculated.setSeqId(dbCon.rs.getInt(1));
			}
			dbCon.rs.close();
		}
		
		return orderCalculated;
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
	private static Order doPayment(DBCon dbCon, Terminal term, Order orderCalculated) throws SQLException{
		
		String sql;
		
		boolean isAutoCommit = dbCon.conn.getAutoCommit();
		
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
						  " status = " + PTable.TABLE_IDLE + ", " +
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
					for(OrderFood food : childOrder.getOrderFoods()){
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
				  " actual_price = " + orderCalculated.getActualPrice() + ", " +
				  " custom_num = " + orderCalculated.getCustomNum() + ", " +
				  " pay_type = " + orderCalculated.getPaymentType() + ", " + 
				  " settle_type = " + orderCalculated.getSettleType() + ", " +
				  " discount_id = " + orderCalculated.getDiscount().getId() + ", " +
				  " price_plan_id = " + (orderCalculated.hasPricePlan() ? orderCalculated.getPricePlan().getId() : "price_plan_id") + ", " +
				  " service_rate = " + orderCalculated.getServiceRate() + ", " +
				  " status = " + (orderCalculated.isUnpaid() ? Order.STATUS_PAID : Order.STATUS_REPAID) + ", " + 
				  (orderCalculated.isUnpaid() ? (" seq_id = " + orderCalculated.getSeqId() + ", ") : "") +
			   	  " order_date = NOW(), " + 
				  " comment = " + "'" + orderCalculated.getComment() + "'" + 
				  " WHERE " +
				  " id = " + orderCalculated.getId();
				
			dbCon.stmt.executeUpdate(sql);			

			//Update each food's discount & unit price.
			for(OrderFood food : orderCalculated.getOrderFoods()){
				sql = " UPDATE " + Params.dbName + ".order_food " +
					  " SET " +
					  " discount = " + food.getDiscount() + ", " +
					  " unit_price = " + food.getPrice() +
					  " WHERE order_id = " + orderCalculated.getId() + 
					  " AND food_alias = " + food.getAliasId();
				dbCon.stmt.executeUpdate(sql);				
			}	

			//Update the table associated with this order to IDLE in case of unpaid.
			if(orderCalculated.isUnpaid()){
				
				sql = " UPDATE " + Params.dbName + ".table SET " +
					  " status = " + PTable.TABLE_IDLE + ", " +
					  " custom_num = NULL, " +
					  " category = NULL " +
					  " WHERE " +
  			  		  " restaurant_id = " + orderCalculated.getRestaurantId() + " AND " +
					  " table_alias = " + orderCalculated.getDestTbl().getAliasId();
				dbCon.stmt.executeUpdate(sql);				
						
			}
			
			//Update the member status if settled by member.
			if(orderCalculated.isSettledByMember()){
				
				MemberOperation mo;
				
				if(orderCalculated.isUnpaid()){
					//Perform the consumption.
					mo = MemberDao.consume(dbCon, term, 
										   orderCalculated.getMember().getId(), 
										   orderCalculated.getActualPrice(), 
										   PayType.valueOf(orderCalculated.getPaymentType()),
										   orderCalculated.getId());
					orderCalculated.setMemberOperationId(mo.getId());

				}else{
					//Perform the repaid consumption.
					mo = MemberDao.repaidConsume(dbCon, term, 
												 orderCalculated.getMember().getId(), 
												 orderCalculated.getActualPrice(), 
												 orderCalculated.getId(),
												 PayType.valueOf(orderCalculated.getPaymentType()))[1];
				}
				
				sql = " UPDATE " + Params.dbName + ".order SET " +
					  " member_id = " + mo.getMemberID() + "," +
					  " member_operation_id = " + mo.getId() +
					  " WHERE id = " + orderCalculated.getId();
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
			dbCon.conn.setAutoCommit(isAutoCommit);
		}
		
		return orderCalculated;
	}
	
	/**
	 * Calculate the order details according to the specific table defined in order regardless of merged status. 
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
	 * Calculate the order details according to the specific table defined in order regardless of merged status. 
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
		PTable tblToCalc = QueryTable.exec(dbCon, term, orderToPay.getDestTbl().getAliasId());
		
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
//		Setting setting = QuerySetting.exec(dbCon, term.restaurantID);
		Setting setting = SystemDao.getSetting(dbCon, term.restaurantID);
		
		//Check to see whether has erase quota and the order exceed the erase quota.
		if(setting.hasEraseQuota() && orderToPay.getErasePrice() > setting.getEraseQuota()){
			throw new BusinessException("The order(id=" + orderToPay.getId() + ") exceeds the erase quota.", ProtocolError.EXCEED_ERASE_QUOTA);
		}
		
		//Get all the details of order to be calculated.
		Order orderToCalc = QueryOrderDao.execByID(orderToPay.getId(), QueryOrderDao.QUERY_TODAY);
		
		PricePlan oriPricePlan = orderToCalc.getPricePlan();
		
		//Set the order calculate parameters.
		setOrderCalcParams(orderToCalc, orderToPay);
		
		//Get the discount to this order.
		PDiscount[] discount = QueryMenu.queryDiscounts(dbCon, 
													   " AND DIST.restaurant_id = " + term.restaurantID +
													   " AND DIST.discount_id = " + orderToCalc.getDiscount().getId(),
													   null);
		if(discount.length > 0){
			orderToCalc.setDiscount(discount[0]);
		}else{
			//Make use the default discount if the discount does NOT exist.
			discount = QueryMenu.queryDiscounts(dbCon, 
					   							" AND DIST.restaurant_id = " + term.restaurantID +
					   							" AND (DIST.status = " + PDiscount.DEFAULT + " OR " + " DIST.status = " + PDiscount.DEFAULT_RESERVED + ")",
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
			OrderFood[] foodsToCalc = orderToCalc.getOrderFoods();
			for(int i = 0; i < foodsToCalc.length; i++){
				if(!foodsToCalc[i].isTemp()){
					sql = " SELECT " +
						  " unit_price " + " FROM " + Params.dbName + ".food_price_plan" +
						  " WHERE " + 
						  " price_plan_id = " + orderToCalc.getPricePlan().getId() +
						  " AND " +
						  " food_id = " + foodsToCalc[i].getFoodId();
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					if(dbCon.rs.next()){
						foodsToCalc[i].setPrice(dbCon.rs.getFloat("unit_price"));
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
				//Deal with the decimal according to setting.
				if(setting.getPriceTail().isDecimalCut()){
					//小数抹零
					actualPrice = Float.valueOf(totalPrice).intValue();
				}else if(setting.getPriceTail().isDecimalRound()){
					//四舍五入
					actualPrice = Math.round(totalPrice);
				}else{
					//不处理
					actualPrice = totalPrice;
				}
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
			if(calcParams.getCustomNum() >= 0){
				orderToCalc.setCustomNum(calcParams.getCustomNum());
			}
		}
		orderToCalc.setDiscount(calcParams.getDiscount());
		orderToCalc.setPricePlan(calcParams.getPricePlan());
		orderToCalc.setSettleType(calcParams.getSettleType());
		orderToCalc.setMember(calcParams.getMember()); 
		orderToCalc.setReceivedCash(calcParams.getReceivedCash());
		orderToCalc.setPaymentType(calcParams.getPaymentType());
		orderToCalc.setComment(calcParams.getComment());
		orderToCalc.setServiceRate(calcParams.getServiceRate());
	}
	
}
