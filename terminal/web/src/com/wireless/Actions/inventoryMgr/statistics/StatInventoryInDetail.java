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
import com.wireless.pack.ErrorCode;
import com.wireless.protocol.Terminal;

public class StatInventoryInDetail extends Action {
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
			String supplier = request.getParameter("supplier");
			String departments = request.getParameter("departments");
			String materials = request.getParameter("materials");

			String condition = "";

			if (!beginDate.equals("")) {
				condition = condition + " AND date >= '" + beginDate
						+ " 00:00:00" + "' ";
			}
			if (!endDate.equals("")) {
				condition = condition + " AND date <= '" + endDate
						+ " 23:59:59" + "' ";
			}

			if (!supplier.equals("-1")) {
				condition = condition + " AND supplier_id = " + supplier + " ";
			}

			if (!departments.equals("")) {
				condition = condition + " AND dept_id IN (" + departments
						+ ") ";
			}

			condition = condition + " AND material_id IN (" + materials + ") ";

			/*
			 * rootData[i].materialID, materialN, rootData[i].date,
			 * rootData[i].supplierID, supplierN, rootData[i].operator,
			 * rootData[i].departmentID, deptN, rootData[i].price,
			 * rootData[i].amount, rootData[i].total
			 */

			String sql = " SELECT material_id, date, supplier_id, staff, dept_id, "
					+ " price, amount, price*amount as total, comment "
					+ " FROM "
					+ Params.dbName
					+ ".material_detail "
					+ " WHERE restaurant_id = "
					+ term.restaurantID
					+ " AND type = "
					+ MaterialDetail.TYPE_INCOME
					+ " "
					+ condition;

			dbCon.rs = dbCon.stmt.executeQuery(sql);

			/**
			 * The json to each order looks like below
			 */
			while (dbCon.rs.next()) {

				HashMap resultMap = new HashMap();
				resultMap.put("materialID", dbCon.rs.getInt("material_id"));
				resultMap.put("date", new SimpleDateFormat("yyyy-MM-dd")
						.format(dbCon.rs.getDate("date")));
				resultMap.put("supplierID", dbCon.rs.getInt("supplier_id"));
				resultMap.put("operator", dbCon.rs.getString("staff"));
				resultMap.put("departmentID", dbCon.rs.getInt("dept_id"));
				resultMap.put("price", dbCon.rs.getFloat("price"));
				resultMap.put("amount", dbCon.rs.getFloat("amount"));
				resultMap.put("total", dbCon.rs.getFloat("total"));
				resultMap.put("comment", dbCon.rs.getString("comment"));

				resultMap.put("message", "normal");

				resultList.add(resultMap);

				allTotalCount = (float) Math.round((allTotalCount + dbCon.rs
						.getFloat("total")) * 100) / 100;
				allTotalAmount = (float) Math.round((allTotalAmount + dbCon.rs
						.getFloat("amount")) * 100) / 100;

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
				String totalPriceDiaplay = fnum.format(allTotalCount);
				HashMap resultMap = new HashMap();
				resultMap.put("materialID", "SUM");
				resultMap.put("price", "汇总");
				resultMap.put("amount", allTotalAmount);
				resultMap.put("total", totalPriceDiaplay);
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

			//System.out.println(outputJson);

			out.write(outputJson);
		}

		return null;
	}
}
