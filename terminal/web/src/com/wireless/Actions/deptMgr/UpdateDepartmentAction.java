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
		
		
		
		JObject jobject = new JObject();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String deptID = request.getParameter("deptID");
			String deptName = request.getParameter("deptName");
			
			Department dept = new Department(Short.valueOf(deptID));
			dept.setRestaurantId(Integer.valueOf(restaurantID));
			dept.setName(deptName);
			
			MenuDao.updateDepartment(dept);
			jobject.initTip(true, "操作成功, 已修改部门信息.");
		}catch(BusinessException e) {
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}

}
