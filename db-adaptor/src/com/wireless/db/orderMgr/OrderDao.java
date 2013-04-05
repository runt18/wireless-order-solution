package com.wireless.db.orderMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.util.SQLUtil;

public class OrderDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<Order> getOrderByToday(DBCon dbCon, Map<Object, Object> params) throws Exception{
		List<Order> list = new ArrayList<Order>();
		Order item = null;
		String querySQL = " SELECT "
						+ " A.id, A.order_date, A.seq_id, A.custom_num, A.table_id, A.table_alias, A.table_name, A.settle_type, "
						+ " A.region_id, A.region_name, A.restaurant_id, A.pay_type, A.category, A.discount_id, A.service_rate, "
						+ " A.gift_price, A.cancel_price, A.discount_price, A.erase_price, A.total_price, A.actual_price, "
						+ " A.waiter, A.status "
						+ " FROM " + Params.dbName + ".order A"
						+ " WHERE A.seq_id IS NOT NULL ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new Order();
			item.setId(dbCon.rs.getLong("id"));
			item.setSeqID(dbCon.rs.getLong("seq_id"));
			item.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			item.setCustomNum(dbCon.rs.getShort("custom_num"));
			item.setTableID(dbCon.rs.getInt("table_id"));
			item.setTableAlias(dbCon.rs.getInt("table_alias"));
			item.setTableName(dbCon.rs.getString("table_name"));
			item.setRegionID(dbCon.rs.getInt("region_id"));
			item.setRegionName(dbCon.rs.getString("region_name"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setPayType(dbCon.rs.getShort("pay_type"));
			item.setCategory(dbCon.rs.getShort("category"));
			item.setSettleType(dbCon.rs.getInt("settle_type"));
			item.setDiscountID(dbCon.rs.getInt("discount_id"));
			item.setServiceRate(dbCon.rs.getFloat("service_rate"));
			item.setGiftPrice(dbCon.rs.getFloat("gift_price"));
			item.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
			item.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
			item.setErasePuotaPrice(dbCon.rs.getInt("erase_price"));
			item.setTotalPrice(dbCon.rs.getFloat("total_price"));
			item.setActuralPrice(dbCon.rs.getFloat("actual_price"));
			item.setWaiter(dbCon.rs.getString("waiter"));
			item.setStatus(dbCon.rs.getShort("status"));
			
			if(item.isMerger()){
				com.wireless.protocol.Order tempOrder = QueryOrderDao.execByID(Integer.valueOf(item.getId()+""), QueryOrderDao.QUERY_TODAY);
				if(tempOrder.hasChildOrder()){
					Order co = null;
					for(int i = 0; i < tempOrder.getChildOrder().length; i++){
						com.wireless.protocol.Order tpco = tempOrder.getChildOrder()[i];
						co = new Order(tpco);
//						co.setId(tpco.getId());
//						co.setCustomNum(tpco.getCustomNum());
//						co.setOrderDate(tpco.getOrderDate());
//						co.setServiceRate(tpco.getServiceRate());
//						co.setCategory(tpco.getCategory());
//						co.setStatus(Short.valueOf(tpco.getStatus() + ""));
//						co.setMinCost(tpco.getDestTbl().getMinimumCost());
//						co.setRestaurantID(tpco.getRestaurantId());
//						co.setDiscountID(tpco.getDiscount().getId());
//						co.setPayManner(Short.valueOf(tpco.getPaymentType() + ""));
						co.setOrderFoods(null);
//						co.setGiftPrice(tpco.getGiftPrice());
//						co.setDiscountPrice(tpco.getDiscountPrice());
//						co.setCancelPrice(tpco.getCancelPrice());
//						co.setErasePuotaPrice(tpco.getErasePrice());
//						co.setActuralPrice(tpco.getActualPrice());
//						co.setTotalPrice(tpco.calcPriceBeforeDiscount());
						
						item.getChildOrder().add(co);
					}
				}
			}
			
			list.add(item);
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<Order> getOrderByToday(Map<Object, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return OrderDao.getOrderByToday(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param orderID
	 * @return
	 * @throws Exception
	 */
	public static Order getOrderByToday(long orderID) throws Exception{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.order_id = " + orderID);
		List<Order> list = OrderDao.getOrderByToday(params);
		return list!= null && list.size() > 0 ? list.get(0) : null;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<Order> getOrderByHistory(DBCon dbCon, Map<Object, Object> params) throws Exception{
		List<Order> list = new ArrayList<Order>();
		Order item = null;
		String querySQL = " SELECT "
						+ " OH.id, OH.seq_id, OH.restaurant_id, OH.birth_date, OH.order_date, OH.custom_num,  "
						+ " OH.table_id, OH.table_alias, OH.table_name, OH.waiter, OH.pay_type, OH.region_id, OH.region_name,  "
						+ " OH.gift_price, OH.cancel_price, OH.discount_price, OH.erase_price, OH.total_price, OH.repaid_price, OH.actual_price, "
						+ " OH.category, OH.comment, OH.service_rate, OH.status "
						+ " FROM " + Params.dbName + ".order_history OH"
						+ " WHERE OH.seq_id IS NOT NULL ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params); // important
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new Order();
			item.setId(dbCon.rs.getLong("id"));
			item.setSeqID(dbCon.rs.getLong("seq_id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setOrderDate(dbCon.rs.getTimestamp("order_date").getTime());
			item.setCustomNum(dbCon.rs.getInt("custom_num"));
			item.setTableID(dbCon.rs.getInt("table_id"));
			item.setTableAlias(dbCon.rs.getInt("table_alias"));
			item.setTableName(dbCon.rs.getString("table_name"));
			item.setWaiter(dbCon.rs.getString("waiter"));
			item.setPayType(dbCon.rs.getShort("pay_type"));
			item.setRegionID(dbCon.rs.getInt("region_id"));
			item.setRegionName(dbCon.rs.getString("region_name"));
			item.setGiftPrice(dbCon.rs.getFloat("gift_price"));
			item.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
			item.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
			item.setErasePuotaPrice(dbCon.rs.getInt("erase_price"));
			item.setTotalPrice(dbCon.rs.getFloat("total_price"));
			item.setRepaidPrice(dbCon.rs.getFloat("repaid_price"));
			item.setActuralPrice(dbCon.rs.getFloat("actual_price"));
			item.setCategory(dbCon.rs.getShort("category"));
			item.setComment(dbCon.rs.getString("comment"));
			item.setServiceRate(dbCon.rs.getFloat("service_rate"));
			item.setStatus(dbCon.rs.getShort("status"));
			
			if(item.isMerger()){
				com.wireless.protocol.Order tempOrder = QueryOrderDao.execByID(Integer.valueOf(item.getId()+""), QueryOrderDao.QUERY_HISTORY);
				if(tempOrder.hasChildOrder()){
					Order co = null;
					for(int i = 0; i < tempOrder.getChildOrder().length; i++){
						co = new Order();
						com.wireless.protocol.Order tpco = tempOrder.getChildOrder()[i];
						co.setId(tpco.getId());
						co.setCustomNum(tpco.getCustomNum());
						co.setOrderDate(tpco.getOrderDate());
						co.setServiceRate(tpco.getServiceRate());
						co.setCategory(tpco.getCategory());
						co.setStatus(Short.valueOf(tpco.getStatus() + ""));
						co.setMinCost(tpco.getDestTbl().getMinimumCost());
						co.setRestaurantID(tpco.getRestaurantId());
						co.setDiscountID(tpco.getDiscount().getId());
						co.setPayType(Short.valueOf(tpco.getPaymentType() + ""));
						co.setOrderFoods(null);
						co.setGiftPrice(tpco.getGiftPrice());
						co.setDiscountPrice(tpco.getDiscountPrice());
						co.setCancelPrice(tpco.getCancelPrice());
						co.setErasePuotaPrice(tpco.getErasePrice());
						co.setActuralPrice(tpco.getActualPrice());
						co.setTotalPrice(tpco.calcPriceBeforeDiscount());
						item.getChildOrder().add(co);
					}
				}
			}
			list.add(item);
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<Order> getOrderByHistory(Map<Object, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return OrderDao.getOrderByHistory(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param orderID
	 * @return
	 * @throws Exception
	 */
	public static Order getOrderByHistory(long orderID) throws Exception{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND OH.order_id = " + orderID);
		List<Order> list = OrderDao.getOrderByHistory(params);
		return list!= null && list.size() > 0 ? list.get(0) : null;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static Order getOrderByHistorySummary(DBCon dbCon, Map<Object, Object> params) throws Exception{
		Order sum = null;
		String querySQL = "SELECT count(OH.id) count, SUM(OH.custom_num) custom_num, SUM(OH.total_price) total_price, SUM(OH.actual_price) actual_price,"
						+ " SUM(OH.gift_price) gift_price, SUM(OH.cancel_price) cancel_price, SUM(OH.discount_price) discount_price,"
						+ " SUM(OH.erase_price) erase_price, SUM(OH.repaid_price) repaid_price"
						+ " FROM " + Params.dbName + ".order_history OH"
						+ " WHERE OH.seq_id IS NOT NULL ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params); // important
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			sum = new Order();
			sum.setId(dbCon.rs.getLong("count"));
			sum.setCustomNum(dbCon.rs.getInt("custom_num"));
			sum.setRepaidPrice(dbCon.rs.getFloat("repaid_price"));
			sum.setActuralPrice(dbCon.rs.getFloat("actual_price"));
			sum.setGiftPrice(dbCon.rs.getFloat("gift_price"));
			sum.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
			sum.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
			sum.setErasePuotaPrice(dbCon.rs.getInt("erase_price"));
			sum.setTotalPrice(dbCon.rs.getFloat("total_price"));
		}
		return sum;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static Order getOrderByHistorySummary(Map<Object, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return OrderDao.getOrderByHistorySummary(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
}
