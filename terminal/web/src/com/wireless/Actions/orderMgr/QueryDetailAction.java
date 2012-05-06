package com.wireless.Actions.orderMgr;

//import gzds.cxwh.control.Actions.jsonProcessor.DateJsonValueProcessor;
//import gzds.cxwh.control.Actions.jsonProcessor.NumberJsonValueProcessor;

import java.io.IOException;
import java.io.PrintWriter;
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
import com.wireless.dbObject.SingleOrderFood;
import com.wireless.dbReflect.SingleOrderFoodReflector;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

public class QueryDetailAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		int orderID = 0;

		// String jsonResp = "{success:$(result), data:'$(value)'}";
		PrintWriter out = null;

		// mod by ZTF @10/02;
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int index = Integer.parseInt(start);
		int pageSize = Integer.parseInt(limit);

		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> outputList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, List<HashMap<String, Object>>> rootMap = new HashMap<String, List<HashMap<String, Object>>>();

		boolean isError = false;
		// end mod;

		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. pin=0x1 & orderID=40
			 */
			String pin = request.getParameter("pin");

			orderID = Integer.parseInt(request.getParameter("orderID"));
			String queryType = request.getParameter("queryType");

			dbCon.connect();

			VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);

			// int nCount = 0;
			// StringBuffer value = new StringBuffer();

			SingleOrderFood[] singleOrderFoods = null;
			if (queryType.equals("Today")) {
				singleOrderFoods = SingleOrderFoodReflector.getDetailToday(
						dbCon, "AND A.order_id=" + orderID, "");
			} else {
				singleOrderFoods = SingleOrderFoodReflector.getDetailHistory(
						dbCon, "AND A.order_id=" + orderID, "");
			}

			for (SingleOrderFood singleOrderFood : singleOrderFoods) {
				/**
				 * The json to each order detail looks like below
				 * [日期,名称,单价,数量,折扣,口味,口味价钱,厨房,服务员,备注]
				 */
				// mod by ZTF @10/02;
				HashMap<String, Object> resultMay = new HashMap<String, Object>();
				resultMay.put("order_date", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss")
						.format(singleOrderFood.orderDate));
				resultMay.put("food_name", singleOrderFood.food.name);
				resultMay.put("unit_price", singleOrderFood.unitPrice);
				resultMay.put("amount", singleOrderFood.orderCount);
				resultMay.put("discount", singleOrderFood.discount);
				resultMay.put("taste_pref",
						singleOrderFood.taste.preference.replaceAll(",", "；"));
				resultMay.put("taste_price", singleOrderFood.taste.getPrice());
				resultMay.put("kitchen", singleOrderFood.kitchen.name);
				resultMay.put("waiter", singleOrderFood.staff.name);
				resultMay.put("comment", singleOrderFood.comment);
				resultMay.put("isPaid", singleOrderFood.isPaid);
				resultMay.put("isDiscount", singleOrderFood.isDiscount());
				resultMay.put("isGift", singleOrderFood.isGift());
				resultMay.put("isReturn", singleOrderFood.isCancelled());
				resultMay.put("message", "normal");

				resultList.add(resultMay);
			}

			// String sql = "SELECT a.*, (CASE WHEN b.kitchen_alias = "
			// + Kitchen.KITCHEN_TEMP + " THEN '临时' "
			// + "WHEN b.kitchen_alias IS NULL THEN '已刪除廚房' "
			// + " ELSE b.name END) AS kitchen_name FROM " + Params.dbName
			// + ".order_food a LEFT OUTER JOIN " + Params.dbName
			// + ".kitchen b ON (a.kitchen_id=b.kitchen_id)"
			// + "WHERE a.order_id=" + orderID + "  AND a.restaurant_id="
			// + term.restaurant_id;

			// dbCon.rs = dbCon.stmt.executeQuery(sql);
			// while (dbCon.rs.next()) {
			// // the string is separated by comma
			// // if (nCount != 0) {
			// // value.append("，");
			// // }
			//
			// /**
			// * The json to each order detail looks like below
			// * [日期,名称,单价,数量,折扣,口味,口味价钱,厨房,服务员,备注]
			// */
			// // mod by ZTF @10/02;
			// HashMap resultMay = new HashMap();
			// resultMay.put("order_date", new SimpleDateFormat(
			// "yyyy-MM-dd HH:mm:ss").format(dbCon.rs
			// .getTimestamp("order_date")));
			// resultMay.put("food_name", dbCon.rs.getString("name"));
			// resultMay.put("unit_price",
			// Float.toString(dbCon.rs.getFloat("unit_price")));
			// resultMay.put("amount",
			// Float.toString(dbCon.rs.getFloat("order_count")));
			// resultMay.put("discount",
			// Float.toString(dbCon.rs.getFloat("discount")));
			// resultMay.put("taste_pref", dbCon.rs.getString("taste")
			// .replaceAll(",", "；"));
			// resultMay.put("taste_price",
			// Float.toString(dbCon.rs.getFloat("taste_price")));
			// resultMay.put("kitchen", dbCon.rs.getString("kitchen_name"));
			// resultMay.put("waiter", dbCon.rs.getString("waiter"));
			// resultMay.put("comment", dbCon.rs.getString("comment"));
			// resultMay.put("message", "normal");
			//
			// resultList.add(resultMay);
			// // end mod;
			// }
			//
			// // if (nCount == 0) {
			// // jsonResp = jsonResp.replace("$(value)", "");
			// // } else {
			// // jsonResp = jsonResp.replace("$(value)", value);
			// // }
			// dbCon.rs.close();

			// jsonResp = jsonResp.replace("$(result)", "true");

		} catch (BusinessException e) {
			e.printStackTrace();
			// jsonResp = jsonResp.replace("$(result)", "false");
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				// jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");
				// mod by ZTF @10/02;
				HashMap<String, Object> resultMay = new HashMap<String, Object>();
				resultMay.put("message", "没有获取到餐厅信息，请重新确认");
				resultList.add(resultMay);
				isError = true;
				// end mod;

			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
				// jsonResp = jsonResp.replace("$(value)", "终端已过期，请重新确认");
				// mod by ZTF @10/02;
				HashMap<String, Object> resultMay = new HashMap<String, Object>();
				resultMay.put("message", "终端已过期，请重新确认");
				resultList.add(resultMay);
				isError = true;
				// end mod;

			} else {
				// jsonResp = jsonResp.replace("$(value)", "没有获取到账单(id=" +
				// orderID + ")的详细信息，请重新确认");
				// mod by ZTF @10/02;
				HashMap<String, Object> resultMay = new HashMap<String, Object>();
				resultMay.put("message", "没有获取到账单(id=" + orderID
						+ ")的详细信息，请重新确认");
				resultList.add(resultMay);
				isError = true;
				// end mod;
			}

		} catch (SQLException e) {
			e.printStackTrace();
			// jsonResp = jsonResp.replace("$(result)", "false");
			// jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			// mod by ZTF @10/02;
			HashMap<String, Object> resultMay = new HashMap<String, Object>();
			resultMay.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMay);
			isError = true;
			// end mod;

		} catch (IOException e) {
			e.printStackTrace();
			// jsonResp = jsonResp.replace("$(result)", "false");
			// jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			// mod by ZTF @10/02;
			HashMap<String, Object> resultMay = new HashMap<String, Object>();
			resultMay.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMay);
			isError = true;
			// end mod;

		} finally {
			dbCon.disconnect();
			// just for debug
			// System.out.println(jsonResp);

			// mod by ZTF @10/02;
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

			JsonConfig jsonConfig = new JsonConfig();
			// 解决数字类型null显示为0的问题
			// jsonConfig.registerDefaultValueProcessor(java.lang.Long.class,
			// new NumberJsonValueProcessor());
			// jsonConfig.registerDefaultValueProcessor(java.lang.Double.class,
			// new NumberJsonValueProcessor());
			// jsonConfig.registerDefaultValueProcessor(java.lang.Integer.class,
			// new NumberJsonValueProcessor());
			// // 解决日期类型显示问题
			// jsonConfig.registerJsonValueProcessor(java.util.Date.class,
			// new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
			// jsonConfig.registerJsonValueProcessor(java.sql.Timestamp.class,
			// new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
			// JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);
			// HashMap testMap = new HashMap();
			// testMap.put("root", "test");
			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			String outputJson = "{\"totalProperty\":" + resultList.size() + ","
					+ obj.toString().substring(1);

			// System.out.println(outputJson);

			// out.write(jsonResp);
			out.write(outputJson);
			// end mod;
		}

		return null;
	}
}
