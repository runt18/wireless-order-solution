package com.wireless.Actions.client.member;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.util.DataPaging;
import com.wireless.util.DateType;
import com.wireless.util.JObject;
import com.wireless.util.WebParams;

public class QueryMemberConsumeSummaryAction extends Action{
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		List<MemberOperation> list = null;
		JObject jobject = new JObject();
		try{
			String restaurantID = request.getParameter("restaurantID");
			String dataSource = request.getParameter("dataSource");
			String operateType = request.getParameter("operateType");
			String memberCard = request.getParameter("memberCard");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			MemberOperation.OperationType ot = null;
			DutyRange duty = null;
			if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType.trim()) > 0){
				ot = MemberOperation.OperationType.valueOf(Integer.valueOf(operateType));
			}
			if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
				duty = new DutyRange(onDuty.trim(), offDuty.trim());
			}
			
			if(DateType.getType(dataSource) == null || DateType.getType(dataSource).isToday()){
				list = MemberOperationDao.getMemberConsumeSummaryByToday(Integer.valueOf(restaurantID), ot, memberCard);
			}else if(DateType.getType(dataSource).isHistory()){
				list = MemberOperationDao.getMemberConsumeSummaryByHistory(Integer.valueOf(restaurantID), duty, ot, memberCard);
			}
			
			if(list != null && !list.isEmpty()){
				jobject.setTotalProperty(list.size());
				list = DataPaging.getPagingData(list, isPaging, start, limit);
				for(MemberOperation temp : list){
					temp.setMember(MemberDao.getMemberById(temp.getMemberID()));
				}
				jobject.setRoot(list);
			}
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(JSONObject.fromObject(jobject).toString());
		}
		return null;
	}

}
