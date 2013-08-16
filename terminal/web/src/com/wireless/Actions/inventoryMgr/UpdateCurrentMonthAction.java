package com.wireless.Actions.inventoryMgr;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.system.SystemDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.WebParams;

public class UpdateCurrentMonthAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		try{
			String pin = (String) request.getSession().getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin), Privilege.Code.INVENTORY);
			SystemDao.updateCurrentMonth(staff);
			jobject.initTip(true, "操作成功, 已月结.");
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
}
