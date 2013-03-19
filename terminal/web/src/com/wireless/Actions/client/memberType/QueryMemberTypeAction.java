package com.wireless.Actions.client.memberType;

import java.util.ArrayList;
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

import com.wireless.db.client.member.MemberTypeDao;
import com.wireless.pojo.client.MemberType;
import com.wireless.util.JObject;
import com.wireless.util.SQLUtil;
import com.wireless.util.WebParams;

public class QueryMemberTypeAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		JObject jobject = new JObject();
		List<MemberType> list = new ArrayList<MemberType>();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			String searchType = request.getParameter("searchType");
			String searchValue = request.getParameter("searchValue");
			
			searchValue = searchValue != null && !searchValue.trim().isEmpty() ? searchValue.trim() : null;
			
			String extraCond = (" AND A.restaurant_id = " + restaurantID);
			
			if(searchValue != null){
				if(searchType.equals("1")){
					extraCond += (" AND A.name like '%" + searchValue.trim() + "%' ");
				}else if(searchType.equals("2")){
					extraCond += (" AND A.discount_type = " + searchValue);
				}else if(searchType.equals("3")){
					extraCond += (" AND A.attribute = " + searchValue);
				}
			}
			
			Map<Object, Object> paramsSet = new HashMap<Object, Object>();
			paramsSet.put(SQLUtil.SQL_PARAMS_EXTRA, extraCond);
			paramsSet.put(SQLUtil.SQL_PARAMS_ORDERBY, " ORDER BY A.member_type_id ");
			list = MemberTypeDao.getMemberType(paramsSet);
		}catch(Exception e){
			e.printStackTrace();
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
		}finally{
			jobject.setTotalProperty(list.size());
			jobject.setRoot(list);
			
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		return null;
	}

}
