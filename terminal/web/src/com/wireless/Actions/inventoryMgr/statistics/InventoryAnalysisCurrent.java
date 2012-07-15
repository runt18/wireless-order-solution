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

@SuppressWarnings({ "rawtypes", "unchecked" })
public class InventoryAnalysisCurrent extends Action {

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
//			String beginDate = request.getParameter("beginDate");
//			String endDate = request.getParameter("endDate");
			String materialCates = request.getParameter("materialCates");
			String departments = request.getParameter("departments");

			String condition = "";

//			if (!beginDate.equals("")) {
//				condition = condition + " AND a.date >= '" + beginDate
//						+ " 00:00:00" + "' ";
//			}
//			if (!endDate.equals("")) {
//				condition = condition + " AND a.date <= '" + endDate
//						+ " 23:59:59" + "' ";
//			}

			if (!materialCates.equals("")) {
				condition = condition + " AND b.cate_id IN (" + materialCates
						+ ") ";
			}
			
			if (!departments.equals("")) {
				condition = condition + " AND a.dept_id IN (" + departments
						+ ") ";
			}

			/*
			 * materialID : 100, materialName : '雞肉', groupID : 112, groupDescr
			 * : '', // price : 6, amount : 150, deptName : 'department',
			 * sumPrice : 1000
			 */
			
			String sql = " SELECT b.cate_id, a.material_id, b.name as materialCate_name, c.name as material_name, "
					+ " sum(a.stock) as stock, a.price as price "
					+ " FROM "
					+ Params.dbName
					+ ".material_dept a, "
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
					+ " GROUP BY b.cate_id, materialCate_name, a.material_id, material_name "
					+ " ORDER BY b.cate_id, materialCate_name, a.material_id, material_name ";

			
			dbCon.rs = dbCon.stmt.executeQuery(sql);

			/**
			 * The json to each order looks like below
			 */
			int groupID = 1;
			float sumPrice = 0;
			while (dbCon.rs.next()) {

				HashMap resultMap = new HashMap();
				resultMap.put("materialCateID", dbCon.rs.getInt("cate_id"));
				resultMap.put("materialCateName", dbCon.rs.getString("materialCate_name"));
				resultMap.put("groupID", groupID);
				resultMap.put("groupDescr", "");
				resultMap.put("materialID", dbCon.rs.getInt("material_id"));
				resultMap.put("materialName", dbCon.rs.getString("material_name"));
				resultMap.put("singlePrice", dbCon.rs.getFloat("price"));
				resultMap.put("amount", dbCon.rs.getFloat("stock"));
				sumPrice = (float) Math.round(dbCon.rs.getFloat("price") * dbCon.rs.getFloat("stock") * 100) / 100;
				resultMap.put("sumPrice", sumPrice);

				resultMap.put("message", "normal");

				resultList.add(resultMap);

				groupID = groupID + 1;

			}
			
			if(resultList.size() == 0){
				HashMap resultMap = new HashMap();
				resultMap.put("materialCateID", "NO_DATA");
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

//			System.out.println(outputJson);

			out.write(outputJson);
		}

		return null;
	}
}
