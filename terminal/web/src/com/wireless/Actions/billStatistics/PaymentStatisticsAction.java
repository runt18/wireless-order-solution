package com.wireless.Actions.billStatistics;

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
	public ActionForward today(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		JObject jobject = new JObject();
		List<PaymentGeneral> list = null;
		try{
			String pin = (String)request.getAttribute("pin");
			list = PaymentDao.getToday(StaffDao.verify(Integer.parseInt(pin)));
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			jobject.setRoot(list);
			response.getWriter().print(jobject.toString());
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
	public ActionForward history(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		
		JObject jobject = new JObject();
		List<PaymentGeneral> list = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = (String)request.getAttribute("pin");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			list = PaymentDao.getHistory(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(onDuty, offDuty));
			
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(e);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(e);
		}finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(DataPaging.getPagingData(list, isPaging, start, limit));
			}
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
