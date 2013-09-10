package com.wireless.Actions.login;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.json.JObject;

public class VerifyLoginAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		
		try{        
			Cookie[] cookies = request.getCookies();
			if(cookies != null){
				for (Cookie cookie : cookies) {
					if(cookie.getName().equalsIgnoreCase("pin")){
						jobject.initTip(true, "true");
						break;
					}else{
						jobject.initTip(false, "false");
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
}
