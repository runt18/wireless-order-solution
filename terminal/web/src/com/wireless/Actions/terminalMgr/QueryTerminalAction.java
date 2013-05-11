package com.wireless.Actions.terminalMgr;

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
import com.wireless.db.frontBusiness.QueryTerminal;
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.protocol.Terminal;

public class QueryTerminalAction extends Action {

	private static final long serialVersionUID = 1L;

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		PrintWriter out = null;

		DBCon dbCon = new DBCon();

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

			String pin = request.getParameter("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			Terminal[] terminals = QueryTerminal.exec(dbCon,
					term.restaurantID, QueryTerminal.QUERY_REAL_TERM, "", "");

			for (int i = 0; i < terminals.length; i++) {
				HashMap resultMap = new HashMap();
				/**
				 * The json to each order looks like below
				 * 
				 */
				resultMap.put("terminalID", terminals[i].id);
				resultMap.put("pin", terminals[i].pin);
				resultMap.put("modelID", terminals[i].modelID);
				resultMap.put("modelName", terminals[i].modelName);
				resultMap.put("ownerName", terminals[i].owner);
				if (terminals[i].expireDate == null) {
					resultMap.put("expireDate", null);
				} else {
					resultMap.put("expireDate", new java.util.Date(
							terminals[i].expireDate.getTime()));
				}
				resultMap.put("giftAmount", terminals[i].getGiftAmount());
				resultMap.put("giftQuota", terminals[i].getGiftQuota());
				resultMap.put("quotaOrig", terminals[i].getGiftQuota());
				if (terminals[i].getGiftQuota() < 0) {
					resultMap.put("noLimit", true);
				} else {
					resultMap.put("noLimit", false);
				}

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

			String outString = "";

			if (isError) {
				rootMap.put("root", resultList);
			} else if (isCombo.equals("true")) {
				outString = "{\"root\":[";

				if (resultList.size() == 0) {

				} else {
					for (int i = 0; i < resultList.size(); i++) {

						outString = outString
								+ "{terminalID:"
								+ ((HashMap) (resultList.get(i))).get(
										"terminalID").toString() + ",";
						outString = outString
								+ "modelName:'"
								+ ((HashMap) (resultList.get(i))).get(
										"modelName").toString() + "'},";

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
			// 解决数字类型null显示为0的问题
			// jsonConfig.registerDefaultValueProcessor(java.lang.Long.class,
			// new NumberJsonValueProcessor());
			// jsonConfig.registerDefaultValueProcessor(java.lang.Double.class,
			// new NumberJsonValueProcessor());
			// jsonConfig.registerDefaultValueProcessor(java.lang.Integer.class,
			// new NumberJsonValueProcessor());
			// 解决日期类型显示问题
//			jsonConfig.registerJsonValueProcessor(java.util.Date.class,
//					new DateJsonValueProcessor("yyyy-MM-dd"));
//			jsonConfig.registerJsonValueProcessor(java.sql.Timestamp.class,
//					new DateJsonValueProcessor("yyyy-MM-dd"));
			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			String outputJson = "{\"totalProperty\":" + resultList.size() + ","
					+ obj.toString().substring(1);

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
