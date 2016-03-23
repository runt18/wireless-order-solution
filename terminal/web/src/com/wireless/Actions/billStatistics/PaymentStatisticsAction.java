package com.wireless.Actions.billStatistics;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.shift.PaymentDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.PaymentGeneral;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DataPaging;

public class PaymentStatisticsAction extends DispatchAction{

	/**
	 * 当日交款记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward today(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		try{
			jObject.setRoot(PaymentDao.getToday(StaffDao.verify(Integer.parseInt(pin))));
		}catch(BusinessException | SQLException e){
			jObject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
	
	
	/**
	 * 历史交款记录
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward history(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		final JObject jObject = new JObject();
		final String pin = (String)request.getAttribute("pin");
		final String onDuty = request.getParameter("onDuty");
		final String offDuty = request.getParameter("offDuty");
		final String branchId = request.getParameter("branchId");
		final String isPaging = request.getParameter("isPaging");
		final String start = request.getParameter("start");
		final String limit = request.getParameter("limit");
		
		try{
			
			final Staff staff;
			if(branchId != null && !branchId.isEmpty()){
				staff = StaffDao.getAdminByRestaurant(Integer.parseInt(branchId));
			}else{
				staff = StaffDao.verify(Integer.parseInt(pin));
			}
			
			final List<PaymentGeneral> list = PaymentDao.getHistory(staff, new DutyRange(onDuty, offDuty));
			if(list != null){
				jObject.setTotalProperty(list.size());
				jObject.setRoot(DataPaging.getPagingData(list, isPaging, start, limit));
			}
		}catch(BusinessException | SQLException e){	
			e.printStackTrace();
			jObject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip4Exception(e);
		}finally{

			response.getWriter().print(jObject.toString());
		}
		return null;
	}

}
