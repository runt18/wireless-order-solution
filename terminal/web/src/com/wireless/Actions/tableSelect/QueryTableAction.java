package com.wireless.Actions.tableSelect;

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
import com.wireless.db.QueryTable;
import com.wireless.db.VerifyPin;
import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Order;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class QueryTableAction extends Action {

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

		// String jsonResp = "{success:$(result), data:'$(value)'}";
		try {
			// 解决后台中文传到前台乱码
			response.setContentType("text/json; charset=utf-8");
			out = response.getWriter();

			String pin = request.getParameter("pin");
			if (pin.startsWith("0x") || pin.startsWith("0X")) {
				pin = pin.substring(2);
			}

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
			// [ "0", "全部" ], [ "1", "编号" ], [ "2", "名称" ], [ "3", "区域" ], [
			// "4", "状态 " ]
			if (type == 1) {
				// 按编号
				filterCondition = " AND alias_id " + ope + filterVal;
			} else if (type == 2) {
				// 按名称
				filterCondition = " AND name like '%" + filterVal + "%'";
			} else if (type == 3) {
				// 按区域
				filterCondition = " AND region_id " + ope + filterVal;
			} else if (type == 4) {
				// 按状态
				filterCondition = " AND status " + ope + filterVal;
			} else {
				// 全部
				filterCondition = "";
			}

			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, Integer.parseInt(pin, 16),
					Terminal.MODEL_STAFF);

			Table[] tables = QueryTable.exec(Integer.parseInt(pin, 16),
					Terminal.MODEL_STAFF, filterCondition, "");

			// jsonResp = jsonResp.replace("$(result)", "true");
			// // format the table results into response string in the form of
			// JSON
			// if (tables.length == 0) {
			// jsonResp = jsonResp.replace("$(value)", "");
			// } else {
			//
			// StringBuffer value = new StringBuffer();
			// for (int i = 0; i < tables.length; i++) {
			// /**
			// * The json format of table query looks like below.
			// * ID，別名編號，名稱，區域，人數，狀態，種類，最低消費
			// *
			// ["餐台1编号","餐台1人数","占用","餐台1名称","一般",0]，["餐台2编号","餐台2人数","空桌","餐台2名称","外卖",300.50]
			// */
			// String jsonTable =
			// "[\"$(alias_id)\",\"$(custom_num)\",\"$(status)\",\"$(alias_name)\",\"$(category)\",$(minimum_cost)]";
			// jsonTable = jsonTable.replace("$(alias_id)",
			// Integer.toString(tables[i].alias_id));
			// jsonTable = jsonTable.replace("$(custom_num)", new
			// Short(tables[i].custom_num).toString());
			//
			// if(tables[i].name != null){
			// jsonTable = jsonTable.replace("$(alias_name)", tables[i].name);
			// }else{
			// jsonTable = jsonTable.replace("$(alias_name)", "");
			// }
			//
			// if(tables[i].status == Table.TABLE_BUSY) {
			// jsonTable = jsonTable.replace("$(status)", "占用");
			// }else{
			// jsonTable = jsonTable.replace("$(status)", "空桌");
			// }
			//
			// if(tables[i].category == Order.CATE_NORMAL){
			// jsonTable = jsonTable.replace("$(category)", "一般");
			//
			// }else if(tables[i].category == Order.CATE_TAKE_OUT){
			// jsonTable = jsonTable.replace("$(category)", "外卖");
			//
			// }else if(tables[i].category == Order.CATE_JOIN_TABLE){
			// jsonTable = jsonTable.replace("$(category)", "并台");
			//
			// }else if(tables[i].category == Order.CATE_MERGER_TABLE){
			// jsonTable = jsonTable.replace("$(category)", "拼台");
			//
			// }else{
			// jsonTable = jsonTable.replace("$(category)", "一般");
			// }
			//
			// jsonTable = jsonTable.replace("$(minimum_cost)",
			// tables[i].getMinimumCost().toString());
			//
			// // put each json table info to the value
			// value.append(jsonTable);
			// // the string is separated by comma
			// if (i != tables.length - 1) {
			// value.append("，");
			// }
			// }
			//
			// jsonResp = jsonResp.replace("$(value)", value);
			// }

			for (int i = 0; i < tables.length; i++) {
				// ID，別名編號，名稱，區域，人數，狀態，種類，最低消費
				HashMap resultMap = new HashMap();

				resultMap.put("tableID", tables[i].alias_id);
				resultMap.put("tableAlias", tables[i].alias_id);
				resultMap.put("tableName", tables[i].name);
				resultMap.put("tableRegion", tables[i].regionID);
				resultMap.put("tableCustNbr", tables[i].custom_num);
				resultMap.put("tableStatus", tables[i].status);
				resultMap.put("tableCategory", tables[i].category);
				resultMap.put("tableMinCost", tables[i].getMinimumCost());

				resultMap.put("message", "normal");

				resultList.add(resultMap);

			}

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

			String outString = "";

			if (isError) {
				rootMap.put("root", resultList);
			} else if (isCombo.equals("true")) {
				outString = "{\"root\":[";

				if (resultList.size() == 0) {

				} else {
					for (int i = 0; i < resultList.size(); i++) {

						outString = outString
								+ "{tableID:"
								+ ((HashMap) (resultList.get(i)))
										.get("tableID").toString() + ",";
						outString = outString
								+ "tableName:'"
								+ ((HashMap) (resultList.get(i))).get(
										"tableName").toString() + "'},";

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
				//System.out.println(outString);
				out.write(outString);
			} else {
				//System.out.println(outputJson);
				out.write(outputJson);
			}

		}
		return null;
	}

}
