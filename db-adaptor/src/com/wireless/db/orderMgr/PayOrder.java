package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.member.MemberDao;
import com.wireless.db.regionMgr.TableDao;
import com.wireless.db.serviceRate.ServicePlanDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.DiscountError;
import com.wireless.exception.FrontBusinessError;
import com.wireless.exception.StaffError;
import com.wireless.pojo.dishesOrder.MixedPayment;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.Order.PayBuilder;
import com.wireless.pojo.dishesOrder.Order.SettleType;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.serviceRate.ServicePlan;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.Setting;
import com.wireless.pojo.util.DateType;

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
			dbCon.conn.setAutoCommit(false);
			Order order = payTemp(dbCon, staff, payBuilder);
			dbCon.conn.commit();
			return order;
			
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
			
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
	 * 			throws if failed to execute {@link this#calc} or has no privilege to do temporary payment
	 */
	public static Order payTemp(DBCon dbCon, Staff staff, PayBuilder payBuilder) throws SQLException, BusinessException{
		if(staff.getRole().hasPrivilege(Privilege.Code.TEMP_PAYMENT)){
			return doTmpPayment(dbCon, staff, payBuilder);
		}else{
			throw new BusinessException(StaffError.TEMP_PAYMENT_NOT_ALLOW);
		}
	}
	
	private static Order doTmpPayment(DBCon dbCon, Staff staff, PayBuilder payBuilder) throws SQLException, BusinessException{
		//Update the temp paid staff & date.
		String sql;
		sql = " UPDATE " + Params.dbName + ".order SET id = " + payBuilder.getOrderId() +
			  " ,temp_date = NOW() " +
			  " ,temp_staff = '" + staff.getName() + "'" +
			  " WHERE id = " + payBuilder.getOrderId();
		dbCon.stmt.executeUpdate(sql);
		
		return calc(dbCon, staff, payBuilder);
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
			dbCon.conn.setAutoCommit(false);
			Order order = pay(dbCon, staff, payBuilder);
			dbCon.conn.commit();
			
			return order;
			
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
			
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
	 * 			<li>the consume price exceeds total balance to this member account in case of normal consumption
	 * 		    <li>perform the member repaid consumption
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	private static Order doPrepare(DBCon dbCon, Staff staff, PayBuilder payBuilder) throws BusinessException, SQLException{
		Order orderCalculated = calc(dbCon, staff, payBuilder);
		
		//Set the received cash if less than actual price.
		if(orderCalculated.getPaymentType().isCash() && payBuilder.getReceivedCash() < orderCalculated.getActualPrice()){
			orderCalculated.setReceivedCash(orderCalculated.getActualPrice());
		}
		
		if(orderCalculated.isSettledByMember() && orderCalculated.isUnpaid()){
			Member member = MemberDao.getById(dbCon, staff, orderCalculated.getMemberId());
			//Check to see whether be able to perform consumption.
			member.checkConsume(orderCalculated.getActualPrice(), 
								orderCalculated.getPaymentType());
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
	 * @throws BusinessException 
	 */
	private static Order doPayment(DBCon dbCon, Staff staff, Order orderCalculated, PayBuilder payBuilder) throws SQLException, BusinessException{
		
		String sql;
		
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
			  (orderCalculated.hasUsedCoupon() ? " ,coupon_price = " + orderCalculated.getCouponPrice() : "") + 
			  " ,pure_price = " + orderCalculated.getPurePrice() +
			  " ,total_price = " + orderCalculated.getTotalPrice() +  
			  " ,actual_price = " + orderCalculated.getActualPrice() + 
			  " ,custom_num = " + orderCalculated.getCustomNum() + 
			  " ,pay_type_id = " + orderCalculated.getPaymentType().getId() +  
			  " ,settle_type = " + orderCalculated.getSettleType().getVal() + 
			  " ,discount_id = " + orderCalculated.getDiscount().getId() + 
			  (orderCalculated.hasPricePlan() ? " ,price_plan_id = " + orderCalculated.getPricePlan().getId() : "") +
			  (orderCalculated.hasServicePlan() ? " ,service_plan_id = " + orderCalculated.getServicePlan().getPlanId() : "") +
			  " ,service_rate = " + orderCalculated.getServiceRate() + 
			  " ,status = " + (orderCalculated.isUnpaid() ? Order.Status.PAID.getVal() : Order.Status.REPAID.getVal()) +  
			  (orderCalculated.isUnpaid() ? (" ,seq_id = " + orderCalculated.getSeqId()) : "") +
		   	  " ,order_date = NOW() " + 
			  " ,comment = '" + orderCalculated.getComment() + "'" + 
			  " WHERE " +
			  " id = " + orderCalculated.getId();
			
		dbCon.stmt.executeUpdate(sql);			

		//Update each food's discount & unit price.
		for(OrderFood of : orderCalculated.getOrderFoods()){
			sql = " UPDATE " + Params.dbName + ".order_food " +
				  " SET discount = " + of.getDiscount() + 
				  " ,unit_price = " + of.asFood().getPrice(orderCalculated.hasPricePlan() ? orderCalculated.getPricePlan() : null) +
				  (of.hasFoodUnit() && orderCalculated.hasPricePlan() ? " ,food_unit_price = " + of.asFood().getPrice(orderCalculated.getPricePlan()) : "") +
				  " WHERE order_id = " + orderCalculated.getId() + 
				  " AND food_id = " + of.getFoodId() +
				  (of.hasFoodUnit() ? " AND food_unit_id = " + of.getFoodUnit().getId() : "");
			dbCon.stmt.executeUpdate(sql);				
		}	
		
		//Insert the mixed payment detail.
		if(orderCalculated.getPaymentType().isMixed()){
			MixedPaymentDao.insert(dbCon, staff, new MixedPayment.InsertBuilder(orderCalculated).setPayments(orderCalculated.getMixedPayment().getPayments()));
		}
		
		if(orderCalculated.isUnpaid()){
			//Delete the temporary table if the category belongs to joined, take out or fast.
			if(orderCalculated.getDestTbl().getCategory().isTemporary()){
				TableDao.deleteById(dbCon, staff, orderCalculated.getDestTbl().getId());
			}
			
			//Perform the member consumption if settle by member.
			if(orderCalculated.isSettledByMember()){
				MemberDao.consume(dbCon, staff, orderCalculated.getMemberId(), orderCalculated.getActualPrice(), 
								  orderCalculated.getPaymentType(),
								  orderCalculated.getId());
			}
		}else{
			if(orderCalculated.isSettledByMember()){
				MemberDao.reConsume(dbCon, staff, orderCalculated.getMemberId(), orderCalculated.getActualPrice(), 
						  			orderCalculated.getPaymentType(),
						  			orderCalculated.getId());
			}
		}
		
		return orderCalculated;
	}
	
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
	 * 			<li>the payment type does NOT exist
	 * 			<li>the sum of each payment NOT equals to the actual price in case of mixed payment
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
		
		//Get all the details of order.
		Order orderToCalc = OrderDao.getById(dbCon, staff, payBuilder.getOrderId(), DateType.TODAY);
		
		//Set the payment type.
		orderToCalc.setPaymentType(PayTypeDao.getById(dbCon, staff, payBuilder.getPaymentType().getId()));
		//Set the settle type.
		orderToCalc.setSettleType(orderToCalc.hasMember() ? SettleType.MEMBER : SettleType.NORMAL);
		
		if(orderToCalc.getPaymentType().isMixed() && orderToCalc.isSettledByMember()){
			throw new BusinessException("会员不支持混合结账");
		}
		
		if(orderToCalc.isSettledByNormal() && orderToCalc.getPaymentType().isMember()){
			throw new IllegalArgumentException("普通结账不能使用【会员卡】的付款方式");
		}
		
		//Set the discount.
		try{
			if(orderToCalc.getDiscountDate() == 0){
				orderToCalc.setDiscount(DiscountDao.getDefault(dbCon, staff));
			}else{
				orderToCalc.setDiscount(DiscountDao.getById(dbCon, staff, orderToCalc.getDiscount().getId()));
			}
		}catch(BusinessException e){
			//Use the default if discount not set before.
			if(e.getErrCode() == DiscountError.DISCOUNT_NOT_EXIST){
				orderToCalc.setDiscount(DiscountDao.getDefault(dbCon, staff));
			}else{
				throw e;
			}
		}

		//Set the price plan.
		final ServicePlan servicePlan;
		if(orderToCalc.hasServicePlan()){
			servicePlan = ServicePlanDao.getByRegion(dbCon, staff, orderToCalc.getServicePlan().getPlanId(), orderToCalc.getRegion());
		}else{
			servicePlan = ServicePlanDao.getDefaultByRegion(dbCon, staff, orderToCalc.getRegion());
		}
		orderToCalc.setServicePlan(servicePlan);
		if(servicePlan.hasRates()){
			orderToCalc.setServiceRate(servicePlan.getRates().get(0).getRate());
		}else{
			orderToCalc.setServiceRate(0);
		}
		
		//Set the erase price.
		orderToCalc.setErasePrice(payBuilder.getErasePrice());
		//Set the received cash.
		orderToCalc.setReceivedCash(payBuilder.getReceivedCash());
		//Set the custom number.
		if(payBuilder.hasCustomNum()){
			orderToCalc.setCustomNum(payBuilder.getCustomNum());
		}
		//Set the comment.
		if(payBuilder.hasComment()){
			orderToCalc.setComment(payBuilder.getComment());
		}
		
		float cancelPrice = 0;
		//Calculate the cancel price to this order.
		sql = " SELECT " + 
			  " ABS(ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2)) AS cancel_price " +
			  " FROM " + Params.dbName + ".order_food OF" + 
			  " JOIN " + Params.dbName + ".taste_group TG ON OF.taste_group_id = TG.taste_group_id " +
			  " WHERE 1 = 1 " + 
			  " AND OF.order_count < 0 " +
			  " AND OF.operation = " + OrderFood.Operation.CANCEL.getVal() +
			  " AND OF.order_id = " + orderToCalc.getId();
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			cancelPrice = dbCon.rs.getFloat("cancel_price");
		}
		
		float repaidPrice = 0;
		//Calculate the repaid price to this order.
		sql = " SELECT " + 
			  " ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2) AS repaid_price " +
			  " FROM " + Params.dbName + ".order_food OF" + 
			  " JOIN " + Params.dbName + ".taste_group TG ON OF.taste_group_id = TG.taste_group_id " +
			  " WHERE OF.is_paid = 1 AND OF.order_id = " + orderToCalc.getId();
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			repaidPrice = dbCon.rs.getFloat("repaid_price");
		}
		
		//Calculate the total price.
		final float totalPrice = orderToCalc.calcTotalPrice();			
		
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
		//Set the pure price.
		orderToCalc.setPurePrice(orderToCalc.calcPureTotalPrice());
		//Set the total price.
		orderToCalc.setTotalPrice(totalPrice);
		//Set the actual price
		orderToCalc.setActualPrice(actualPrice);
	
		//Set the mixed payment detail.
		if(orderToCalc.getPaymentType().isMixed()){
			if(payBuilder.getMixedPayment().getPrice() == orderToCalc.getActualPrice()){
				MixedPayment mixedPayment = new MixedPayment();
				for(Map.Entry<PayType, Float> entry : payBuilder.getMixedPayment().getPayments().entrySet()){
					mixedPayment.add(PayTypeDao.getById(dbCon, staff, entry.getKey().getId()), entry.getValue().floatValue());
				}
				orderToCalc.setMixedPayment(mixedPayment);
			}else{
				throw new BusinessException(FrontBusinessError.MIXED_PAYMENT_NOT_EQUALS_TO_ACTUAL);
			}
		}
		
		return orderToCalc;
	}
	
}
