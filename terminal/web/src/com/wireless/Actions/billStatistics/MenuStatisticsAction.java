package com.wireless.Actions.billStatistics;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.orderMgr.QueryOrderFoodDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Terminal;

public class MenuStatisticsAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int index = Integer.parseInt(start);
		int pageSize = Integer.parseInt(limit);

		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> outputList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> rootMap = new HashMap<String, Object>();

		boolean isError = false;
		float totalPrice = 0;

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
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);

			// get the query condition
			String dateBegin = request.getParameter("dateBegin");
			String dateEnd = request.getParameter("dateEnd");
			String foodAlias = request.getParameter("foodAlias");
			String statType = request.getParameter("StatisticsType");

			/**
			 * Select all the today orders matched the conditions below. 1 -
			 * belong to this restaurant 2 - has been paid 3 - match extra
			 * filter condition
			 */
			


			List<OrderFood> orderFoods = null;
			if (statType.equals("Today")) {
				String condition = " AND OF.food_alias IN (" + foodAlias + ") " +
								   " AND O.order_date >= '" + dateBegin + "' " + 
								   " AND O.order_date <= '" + dateEnd + "'" +
								   " AND O.total_price IS NOT NULL AND O.restaurant_id = " + term.restaurantID;

				String orderClause = " ORDER BY food_alias ASC, pay_datetime ";	
				orderFoods = QueryOrderFoodDao.getDetailToday(dbCon, condition, orderClause);
				
			} else if (statType.equals("History")) {
				
				String condition = " AND OFH.food_alias IN (" + foodAlias + ") " +
								   " AND OH.order_date >= '" + dateBegin + "' " + 
								   " AND OH.order_date <= '" + dateEnd + "'" +
								   " AND OH.total_price IS NOT NULL AND OH.restaurant_id = " + term.restaurantID;

				String orderClause = " ORDER BY food_alias ASC, pay_datetime ";
				orderFoods = QueryOrderFoodDao.getDetailHistory(dbCon, condition, orderClause);
			}

			int lastFoodAlias = -100;
			int lastKitchenAlias = -100;
			String lastFoodName = "";
			int rowCount = 0;
			float sumAmout = 0;
			float SumPrice = 0;
			for(OrderFood orderFood : orderFoods){
				int thisFoodAlias = orderFood.getAliasId();

				if (thisFoodAlias != lastFoodAlias) {
					if (rowCount != 0) {
						HashMap<String, Object> resultMap = new HashMap<String, Object>();

						resultMap.put("dishNumber", lastFoodAlias);
						resultMap.put("dishName", lastFoodName);
						// resultMap.put("dishPrice",
						// dbCon.rs.getFloat("unit_price"));
						// resultMap.put("isTemp",
						// orderFoods[i].isTemporary);
						resultMap.put("kitchenAlias", lastKitchenAlias);
						resultMap.put("dishCount", sumAmout);
						resultMap.put("dishTotalPrice", SumPrice);
						resultMap.put("message", "normal");

						resultList.add(resultMap);

					}

					sumAmout = 0;
					SumPrice = 0;
				}

				rowCount = rowCount + 1;
				lastFoodAlias = thisFoodAlias;
				lastKitchenAlias = orderFood.getKitchen().getAliasId();
				lastFoodName = orderFood.getName();

				float allPrice = (float) Math.round(orderFood.calcPriceWithTaste() * 100) / 100;

				totalPrice = (float) Math.round((totalPrice + allPrice) * 100) / 100;

				SumPrice = (float) Math.round((SumPrice + allPrice) * 100) / 100;
				sumAmout = (float) Math
						.round((sumAmout + orderFood.getCount()) * 100) / 100;

			}

			if (totalPrice != 0) {
				HashMap<String, Object> resultMap = new HashMap<String, Object>();

				resultMap.put("dishNumber", lastFoodAlias);
				resultMap.put("dishName", lastFoodName);
				// resultMap.put("dishPrice",
				// dbCon.rs.getFloat("unit_price"));
				// resultMap.put("isTemp", orderFoods[i].isTemporary);
				resultMap.put("kitchen", lastFoodAlias);
				resultMap.put("dishCount", sumAmout);
				resultMap.put("dishTotalPrice", SumPrice);
				resultMap.put("message", "normal");

				resultList.add(resultMap);
			}

			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			if (e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED) {
				resultMap.put("message", "没有获取到餐厅信息，请重新确认");

			} else if (e.getErrCode() == ProtocolError.TERMINAL_EXPIRED) {
				resultMap.put("message", "终端已过期，请重新确认");

			} else {
				resultMap.put("message", "没有获取到当日账单信息，请重新确认");
			}
			resultList.add(resultMap);
			isError = true;
		} catch (SQLException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;

		} catch (IOException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;

		} finally {
			dbCon.disconnect();

			if (isError) {
				rootMap.put("root", resultList);
			} else {
				// 分页
				for (int i = index; i < pageSize + index; i++) {
					try {
						outputList.add(resultList.get(i));
					} catch (Exception e) {
						// 最后一页可能不足一页，会报错，忽略
					}
				}
				DecimalFormat fnum = new DecimalFormat("##0.00");
				String totalPriceDiaplay = fnum.format(totalPrice);
				HashMap<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put("kitchenAlias", "SUM");
				resultMap.put("dishCount", "汇总");
				resultMap.put("dishTotalPrice", totalPriceDiaplay);
				resultMap.put("message", "normal");
				outputList.add(resultMap);

				rootMap.put("root", outputList);
			}

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			String outputJson = "{\"totalProperty\":" + resultList.size() + ","
					+ obj.toString().substring(1);

			// System.out.println(outputJson);

			out.write(outputJson);
		}

		return null;
	}

}
