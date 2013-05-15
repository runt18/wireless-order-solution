package com.wireless.Actions.orderMgr;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.orderMgr.QueryOrderFoodDao;
import com.wireless.pojo.regionMgr.Table;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.protocol.OrderFood;

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
//			String pin = request.getParameter("pin");
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
				t.setTableAlias(Integer.valueOf(tableAlias));
				orderFoods = QueryOrderFoodDao.getSingleDetailTodayByTable(null,null,t);
			}else {
				orderFoods = QueryOrderFoodDao.getSingleDetailHistory(dbCon, " AND OFH.order_id=" + orderID, " ORDER BY OFH.order_date ");
			}
			
			for(OrderFood temp : orderFoods){
				HashMap<String, Object> resultMay = new HashMap<String, Object>();
				resultMay.put("order_date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(temp.getOrderDate()));
				resultMay.put("food_name", temp.getName());
				resultMay.put("unit_price", temp.getPrice());
				resultMay.put("amount", temp.getCount());
				resultMay.put("discount", temp.getDiscount());
				resultMay.put("taste_pref",	temp.hasTaste() ? temp.getTasteGroup().getTastePref() : TasteGroup.NO_TASTE_PREF);
				resultMay.put("taste_price", temp.hasTaste() ? temp.getTasteGroup().getTastePrice() : 0);
				resultMay.put("kitchen", temp.getKitchen().getName());
				resultMay.put("waiter", temp.getWaiter());
				resultMay.put("isPaid", temp.isRepaid());
				resultMay.put("isDiscount", temp.getDiscount() != 1.00f ? true : false);
				resultMay.put("isGift", temp.asFood().isGift());
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
}
