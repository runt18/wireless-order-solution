package com.wireless.Actions.supplierMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

public class TestAction extends Action{
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		
/*		System.out.println("jin");
		String data = "restautantID=39";
		String key = "mi";
		
		ActionForward forward = new ActionForward("/pages/Stock_Module/Test.html?"+Encrypt.strEncode(data, key, null, null));
		
		ActionForward forwards = new ActionForward();
		//forward.setPath(path)
		return forward; 
*/
		System.out.println(request.getSession().getId());
		return null;
	}
}
