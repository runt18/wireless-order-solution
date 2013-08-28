package com.wireless.Actions.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Privilege.Code;
import com.wireless.util.WebParams;

public class VerifyStaffAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		

		response.setContentType("text/json; charset=utf-8");
		String pin = (String) request.getAttribute("pin");
		String code = request.getParameter("code");
		JObject jobject = new JObject();
		
		try{        
			StaffDao.verify(Integer.parseInt(pin), Code.valueOf(Integer.parseInt(code)));
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_DEFAULT, e.getMessage());
		}
		finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
	
}
