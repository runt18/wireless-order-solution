package com.wireless.Actions.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class OperateStaffAction extends Action{
	public ActionForward execute(ActionMapping mapping, ActionForm form,HttpServletRequest request, HttpServletResponse response)throws Exception {
				
		String pin = (String)request.getParameter("pin");
		String restaurantID = request.getParameter("restaurantID");
		try{
			HttpSession session = request.getSession();
			session.setAttribute("pin", pin);
			session.setAttribute("restaurantID", restaurantID);
			
			//session.put("staffId", pin); 
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	
}
