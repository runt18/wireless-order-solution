package com.wireless.Actions.orderMgr;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.VerifyPin;
import com.wireless.db.orderMgr.QueryOrderDao;
import com.wireless.db.orderMgr.QueryOrderFoodDao;
import com.wireless.dbObject.SingleOrderFood;
import com.wireless.dbReflect.SingleOrderFoodReflector;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Table;
import com.wireless.protocol.TasteGroup;
import com.wireless.protocol.Terminal;

public class QueryDetailAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int index = Integer.parseInt(start);
		int pageSize = Integer.parseInt(limit);

		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> outputList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, List<HashMap<String, Object>>> rootMap = new HashMap<String, List<HashMap<String, Object>>>();

		boolean isError = false;
		try {
			response.setContentType("text/json; charset=utf-8");
			/**
			 * The parameters looks like below. pin=0x1 & orderID=40
			 */
			String pin = request.getParameter("pin");
			String oid = request.getParameter("orderID");
			String rid = request.getParameter("restaurantID");
			String talias = request.getParameter("tableAlias");
			
			int orderID = oid != null && !oid.trim().isEmpty() ? Integer.parseInt(oid) : 0;
			int restaurantID = rid != null && !rid.trim().isEmpty() ? Integer.parseInt(rid) : 0;
			int tableAlias = talias != null && !talias.trim().isEmpty() ? Integer.parseInt(talias) : 0;
			String queryType = request.getParameter("queryType");

			dbCon.connect();

//			VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
			
			OrderFood[] orderFoods = null;
			if (queryType.equals("Today")) {
				orderFoods = QueryOrderFoodDao.getSingleDetailToday(dbCon, " AND OF.order_id=" + orderID, " ORDER BY OF.order_date ");
			}else if (queryType.equals("TodayByTbl")) {
				Table t = new Table();
				t.setRestaurantId(Integer.valueOf(restaurantID));
				t.aliasID = Integer.valueOf(tableAlias);
				orderFoods = QueryOrderFoodDao.getSingleDetailTodayByTable(null,null,t);
			}else {
				orderFoods = QueryOrderFoodDao.getSingleDetailHistory(dbCon, " AND OFH.order_id=" + orderID, " ORDER BY OFH.order_date ");
			}
			
			for(OrderFood temp : orderFoods){
				HashMap<String, Object> resultMay = new HashMap<String, Object>();
				resultMay.put("order_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(temp.orderDate));
				resultMay.put("food_name", temp.name);
				resultMay.put("unit_price", temp.getPrice());
				resultMay.put("amount", temp.getCount());
				resultMay.put("discount", temp.getDiscount());
				resultMay.put("taste_pref",	temp.hasTaste() ? temp.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
				resultMay.put("taste_price", temp.hasTaste() ? temp.getTasteGroup().getTastePrice() : 0);
				resultMay.put("kitchen", temp.kitchen.getName());
				resultMay.put("waiter", temp.waiter);
				resultMay.put("isPaid", temp.isRepaid());
				resultMay.put("isDiscount", temp.getDiscount() != 1.00f ? true : false);
				resultMay.put("isGift", temp.isGift());
				resultMay.put("isReturn", temp.getCount() < 0 ? true : false);
				resultMay.put("cancelReason", temp.hasCancelReason() ? temp.getCancelReason().getReason() : "--");

				resultList.add(resultMay);
			}
//		} catch (BusinessException e) {
//			e.printStackTrace();
//			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
//				HashMap<String, Object> resultMay = new HashMap<String, Object>();
//				resultMay.put("message", "没有获取到餐厅信息，请重新确认");
//				resultList.add(resultMay);
//				isError = true;
//			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
//				HashMap<String, Object> resultMay = new HashMap<String, Object>();
//				resultMay.put("message", "终端已过期，请重新确认");
//				resultList.add(resultMay);
//				isError = true;
//			} else {
//				HashMap<String, Object> resultMay = new HashMap<String, Object>();
//				resultMay.put("message", "没有获取到账单的详细信息，请重新确认");
//				resultList.add(resultMay);
//				isError = true;
//			}
		} catch (SQLException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMay = new HashMap<String, Object>();
			resultMay.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMay);
			isError = true;
		} catch (Exception e) {
			e.printStackTrace();
			HashMap<String, Object> resultMay = new HashMap<String, Object>();
			resultMay.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMay);
			isError = true;
		} finally {
			dbCon.disconnect();
			if (isError) {
				rootMap.put("root", resultList);
			} else {
				for (int i = index; i < pageSize + index; i++) {
					try {
						outputList.add(resultList.get(i));
					} catch (Exception e) {
						// 最后一页可能不足一页，会报错，忽略
					}
				}
				rootMap.put("root", outputList);
			}

			JSONObject obj = JSONObject.fromObject(rootMap);
			String outputJson = "{\"totalProperty\":" + resultList.size() + "," + obj.toString().substring(1);
			response.getWriter().print(outputJson);
		}

		return null;
	}
	
//	public ActionForward execute(ActionMapping mapping, ActionForm form,
//			HttpServletRequest request, HttpServletResponse response)
//			throws Exception {
//
//		DBCon dbCon = new DBCon();
//
//		String start = request.getParameter("start");
//		String limit = request.getParameter("limit");
//		int index = Integer.parseInt(start);
//		int pageSize = Integer.parseInt(limit);
//
//		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
//		List<HashMap<String, Object>> outputList = new ArrayList<HashMap<String, Object>>();
//		HashMap<String, List<HashMap<String, Object>>> rootMap = new HashMap<String, List<HashMap<String, Object>>>();
//
//		boolean isError = false;
//		try {
//			response.setContentType("text/json; charset=utf-8");
//			/**
//			 * The parameters looks like below. pin=0x1 & orderID=40
//			 */
//			String pin = request.getParameter("pin");
//			String oid = request.getParameter("orderID");
//			String rid = request.getParameter("restaurantID");
//			String talias = request.getParameter("tableAlias");
//			
//			int orderID = oid != null && !oid.trim().isEmpty() ? Integer.parseInt(oid) : 0;
//			int restaurantID = rid != null && !rid.trim().isEmpty() ? Integer.parseInt(rid) : 0;
//			int tableAlias = talias != null && !talias.trim().isEmpty() ? Integer.parseInt(talias) : 0;
//			String queryType = request.getParameter("queryType");
//
//			dbCon.connect();
//
//			VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);
//
//			SingleOrderFood[] singleOrderFoods = null;
//			if (queryType.equals("Today")) {
//				singleOrderFoods = SingleOrderFoodReflector.getDetailToday(dbCon, "AND B.id=" + orderID, "");
//			}else if (queryType.equals("TodayByTbl")) {
//				singleOrderFoods = SingleOrderFoodReflector.getDetailToday(dbCon, " AND B.total_price IS NULL " +
//																				  " AND B.table_alias=" + tableAlias +
//																				  " AND A.restaurant_id=" + restaurantID, "");
//			}else {
//				singleOrderFoods = SingleOrderFoodReflector.getDetailHistory(dbCon, "AND B.id=" + orderID, "");
//			}
//
//			for (SingleOrderFood singleOrderFood : singleOrderFoods) {
//				/**
//				 * The json to each order detail looks like below
//				 * [日期,名称,单价,数量,折扣,口味,口味价钱,厨房,服务员,备注]
//				 */
//				HashMap<String, Object> resultMay = new HashMap<String, Object>();
//				resultMay.put("order_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(singleOrderFood.orderDate));
//				resultMay.put("food_name", singleOrderFood.food.name);
//				resultMay.put("unit_price", singleOrderFood.unitPrice);
//				resultMay.put("amount", singleOrderFood.orderCount);
//				resultMay.put("discount", singleOrderFood.discount);
//				resultMay.put("taste_pref",	singleOrderFood.hasTaste() ? singleOrderFood.tasteGroup.getTastePref().replaceAll(",", "；") : TasteGroup.NO_TASTE_PREF);
//				resultMay.put("taste_price", singleOrderFood.hasTaste() ? singleOrderFood.tasteGroup.getTastePrice() : 0);
//				resultMay.put("kitchen", singleOrderFood.kitchen.name);
//				resultMay.put("waiter", singleOrderFood.staff.name);
//				resultMay.put("comment", "");
//				resultMay.put("isPaid", singleOrderFood.isPaid);
//				resultMay.put("isDiscount", singleOrderFood.isDiscount());
//				resultMay.put("isGift", singleOrderFood.isGift());
//				resultMay.put("isReturn", singleOrderFood.isCancelled());
//				resultMay.put("cancelReason", singleOrderFood.cancelReason == null || singleOrderFood.cancelReason.trim().isEmpty() ? "--" : singleOrderFood.cancelReason.trim());
//				resultMay.put("message", "normal");
//
//				resultList.add(resultMay);
//			}
//			
//		} catch (BusinessException e) {
//			e.printStackTrace();
//			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
//				HashMap<String, Object> resultMay = new HashMap<String, Object>();
//				resultMay.put("message", "没有获取到餐厅信息，请重新确认");
//				resultList.add(resultMay);
//				isError = true;
//			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
//				HashMap<String, Object> resultMay = new HashMap<String, Object>();
//				resultMay.put("message", "终端已过期，请重新确认");
//				resultList.add(resultMay);
//				isError = true;
//			} else {
//				HashMap<String, Object> resultMay = new HashMap<String, Object>();
//				resultMay.put("message", "没有获取到账单的详细信息，请重新确认");
//				resultList.add(resultMay);
//				isError = true;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//			HashMap<String, Object> resultMay = new HashMap<String, Object>();
//			resultMay.put("message", "数据库请求发生错误，请确认网络是否连接正常");
//			resultList.add(resultMay);
//			isError = true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			HashMap<String, Object> resultMay = new HashMap<String, Object>();
//			resultMay.put("message", "数据库请求发生错误，请确认网络是否连接正常");
//			resultList.add(resultMay);
//			isError = true;
//		} finally {
//			dbCon.disconnect();
//			if (isError) {
//				rootMap.put("root", resultList);
//			} else {
//				for (int i = index; i < pageSize + index; i++) {
//					try {
//						outputList.add(resultList.get(i));
//					} catch (Exception e) {
//						// 最后一页可能不足一页，会报错，忽略
//					}
//				}
//				rootMap.put("root", outputList);
//			}
//
//			JSONObject obj = JSONObject.fromObject(rootMap);
//			String outputJson = "{\"totalProperty\":" + resultList.size() + "," + obj.toString().substring(1);
//			response.getWriter().print(outputJson);
//		}
//
//		return null;
//	}
}
