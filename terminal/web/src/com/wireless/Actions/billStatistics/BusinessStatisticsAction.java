package com.wireless.Actions.billStatistics;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.billStatistics.BusinessStatisticsDao;
import com.wireless.pojo.billStatistics.BusinessStatistics;
import com.wireless.util.DateType;
import com.wireless.util.JObject;
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		try{
			String pin = (String) request.getSession().getAttribute("pin");
			String restaurantID = request.getParameter("restaurantID");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			String queryPattern = request.getParameter("queryPattern");
			
			Map<Object, Object> params = new HashMap<Object, Object>();
			params.put(DateType.HISTORY, DateType.HISTORY.getValue());
			params.put("pin", pin);
			params.put("restaurantID", restaurantID);
			params.put("onDuty", onDuty);
			params.put("offDuty", offDuty);
			params.put("queryPattern", queryPattern);
			
			BusinessStatistics business = BusinessStatisticsDao.getBusinessStatistics(params);
			if(business != null){
				jobject.getOther().put("business", business);
			}else{
				jobject.initTip(false, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
			}
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
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
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		JObject jobject = new JObject();
		try{
			String pin = (String) request.getSession().getAttribute("pin");
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
			
			BusinessStatistics business = BusinessStatisticsDao.getBusinessStatistics(params);
			if(business != null){
				jobject.getOther().put("business", business);
			}else{
				jobject.initTip(false, WebParams.TIP_TITLE_DEFAULT, 1111, "操作成功, 该时间段没有记录, 请重新查询.");
			}
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
}
