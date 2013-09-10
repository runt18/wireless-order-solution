package com.wireless.Actions.login;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class VerifyLoginAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		
		try{        
			Cookie[] cookies = request.getCookies();
			if(cookies != null){
				for (Cookie cookie : cookies) {
					if(cookie.getName().equalsIgnoreCase("pin")){
						Staff staff = StaffDao.verify(Integer.parseInt(cookie.getValue()));
						Map<Object, Object> other = new HashMap<Object, Object>();
						other.put("staff", staff);
						jobject.setOther(other);
						jobject.initTip(true, "true");
						break;
					}else{
						jobject.initTip(false, "false");
					}
				}
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_DEFAULT, e.getCode(), e.getMessage());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}
		finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
}
