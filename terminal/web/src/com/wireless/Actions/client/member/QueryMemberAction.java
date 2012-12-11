package com.wireless.Actions.client.member;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.MemberDao;
import com.wireless.pojo.client.Member;
import com.wireless.util.JObject;
import com.wireless.util.PagingData;
import com.wireless.util.WebParams;

public class QueryMemberAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
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
			String memberType = jp.getString("searcheMemberType");
			String so;
			
			if(memberType != null && !memberType.trim().isEmpty()){
				extraCond = " AND B.member_type_id = " + memberType;
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
			
			list = MemberDao.getMember(extraCond, orderClause);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			if(list != null){
				jobject.setTotalProperty(list.size());
				jobject.setRoot(PagingData.getPagingData(list, isPaging, start, limit));
			}
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}

}
