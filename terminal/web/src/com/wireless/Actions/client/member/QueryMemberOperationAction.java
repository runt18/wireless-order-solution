package com.wireless.Actions.client.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryMemberOperationAction extends Action{

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String isPaging = request.getParameter("isPaging");
		List<MemberOperation> list = null;
		try{
			
			String pin = (String)request.getAttribute("pin");
			Staff staff = StaffDao.verify(Integer.parseInt(pin));
			
			String restaurantID = request.getParameter("restaurantID");
			String dataSource = request.getParameter("dataSource");
			String memberMobile = request.getParameter("memberMobile");
			String memberCard = request.getParameter("memberCard");
			String memberType = request.getParameter("memberType");
			String operateType = request.getParameter("operateType");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			String extraCond = null, orderClause = null;
			extraCond = " AND MO.restaurant_id = " + restaurantID;
			
			if(memberMobile != null && !memberMobile.trim().isEmpty()){
				extraCond += (" AND MO.member_mobile like '%" + memberMobile.trim() + "%'");
			}
			if(memberCard != null && !memberCard.trim().isEmpty()){
				extraCond += (" AND MO.member_card like '%" + memberCard.trim() + "%'");
			}
			if(memberType != null && !memberType.trim().isEmpty()){
				extraCond += (" AND M.member_type_id = " + memberType);
			}
			if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
				extraCond += (" AND MO.operate_type = " + operateType);
			}
			
			orderClause = " ORDER BY MO.operate_date ";
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>(), countSet = null;
			if(isPaging != null && isPaging.trim().equals("true")){
				countSet = new HashMap<Object, Object>();
				if(DateType.getValue(dataSource) == DateType.TODAY.getValue()){
					countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
					countSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
					jobject.setTotalProperty(MemberOperationDao.getTodayCount(countSet));
				}else if(DateType.getValue(dataSource) == DateType.HISTORY.getValue()){
					if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
						extraCond += (" AND MO.operate_date >= '" + onDuty + "'");
						extraCond += (" AND MO.operate_date <= '" + offDuty + "'");
					}
					countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
					countSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
					jobject.setTotalProperty(MemberOperationDao.getHistoryCount(countSet));
				}
				paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, start);
				paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, limit);
			}
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
			if(DateType.getValue(dataSource) == DateType.TODAY.getValue()){
				list = MemberOperationDao.getToday(paramsSet);
			}else if(DateType.getValue(dataSource) == DateType.HISTORY.getValue()){
				list = MemberOperationDao.getHistory(paramsSet);
			}
			if(list != null){
				for(MemberOperation temp : list){
					temp.setMember(MemberDao.getMemberById(staff, temp.getMemberId()));
				}
			}
			jobject.setRoot(list);
			
		}catch(BusinessException e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getCode(), e.getDesc());
				
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(jobject.toString());
		}
		return null;
	}

}
