package com.wireless.Actions.deptMgr.kitchenMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.DepartmentTree.DeptNode;
import com.wireless.pojo.menuMgr.DepartmentTree.KitchenNode;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.Kitchen.Type;
import com.wireless.pojo.staffMgr.Staff;

public class QueryKitchenAction extends DispatchAction {
	
	/**
	 * 树形数据格式
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward tree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		StringBuilder jsb = new StringBuilder();
		try{
			String pin = (String)request.getAttribute("pin");
			List<Kitchen> list = KitchenDao.getByType(StaffDao.verify(Integer.parseInt(pin)), Type.NORMAL);
			for(int i = 0; i < list.size(); i++){
				if(i > 0){
					jsb.append(",");
				}
				jsb.append("{");
				jsb.append("leaf:true");
				jsb.append(",text:'" + list.get(i).getName() + "'");
				jsb.append(",alias:" + list.get(i).getDisplayId());
				jsb.append(",name:'" + list.get(i).getName() + "'");
				jsb.append(",kid:" + list.get(i).getId());
				jsb.append("}");
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print("[" + jsb.toString() + "]");
		}
		return null;
	}
	
	public ActionForward deptKitchenTree(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		StringBuilder jsonSB = new StringBuilder();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
/*			List<Department> depts = DepartmentDao.getByType(staff, Department.Type.NORMAL);
			
			for (int i = 0; i < depts.size(); i++) {
				jsonSB.append(i > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("id:'dept_id_" + depts.get(i).getId() + "'");
				jsonSB.append(",text:'" + depts.get(i).getName() + "'");
				jsonSB.append(",deptID:'" + depts.get(i).getId() + "'");
				jsonSB.append(",type:'" + depts.get(i).getType().getVal() + "'");
				jsonSB.append(",cls:'floatBarStyle'");
				jsonSB.append(",restaurantID:'" + depts.get(i).getRestaurantId() + "'");
				jsonSB.append(",expanded : true");
				jsonSB.append(",expandable : true");
				jsonSB.append(",children:[");
				jsonSB.append(getChildren(staff, depts.get(i).getId()));
				jsonSB.append("]}");
			}
*/
			List<DeptNode> depts = new DepartmentTree.Builder(DepartmentDao.getByType(staff, Department.Type.NORMAL), KitchenDao.getByType(staff, Kitchen.Type.NORMAL)).build().asDeptNodes();
			for (int i = 0; i < depts.size(); i++) {
				jsonSB.append(i > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("id:'dept_id_" + depts.get(i).getKey().getId() + "'");
				jsonSB.append(",text:'" + depts.get(i).getKey().getName() + "'");
				jsonSB.append(",deptID:'" + depts.get(i).getKey().getId() + "'");
				jsonSB.append(",type:'" + depts.get(i).getKey().getType().getVal() + "'");
				jsonSB.append(",cls:'floatBarStyle'");
				jsonSB.append(",restaurantID:'" + depts.get(i).getKey().getRestaurantId() + "'");
				jsonSB.append(",expanded : true");
				jsonSB.append(",expandable : true");
				jsonSB.append(",children:[");
				jsonSB.append(getChildren(depts.get(i).getValue()));
				jsonSB.append("]}");
			}
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
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
			jsb.append(",name:'" + list.get(i).getKey().getName() + "'");
			jsb.append(",kid:" + list.get(i).getKey().getId());
			jsb.append(",belongDept:" + list.get(i).getKey().getDept().getId());
			if(list.get(i).getKey().isAllowTemp()){
				jsb.append(",icon:'../../images/linshicai.png'");
			}
			jsb.append("}");
		}
		
		return jsb;
	}
	
	/**
	 * 普通数据格式
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		List<Kitchen> root = null;
		
		try{
			String pin = (String)request.getAttribute("pin");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
//			root = KitchenDao.getByType(staff, Type.NORMAL);
			root = new DepartmentTree.Builder(DepartmentDao.getByType(staff, Department.Type.NORMAL), KitchenDao.getByType(staff, Kitchen.Type.NORMAL)).build().asKitchenList();
			
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(root != null){
				jobject.setTotalProperty(root.size());
				jobject.setRoot(root);
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
