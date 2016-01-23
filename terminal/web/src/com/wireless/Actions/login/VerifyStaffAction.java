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

public class VerifyStaffAction extends Action{

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		

		
		String pin = (String) request.getAttribute("pin");
		String code = request.getParameter("code");
		JObject jobject = new JObject();
		
		try{        
			StaffDao.verify(Integer.parseInt(pin), Code.valueOf(Integer.parseInt(code)));
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
	
}
