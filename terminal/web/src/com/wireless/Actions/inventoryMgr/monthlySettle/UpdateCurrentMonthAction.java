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
import com.wireless.pojo.stockMgr.MonthlyBalance;
import com.wireless.pojo.stockMgr.MonthlyBalance.InsertBuilder;

public class UpdateCurrentMonthAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			MonthlyBalance.InsertBuilder build = new InsertBuilder(staff.getRestaurantId(), staff.getName());

			MonthlyBalanceDao.insert(build, staff);
			
			jobject.initTip(true, "操作成功, 已经月结.");
		}catch(BusinessException e){
			jobject.initTip(e);
			e.printStackTrace();
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip4Exception(e);
		} finally {
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}
	
}
