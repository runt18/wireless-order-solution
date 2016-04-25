package com.wireless.Actions.deptMgr;

import java.sql.SQLException;
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
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.DepartmentTree.DeptNode;
import com.wireless.pojo.menuMgr.DepartmentTree.KitchenNode;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;

public class QueryDeptTreeAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String) request.getAttribute("pin");
		
		final String branchId = request.getParameter("branchId");
		
		final String warehouse = request.getParameter("warehouse");
		
		final StringBuilder jsonSB = new StringBuilder();
		
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			List<Department> deptList;
			
			if(warehouse != null && !warehouse.isEmpty()){
				deptList = DepartmentDao.getDepartments4Inventory(staff);
			}else{
				deptList = DepartmentDao.getByType(staff, Department.Type.NORMAL);
			}
			
			List<DeptNode> depts = new DepartmentTree.Builder(deptList, KitchenDao.getByType(staff, Kitchen.Type.NORMAL)).build().asDeptNodes();
			
			for (int i = 0; i < depts.size(); i++) {
				jsonSB.append(i > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("id:'dept_id_" + depts.get(i).getKey().getId() + "'");
				jsonSB.append(",");
				jsonSB.append("text:'" +depts.get(i).getKey().getName() + "'");
				jsonSB.append(",deptID:'" + depts.get(i).getKey().getId() + "'");
				jsonSB.append(",type:'" + depts.get(i).getKey().getType().getVal() + "'");
				jsonSB.append(",restaurantID:'" + depts.get(i).getKey().getRestaurantId() + "'");
				jsonSB.append(",expanded : true");
				jsonSB.append(",expandable : true");
				jsonSB.append(",children:[");
				jsonSB.append(getChildren(depts.get(i).getValue()));
				jsonSB.append("]}");
			}
		} catch(BusinessException | SQLException e){
			e.printStackTrace();
		} finally{
			response.getWriter().print("[" + jsonSB.toString() + "]");
		}
		return null;
	}
	
	private StringBuilder getChildren(List<KitchenNode> list) throws SQLException{
		StringBuilder jsb = new StringBuilder();
		
		for(int i = 0; i < list.size(); i++){
			if(i > 0){
				jsb.append(",");
			}
			jsb.append("{");
			jsb.append("leaf:true");
			jsb.append(",text:'" + list.get(i).getKey().getName() + "'");
			jsb.append(",isAllowTemp:" + list.get(i).getKey().isAllowTemp());
			jsb.append(",KitchenID:" + list.get(i).getKey().getId());
			jsb.append(",name:'" + list.get(i).getKey().getName() + "'");
			jsb.append(",belongDept:" + list.get(i).getKey().getDept().getId());
			if(list.get(i).getKey().isAllowTemp()){
				jsb.append(",icon:'../../images/linshicai.png'");
			}
			jsb.append("}");
		}
		
		return jsb;
	}
	
}
