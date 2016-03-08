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
import com.wireless.db.printScheme.PrintFuncDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.DepartmentTree.DeptNode;
import com.wireless.pojo.menuMgr.DepartmentTree.KitchenNode;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.Kitchen.Type;
import com.wireless.pojo.printScheme.PrintFunc;
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
	public ActionForward tree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		final String pin = (String)request.getAttribute("pin");
		final StringBuilder jsb = new StringBuilder();
		try{
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
	
	public ActionForward deptKitchenTree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final StringBuilder jsonSB = new StringBuilder();
		try{
			final String pin = (String)request.getAttribute("pin");
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
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
	public ActionForward normal(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		
		final JObject jObject = new JObject();
		final String flag = request.getParameter("flag");
		try{
			
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final List<Kitchen> root = new DepartmentTree.Builder(DepartmentDao.getByType(staff, Department.Type.NORMAL), KitchenDao.getByType(staff, Kitchen.Type.NORMAL)).build().asKitchenList();
			if(root != null){
				jObject.setTotalProperty(root.size());
				jObject.setRoot(root);
			}
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString(flag != null ? Kitchen.KITCHEN_JSONABLE_SIMPLE : Kitchen.KITCHEN_JSONABLE_COMPLEX));
		}
		return null;
	}
	
	/**
	 * 添加打印方案的厨房列表
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward printKitchenTree(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)	throws Exception {
		StringBuilder jsonSB = new StringBuilder();
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			List<DeptNode> depts = new DepartmentTree.Builder(DepartmentDao.getByType(staff, Department.Type.NORMAL), KitchenDao.getByType(staff, Kitchen.Type.NORMAL)).build().asDeptNodes();
			for (int i = 0; i < depts.size(); i++) {
				jsonSB.append(i > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("leaf:false");
				jsonSB.append(",text:'" + depts.get(i).getKey().getName() + "'");
				jsonSB.append(",deptID:'" + depts.get(i).getKey().getId() + "'");
				jsonSB.append(",expanded : false");
				jsonSB.append(",checked:false");
				jsonSB.append(",children:[");
				jsonSB.append(getChildren4Print(depts.get(i).getValue(), null));
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
	
	private boolean deptCheck = false;
	
	/**
	 * 修改打印方案的厨房列表
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward printKitchenTree4Update(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		StringBuilder jsonSB = new StringBuilder();
		try{
			String pin = (String)request.getAttribute("pin");
			
			String printerId = request.getParameter("printerId");
			String schemeId = request.getParameter("schemeId");
			
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			List<PrintFunc> pfs = PrintFuncDao.getByCond(StaffDao.verify(Integer.parseInt(pin)), new PrintFuncDao.ExtraCond().setPrinter(Integer.parseInt(printerId)));
			PrintFunc scheme = null;
			for (PrintFunc printFunc : pfs) {
				if((printFunc.getId()+"").equals(schemeId)){
					scheme = printFunc;
				}
			}
			
			
			List<DeptNode> depts = new DepartmentTree.Builder(DepartmentDao.getByType(staff, Department.Type.NORMAL), KitchenDao.getByType(staff, Kitchen.Type.NORMAL)).build().asDeptNodes();
			for (int i = 0; i < depts.size(); i++) {
				deptCheck = false;
				jsonSB.append(i > 0 ? "," : "");
				jsonSB.append("{");
				jsonSB.append("leaf:false");
				jsonSB.append(",text:'" + depts.get(i).getKey().getName() + "'");
				jsonSB.append(",deptID:'" + depts.get(i).getKey().getId() + "'");
				jsonSB.append(",children:[");
				jsonSB.append(getChildren4Print(depts.get(i).getValue(), scheme.getKitchens()));
				jsonSB.append("]");
				jsonSB.append(",expanded :"+ deptCheck);
				jsonSB.append(",checked:" + deptCheck);
				jsonSB.append("}");
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
	
	
	private StringBuilder getChildren4Print(List<KitchenNode> list, List<Kitchen> kitchens) throws SQLException{
		StringBuilder jsb = new StringBuilder();
		
		for(int i = 0; i < list.size(); i++){
			if(i > 0){
				jsb.append(",");
			}
			jsb.append("{");
			jsb.append("leaf:true");
			jsb.append(",text:'" + list.get(i).getKey().getName() + "'");
			boolean kitchenCheck = false;
			if(kitchens != null){
				for (Kitchen k : kitchens) {
					if(k.getId() == list.get(i).getKey().getId()){
						kitchenCheck = true;
						deptCheck = true;
						break;
					}
				}
			}
			jsb.append(",checked:" + kitchenCheck);
			jsb.append(",isKitchen:true");
			jsb.append(",kid:" + list.get(i).getKey().getId());
			jsb.append("}");
		}
		
		return jsb;
	}		
	
}
