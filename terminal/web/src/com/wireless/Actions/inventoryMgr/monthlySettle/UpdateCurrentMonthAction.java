package com.wireless.Actions.inventoryMgr.monthlySettle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.stockMgr.MonthlyBalanceDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.staffMgr.Staff;

public class UpdateCurrentMonthAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final String pin = (String)request.getAttribute("pin");
		final Staff staff = StaffDao.verify(Integer.parseInt(pin));
		final JObject jObject = new JObject();
		
		try{
			MonthlyBalanceDao.insert(staff);
			jObject.initTip(true, "操作成功, 已经月结.");
		}catch(BusinessException e){
			jObject.initTip(e);
			e.printStackTrace();
		}finally {
			response.getWriter().print(jObject.toString());
		}
		
		return null;
	}
	
}
