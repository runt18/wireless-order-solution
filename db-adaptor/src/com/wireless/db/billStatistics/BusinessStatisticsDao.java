package com.wireless.db.billStatistics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.billStatistics.BusinessStatistics;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.system.DailySettle;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.DateType;

public class BusinessStatisticsDao {
	
	/**
	 * 
	 * @param stat
	 * @param ol
	 * @return
	 * @throws Exception
	 */
	public static BusinessStatistics calcBusinessReceiptsStatistics(BusinessStatistics stat, List<Order> ol) throws Exception{
		stat.setOrderAmount(ol.size());
		for(Order temp : ol){
			if(temp.getPaymentType() == com.wireless.pojo.dishesOrder.Order.PayType.CASH){
				stat.setCashAmount(stat.getCashAmount() + 1);
				stat.setCashIncome2(stat.getCashIncome2() + temp.getActualPrice());
			}else if(temp.getPaymentType() == com.wireless.pojo.dishesOrder.Order.PayType.CREDIT_CARD){
				stat.setCreditCardAmount(stat.getCreditCardAmount() + 1);
				stat.setCreditCardIncome2(stat.getCreditCardIncome2() + temp.getActualPrice());
			}else if(temp.getPaymentType() == com.wireless.pojo.dishesOrder.Order.PayType.SIGN){
				stat.setSignAmount(stat.getSignAmount() + 1);
				stat.setSignIncome2(stat.getSignIncome2() + temp.getActualPrice());
			}else if(temp.getPaymentType() == com.wireless.pojo.dishesOrder.Order.PayType.HANG){
				stat.setHangAmount(stat.getHangAmount() + 1);
				stat.setHangIncome2(stat.getHangIncome2() + temp.getActualPrice());
			}
			stat.setEraseAmount(stat.getEraseAmount() + (temp.getErasePrice() > 0 ? 1 : 0));
			stat.setEraseIncome(stat.getEraseIncome() + temp.getErasePrice());
			stat.setDiscountAmount(stat.getDiscountAmount() + (temp.getDiscountPrice() > 0 ? 1 : 0));
			stat.setDiscountIncome(stat.getDiscountIncome() + temp.getDiscountPrice());
			stat.setGiftAmount(stat.getGiftAmount() + (temp.getGiftPrice() > 0 ? 1 : 0));
			stat.setGiftIncome(stat.getGiftIncome() + temp.getGiftPrice());
			stat.setCancelAmount(stat.getCancelAmount() + (temp.getCancelPrice() > 0 ? 1 : 0));
			stat.setCancelIncome(stat.getCancelIncome() + temp.getCancelPrice());
			if(temp.getStatus() == Order.Status.REPAID){
				stat.setPaidIncome(stat.getPaidIncome() + temp.getRepaidPrice());
			}
			stat.setTotalPrice(stat.getTotalPrice() + temp.getTotalPrice());
			stat.setTotalPrice2(stat.getTotalPrice2() + temp.getActualPrice());
		}
		return stat;
	}
	
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<BusinessStatistics> getBusinessReceiptsStatisticsByHistory(DBCon dbCon, Map<String, Object> params) throws Exception{
		List<BusinessStatistics> list = new ArrayList<BusinessStatistics>();
		BusinessStatistics item = null;
		List<DailySettle> dailySettleList = new ArrayList<DailySettle>();
		DailySettle dailySettleItem = null;
		List<Order> orderList = null;
		Order orderItem = null;
		String querySQL = "";
		if(params == null || params.get("pin") == null || params.get("restaurantID") == null){
			throw new BusinessException("操作失败, 读取查询参数或操作人员验证信息失败, 请检查输入参数!");
		}
//		Object pin = params.get("pin");
		Object restaurantID = params.get("restaurantID");
		Object onDuty = params.get("onDuty");
		Object offDuty = params.get("offDuty");
		Date dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(onDuty.toString());
		Date dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(offDuty.toString());
		
		if(onDuty == null || offDuty == null){
			throw new BusinessException("操作失败, 读取查询时间失败, 请检查输入参数!");
		}
		
		//***************************************************
//		System.out.println("begin: "+ DateUtil.formatToLocalhost.format(new Date()));
		
		// 获取日结时间范围
		querySQL = "SELECT id, restaurant_id, name, on_duty, off_duty"
				 + " FROM " + Params.dbName + ".daily_settle_history "
				 + " WHERE restaurant_id = " + restaurantID
				 + " AND off_duty >= '" + onDuty.toString() + "' "
				 + " AND off_duty <= '" + offDuty.toString() + "'";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			dailySettleItem = new DailySettle();
			dailySettleItem.setId(dbCon.rs.getInt("id"));
			dailySettleItem.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			dailySettleItem.setName(dbCon.rs.getString("name"));
			dailySettleItem.setOnDuty(dbCon.rs.getTimestamp("on_duty").getTime());
			dailySettleItem.setOffDuty(dbCon.rs.getTimestamp("off_duty").getTime());
			dailySettleList.add(dailySettleItem);
			dailySettleItem = null;
		}
		dbCon.rs.close();
		dbCon.rs = null;
		
		Calendar c = Calendar.getInstance();
		c.setTime(dateBegin);
		
		while (dateBegin.compareTo(dateEnd) <= 0) {
			c.add(Calendar.DATE, 1);
			item = new BusinessStatistics();
			item.setRestaurantID(Integer.valueOf(restaurantID.toString()));
			item.setOnDuty(dateBegin);
			item.setOffDuty(dateBegin);
			// 计算某一天所有日结信息中最小开始时间和最大结束时间
			for(int i = dailySettleList.size() - 1 ; i >= 0; i--){
				DailySettle temp = dailySettleList.get(i);
				if(DateUtil.formatToDate(temp.getOffDuty()).equals(DateUtil.formatToDate(item.getOffDuty()))){
					if(temp.getOffDuty() > item.getOffDuty()){
						item.setOffDuty(temp.getOffDuty());
					}
					if(temp.getOnDuty() < item.getOnDuty()){
						item.setOnDuty(temp.getOnDuty());
					}
					
					dailySettleList.remove(i);
				}
			}
			// 读取某天实际日结账单记录
			orderList = new ArrayList<Order>();
			querySQL = "SELECT OH.id, OH.pay_type, OH.category, OH.status, OH.service_rate, OH.total_price, OH.actual_price, "
					 + " OH.gift_price, OH.cancel_price, OH.discount_price, OH.erase_price, OH.repaid_price"
					 + " FROM " + Params.dbName + ".order_history OH"
					 + " WHERE OH.restaurant_id = " + item.getRestaurantID()
					 + " AND OH.order_date BETWEEN '" + item.getOnDutyToSimple() + "' AND '" + item.getOffDutyToSimple() + "' ";
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			while(dbCon.rs != null && dbCon.rs.next()){
				orderItem = new Order();
				orderItem.setId(dbCon.rs.getInt("id"));
				orderItem.setPaymentType(dbCon.rs.getShort("pay_type"));
				orderItem.setCategory(dbCon.rs.getShort("category"));
				orderItem.setStatus(dbCon.rs.getShort("status"));
				orderItem.setServiceRate(dbCon.rs.getFloat("service_rate"));
				orderItem.setTotalPrice(dbCon.rs.getFloat("total_price"));
				orderItem.setActualPrice(dbCon.rs.getFloat("actual_price"));
				orderItem.setGiftPrice(dbCon.rs.getFloat("gift_price"));
				orderItem.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
				orderItem.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
				orderItem.setErasePrice(dbCon.rs.getInt("erase_price"));
				orderItem.setRepaidPrice(dbCon.rs.getFloat("repaid_price"));
				
				orderList.add(orderItem);
				orderItem = null;
			}
			dbCon.rs.close();
			dbCon.rs = null;
			
			// 计算某天汇总信息
			item = BusinessStatisticsDao.calcBusinessReceiptsStatistics(item, orderList);
			
			list.add(item);
			item = null;
			dateBegin = c.getTime();
		}
		
		//***************************************************
//		System.out.println("end: "+ DateUtil.formatToLocalhost.format(new Date()));
		
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<BusinessStatistics> getBusinessReceiptsStatisticsByHistory(Map<String, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return BusinessStatisticsDao.getBusinessReceiptsStatisticsByHistory(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param staffId
	 * @param onDuty
	 * @param offDuty
	 * @return
	 * @throws Exception
	 */
	public static BusinessStatistics getBusinessStatistics(Map<Object, Object> params) throws Exception{
		BusinessStatistics bs = null;
		DBCon dbCon = new DBCon();
		Object pin, onDuty, offDuty, queryPattern;
		try{
			if(params == null || !DateType.hasType(params)){
				return null;
			}
			pin = params.get("pin");
			onDuty = params.get("onDuty");
			offDuty = params.get("offDuty");
			queryPattern = params.get("queryPattern");
			dbCon.connect();
			if(queryPattern == null || queryPattern.toString().equals("1")){
				DutyRange duty = QueryDutyRange.exec(dbCon, StaffDao.verify(dbCon, Integer.parseInt(pin.toString())), onDuty.toString(), offDuty.toString());
				if(duty != null){
					ShiftDetail res = QueryShiftDao.exec(dbCon, StaffDao.verify(Integer.parseInt(pin.toString())), duty.getOnDutyFormat(), duty.getOffDutyFormat(), DateType.getValue(params));
					bs = new BusinessStatistics(res);				
				}
			}else{
				if(queryPattern.toString().equals("2")){
					ShiftDetail res = QueryShiftDao.exec(dbCon, StaffDao.verify(Integer.parseInt(pin.toString())), onDuty.toString(), offDuty.toString(), DateType.getValue(params));
					bs = new BusinessStatistics(res);
				}
			}
		}finally{
			dbCon.disconnect();
		}
		return bs;
	}
	
}
