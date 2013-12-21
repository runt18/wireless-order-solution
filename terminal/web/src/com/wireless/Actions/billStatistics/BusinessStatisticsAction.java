package com.wireless.Actions.billStatistics;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.QueryDutyRange;
import com.wireless.db.shift.QueryShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.billStatistics.ShiftDetail;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;
import com.wireless.util.WebParams;

public class BusinessStatisticsAction extends DispatchAction {
	
	/**
	 * history
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
		try{
			String pin = (String)request.getAttribute("pin");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			String dutyRange = request.getParameter("dutyRange");
			ShiftDetail sdetail = new ShiftDetail();
			if(!dutyRange.equals("null") && !dutyRange.trim().isEmpty()){
				DutyRange range = QueryDutyRange.exec(staff, onDuty, offDuty);
				
				if(range != null){
					sdetail = QueryShiftDao.exec(staff, range.getOnDutyFormat(), range.getOffDutyFormat(), DateType.HISTORY);
					
				}else{
					jobject.initTip(false, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
				}
			}else{
				sdetail = QueryShiftDao.exec(staff, onDuty, offDuty, DateType.HISTORY);
			}
			jobject.getOther().put("business", sdetail);
		}catch(BusinessException e){
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
	
	/**
	 * today
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
		try{
			String pin = (String)request.getAttribute("pin");
			String restaurantID = request.getParameter("restaurantID");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			String queryPattern = request.getParameter("queryPattern");
			
			Map<Object, Object> params = new HashMap<Object, Object>();
			params.put(DateType.TODAY, DateType.TODAY.getValue());
			params.put("pin", pin);
			params.put("restaurantID", restaurantID);
			params.put("onDuty", onDuty);
			params.put("offDuty", offDuty);
			params.put("queryPattern", queryPattern);
			
			ShiftDetail sdetail = QueryShiftDao.exec(StaffDao.verify(Integer.parseInt(pin)), onDuty, offDuty, DateType.TODAY);
			
			if(sdetail != null){
				jobject.getOther().put("business", sdetail);
			}else{
				jobject.initTip(false, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
			}
			
		}catch(BusinessException e){
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
