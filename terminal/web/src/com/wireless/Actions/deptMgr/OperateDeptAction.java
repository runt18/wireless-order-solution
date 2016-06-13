package com.wireless.Actions.deptMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Department.AddBuilder;
import com.wireless.pojo.menuMgr.Department.MoveBuilder;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class OperateDeptAction extends DispatchAction{

	/**
	 * 获取部门信息
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward getByCond(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String branchId = request.getParameter("branchId");
		final String isPaging = request.getParameter("isPaging");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		final String inventory = request.getParameter("inventory");
		final String type = request.getParameter("type");
		try{
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}
			
			List<Department> root;
			if(inventory != null && !inventory.isEmpty()){
				root = DepartmentDao.getDepartments4Inventory(staff);
			}else{
				if(type != null && !type.isEmpty()){
					root = DepartmentDao.getByType(staff, Department.Type.valueOf(Integer.parseInt(type)));
				}else{
					root = DepartmentDao.getByType(staff, Department.Type.NORMAL);
				}
			}
			
			if(start != null && !start.isEmpty() && limit != null && !limit.isEmpty()){
				root = DataPaging.getPagingData(root, Boolean.parseBoolean(isPaging), start, limit);
			}
			
			jObject.setTotalProperty(root.size());
			jObject.setRoot(DataPaging.getPagingData(root, Boolean.parseBoolean(isPaging), start, limit));
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{

			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	/**
	 * 互换部门
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward swap(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String deptA = request.getParameter("deptA");
		final String deptB = request.getParameter("deptB");
		
		final String pin = (String) request.getAttribute("pin");
		
		JObject jobject = new JObject();
		try{
			final Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			final Department.MoveBuilder builder = new MoveBuilder(Integer.parseInt(deptA), Integer.parseInt(deptB));
			
			DepartmentDao.move(staff, builder);
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	/**
	 * 增加部门
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward addDept(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String deptName = request.getParameter("deptName");
		final String pin = (String) request.getAttribute("pin");
		final JObject jObject = new JObject();
		try{
			DepartmentDao.add(StaffDao.verify(Integer.parseInt(pin)), new AddBuilder(deptName));
			jObject.initTip(true, "添加部门成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	/**
	 * 修改部门
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward updateDept(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String deptId = request.getParameter("deptID");
		final String deptName = request.getParameter("deptName");
		final String pin = (String) request.getAttribute("pin");
		final JObject jObject = new JObject();
		try{
			DepartmentDao.update(StaffDao.verify(Integer.parseInt(pin)), 
					new Department.UpdateBuilder(Department.DeptId.valueOf(Integer.parseInt(deptId)), deptName));
			jObject.initTip(true, "修改部门成功");
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
	/**
	 * 删除部门
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward removeDept(ActionMapping mapping, ActionForm form,	HttpServletRequest request, HttpServletResponse response) throws Exception {
		final String deptId = request.getParameter("deptID");
		final String pin = (String) request.getAttribute("pin");
		final JObject jObject = new JObject();
		try{
			DepartmentDao.remove(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(deptId));
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}

}
