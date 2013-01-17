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

import com.wireless.db.billStatistics.QueryCancelledFood;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.CancelledFood;
import com.wireless.util.JObject;

@SuppressWarnings({"rawtypes", "unchecked"})
public class QueryCancelledFoodAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setContentType("text/json; charset=utf-8");
		
		String pin = request.getParameter("pin");
		String limit = request.getParameter("limit");
		String start = request.getParameter("start");
		String qtype = request.getParameter("qtype");
		String otype = request.getParameter("otype");
		String beginDate = request.getParameter("beginDate");
		String endDate = request.getParameter("endDate");
		String deptID = request.getParameter("deptID");
		
		Integer ps = Integer.valueOf(limit), pi = Integer.valueOf(start); 
		Integer qt = Integer.valueOf(qtype), ot = Integer.valueOf(otype);
		
		JObject jobject = new JObject();
		CancelledFood[] bf = null;
		List<CancelledFood> list = new ArrayList();
		
		DutyRange queryDate = new DutyRange(beginDate+" 00:00:00", endDate+" 23:59:59");
		
		try{
			if(qt == QueryCancelledFood.QUERY_BY_DEPT){
				bf = QueryCancelledFood.getCancelledFoodByDept(Long.valueOf(pin), queryDate, ot);
			}else if(qt == QueryCancelledFood.QUERY_BY_REASON){
				
			}else if(qt == QueryCancelledFood.QUERY_BY_FOOD){
				bf = QueryCancelledFood.getCancelledFoodDetail(Long.valueOf(pin), queryDate, ot, deptID);
			}
		} catch(Exception e){
			System.out.println(e.getMessage());
		} finally{
			if(bf != null && bf.length > 0){				
				
				if(ps != null && pi != null){
					ps = (ps + pi) > bf.length ? (ps - ((ps + pi) - bf.length)) : ps;
					for(int i = 0; i < ps; i++){
						list.add(bf[pi+i]);
					}
				}else{
					for(int i = 0; i < bf.length; i++){
						list.add(bf[i]);
					}
				}
				
				CancelledFood sum = new CancelledFood("汇总", "汇总"), tp = null;
				float sumPrice = 0.00f, sumCount = 0.00f;
				for(int i = 0; i < bf.length; i++){
					tp = bf[i];
					sumCount += tp.getCount();
					sumPrice += tp.getTotalPrice();
				}				
				sum.setCount(sumCount);
				sum.setTotalPrice(sumPrice);				
				list.add(sum);
				
				jobject.setTotalProperty(bf.length);
				jobject.setRoot(list);
			}
			
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
			
		}
		
		return null;
	}

	
}
