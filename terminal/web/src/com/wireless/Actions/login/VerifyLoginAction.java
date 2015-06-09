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
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.staffMgr.Staff;

public class VerifyLoginAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, final HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		JObject jobject = new JObject();
		try{        
			String pin = (String) request.getSession().getAttribute("pin");
	
			if(pin != null){
				final Staff staff = StaffDao.verify(Integer.parseInt(pin));
				
				jobject.setExtra(new Jsonable(){

					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putJsonable("staff", staff, Staff.ST_PARCELABLE_COMPLEX);
						jm.putString("sessionId", request.getSession().getId());
						return jm;
					}

					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
					
				});
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
			jobject.initTip4Exception(e);
		}
		finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
		
	}
}
