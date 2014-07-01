package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.client.member.MemberDao;
import com.wireless.db.coupon.CouponDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.Order.PayBuilder;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.Setting;
import com.wireless.util.DateType;

public class PayOrder {
	
	/**
	 * Perform to pay a temporary order according to {@link Order.PayBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param payBuilder
	 * 			the parameters to pay order
	 * @return the order calculated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if failed to execute {@link#calcById}
	 */
	public static Order payTemp(Staff staff, PayBuilder payBuilder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return payTemp(dbCon, staff, payBuilder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to pay a temporary order according to {@link Order.PayBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param payBuilder
	 * 			the parameters to pay order
	 * @return the order calculated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if failed to execute {@link#calcById} or has no privilege to do temporary payment
	 */
	public static Order payTemp(DBCon dbCon, Staff staff, PayBuilder payBuilder) throws SQLException, BusinessException{
		if(staff.getRole().hasPrivilege(Privilege.Code.TEMP_PAYMENT)){
			return doTmpPayment(dbCon, staff, calc(dbCon, staff, payBuilder));
		}else{
			throw new BusinessException(StaffError.TEMP_PAYMENT_NOT_ALLOW);
		}
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
						  " unit_price = " + food.asFood().getPrice() +
						  " WHERE order_id = " + orderCalculated.getId() + 
						  " AND food_id = " + food.getFoodId();
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
	 * Perform to pay an order according to {@link Order.PayBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param payBuilder
	 * 			the parameters to pay order
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException
	 *             throws if one of the cases below
	 *             <li>failed to perform prepare and payment referred to {@link#doPayment} and {@link#doPrepare}
	 *             <li>the order to pay does NOT exist
	 *             <li>the staff has no privilege for payment in case of order unpaid
	 *             <li>the staff has no privilege for re-payment in case of order paid
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order pay(Staff staff, PayBuilder payBuilder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return pay(dbCon, staff, payBuilder);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to pay an order according to {@link Order.PayBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param payBuilder
	 * 			the parameters to pay order
	 * @return Order completed pay order information to paid order
	 * @throws BusinessException
	 *             throws if one of the cases below
	 *             <li>failed to perform prepare and payment referred to {@link#doPayment} and {@link#doPrepare}
	 *             <li>the order to pay does NOT exist
	 *             <li>the staff has no privilege for payment in case of order unpaid
	 *             <li>the staff has no privilege for re-payment in case of order paid
	 * @throws SQLException
	 *             throws if fail to execute any SQL statement
	 */
	public static Order pay(DBCon dbCon, Staff staff, PayBuilder payBuilder) throws BusinessException, SQLException{
		Order.Status status = OrderDao.getStatusById(dbCon, staff, payBuilder.getOrderId());
		if(status == Order.Status.UNPAID && !staff.getRole().hasPrivilege(Privilege.Code.PAYMENT)){
			throw new BusinessException(StaffError.PAYMENT_NOT_ALLOW);
			
		}else if((status == Order.Status.PAID || status == Order.Status.REPAID) && !staff.getRole().hasPrivilege(Privilege.Code.RE_PAYMENT)){
			throw new BusinessException(StaffError.RE_PAYMENT_NOT_ALLOW);
			
		}else{
			return doPayment(dbCon, staff, doPrepare(dbCon, staff, payBuilder), payBuilder);
		}
		
	}
	
	/**
	 * 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param payBuilder
	 * 			the parameters to pay order
	 * @return
	 * @throws BusinessException
	 * 			throws if one of cases below
	 * 			<li>failed to calculate the order referred to {@link PayOrder#calcById}
	 * 			<li>the consume price exceeds total balance to this member account in case of normal consumption.
	 * 		    <li>perform the member repaid consumption.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	private static Order doPrepare(DBCon dbCon, Staff staff, PayBuilder payBuilder) throws BusinessException, SQLException{
		Order orderCalculated = calc(dbCon, staff, payBuilder);
		
		//Set the received cash if less than actual price.
		if(orderCalculated.isPayByCash() && payBuilder.getReceivedCash() < orderCalculated.getActualPrice()){
			orderCalculated.setReceivedCash(orderCalculated.getActualPrice());
		}
		
		if(orderCalculated.isSettledByMember()){
			
			Member member = MemberDao.getById(staff, payBuilder.getMemberId());
			
			if(orderCalculated.isUnpaid()){
				//Check to see whether be able to perform consumption.
				member.checkConsume(orderCalculated.getActualPrice(), 
									payBuilder.hasCoupon() ? CouponDao.getById(dbCon, staff, payBuilder.getCouponId()) : null, 
									orderCalculated.getPaymentType());
			}else{
				//Check to see whether be able to perform repaid consumption.
				throw new BusinessException("Repaid to member consumption is NOT supported.");
			}
			
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
	 * @param payBuilder
	 * @return
	 * @throws SQLException
	 */
	private static Order doPayment(DBCon dbCon, Staff staff, Order orderCalculated, PayBuilder payBuilder) throws SQLException{
		
		String sql;
		
		boolean isAutoCommit = dbCon.conn.getAutoCommit();
		
		/**
		 * Put all the INSERT statements into a database transition so as to assure 
		 * the status to both table and order is consistent. 
		 */
		try{
			
			dbCon.conn.setAutoCommit(false);	
			
			//Update the order.
			sql = " UPDATE " + Params.dbName + ".order SET " +
				  " id = " + orderCalculated.getId() +
				  " ,staff_id = " + staff.getId() + 
				  " ,waiter = '" + staff.getName() + "'" +
				  " ,category = " + orderCalculated.getCategory().getVal() + 
				  " ,gift_price = " + orderCalculated.getGiftPrice() + 
				  " ,discount_price = " + orderCalculated.getDiscountPrice() + 
				  " ,cancel_price = " + orderCalculated.getCancelPrice() + 
				  " ,repaid_price =  " + orderCalculated.getRepaidPrice() + 
				  " ,erase_price = " + orderCalculated.getErasePrice() + 
				  " ,coupon_price = " + orderCalculated.getCouponPrice() + 
				  " ,total_price = " + orderCalculated.getTotalPrice() +  
				  " ,actual_price = " + orderCalculated.getActualPrice() + 
				  " ,custom_num = " + orderCalculated.getCustomNum() + 
				  " ,pay_type = " + orderCalculated.getPaymentType().getVal() +  
				  " ,settle_type = " + orderCalculated.getSettleType().getVal() + 
				  " ,discount_id = " + orderCalculated.getDiscount().getId() + 
				  " ,service_rate = " + orderCalculated.getServiceRate() + 
				  " ,status = " + (orderCalculated.isUnpaid() ? Order.Status.PAID.getVal() : Order.Status.REPAID.getVal()) +  
				  (orderCalculated.isUnpaid() ? (" ,seq_id = " + orderCalculated.getSeqId()) : "") +
			   	  " ,order_date = NOW() " + 
				  " ,comment = '" + orderCalculated.getComment() + "'" + 
				  " WHERE " +
				  " id = " + orderCalculated.getId();
				
			dbCon.stmt.executeUpdate(sql);			

			//Update each food's discount & unit price.
			for(OrderFood food : orderCalculated.getOrderFoods()){
				sql = " UPDATE " + Params.dbName + ".order_food " +
					  " SET " +
					  " food_id = " + food.getFoodId() +
					  " ,discount = " + food.getDiscount() + 
					  " ,unit_price = " + food.asFood().getPrice() +
					  " WHERE order_id = " + orderCalculated.getId() + 
					  " AND food_id = " + food.getFoodId();
				dbCon.stmt.executeUpdate(sql);				
			}	

			//Update the table associated with this order to IDLE in case of unpaid.
			if(orderCalculated.isUnpaid()){
				
				sql = " UPDATE " + Params.dbName + ".table SET " +
					  " status = " + Table.Status.IDLE.getVal() + 
					  " ,custom_num = NULL " +
					  " ,category = NULL " +
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
										   payBuilder.getMemberId(), 
										   orderCalculated.getActualPrice(), 
										   payBuilder.hasCoupon() ? CouponDao.getById(dbCon, staff, payBuilder.getCouponId()) : null,
										   orderCalculated.getPaymentType(),
										   orderCalculated.getId());
					orderCalculated.setMemberOperationId(mo.getId());

				}else{
					throw new BusinessException("Repaid to member is NOT supported.");
				}
				
				sql = " UPDATE " + Params.dbName + ".order SET " +
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
//	public static Order calcByTable(Staff staff, Order orderToPay) throws BusinessException, SQLException{
//		DBCon dbCon = new DBCon();
//		try{
//			dbCon.connect();
//			return calcByTable(dbCon, staff, orderToPay);
//			
//		}finally{
//			dbCon.disconnect();
//		}
//	}
	
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
//	public static Order calcByTable(DBCon dbCon, Staff staff, Order orderToPay) throws BusinessException, SQLException{
//		
//		orderToPay.setId(OrderDao.getOrderIdByUnPaidTable(dbCon, staff, TableDao.getTableByAlias(dbCon, staff, orderToPay.getDestTbl().getAliasId())));			
//		
//		return calc(dbCon, staff, orderToPay);		
//
//	}
	
	/**
	 * Calculate the details to the order according to {@link Order.PayBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param payBuilder
	 * 			the parameters to pay order
	 * @return the order result after calculated
	 * @throws BusinessException
	 * 			throws if the order to this id does NOT exist
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Order calc(Staff staff, PayBuilder payBuilder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calc(dbCon, staff, payBuilder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the details to the order according according to {@link Order.PayBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param payBuilder
	 * 			the parameters to pay order
	 * @return the order result after calculated
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the erase price exceeds the quota 
	 * 			<li>the order to this id does NOT exist
	 * 			<li>the staff has no privilege to use the requested discount
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Order calc(DBCon dbCon, Staff staff, PayBuilder payBuilder) throws BusinessException, SQLException{
		
		String sql;
		
		//Get the setting.
		Setting setting = SystemDao.getSetting(dbCon, staff.getRestaurantId());
		
		//Check to see whether has erase quota and the order exceed the erase quota.
		if(setting.hasEraseQuota() && payBuilder.getErasePrice() > setting.getEraseQuota()){
			throw new BusinessException(FrontBusinessError.EXCEED_ERASE_QUOTA);
		}
		
		//Get all the details of order to be calculated.
		Order orderToCalc = OrderDao.getById(staff, payBuilder.getOrderId(), DateType.TODAY);
		
		//Set the order calculate parameters.
		setOrderCalcParams(orderToCalc, payBuilder);

		//If the coupon is set, get the coupon price to this order.
		if(payBuilder.hasCoupon()){
			orderToCalc.setCouponPrice(CouponDao.getById(dbCon, staff, payBuilder.getCouponId()).getPrice());
		}
		
		//If the service rate is set, just use it, otherwise use the one associated with the table belongs to this order.
		if(payBuilder.hasServiceRate()){
			orderToCalc.setServiceRate(payBuilder.getServiceRate());
		}else{
			orderToCalc.setServiceRate(orderToCalc.getDestTbl().getServiceRate());
		}
		
		//If the discount is set, check to see whether it is the same as before.
		if(payBuilder.hasDiscount()){
			//If the discount to set is the same as before, just use original.
			if(orderToCalc.getDiscount().getId() == payBuilder.getDiscountId()){
				orderToCalc.setDiscount(DiscountDao.getById(dbCon, staff, orderToCalc.getDiscount().getId()));
			}else{
				List<Discount> discounts;
				//If the discount to set is NOT the same as before, check to see whether the discount is permitted to use.
				if(payBuilder.getSettleType() == Order.SettleType.MEMBER){
					discounts = DiscountDao.getByMemberType(dbCon, staff, 
															MemberDao.getById(dbCon, staff, payBuilder.getMemberId()).getMemberType().getId());
				}else{
					discounts = DiscountDao.getByRole(dbCon, staff, staff.getRole());
				}
				
				int index = discounts.indexOf(new Discount(payBuilder.getDiscountId()));
				if(index < 0){
					throw new BusinessException(StaffError.DISCOUNT_NOT_ALLOW);
				}else{
					orderToCalc.setDiscount(discounts.get(index));
				}
			}
		}else{
			//If the discount NOT set, just use the original.
			orderToCalc.setDiscount(DiscountDao.getById(dbCon, staff, orderToCalc.getDiscount().getId()));
		}
		

		float cancelPrice = 0;
		//Calculate the cancel price to this order.
		sql = " SELECT " + 
			  " ABS(ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2)) AS cancel_price " +
			  " FROM " + Params.dbName + ".order_food OF" + 
			  " JOIN " + Params.dbName + ".taste_group TG" +
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
			  " FROM " + Params.dbName + ".order_food OF" + 
			  " JOIN " + Params.dbName + ".taste_group TG" +
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
		
		//Minus the erase & coupon price.
		actualPrice = actualPrice - orderToCalc.getErasePrice() - orderToCalc.getCouponPrice();
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
			
		
		return orderToCalc;
	}
	
	private static void setOrderCalcParams(Order orderToCalc, PayBuilder payBuilder){
		orderToCalc.setErasePrice(payBuilder.getErasePrice());
		orderToCalc.setCustomNum(payBuilder.getCustomNum());
		orderToCalc.setSettleType(payBuilder.getSettleType());
		orderToCalc.setReceivedCash(payBuilder.getReceivedCash());
		orderToCalc.setPaymentType(payBuilder.getPaymentType());
		orderToCalc.setComment(payBuilder.getComment());
	}
	
}