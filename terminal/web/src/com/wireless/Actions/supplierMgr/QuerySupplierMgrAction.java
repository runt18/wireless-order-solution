package com.wireless.Actions.supplierMgr;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Food;
import com.wireless.protocol.Terminal;
import com.wireless.protocol.Util;

import com.wireless.db.QueryMenu;

public class QuerySupplierMgrAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		DBCon dbCon = new DBCon();

		PrintWriter out = null;

		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		int index = 0;
		int pageSize = 0;
		if (!(start == null)) {
			index = Integer.parseInt(start);
			pageSize = Integer.parseInt(limit);
		}

		List resultList = new ArrayList();
		List outputList = new ArrayList();
		// List chooseList = new ArrayList();
		HashMap rootMap = new HashMap();

		boolean isError = false;
		// 是否分頁
		String isPaging = request.getParameter("isPaging");

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
			 * pin : the pin the this terminal type : the type is one of the
			 * values below. 0 - 全部 1 - 名称 2 - 电话 3 - 地址 ope : the operator is
			 * one of the values below. 1 - 等于 2 - 大于等于 3 - 小于等于 value : the
			 * value to search, the content is depending on the type isSpecial :
			 * additional condition. isRecommend : additional condition. isFree
			 * : additional condition. isStop : additional condition.
			 */

			String pin = request.getParameter("pin");
			if (pin.startsWith("0x") || pin.startsWith("0X")) {
				pin = pin.substring(2);
			}

			// get the type to filter
			int type = Integer.parseInt(request.getParameter("type"));

			// get the value to filter
			String filterVal = request.getParameter("value");

			// combine the operator and filter value
			String filterCondition = null;

			if (type == 1) {
				// 按名称
				filterCondition = " AND name like '" + filterVal + "%'";
			} else if (type == 2) {
				// 按电话
				filterCondition = " AND tele like '" + filterVal + "%'";
			} else if (type == 3) {
				// 按地址
				filterCondition = " AND addr like '" + filterVal + "%'";
			} else {
				// 全部
				filterCondition = "";
			}

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Integer.parseInt(pin, 16),
					Terminal.MODEL_STAFF);

			String sql = " SELECT supplier_id, supplier_alias, name, tele, contact, addr "
					+ " FROM " + Params.dbName + ".supplier "
					+ " WHERE restaurant_id = " + term.restaurant_id
					+ filterCondition;

			dbCon.rs = dbCon.stmt.executeQuery(sql);

			while (dbCon.rs.next()) {
				HashMap resultMap = new HashMap();
				/**
				 * The json to each order looks like below 供應商ID，供應商別名，名稱，電話，聯係人，地址
				 */
				resultMap.put("supplierID", dbCon.rs.getInt("supplier_id"));
				resultMap.put("supplierAlias", dbCon.rs.getInt("supplier_alias"));
				resultMap.put("supplierName", dbCon.rs.getString("name"));
				resultMap.put("supplierPhone", dbCon.rs.getString("tele"));
				resultMap.put("supplierContact", dbCon.rs.getString("contact"));
				resultMap.put("supplierAddress", dbCon.rs.getString("addr"));

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
				resultMap.put("message", "没有获取到供应商信息，请重新确认");
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
				if (isPaging.equals("true")) {
					// 分页
					for (int i = index; i < pageSize + index; i++) {
						try {
							outputList.add(resultList.get(i));
						} catch (Exception e) {
							// 最后一页可能不足一页，会报错，忽略
						}
					}
				} else {
					for (int i = 0; i < resultList.size(); i++) {
						outputList.add(resultList.get(i));
					}
				}
				rootMap.put("root", outputList);
			}

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			String outputJson = "{\"totalProperty\":" + resultList.size() + ","
					+ obj.toString().substring(1);

			//System.out.println(outputJson);

			out.write(outputJson);

		}
		return null;
	}
}
