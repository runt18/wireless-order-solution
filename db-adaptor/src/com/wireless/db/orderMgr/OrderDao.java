package com.wireless.db.orderMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.util.WebParams;

public class OrderDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<Order> getOrderByToday(DBCon dbCon, Map<String, Object> params) throws Exception{
		List<Order> list = new ArrayList<Order>();
		Order item = null;
		Object extra = null, orderBy = null;
		if(params != null){
			extra = params.get(WebParams.SQL_PARAMS_EXTRA);
			orderBy = params.get(WebParams.SQL_PARAMS_ORDERBY);
		}
		
		String querySQL = " SELECT "
						+ " A.id, A.order_date, A.seq_id, A.custom_num, A.table_id, A.table_alias, A.table_name, A.table2_id, A.table2_alias, A.table2_name, "
						+ " A.region_id, A.region_name, A.restaurant_id, A.type, A.category, A.discount_id, A.service_rate, "
						+ " A.gift_price, A.cancel_price, A.discount_price, A.erase_price, A.total_price, A.total_price_2, "
						+ " A.waiter, A.status "
						+ " FROM " + Params.dbName + ".order A"
						+ " WHERE 1=1 AND A.seq_id IS NOT NULL "
						+ (extra != null  ? " " + extra : "")
						+ (orderBy != null ? " " + orderBy : "");
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
			item.setTableID2(dbCon.rs.getInt("table2_id"));
			item.setTableAlias2(dbCon.rs.getInt("table2_alias"));
			item.setTableName2(dbCon.rs.getString("table2_name"));
			item.setRegionID(dbCon.rs.getInt("region_id"));
			item.setRegionName(dbCon.rs.getString("region_name"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setPayManner(dbCon.rs.getShort("type"));
			item.setCategory(dbCon.rs.getShort("category"));
			item.setDiscountID(dbCon.rs.getInt("discount_id"));
			item.setServiceRate(dbCon.rs.getFloat("service_rate"));
			item.setGiftPrice(dbCon.rs.getFloat("gift_price"));
			item.setCancelPrice(dbCon.rs.getFloat("cancel_price"));
			item.setDiscountPrice(dbCon.rs.getFloat("discount_price"));
			item.setErasePuotaPrice(dbCon.rs.getInt("erase_price"));
			item.setTotalPrice(dbCon.rs.getFloat("total_price"));
			item.setActuralPrice(dbCon.rs.getFloat("total_price_2"));
			item.setWaiter(dbCon.rs.getString("waiter"));
			item.setStatus(dbCon.rs.getShort("status"));
			
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
	public static List<Order> getOrderByToday(Map<String, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getOrderByToday(dbCon, params);
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
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(WebParams.SQL_PARAMS_EXTRA, " AND A.order_id = " + orderID);
		return getOrderByToday(params).get(0);
	}
}
