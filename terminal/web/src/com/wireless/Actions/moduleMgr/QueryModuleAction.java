package com.wireless.Actions.moduleMgr;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.restaurantMgr.ModuleDao;
import com.wireless.db.sms.SMStatDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.pojo.restaurantMgr.Module;
import com.wireless.pojo.sms.SMStat;

public class QueryModuleAction extends DispatchAction{

	public ActionForward checkModule(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String pin = (String) request.getAttribute("pin");
		String restaurnatId = (String) request.getAttribute("restaurantID");
		String code = request.getParameter("code");
		JObject jobject = new JObject();
		try{
			Boolean b = ModuleDao.checkModule(Integer.parseInt(restaurnatId), Module.Code.valueOf(Integer.parseInt(code)));
			SMStat sms = SMStatDao.get(StaffDao.verify(Integer.parseInt(pin)));
			if(b){
				jobject.initTip(true, sms.getRemaining(), "");
			}else{
				jobject.initTip(false, "");
			}
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
