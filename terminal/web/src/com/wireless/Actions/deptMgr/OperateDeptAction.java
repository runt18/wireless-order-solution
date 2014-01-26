package com.wireless.Actions.deptMgr;

import java.sql.SQLException;

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
import com.wireless.pojo.menuMgr.Department.SwapDisplayBuilder;
import com.wireless.pojo.staffMgr.Staff;

public class OperateDeptAction extends DispatchAction{

	public ActionForward swap(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		String deptA = request.getParameter("deptA");
		String deptB = request.getParameter("deptB");
		
		String pin = (String) request.getAttribute("pin");
		Staff staff = StaffDao.verify(Integer.parseInt(pin));
		
		Department.SwapDisplayBuilder builder = new SwapDisplayBuilder(Integer.parseInt(deptA), Integer.parseInt(deptB));
		
		JObject jobject = new JObject();
		try{
			DepartmentDao.swap(staff, builder);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	public ActionForward addDept(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String deptName = request.getParameter("deptName");
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			DepartmentDao.add(StaffDao.verify(Integer.parseInt(pin)), new AddBuilder(deptName));
			jobject.initTip(true, "添加部门成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	public ActionForward updateDept(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String deptId = request.getParameter("deptID");
		String deptName = request.getParameter("deptName");
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			DepartmentDao.update(StaffDao.verify(Integer.parseInt(pin)), 
					new Department.UpdateBuilder(Department.DeptId.valueOf(Integer.parseInt(deptId)), deptName));
			jobject.initTip(true, "修改部门成功");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
	public ActionForward removeDept(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String deptId = request.getParameter("deptID");
		String pin = (String) request.getAttribute("pin");
		JObject jobject = new JObject();
		try{
			DepartmentDao.remove(StaffDao.verify(Integer.parseInt(pin)), Integer.parseInt(deptId));
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}

}
