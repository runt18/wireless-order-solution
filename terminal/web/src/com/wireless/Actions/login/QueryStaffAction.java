package com.wireless.Actions.login;

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

import com.wireless.db.staffMgr.QueryStaffTerminal;
import com.wireless.pojo.staffMgr.StaffTerminal;

public class QueryStaffAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

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

		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			int restaurantID = Integer.parseInt(request.getParameter("restaurantID"));

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

			// combine the operator and filter value
			String filterCondition = null;
			// [ "0", "全部" ], [ "1", "编号" ], [ "2", "姓名" ]
			if (type == 1) {
				// 按编号
				filterCondition = " AND staff_alias " + ope + filterVal;
			} else if (type == 2) {
				// 按姓名
				filterCondition = " AND name like '%" + filterVal + "%'";
			} else {
				// 全部
				filterCondition = "";
			}

			StaffTerminal[] staffTerminals = QueryStaffTerminal.exec(restaurantID, filterCondition, " order by staff_alias ");

			// if (staffs.length != 0) {
			for (int i = 0; i < staffTerminals.length; i++) {
				//
				HashMap resultMap = new HashMap();

				resultMap.put("staffID", staffTerminals[i].id);
				resultMap.put("staffAlias", staffTerminals[i].aliasID);
				resultMap.put("staffName", staffTerminals[i].name);
				resultMap.put("staffPassword", staffTerminals[i].pwd);
				resultMap.put("terminalID", staffTerminals[i].terminalId);
				resultMap.put("staffGift", staffTerminals[i].getGiftAmount());
				resultMap.put("quotaOrig", staffTerminals[i].getGiftQuota());
				resultMap.put("staffQuota", staffTerminals[i].getGiftQuota());
				resultMap.put("pin", staffTerminals[i].pin);
				resultMap.put("type", staffTerminals[i].type);
				
				if (staffTerminals[i].getGiftQuota() < 0) {
					resultMap.put("noLimit", true);
				} else {
					resultMap.put("noLimit", false);
				}

				resultMap.put("message", "normal");

				resultList.add(resultMap);

			}
			// } else {
			// isError = true;
			// HashMap resultMap = new HashMap();
			// resultMap.put("message", "您的餐厅还没有任何员工信息，请在会员中心中添加员工");
			// resultList.add(resultMap);
			// }

		} catch (SQLException e) {
			e.printStackTrace();
			isError = true;
			HashMap resultMap = new HashMap();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);

		} catch (IOException e) {
			e.printStackTrace();
			isError = true;
			HashMap resultMap = new HashMap();
			resultMap.put("message", "未处理异常");
			resultList.add(resultMap);

		} finally {
			String outString = "";

			if (isError) {
				rootMap.put("root", resultList);
			} else if (isCombo.equals("true")) {
				outString = "{\"root\":[";

				if (resultList.size() == 0) {

				} else {
					for (int i = 0; i < resultList.size(); i++) {

						outString = outString
								+ "{staffID:"
								+ ((HashMap) (resultList.get(i))).get("staffID").toString() + ",";
						outString = outString
								+ "staffName:'"
								+ ((HashMap) (resultList.get(i))).get("staffName").toString() + "'},";

					}
					outString = outString.substring(0, outString.length() - 1);

				}
				outString = outString + "]}";
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

			String outputJson = "{\"totalProperty\":" + resultList.size() + "," + obj.toString().substring(1);

			if (isCombo.equals("true")) {
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
