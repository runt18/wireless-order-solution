package com.wireless.Actions.dailySettle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.shift.PaymentDao;
import com.wireless.db.shift.ShiftDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.ShiftDetail;

public class QueryDailySettleByNowAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JObject jobject = new JObject();
		
		try{
			String pin = (String)request.getAttribute("pin");
			String queryType = request.getParameter("queryType");
			
			final ShiftDetail shiftDetail;
			if(Integer.valueOf(queryType) == 0){
				shiftDetail = ShiftDao.getCurrentShift(StaffDao.verify(Integer.parseInt(pin)));
			}else if(Integer.valueOf(queryType) == 1){
				shiftDetail = ShiftDao.getTodayDaily(StaffDao.verify(Integer.parseInt(pin)));
			}else if(Integer.valueOf(queryType) == 2){
				shiftDetail = PaymentDao.getCurrentPayment(StaffDao.verify(Integer.parseInt(pin)));
			}else{
				shiftDetail = null;
			}
			
			jobject.setExtra(new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("business", shiftDetail, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			});
		}catch(Exception e){
			jobject.initTip(e);
			e.printStackTrace();
		}finally{
			response.getWriter().print(jobject.toString());
		}
		
		return null;
	}

}
