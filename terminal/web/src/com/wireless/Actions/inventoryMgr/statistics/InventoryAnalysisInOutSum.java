package com.wireless.Actions.inventoryMgr.statistics;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.protocol.Terminal;

public class InventoryAnalysisInOutSum extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		List resultList = new ArrayList();
		HashMap rootMap = new HashMap();

		boolean isError = false;

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
			String materialCates = request.getParameter("materialCates");
			String departments = request.getParameter("departments");

			String condition = "";

			if (!beginDate.equals("")) {
				condition = condition + " AND a.date >= '" + beginDate
						+ " 00:00:00" + "' ";
			}
			if (!endDate.equals("")) {
				condition = condition + " AND a.date <= '" + endDate
						+ " 23:59:59" + "' ";
			}

			if (!materialCates.equals("")) {
				condition = condition + " AND b.cate_id IN (" + materialCates
						+ ") ";
			}

			if (!departments.equals("")) {
				condition = condition + " AND a.dept_id IN (" + departments
						+ ") ";
			}

			String sql = " ";
			/*
			 * 食材种类 食材 进货数量 进货价格 进货金额 退货数量 退货价格 退货金额 出仓数量 出仓价格 出仓金额 报损数量 报损价格
			 * 报损金额
			 */
			// 入庫！
			sql = " SELECT b.cate_id, a.material_id, b.name as materialCate_name, c.name as material_name, "
					+ " sum(a.amount) as count, sum(a.price) as price "
					+ " FROM "
					+ Params.dbName
					+ ".material_detail a, "
					+ Params.dbName
					+ ".material_cate b, "
					+ Params.dbName
					+ ".material c "
					+ " WHERE a.restaurant_id = "
					+ term.restaurantID
					+ " AND a.restaurant_id = c.restaurant_id AND a.material_id = c.material_id "
					+ " AND b.restaurant_id = c.restaurant_id AND b.cate_id = c.cate_id "
					+ " "
					+ condition
					+ " AND a.type = 5 "
					+ " GROUP BY b.cate_id, materialCate_name, a.material_id, material_name "
					+ " ORDER BY b.cate_id, materialCate_name, a.material_id, material_name ";

			dbCon.rs = dbCon.stmt.executeQuery(sql);

			/**
			 * The json to each order looks like below
			 */
			int groupID = 1;
			int materialID = 0;
			int materialCateID = 0;
			float count = 0;
			float price = 0;
			float totalPrice = 0;
			String sqlReturn = " ";
			String sqlOut = " ";
			String sqlCost = " ";
			while (dbCon.rs.next()) {

				materialID = dbCon.rs.getInt("material_id");
				materialCateID = dbCon.rs.getInt("cate_id");

				HashMap resultMap = new HashMap();
				resultMap.put("materialCateID", dbCon.rs.getInt("cate_id"));
				resultMap.put("materialCateName",
						dbCon.rs.getString("materialCate_name"));
				resultMap.put("groupID", groupID);
				resultMap.put("groupDescr", "");
				resultMap.put("materialID", dbCon.rs.getInt("material_id"));
				resultMap.put("materialName",
						dbCon.rs.getString("material_name"));
				resultMap.put("inCount", dbCon.rs.getFloat("count"));
				resultMap.put("inPrice", dbCon.rs.getFloat("price"));
				totalPrice = (float) Math.round(dbCon.rs.getFloat("count")
						* dbCon.rs.getFloat("price") * 100) / 100;
				resultMap.put("inTotalPrice", totalPrice);

				// 退貨！
				sqlReturn = " SELECT sum(a.amount) as count, sum(a.price) as price "
						+ " FROM "
						+ Params.dbName
						+ ".material_detail a, "
						+ Params.dbName
						+ ".material_cate b, "
						+ Params.dbName
						+ ".material c "
						+ " WHERE a.restaurant_id = "
						+ term.restaurantID
						+ " AND a.restaurant_id = c.restaurant_id AND a.material_id = c.material_id "
						+ " AND b.restaurant_id = c.restaurant_id AND b.cate_id = c.cate_id "
						+ " "
						+ condition
						+ " AND a.type = 3 AND a.material_id = "
						+ materialID
						+ " AND b.cate_id = " + materialCateID;
				dbCon.rs2 = dbCon.conn.createStatement()
						.executeQuery(sqlReturn);
				while (dbCon.rs2.next()) {
					count = dbCon.rs2.getFloat("count");
					price = dbCon.rs2.getFloat("price");
				}
				count = count * (-1);
				resultMap.put("returnCount", count);
				resultMap.put("returnPrice", price);
				totalPrice = (float) Math.round(count * price * 100) / 100;
				resultMap.put("returnTotalPrice", totalPrice);

				// 出仓！
				sqlOut = " SELECT sum(a.amount) as count, sum(a.price) as price "
						+ " FROM "
						+ Params.dbName
						+ ".material_detail a, "
						+ Params.dbName
						+ ".material_cate b, "
						+ Params.dbName
						+ ".material c "
						+ " WHERE a.restaurant_id = "
						+ term.restaurantID
						+ " AND a.restaurant_id = c.restaurant_id AND a.material_id = c.material_id "
						+ " AND b.restaurant_id = c.restaurant_id AND b.cate_id = c.cate_id "
						+ " "
						+ condition
						+ " AND a.type = 4 AND a.material_id = "
						+ materialID
						+ " AND b.cate_id = " + materialCateID;
				dbCon.rs3 = dbCon.conn.createStatement().executeQuery(sqlOut);
				while (dbCon.rs3.next()) {
					count = dbCon.rs3.getFloat("count");
					price = dbCon.rs3.getFloat("price");
				}
				count = count * (-1);
				resultMap.put("outCount", count);
				resultMap.put("outPrice", price);
				totalPrice = (float) Math.round(count * price * 100) / 100;
				resultMap.put("outTotalPrice", totalPrice);

				// 报损！
				sqlCost = " SELECT sum(a.amount) as count, sum(a.price) as price "
						+ " FROM "
						+ Params.dbName
						+ ".material_detail a, "
						+ Params.dbName
						+ ".material_cate b, "
						+ Params.dbName
						+ ".material c "
						+ " WHERE a.restaurant_id = "
						+ term.restaurantID
						+ " AND a.restaurant_id = c.restaurant_id AND a.material_id = c.material_id "
						+ " AND b.restaurant_id = c.restaurant_id AND b.cate_id = c.cate_id "
						+ " "
						+ condition
						+ " AND a.type = 1 AND a.material_id = "
						+ materialID
						+ " AND b.cate_id = " + materialCateID;
				dbCon.rs4 = dbCon.conn.createStatement().executeQuery(sqlCost);
				while (dbCon.rs4.next()) {
					count = dbCon.rs4.getFloat("count");
					price = dbCon.rs4.getFloat("price");
				}
				count = count * (-1);
				resultMap.put("costCount", count);
				resultMap.put("costPrice", price);
				totalPrice = (float) Math.round(count * price * 100) / 100;
				resultMap.put("costTotalPrice", totalPrice);

				resultMap.put("message", "normal");

				resultList.add(resultMap);

				groupID = groupID + 1;

			}

			if (resultList.size() == 0) {
				HashMap resultMap = new HashMap();
				resultMap.put("materialCateID", "NO_DATA");
				resultMap.put("message", "normal");
				resultList.add(resultMap);
			}

			// dbCon.rs.close();

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
