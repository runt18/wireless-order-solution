package com.wireless.Actions.login;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;

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
				response.addCookie(cPin);
				Map<Object, Object> other = new HashMap<Object, Object>();
				other.put("staff", staff);
				jobject.setOther(other);
				jobject.initTip(true, "登陆成功");
			}else{
				jobject.initTip(false, "密码输入错误");
			}
			

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
