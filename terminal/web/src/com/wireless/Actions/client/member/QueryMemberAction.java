package com.wireless.Actions.client.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.pojo.client.Member;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryMemberAction extends DispatchAction {
	
	/**
	 * 搜索
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward normal(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<Member> list = null;
		String isPaging = request.getParameter("isPaging");
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		try{
			String pin = (String)request.getAttribute("pin");
			StaffDao.verify(Integer.parseInt(pin));
			
			String extraCond = " ", orderClause = " ";
			String id = request.getParameter("id");
			String restaurantID = request.getParameter("restaurantID");
			String memberType = request.getParameter("memberType");
			String memberTypeAttr = request.getParameter("memberTypeAttr");
			String name = request.getParameter("name");
			String memberCard = request.getParameter("memberCard");
			String mobile = request.getParameter("mobile");
			String totalBalance = request.getParameter("totalBalance");
			String usedBalance = request.getParameter("usedBalance");
			String consumptionAmount = request.getParameter("consumptionAmount");
			String point = request.getParameter("point");
			String usedPoint = request.getParameter("usedPoint");
			String so = request.getParameter("so");
			
			if(id != null && !id.trim().isEmpty() && Integer.valueOf(id.trim()) > 0){
				extraCond += (" AND M.member_id = " + id);
			}else{
				if(so != null){
					so = so.trim();
					if(so.equals("0")){
						so = "=";
					}else if(so.equals("1")){
						so = ">=";
					}else if(so.equals("2")){
						so = "<=";
					}else{
						so = "=";
					}
				}else{
					so = "=";
				}
				
				if(restaurantID != null && !restaurantID.trim().isEmpty())
					extraCond += (" AND M.restaurant_id = " + restaurantID);
				if(memberType != null && !memberType.trim().isEmpty())
					extraCond += (" AND MT.member_type_id = " + memberType);
				
				if(memberTypeAttr != null && !memberTypeAttr.trim().isEmpty())
					extraCond += (" AND MT.attribute = " + memberTypeAttr);
				
				if(name != null && !name.trim().isEmpty())
					extraCond += (" AND M.name like '%" + name.trim() + "%'");
				
				if(memberCard != null && !memberCard.trim().isEmpty())
					extraCond += (" AND M.member_card like '%" + memberCard.trim() + "%'");
				
				if(mobile != null && !mobile.trim().isEmpty())
					extraCond += (" AND M.mobile like '%" + mobile.trim() + "%'");
					
				if(totalBalance != null && !totalBalance.trim().isEmpty())
					extraCond += (" AND (M.base_balance + M.extra_balance) " + so + totalBalance);
				
				if(usedBalance != null && !usedBalance.trim().isEmpty())
					extraCond += (" AND M.used_balance " + so + usedBalance);
				
				if(consumptionAmount != null && !consumptionAmount.trim().isEmpty())
					extraCond += (" AND M.consumption_amount " + so + consumptionAmount);
				
				if(usedPoint != null && !usedPoint.trim().isEmpty())
					extraCond += (" AND M.used_point " + so + usedPoint);
				
				if(point != null && !point.trim().isEmpty())
					extraCond += (" AND M.point " + so + point);
			}
			
			orderClause = " ORDER BY M.member_id ";
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>(), countSet = null;
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
			if(isPaging != null && isPaging.trim().equals("true")){
				countSet = new HashMap<Object, Object>();
				countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
				jobject.setTotalProperty(MemberDao.getMemberCount(countSet));
				// 分页
				paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, start);
				paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, limit);
			}
			list = MemberDao.getMember(paramsSet);
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
