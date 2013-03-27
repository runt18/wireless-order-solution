package com.wireless.Actions.regionMgr;

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
import com.wireless.db.QueryRegion;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.pack.ErrorCode;
import com.wireless.protocol.Region;
import com.wireless.protocol.Terminal;

public class QueryRegionMgrAction extends Action {

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
		// 是否combo
		String isCombo = request.getParameter("isCombo");
		// 是否Tree
		String isTree = request.getParameter("isTree");

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

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			Region[] regions = QueryRegion.exec(Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			// String sql = " SELECT region_id, name " + " FROM " +
			// Params.dbName
			// + ".region " + " WHERE restaurant_id = "
			// + term.restaurant_id + " ORDER BY region_id ";
			//
			// dbCon.rs = dbCon.stmt.executeQuery(sql);

			// while (dbCon.rs.next()) {
			for (int i = 0; i < regions.length; i++) {
				HashMap resultMap = new HashMap();
				/**
				 * The json to each order looks like below 編號，名稱
				 */
				resultMap.put("regionID", regions[i].getRegionId());
				resultMap.put("regionName", regions[i].getName());

				resultMap.put("message", "normal");

				resultList.add(resultMap);

			}
			dbCon.rs.close();

		} catch (BusinessException e) {
			e.printStackTrace();
//			HashMap resultMap = new HashMap();
//			if (e.errCode == ErrorCode.TERMINAL_NOT_ATTACHED) {
//				resultMap.put("message", "没有获取到餐厅信息，请重新确认");
//			} else if (e.errCode == ErrorCode.TERMINAL_EXPIRED) {
//				resultMap.put("message", "终端已过期，请重新确认");
//			} else {
//				resultMap.put("message", "没有获取到区域信息，请重新确认");
//			}
//			resultList.add(resultMap);
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

			String outString = "";

			if (isError) {
				// 返回報錯
				rootMap.put("root", resultList);
			} else if (isCombo.equals("true")) {
				// 返回combo數據
				outString = "{\"root\":[";
				if (resultList.size() == 0) {
				} else {
					for (int i = 0; i < resultList.size(); i++) {

						outString = outString
								+ "{regionID:"
								+ ((HashMap) (resultList.get(i))).get(
										"regionID").toString() + ",";
						outString = outString
								+ "regionName:'"
								+ ((HashMap) (resultList.get(i))).get(
										"regionName").toString() + "'},";

					}
					outString = outString.substring(0, outString.length() - 1);

				}
				outString = outString + "]}";
			} else if (isTree.equals("true")) {
				// 返回樹數據
				outString = "[";
				if (resultList.size() == 0) {
				} else {
					for (int i = 0; i < resultList.size(); i++) {

						outString = outString
								+ "{id:'region"
								+ ((HashMap) (resultList.get(i))).get(
										"regionID").toString() + "',";
						outString = outString
								+ "text:'"
								+ ((HashMap) (resultList.get(i))).get(
										"regionName").toString()
								+ "',leaf:true},";

					}
					outString = outString.substring(0, outString.length() - 1);

				}
				outString = outString + "]";

			} else {
				if (isPaging.equals("true")) {
					// 返回分页表格數據
					for (int i = index; i < pageSize + index; i++) {
						try {
							outputList.add(resultList.get(i));
						} catch (Exception e) {
							// 最后一页可能不足一页，会报错，忽略
						}
					}
				} else {
					// 返回不分頁表格數據
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

			if (isCombo.equals("true") || isTree.equals("true")) {
				// System.out.println(outString);
				out.write(outString);
			} else {
				// System.out.println(outputJson);
				out.write(outputJson);
			}

		}
		return null;
	}
}
