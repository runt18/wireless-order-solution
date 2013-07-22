package com.wireless.Actions.client.member;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.json.JObject;
import com.wireless.pojo.client.MOSummary;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.util.DateType;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryMemberOperationSummaryAction extends DispatchAction{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		List<MOSummary> list = null;
		JObject jobject = new JObject();
		try{
			String restaurantID = request.getParameter("restaurantID");
			String dataSource = request.getParameter("dataSource");
			String operateType = request.getParameter("operateType");
			String memberType = request.getParameter("memberType");
			String memberMobile = request.getParameter("memberMobile");
			String memberCard = request.getParameter("memberCard");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			String extra = "";
			if(restaurantID != null && !restaurantID.trim().isEmpty()){
				extra += (" AND MO.restaurant_id = " + restaurantID);
			}
			MemberOperation.OperationType ot = null;
			if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType.trim()) > 0){
				ot = MemberOperation.OperationType.valueOf(Integer.valueOf(operateType.trim()));
				extra += (" AND MO.operate_type = " + ot.getValue());
				
			}
			if(memberType != null && !memberType.trim().isEmpty()){
				extra += (" AND M.member_type_id = " + memberType);
			}
			if(memberMobile != null && !memberMobile.trim().isEmpty()){
				extra += (" AND M.mobile LIKE '%" + memberMobile.trim() + "%'");
			}
			if(memberCard != null && !memberCard.trim().isEmpty()){
				extra += (" AND M.member_card LIKE '%" + memberCard.trim() + "%'");
			}
			if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
				extra += (" AND MO.operate_date BETWEEN '"+onDuty+"' AND '"+offDuty+"'");
			}
			
			Map<Object, Object> params = new LinkedHashMap<Object, Object>();
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			if(DateType.getType(dataSource) != null && DateType.getType(dataSource).isHistory()){
				if(isPaging != null && Boolean.valueOf(isPaging)){
					jobject.setTotalProperty(MemberOperationDao.getSummaryByHistoryCount(params));
					
					params.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, Integer.valueOf(start));
					params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, Integer.valueOf(limit));
				}
				params.put(SQLUtil.SQL_PARAMS_ORDERBY, define(ot));
				list = MemberOperationDao.getSummaryByHistory(params);
			}else if(DateType.getType(dataSource).isToday()){
				if(isPaging != null && Boolean.valueOf(isPaging)){
					jobject.setTotalProperty(MemberOperationDao.getSummaryByTodayCount(params));
					
					params.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, Integer.valueOf(start));
					params.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, Integer.valueOf(limit));
				}
				params.put(SQLUtil.SQL_PARAMS_ORDERBY, define(ot));
				list = MemberOperationDao.getSummaryByToday(params);
			}
			
			jobject.setRoot(list);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, WebParams.TIP_CODE_ERROE, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}
	
	private String define(MemberOperation.OperationType ot){
		String orderby = "";
		if(ot == null){
			orderby = " ORDER BY MO.member_id ";
		}else if(ot == OperationType.CHARGE){
			orderby = " ORDER BY charge_money DESC ";
		}else if(ot == OperationType.CONSUME){
			orderby = " ORDER BY pay_money DESC ";
		}else if(ot == OperationType.POINT_CONSUME){
			orderby = " ORDER BY point_consume DESC ";
		}else if(ot == OperationType.POINT_ADJUST){
			orderby = " ORDER BY point_adjust DESC ";
		}else if(ot == OperationType.BALANCE_ADJUST){
			orderby = " ORDER BY money_adjust DESC ";
		}
		return orderby;
	}
}
