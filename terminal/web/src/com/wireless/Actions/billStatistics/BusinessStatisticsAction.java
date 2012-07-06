package com.wireless.Actions.billStatistics;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
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

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryShift;
import com.wireless.db.VerifyPin;
import com.wireless.db.billStatistics.QueryDutyRange;
import com.wireless.pojo.billStatistics.DutyRange;
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

		List<HashMap<String, Object>> resultList = new ArrayList<HashMap<String, Object>>();
		List<HashMap<String, Object>> outputList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> rootMap = new HashMap<String, Object>();

		float orderCountAll = 0;
		float cashAll = 0;
		float bankCardAll = 0;
		float memberCardAll = 0;
		float creditAll = 0;
		float signAll = 0;
		float discountAll = 0;
		float giftAll = 0;
		float returnAll = 0;
		float paidAll = 0;
		float serviceAll = 0;
		float totalPriceAll = 0;
		float actualPriceAll = 0;

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
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin), Terminal.MODEL_STAFF);

			Date dateBegin = null;
			/**
			 * In the case not input the begin date, set the minimum on duty of
			 * daily settle history as dateBegin
			 */
			if (request.getParameter("dateBegin").equals("")) {
				sql = " SELECT MIN(on_duty) AS date_begin FROM "	+ 
					  Params.dbName + ".daily_settle_history " + 
					  " WHERE "	+ 
					  " restaurant_id = " + term.restaurant_id;
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if (dbCon.rs.next()) {
					dateBegin = new Date(dbCon.rs.getTimestamp("date_begin").getTime());
				}
				dbCon.rs.close();
			} else {
				dateBegin = new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("dateBegin"));
			}

			/**
			 * In the case not input the begin date, set maximum off duty of
			 * daily settle history as dateEnd
			 */
			Date dateEnd = null;
			if (request.getParameter("dateEnd").equals("")) {
				sql = " SELECT MAX(off_duty) AS date_end FROM " + 
					  Params.dbName + ".daily_settle_history" + 
					  " WHERE "	+ 
					  "restaurant_id = " + term.restaurant_id;
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if (dbCon.rs.next()) {
					dateEnd = new Date(dbCon.rs.getTimestamp("date_end").getTime());
				}
				dbCon.rs.close();
			} else {
				dateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(request.getParameter("dateEnd"));
			}

			Calendar c = Calendar.getInstance();
			c.setTime(dateBegin);

			while (dateBegin.compareTo(dateEnd) <= 0) {
				Date datePrev = c.getTime();
				c.add(Calendar.DATE, 1);

				DutyRange dutyRange = QueryDutyRange.exec(dbCon, 
														  term, 
														  new SimpleDateFormat("yyyy-MM-dd").format(dateBegin), 
														  new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
				
				QueryShift.Result result;
				
				if (dutyRange != null) {
					
					result = QueryShift.exec(dbCon, 
											term,
											dutyRange.getOnDuty(), 
											dutyRange.getOffDuty(), 
											QueryShift.QUERY_HISTORY);
					
				}else{
					result = new QueryShift.Result();
				}
				
				HashMap<String, Object> resultMap = new HashMap<String, Object>();

				// resultMap.put("statDate", lastDate);
				resultMap.put("date", new SimpleDateFormat("yyyy-MM-dd").format(datePrev));
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
				float totalIncome = result.cashIncome + 
									result.creditCardIncome + result.memberCardIncome + 
									result.signIncome + result.hangIncome;
				resultMap.put("totalPrice", totalIncome);
				float totalActual = result.cashIncome2
							+ result.creditCardIncome2
							+ result.memberCardIncome2 + result.signIncome2
							+ result.hangIncome2;
				resultMap.put("actualPrice", totalActual);

				resultMap.put("message", "normal");

				resultList.add(resultMap);

				orderCountAll = (float)Math.round((orderCountAll + result.orderAmount) * 100) / 100;
				cashAll = (float) Math.round((cashAll + result.cashIncome) * 100) / 100;
				bankCardAll = (float) Math.round((bankCardAll + result.creditCardIncome) * 100) / 100;
				memberCardAll = (float) Math.round((memberCardAll + result.memberCardIncome) * 100) / 100;
				creditAll = (float) Math.round((creditAll + result.hangIncome) * 100) / 100;
				signAll = (float) Math.round((signAll + result.signIncome) * 100) / 100;
				discountAll = (float) Math.round((discountAll + result.discountIncome) * 100) / 100;
				giftAll = (float) Math.round((giftAll + result.giftIncome) * 100) / 100;
				returnAll = (float) Math.round((returnAll + result.cancelIncome) * 100) / 100;
				paidAll = (float) Math.round((paidAll + result.paidIncome) * 100) / 100;
				serviceAll = (float) Math.round((serviceAll + result.serviceIncome) * 100) / 100;
				totalPriceAll = (float) Math.round((totalPriceAll + totalIncome) * 100) / 100;
				actualPriceAll = (float) Math.round((actualPriceAll + totalActual) * 100) / 100;		

				dateBegin = c.getTime();
			}

			dbCon.rs.close();

		} catch (ParseException e) {

			HashMap<String, Object> resultMap = new HashMap<String ,Object>();
			resultMap.put("message", "日期格式不正确");
			resultList.add(resultMap);
			isError = true;
			e.printStackTrace();
			
		} catch (SQLException e) {

			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put("message", "数据查询语句不正确");
			resultList.add(resultMap);
			isError = true;
			e.printStackTrace();
			
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

				HashMap<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put("date", "汇总");
				resultMap.put("orderCount", orderCountAll);
				resultMap.put("cash", cashAll);
				resultMap.put("bankCard", bankCardAll);
				resultMap.put("memberCard", memberCardAll);
				resultMap.put("credit", creditAll);
				resultMap.put("sign", signAll);
				resultMap.put("discount", discountAll);
				resultMap.put("gift", giftAll);
				resultMap.put("return", returnAll);
				resultMap.put("paid", paidAll);
				resultMap.put("service", serviceAll);
				resultMap.put("totalPrice", totalPriceAll);
				resultMap.put("actualPrice", actualPriceAll);
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
