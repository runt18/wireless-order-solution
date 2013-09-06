package com.wireless.Actions.login;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class OperateStaffAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)throws Exception {
				
		String pin = request.getParameter("pin");
		String pwd = request.getParameter("pwd");
		JObject jobject = new JObject();
		try{
			response.setContentType("text/json; charset=utf-8");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			if(staff.getPwd().equalsIgnoreCase(pwd)){
				HttpSession session = request.getSession();
				session.setAttribute("pin", pin);
				
				Cookie cPin = new Cookie("pin", pin);
				cPin.setMaxAge(24*60*60);
				response.addCookie(cPin);
			}else{
				throw new BusinessException("密码输入错误!");
			}
			

		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_DEFAULT, e.getCode(), e.getMessage());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, e.getMessage(), 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

	
}
