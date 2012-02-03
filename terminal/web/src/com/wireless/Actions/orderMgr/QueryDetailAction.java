package com.wireless.Actions.orderMgr;

//import gzds.cxwh.control.Actions.jsonProcessor.DateJsonValueProcessor;
//import gzds.cxwh.control.Actions.jsonProcessor.NumberJsonValueProcessor;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

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

		List resultList = new ArrayList();
		List outputList = new ArrayList();
		HashMap rootMap = new HashMap();

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
			if (pin.startsWith("0x") || pin.startsWith("0X")) {
				pin = pin.substring(2);
			}

			orderID = Integer.parseInt(request.getParameter("orderID"));

			dbCon.connect();

			Terminal term = VerifyPin.exec(dbCon, Integer.parseInt(pin, 16),
					Terminal.MODEL_STAFF);

			int nCount = 0;
			StringBuffer value = new StringBuffer();

			// String sql = "SELECT a.*, b.name AS kitchen_name FROM "
			// + Params.dbName + ".order_food a, " + Params.dbName
			// + ".kitchen b " + "WHERE order_id=" + orderID
			// + " AND a.kitchen=b.alias_id AND b.restaurant_id="
			// + term.restaurant_id;
			
			String sql = "SELECT a.*, (CASE WHEN b.name IS NOT NULL THEN b.name ELSE '临时' END) AS kitchen_name FROM "
					+ Params.dbName + ".order_food a LEFT OUTER JOIN " + Params.dbName
					+ ".kitchen b ON (a.kitchen=b.alias_id AND a.restaurant_id = b.restaurant_id)" + 
					"WHERE a.order_id=" + orderID
					+ "  AND a.restaurant_id="
					+ term.restaurant_id;
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while (dbCon.rs.next()) {
				// the string is separated by comma
				if (nCount != 0) {
					value.append("，");
				}

				/**
				 * The json to each order detail looks like below
				 * [日期,名称,单价,数量,折扣,口味,口味价钱,厨房,服务员,备注]
				 */
				// String jsonOrderDetail =
				// "[$(order_date),$(food_name),$(unit_price),$(amount),$(discount),"
				// +
				// "$(taste_pref),$(taste_price),$(kitchen),$(waiter),$(comment)]";
				// jsonOrderDetail = jsonOrderDetail.replace("$(order_date)",
				// new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				// .format(dbCon.rs.getTimestamp("order_date")));
				// jsonOrderDetail = jsonOrderDetail.replace("$(food_name)",
				// dbCon.rs.getString("name"));
				// jsonOrderDetail = jsonOrderDetail.replace("$(unit_price)",
				// Float.toString(dbCon.rs.getFloat("unit_price")));
				// jsonOrderDetail = jsonOrderDetail.replace("$(amount)",
				// Float.toString(dbCon.rs.getFloat("order_count")));
				// jsonOrderDetail = jsonOrderDetail.replace("$(discount)",
				// Float.toString(dbCon.rs.getFloat("discount")));
				// jsonOrderDetail = jsonOrderDetail.replace("$(taste_pref)",
				// dbCon.rs.getString("taste").replaceAll(",", "；"));
				// jsonOrderDetail = jsonOrderDetail.replace("$(taste_price)",
				// Float.toString(dbCon.rs.getFloat("taste_price")));
				// jsonOrderDetail = jsonOrderDetail.replace("$(kitchen)",
				// dbCon.rs.getString("kitchen_name"));
				// jsonOrderDetail = jsonOrderDetail.replace("$(waiter)",
				// dbCon.rs.getString("waiter"));
				// String comment = dbCon.rs.getString("comment");
				// jsonOrderDetail = jsonOrderDetail.replace("$(comment)",
				// comment != null ? comment : "");
				// // put each json order info to the value
				// value.append(jsonOrderDetail);
				//
				// nCount++;

				// mod by ZTF @10/02;
				HashMap resultMay = new HashMap();
				resultMay.put("order_date", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(dbCon.rs
						.getTimestamp("order_date")));
				resultMay.put("food_name", dbCon.rs.getString("name"));
				resultMay.put("unit_price",
						Float.toString(dbCon.rs.getFloat("unit_price")));
				resultMay.put("amount",
						Float.toString(dbCon.rs.getFloat("order_count")));
				resultMay.put("discount",
						Float.toString(dbCon.rs.getFloat("discount")));
				resultMay.put("taste_pref", dbCon.rs.getString("taste")
						.replaceAll(",", "；"));
				resultMay.put("taste_price",
						Float.toString(dbCon.rs.getFloat("taste_price")));
				resultMay.put("kitchen", dbCon.rs.getString("kitchen_name"));
				resultMay.put("waiter", dbCon.rs.getString("waiter"));
				resultMay.put("comment", dbCon.rs.getString("comment"));
				resultMay.put("message", "normal");

				resultList.add(resultMay);
				// end mod;
			}

			// if (nCount == 0) {
			// jsonResp = jsonResp.replace("$(value)", "");
			// } else {
			// jsonResp = jsonResp.replace("$(value)", value);
			// }
			dbCon.rs.close();

			// jsonResp = jsonResp.replace("$(result)", "true");

		} catch (BusinessException e) {
			e.printStackTrace();
			// jsonResp = jsonResp.replace("$(result)", "false");
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				// jsonResp = jsonResp.replace("$(value)", "没有获取到餐厅信息，请重新确认");
				// mod by ZTF @10/02;
				HashMap resultMay = new HashMap();
				resultMay.put("message", "没有获取到餐厅信息，请重新确认");
				resultList.add(resultMay);
				isError = true;
				// end mod;

			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
				// jsonResp = jsonResp.replace("$(value)", "终端已过期，请重新确认");
				// mod by ZTF @10/02;
				HashMap resultMay = new HashMap();
				resultMay.put("message", "终端已过期，请重新确认");
				resultList.add(resultMay);
				isError = true;
				// end mod;

			} else {
				// jsonResp = jsonResp.replace("$(value)", "没有获取到账单(id=" +
				// orderID + ")的详细信息，请重新确认");
				// mod by ZTF @10/02;
				HashMap resultMay = new HashMap();
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
			HashMap resultMay = new HashMap();
			resultMay.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMay);
			isError = true;
			// end mod;

		} catch (IOException e) {
			e.printStackTrace();
			// jsonResp = jsonResp.replace("$(result)", "false");
			// jsonResp = jsonResp.replace("$(value)", "数据库请求发生错误，请确认网络是否连接正常");
			// mod by ZTF @10/02;
			HashMap resultMay = new HashMap();
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

			//System.out.println(outputJson);

			// out.write(jsonResp);
			out.write(outputJson);
			// end mod;
		}

		return null;
	}
}
