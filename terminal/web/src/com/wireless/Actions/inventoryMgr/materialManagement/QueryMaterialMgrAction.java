package com.wireless.Actions.inventoryMgr.materialManagement;

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
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Terminal;

public class QueryMaterialMgrAction extends Action {

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
		List chooseList = new ArrayList();
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
			 * values below. 0 - 全部全部 1 - 编号 2 - 名称 3 - 拼音 4 - 价格 5 - 厨房 ope :
			 * the operator is one of the values below. 1 - 等于 2 - 大于等于 3 - 小于等于
			 * value : the value to search, the content is depending on the type
			 * isSpecial : additional condition. isRecommend : additional
			 * condition. isFree : additional condition. isStop : additional
			 * condition.
			 */

			String pin = request.getParameter("pin");
			
			// get the type to filter
			int type = Integer.parseInt(request.getParameter("type"));

			// get the operator to filter
			String ope = request.getParameter("ope");
			if (ope != null) {
				int opeType = Integer.parseInt(ope);

				if (opeType == 1) {
					ope = "=";
				} else if (opeType == 2) {
					ope = ">=";
				} else if (opeType == 3) {
					ope = "<=";
				} else {
					// 不可能到这里
					ope = "=";
				}
			} else {
				// 不可能到这里
				ope = "";
			}

			// get the value to filter
			String filterVal = request.getParameter("value");

			// get the checkbox values
			String isWarning = request.getParameter("isWarning");
			String isDanger = request.getParameter("isDanger");

			// combine the operator and filter value
			String filterCondition = null;

			if (type == 1) {
				// 按编号
				filterCondition = " AND A.material_alias " + ope + filterVal;
			} else if (type == 2) {
				// 按名称
				filterCondition = " AND A.name like '%" + filterVal + "%'";
			} else if (type == 3) {
				// 按種類
				filterCondition = " AND A.cate_id = " + filterVal;
			} else if (type == 4) {
				// 按库存量
				filterCondition = " AND stock " + ope + filterVal;
			} else if (type == 5) {
				// 按价格
				filterCondition = " AND B.price " + ope + filterVal;
			} else if (type == 6) {
				// 按预警阀值
				filterCondition = " AND A.warning_threshold " + ope + filterVal;
			} else if (type == 7) {
				// 按危险阀值
				filterCondition = " AND A.danger_threshold " + ope + filterVal;
			} else {
				// 全部
				filterCondition = "";
			}

			String havingCondition = "";
			if (isDanger.equals("true")) {
				// 低於危險閥值
				havingCondition = " HAVING stock < A.danger_threshold ";
			}
			if (isWarning.equals("true")) {
				// 低於預警閥值
				if (havingCondition.equals("")) {
					havingCondition = " HAVING stock < A.warning_threshold ";
				} else {
					havingCondition = havingCondition
							+ " AND stock < A.warning_threshold ";
				}
			}

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);
			// 编号 名称 库存量 价格（￥） 预警阀值 危险阀值
			String sql = " SELECT A.material_id, A.material_alias, A.name, SUM(B.stock) AS stock, B.price, A.warning_threshold, A.danger_threshold, A.cate_id, C.name cateName "
					+ " FROM "
					+ Params.dbName + ".material A " 
					+ " LEFT OUTER JOIN " + Params.dbName + ".material_cate C ON ( A.restaurant_id = C.restaurant_id AND A.cate_id =  C.cate_id) "
					+ " LEFT OUTER JOIN " + Params.dbName + ".material_dept B ON ( A.restaurant_id = B.restaurant_id AND A.material_id = B.material_id ) "
					+ " WHERE A.restaurant_id = "
					+ term.restaurantID
					+ " "
					+ filterCondition
					+ " GROUP BY A.material_id, A.material_alias, A.name, B.price, A.warning_threshold, A.danger_threshold  "
					+ havingCondition;

			dbCon.rs = dbCon.stmt.executeQuery(sql);

			while (dbCon.rs.next()) {
				HashMap resultMap = new HashMap();
				/**
				 * The json to each order looks like below 编号 名称 库存量 价格（￥） 预警阀值
				 * 危险阀值
				 */
				resultMap.put("materialID", dbCon.rs.getInt("material_id"));
				resultMap.put("materialAlias", dbCon.rs.getInt("material_alias"));
				resultMap.put("materialName", dbCon.rs.getString("name"));
				resultMap.put("storage", dbCon.rs.getFloat("stock"));
				resultMap.put("price", dbCon.rs.getFloat("price"));
				resultMap.put("warningNbr", dbCon.rs.getFloat("warning_threshold"));
				resultMap.put("dangerNbr", dbCon.rs.getFloat("danger_threshold"));
				resultMap.put("cateID", dbCon.rs.getInt("cate_id"));
				resultMap.put("cateName", dbCon.rs.getString("cateName"));

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
				resultMap.put("message", "没有获取到食材信息，请重新确认");
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

			// System.out.println(outputJson);

			out.write(outputJson);

		}
		return null;
	}
}
