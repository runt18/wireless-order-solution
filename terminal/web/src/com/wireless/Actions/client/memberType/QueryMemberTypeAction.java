package com.wireless.Actions.client.memberType;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.MemberDao;
import com.wireless.pojo.client.MemberType;
import com.wireless.util.JObject;
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
			
			list = MemberDao.getMemberType(extraCond, null);
			
		}catch(Exception e){
			jobject.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, "操作失败, 数据库操作请求发生错误!");
			e.printStackTrace();
		}finally{
			jobject.setTotalProperty(list.size());
			jobject.setRoot(list);
			
			JSONObject json = JSONObject.fromObject(jobject);
			response.getWriter().print(json.toString());
		}
		
		return null;
	}

}
