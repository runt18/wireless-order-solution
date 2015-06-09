package com.wireless.Actions.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.json.JObject;

public class LoginOutAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		JObject jobject = new JObject();
		try{
			HttpSession session = request.getSession();
			session.invalidate();
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		}
		finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
