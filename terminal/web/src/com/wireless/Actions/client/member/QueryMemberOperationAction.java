package com.wireless.Actions.client.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.client.member.MemberOperationDao;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.util.DataType;
import com.wireless.util.JObject;
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
			String restaurantID = request.getParameter("restaurantID");
			String dataSource = request.getParameter("dataSource");
			String memberCard = request.getParameter("memberCard");
			String memberType = request.getParameter("memberType");
			String operateType = request.getParameter("operateType");
			String onDuty = request.getParameter("onDuty");
			String offDuty = request.getParameter("offDuty");
			
			String extraCond = null, orderClause = null;
			extraCond = " AND A.restaurant_id = " + restaurantID;
			
			if(memberCard != null && !memberCard.trim().isEmpty()){
				extraCond += (" AND A.member_card_alias like '%" + memberCard + "%'");
			}
			if(memberType != null && !memberType.trim().isEmpty()){
				extraCond += (" AND B.member_type_id = " + memberType);
			}
			if(operateType != null && !operateType.trim().isEmpty() && Integer.valueOf(operateType) > 0){
				extraCond += (" AND A.operate_type = " + operateType);
			}else{
				extraCond += (" AND A.operate_type in ("
						+ MemberOperation.OperationType.CHARGE.getValue()
						+ "," 
						+ MemberOperation.OperationType.CONSUME.getValue()
//						+ "," 
//						+ MemberOperation.OperationType.EXCHANGE.getValue()
						+ ")");
			}
			
			orderClause = " ORDER BY A.operate_date ";
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>(), countSet = null;
			if(isPaging != null && isPaging.trim().equals("true")){
				countSet = new HashMap<Object, Object>();
				if(DataType.getValue(dataSource) == DataType.TODAY.getValue()){
					countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
					countSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
					jobject.setTotalProperty(MemberOperationDao.getTodayCount(countSet));
				}else if(DataType.getValue(dataSource) == DataType.HISTORY.getValue()){
					if(onDuty != null && !onDuty.trim().isEmpty() && offDuty != null && !offDuty.trim().isEmpty()){
						extraCond += (" AND A.operate_date >= '" + onDuty + "'");
						extraCond += (" AND A.operate_date <= '" + offDuty + "'");
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
			if(DataType.getValue(dataSource) == DataType.TODAY.getValue()){
				list = MemberOperationDao.getToday(paramsSet);
			}else if(DataType.getValue(dataSource) == DataType.HISTORY.getValue()){
				list = MemberOperationDao.getHistory(paramsSet);
			}
			if(list != null){
				for(MemberOperation temp : list){
					temp.setMember(MemberDao.getMemberById(temp.getMemberID()));
				}
			}
			jobject.setRoot(list);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			response.getWriter().print(JSONObject.fromObject(jobject).toString());
		}
		return null;
	}

}
