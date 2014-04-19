package com.wireless.Actions.smsMgr;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.sms.SMStatDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.sms.SMSDetail;

public class QuerySmsDetailAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String query = request.getParameter("query");
		String pin = (String) request.getAttribute("pin");
		String extra = "";
		JObject jobject = new JObject();
		try{
			if(query != null && Integer.parseInt(query) != 0){
				extra += " AND operation = " + query;
			}
			List<SMSDetail> list = SMStatDao.getDetails(StaffDao.verify(Integer.parseInt(pin)), extra, null);
			jobject.setRoot(list);
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}

}
