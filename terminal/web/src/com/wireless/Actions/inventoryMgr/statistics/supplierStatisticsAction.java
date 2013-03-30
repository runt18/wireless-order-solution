package com.wireless.Actions.inventoryMgr.statistics;

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
import com.wireless.db.Params;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.protocol.Terminal;

public class supplierStatisticsAction extends Action {
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
		float allInCount = 0;
		float allReturnCount = 0;
		float allTotalCount = 0;

		String sqlSuppliers = "";
		String sqlDtl = "";
		String condition = "";

		String supplierID = "";
		float inAmount = 0;
		float returnAmount = 0;
		float totalAmount = 0;

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
			String supplierIDs = request.getParameter("supplierIDs");
			String StatisticsType = request.getParameter("StatisticsType");

			if (!dateBegin.equals("")) {
				condition = condition + " AND date >= '" + dateBegin
						+ " 00:00:00" + "' ";
			}
			if (!dateEnd.equals("")) {
				condition = condition + " AND date <= '" + dateEnd
						+ " 23:59:59" + "' ";
			}

			sqlSuppliers = " SELECT DISTINCT supplier_id FROM " + Params.dbName
					+ ".material_detail " + " WHERE restaurant_id = "
					+ term.restaurantID + " AND supplier_id IN ("
					+ supplierIDs + ") " + condition;

			dbCon.rs = dbCon.stmt.executeQuery(sqlSuppliers);

			while (dbCon.rs.next()) {
				supplierID = dbCon.rs.getString("supplier_id");

				sqlDtl = " SELECT SUM(amount) as amount, type from "
						+ Params.dbName + ".material_detail "
						+ " WHERE restaurant_id = " + term.restaurantID
						+ " AND supplier_id =  " + supplierID
						+ " AND type IN ('3', '5') " + condition
						+ " GROUP BY type ORDER BY type ";

				dbCon.rs = dbCon.stmt.executeQuery(sqlDtl);

				while (dbCon.rs.next()) {
					if (dbCon.rs.getInt("type") == 3) {
						returnAmount = dbCon.rs.getFloat("amount");
					} else {
						inAmount = dbCon.rs.getFloat("amount");
					}
				}
				
				totalAmount = (float) Math
						.round((returnAmount + inAmount) * 100) / 100;
				
				allTotalCount = (float) Math
						.round((allTotalCount + totalAmount) * 100) / 100;
				allInCount = (float) Math
						.round((allInCount + inAmount) * 100) / 100;
				allReturnCount = (float) Math
						.round((allReturnCount + returnAmount) * 100) / 100;
				
				
				HashMap resultMap = new HashMap();

				// resultMap.put("statDate", lastDate);
				resultMap.put("supplierID", supplierID);
				resultMap.put("inAmount", inAmount);
				resultMap.put("returnAmount", returnAmount);
				resultMap.put("payAmount", totalAmount);
				
				resultMap.put("message", "normal");

				resultList.add(resultMap);

			}		
	
			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
			HashMap resultMap = new HashMap();
			if (e.getErrCode() == ProtocolError.TERMINAL_NOT_ATTACHED) {
				resultMap.put("message", "没有获取到餐厅信息，请重新确认");

			} else if (e.getErrCode() == ProtocolError.TERMINAL_EXPIRED) {
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
				resultMap.put("supplierID", "SUM");
				resultMap.put("inAmount", allInCount);
				resultMap.put("returnAmount", allReturnCount);
				resultMap.put("payAmount", allTotalCount);
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
