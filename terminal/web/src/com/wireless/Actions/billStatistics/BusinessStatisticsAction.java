package com.wireless.Actions.billStatistics;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.DutyRangeDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
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
	public ActionForward history(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JObject jObject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			String dutyRange = request.getParameter("dutyRange");
			final ShiftDetail sdetail;
			if(!dutyRange.equals("null") && !dutyRange.trim().isEmpty()){
				DutyRange range = DutyRangeDao.exec(staff, onDuty, offDuty);
				
				if(range != null){
					sdetail = ShiftDao.getByRange(staff, range, DateType.HISTORY);
				}else{
					sdetail = null;
					jObject.initTip(false, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
				}
			}else{
				sdetail = ShiftDao.getByRange(staff, new DutyRange(onDuty, offDuty), DateType.HISTORY);
			}
			
			jObject.setExtra(new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("business", sdetail, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}finally{
			
			response.getWriter().print(jObject.toString());
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
	public ActionForward today(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		
		
		JObject jObject = new JObject();
		try{
			String pin = (String)request.getAttribute("pin");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			final ShiftDetail sdetail = ShiftDao.getByRange(StaffDao.verify(Integer.parseInt(pin)), new DutyRange(onDuty, offDuty), DateType.TODAY);
			
			if(sdetail != null){
				jObject.setExtra(new Jsonable(){
					@Override
					public JsonMap toJsonMap(int flag) {
						JsonMap jm = new JsonMap();
						jm.putJsonable("business", sdetail, 0);
						return jm;
					}

					@Override
					public void fromJsonMap(JsonMap jsonMap, int flag) {
						
					}
					
				});
			}else{
				jObject.initTip(false, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
			}
			
		}catch(BusinessException e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}catch(Exception e){
			e.printStackTrace();
			jObject.initTip(e);
			
		}finally{
			response.getWriter().print(jObject.toString());
		}
		return null;
	}
	
}
