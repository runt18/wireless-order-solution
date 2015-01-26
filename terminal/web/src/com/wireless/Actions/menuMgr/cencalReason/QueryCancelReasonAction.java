package com.wireless.Actions.menuMgr.cencalReason;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.crMgr.CancelReason;

public class QueryCancelReasonAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		List<CancelReason> list = new ArrayList<CancelReason>();
		JObject jobject = new JObject();
		try{
			
			String pin = (String)request.getAttribute("pin");
			
			list = CancelReasonDao.get(StaffDao.verify(Integer.parseInt(pin)));
			list = (list != null ? list : new ArrayList<CancelReason>());
//			list.add(0, new CancelReason(1, "无原因", 0));
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			jobject.setRoot(list);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
