package com.wireless.Actions.deptMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.menuMgr.Department;

public class QueryDeptTreeAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String warehouse = request.getParameter("warehouse");
		String extra = "";
		if(warehouse == null || warehouse.isEmpty()){
			extra = " AND dept_id <> " + Department.DeptId.DEPT_WAREHOUSE.getVal();
		}
		DBCon dbCon = new DBCon();
		
		StringBuffer jsonSB = new StringBuffer();
		try{
			String restaurantID = (String)request.getAttribute("restaurantID");
			if(restaurantID == null){
				return null;
			}
			String sql = " SELECT dept_id, name, type, restaurant_id " 
					+ " FROM " 
					+ Params.dbName + ".department " 
					+ " WHERE restaurant_id = " + restaurantID 
					+ " AND type = " + Department.Type.NORMAL.getVal() 
					+ extra
					+ " ORDER BY dept_id ";
			
			dbCon.connect();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			int index = 0;
			while (dbCon.rs != null && dbCon.rs.next()) {
				jsonSB.append(index > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("id:'dept_id_" + dbCon.rs.getInt("dept_id") + "'");
				jsonSB.append(",");
				jsonSB.append("text:'" + dbCon.rs.getString("name") + "'");
				jsonSB.append(",deptID:'" + dbCon.rs.getInt("dept_id") + "'");
				jsonSB.append(",type:'" + dbCon.rs.getInt("type") + "'");
				jsonSB.append(",restaurantID:'" + dbCon.rs.getInt("restaurant_id") + "'");
				jsonSB.append(",leaf:true");
				jsonSB.append("}");
				index++;
				
			}
			dbCon.rs.close();
		} catch(Exception e){
			e.printStackTrace();
		} finally{
			dbCon.disconnect();
			response.getWriter().print("[" + jsonSB.toString() + "]");
		}
		return null;
	}

	
}
