package com.wireless.Actions.billStatistics;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryShift;
import com.wireless.db.VerifyPin;
import com.wireless.protocol.Terminal;

public class BusinessStatisticsAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		boolean isError = false;

		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int index = Integer.parseInt(start);
		int pageSize = Integer.parseInt(limit);

		List resultList = new ArrayList();
		List outputList = new ArrayList();
		HashMap rootMap = new HashMap();

		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			/**
			 * The parameters looks like below. pin = 1 & dateBegin='2012-5-1' &
			 * dateEnd='2012-5-5' "pin" : pin, "dateBegin" :
			 * businessStaticBeginDate, "dateEnd" : businessStaticEndDate,
			 * "StatisticsType" : "History"
			 */
			dbCon.connect();

			String sql;

			String pin = request.getParameter("pin");
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			Date dateBegin = null;
			/**
			 * In the case not input the begin date, set the minimum on duty of
			 * daily settle history as dateBegin
			 */
			if (request.getParameter("dateBegin").equals("")) {
				sql = " SELECT MIN(order_date) AS date_begin FROM " + Params.dbName
						+ ".order_history " + " WHERE "
						+ " restaurant_id = " + term.restaurant_id;
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if (dbCon.rs.next()) {
					dateBegin = new Date(dbCon.rs.getTimestamp("date_begin")
							.getTime());
				}
				dbCon.rs.close();
			} else {
				dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(request
						.getParameter("dateBegin"));
			}

			/**
			 * In the case not input the begin date, set maximum off duty of
			 * daily settle history as dateEnd
			 */
			Date dateEnd = null;
			if (request.getParameter("dateEnd").equals("")) {
				sql = " SELECT MAX(order_date) AS date_end FROM " + Params.dbName
						+ ".order_history" + " WHERE "
						+ "restaurant_id = " + term.restaurant_id;
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if (dbCon.rs.next()) {
					dateEnd = new Date(dbCon.rs.getTimestamp("date_end")
							.getTime());
				}
				dbCon.rs.close();
			} else {
				dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(request
						.getParameter("dateEnd"));
			}

			Calendar c = Calendar.getInstance();
			c.setTime(dateBegin);

			while (dateBegin.compareTo(dateEnd) < 0) {
				c.add(Calendar.DATE, 1);
				Date dateItemEnd = c.getTime();
				sql = " SELECT MIN(on_duty) AS on_duty, MAX(off_duty) AS off_duty FROM "
						+ Params.dbName
						+ ".daily_settle_history "
						+ " WHERE "
						+ " restaurant_id = "
						+ term.restaurant_id
						+ " AND "
						+ " off_duty BETWEEN "
						+ new SimpleDateFormat("yyyy-MM-dd").format(dateBegin)
						+ " AND "
						+ new SimpleDateFormat("yyyy-MM-dd")
								.format(dateItemEnd);
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if (dbCon.rs.next()) {
					String onDuty;
					java.sql.Timestamp onDutyTS = dbCon.rs
							.getTimestamp("on_duty");
					if (onDutyTS == null) {
						onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(dateBegin);
					} else {
						onDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(new Date(dbCon.rs.getTimestamp(
										"on_duty").getTime()));
					}

					String offDuty;
					java.sql.Timestamp offDutyTS = dbCon.rs
							.getTimestamp("on_duty");
					if (offDutyTS == null) {
						offDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(dateItemEnd);
					} else {
						offDuty = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
								.format(new Date(dbCon.rs.getTimestamp(
										"off_duty").getTime()));
					}
					QueryShift.Result result = QueryShift.exec(dbCon, term,
							onDuty, offDuty, QueryShift.QUERY_HISTORY);

					HashMap resultMap = new HashMap();

					// resultMap.put("statDate", lastDate);
					resultMap.put("date", new SimpleDateFormat("yyyy-MM-dd")
							.format(dateItemEnd));
					resultMap.put("orderCount", result.orderAmount);
					resultMap.put("cash", result.cashIncome);
					resultMap.put("bankCard", result.creditCardIncome);
					resultMap.put("memberCard", result.memberCardIncome);
					resultMap.put("credit", result.hangIncome);
					resultMap.put("sign", result.signIncome);
					resultMap.put("discount", result.discountIncome);
					resultMap.put("gift", result.giftIncome);
					resultMap.put("return", result.cancelIncome);
					resultMap.put("paid", result.paidIncome);
					resultMap.put("service", result.serviceIncome);
					float totalIncome = result.cashIncome
							+ result.creditCardIncome + result.memberCardIncome
							+ result.signIncome + result.hangIncome;
					resultMap.put("totalPrice", totalIncome);
					float totalActual = result.cashIncome2
							+ result.creditCardIncome2
							+ result.memberCardIncome2 + result.signIncome2
							+ result.hangIncome2;
					resultMap.put("actualPrice", totalActual);

					resultMap.put("message", "normal");

					resultList.add(resultMap);

				}

				dateBegin = c.getTime();
			}

			dbCon.rs.close();

		} catch (ParseException e) {

			HashMap resultMap = new HashMap();
			resultMap.put("message", "日期格式不正确");
			resultList.add(resultMap);
			isError = true;

		} catch (SQLException e) {

			HashMap resultMap = new HashMap();
			resultMap.put("message", "数据查询语句不正确");
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
