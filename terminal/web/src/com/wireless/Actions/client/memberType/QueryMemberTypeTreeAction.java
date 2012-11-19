package com.wireless.Actions.client.memberType;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.wireless.db.client.MemberDao;
import com.wireless.pojo.client.MemberType;

public class QueryMemberTypeTreeAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		StringBuffer tsb = new StringBuffer();
		
		try{
			String restaurantID = request.getParameter("restaurantID");
			
			List<MemberType> list = MemberDao.getMemberType((" AND A.restaurant_id = " + restaurantID), " ORDER BY A.member_type_id");
			MemberType item = null;
			
			tsb.append("[");
			for(int i = 0; i < list.size(); i++){
				item = list.get(i);
				tsb.append(i > 0 ? "," : "");
				tsb.append("{");
				tsb.append("text:'" + item.getName() + "'");
				tsb.append(",");
				tsb.append("leaf:true");
				tsb.append(",");
				tsb.append("MemberTypeID:" + item.getTypeID());
				tsb.append(",");
				tsb.append("MemberTypeName:'" + item.getName() + "'");
				tsb.append("}");
			}
			tsb.append("]");
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			response.getWriter().print(tsb.toString());
		}
		
		return null;
	}
}
