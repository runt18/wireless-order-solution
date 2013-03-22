package com.wireless.Actions.client.member;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.wireless.db.client.member.MemberDao;
import com.wireless.pojo.client.Member;
import com.wireless.util.DataPaging;
import com.wireless.util.JObject;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryMemberAction extends DispatchAction {
	
	/**
	 * 普通搜索
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
		String extraCond = null, orderClause = null;
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String isPaging = request.getParameter("isPaging");
		String restaurantID = request.getParameter("restaurantID");
		String params = request.getParameter("params");
		JSONObject jp = JSONObject.fromObject(params);
		try{
			extraCond = " AND A.restaurant_id = " + restaurantID;
			int searchType = jp.getInt("searchType");
			String searchValue = jp.getString("searchValue");
			int searchOperation = jp.getInt("searchOperation");
			String memberType = jp.get("searcheMemberType") != null ? jp.getString("searcheMemberType") : null;
			String cardStatus = jp.get("cardStatus") != null ? jp.getString("cardStatus") : null;
			String so;
			
			if(memberType != null && !memberType.trim().isEmpty()){
				extraCond = " AND B.member_type_id = " + memberType;
			}
			if(cardStatus != null && !cardStatus.trim().isEmpty()){
				extraCond = " AND A.status = " + cardStatus;
			}
			if(searchValue != null && !searchValue.trim().isEmpty()){
				if(searchOperation == 0){
					so = "=";
				}else if(searchOperation == 1){
					so = ">=";
				}else if(searchOperation == 2){
					so = "<=";
				}else{
					so = "=";
				}
				if(searchType == 1){
					extraCond += (" AND E.name like '%" + searchValue + "%' ");
				}else if(searchType == 2){
					extraCond += (" AND C.member_card_alias " + so + " " + searchValue);
				}else if(searchType == 3){
					extraCond += (" HAVING totalBalance " + so + " " + searchValue);
				}else if(searchType == 4){
					extraCond += (" AND A.point " + so + " " + searchValue);
				}else if(searchType == 5){
					extraCond += (" AND A.status = " + searchValue);
				}
			}
			orderClause = " ORDER BY A.member_id ";
			Map<Object, Object> paramsSet = new HashMap<Object, Object>();
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
			list = MemberDao.getMember(paramsSet);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(DataPaging.getPagingData(list, isPaging, start, limit));
			}
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}
	
	/**
	 * 高级搜索
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return
	 * @throws Exception
	 */
	public ActionForward adv(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<Member> list = null;
		String start = request.getParameter("start");
		String limit = request.getParameter("limit");
		String isPaging = request.getParameter("isPaging");
		try{
			String extraCond = " ", orderClause = " ";
			String restaurantID = request.getParameter("restaurantID");
			String memberType = request.getParameter("memberType");
			String memberName = request.getParameter("memberName");
			String memberCardAlias = request.getParameter("memberCardAlias");
			String memberCardStatus = request.getParameter("memberCardStatus");
			String totalBalance = request.getParameter("totalBalance");
			String point = request.getParameter("point");
			String so = request.getParameter("so");
			
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
				extraCond += (" AND A.restaurant_id = " + restaurantID);
			
			if(memberType != null && !memberType.trim().isEmpty())
				extraCond += (" AND B.member_type_id = " + memberType);
			
			if(memberName != null && !memberName.trim().isEmpty())
				extraCond += (" AND E.name like '%" + memberName.trim() + "&'");
			
			if(memberCardStatus != null && !memberCardStatus.trim().isEmpty())
				extraCond += (" AND A.status = " + memberCardStatus);
			
			if(memberCardAlias != null && !memberCardAlias.trim().isEmpty())
				extraCond += (" AND C.member_card_alias like '%" + memberCardAlias.trim() + "%'");
			
			if(totalBalance != null && !totalBalance.trim().isEmpty())
				extraCond += (" HAVING totalBalance " + so + " " + totalBalance);
			
			if(point != null && !point.trim().isEmpty())
				extraCond += (" HAVING A.point " + so + " " + point);
			
			orderClause = " ORDER BY A.member_id ";
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>(), countSet = null;
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
			if(isPaging != null && isPaging.trim().equals("true")){
				countSet = new HashMap<Object, Object>();
				countSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
				countSet.put(SQLUtil.SQL_PARAMS_ORDERBY, orderClause);
				jobject.setTotalProperty(MemberDao.getMemberCount(countSet));
				// 分页
				paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_OFFSET, start);
				paramsSet.put(SQLUtil.SQL_PARAMS_LIMIT_ROWCOUNT, limit);
			}
			list = MemberDao.getMember(paramsSet);
			jobject.setRoot(list);
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
