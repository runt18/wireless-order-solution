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
		String regionName = request.getParameter("regionName");
		String restaurantID = request.getParameter("restaurantID");
//		String dbNumber = request.getParameter("dbNumber");

//		if (regionID == null && regionName == null) {
//			System.out.println("::::::::::::11111");
//		} else {
//			System.out.println(regionID);
//			System.out.println("111111111111111::::::::::::11111");
//		}
//		 System.out.println(regionID + ":::"+regionName);

//		if(regionID == null && regionName == null && dbNumber == null){
//			System.out.println("1111111111111");
//			
//		}else if(dbNumber != null){
//			System.out.println(dbNumber+"2222222222222");
//		}

		String SQL_ALL_COUNT = "SELECT count(*) AS all_table_cnt FROM "
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
				+ (regionName == null || regionName.trim().equals("") ? ""
						: " AND name like '%" + regionName + "%' ");//
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
				+ (regionName == null || regionName.trim().equals("") ? ""
						: " AND name like '%" + regionName + "%' ")
				+ " ORDER BY"
				+ " table_id "
				+ "LIMIT "
				+ start
				+ ","
				+ limit + "";
		try {
			dbCon.connect();// 获得链接
			dbCon.rs = dbCon.stmt.executeQuery(SQL_ALL_COUNT);
			if (dbCon.rs.next()) {
				totalProperty = dbCon.rs.getInt("all_table_cnt");
			}
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			int index = 0;
			jsonSB.append("{totalProperty:" + totalProperty + ",root:[");

			while (dbCon.rs != null && dbCon.rs.next()) {
				jsonSB.append(index > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("tableID : '" + dbCon.rs.getInt("table_id")
						+ "'");
				jsonSB.append(",");
				jsonSB.append("tableAlias : '"
						+ dbCon.rs.getInt("table_alias") + "'");
				jsonSB.append(",");
				jsonSB.append("tableName : '" + dbCon.rs.getString("name")
						+ "'");
				jsonSB.append(",");
				jsonSB.append("tableRegion : '"
						+ dbCon.rs.getInt("region_id") + "'");
				jsonSB.append(",");
				jsonSB.append("tableMinCost : '"
						+ dbCon.rs.getInt("minimum_cost") + "'");
				jsonSB.append(",");
				jsonSB.append("tableServiceRate : '"
						+ dbCon.rs.getFloat("service_rate") + "'");
				jsonSB.append(",");
				jsonSB.append("tableStatusDisplay : '"
						+ dbCon.rs.getInt("status") + "'");
				jsonSB.append(",");
				jsonSB.append("tableCategoryDisplay : '"
						+ dbCon.rs.getInt("category") + "'");
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
