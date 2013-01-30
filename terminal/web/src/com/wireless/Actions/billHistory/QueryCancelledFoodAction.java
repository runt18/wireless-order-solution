package com.wireless.Actions.billHistory;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.VerifyPin;
import com.wireless.db.billStatistics.QueryCancelledFood;
import com.wireless.pojo.billStatistics.CancelIncomeByDept;
import com.wireless.pojo.billStatistics.CancelIncomeByReason;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.protocol.Terminal;
import com.wireless.util.DataPaging;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryCancelledFoodAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/json; charset=utf-8");
		JObject jobject = new JObject();
		List list = new ArrayList();
		
		String isPaging = request.getParameter("isPaging");
		String limit = request.getParameter("limit");
		String start = request.getParameter("start");
		
		try{
			String pin = request.getParameter("pin");
			String qtype = request.getParameter("qtype");
			String otype = request.getParameter("otype");
			String dtype = request.getParameter("dtype");
			String dateBeg = request.getParameter("dateBeg");
			String dateEnd = request.getParameter("dateEnd");
			String deptID = request.getParameter("deptID");
			String reasonID = request.getParameter("reasonID");
			
			if(dtype == null || dtype.trim().isEmpty()){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, WebParams.TIP_CODE_ERROE, "操作失败, 请指定查询当日数据或历史数据.");
				return null;
			}
			if(qtype == null || qtype.trim().isEmpty()){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, WebParams.TIP_CODE_ERROE, "操作失败, 请指定统计数据来源.");
				return null;
			}
			if(dateBeg == null || dateBeg.trim().isEmpty()){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, WebParams.TIP_CODE_ERROE, "操作失败, 请指定统计日期开始时间.");
				return null;
			}
			if(dateEnd == null || dateEnd.trim().isEmpty()){
				jobject.initTip(false, WebParams.TIP_TITLE_ERROE, WebParams.TIP_CODE_ERROE, "操作失败, 请指定统计日期结束时间.");
				return null;
			}
			if(otype == null || otype.trim().isEmpty()){
				otype = QueryCancelledFood.ORDER_BY_COUNT + "";
			}
			if(deptID == null || deptID.trim().isEmpty()){
				deptID = "-1";
			}
			if(reasonID == null || reasonID.trim().isEmpty()){
				reasonID = "-1";
			}
			Integer qt = Integer.valueOf(qtype), ot = Integer.valueOf(otype), dt = Integer.valueOf(dtype);
			Integer did = Integer.valueOf(deptID), rid = Integer.valueOf(reasonID);
			
			DutyRange queryDate = new DutyRange(dateBeg, dateEnd);
			Terminal terminal = VerifyPin.exec(Long.parseLong(pin), Terminal.MODEL_STAFF);
			
			if(qt == QueryCancelledFood.QUERY_BY_DEPT){
				CancelIncomeByDept dept = QueryCancelledFood.getCancelledFoodByDept(terminal, queryDate, did, dt, ot);
				if(dept != null){
					list = dept.getIncomeByEachReason();
					jobject.getOther().put("dept", dept);					
				}
			}else if(qt == QueryCancelledFood.QUERY_BY_REASON){
				CancelIncomeByReason reason = QueryCancelledFood.getCancelledFoodByReason(terminal, queryDate, rid, dt, ot);
				if(reason != null){
					list = reason.getIncomeByEachDept();
					jobject.getOther().put("reason", reason);					
				}
			}else if(qt == QueryCancelledFood.QUERY_BY_FOOD){
				list = QueryCancelledFood.getCancelledFoodDetail(terminal, queryDate, dt, ot, did, rid);
			}
		} catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		} finally{
			if(list != null && list.size() > 0){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(DataPaging.getPagingData(list, isPaging, start, limit));
			}
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
			
		}
		
		return null;
	}

	
}
