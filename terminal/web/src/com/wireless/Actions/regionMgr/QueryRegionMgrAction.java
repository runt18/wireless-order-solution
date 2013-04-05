package com.wireless.Actions.regionMgr;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.system.Region;
import com.wireless.protocol.Terminal;

public class QueryRegionMgrAction extends Action {

	HashMap<String, String> resultMap;

	List<Map<String, String>> resultList;
	List<Map<String, String>> outputList;
	HashMap<String, String> rootMap;

	List<Region> regions;

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

		resultList = new ArrayList<Map<String, String>>();
		outputList = new ArrayList<Map<String, String>>();
		rootMap = new HashMap<String, String>();

		boolean isError = false;
		// 是否分頁
		String isPaging = request.getParameter("isPaging");
		// 是否combo
		String isCombo = request.getParameter("isCombo");
		// 是否Tree
		String isTree = request.getParameter("isTree");
		String isRegionTree = request.getParameter("isRegionTree");

		try {
			response.setContentType("text/json; charset=utf-8");// 解决后台中文传到前台乱码；
			out = response.getWriter();

			String pin = request.getParameter("pin");

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Long.parseLong(pin),
					Terminal.MODEL_STAFF);

			regions = RegionDao.exec(dbCon, term, null, null);

			for (int i = 0; i < regions.size(); i++) {

				resultMap = new HashMap<String, String>();

				resultMap.put("regionID", "" + regions.get(i).getRegionID());
				resultMap.put("regionName", regions.get(i).getRegionName());
				resultMap.put("message", "normal");

				resultList.add(resultMap);

			}
		} catch (BusinessException e) {
			e.printStackTrace();

			isError = true;
		} catch (SQLException e) {
			e.printStackTrace();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;
		} catch (IOException e) {
			e.printStackTrace();
			resultMap.put("message", "数据库请求发生错误，请确认网络是否连接正常");
			resultList.add(resultMap);
			isError = true;
		} finally {
			dbCon.disconnect();

			String outString = "";

			if (isError) {// 返回报错；
				rootMap.put("root", "" + resultList);
			} else if (isCombo.equals("true")) {// 返回Combo数据；
				outString = "{\"root\":[";
				if (resultList.size() == 0) {
				} else {
					for (int i = 0; i < resultList.size(); i++) {

						outString = outString
								+ "{regionID:"
								+ ((resultList.get(i))).get("regionID")
										.toString() + ",";
						outString = outString
								+ "regionName:'"
								+ ((resultList.get(i))).get("regionName")
										.toString() + "'},";
					}
					outString = outString.substring(0, outString.length() - 1);

				}
				outString = outString + "]}";
			} else if (isTree.equals("true")) {// 返回数据；
				if ("false".equals(isRegionTree)) {// 返回区域数据；
					outString = "[";
					if (resultList.size() == 0) {
					} else {
						for (int i = 0; i < resultList.size(); i++) {
							outString = outString
									+ "{regionID:'"
									+ ((resultList.get(i))).get("regionID")
											.toString() + "',";
							outString = outString
									+ "text:'"
									+ ((resultList.get(i))).get("regionName")
											.toString() + "',leaf:true},";
						}
						outString = outString.substring(0,
								outString.length() - 1);
					}
					outString = outString + "]";
				} else { // 返回其他区域数据；
					outString = "[";
					if (resultList.size() == 0) {
					} else {
						for (int i = 0; i < resultList.size(); i++) {
							outString = outString
									+ "{id:'region"
									+ ((resultList.get(i))).get("regionID")
											.toString() + "',";
							outString = outString
									+ "text:'"
									+ ((resultList.get(i))).get("regionName")
											.toString() + "',leaf:true},";
						}
						outString = outString.substring(0,
								outString.length() - 1);
					}
					outString = outString + "]";
				}
			} else {
				if (isPaging.equals("true")) { // 返回分页表格数据；
					for (int i = index; i < pageSize + index; i++) {
						try {
							outputList.add(resultList.get(i));
						} catch (Exception e) {
							// 最后一页可能不足一页，会报错，忽略
						}
					}
				} else {// 返回不分頁表格数据；
					for (int i = 0; i < resultList.size(); i++) {
						outputList.add(resultList.get(i));
					}
				}
				rootMap.put("root", "" + outputList);
			}

			JsonConfig jsonConfig = new JsonConfig();

			JSONObject obj = JSONObject.fromObject(rootMap, jsonConfig);

			String outputJson = "{\"totalProperty\":" + resultList.size() + ","
					+ obj.toString().substring(1);

			if (isCombo.equals("true") || isTree.equals("true")) {
				out.write(outString);
			} else if ("false".equals(isRegionTree) && "false".equals(isCombo)) {
				out.write(outString);
			} else {
				out.write(outputJson);
			}
		}
		return null;
	}
}