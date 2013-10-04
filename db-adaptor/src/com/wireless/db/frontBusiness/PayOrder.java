package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.menuMgr.PricePlanDao;
import com.wireless.db.orderMgr.OrderDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.Setting;
import com.wireless.util.DateType;

public class PayOrder {
	
	/**
	 * Perform to pay a temporary order.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderToPay
	 * 			the order to pay temporary
	 * @return the order calculated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the order to this id does NOT exist
	 */
	public static Order execTmpById(Staff staff, Order orderToPay) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execTmpById(dbCon, staff, orderToPay);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to pay a temporary order.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderToPay
	 * 			the order to pay temporary
	 * @return the order calculated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the order to this id does NOT exist
	 */
	public static Order execTmpById(DBCon dbCon, Staff staff, Order orderToPay) throws SQLException, BusinessException{
		return doTmpPayment(dbCon, staff, calcById(dbCon, staff, orderToPay));
	}
	
	private static Order doTmpPayment(DBCon dbCon, Staff staff, Order orderCalculated) throws SQLException{
		
		boolean isAutoCommit = dbCon.conn.getAutoCommit();
		
		try{
			dbCon.conn.setAutoCommit(false);
			
			String sql;
			//Update the discount to this order.
			sql = " UPDATE " + Params.dbName + ".order SET " +
				  " staff_id = " + staff.getId() + ", " +
				  " waiter = '" + staff.getName() + "', " +
				  " discount_id = " + orderCalculated.getDiscount().getId() + ", " +
				  " status = " + Order.Status.UNPAID.getVal() + ", " + 
			   	  " order_date = NOW() " + 
				  " WHERE 1 = 1 " +
				  " AND id = " + orderCalculated.getId() +
				  " AND status = " + Order.Status.UNPAID.getVal();
				
			if(dbCon.stmt.executeUpdate(sql) > 0 ){			
				//Update each food's discount & unit price to this order.
				for(OrderFood food : orderCalculated.getOrderFoods()){
					sql = " UPDATE " + Params.dbName + ".order_food " +
						  " SET " +
						  " discount = " + food.getDiscount() + ", " +
						  " unit_price = " + food.getPrice() +
						  " WHERE order_id = " + orderCalculated.getId() + 
						  " AND food_alias = " + food.getAliasId();
					dbCon.stmt.executeUpdate(sql);				
				}	
			}
			
			dbCon.conn.commit();

			return orderCalculated;
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.conn.setAutoCommit(isAutoCommit);
		}

	}
	
	
	/**
	 * Perform to pay an order along with the id and other pay condition.
	 * 
	 * @param staff
	 * 			the staff to perform this action
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
	public static Order execById(Staff staff, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execById(dbCon, staff, orderToPay);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to pay an order along with the id and other pay condition.
	 * 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderToPay
	 * 			the order to pay along with id and other pay condition
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException
	 *             throws if one of the cases below<br>
	 *             - the terminal is NOT attached to any restaurant<br>
	 *             - the terminal is expired<br>
	 *             - the order to be paid repeat<br>
	 *             - the order to query does NOT exist<br>
	 *             - the staff has no privilege for payment in case of order unpaid<br>
	 *             - the staff has no privilege for re-payment in case of order paid
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order execById(DBCon dbCon, Staff staff, Order orderToPay) throws BusinessException, SQLException{
		Order.Status status = OrderDao.getStatusById(dbCon, staff, orderToPay.getId());
		if(status == Order.Status.UNPAID && !staff.getRole().hasPrivilege(Privilege.Code.PAYMENT)){
			throw new BusinessException(StaffError.PAYMENT_NOT_ALLOW);
			
		}else if((status == Order.Status.PAID || status == Order.Status.REPAID) && !staff.getRole().hasPrivilege(Privilege.Code.RE_PAYMENT)){
			throw new BusinessException(StaffError.RE_PAYMENT_NOT_ALLOW);
			
		}else{
			return doPayment(dbCon, staff, doPrepare(dbCon, staff, orderToPay));
		}
		
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param staff
	 * @param orderToPay
	 * @return
	 * @throws BusinessException
	 * 			throws if one of cases below
	 * 			<li>failed to calculate the order referred to {@link PayOrder#calcById}
	 * 			<li>the consume price exceeds total balance to this member account in case of normal consumption.
	 * 		    <li>perform the member repaid consumption.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	private static Order doPrepare(DBCon dbCon, Staff staff, Order orderToPay) throws BusinessException, SQLException{
		int customNum = orderToPay.getCustomNum();
		Order orderCalculated = calcById(dbCon, staff, orderToPay);
		orderCalculated.setCustomNum(customNum);
		
		//Set the received cash if less than actual price.
		if(orderCalculated.isPayByCash() && orderCalculated.getReceivedCash() < orderCalculated.getActualPrice()){
			orderCalculated.setReceivedCash(orderCalculated.getActualPrice());
		}
		
		if(orderCalculated.isSettledByMember()){
			
			Member member = MemberDao.getMemberById(staff, orderCalculated.getMember().getId());
			
			if(orderCalculated.isUnpaid()){
				//Check to see whether be able to perform consumption.
				member.checkConsume(orderCalculated.getActualPrice(), orderCalculated.getPaymentType());
			}else{
				//Check to see whether be able to perform repaid consumption.
				throw new BusinessException("Repaid to member consumption is NOT supported.");
			}
			
			orderCalculated.setMember(member);
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
	 * @param staff
	 * @param orderCalculated
	 * @param isPaidAgain
	 * @return
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	private static Order doPayment(DBCon dbCon, Staff staff, Order orderCalculated) throws SQLException{
		
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
						  " status = " + Table.Status.IDLE.getVal() + ", " +
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
				orderCalculated.setCategory(Order.Category.NORMAL);
			}
			
			//Update the order.
			sql = " UPDATE " + Params.dbName + ".order SET " +
				  " staff_id = " + staff.getId() + ", " +
				  " waiter = '" + staff.getName() + "', " +
				  " category = " + orderCalculated.getCategory().getVal() + ", " +
				  " gift_price = " + orderCalculated.getGiftPrice() + ", " +
				  " discount_price = " + orderCalculated.getDiscountPrice() + ", " +
				  " cancel_price = " + orderCalculated.getCancelPrice() + ", " +
				  " repaid_price =  " + orderCalculated.getRepaidPrice() + ", " +
				  " erase_price = " + orderCalculated.getErasePrice() + ", " +
				  " total_price = " + orderCalculated.getTotalPrice() + ", " + 
				  " actual_price = " + orderCalculated.getActualPrice() + ", " +
				  " custom_num = " + orderCalculated.getCustomNum() + ", " +
				  " pay_type = " + orderCalculated.getPaymentType().getVal() + ", " + 
				  " settle_type = " + orderCalculated.getSettleType().getVal() + ", " +
				  " discount_id = " + orderCalculated.getDiscount().getId() + ", " +
				  " price_plan_id = " + (orderCalculated.hasPricePlan() ? orderCalculated.getPricePlan().getId() : "price_plan_id") + ", " +
				  " service_rate = " + orderCalculated.getServiceRate() + ", " +
				  " status = " + (orderCalculated.isUnpaid() ? Order.Status.PAID.getVal() : Order.Status.REPAID.getVal()) + ", " + 
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
					  " status = " + Table.Status.IDLE.getVal() + ", " +
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
					mo = MemberDao.consume(dbCon, staff, 
										   orderCalculated.getMember().getId(), 
										   orderCalculated.getActualPrice(), 
										   orderCalculated.getPaymentType(),
										   orderCalculated.getId());
					orderCalculated.setMemberOperationId(mo.getId());

				}else{
					throw new BusinessException("Repaid to member is NOT supported.");
				}
				
				sql = " UPDATE " + Params.dbName + ".order SET " +
					  " member_id = " + mo.getMemberId() + "," +
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
	 * @param staff
	 * 			the staff to perform this action 
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
	public static Order calcByTable(Staff staff, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcByTable(dbCon, staff, orderToPay);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the order details according to the specific table defined in order regardless of merged status. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action 
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
	public static Order calcByTable(DBCon dbCon, Staff staff, Order orderToPay) throws BusinessException, SQLException{
		
		orderToPay.setId(OrderDao.getOrderIdByUnPaidTable(dbCon, TableDao.getTableByAlias(dbCon, staff, orderToPay.getDestTbl().getAliasId()))[0]);			
		
		return calcById(dbCon, staff, orderToPay);		

	}
	
	/**
	 * Calculate the details to order according to the specific table
	 * and other condition referring to {@link ReqPayOrderParser}.
	 * If the table is merged, perform to get its parent order,
	 * otherwise get the order of its own.
	 * @param staff
	 * 			the staff to perform this action 
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
	public static Order calcByTableDync(Staff staff, Order orderToPay) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcByTableDync(dbCon, staff, orderToPay);
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
	 * @param staff
	 * 			the staff to perform this action 
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
	public static Order calcByTableDync(DBCon dbCon, Staff staff, Order orderToPay) throws BusinessException, SQLException{
		
		//Get the details of table to be calculated.
		Table tblToCalc = TableDao.getTableByAlias(dbCon, staff, orderToPay.getDestTbl().getAliasId());
		
		//If the table is merged, get its parent order.
		//Otherwise get the order of its own.
		int[] unpaidId = OrderDao.getOrderIdByUnPaidTable(dbCon, tblToCalc);
		if(unpaidId.length > 1){
			orderToPay.setId(unpaidId[1]);			
		}else{
			orderToPay.setId(unpaidId[0]);			
		}
		
		return calcById(dbCon, staff, orderToPay);		

	}
	
	/**
	 * Calculate the details to the order according to id and
	 * other condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderToPay
	 * 			the order along with id and other condition
	 * @return the order result after calculated
	 * @throws BusinessException
	 * 			Throws if the order to this id does NOT exist.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static Order calcById(Staff staff, Order orderToPay) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcById(dbCon, staff, orderToPay);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the details to the order according to id and
	 * other condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param orderToPay
	 * 			the order along with id and other condition
	 * @return the order result after calculated
	 * @throws BusinessException
	 * 			throws if the order to this id does NOT exist
	 * 			throws if the staff has no privilege to make use of the requested discount
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Order calcById(DBCon dbCon, Staff staff, Order orderToPay) throws BusinessException, SQLException{
		
		String sql;
		
		//Get the setting.
		Setting setting = SystemDao.getSetting(dbCon, staff.getRestaurantId());
		
		//Check to see whether has erase quota and the order exceed the erase quota.
		if(setting.hasEraseQuota() && orderToPay.getErasePrice() > setting.getEraseQuota()){
			throw new BusinessException(FrontBusinessError.EXCEED_ERASE_QUOTA);
		}
		
		//Get all the details of order to be calculated.
		Order orderToCalc = OrderDao.getById(staff, orderToPay.getId(), DateType.TODAY);
		
		PricePlan oriPricePlan = orderToCalc.getPricePlan();
		
		//Set the order calculate parameters.
		setOrderCalcParams(orderToCalc, orderToPay);

		if(orderToCalc.getDiscount().equals(orderToPay.getDiscount())){
			orderToCalc.setDiscount(DiscountDao.getDiscountById(dbCon, staff, orderToPay.getDiscount().getId()));
		}else{
			//Get the discounts to role owned by staff
			//and check to see whether the staff is permitted to make use of the discount.
			List<Discount> discounts = DiscountDao.getDiscountByRole(dbCon, staff, staff.getRole());
			int index = discounts.indexOf(orderToCalc.getDiscount());
			if(index < 0){
				throw new BusinessException(StaffError.DISCOUNT_NOT_ALLOW);
			}else{
				orderToCalc.setDiscount(discounts.get(index));
			}
		}

		//Get the details if the requested plan is set.
		//Otherwise use the price plan which is active instead.
		if(orderToCalc.hasPricePlan()){
			orderToCalc.setPricePlan(PricePlanDao.getPricePlanById(dbCon, staff,  orderToCalc.getPricePlan().getId()));
		}else{
			orderToCalc.setPricePlan(PricePlanDao.getActivePricePlan(dbCon, staff));
		}
		
		//Check to see whether the requested price plan is same as before.
		if(!orderToCalc.getPricePlan().equals(oriPricePlan)){
			
			//Get the price belongs to requested plan to each order food(except the temporary food) if different from before.
			List<OrderFood> foodsToCalc = orderToCalc.getOrderFoods();
			for(OrderFood of : foodsToCalc){
				if(!of.isTemp()){
					sql = " SELECT " +
						  " unit_price " + " FROM " + Params.dbName + ".food_price_plan" +
						  " WHERE " + 
						  " price_plan_id = " + orderToCalc.getPricePlan().getId() +
						  " AND " +
						  " food_id = " + of.getFoodId();
					dbCon.rs = dbCon.stmt.executeQuery(sql);
					if(dbCon.rs.next()){
						of.asFood().setPrice(dbCon.rs.getFloat("unit_price"));
					}
					dbCon.rs.close();
				}
			}
		}
		
		if(orderToCalc.isMerged() && orderToCalc.hasChildOrder()){
			//Add up the custom number to each child order
			orderToCalc.setCustomNum(0);
			
			for(Order childOrder : orderToCalc.getChildOrder()){
				//Set the calculate parameters to each child order.
				setOrderCalcParams(childOrder, orderToPay);
				//Calculate each child order.
				childOrder.copyFrom(calcById(dbCon, staff, childOrder));
				//Accumulate the custom number.
				orderToCalc.setCustomNum(orderToCalc.getCustomNum() + childOrder.getCustomNum());
				//Accumulate the discount price.
				orderToCalc.setDiscountPrice(orderToCalc.getDiscountPrice() + childOrder.getDiscountPrice());
				//Accumulate the gift price.
				orderToCalc.setGiftPrice(orderToCalc.getGiftPrice() + childOrder.getGiftPrice());
				//Accumulate the cancel price.
				orderToCalc.setCancelPrice(orderToCalc.getCancelPrice() + childOrder.getCancelPrice());
				//Accumulate the repaid price.
				orderToCalc.setRepaidPrice(orderToCalc.getRepaidPrice() + childOrder.getRepaidPrice());
				//Accumulate the total price.
				orderToCalc.setTotalPrice(orderToCalc.getTotalPrice() + childOrder.getTotalPrice());
				//Accumulate the actual price.
				orderToCalc.setActualPrice(orderToCalc.getActualPrice() + childOrder.getActualPrice());
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
