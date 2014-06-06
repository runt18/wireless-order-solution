package com.wireless.Actions.billStatistics;

import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.ShiftGeneralDao;
import com.wireless.db.billStatistics.ShiftGeneralDao.ExtraCond;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.ShiftGeneral;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;
import com.wireless.util.WebParams;

public class DutyRangeStatisticsAction extends DispatchAction {
	
	/**
	 * 当日交班记录
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
		List<ShiftGeneral> list = null;
		try{
			String pin = (String)request.getAttribute("pin");
			list = ShiftGeneralDao.getToday(StaffDao.verify(Integer.parseInt(pin)));
		}catch(BusinessException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getMessage());
			e.printStackTrace();
			
		}catch(SQLException e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrorCode(), e.getMessage());
			e.printStackTrace();
			
		}finally{
			jobject.setRoot(list);
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	/**
	 * 历史交班记录
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
		List<ShiftGeneral> list = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = (String)request.getAttribute("pin");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			list = ShiftGeneralDao.getByRange(StaffDao.verify(Integer.parseInt(pin)), new ExtraCond(DateType.HISTORY).setRange(new DutyRange(onDuty, offDuty)));
			
		}catch(BusinessException e){	
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
		}catch(SQLException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrorCode(), e.getMessage());
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
