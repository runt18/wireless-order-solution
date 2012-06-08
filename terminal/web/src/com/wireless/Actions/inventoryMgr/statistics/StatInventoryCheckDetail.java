package com.wireless.Actions.inventoryMgr.statistics;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
import com.wireless.dbObject.MaterialDetail;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

public class StatInventoryCheckDetail extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		List resultList = new ArrayList();
		HashMap rootMap = new HashMap();

		boolean isError = false;
		float allTotalCount = 0;
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
			String departments = request.getParameter("departments");
			String materials = request.getParameter("materials");
			String isDiff = request.getParameter("isDiff");

			String condition = "";

			if (!beginDate.equals("")) {
				condition = condition + " AND a.date >= '" + beginDate
						+ " 00:00:00" + "' ";
			}
			if (!endDate.equals("")) {
				condition = condition + " AND a.date <= '" + endDate
						+ " 23:59:59" + "' ";
			}

			if (!departments.equals("")) {
				condition = condition + " AND a.dept_id IN (" + departments
						+ ") ";
			}

			condition = condition + " AND a.material_id IN (" + materials
					+ ") ";

			if (isDiff.equals("true")) {
				condition = condition
						+ " AND (a.amount <> 0 OR a.price_prev <> a.price) ";
			}

			/*
			 */

			String sql = " SELECT a.material_id, a.date, a.dept_id, a.staff, "
					+ " a.price_prev, a.amount_prev, a.price, (a.amount_prev + a.amount) AS amount "
					+ " FROM " + Params.dbName + ".material_detail a "
					+ " WHERE a.restaurant_id = " + term.restaurant_id
					+ " AND a.type = " + MaterialDetail.TYPE_CHECK + " "
					+ condition;

			dbCon.rs = dbCon.stmt.executeQuery(sql);

			/**
			 * The json to each order looks like below
			 */
			while (dbCon.rs.next()) {

				HashMap resultMap = new HashMap();
				resultMap.put("materialID", dbCon.rs.getInt("material_id"));
				// resultMap.put("date", new SimpleDateFormat("yyyy-MM-dd")
				// .format(dbCon.rs.getDate("date")));
				resultMap.put("date", new SimpleDateFormat(
						"yyyy-MM-dd HH:mm:ss").format(dbCon.rs
						.getTimestamp("date")));
				resultMap.put("deptID", dbCon.rs.getInt("dept_id"));
				resultMap.put("operator", dbCon.rs.getString("staff"));
				resultMap.put("pricePrevious", dbCon.rs.getFloat("price_prev"));
				resultMap.put("amountPrevious", dbCon.rs.getFloat("amount_prev"));
				resultMap.put("price", dbCon.rs.getFloat("price"));
				resultMap.put("amount", dbCon.rs.getFloat("amount"));

				resultMap.put("message", "normal");

				resultList.add(resultMap);

				// allTotalCount = (float) Math.round((allTotalCount + dbCon.rs
				// .getFloat("total")) * 100) / 100;
				// allTotalAmount = allTotalAmount + dbCon.rs.getInt("amount");

			}

			if (resultList.size() == 0) {
				HashMap resultMap = new HashMap();
				resultMap.put("materialID", "NO_DATA");
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

				// DecimalFormat fnum = new DecimalFormat("##0.00");
				// String totalPriceDiaplay = fnum.format(allTotalCount);
				// HashMap resultMap = new HashMap();
				// resultMap.put("materialID", "SUM");
				// resultMap.put("price", "汇总");
				// resultMap.put("amount", allTotalAmount);
				// resultMap.put("total", totalPriceDiaplay);
				// resultMap.put("message", "normal");
				// resultList.add(resultMap);

				rootMap.put("root", resultList);
			}

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			// String outputJson = "{\"totalProperty\":" + resultList.size() +
			// ","
			// + obj.toString().substring(1);

			String outputJson = obj.toString();

			//System.out.println(outputJson);

			out.write(outputJson);
		}

		return null;
	}
}
