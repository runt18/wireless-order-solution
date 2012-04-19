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
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.dbReflect.OrderFoodReflector;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
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

		List resultList = new ArrayList();
		List outputList = new ArrayList();
		HashMap rootMap = new HashMap();

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
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			// get the query condition
			String dateBegin = request.getParameter("dateBegin");
			String dateEnd = request.getParameter("dateEnd");
			String deptIDs = request.getParameter("deptIDs");
			String StatisticsType = request.getParameter("StatisticsType");

			String condition = " AND C.dept_id IN (" + deptIDs + ") ";
			if (!dateBegin.equals("")) {
				condition = condition + " AND D.pay_datetime >= '" + dateBegin
						+ " 00:00:00" + "' ";
			}
			if (!dateEnd.equals("")) {
				condition = condition + " AND D.pay_datetime <= '" + dateEnd
						+ " 23:59:59" + "' ";
			}
			condition = condition
					+ " AND D.total_price IS NOT NULL AND C.restaurant_id =  "
					+ term.restaurant_id;

			OrderFoodReflector foodRef = new OrderFoodReflector();
			String orderClause = " ORDER BY C.dept_id";

			OrderFood orderFoods[] = null;
			if (StatisticsType.equals("Today")) {
				orderFoods = foodRef.getDetailToday(dbCon, condition,
						orderClause);
			} else if (StatisticsType.equals("History")) {
				orderFoods = foodRef.getDetailHistory(dbCon, condition,
						orderClause);
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
					int dept = orderFood.kitchen.deptID;

					if (/* !orderDate.equals(lastDate) || */dept != lastDept) {
						if (rowCount != 0) {
							HashMap resultMap = new HashMap();

							// resultMap.put("statDate", lastDate);
							resultMap.put("deptID", lastDept);
							resultMap.put("cash", cashCount);
							resultMap.put("bankCard", bankCardCount);
							resultMap.put("memberCard", memberCardCount);
							resultMap.put("credit", handCount);
							resultMap.put("sign", signCount);
							resultMap.put("discount", discountCount);
							resultMap.put("gift", giftCount);
							resultMap.put("total", totalCount);

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

					float allPrice = (float) Math.round((orderFood.getPrice2()
							.floatValue() * orderFood.getCount()) * 100) / 100;

					// pay
					int payManner = orderFood.payManner;
					switch (payManner) {
					case 1:
						cashCount = (float) Math
								.round((cashCount + allPrice) * 100) / 100;
						allCashCount = (float) Math
								.round((allCashCount + allPrice) * 100) / 100;
						break;
					case 2:
						bankCardCount = (float) Math
								.round((bankCardCount + allPrice) * 100) / 100;
						allBankCardCount = (float) Math
								.round((allBankCardCount + allPrice) * 100) / 100;
						break;
					case 3:
						memberCardCount = (float) Math
								.round((memberCardCount + allPrice) * 100) / 100;
						allMemberCardCount = (float) Math
								.round((allMemberCardCount + allPrice) * 100) / 100;
						break;
					case 4:
						signCount = (float) Math
								.round((signCount + allPrice) * 100) / 100;
						allSignCount = (float) Math
								.round((allSignCount + allPrice) * 100) / 100;
						break;
					case 5:
						handCount = (float) Math
								.round((handCount + allPrice) * 100) / 100;
						allHandCount = (float) Math
								.round((allHandCount + allPrice) * 100) / 100;
						break;
					}

					// discount : unit_price * order_count * (1 - discount)
					float singlePriceXcount = (float) Math
							.round((orderFood.getPrice().floatValue() * orderFood
									.getCount()) * 100) / 100;
					float thisDiscount = (float) Math.round(singlePriceXcount
							* (1 - orderFood.getDiscount()) * 100) / 100;
					discountCount = (float) Math
							.round((discountCount + thisDiscount) * 100) / 100;
					allDiscountCount = (float) Math
							.round((allDiscountCount + thisDiscount) * 100) / 100;

					// gift
					if (orderFood.isGift()) {
						giftCount = (float) Math
								.round((giftCount + allPrice) * 100) / 100;
						allGiftCount = (float) Math
								.round((allGiftCount + allPrice) * 100) / 100;
					}

					// total price
					totalCount = (float) Math
							.round((totalCount + allPrice) * 100) / 100;
					allTotalCount = (float) Math
							.round((allTotalCount + allPrice) * 100) / 100;

				}
			}

			if (totalCount != 0) {
				HashMap resultMap = new HashMap();

				// resultMap.put("statDate", lastDate);
				resultMap.put("deptID", lastDept);
				resultMap.put("cash", cashCount);
				resultMap.put("bankCard", bankCardCount);
				resultMap.put("memberCard", memberCardCount);
				resultMap.put("credit", handCount);
				resultMap.put("sign", signCount);
				resultMap.put("discount", discountCount);
				resultMap.put("gift", giftCount);
				resultMap.put("total", totalCount);

				resultMap.put("message", "normal");

				resultList.add(resultMap);
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
				HashMap resultMap = new HashMap();
				resultMap.put("deptID", "汇总");
				resultMap.put("cash", allCashCount);
				resultMap.put("bankCard", allBankCardCount);
				resultMap.put("memberCard", allMemberCardCount);
				resultMap.put("credit", allHandCount);
				resultMap.put("sign", allSignCount);
				resultMap.put("discount", allDiscountCount);
				resultMap.put("gift", allGiftCount);
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
