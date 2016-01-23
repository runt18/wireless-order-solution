package com.wireless.Actions.login;

import java.sql.SQLException;

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

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		final JObject jObject = new JObject();
		try{        
			final String pin = (String) request.getSession().getAttribute("pin");
	
			if(pin != null){
				jObject.setRoot(StaffDao.verify(Integer.parseInt(pin)));
				jObject.initTip(true, "true");
			}else{
				jObject.initTip(false, "false");
			}
			
		}catch(BusinessException | SQLException e){
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}
		finally{
			response.getWriter().print(jObject.toString(Staff.ST_PARCELABLE_COMPLEX));
		}
		return null;
		
	}
}
