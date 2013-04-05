package com.wireless.Actions.regionMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;

public class QueryRegionTableAction extends Action {

	// 操作符： 1表示等于号，2表示大于等于，3表示小于等于
	private final static String EQUAL_TO = "1";// 等于
	private final static String EQUAL_THAN = "2";// 大于等于
	private final static String EQUAL_LESS = "3";// 小于等于

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		response.setContentType("text/json; charset=utf-8");
		DBCon dbCon = new DBCon();
		StringBuffer jsonSB = new StringBuffer();
		int totalProperty = 0;// 总记录数；

		int start = Integer.parseInt(request.getParameter("start").toString());
		int limit = Integer.parseInt(request.getParameter("limit").toString());
		String regionID = request.getParameter("regionID");

		String restaurantID = request.getParameter("restaurantID");

		String operatorNumbersOS;
		if (EQUAL_TO.equals(request.getParameter("operatorNumbersO"))) {
			operatorNumbersOS = " = ";
		} else if (EQUAL_THAN.equals(request.getParameter("operatorNumbersO"))) {
			operatorNumbersOS = " >= ";
		} else if (EQUAL_LESS.equals(request.getParameter("operatorNumbersO"))) {
			operatorNumbersOS = " <= ";
		} else {
			operatorNumbersOS = " = ";
		}
		// 操作值
		String operatorNumbersN = request.getParameter("operatorNumbersN");
		String operatorNumbersA = request.getParameter("operatorNumbersA");
		// 餐台名字
		String operatorName = request.getParameter("operatorName");
		// 餐台状态
		String operatorStates = request.getParameter("operatorStates");
		// 餐台类型
		String operatorTypes = request.getParameter("operatorTypes");

		String sqlAllCount = "SELECT count(*) AS all_table_cnt FROM "
				+ Params.dbName
				+ ".table "
				+ "WHERE restaurant_id = "
				+ restaurantID
				+ " "
				+ ""
				+ (regionID == null || regionID.trim().equals("")
						|| regionID == "" || regionID.equals("-1")
						|| regionID == "-1" ? ""
						: (" AND region_id = " + regionID))
				+ (operatorName == null || operatorName.trim().equals("") ? ""
						: " AND name like '%" + operatorName.trim() + "%' ")
				+ (operatorNumbersN == null
						|| operatorNumbersN.trim().equals("") ? ""
						: " AND table_alias " + operatorNumbersOS + " "
								+ operatorNumbersN.trim() + " ")
				+ (operatorNumbersA == null
						|| operatorNumbersA.trim().equals("") ? ""
						: " AND minimum_cost " + operatorNumbersOS + ""
								+ operatorNumbersA.trim() + " ")
				+ (operatorStates == null || operatorStates.trim().equals("") ? ""
						: " AND status like '%" + operatorStates.trim() + "%' ")
				+ (operatorTypes == null || operatorTypes.trim().equals("") ? ""
						: " AND category like '%" + operatorTypes.trim()
								+ "%' ");//
		String sql = "SELECT table_id, table_alias, name, region_id, minimum_cost, service_rate, status, category"
				+ " FROM "
				+ Params.dbName
				+ ".table WHERE restaurant_id = "
				+ restaurantID
				+ ""
				+ (regionID == null || regionID.trim().equals("")
						|| regionID == "" || regionID.equals("-1")
						|| regionID == "-1" ? ""
						: (" AND region_id = " + regionID))
				+ (operatorName == null || operatorName.trim().equals("") ? ""
						: " AND name like '%" + operatorName.trim() + "%' ")
				+ (operatorNumbersN == null
						|| operatorNumbersN.trim().equals("") ? ""
						: " AND table_alias " + operatorNumbersOS + ""
								+ operatorNumbersN.trim() + " ")
				+ (operatorNumbersA == null
						|| operatorNumbersA.trim().equals("") ? ""
						: " AND minimum_cost " + operatorNumbersOS + " "
								+ operatorNumbersA.trim() + " ")
				+ (operatorStates == null || operatorStates.trim().equals("") ? ""
						: " AND status like '%" + operatorStates.trim() + "%' ")
				+ (operatorTypes == null || operatorTypes.trim().equals("") ? ""
						: " AND category like '%" + operatorTypes.trim()
								+ "%' ")
				+ " ORDER BY"
				+ " table_id "
				+ "LIMIT " + start + "," + limit + "";
		try {

			dbCon.connect();// 获得链接
			dbCon.rs = dbCon.stmt.executeQuery(sqlAllCount);

			if (dbCon.rs.next()) {
				totalProperty = dbCon.rs.getInt("all_table_cnt");
			}

			dbCon.rs = dbCon.stmt.executeQuery(sql);
			int index = 0;
			jsonSB.append("{totalProperty:" + totalProperty + ",root:[");

			while (dbCon.rs != null && dbCon.rs.next()) {
				jsonSB.append(index > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("tableID : '" + dbCon.rs.getInt("table_id") + "'");
				jsonSB.append(",");
				jsonSB.append("tableAlias : '" + dbCon.rs.getInt("table_alias")+ "'");
				jsonSB.append(",");
				jsonSB.append("tableName : '" + dbCon.rs.getString("name")+ "'");
				jsonSB.append(",");
				jsonSB.append("tableRegion : '" + dbCon.rs.getInt("region_id")+ "'");
				jsonSB.append(",");
				jsonSB.append("tableMinCost : '"+ dbCon.rs.getInt("minimum_cost") + "'");
				jsonSB.append(",");
				jsonSB.append("tableServiceRate : '"+ dbCon.rs.getFloat("service_rate") + "'");
				jsonSB.append(",");
				jsonSB.append("tableStatusDisplay : '"+ dbCon.rs.getInt("status") + "'");
				jsonSB.append(",");
				jsonSB.append("tableCategoryDisplay : '"+ dbCon.rs.getInt("category") + "'");
				jsonSB.append(",");
				jsonSB.append("tableOpt : 'tableOpt'");
				jsonSB.append("}");
				index++;
			}
			jsonSB.append("]}");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			response.getWriter().print(jsonSB.toString());
		}
		return null;
	}
}
