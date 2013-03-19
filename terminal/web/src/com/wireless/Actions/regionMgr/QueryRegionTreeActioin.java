package com.wireless.Actions.regionMgr;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;

public class QueryRegionTreeActioin extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		response.setContentType("text/json; charset=utf-8");
		DBCon dbCon = new DBCon();// openConnection；

		PrintWriter out = response.getWriter();// 获得打印流

		String restaurantID = request.getParameter("restaurantID");
		StringBuffer jsonSB = new StringBuffer();

		if (restaurantID != null) {
			try {
				String sql = "SELECT restaurant_id, region_id, name FROM "
						+ Params.dbName + ".region WHERE restaurant_id = "
						+ restaurantID + " ORDER BY region_id";
				dbCon.connect();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				int index = 0;
				while (dbCon.rs != null && dbCon.rs.next()) {
					jsonSB.append(index > 0 ? "," : "");
					jsonSB.append("{");
					jsonSB.append("regionID : '" + dbCon.rs.getInt("region_id")
							+ "'");
					jsonSB.append(",");
					jsonSB.append("text : '" + dbCon.rs.getString("name") + "'");
					jsonSB.append(",leaf : true");
					jsonSB.append("}");
					index++;
				}
				dbCon.rs.close();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				dbCon.disconnect();
				out.print("[" + jsonSB.toString() + "]");
			}
		}
		return null;
	}
}
