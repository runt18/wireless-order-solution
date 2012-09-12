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
import com.wireless.db.VerifyPin;
import com.wireless.dbReflect.OrderFoodReflector;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Terminal;

public class DeptStatisticsAction extends Action {
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
		float allTotalCount = 0;
		float allCashCount = 0;
		float allBankCardCount = 0;
		float allMemberCardCount = 0;
		float allHandCount = 0;
		float allSignCount = 0;
		float allDiscountCount = 0;
		float allGiftCount = 0;

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
			String deptIDs = request.getParameter("deptIDs");
			String StatisticsType = request.getParameter("StatisticsType");

			OrderFood orderFoods[] = null;


			if (StatisticsType.equals("Today")) {

				String condition = " AND A.dept_id IN (" + deptIDs + ") " + 
								   " AND B.order_date >= '" + dateBegin + "'" +
								   " AND B.order_date <= '" + dateEnd + "' " +
								   " AND B.total_price IS NOT NULL AND A.restaurant_id = "+ term.restaurantID;

				String orderClause = " ORDER BY dept_id ";

				orderFoods = OrderFoodReflector.getDetailToday(dbCon, condition, orderClause);
			} else if (StatisticsType.equals("History")) {

				String condition = " AND A.dept_id IN (" + deptIDs + ") " + 
						   		   " AND B.order_date >= '" + dateBegin + "'" +
						   		   " AND B.order_date <= '" + dateEnd + "' " +
						   		   " AND B.total_price IS NOT NULL AND A.restaurant_id = "+ term.restaurantID;

				String orderClause = " ORDER BY dept_id ";

				orderFoods = OrderFoodReflector.getDetailHistory(dbCon, condition, orderClause);
			}

			/**
			 * Select all the today orders matched the conditions below. 1 -
			 * belong to this restaurant 2 - has been paid 3 - match extra
			 * filter condition
			 */
			// String lastDate = "1900-01-01";
			int lastDept = -1;
			float cashCount = 0;
			float bankCardCount = 0;
			float memberCardCount = 0;
			float handCount = 0;
			float signCount = 0;
			float discountCount = 0;
			float giftCount = 0;
			float totalCount = 0;
			int rowCount = 0;
			for (int i = 0; i < orderFoods.length; i++) {
				if (!orderFoods[i].isGift()) {
					OrderFood orderFood = orderFoods[i];
					// String orderDate = new
					// SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					// .format(new Date(orderFood.orderDate));
					int dept = orderFood.kitchen.dept.deptID;

					if (/* !orderDate.equals(lastDate) || */dept != lastDept) {
						if (rowCount != 0) {
							HashMap<String, Object> resultMap = new HashMap<String, Object>();

							// resultMap.put("statDate", lastDate);
							resultMap.put("deptID", lastDept);
							resultMap.put("cash", (float)Math.round(cashCount * 100) / 100);
							resultMap.put("bankCard", (float)Math.round(bankCardCount * 100) / 100);
							resultMap.put("memberCard", (float)Math.round(memberCardCount * 100) / 100);
							resultMap.put("credit", (float)Math.round(handCount * 100) / 100);
							resultMap.put("sign", (float)Math.round(signCount * 100) / 100);
							resultMap.put("discount", (float)Math.round(discountCount * 100) / 100);
							resultMap.put("gift", (float)Math.round(giftCount * 100) / 100);
							resultMap.put("total", (float)Math.round(totalCount * 100) / 100);

							resultMap.put("message", "normal");

							resultList.add(resultMap);
						}

						cashCount = 0;
						bankCardCount = 0;
						memberCardCount = 0;
						handCount = 0;
						signCount = 0;
						discountCount = 0;
						giftCount = 0;
						totalCount = 0;
					}

					rowCount = rowCount + 1;
					// lastDate = orderDate;
					lastDept = dept;

					float allPrice = orderFood.calcPriceWithTaste();

					// pay
					int payManner = orderFood.payManner;
					switch (payManner) {
					case Order.MANNER_CASH:
						cashCount = cashCount + allPrice;
						allCashCount = allCashCount + allPrice;
						break;
						
					case Order.MANNER_CREDIT_CARD:
						bankCardCount = bankCardCount + allPrice;
						allBankCardCount = allBankCardCount + allPrice;
						break;
						
					case Order.MANNER_MEMBER:
						memberCardCount = memberCardCount + allPrice;
						allMemberCardCount = allMemberCardCount + allPrice;
						break;
						
					case Order.MANNER_SIGN:
						signCount = signCount + allPrice;
						allSignCount = allSignCount + allPrice;
						break;
						
					case Order.MANNER_HANG:
						handCount = handCount + allPrice;
						allHandCount = allHandCount + allPrice;
						break;
					}

					// discount : unit_price * order_count * (1 - discount)
					//float singlePriceXcount = (float) Math.round((orderFood.getPrice().floatValue() * orderFood.getCount()) * 100) / 100;
					//float thisDiscount = (float) Math.round(singlePriceXcount * (1 - orderFood.getDiscount()) * 100) / 100;
					float thisDiscount = orderFood.calcDiscountPrice();
					discountCount = discountCount + thisDiscount;
					allDiscountCount = allDiscountCount + thisDiscount;

					// gift
					if (orderFood.isGift()) {
						giftCount = giftCount + allPrice;
						allGiftCount = allGiftCount + allPrice;
					}

					// total price
					totalCount = totalCount + allPrice;
					allTotalCount = allTotalCount + allPrice;

				}
			}

			if (totalCount != 0) {
				HashMap<String, Object> resultMap = new HashMap<String, Object>();

				// resultMap.put("statDate", lastDate);
				resultMap.put("deptID", lastDept);
				resultMap.put("cash", (float)Math.round(cashCount * 100) / 100);
				resultMap.put("bankCard", (float)Math.round(bankCardCount * 100) / 100);
				resultMap.put("memberCard", (float)Math.round(memberCardCount * 100) / 100);
				resultMap.put("credit", (float)Math.round(handCount * 100) / 100);
				resultMap.put("sign", (float)Math.round(signCount * 100) / 100);
				resultMap.put("discount", (float)Math.round(discountCount * 100) / 100);
				resultMap.put("gift", (float)Math.round(giftCount * 100) / 100);
				resultMap.put("total", (float)Math.round(totalCount * 100) / 100);

				resultMap.put("message", "normal");

				resultList.add(resultMap);
			}

			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap<String, Object> resultMap = new HashMap<String, Object>();
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
				String totalPriceDiaplay = fnum.format(allTotalCount);
				HashMap<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put("deptID", "汇总");
				resultMap.put("cash", (float)Math.round(allCashCount * 100) / 100);
				resultMap.put("bankCard", (float)Math.round(allBankCardCount * 100) / 100);
				resultMap.put("memberCard", (float)Math.round(allMemberCardCount * 100) / 100);
				resultMap.put("credit", (float)Math.round(allHandCount * 100) / 100);
				resultMap.put("sign", (float)Math.round(allSignCount * 100) / 100);
				resultMap.put("discount", (float)Math.round(allDiscountCount * 100) / 100);
				resultMap.put("gift", (float)Math.round(allGiftCount * 100) / 100);
				resultMap.put("total", totalPriceDiaplay);
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
