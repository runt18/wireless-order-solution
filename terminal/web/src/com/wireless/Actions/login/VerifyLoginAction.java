package com.wireless.Actions.login;

import java.util.HashMap;
import java.util.Map;

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

public class VerifyLoginAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		JObject jobject = new JObject();
		Map<Object, Object> other = new HashMap<Object, Object>();
		try{        
/*			Cookie[] cookies = request.getCookies();
			if(cookies != null){
				for (Cookie cookie : cookies) {
					if(cookie.getName().equals("pin")){
						Staff staff = StaffDao.verify(Integer.parseInt(cookie.getValue()));
						
						other.put("staff", staff);
						jobject.setOther(other);
						jobject.initTip(true, "true");
						break;
					}
				}
				if(other.isEmpty()){
					jobject.initTip(false, "false");
				}
			}*/
			String pin = (String) request.getSession().getAttribute("pin");
			if(pin != null){
				Staff staff = StaffDao.verify(Integer.parseInt(pin));
				
				other.put("staff", staff);
				jobject.setOther(other);
				jobject.initTip(true, "true");
			}
			else{
				jobject.initTip(false, "false");
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}
		finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
}
