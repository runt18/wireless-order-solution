package com.wireless.Actions.billStatistics;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

public class DiscountStatisticsDetailAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		List resultList = new ArrayList();
		HashMap rootMap = new HashMap();

		boolean isError = false;
		float allTotalAmount = 0;

		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. 1st example, filter the order
			 * whose id equals 321 pin=0x1 & type=1 & ope=1 & value=321 2nd
			 * example, filter the order date greater than or equal 2011-7-14
			 * 14:30:00 pin=0x1 & type=3 & ope=2 & value=2011-7-14 14:30:00
			 * 
			 * pin : the pin the this terminal foodIDs : array
			 * "food1,food2,food3" dateBegin: dateEnd :
			 */

			String pin = request.getParameter("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			// get the query condition
			String beginDate = request.getParameter("beginDate");
			String endDate = request.getParameter("endDate");
			String staffs = request.getParameter("staffs");
			String StatisticsType = request.getParameter("StatisticsType");

			String condition = " AND A.waiter IN (" + staffs + ") ";
			if (!beginDate.equals("")) {
				condition = condition + " AND A.order_date >= '" + beginDate
						+ "' ";
			}
			if (!endDate.equals("")) {
				condition = condition + " AND B.total_price IS NOT NULL AND A.order_date <= '" + endDate
						+ "' ";
			}

			condition = condition + " AND A.restaurant_id =  "
					+ term.restaurant_id;

			SingleOrderFoodReflector foodRef = new SingleOrderFoodReflector();
			String orderClause = " ORDER BY A.order_id, A.order_date ";

			SingleOrderFood SingleOrderFoods[] = null;
			if (StatisticsType.equals("Today")) {
				SingleOrderFoods = foodRef.getDetailToday(dbCon, condition,
						orderClause);
			} else if (StatisticsType.equals("History")) {
				SingleOrderFoods = foodRef.getDetailHistory(dbCon, condition,
						orderClause);
			}

			/**
			 * The json to each order looks like below
			 * 
			 */
			for (int i = 0; i < SingleOrderFoods.length; i++) {

				HashMap resultMap = new HashMap();
				resultMap.put("orderID", SingleOrderFoods[i].orderID);
				resultMap.put("datetime", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss")
						.format(SingleOrderFoods[i].orderDate));
				resultMap.put("foodName", SingleOrderFoods[i].food.name);
				resultMap.put("singlePrice", SingleOrderFoods[i].unitPrice);
				resultMap.put("count", SingleOrderFoods[i].orderCount);
				resultMap.put("discount", SingleOrderFoods[i].discount);
				resultMap.put("taste", SingleOrderFoods[i].taste.preference);
				resultMap.put("tastePrice",
						SingleOrderFoods[i].taste.getPrice());
				resultMap.put("kitchenID",
						SingleOrderFoods[i].kitchen.kitchenID);
				// resultMap.put("kitchenName",
				// dbCon.rs.getString("kitchenName"));
				resultMap.put("staffName", SingleOrderFoods[i].staff.name);
				resultMap.put("amount",
						SingleOrderFoods[i].calcPriceWithTaste());

				resultMap.put("message", "normal");

				resultList.add(resultMap);

				allTotalAmount = (float) Math
						.round((allTotalAmount + SingleOrderFoods[i]
								.calcPriceWithTaste()) * 100) / 100;

			}

			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
				resultMap.put("message", "没有获取到餐厅信息，请重新确认");

			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
				resultMap.put("message", "终端已过期，请重新确认");

			} else {
				resultMap.put("message", "没有获取到信息，请重新确认");
			}
			resultList.add(resultMap);
			isError = true;
		} catch (SQLException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;

		} catch (IOException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;

		} finally {
			dbCon.disconnect();

			if (isError) {
				rootMap.put("root", resultList);
			} else {

				DecimalFormat fnum = new DecimalFormat("##0.00");
				HashMap resultMap = new HashMap();
				resultMap.put("kitchenID", "SUM");
				resultMap.put("waiter", "合计：");
				resultMap.put("amount", allTotalAmount);
				resultMap.put("message", "normal");
				resultList.add(resultMap);

				rootMap.put("root", resultList);
			}

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			// String outputJson = "{\"totalProperty\":" + resultList.size() +
			// ","
			// + obj.toString().substring(1);

			String outputJson = obj.toString();

			// System.out.println(outputJson);

			out.write(outputJson);
		}

		return null;
	}
}
