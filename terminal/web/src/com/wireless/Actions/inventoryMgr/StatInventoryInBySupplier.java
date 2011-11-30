package com.wireless.Actions.inventoryMgr;

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
import com.wireless.dbObject.MaterialDetail;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

public class StatInventoryInBySupplier extends Action {
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
			if (pin.startsWith("0x") || pin.startsWith("0X")) {
				pin = pin.substring(2);
			}
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Integer.parseInt(pin, 16),
					Terminal.MODEL_STAFF);

			// get the query condition
			String beginDate = request.getParameter("beginDate");
			String endDate = request.getParameter("endDate");
			String supplier = request.getParameter("supplier");
			String departments = request.getParameter("departments");
			String materials = request.getParameter("materials");

			String condition = "";

			if (!beginDate.equals("")) {
				condition = condition + " AND a.date >= '" + beginDate
						+ " 00:00:00" + "' ";
			}
			if (!endDate.equals("")) {
				condition = condition + " AND a.date <= '" + endDate
						+ " 23:59:59" + "' ";
			}

			if (!supplier.equals("-1")) {
				condition = condition + " AND a.supplier_id = " + supplier
						+ " ";
			}

			if (!departments.equals("")) {
				condition = condition + " AND a.dept_id IN (" + departments
						+ ") ";
			}

			condition = condition + " AND a.material_id IN (" + materials
					+ ") ";

			/*
			 */

			String sql = " SELECT a.material_id, a.supplier_id, b.name as material_name, c.name as supplier_name, "
					+ " sum(a.amount) as amount, sum(a.amount*a.price) as total_price "
					+ " FROM "
					+ Params.dbName
					+ ".material_detail a, "
					+ Params.dbName
					+ ".material b, "
					+ Params.dbName
					+ ".supplier c "
					+ " WHERE a.restaurant_id = "
					+ term.restaurant_id
					+ " AND a.restaurant_id = b.restaurant_id AND a.material_id = b.material_id "
					+ " AND a.restaurant_id = c.restaurant_id AND a.supplier_id = c.supplier_id "
					+ " AND a.type = "
					+ MaterialDetail.TYPE_INCOME
					+ " "
					+ condition
					+ " GROUP BY a.supplier_id, supplier_name, a.material_id, material_name "
					+ " ORDER BY a.supplier_id, a.material_id ";

			System.out.println(sql);
			dbCon.rs = dbCon.stmt.executeQuery(sql);

			/**
			 * The json to each order looks like below
			 */
			int groupID = 1;
			while (dbCon.rs.next()) {

				HashMap resultMap = new HashMap();
				resultMap.put("supplierID", dbCon.rs.getInt("supplier_id"));
				resultMap.put("supplierName",
						dbCon.rs.getString("supplier_name"));
				resultMap.put("groupID", groupID);
				resultMap.put("groupDescr", "");
				resultMap.put("materialID", dbCon.rs.getInt("material_id"));
				resultMap.put("materialName",
						dbCon.rs.getString("material_name"));
				resultMap.put("amount", dbCon.rs.getFloat("amount"));
				resultMap.put("sumPrice", dbCon.rs.getFloat("total_price"));

				resultMap.put("message", "normal");

				resultList.add(resultMap);

				groupID = groupID + 1;

			}
			
			if(resultList.size() == 0){
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
				rootMap.put("root", resultList);
			}

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			// String outputJson = "{\"totalProperty\":" + resultList.size() +
			// ","
			// + obj.toString().substring(1);

			String outputJson = obj.toString();

			System.out.println(outputJson);

			out.write(outputJson);
		}

		return null;
	}
}
