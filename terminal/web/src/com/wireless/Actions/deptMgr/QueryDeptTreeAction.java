package com.wireless.Actions.deptMgr;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.DepartmentTree.DeptNode;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;

public class QueryDeptTreeAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
/*		String warehouse = request.getParameter("warehouse");
		String extra = "";
		if(warehouse != null && !warehouse.isEmpty()){
			extra = " AND (type = " + Department.Type.NORMAL.getVal() + " OR type = " + Department.Type.WARE_HOUSE.getVal() + ")";
		}else{
			extra = " AND type = " + Department.Type.NORMAL.getVal();
		}
		DBCon dbCon = new DBCon();*/
		String pin = (String) request.getAttribute("pin");
		
		String warehouse = request.getParameter("warehouse");
		
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		StringBuffer jsonSB = new StringBuffer();
		
		List<Department> deptList;
		
		if(warehouse != null && !warehouse.isEmpty()){
			deptList = DepartmentDao.getDepartments4Inventory(staff);
		}else{
			deptList = DepartmentDao.getByType(staff, Department.Type.NORMAL);
		}
		
		
		List<DeptNode> depts = new DepartmentTree.Builder(deptList, KitchenDao.getByType(staff, Kitchen.Type.NORMAL)).build().asDeptNodes();
		try{
/*			String restaurantID = (String)request.getAttribute("restaurantID");
			if(restaurantID == null){
				return null;
			}
			String sql = " SELECT dept_id, name, type, restaurant_id " 
					+ " FROM " 
					+ Params.dbName + ".department " 
					+ " WHERE restaurant_id = " + restaurantID 
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
*/			
			
			for (int i = 0; i < depts.size(); i++) {
				jsonSB.append(i > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("id:'dept_id_" + depts.get(i).getKey().getId() + "'");
				jsonSB.append(",");
				jsonSB.append("text:'" +depts.get(i).getKey().getName() + "'");
				jsonSB.append(",deptID:'" + depts.get(i).getKey().getId() + "'");
				jsonSB.append(",type:'" + depts.get(i).getKey().getType().getVal() + "'");
				jsonSB.append(",restaurantID:'" + depts.get(i).getKey().getRestaurantId() + "'");
				jsonSB.append(",leaf:true");
				jsonSB.append("}");
			}
		} catch(Exception e){
			e.printStackTrace();
		} finally{
//			dbCon.disconnect();
			response.getWriter().print("[" + jsonSB.toString() + "]");
		}
		return null;
	}

	
}
