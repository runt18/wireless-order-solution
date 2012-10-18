package com.wireless.Actions.deptMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.menuMgr.MenuDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class UpdateDepartmentAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String deptID = request.getParameter("deptID");
			String deptName = request.getParameter("deptName");
			
			Department dept = new Department();
			dept.setRestaurantID(Integer.valueOf(restaurantID));
			dept.setDeptID(Integer.valueOf(deptID));
			dept.setDeptName(deptName);
			
			MenuDao.updateDepartment(dept);
			
			jobject.initTip(true, "操作成功,已修改部门信息.");
			
		}catch(BusinessException e) {
			e.printStackTrace();
			jobject.initTip(false, e.getMessage());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}

}
