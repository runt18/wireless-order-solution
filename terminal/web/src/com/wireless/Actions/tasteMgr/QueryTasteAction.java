package com.wireless.Actions.tasteMgr;

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
import com.wireless.db.frontBusiness.VerifyPin;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.protocol.Terminal;

public class QueryTasteAction extends Action {

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

		// String jsonResp = "{success:$(result), data:'$(value)'}";
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

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

			// combine the operator and filter value
			String filterCondition = null;
			// [ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ], [ "3", "價格" ], [
			// "4", "類型" ]
			if (type == 1) {
				// 按编号
				filterCondition = " AND taste_alias " + ope + filterVal;
			} else if (type == 2) {
				// 按名称
				filterCondition = " AND preference like '%" + filterVal + "%'";
			} else if (type == 3) {
				// 按價格
				filterCondition = " AND price " + ope + filterVal;
			} else if (type == 4) {
				// 按類型
				filterCondition = " AND category = " + filterVal;
			} else {
				// 全部
				filterCondition = "";
			}

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			List<Taste> tastes = TasteDao.getTastes(dbCon, term, null, " ORDER BY TASTE.taste_alias ");

			/*
			 * “口味分类”的值如下： 0 - 口味 ， 1 - 做法， 2 - 规格 “计算方式”的值如下：0 - 按价格，1 - 按比例
			 */

			for (Taste t : tastes) {
				// ID，別名編號，名稱，價錢，比例，種類，計算方法
				HashMap resultMap = new HashMap();

				resultMap.put("tasteID", t.getTasteId());
				resultMap.put("tasteAlias", t.getAliasId());
				resultMap.put("tasteName", t.getPreference());
				resultMap.put("tastePrice", t.getPrice());
				resultMap.put("tasteRate", t.getRate());
				resultMap.put("tasteCategory", t.getCategory());
				resultMap.put("tasteCalc", t.getCalc());
				resultMap.put("type", t.getType());

				resultMap.put("message", "normal");

				resultList.add(resultMap);

			}

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
								+ "{tasteID:"
								+ ((HashMap) (resultList.get(i)))
										.get("tasteID").toString() + ",";
						outString = outString
								+ "tasteName:'"
								+ ((HashMap) (resultList.get(i))).get(
										"tasteName").toString() + "'},";

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